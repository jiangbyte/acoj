"""
SM2 国密算法工具类

用于解密前端传回的加密密码
前端使用 sm-crypto 库（cipherMode = 1，即 C1C3C2 模式）
后端使用 gmssl 库（默认 C1C2C3 模式）

关键：需要将 C1C3C2 格式的密文转换为 C1C2C3 格式再解密
"""

from typing import Tuple, Optional
from gmssl import sm2, sm3, func
import secrets

_private_key: Optional[str] = None
_public_key: Optional[str] = None
_sm2_crypt: Optional[sm2.CryptSM2] = None


def init(private_key: str, public_key: str):
    if not private_key or not private_key.strip():
        raise ValueError("Private key cannot be null or empty")
    if not public_key or not public_key.strip():
        raise ValueError("Public key cannot be null or empty")
    
    global _private_key, _public_key, _sm2_crypt
    _private_key = private_key.strip().lower().replace('0x', '')
    public_key = public_key.strip().lower().replace('0x', '')
    _public_key = public_key
    
    _sm2_crypt = sm2.CryptSM2(
        private_key=_private_key,
        public_key=_public_key
    )


def encrypt(plaintext: str) -> str:
    if _sm2_crypt is None:
        raise RuntimeError("SM2 has not been initialized. Please call init(private_key, public_key) first.")
    
    encrypted = _sm2_crypt.encrypt(plaintext.encode('utf-8'))
    if not encrypted:
        raise RuntimeError("SM2 encryption failed")
    return encrypted.hex()


def encrypt_c1c3c2(plaintext: str) -> str:
    if _sm2_crypt is None:
        raise RuntimeError("SM2 has not been initialized. Please call init(private_key, public_key) first.")
    
    encrypted = _sm2_crypt.encrypt(plaintext.encode('utf-8'))
    if not encrypted:
        raise RuntimeError("SM2 encryption failed")
    
    c1 = encrypted[:64]
    c2 = encrypted[64:-32]
    c3 = encrypted[-32:]
    
    c1c3c2 = c1 + c3 + c2
    return c1c3c2.hex()


def decrypt(ciphertext: str) -> str:
    raw = decrypt_raw(ciphertext)
    if raw.startswith(b'\xef\xbb\xbf'):
        raw = raw[3:]
    return raw.decode('utf-8')


def decrypt_raw(ciphertext: str) -> bytes:
    if _sm2_crypt is None:
        raise RuntimeError("SM2 has not been initialized. Please call init(private_key, public_key) first.")
    
    if ciphertext.startswith('04'):
        ciphertext = ciphertext[2:]
    
    ciphertext_bytes = bytes.fromhex(ciphertext)
    
    if len(ciphertext_bytes) < 96:
        raise ValueError("密文长度不足，格式不正确")
    
    # 先尝试直接解密（C1C2C3 格式）
    decrypted = _sm2_crypt.decrypt(ciphertext_bytes)
    if decrypted:
        try:
            decrypted.decode('utf-8')
            return decrypted
        except UnicodeDecodeError:
            pass
    
    # 如果失败，尝试将 C1C3C2 格式转换为 C1C2C3 格式
    c1 = ciphertext_bytes[:64]
    remaining = ciphertext_bytes[64:]
    
    c3_from_c1c3c2 = remaining[:32]
    c2_from_c1c3c2 = remaining[32:]
    c1c2c3_from_c1c3c2 = c1 + c2_from_c1c3c2 + c3_from_c1c3c2
    
    decrypted = _sm2_crypt.decrypt(c1c2c3_from_c1c3c2)
    if decrypted:
        try:
            decrypted.decode('utf-8')
            return decrypted
        except UnicodeDecodeError:
            pass
    
    raise RuntimeError("SM2 decryption failed, invalid ciphertext or wrong key")


def hash_with_salt(data: str, salt: str) -> str:
    return sm3.sm3_hash(func.bytes_to_list((data + salt).encode('utf-8')))


def gen_salt(length: int = 16) -> str:
    return secrets.token_hex(length // 2)


def gen_keypair() -> Tuple[str, str]:
    private_key = secrets.token_hex(32)
    sm2_crypt = sm2.CryptSM2(public_key="", private_key="")
    public_key = sm2_crypt._kg(int(private_key, 16), sm2_crypt.ecc_table['g'])
    return private_key, public_key


def get_public_key() -> str:
    if not _public_key:
        raise RuntimeError("SM2 has not been initialized.")
    return _public_key
