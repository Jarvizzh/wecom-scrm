import request from './request'

export interface CustomerMessage {
  id?: number
  taskName: string
  sendType: number // 0: Immediate, 1: Scheduled
  sendTime?: string
  targetType: number // 0: All, 1: Filtered
  targetCondition?: TargetCondition
  text: string
  attachments?: any[]
  senderList?: string[]
  status?: number // 0: Pending, 1: Sending, 2: Finished, 3: Failed
  failMsg?: string
  creatorUserid?: string
  createTime?: string
}

export interface TargetCondition {
  addTimeStart?: string
  addTimeEnd?: string
  includeTags?: string[]
  includeTagsAny?: boolean
  excludeTags?: string[]
}

export function createCustomerMessage(data: CustomerMessage) {
  return request({
    url: '/customer-message',
    method: 'post',
    data
  })
}

export function getCustomerMessageList(params?: { page: number; size: number }) {
  return request({
    url: '/customer-message/list',
    method: 'get',
    params
  })
}

export function getCustomerMessage(id: number | string) {
  return request({
    url: `/customer-message/${id}`,
    method: 'get'
  })
}

export function updateCustomerMessage(id: number | string, data: any) {
  return request({
    url: `/customer-message/${id}`,
    method: 'put',
    data
  })
}

export function deleteCustomerMessage(id: number | string) {
  return request({
    url: `/customer-message/${id}`,
    method: 'delete'
  })
}
export function getCustomerMessageSendResult(id: number) {
  return request({
    url: `/customer-message/${id}/send-result`,
    method: 'get'
  })
}

export function getMemberSendResult(id: number, userid: string) {
  return request({
    url: `/customer-message/${id}/send-result/${userid}`,
    method: 'get'
  })
}

export interface CustomerMessageLoop {
  id?: number
  taskName: string
  targetType: number // 0: All, 1: Filtered
  targetCondition?: TargetCondition
  text: string
  attachments?: any[]
  senderList?: string[]
  loopType: number // 1: Daily, 2: Weekly
  loopDayOfWeek?: string // e.g. "1,2,3" (1=Mon, 7=Sun)
  sendTimeOfDay: string // HH:mm:ss
  lastTriggerTime?: string
  status?: number // 0: Disabled, 1: Enabled
  createTime?: string
}

export function createCustomerMessageLoop(data: CustomerMessageLoop) {
  return request({
    url: '/customer-message-loop',
    method: 'post',
    data
  })
}

export function getCustomerMessageLoopList(params?: { page: number; size: number }) {
  return request({
    url: '/customer-message-loop/list',
    method: 'get',
    params
  })
}

export function getCustomerMessageLoop(id: number | string) {
  return request({
    url: `/customer-message-loop/${id}`,
    method: 'get'
  })
}

export function updateCustomerMessageLoop(id: number | string, data: any) {
  return request({
    url: `/customer-message-loop/${id}`,
    method: 'put',
    data
  })
}

export function deleteCustomerMessageLoop(id: number | string) {
  return request({
    url: `/customer-message-loop/${id}`,
    method: 'delete'
  })
}

