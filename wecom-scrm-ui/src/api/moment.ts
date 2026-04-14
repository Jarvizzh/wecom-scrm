import request from './request'

export function createMoment(data: any) {
  return request({
    url: '/moments',
    method: 'post',
    data
  })
}

export function getMoments() {
  return request({
    url: '/moments',
    method: 'get'
  })
}

export function getMomentRecords(id: number) {
  return request({
    url: `/moments/${id}/records`,
    method: 'get'
  })
}

export function syncMomentStatuses() {
  return request({
    url: '/moments/sync',
    method: 'post'
  })
}
