# -*- coding: utf-8 -*-
"""Backend Ostirotix — FastAPI + WebSocket.
Lancer : uvicorn main:app --host 0.0.0.0 --port 8000
Émulateur Android → http://10.0.2.2:8000"""
import random
from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from pydantic import BaseModel
import db, semantic, rooms

app = FastAPI(title="Ostirotix API")
db.init()

class RegisterReq(BaseModel):
    username: str

class GuessReq(BaseModel):
    secretIndex: int
    word: str

class RoomReq(BaseModel):
    userId: str
    ranked: bool = False
    bot: bool = False               # ajoute un adversaire bot dans la salle
    secretIndex: int | None = None  # tests/seed uniquement

class JoinReq(BaseModel):
    userId: str

@app.get("/health")
def health():
    return {"status": "ok", "secrets": semantic.secret_count()}

@app.post("/auth/register")
def register(req: RegisterReq):
    name = req.username.strip()
    if not (2 <= len(name) <= 20):
        raise HTTPException(400, "Pseudo : 2 à 20 caractères")
    if db.username_taken(name):
        raise HTTPException(409, "Pseudo déjà pris")
    return db.create_user(name, is_guest=False)

@app.post("/auth/guest")
def guest():
    name = f"Invité{random.randint(1000, 9999)}"
    while db.username_taken(name):
        name = f"Invité{random.randint(1000, 9999)}"
    return db.create_user(name, is_guest=True)

@app.get("/daily")
def daily():
    return {"secretIndex": semantic.daily_index()}

@app.post("/solo/guess")
def solo_guess(req: GuessReq):
    if not (0 <= req.secretIndex < semantic.secret_count()):
        raise HTTPException(400, "Index invalide")
    return semantic.guess(req.secretIndex, req.word)

@app.post("/rooms")
def create_room(req: RoomReq):
    if db.get_user(req.userId) is None:
        raise HTTPException(404, "Utilisateur inconnu")
    code = rooms.create_room(req.ranked, req.userId, req.secretIndex)
    if req.bot:
        rooms.add_bot(rooms.ROOMS[code])
    return {"roomCode": code, "ranked": req.ranked}

@app.post("/rooms/{code}/join")
def join_room(code: str, req: JoinReq):
    room = rooms.ROOMS.get(code.upper())
    if room is None:
        raise HTTPException(404, "Room introuvable")
    if room.state != "lobby":
        raise HTTPException(409, "Partie déjà commencée")
    if db.get_user(req.userId) is None:
        raise HTTPException(404, "Utilisateur inconnu")
    return {"roomCode": room.code, "ranked": room.ranked}

@app.get("/leaderboard")
def leaderboard():
    return {"players": db.leaderboard()}

@app.get("/profile/{user_id}")
def profile(user_id: str):
    u = db.get_user(user_id)
    if u is None:
        raise HTTPException(404, "Utilisateur inconnu")
    u["winrate"] = round(u["wins"] / u["games"], 3) if u["games"] else 0.0
    return u

@app.websocket("/ws/room/{code}")
async def ws_room(ws: WebSocket, code: str, userId: str):
    await ws.accept()
    room = rooms.ROOMS.get(code.upper())
    user = db.get_user(userId)
    if room is None or user is None:
        await ws.send_json({"type": "error", "message": "Room ou utilisateur introuvable"})
        await ws.close()
        return
    p = room.players.get(userId)
    if p is None:
        if room.state != "lobby":
            await ws.send_json({"type": "error", "message": "Partie déjà commencée"})
            await ws.close()
            return
        p = rooms.Player(userId, user["username"])
        room.players[userId] = p
        rooms.remove_standby_bots(room)
    p.ws = ws
    await room.broadcast({"type": "players", "players": room.players_payload(),
                          "state": room.state, "hostId": room.host_id, "ranked": room.ranked})
    try:
        while True:
            msg = await ws.receive_json()
            t = msg.get("type")
            if t == "start" and userId == room.host_id and room.state == "lobby":
                if len(room.players) >= 2:
                    await room.start()
                else:
                    await ws.send_json({"type": "error", "message": "Il faut au moins 2 joueurs"})
            elif t == "guess":
                await room.handle_guess(userId, str(msg.get("word", "")))
    except WebSocketDisconnect:
        p.ws = None
        if room.state == "lobby":
            room.players.pop(userId, None)
            # une salle où il ne reste que des bots est détruite
            if not any(not q.is_bot for q in room.players.values()):
                rooms.ROOMS.pop(room.code, None)
            else:
                await room.broadcast({"type": "players", "players": room.players_payload(),
                                      "state": room.state, "hostId": room.host_id, "ranked": room.ranked})
