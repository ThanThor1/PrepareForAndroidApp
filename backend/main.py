                                                                         # version 1 dùng perspective




# import os
# import requests
# from fastapi import FastAPI, HTTPException
# from pydantic import BaseModel

# PERSPECTIVE_API_KEY = os.getenv("PERSPECTIVE_API_KEY")
# API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze"

# app = FastAPI(title="Toxic Comment API (Perspective)")


# class CommentIn(BaseModel):
#     text: str


# class CommentOut(BaseModel):
#     score: float
#     is_toxic: bool
#     threshold: float = 0.7


# def call_perspective(text: str, threshold: float = 0.7) -> CommentOut:
#     if not PERSPECTIVE_API_KEY:
#         raise RuntimeError("Missing PERSPECTIVE_API_KEY env var")

#     data = {
#         "comment": {"text": text},
#         "languages": ["en"],
#         "requestedAttributes": {
#             "TOXICITY": {}
#         }
#     }
#     params = {"key": PERSPECTIVE_API_KEY}

#     resp = requests.post(API_URL, params=params, json=data)
#     try:
#         resp.raise_for_status()
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"Perspective error: {e}")

#     result = resp.json()
#     score = result["attributeScores"]["TOXICITY"]["summaryScore"]["value"]
#     return CommentOut(score=score, is_toxic=score >= threshold, threshold=threshold)


# @app.get("/")
# def root():
#     return {"message": "Perspective moderation API is running"}


# @app.post("/moderate", response_model=CommentOut)
# def moderate_comment(body: CommentIn):
#     return call_perspective(body.text)



                                                                       # version 2 dùng sightengine

import os
import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

# Dùng API của Sightengine thay cho Google
SIGHTENGINE_API_USER = os.getenv("SIGHTENGINE_API_USER")
SIGHTENGINE_API_SECRET = os.getenv("SIGHTENGINE_API_SECRET")
SIGHTENGINE_TEXT_URL = "https://api.sightengine.com/1.0/text/check.json"

app = FastAPI(title="Toxic Comment API (Sightengine)")


class CommentIn(BaseModel):
    text: str


class CommentOut(BaseModel):
    score: float
    is_toxic: bool
    threshold: float = 0.7


def call_sightengine(text: str, threshold: float = 0.7) -> CommentOut:
    if not SIGHTENGINE_API_USER or not SIGHTENGINE_API_SECRET:
        raise RuntimeError("Missing SIGHTENGINE_API_USER or SIGHTENGINE_API_SECRET env vars")

    # Theo docs text API của Sightengine: gửi text + lang + mode
    payload = {
        "text": text,
        "lang": "en",           # nếu bạn có tiếng Việt thì đổi/cho thêm
        "mode": "standard",     # hoặc "ml" tùy bạn cấu hình
        "api_user": SIGHTENGINE_API_USER,
        "api_secret": SIGHTENGINE_API_SECRET,
    }

    try:
        resp = requests.post(SIGHTENGINE_TEXT_URL, data=payload)
        resp.raise_for_status()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Sightengine error: {e}")

    result = resp.json()

    if result.get("status") != "success":
        raise HTTPException(status_code=500, detail=f"Sightengine returned error: {result}")

    # Tuỳ theo plan, bạn có thể có: "profanity", "personal", "threat", "discrimination", ...
    # Ở đây mình gom các 'xấu' lại lấy max score để ra 1 score chung giống TOXICITY.
    toxic_scores = []

    profanity = result.get("profanity", {})
    if "match" in profanity:
        # "match" là score tổng (0–1) cho profanity
        toxic_scores.append(float(profanity.get("match", 0.0)))

    # nếu bạn bật thêm các module khác thì cộng thêm vào:
    # threats
    threat = result.get("threat", {})
    if "match" in threat:
        toxic_scores.append(float(threat.get("match", 0.0)))

    # insults / hate / discrimination (tuỳ bạn enable module trong dashboard)
    # ví dụ:
    discrimination = result.get("discrimination", {})
    if "match" in discrimination:
        toxic_scores.append(float(discrimination.get("match", 0.0)))

    # Nếu không có module nào trả về, mặc định 0
    if toxic_scores:
        score = max(toxic_scores)
    else:
        score = 0.0

    is_toxic = score >= threshold
    return CommentOut(score=score, is_toxic=is_toxic, threshold=threshold)


@app.get("/")
def root():
    return {"message": "Sightengine text moderation API is running"}


@app.post("/moderate", response_model=CommentOut)
def moderate_comment(body: CommentIn):
    return call_sightengine(body.text)
