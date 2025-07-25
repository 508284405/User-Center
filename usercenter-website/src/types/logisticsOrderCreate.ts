export interface CreateLogisticsOrderParams {
  vendorId?: number;
  vendorName?: string;
  refNo?: string;
  providerCode?: string;
  bizType?: string;
  expectedPickupAt?: string;
  senderName?: string;
  senderMobile?: string;
  senderProvince?: string;
  senderCity?: string;
  senderDistrict?: string;
  senderAddress?: string;
  receiverName?: string;
  receiverMobile?: string;
  receiverProvince?: string;
  receiverCity?: string;
  receiverDistrict?: string;
  receiverAddress?: string;
  packages?: Array<{
    productName?: string;
    qty?: number;
    skuId?: number;
    weight?: number;
    length?: number;
    width?: number;
    height?: number;
  }>;
} 