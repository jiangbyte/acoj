# HTTP 请求封装

框架基于 Alova 3.x 封装了 HTTP 请求层，提供双适配器支持和统一的错误处理。

## 请求实例

### 默认请求

```typescript
import { request } from '@/utils'

export function fetchUserPage(params: any) {
  return request.Get('/api/v1/sys/user/page', { params })
}
```

### 上传请求

```typescript
import { uploadRequest } from '@/utils'

// 上传专用实例，使用 XHR adapter 以支持上传进度
uploadRequest.Post('/api/v1/sys/file/upload', formData)
```

### 无 baseURL 请求

```typescript
import { blankInstance } from '@/utils'

// 适用于外部链接或 blob 下载
blankInstance.Get('https://example.com/file.pdf')
```

## 统一配置

```typescript
// src/utils/http/config.ts
export const DEFAULT_ALOVA_OPTIONS = {
  timeout: 15 * 1000,  // 15s 超时
}

export const DEFAULT_BACKEND_OPTIONS = {
  codeKey: 'code',
  dataKey: 'data',
  msgKey: 'message',
  successCode: 200,
}
```

HTTP 状态码映射：

| 状态码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 登录已过期，请重新登录 |
| 403 | 无权限访问 |
| 404 | 请求资源不存在 |
| 405 | 请求方法不允许 |
| 500 | 服务器内部错误 |
| 502 | 网关错误 |
| 503 | 服务不可用 |
| 504 | 网关超时 |

## 请求拦截

每次请求自动携带当前用户的 Token：

```typescript
const { onAuthRequired } = createServerTokenAuthentication({
  assignToken: method => {
    const authStore = useAuthStore()
    if (authStore.token) {
      method.config.headers.Authorization = `${authStore.token}`
    }
  },
})
```

## 响应拦截

### 成功处理

```typescript
onSuccess: async (response, method) => {
  const { status } = response

  if (status === 200) {
    const apiData = await response.json()
    // Token 过期（业务 code 401）
    if (apiData.code === 401) {
      forceLogout()
      return { ...apiData, success: false }
    }
    // 业务成功
    if (apiData.code === 200) {
      return { ...apiData, success: true, data: apiData.data }
    }
    // 其他业务错误
    return handleBusinessError(apiData, bc)
  }

  if (status === 401) {
    forceLogout()
  }

  return handleResponseError(response)
}
```

### 网络错误处理

```typescript
onError: (error, method) => {
  const msg = error.message || '网络错误'
  console.error(`[${method.type}] ${method.url}:`, msg)
  message.error(msg)
}
```

## API 规范

### 后端响应格式

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {},
  "success": true,
  "trace_id": "uuid-string"
}
```

### 分页响应

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "records": [],
    "total": 100,
    "current": 1,
    "size": 20
  },
  "success": true
}
```

### API 路径规则

| 模式 | 说明 | 示例 |
|------|------|------|
| `/api/v1/public/b/*` | B 端公开接口（登录、验证码、SM2 公钥） | `/api/v1/public/b/login` |
| `/api/v1/public/c/*` | C 端公开接口 | `/api/v1/public/c/login` |
| `/api/v1/sys/*` | B 端业务接口 | `/api/v1/sys/user/page` |
| `/api/v1/client/*` | C 端业务接口 | `/api/v1/client/user/page` |

## API 层定义

```typescript
// src/api/user.ts
import { request } from '@/utils'

export function fetchUserPage(params: any) {
  return request.Get('/api/v1/sys/user/page', { params })
}
export function fetchUserCreate(data: any) {
  return request.Post('/api/v1/sys/user/create', data)
}
export function fetchUserModify(data: any) {
  return request.Post('/api/v1/sys/user/modify', data)
}
export function fetchUserRemove(data: any) {
  return request.Post('/api/v1/sys/user/remove', data)
}
export function fetchUserDetail(params: any) {
  return request.Get('/api/v1/sys/user/detail', { params })
}
```
