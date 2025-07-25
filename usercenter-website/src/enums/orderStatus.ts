export enum OrderStatusEnum {
  PENDING = 'PENDING',    // 待支付
  PAID = 'PAID',          // 已支付(商家待发货)
  PARTIAL_SHIPPED = 'PARTIAL_SHIPPED', // 商家部分发货
  SHIPPED = 'SHIPPED',    // 商家已发货
  COMPLETED = 'COMPLETED', // 交易成功
  CLOSED = 'CLOSED'       // 交易关闭
}

interface OrderStatusInfo {
  text: string;
  type: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

export const OrderStatusMap: Record<string, OrderStatusInfo> = {
  [OrderStatusEnum.PENDING]: {
    text: '待支付',
    type: 'warning'
  },
  [OrderStatusEnum.PAID]: {
    text: '已支付',
    type: 'success'
  },
  [OrderStatusEnum.PARTIAL_SHIPPED]: {
    text: '部分发货',
    type: 'primary'
  },
  [OrderStatusEnum.SHIPPED]: {
    text: '已发货',
    type: 'primary'
  },
  [OrderStatusEnum.COMPLETED]: {
    text: '交易成功',
    type: 'success'
  },
  [OrderStatusEnum.CLOSED]: {
    text: '交易关闭',
    type: 'danger'
  }
}

// 获取订单状态显示文本
export const getOrderStatusText = (status: string): string => {
  return OrderStatusMap[status]?.text || status;
}

// 获取订单状态标签类型
export const getOrderStatusType = (status: string): string => {
  return OrderStatusMap[status]?.type || 'info';
}

// 订单状态选项（用于下拉选择）
export const orderStatusOptions = Object.entries(OrderStatusMap).map(([value, info]) => ({
  value,
  label: info.text
}));