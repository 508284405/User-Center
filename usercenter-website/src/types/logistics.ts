export interface FilterField {
  name: string
  label: string
  type: 'input' | 'select' | 'datePicker' | 'dateRange'
  options?: Array<{
    label: string
    value: string | number
  }>
  placeholder?: string
  defaultValue?: any
}

export interface TableColumn {
  prop: string
  label: string
  width?: number | string
  formatter?: (row: any) => string
  fixed?: 'left' | 'right'
}

export interface LogisticsOrder {
  id: number
  orderNo: string
  status: number
  statusText: string
  serviceType: string
  sourceChannel: string
  senderName: string
  senderPhone: string
  senderAddress: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  createTime: number
  updateTime: number
}

export interface LogisticsPackage {
  id: number
  logisticsOrderId: number
  waybillNo: string
  productName: string
  skuId: string
  qty: number
  weight: number
  length: number
  width: number
  height: number
  createTime: number
  updateTime: number
}

export interface LogisticsException {
  id: number
  logisticsOrderId: number
  packageId: number
  exceptionType: string
  exceptionDesc: string
  status: number
  statusText: string
  createTime: number
  updateTime: number
  resolveTime?: number
}

export interface LogisticsSLA {
  id: number
  logisticsOrderId: number
  packageId: number
  slaType: string
  plannedTime: number
  actualTime?: number
  status: string
  createTime: number
  updateTime: number
} 