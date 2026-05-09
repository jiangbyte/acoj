import uuid
import random
import io
import base64
from PIL import Image, ImageDraw, ImageFont
from typing import Optional
from pydantic import BaseModel
from config.settings import settings


class CaptchaResult(BaseModel):
    captcha_base64: str
    captcha_id: str
    captcha_code: Optional[str] = None


class CaptchaService:
    EXPIRE_SECONDS = 300

    def __init__(self, prefix: str):
        self._prefix = prefix
        self._redis = None

    def init(self, redis):
        self._redis = redis

    async def get_captcha(self) -> CaptchaResult:
        captcha_id = str(uuid.uuid4())
        code = ''.join(random.choices('ABCDEFGHJKLMNPQRSTUVWXYZ23456789', k=4))

        img = Image.new('RGB', (100, 38), color=(255, 255, 255))
        draw = ImageDraw.Draw(img)

        try:
            font = ImageFont.truetype("arial.ttf", 24)
        except IOError:
            font = ImageFont.load_default()

        for i, char in enumerate(code):
            x = 10 + i * 22
            y = random.randint(5, 15)
            color = (random.randint(0, 100), random.randint(0, 100), random.randint(0, 100))
            draw.text((x, y), char, font=font, fill=color)

        for _ in range(10):
            x1, y1 = random.randint(0, 100), random.randint(0, 38)
            x2, y2 = random.randint(0, 100), random.randint(0, 38)
            draw.line([(x1, y1), (x2, y2)], fill=(random.randint(0, 200), random.randint(0, 200), random.randint(0, 200)), width=1)

        buffer = io.BytesIO()
        img.save(buffer, format='PNG')
        buffer.seek(0)

        image_base64 = base64.b64encode(buffer.read()).decode('utf-8')

        if self._redis:
            await self._redis.setex(f"{self._prefix}{captcha_id}", self.EXPIRE_SECONDS, code)

        result = CaptchaResult(captcha_base64=f"data:image/png;base64,{image_base64}", captcha_id=captcha_id)
        if settings.app.debug:
            result.captcha_code = code

        return result

    async def check_captcha(self, captcha_id: str, captcha_code: str) -> bool:
        if not captcha_id or not captcha_code:
            raise ValueError("验证码ID或验证码内容不能为空")

        if not self._redis:
            return True

        key = f"{self._prefix}{captcha_id}"
        stored_code = await self._redis.get(key)

        if not stored_code:
            raise ValueError("验证码已过期或无效")

        if stored_code.upper() != captcha_code.strip().upper():
            raise ValueError("验证码错误")

        await self._redis.delete(key)
        return True


b_captcha = CaptchaService(prefix="b:captcha:")
c_captcha = CaptchaService(prefix="c:captcha:")
