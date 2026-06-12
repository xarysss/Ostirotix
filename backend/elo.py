# -*- coding: utf-8 -*-
"""ELO multijoueur pairwise. Tous démarrent à 1000."""

def k_factor(games: int, rating: float) -> int:
    if games < 10: return 40
    if rating > 1800: return 16
    return 24

def compute_deltas(players: list) -> dict:
    """players: [{id, rating, games, score(perf), found(bool)}]. Retourne {id: delta}.
    Tri réel : found d'abord, puis score. Égalité (0.5) si même found et |Δscore| < 10."""
    deltas = {}
    for a in players:
        if len(players) < 2:
            deltas[a["id"]] = 0
            continue
        total = 0.0
        for b in players:
            if a["id"] == b["id"]: continue
            if a["found"] != b["found"]:
                real = 1.0 if a["found"] else 0.0
            elif abs(a["score"] - b["score"]) < 10:
                real = 0.5
            else:
                real = 1.0 if a["score"] > b["score"] else 0.0
            expected = 1.0 / (1.0 + 10 ** ((b["rating"] - a["rating"]) / 400.0))
            total += real - expected
        k = k_factor(a["games"], a["rating"])
        deltas[a["id"]] = round(k * total / (len(players) - 1))
    return deltas
