import request from './request'

export function getWelcomeMsgs() {
  return request({
    url: '/welcome-msg',
    method: 'get'
  })
}

export function getWelcomeMsg(id: number) {
  return request({
    url: `/welcome-msg/${id}`,
    method: 'get'
  })
}

export function saveWelcomeMsg(data: any) {
  return request({
    url: '/welcome-msg',
    method: 'post',
    data
  })
}

export function deleteWelcomeMsg(id: number) {
  return request({
    url: `/welcome-msg/${id}`,
    method: 'delete'
  })
}
