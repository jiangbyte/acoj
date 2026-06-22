import { baseRequest } from '@/utils/http'

const request = (url: string, ...arg: any[]) => baseRequest<any>(`/api/v1/admin/banner/sys/banners/${url}`, ...arg)

export default {
  bannerList(data = {}) {
    return request('list', data, 'get')
  },
  bannerDetail(data = {}) {
    return request('detail', data, 'get')
  },
  submitForm(data = {}, edit = false) {
    return request(edit ? 'update' : 'create', data)
  },
  bannerDelete(data = {}) {
    return request('delete', data)
  },
}
