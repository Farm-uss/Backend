#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

import cv2
import torch.nn as nn
from ultralytics import YOLO
import ultralytics.nn.modules.conv as conv_mod

DEFAULT_MODEL_PATH = Path("/Users/jeongsuyeon/Documents/똑순이/In-door 육묘장 생장 데이터/renamed_weights/pa_best.pt")
DEFAULT_IMAGE_PATH = Path("/Users/jeongsuyeon/Documents/411_PA_G6_L3_D2024-11-11-13-52_001_001.jpg")
DEFAULT_OUTPUT_IMAGE_PATH = Path("/tmp/411_PA_G6_L3_D2024-11-11-13-52_001_001_pa_boxes.jpg")


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


def is_leaf_class(class_name: str) -> bool:
    return str(class_name).lower() in {"leaf", "leaves", "cotyledons", "cotyledon"}


def draw_boxes(image_path: Path, boxes_info: list[dict], output_path: Path) -> None:
    img = cv2.imread(str(image_path))
    if img is None:
        raise RuntimeError(f"Failed to read image: {image_path}")

    for i, b in enumerate(boxes_info, start=1):
        x1, y1, x2, y2 = b["xyxy"]
        color = (30, 220, 30) if b["isLeaf"] else (50, 160, 255)
        cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)
        label = f"{b['className']}#{i} {b['confidence']:.3f}"
        cv2.putText(img, label, (x1, max(18, y1 - 8)), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

    cv2.imwrite(str(output_path), img)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=Path, default=DEFAULT_MODEL_PATH)
    parser.add_argument("--image", type=Path, default=DEFAULT_IMAGE_PATH)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT_IMAGE_PATH)
    parser.add_argument("--conf", type=float, default=0.25)
    args = parser.parse_args()

    setattr(conv_mod, "Conv_RGB", Conv_RGB)
    setattr(conv_mod, "Conv_Thermo", Conv_Thermo)
    setattr(conv_mod, "Conv_Depth", Conv_Depth)
    setattr(conv_mod, "Triple_Conv", Triple_Conv)

    model = YOLO(str(args.model))
    result = model.predict(source=str(args.image), conf=args.conf, verbose=False)[0]

    boxes = result.boxes
    detections = 0 if boxes is None else len(boxes)
    names = result.names if isinstance(result.names, dict) else {}

    boxes_info: list[dict] = []
    confidences: list[float] = []
    areas: list[float] = []

    if boxes is not None and detections > 0:
        xyxy = boxes.xyxy.cpu().numpy().tolist()
        confidences = boxes.conf.cpu().numpy().tolist()
        classes = [int(v) for v in boxes.cls.cpu().numpy().tolist()]

        for idx, (coords, conf, cls_idx) in enumerate(zip(xyxy, confidences, classes), start=1):
            x1, y1, x2, y2 = [int(round(v)) for v in coords]
            area = float(max(0, (x2 - x1) * (y2 - y1)))
            class_name = names.get(str(cls_idx), names.get(cls_idx, str(cls_idx)))
            areas.append(area)
            boxes_info.append(
                {
                    "index": idx,
                    "classId": cls_idx,
                    "className": class_name,
                    "isLeaf": is_leaf_class(class_name),
                    "confidence": float(conf),
                    "xyxy": [x1, y1, x2, y2],
                    "bboxAreaPx": area,
                }
            )

    draw_boxes(args.image, boxes_info, args.output)

    response = {
        "success": True,
        "taskType": "GROWTH_MEASUREMENT",
        "data": {
            "leafCount": int(sum(1 for b in boxes_info if b["isLeaf"])),
            "sizePxAvg": float((sum(areas) / len(areas)) if areas else 0.0),
            "sizePxTotal": float(sum(areas) if areas else 0.0),
            "confidence": float((sum(confidences) / len(confidences)) if confidences else 0.0),
            "predictConfThreshold": args.conf,
            "topConfidences": [round(float(c), 6) for c in confidences],
            "imageShape": list(result.orig_shape),
            "modelName": args.model.name,
            "modelVersion": "local-test",
            "classNames": names,
            "boxes": boxes_info,
            "annotatedImagePath": str(args.output),
        },
    }

    print(json.dumps(response, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
