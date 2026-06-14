from fastapi import APIRouter, Request
from sdk.web.result import Result, success
from sdk.captcha import b_captcha, CaptchaResult

router = APIRouter()


@router.get(
    "/api/v1/public/b/captcha",
    summary="B端验证码",
    response_model=Result[CaptchaResult]
)
async def get_captcha(request: Request):
    return success(await b_captcha.get_captcha())
