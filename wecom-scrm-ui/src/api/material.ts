import request from './request'

export function getMaterials(params?: {
  type?: string
  title?: string
  sourceType?: string
  page: number
  size: number
}) {
  return request({
    url: '/materials',
    method: 'get',
    params
  })
}

export function getMaterial(id: number) {
  return request({
    url: `/materials/${id}`,
    method: 'get'
  })
}

export function saveMaterial(data: any) {
  return request({
    url: '/materials',
    method: 'post',
    data
  })
}

export function deleteMaterial(id: number) {
  return request({
    url: `/materials/${id}`,
    method: 'delete'
  })
}

export function uploadMaterialLocal(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/materials/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function saveMaterialBatch(data: any[]) {
  return request({
    url: '/materials/batch',
    method: 'post',
    data
  })
}
