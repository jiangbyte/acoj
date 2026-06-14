from fastapi import APIRouter, Request
from sdk.web.result import Result, success
from sdk.utils.sm2_crypto_util import get_public_key

router = APIRouter()


@router.get(
    "/api/v1/public/c/sm2/public-key",
    summary="获取SM2公钥(C端)",
    response_model=Result[str]
)
def get_sm2_public_key_c(request: Request):
    return success(get_public_key())
