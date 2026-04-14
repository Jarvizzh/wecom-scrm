import request from './request'

export function getWecomEvents(params: { page: number; size: number }) {
  return request({
    url: '/admin/wecom/events',
    method: 'get',
    params
  })
}
