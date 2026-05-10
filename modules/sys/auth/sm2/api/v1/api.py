from fastapi import APIRouter
from core.result import Result, success
from core.utils.sm2_crypto_util import get_public_key

router = APIRouter()


@router.get(
    "/api/v1/public/b/sm2/public-key",
    summary="获取SM2公钥(B端)",
    response_model=Result[str]
)
async def get_sm2_public_key_b():
    return success(get_public_key())


@router.get(
    "/api/v1/public/c/sm2/public-key",
    summary="获取SM2公钥(C端)",
    response_model=Result[str]
)
async def get_sm2_public_key_c():
    return success(get_public_key())
