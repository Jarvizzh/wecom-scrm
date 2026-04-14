import request from './request'

export function getStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}
