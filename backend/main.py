# main.py
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification, pipeline

MODEL_NAME = "gravitee-io/bert-mini-toxicity"

# load model + tokenizer 1 lần khi start server
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)

# in ra mapping nhãn cho chắc (chỉ log 1 lần khi start)
print("id2label:", model.config.id2label)

clf = pipeline(
    "text-classification",
    model=model,
    tokenizer=tokenizer,
    # max_length để tránh input quá dài tốn RAM
    truncation=True
)

app = FastAPI()

class TextIn(BaseModel):
    text: str

class ToxicOut(BaseModel):
    label: str      # "toxic" hoặc "not-toxic"
    score: float    # độ tin cậy
    is_toxic: bool  # tiện dùng cho frontend/backend khác

@app.post("/predict", response_model=ToxicOut)
def predict(inp: TextIn):
    out = clf(inp.text)[0]        # ví dụ: {'label': 'toxic', 'score': 0.97}
    label = out["label"]
    score = float(out["score"])

    # model đã trả sẵn 'toxic' / 'not-toxic', mình convert sang bool cho tiện
    is_toxic = (label.lower() == "toxic")

    return ToxicOut(
        label=label,
        score=score,
        is_toxic=is_toxic
    )
