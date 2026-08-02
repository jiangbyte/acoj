import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import { Input, Spin } from 'antd'
import * as authApi from '@/api/auth'

export type CaptchaInputHandle = {
  refresh: () => Promise<void>
}

type Props = {
  captchaId: string
  captchaValue: string
  onCaptchaIdChange: (value: string) => void
  onCaptchaValueChange: (value: string) => void
}

export const CaptchaInput = forwardRef<CaptchaInputHandle, Props>(function CaptchaInput(
  { captchaValue, onCaptchaIdChange, onCaptchaValueChange },
  ref,
) {
  const [loading, setLoading] = useState(false)
  const [imageBase64, setImageBase64] = useState('')
  const idChangeRef = useRef(onCaptchaIdChange)
  const valueChangeRef = useRef(onCaptchaValueChange)
  idChangeRef.current = onCaptchaIdChange
  valueChangeRef.current = onCaptchaValueChange

  async function refresh() {
    setLoading(true)
    try {
      const response = await authApi.captcha('svg')
      idChangeRef.current(response.data.captcha_id)
      valueChangeRef.current('')
      setImageBase64(response.data.image_base64)
    } finally {
      setLoading(false)
    }
  }

  useImperativeHandle(ref, () => ({ refresh }))

  useEffect(() => {
    void refresh()
  }, [])

  const imageSrc = imageBase64 ? `data:image/svg+xml;base64,${imageBase64}` : ''

  return (
    <div className="grid grid-cols-[minmax(0,1fr)_140px] gap-2.5 items-center">
      <Input
        value={captchaValue}
        placeholder="请输入验证码"
        allowClear
        onChange={(e) => onCaptchaValueChange(e.target.value)}
      />
      <button
        type="button"
        className="h-11 w-140px overflow-hidden rounded-md border border-gray-200 bg-gray-50 p-0 cursor-pointer disabled:cursor-wait"
        disabled={loading}
        onClick={() => void refresh()}
      >
        <Spin spinning={loading} size="small">
          {imageSrc ? <img src={imageSrc} alt="验证码" className="block h-11 w-140px" /> : null}
        </Spin>
      </button>
    </div>
  )
})
