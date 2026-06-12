# -*- coding: utf-8 -*-
"""Rooms multijoueur en mémoire : état joueurs, points de manche, fin de partie, ELO."""
import asyncio, random, string, time
import semantic, elo, db, bots

END_TIMER = 30          # secondes après le 1er mot exact
COOLDOWN = 1.5          # anti-spam entre 2 essais
SPEED_BONUS = {70: 5, 85: 10, 95: 20}
EXACT_WIN_BONUS = 1000  # garantit que le gagnant exact est devant

ROOMS = {}

def round_points(temp: float, exact: bool) -> int:
    if exact: return 100
    if temp >= 95: return 15
    if temp >= 85: return 7
    if temp >= 70: return 3
    if temp >= 50: return 1
    return 0

class Player:
    def __init__(self, uid, username, is_bot=False):
        self.id = uid; self.username = username
        self.ws = None; self.is_bot = is_bot
        self.attempts = 0; self.invalid = 0
        self.max_temp = -100.0; self.points = 0; self.speed_bonus = 0
        self.found_at = None; self.last_guess = 0.0
        self.words = set()

class Room:
    def __init__(self, code, ranked, host_id, secret_index=None):
        self.code = code; self.ranked = ranked; self.host_id = host_id
        self.secret_index = secret_index if secret_index is not None \
            else random.randrange(semantic.secret_count())
        self.players = {}
        self.state = "lobby"   # lobby | playing | ending | done
        self.started_at = None; self.ends_at = None
        self.milestones_taken = set()
        self._end_task = None
        self._bot_tasks = []

    async def broadcast(self, msg: dict):
        for p in list(self.players.values()):
            if p.ws is not None:
                try: await p.ws.send_json(msg)
                except Exception: p.ws = None

    async def _send(self, p, msg: dict):
        """Envoi privé : silencieux pour les bots (pas de WebSocket)."""
        if p.ws is not None:
            try: await p.ws.send_json(msg)
            except Exception: p.ws = None

    def players_payload(self):
        return [{"id": p.id, "username": p.username, "maxTemp": p.max_temp,
                 "points": p.points, "attempts": p.attempts,
                 "found": p.found_at is not None} for p in self.players.values()]

    async def start(self):
        self.state = "playing"
        self.started_at = time.time()
        await self.broadcast({"type": "game_started", "players": self.players_payload()})
        for p in self.players.values():
            if p.is_bot:
                self._bot_tasks.append(asyncio.create_task(bots.bot_loop(self, p)))

    async def handle_guess(self, uid: str, word: str):
        p = self.players.get(uid)
        if p is None or self.state not in ("playing", "ending"): return
        now = time.time()
        if now - p.last_guess < COOLDOWN:
            await self._send(p, {"type": "guess_rejected", "reason": "cooldown"})
            return
        p.last_guess = now
        norm = word.strip().lower()
        if norm in p.words:
            await self._send(p, {"type": "guess_rejected", "reason": "déjà proposé"})
            return
        p.words.add(norm)
        p.attempts += 1
        r = semantic.guess(self.secret_index, word)
        if not r["recognized"]:
            p.invalid += 1
        # résultat privé au joueur
        await self._send(p, {"type": "guess_result", "word": word, **r})
        new_best = r["temp"] > p.max_temp
        if new_best: p.max_temp = r["temp"]
        if r["recognized"]:
            p.points += round_points(r["temp"], r["exact"])
            for seuil, bonus in SPEED_BONUS.items():
                if r["temp"] >= seuil and seuil not in self.milestones_taken:
                    self.milestones_taken.add(seuil)
                    p.speed_bonus += bonus
                    await self.broadcast({"type": "milestone", "username": p.username, "temp": seuil})
        if new_best and r["temp"] >= 50:
            await self.broadcast({"type": "player_progress", "username": p.username,
                                  "maxTemp": p.max_temp})
        await self.broadcast({"type": "players", "players": self.players_payload()})
        if r["exact"] and p.found_at is None:
            p.found_at = now
            if self.state == "playing":
                self.state = "ending"
                self.ends_at = now + END_TIMER
                await self.broadcast({"type": "endgame_started", "winner": p.username,
                                      "secondsLeft": END_TIMER})
                self._end_task = asyncio.create_task(self._finish_later())

    async def _finish_later(self):
        await asyncio.sleep(END_TIMER)
        await self.finish()

    async def finish(self):
        if self.state == "done": return
        self.state = "done"
        results = []
        avg_attempts = max(1.0, sum(p.attempts for p in self.players.values()) / max(1, len(self.players)))
        for p in self.players.values():
            spam_penalty = max(0, int((p.attempts - 2 * avg_attempts) * 2)) + (p.invalid // 5) * 2
            perf = ((EXACT_WIN_BONUS if p.found_at else 0) + p.points
                    + int(max(0, p.max_temp) * 2) + p.speed_bonus - spam_penalty)
            speed = -(p.found_at or 0)  # départage : trouvé plus tôt = mieux
            results.append({"player": p, "perf": perf, "speed": speed})
        results.sort(key=lambda r: (r["player"].found_at is not None, r["perf"], r["speed"]), reverse=True)
        deltas = {}
        if self.ranked and len(self.players) >= 2:
            users = {p.id: db.get_user(p.id) for p in self.players.values()}
            elo_in = [{"id": r["player"].id, "rating": users[r["player"].id]["rating"],
                       "games": users[r["player"].id]["games"], "score": r["perf"],
                       "found": r["player"].found_at is not None} for r in results]
            deltas = elo.compute_deltas(elo_in)
        rows = []
        payload = []
        for pos, r in enumerate(results, 1):
            p = r["player"]
            d = deltas.get(p.id, 0)
            rows.append({"user_id": p.id, "perf_score": r["perf"], "max_temp": p.max_temp,
                         "found": p.found_at is not None, "elo_delta": d, "final_pos": pos})
            payload.append({"pos": pos, "username": p.username, "perfScore": r["perf"],
                            "maxTemp": p.max_temp, "points": p.points, "found": p.found_at is not None,
                            "eloDelta": d, "attempts": p.attempts})
        if len(self.players) >= 1:
            db.save_match("ranked" if self.ranked else "casual", self.secret_index, rows)
        await self.broadcast({"type": "match_end", "secret": semantic.secret_word(self.secret_index),
                              "results": payload})
        ROOMS.pop(self.code, None)
        cur = asyncio.current_task()
        for t in self._bot_tasks:
            if t is not cur:
                t.cancel()
        self._bot_tasks.clear()

def create_room(ranked: bool, host_id: str, secret_index=None) -> str:
    while True:
        code = "".join(random.choices(string.ascii_uppercase + string.digits, k=5))
        if code not in ROOMS: break
    ROOMS[code] = Room(code, ranked, host_id, secret_index)
    return code

def add_bot(room: Room) -> Player:
    """Ajoute un bot (compte persistant) à une room en lobby."""
    used = {p.username for p in room.players.values()}
    candidates = [n for n in bots.BOT_NAMES if n not in used] or bots.BOT_NAMES
    u = bots.get_bot_user(random.choice(candidates))
    p = Player(u["id"], u["username"], is_bot=True)
    room.players[u["id"]] = p
    return p

def remove_standby_bots(room: Room):
    """Retire les bots d'appoint dès que la salle a assez de joueurs humains."""
    if room.state != "lobby":
        return
    humans = [p for p in room.players.values() if not p.is_bot]
    if len(humans) < 2:
        return
    for uid, p in list(room.players.items()):
        if p.is_bot:
            room.players.pop(uid, None)
