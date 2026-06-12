from fastapi import APIRouter
from sdk.web.result import Result, success
from sdk.captcha import c_captcha, CaptchaResult

router = APIRouter()


@router.get(
    "/api/v1/public/c/captcha",
    summary="C端验证码",
    response_model=Result[CaptchaResult]
)
async def get_captcha():
    captcha_result = await c_captcha.get_captcha()
    return success(captcha_result.model_dump())
