import { http } from '~/composables/useHttp'

const prefix = '/api/v1/portal'

export function captcha(format: 'svg' | 'png' = 'svg') {
  return http.get<{
    captcha_id: string
    image_base64: string
    image_type: string
  }>(`${prefix}/captcha`, { params: { format }, addToken: false })
}

export function passwordKey() {
  return http.get<{
    key_id: string
    public_key: string
  }>(`${prefix}/password-key`, { addToken: false })
}

export function login(data: {
  account: string
  password: string
  password_key_id: string
  captcha_id: string
  captcha_value: string
  identity_type?: string
  remember_me?: boolean
}) {
  return http.post<{
    token: string
    account_id: string
    account_type: string
    password_expired: boolean
  }>(`${prefix}/login`, data, { addToken: false })
}

export function register(data: {
  account: string
  password: string
  password_key_id: string
  captcha_id: string
  captcha_value: string
  nickname: string
  email: string
  name?: string
}) {
  return http.post<{
    account_id: string
    account: string
    account_type: string
  }>(`${prefix}/register`, data, { addToken: false })
}

export interface PortalMeProfile {
  account_id: string
  name: string | null
  nickname: string | null
  avatar: string | null
  signature: string | null
  phone: string | null
  email: string | null
  phone_login_enabled: boolean
  email_login_enabled: boolean
  created_at: string
  updated_at: string
}

export interface PortalMeResponse {
  account_id: string
  account: string
  account_type: string
  name: string | null
  nickname: string | null
  avatar: string | null
  role_ids: string[]
  dept_ids: string[]
  group_ids: string[]
  role_id_names: { id: string; name: string }[]
  dept_id_names: { id: string; name: string }[]
  group_id_names: { id: string; name: string }[]
  permission_keys: string[]
  button_codes: string[]
  profile: PortalMeProfile
}

export function me() {
  return http.get<PortalMeResponse>(`${prefix}/me`)
}

export function forgotPassword(data: { email: string; captcha_id: string; captcha_value: string }) {
  return http.post<null>(`${prefix}/forgot-password`, data, { addToken: false })
}

export function resetPassword(data: {
  email: string
  token: string
  password: string
  password_key_id: string
  captcha_id: string
  captcha_value: string
}) {
  return http.post<null>(`${prefix}/reset-password`, data, { addToken: false })
}

export function logout() {
  return http.post<null>(`${prefix}/logout`)
}

// --- User Center ---

export function updateProfile(data: {
  name?: string | null
  nickname?: string | null
  signature?: string | null
}) {
  return http.post<null>(`${prefix}/user-center/profile/update`, data)
}

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<{
    avatar: string
    file_id: string
    object_name: string
    url: string
  }>(`${prefix}/user-center/avatar/upload`, formData)
}

export function updatePassword(data: {
  old_password: string
  new_password: string
  password_key_id: string
}) {
  return http.post<null>(`${prefix}/user-center/password/update`, data)
}

export function updatePhone(data: {
  password: string
  phone: string | null
  phone_login_enabled: boolean
  password_key_id: string
}) {
  return http.post<null>(`${prefix}/user-center/phone/update`, data)
}

export function updateEmail(data: {
  password: string
  email: string | null
  email_login_enabled: boolean
  password_key_id: string
}) {
  return http.post<null>(`${prefix}/user-center/email/update`, data)
}
