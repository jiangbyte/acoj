"""
Image processing utilities — mirrors hei-gin's ``sdk/utils/image.go``.

Provides ``compress_base64_image()`` for resizing and re-encoding
base64 data URL images, used for avatar upload processing.
"""

from __future__ import annotations

import base64
import io
import logging
from typing import Optional

from PIL import Image

logger = logging.getLogger(__name__)


def compress_base64_image(
    data_url: str,
    max_width: int = 256,
    max_height: int = 256,
    quality: int = 80,
) -> str:
    """Compress a base64 data URL image.

    Resizes to fit within *max_width* × *max_height* while preserving
    aspect ratio, then re-encodes at the given JPEG *quality*.

    PNG images are preserved as PNG; all other formats are converted to JPEG.

    Args:
        data_url: Base64 data URL (e.g. ``"data:image/png;base64,..."``).
        max_width: Maximum width in pixels.
        max_height: Maximum height in pixels.
        quality: JPEG quality (1–100).

    Returns:
        Compressed base64 data URL, or the original string if it cannot
        be decoded or is not a data URL.

    Mirrors hei-gin's ``utils.CompressBase64Image(dataURL, maxWidth, maxHeight, quality)``.
    """
    if not data_url or not data_url.startswith("data:image/"):
        return data_url

    try:
        # Extract MIME type and base64 data
        header, _, b64_data = data_url.partition(",")
        if not b64_data:
            return data_url

        mime_type = header.split(":")[1].split(";")[0] if ":" in header else "image/jpeg"
        raw = base64.b64decode(b64_data)

        # Decode image
        img = Image.open(io.BytesIO(raw))
        original_format = img.format or "JPEG"

        # Resize if needed
        w, h = img.size
        if w > max_width or h > max_height:
            ratio = min(max_width / w, max_height / h)
            new_size = (int(w * ratio), int(h * ratio))
            img = img.resize(new_size, Image.LANCZOS)

        # Re-encode
        buf = io.BytesIO()
        if original_format and original_format.upper() == "PNG":
            img.save(buf, format="PNG")
            new_mime = "image/png"
        else:
            if img.mode in ("P", "RGBA"):
                img = img.convert("RGB")
            img.save(buf, format="JPEG", quality=quality)
            new_mime = "image/jpeg"

        compressed = base64.b64encode(buf.getvalue()).decode("ascii")
        return f"data:{new_mime};base64,{compressed}"

    except Exception:
        logger.warning("Image compression failed, returning original")
        return data_url
