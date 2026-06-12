# -*- coding: utf-8 -*-
"""Bots du mode classé : adversaires « élastiques ».

Caractère voulu :
- pas trop fort : tant que le joueur progresse, le bot reste quelques degrés
  DERRIÈRE le meilleur humain (il fait semblant de chercher) ;
- mais il punit l'inaction : si le joueur stagne sous 50° après PATIENCE, ou si
  la partie traîne au-delà de HARD_LIMIT, le bot part en chasse et finit par
  trouver le mot — jamais avant MIN_WIN cependant.
"""
import asyncio, os, random, time
import db, semantic

BOT_NAMES = ["Le Scribe", "L'Archiviste", "Maître Plume", "Dame Encre",
             "Le Copiste", "Frère Vélin"]

SHADOW_EVERY = (7.0, 12.0)   # délai entre 2 essais en mode ombre
HUNT_EVERY = (4.5, 7.0)      # délai en mode chasse
# Réglables par env pour les tests (BOT_PATIENCE=15 etc.)
PATIENCE = float(os.environ.get("BOT_PATIENCE", 120))    # s avant de chasser un joueur froid (<50°)
HARD_LIMIT = float(os.environ.get("BOT_HARD_LIMIT", 420))  # s : au-delà, chasse quoi qu'il arrive
MIN_WIN = float(os.environ.get("BOT_MIN_WIN", 210))      # s : le bot ne peut JAMAIS gagner avant
SHADOW_DECAY = 0.93          # rapprochement lent du rang interne en ombre
HUNT_DECAY = (0.58, 0.78)    # rapprochement rapide en chasse


def get_bot_user(name: str) -> dict:
    """Compte persistant du bot (ELO réel dans le leaderboard)."""
    return db.get_user_by_username(name) or db.create_user(name, is_guest=False)


def _rank_for_temp(t: float) -> int:
    """Inverse approchée de semantic.temp_for_rank (bornée au top 1000)."""
    if t >= 90: return max(1, round(100 - t))
    if t >= 70: return round(11 + (89 - t) * (89 / 19))
    if t >= 45: return round(101 + (69 - t) * (399 / 24))
    if t >= 30: return round(501 + (44 - t) * (499 / 14))
    return 1000


def _pick_neighbor(secret_index: int, target_rank: int, used: set):
    """Voisin au rang le plus proche de la cible, jamais déjà proposé."""
    nb = semantic.neighbors(secret_index)
    if not nb:
        return None
    max_rank = max(nb)
    target = max(1, min(target_rank, max_rank))
    for off in range(max_rank + 1):
        for r in (target + off, target - off):
            w = nb.get(r)
            if w and w.strip().lower() not in used:
                return w
    return None


def _pick_cold(secret_index: int, used: set):
    """Mot du dictionnaire hors voisins : sort froid (-20..29°), l'air de chercher."""
    words = semantic.dictionary_words()
    secret_norm = semantic._norm(semantic.secret_word(secret_index))
    nb_used = {w.strip().lower() for w in semantic.neighbors(secret_index).values()}
    for _ in range(40):
        w = random.choice(words)
        k = w.strip().lower()
        if k not in used and k not in nb_used and semantic._norm(w) != secret_norm:
            return w
    return None


async def bot_loop(room, bot):
    """Tâche lancée au démarrage de la partie ; vit tant que la room joue."""
    try:
        nb = semantic.neighbors(room.secret_index)
        rank = max(nb) if nb else 400        # part du voisin le plus lointain
        hunting = False
        while True:
            await asyncio.sleep(random.uniform(*(HUNT_EVERY if hunting else SHADOW_EVERY)))
            if room.state not in ("playing", "ending"):
                return
            if room.state == "ending":
                return                        # un humain a trouvé : le bot s'incline
            humans = [p for p in room.players.values() if not p.is_bot]
            if humans and all(p.ws is None for p in humans):
                await room.finish()           # plus personne en face : on clôt
                return
            elapsed = time.time() - room.started_at
            best_h = max((p.max_temp for p in humans), default=-100.0)
            hunting = elapsed >= HARD_LIMIT or (elapsed >= PATIENCE and best_h < 50)
            if hunting:
                rank = max(1, int(rank * random.uniform(*HUNT_DECAY)))
                if rank <= 1:
                    if elapsed >= MIN_WIN:    # victoire du bot
                        word = semantic.secret_word(room.secret_index)
                    else:                     # tourne autour en attendant le droit de gagner
                        word = _pick_neighbor(room.secret_index, random.randint(2, 6), bot.words)
                else:
                    word = _pick_neighbor(room.secret_index, rank + random.randint(-2, 2), bot.words)
            else:
                rank = max(2, int(rank * SHADOW_DECAY))
                margin = max(3.0, 9.0 - elapsed / 60.0)      # la pression monte doucement
                target_t = best_h - margin - random.uniform(0.0, 6.0)
                tr = max(rank, _rank_for_temp(target_t))
                # temp réellement atteignable une fois borné à la liste de voisins
                achievable = semantic.temp_for_rank(min(tr, max(nb))) if nb else 100.0
                if target_t >= 50.0 and achievable <= best_h - 2.0:
                    word = _pick_neighbor(room.secret_index, tr, bot.words)
                else:
                    # même le voisin le plus lointain serait trop chaud : mot froid
                    word = _pick_cold(room.secret_index, bot.words)
            if word:
                await room.handle_guess(bot.id, word)
            if bot.found_at is not None:
                return
    except asyncio.CancelledError:
        pass
