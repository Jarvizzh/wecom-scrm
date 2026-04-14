import request from './request'

export function getSyncLogs(params: { page: number; size: number }) {
  return request({
    url: '/admin/sync/logs',
    method: 'get',
    params
  })
}
