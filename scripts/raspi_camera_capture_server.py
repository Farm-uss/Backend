from __future__ import annotations

import os
import subprocess

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse, Response


DEVICE_PATH = os.getenv("CAMERA_DEVICE", "/dev/video0")
FFMPEG_BIN = os.getenv("FFMPEG_BIN", "ffmpeg")
CAPTURE_WIDTH = os.getenv("CAPTURE_WIDTH", "1920")
CAPTURE_HEIGHT = os.getenv("CAPTURE_HEIGHT", "1080")
CAPTURE_FRAMERATE = os.getenv("CAPTURE_FRAMERATE", "5")
CAPTURE_TIMEOUT_SECONDS = float(os.getenv("CAPTURE_TIMEOUT_SECONDS", "10"))


app = FastAPI(title="Raspberry Pi Camera Capture Server")


def capture_jpeg() -> bytes:
    command = [
        FFMPEG_BIN,
        "-hide_banner",
        "-loglevel",
        "error",
        "-f",
        "video4linux2",
        "-framerate",
        CAPTURE_FRAMERATE,
        "-video_size",
        f"{CAPTURE_WIDTH}x{CAPTURE_HEIGHT}",
        "-i",
        DEVICE_PATH,
        "-frames:v",
        "1",
        "-f",
        "image2pipe",
        "-vcodec",
        "mjpeg",
        "pipe:1",
    ]

    try:
        result = subprocess.run(
            command,
            check=True,
            capture_output=True,
            timeout=CAPTURE_TIMEOUT_SECONDS,
        )
    except subprocess.TimeoutExpired as exc:
        raise HTTPException(status_code=504, detail="camera capture timeout") from exc
    except FileNotFoundError as exc:
        raise HTTPException(status_code=500, detail="ffmpeg not found") from exc
    except subprocess.CalledProcessError as exc:
        stderr = exc.stderr.decode("utf-8", errors="ignore").strip()
        raise HTTPException(
            status_code=502,
            detail=stderr or "failed to capture camera image",
        ) from exc

    if not result.stdout:
        raise HTTPException(status_code=502, detail="empty capture response")

    return result.stdout


@app.get("/health")
def health() -> JSONResponse:
    return JSONResponse(
        {
            "status": "ok",
            "device": DEVICE_PATH,
            "width": CAPTURE_WIDTH,
            "height": CAPTURE_HEIGHT,
            "framerate": CAPTURE_FRAMERATE,
        }
    )


@app.post("/capture")
def capture() -> Response:
    image_bytes = capture_jpeg()
    return Response(content=image_bytes, media_type="image/jpeg")
