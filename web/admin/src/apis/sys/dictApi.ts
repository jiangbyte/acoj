import { baseRequest } from '@/utils/http'

const request = (url: string, ...arg: any[]) => baseRequest<any>(`/api/v1/admin/dict/sys/dicts/${url}`, ...arg)

export default {
  dictList(data = {}) {
    return request('list', data, 'get')
  },
  dictDetail(data = {}) {
    return request('detail', data, 'get')
  },
  submitForm(data = {}, edit = false) {
    return request(edit ? 'update' : 'create', data)
  },
  dictDelete(data = {}) {
    return request('delete', data)
  },
  dictTree(data = {}) {
    return request('tree', data, 'get')
  },
}
