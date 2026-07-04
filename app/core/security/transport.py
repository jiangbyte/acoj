import base64
import html
import secrets
from uuid import uuid4

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa
from pydantic import Field

from app.core.config.settings import settings
from app.core.exceptions.business import BusinessError
from app.core.response.schema import ApiResponse
from app.core.schema.base import ApiSchema
from app.core.security.password import hash_password, verify_password
from app.platform.cache.keys import captcha_key, password_crypto_key
from app.platform.cache.redis import get_redis


class CaptchaResponse(ApiSchema):
    captcha_id: str
    image_base64: str


class PasswordKeyResponse(ApiSchema):
    key_id: str
    public_key: str


class CaptchaMixin(ApiSchema):
    captcha_id: str = Field(min_length=1, max_length=64)
    captcha_value: str = Field(min_length=1, max_length=16)


class PasswordKeyMixin(ApiSchema):
    password_key_id: str = Field(min_length=1, max_length=64)


CaptchaApiResponse = ApiResponse[CaptchaResponse]
PasswordKeyApiResponse = ApiResponse[PasswordKeyResponse]

CAPTCHA_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"


async def create_captcha() -> CaptchaResponse:
    value = "".join(secrets.choice(CAPTCHA_ALPHABET) for _ in range(4))
    captcha_id = uuid4().hex
    redis = _required_redis("Redis is required for captcha")
    await redis.setex(
        captcha_key(captcha_id),
        settings.auth.captcha_ttl_seconds,
        hash_password(value.lower()),
    )
    return CaptchaResponse(captcha_id=captcha_id, image_base64=_captcha_svg_base64(value))


async def verify_captcha(captcha_id: str, captcha_value: str) -> None:
    redis = _required_redis("Redis is required for captcha")
    key = captcha_key(captcha_id)
    raw = await redis.get(key)
    await redis.delete(key)
    raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
    if not raw_text or not verify_password(captcha_value.strip().lower(), str(raw_text)):
        raise BusinessError("Invalid or expired captcha")


async def create_password_key() -> PasswordKeyResponse:
    private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    private_pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption(),
    ).decode("utf-8")
    public_pem = private_key.public_key().public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    ).decode("utf-8")
    key_id = uuid4().hex
    redis = _required_redis("Redis is required for password encryption")
    await redis.setex(
        password_crypto_key(key_id),
        settings.auth.password_crypto_key_ttl_seconds,
        private_pem,
    )
    return PasswordKeyResponse(key_id=key_id, public_key=public_pem)


async def decrypt_passwords(
    password_key_id: str,
    *encrypted_values: str | None,
) -> list[str | None]:
    redis = _required_redis("Redis is required for password encryption")
    key = password_crypto_key(password_key_id)
    raw = await redis.get(key)
    raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
    if not raw_text:
        raise BusinessError("Invalid or expired password encryption key")
    try:
        private_key = serialization.load_pem_private_key(
            str(raw_text).encode("utf-8"),
            password=None,
        )
        result: list[str | None] = []
        for value in encrypted_values:
            result.append(_decrypt_password(private_key, value) if value else None)
        return result
    finally:
        await redis.delete(key)


def _decrypt_password(private_key, encrypted_value: str) -> str:
    try:
        ciphertext = base64.b64decode(encrypted_value)
        plaintext = private_key.decrypt(
            ciphertext,
            padding.OAEP(
                mgf=padding.MGF1(algorithm=hashes.SHA256()),
                algorithm=hashes.SHA256(),
                label=None,
            ),
        )
        return plaintext.decode("utf-8")
    except Exception as exc:
        raise BusinessError("Invalid encrypted password") from exc


def _captcha_svg_base64(value: str) -> str:
    escaped = html.escape(value)
    noise = "\n".join(
        f'<line x1="{secrets.randbelow(140)}" y1="{secrets.randbelow(44)}" '
        f'x2="{secrets.randbelow(140)}" y2="{secrets.randbelow(44)}" '
        f'stroke="#94a3b8" stroke-width="1" opacity="0.45" />'
        for _ in range(6)
    )
    text_nodes = "\n".join(
        f'<text x="{22 + index * 26}" y="{29 + secrets.randbelow(5)}" '
        f'font-size="24" font-family="Arial, sans-serif" font-weight="700" '
        f'fill="#0f172a" transform="rotate({secrets.randbelow(21) - 10} {22 + index * 26} 25)">'
        f'{html.escape(char)}</text>'
        for index, char in enumerate(escaped)
    )
    svg = (
        '<svg xmlns="http://www.w3.org/2000/svg" width="140" height="44" '
        'viewBox="0 0 140 44">'
        '<rect width="140" height="44" rx="6" fill="#f8fafc"/>'
        f"{noise}{text_nodes}"
        "</svg>"
    )
    return base64.b64encode(svg.encode("utf-8")).decode("ascii")


def _required_redis(message: str):
    redis = get_redis()
    if redis is None:
        raise BusinessError(message)
    return redis
