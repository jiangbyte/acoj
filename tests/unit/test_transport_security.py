import base64

import pytest
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding

from app.core.exceptions.business import BusinessError
from app.core.security.transport import (
    create_captcha,
    create_password_key,
    decrypt_passwords,
    verify_captcha,
)


async def test_captcha_returns_base64_and_is_single_use(monkeypatch):
    monkeypatch.setattr("app.core.security.transport.secrets.choice", lambda alphabet: "A")
    monkeypatch.setattr("app.core.security.transport.secrets.randbelow", lambda maximum: 0)

    captcha = await create_captcha()

    assert captcha.captcha_id
    assert "<svg" in base64.b64decode(captcha.image_base64).decode("utf-8")

    await verify_captcha(captcha.captcha_id, "aaaa")
    with pytest.raises(BusinessError):
        await verify_captcha(captcha.captcha_id, "aaaa")


async def test_password_key_decrypts_rsa_oaep_ciphertext():
    key = await create_password_key()
    public_key = serialization.load_pem_public_key(key.public_key.encode("utf-8"))
    encrypted = public_key.encrypt(
        b"Secret@123456",
        padding.OAEP(
            mgf=padding.MGF1(algorithm=hashes.SHA256()),
            algorithm=hashes.SHA256(),
            label=None,
        ),
    )

    decrypted = await decrypt_passwords(key.key_id, base64.b64encode(encrypted).decode("ascii"))

    assert decrypted == ["Secret@123456"]
    with pytest.raises(BusinessError):
        await decrypt_passwords(key.key_id, base64.b64encode(encrypted).decode("ascii"))
