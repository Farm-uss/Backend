import os
import ast
import datetime
from io import BytesIO

import numpy as np
import torch
import torch.nn as nn
from PIL import Image
from torchvision import transforms
from torchvision.models import resnet50
from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import JSONResponse

TASK = "a"
MODEL_NAME = os.getenv("MODEL_NAME", "kimphys-62-a")
MODEL_VERSION = os.getenv("MODEL_VERSION", "v2.0")
GPU_INDEX = int(os.getenv("GPU_INDEX", "0"))
GROWTH_CONF_THRESHOLD = float(os.getenv("GROWTH_CONF_THRESHOLD", "0.6"))

app = FastAPI()


def parse_eval_py():
    text = open("/workspace/evaluate.py", "r", encoding="utf-8", errors="ignore").read()
    module = ast.parse(text)
    cls_dict = None
    classes = None
    for node in module.body:
        if isinstance(node, ast.Assign):
            for t in node.targets:
                if isinstance(t, ast.Name) and t.id == "cls_dict":
                    cls_dict = ast.literal_eval(node.value)
                if isinstance(t, ast.Name) and t.id == "classes":
                    classes = ast.literal_eval(node.value)
    if cls_dict is None or classes is None:
        raise RuntimeError("failed to parse cls_dict/classes from evaluate.py")
    return cls_dict, classes


CLS_DICT, CLASSES = parse_eval_py()


def build_model(num_classes):
    model = resnet50(pretrained=False)
    model.fc = nn.Linear(model.fc.in_features, num_classes)
    return model


device = torch.device(f"cuda:{GPU_INDEX}" if torch.cuda.is_available() else "cpu")
models = {}
for crop in sorted(CLS_DICT.keys()):
    model = build_model(len(CLS_DICT[crop]))
    ckpt = torch.load(f"/workspace/weights/{crop}_{TASK}_best.pt", map_location=device)
    model.load_state_dict(ckpt["model_state_dict"])
    model.to(device).eval()
    models[crop] = model

disease_transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])


def decode_rgb(image_bytes):
    pil = Image.open(BytesIO(image_bytes)).convert("RGB")
    return np.array(pil)


def preprocess(img_rgb):
    pil = Image.fromarray(img_rgb)
    return disease_transform(pil).unsqueeze(0).to(device)


GROWTH_MODELS = {}
GROWTH_NAMES = {1: "ca", 2: "le", 3: "na", 4: "pa", 5: "pe", 6: "to"}
GROWTH_LOAD_ERROR = None

try:
    from ultralytics import YOLO
    import ultralytics.nn.modules.conv as conv_mod

    class Conv_RGB(conv_mod.Conv):
        pass

    class Conv_Thermo(conv_mod.Conv):
        pass

    class Conv_Depth(conv_mod.Conv):
        pass

    class Triple_Conv(nn.Module):
        def __init__(self, *args, **kwargs):
            super().__init__()

        def forward(self, x):
            y1 = self.bn(self.conv(x))
            y2 = self.bn2(self.conv2(x))
            y3 = self.bn3(self.conv3(x))
            return self.act(y1 + y2 + y3)

    setattr(conv_mod, "Conv_RGB", Conv_RGB)
    setattr(conv_mod, "Conv_Thermo", Conv_Thermo)
    setattr(conv_mod, "Conv_Depth", Conv_Depth)
    setattr(conv_mod, "Triple_Conv", Triple_Conv)

    for code, name in GROWTH_NAMES.items():
        path = f"/workspace/models/growth/{name}_best.pt"
        if os.path.exists(path):
            GROWTH_MODELS[code] = YOLO(path)
except Exception as e:
    GROWTH_LOAD_ERROR = str(e)


@app.get("/health")
def health():
    return {
        "status": "UP",
        "model": MODEL_NAME,
        "device": str(device),
        "growth_models_loaded": sorted(GROWTH_MODELS.keys()),
        "growth_load_error": GROWTH_LOAD_ERROR,
    }


def parse_crop_code(crop_code: str | None, task_name: str):
    if crop_code is None:
        return None, JSONResponse(status_code=400, content={"error": f"crop_code is required for {task_name}"})
    try:
        return int(crop_code), None
    except Exception:
        return None, JSONResponse(status_code=400, content={"error": "crop_code must be integer"})


@app.post("/predict")
async def predict(
    image: UploadFile = File(...),
    task_type: str = Form("DISEASE_CLASSIFICATION"),
    capture_id: str = Form(None),
    crop_code: str = Form(None),
):
    try:
        _ = capture_id
        data = await image.read()
        img_rgb = decode_rgb(data)

        ttype = (task_type or "DISEASE_CLASSIFICATION").strip().upper()

        if ttype == "GROWTH_MEASUREMENT":
            ccode, error = parse_crop_code(crop_code, "GROWTH_MEASUREMENT")
            if error is not None:
                return error
            if ccode not in GROWTH_MODELS:
                return JSONResponse(status_code=400, content={"error": f"unsupported crop_code: {ccode}"})

            model = GROWTH_MODELS[ccode]
            result = model.predict(source=img_rgb, conf=0.25, verbose=False)[0]
            boxes = result.boxes

            confidences = []
            bboxes = []
            if boxes is not None and len(boxes) > 0:
                xyxy = boxes.xyxy.cpu().numpy().tolist()
                classes = boxes.cls.cpu().numpy().tolist()
                conf = boxes.conf.cpu().numpy().tolist()
                for cls_idx, score, box in zip(classes, conf, xyxy):
                    x1, y1, x2, y2 = [int(round(v)) for v in box]
                    confidences.append(float(score))
                    bboxes.append({
                        "class_id": int(cls_idx),
                        "class_name": result.names.get(int(cls_idx), str(int(cls_idx))) if isinstance(result.names, dict) else str(int(cls_idx)),
                        "confidence": round(float(score), 6),
                        "xyxy": [x1, y1, x2, y2],
                    })

            top_confidences = sorted(confidences, reverse=True)
            filtered = [bbox for bbox in bboxes if bbox["confidence"] >= GROWTH_CONF_THRESHOLD]
            leaf_count = len(filtered)
            size_px_total = 0.0
            for bbox in filtered:
                x1, y1, x2, y2 = bbox["xyxy"]
                size_px_total += float(max(0, (x2 - x1) * (y2 - y1)))

            avg_conf = float(sum(top_confidences) / len(top_confidences)) if top_confidences else 0.0
            top3 = [{"label": "leaf", "p": round(conf, 6)} for conf in top_confidences[:3]]

            return {
                "disease": "growth",
                "confidence": round(avg_conf, 6),
                "is_unknown": False,
                "is_abnormal": False,
                "abnormal_reason": None,
                "model_version": MODEL_VERSION,
                "model_name": f"growth-{GROWTH_NAMES.get(ccode, ccode)}",
                "task_type": ttype,
                "label": "leaf",
                "bbox_json": bboxes,
                "inferred_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "top3": top3,
                "leaf_count": leaf_count,
                "fruit_count": None,
                "size_cm": round(size_px_total, 3),
                "summary": f"growth leaf_count={leaf_count}",
                "topConfidences": [round(conf, 6) for conf in top_confidences],
                "sizePxTotal": round(size_px_total, 3),
            }

        ccode, error = parse_crop_code(crop_code, "DISEASE_CLASSIFICATION")
        if error is not None:
            return error
        if ccode not in models:
            return JSONResponse(status_code=400, content={"error": f"unsupported crop_code: {ccode}"})

        x = preprocess(img_rgb)
        with torch.no_grad():
            logits = models[ccode](x)[0]
            probs = torch.softmax(logits, dim=0).detach().cpu().numpy()

        ranked = sorted(
            ((CLS_DICT[ccode][i], float(prob)) for i, prob in enumerate(probs)),
            key=lambda item: item[1],
            reverse=True,
        )
        pred_label, pred_conf = ranked[0]
        top3 = [{"label": label, "p": round(prob, 6)} for label, prob in ranked[:3]]
        abnormal = pred_label not in ("00", "healthy", "normal")

        return {
            "disease": pred_label,
            "confidence": round(pred_conf, 6),
            "is_unknown": pred_label == "unknown",
            "is_abnormal": abnormal,
            "abnormal_reason": None if abnormal else "healthy",
            "model_version": MODEL_VERSION,
            "model_name": MODEL_NAME,
            "task_type": ttype,
            "label": pred_label,
            "bbox_json": [],
            "inferred_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "top3": top3,
            "leaf_count": None,
            "fruit_count": None,
            "size_cm": None,
            "summary": f"{pred_label} ({pred_conf:.4f})",
        }
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": str(e)})
