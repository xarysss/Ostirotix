# -*- coding: utf-8 -*-
"""SQLite : comptes, parties, scores. stdlib sqlite3, simple pour MVP."""
import sqlite3, os, uuid, time

PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ostirotix.db")

def conn():
    c = sqlite3.connect(PATH)
    c.row_factory = sqlite3.Row
    return c

def init():
    with conn() as c:
        c.executescript("""
        CREATE TABLE IF NOT EXISTS users(
            id TEXT PRIMARY KEY, username TEXT UNIQUE NOT NULL,
            is_guest INTEGER DEFAULT 0, created_at INTEGER,
            rating INTEGER DEFAULT 1000, games INTEGER DEFAULT 0,
            wins INTEGER DEFAULT 0, best_rating INTEGER DEFAULT 1000);
        CREATE TABLE IF NOT EXISTS matches(
            id TEXT PRIMARY KEY, mode TEXT, secret_index INTEGER, created_at INTEGER);
        CREATE TABLE IF NOT EXISTS match_players(
            match_id TEXT, user_id TEXT, perf_score INTEGER, max_temp REAL,
            found INTEGER, elo_delta INTEGER, final_pos INTEGER,
            PRIMARY KEY(match_id, user_id));
        """)

def create_user(username: str, is_guest: bool) -> dict:
    uid = uuid.uuid4().hex
    with conn() as c:
        c.execute("INSERT INTO users(id,username,is_guest,created_at) VALUES(?,?,?,?)",
                  (uid, username, 1 if is_guest else 0, int(time.time())))
    return get_user(uid)

def get_user(uid: str):
    with conn() as c:
        r = c.execute("SELECT * FROM users WHERE id=?", (uid,)).fetchone()
    return dict(r) if r else None

def get_user_by_username(username: str):
    with conn() as c:
        r = c.execute("SELECT * FROM users WHERE username=?", (username,)).fetchone()
    return dict(r) if r else None

def username_taken(username: str) -> bool:
    with conn() as c:
        return c.execute("SELECT 1 FROM users WHERE username=?", (username,)).fetchone() is not None

def leaderboard(limit=50):
    with conn() as c:
        rs = c.execute("SELECT id,username,rating,games,wins FROM users WHERE games>0 "
                       "ORDER BY rating DESC LIMIT ?", (limit,)).fetchall()
    return [dict(r) for r in rs]

def save_match(mode: str, secret_index: int, results: list):
    """results: [{user_id, perf_score, max_temp, found, elo_delta, final_pos}]"""
    mid = uuid.uuid4().hex
    with conn() as c:
        c.execute("INSERT INTO matches VALUES(?,?,?,?)", (mid, mode, secret_index, int(time.time())))
        for r in results:
            c.execute("INSERT INTO match_players VALUES(?,?,?,?,?,?,?)",
                      (mid, r["user_id"], r["perf_score"], r["max_temp"],
                       1 if r["found"] else 0, r["elo_delta"], r["final_pos"]))
            c.execute("UPDATE users SET games=games+1, wins=wins+?, rating=rating+?, "
                      "best_rating=MAX(best_rating, rating+?) WHERE id=?",
                      (1 if r["final_pos"] == 1 else 0, r["elo_delta"], r["elo_delta"], r["user_id"]))
    return mid
