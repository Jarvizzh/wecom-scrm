import request from './request'

export function uploadMedia(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/media/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function uploadImg(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/media/uploadImg',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function uploadTempMedia(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/media/uploadTemp',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function getImageUrl(mediaId?: string, picUrl?: string) {
  if (picUrl) return picUrl
  if (mediaId && mediaId !== '-') {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
    const corpId = localStorage.getItem('currentCorpId')
    return `${baseUrl}/media/image/${corpId}/${mediaId}`
  }
  return ''
}
