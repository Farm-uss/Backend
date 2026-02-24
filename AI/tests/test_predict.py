from io import BytesIO

from fastapi.testclient import TestClient
from PIL import Image

from app.main import app


client = TestClient(app)


def _sample_png_bytes() -> bytes:
    image = Image.new("RGB", (32, 32), color=(255, 255, 255))
    buffer = BytesIO()
    image.save(buffer, format="PNG")
    return buffer.getvalue()


def test_predict() -> None:
    files = {"image": ("sample.png", _sample_png_bytes(), "image/png")}
    response = client.post("/predict", files=files)

    assert response.status_code == 200
    body = response.json()
    assert "disease" in body
    assert "confidence" in body
    assert "is_unknown" in body
    assert "top3" in body
