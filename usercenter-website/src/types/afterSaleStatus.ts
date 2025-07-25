export enum AfterSaleStatus {
  APPLYING = 'APPLYING',                  // 用户提交售后申请
  APPROVED = 'APPROVED',                  // 审核通过
  REJECTED = 'REJECTED',                  // 审核拒绝
  CANCELED = 'CANCELED',                  // 用户撤销申请
  WAIT_BUYER_RETURN_GOODS = 'WAIT_BUYER_RETURN_GOODS', // 等待用户退货（退货退款/换货）
  WAIT_SELLER_CONFIRM = 'WAIT_SELLER_CONFIRM',         // 商家待收货（退货退款）
  WAIT_USER_RETURN_GOODS = 'WAIT_USER_RETURN_GOODS',   // 换货流程中等待用户退货
  WAIT_SELLER_RESEND = 'WAIT_SELLER_RESEND',           // 商家等待发货（换货）
  REFUNDING = 'REFUNDING',                  // 退款中（包括仅退款、退货退款）
  RESENT = 'RESENT',                        // 商家已发货（换货）
  COMPLETED = 'COMPLETED'                   // 完成
} 