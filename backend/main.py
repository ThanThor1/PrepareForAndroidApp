import os
import tempfile
import requests
from fastapi import FastAPI, File, UploadFile, HTTPException
from pydantic import BaseModel

SIGHTENGINE_API_USER = os.getenv("SIGHTENGINE_API_USER")
SIGHTENGINE_API_SECRET = os.getenv("SIGHTENGINE_API_SECRET")
SIGHTENGINE_URL = "https://api.sightengine.com/1.0/check.json"

app = FastAPI(title="Image Moderation API (Sightengine)")


class ImageModerationOut(BaseModel):
    nudity_raw: dict
    violence_score: float
    is_nude: bool
    is_violent: bool
    nudity_threshold: float = 0.7
    violence_threshold: float = 0.7


def call_sightengine_image(
    file_path: str,
    nudity_threshold: float = 0.7,
    violence_threshold: float = 0.7,
) -> ImageModerationOut:
    if not SIGHTENGINE_API_USER or not SIGHTENGINE_API_SECRET:
        raise RuntimeError("Missing SIGHTENGINE_API_USER or SIGHTENGINE_API_SECRET env vars")

    # các model có thể dùng: nudity, violence, weapons, alcohol, drugs,...
    payload = {
        "api_user": SIGHTENGINE_API_USER,
        "api_secret": SIGHTENGINE_API_SECRET,
        "models": "nudity,violence",
    }

    files = {
        "media": open(file_path, "rb")
    }

    try:
        resp = requests.post(SIGHTENGINE_URL, files=files, data=payload)
    finally:
        files["media"].close()

    try:
        resp.raise_for_status()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Sightengine error: {e}")

    result = resp.json()

    # Kiểm tra response có lỗi không
    if result.get("status") != "success":
        raise HTTPException(status_code=500, detail=f"Sightengine returned error: {result}")

    # --- Xử lý nudity ---
    # Tuỳ option bạn bật trong dashboard, có thể là 'raw', 'partial', 'safe', ...:
    nudity = result.get("nudity", {})
    # ví dụ: nudity = {"raw": 0.85, "partial": 0.1, "safe": 0.05}
    nudity_raw = nudity
    nudity_score = nudity.get("raw") or nudity.get("none") or 0.0  # fallback

    # --- Xử lý violence ---
    # structure ví dụ: "violence": {"prob": 0.12}
    violence = result.get("violence", {})
    violence_score = float(violence.get("prob", 0.0))

    is_nude = nudity_score >= nudity_threshold
    is_violent = violence_score >= violence_threshold

    return ImageModerationOut(
        nudity_raw=nudity_raw,
        violence_score=violence_score,
        is_nude=is_nude,
        is_violent=is_violent,
        nudity_threshold=nudity_threshold,
        violence_threshold=violence_threshold,
    )


@app.get("/")
def root():
    return {"message": "Sightengine image moderation API is running"}


@app.post("/moderate-image", response_model=ImageModerationOut)
async def moderate_image(file: UploadFile = File(...)):
    # Lưu tạm file xuống disk (Sightengine cần đường dẫn file hoặc URL)
    try:
        suffix = os.path.splitext(file.filename)[1] or ".jpg"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            content = await file.read()
            tmp.write(content)
            tmp_path = tmp.name
    except Exception:
        raise HTTPException(status_code=500, detail="Cannot save uploaded file")

    try:
        result = call_sightengine_image(tmp_path)
    finally:
        # Xoá file tạm
        if os.path.exists(tmp_path):
            os.remove(tmp_path)

    return result
