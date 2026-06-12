# -*- coding: utf-8 -*-
"""Test E2E du bot ranked : un joueur volontairement nul affronte le bot.
Lancer le serveur avec des délais courts d'abord :
  BOT_PATIENCE=15 BOT_MIN_WIN=30 BOT_HARD_LIMIT=90 uvicorn main:app --port 8000
Attendu : le bot reste froid au début (ombre), passe en chasse vers ~15 s,
trouve le mot après ~30 s -> endgame_started(winner=bot) puis match_end."""
import asyncio, json, time, urllib.request
import websockets

BASE = "http://127.0.0.1:8000"

def post(path, body):
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.loads(urllib.request.urlopen(req).read())

COLD_WORDS = ["maison", "chaise", "route", "nuage", "papier", "verre", "sable",
              "moteur", "plage", "fleur", "tasse", "lampe"]

async def main():
    u = post("/auth/guest", {})
    print("joueur:", u["username"])
    r = post("/rooms", {"userId": u["id"], "ranked": True, "bot": True})
    code = r["roomCode"]
    print("room:", code)
    t0 = time.time()
    bot_won = False
    async with websockets.connect(f"ws://127.0.0.1:8000/ws/room/{code}?userId={u['id']}") as ws:
        first = json.loads(await ws.recv())
        names = [p["username"] for p in first.get("players", [])]
        print("lobby:", names)
        assert len(names) == 2, "le bot doit être dans le lobby"
        await ws.send(json.dumps({"type": "start"}))

        async def guesser():
            for w in COLD_WORDS:
                await asyncio.sleep(7)
                await ws.send(json.dumps({"type": "guess", "word": w}))
        gtask = asyncio.create_task(guesser())
        try:
            while True:
                m = json.loads(await asyncio.wait_for(ws.recv(), timeout=180))
                el = time.time() - t0
                t = m.get("type")
                if t == "players":
                    print(f"[{el:5.1f}s] " + " | ".join(
                        f"{p['username']} {p['maxTemp']:.0f}° ({p['attempts']} essais)"
                        for p in m["players"]))
                elif t == "endgame_started":
                    print(f"[{el:5.1f}s] ENDGAME — gagnant : {m['winner']}")
                    bot_won = m["winner"] != u["username"]
                elif t == "match_end":
                    print(f"[{el:5.1f}s] FIN — secret : {m['secret']}")
                    for res in m["results"]:
                        print(f"   #{res['pos']} {res['username']} perf={res['perfScore']} "
                              f"max={res['maxTemp']:.0f}° elo{res['eloDelta']:+d} found={res['found']}")
                    break
        finally:
            gtask.cancel()
    print("RESULTAT:", "BOT GAGNE (attendu, joueur nul)" if bot_won else "ECHEC: le bot n'a pas gagné")

asyncio.run(main())
