import request from './request'

export function getSysUsers() {
  return request.get('/sys/users')
}

export function createSysUser(data: any) {
  return request.post('/sys/users', data)
}

export function updateSysUser(data: any) {
  return request.put('/sys/users', data)
}

export function updateSysUserPassword(id: number, data: any) {
  return request.put(`/sys/users/${id}/password`, data)
}

export function deleteSysUser(id: number) {
  return request.delete(`/sys/users/${id}`)
}
