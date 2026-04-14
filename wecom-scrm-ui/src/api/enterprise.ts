import request from './request'

export function getEnterprises() {
  return request({
    url: '/enterprises',
    method: 'get'
  })
}
