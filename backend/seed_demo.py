# -*- coding: utf-8 -*-
"""Seed + test E2E : simule une partie ranked à 2 joueurs (WebSocket) et peuple la base.
Prérequis : backend lancé (uvicorn main:app --port 8000).
Usage : python seed_demo.py    (durée ~40 s, timer de fin de manche inclus)"""
import asyncio, json, random, urllib.request

BASE = "http://localhost:8000"

def http(method, path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read().decode())

async def player(name, uid, code, words, delay_start):
    import websockets
    uri = f"ws://localhost:8000/ws/room/{code}?userId={uid}"
    results = {"events": [], "end": None}
    async with websockets.connect(uri) as ws:
        async def reader():
            async for raw in ws:
                msg = json.loads(raw)
                results["events"].append(msg["type"])
                if msg["type"] == "match_end":
                    results["end"] = msg
                    return
        r = asyncio.create_task(reader())
        await asyncio.sleep(delay_start)
        if name == "host":
            await ws.send(json.dumps({"type": "start"}))
        await asyncio.sleep(1)
        for w in words:
            await ws.send(json.dumps({"type": "guess", "word": w}))
            await asyncio.sleep(2)  # > cooldown 1.5s
        await asyncio.wait_for(r, timeout=60)
    return results

async def main():
    print("health:", http("GET", "/health"))
    sfx = random.randint(100, 999)
    a = http("POST", "/auth/register", {"username": f"Alice{sfx}"})
    b = http("POST", "/auth/register", {"username": f"Bob{sfx}"})
    room = http("POST", "/rooms", {"userId": a["id"], "ranked": True, "secretIndex": 0})
    code = room["roomCode"]
    http("POST", f"/rooms/{code}/join", {"userId": b["id"]})
    print(f"room {code} (secret index 0 = 'restaurant')")
    # Bob chauffe (cuisine=99°C → bonus vitesse), Alice trouve le mot exact.
    res = await asyncio.gather(
        player("host", a["id"], code, ["repas", "menu", "restaurant"], 2),
        player("guest", b["id"], code, ["cuisine", "serveur"], 0),
    )
    end = res[0]["end"] or res[1]["end"]
    print("\n--- RÉSULTATS ---")
    for r in end["results"]:
        print(f"  #{r['pos']} {r['username']:12} perf={r['perfScore']:5} "
              f"max={r['maxTemp']}°C found={r['found']} ELO {r['eloDelta']:+}")
    print("secret révélé :", end["secret"])
    print("\nleaderboard :", json.dumps(http("GET", "/leaderboard"), ensure_ascii=False))
    print("profil Alice :", json.dumps(http("GET", f"/profile/{a['id']}"), ensure_ascii=False))

if __name__ == "__main__":
    asyncio.run(main())
