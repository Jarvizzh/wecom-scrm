import request from './request'

// Fetch paginated user list
export const getUsers = (params: { departmentId?: number; page: number; size: number; keyword?: string }) => {
  return request.get('/admin/users', { params })
}

// Fetch all departments
export const getDepartments = () => {
  return request.get('/admin/users/departments')
}

// Update user SCRM status
export const updateUserStatus = (userid: string, scrmStatus: number) => {
  return request.post(`/admin/users/${userid}/status`, { scrmStatus })
}

// Trigger user sync from WeCom
export const syncUsers = () => {
  return request.post('/admin/sync/users')
}
