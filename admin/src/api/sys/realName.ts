/** Author: Charlie */

import { API_PREFIX } from '@/constants/api'
import { http } from '@/utils'

export function getIdentityStatus() {
  return http.get<any>(`${API_PREFIX}/profile/identity/status`)
}

export function getCaseOptions() {
  return http.get<any>(`${API_PREFIX}/real-name/case/options`)
}

export function submitCase(data: {
  business_type?: string
  document_type: string
  real_name: string
  document_no: string
  attachment_ids?: string[]
  applicant_contact?: string | null
}) {
  return http.post<any>(`${API_PREFIX}/real-name/case/submit`, data)
}

export function initThirdParty(data: {
  business_type?: string
  document_type: string
  real_name: string
  document_no: string
  provider?: string | null
}) {
  return http.post<any>(`${API_PREFIX}/real-name/case/init-third-party`, data)
}

export function myCasePage(params?: { current?: number; size?: number }) {
  return http.get<any>(`${API_PREFIX}/real-name/case/my-page`, { params })
}

export function reviewPage(params?: {
  current?: number
  size?: number
  business_type?: string
  status?: string
  account_id?: string
}) {
  return http.get<any>(`${API_PREFIX}/sys/real-name-case/review-page`, { params })
}

export function reviewDetail(id: string) {
  return http.get<any>(`${API_PREFIX}/sys/real-name-case/detail`, { params: { id } })
}

export function approveCase(data: { id: string; remark?: string | null }) {
  return http.post<any>(`${API_PREFIX}/sys/real-name-case/approve`, { case_id: data.id, remark: data.remark })
}

export function rejectCase(data: { id: string; reject_reason: string }) {
  return http.post<any>(`${API_PREFIX}/sys/real-name-case/reject`, {
    case_id: data.id,
    reject_reason: data.reject_reason,
  })
}

export function identityPage(params?: {
  current?: number
  size?: number
  account_id?: string
  status?: string
  document_type?: string
}) {
  return http.get<any>(`${API_PREFIX}/sys/identity/page`, { params })
}

export function revokeIdentity(data: { account_id: string; reason?: string | null }) {
  return http.post<any>(`${API_PREFIX}/sys/identity/revoke`, {
    account_id: data.account_id,
    remark: data.reason,
  })
}
