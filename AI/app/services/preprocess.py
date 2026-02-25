from io import BytesIO

from PIL import Image


def decode_image(image_bytes: bytes) -> Image.Image:
    image = Image.open(BytesIO(image_bytes))
    return image.convert("RGB")
