# -*- coding: utf-8 -*-
"""Moteur sémantique partagé. Mêmes formules que le SemanticEngine Kotlin.
Remplaçable par word2vec/FastText : garder la même interface guess()."""
import json, hashlib, os, unicodedata
from datetime import date

_PACK = None

def _norm(w: str) -> str:
    w = w.strip().lower()
    w = unicodedata.normalize("NFD", w)
    return "".join(c for c in w if unicodedata.category(c) != "Mn")

def load_pack():
    global _PACK
    if _PACK is None:
        p = os.path.join(os.path.dirname(os.path.abspath(__file__)), "semantic_pack_demo.json")
        with open(p, encoding="utf-8") as f:
            raw = json.load(f)
        secrets = []
        for s in raw["secrets"]:
            ranks = {_norm(w): (i + 1, w) for i, (w, _) in enumerate(s["neighbors"])}
            secrets.append({"word": s["word"], "norm": _norm(s["word"]), "ranks": ranks})
        _PACK = {"secrets": secrets, "dico": {_norm(w) for w in raw["dictionary"]}}
    return _PACK

def daily_index(d: date = None) -> int:
    d = d or date.today()
    return (d.toordinal() * 31 + 7) % len(load_pack()["secrets"])

def _hash(s: str) -> int:
    return int(hashlib.md5(s.encode("utf-8")).hexdigest()[:8], 16)

def temp_for_rank(rank: int) -> float:
    if rank <= 10: return 99 - (rank - 1)
    if rank <= 100: return round(89 - (rank - 11) * (19 / 89), 1)
    if rank <= 500: return round(69 - (rank - 101) * (24 / 399), 1)
    if rank <= 1000: return round(44 - (rank - 501) * (14 / 499), 1)
    return 0.0

def guess(secret_index: int, word: str) -> dict:
    """Retourne temp(-100..100), rank(0=exact,-1=absent), progression(‰), recognized, exact."""
    pack = load_pack()
    s = pack["secrets"][secret_index]
    n = _norm(word)
    if not n:
        return {"temp": -100, "rank": -1, "progression": 0, "recognized": False, "exact": False}
    if n == s["norm"]:
        return {"temp": 100, "rank": 0, "progression": 1000, "recognized": True, "exact": True}
    if n in s["ranks"]:
        rank, display = s["ranks"][n]
        return {"temp": temp_for_rank(rank), "rank": rank,
                "progression": 1000 - rank if rank <= 1000 else 0,
                "recognized": True, "exact": False}
    if n in pack["dico"]:
        return {"temp": -20 + (_hash(n + s["norm"]) % 50), "rank": -1, "progression": 0,
                "recognized": True, "exact": False}
    return {"temp": -90 + (_hash(n + s["norm"]) % 41), "rank": -1, "progression": 0,
            "recognized": False, "exact": False}

def secret_word(secret_index: int) -> str:
    return load_pack()["secrets"][secret_index]["word"]

def neighbors(secret_index: int) -> dict:
    """rang -> mot d'affichage (pour les bots et les indices)."""
    s = load_pack()["secrets"][secret_index]
    return {rank: w for (rank, w) in s["ranks"].values()}

def dictionary_words() -> list:
    """Mots du dictionnaire (normalisés), en liste stable pour tirage aléatoire."""
    pack = load_pack()
    if "dico_list" not in pack:
        pack["dico_list"] = sorted(pack["dico"])
    return pack["dico_list"]

def secret_count() -> int:
    return len(load_pack()["secrets"])

def hint(secret_index: int, hint_no: int) -> str:
    """Indice n : révèle un voisin de plus en plus proche (rang 50, 25, 10)."""
    s = load_pack()["secrets"][secret_index]
    target = {1: 50, 2: 25, 3: 10}.get(hint_no, 10)
    for n, (rank, w) in s["ranks"].items():
        if rank == target:
            return w
    return next(iter(s["ranks"].values()))[1]
