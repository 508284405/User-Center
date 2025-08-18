<template>
  <div class="order-detail-container">
    <div class="page-header">
      <el-page-header @back="goBack" :content="`订单详情: ${orderNumber}`">
      </el-page-header>
    </div>

    <div class="detail-content" v-loading="loading" v-if="orderDetail">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <div>
              <el-tag :type="getOrderStatusType(orderDetail.orderStatus)">
                {{ getOrderStatusText(orderDetail.orderStatus) }}
              </el-tag>
              <el-tooltip
                :disabled="isAddressComplete"
                :content="shipButtonDisabledReason"
                placement="top"
                effect="dark"
              >
                <el-button
                  type="primary"
                  size="small"
                  v-if="orderDetail.orderStatus === OrderStatusEnum.PAID"
                  @click="showShipDialog"
                  style="margin-left: 10px;"
                  :disabled="!isAddressComplete"
                >发货</el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
        <div class="info-section">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="订单编号">{{ orderDetail.orderNumber }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ orderDetail.userId }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ orderDetail.userName }}</el-descriptions-item>
            <el-descriptions-item label="总金额">¥{{ orderDetail.totalAmount?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ orderDetail.paymentMethod || '未指定' }}</el-descriptions-item>
            <el-descriptions-item label="交易ID">{{ orderDetail.transactionId || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(orderDetail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatDate(orderDetail.payTime) || '未支付' }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ formatDate(orderDetail.shipTime) || '未发货' }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatDate(orderDetail.completeTime) || '未完成' }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ formatDate(orderDetail.expireTime) || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
         <div v-if="orderDetail.paymentForm" class="info-section">
           <el-descriptions title="支付表单信息" :column="1" border>
             <el-descriptions-item label="支付表单">
                <el-input type="textarea" :value="orderDetail.paymentForm" :rows="4" readonly />
                <el-button size="small" @click="copyPaymentForm" style="margin-top: 5px;">复制</el-button>
             </el-descriptions-item>
           </el-descriptions>
         </div>
      </el-card>

      <el-card class="box-card" v-if="orderDetail.address">
        <template #header>
          <div class="card-header">
            <span>收货信息</span>
          </div>
        </template>
        <div class="info-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收件人">{{ orderDetail.address?.recipient }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ orderDetail.address?.phone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ orderDetail.address?.province }} {{ orderDetail.address?.city }} {{ orderDetail.address?.district }} {{ orderDetail.address?.detailAddress }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <el-card class="box-card" v-if="orderDetail.trackingNumber || orderDetail.carrier">
        <template #header>
          <div class="card-header">
            <span>物流信息</span>
          </div>
        </template>
        <div class="info-section">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="物流公司">{{ orderDetail.carrier || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ orderDetail.trackingNumber || '未设置' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <el-card class="box-card" v-if="orderDetail.refundReason || orderDetail.refundStatus">
        <template #header>
          <div class="card-header">
            <span>退款信息</span>
          </div>
        </template>
        <div class="info-section">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="退款状态">{{ orderDetail.refundStatus || '未申请退款' }}</el-descriptions-item>
            <el-descriptions-item label="退款原因">{{ orderDetail.refundReason || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>订单商品</span>
          </div>
        </template>
        <div class="info-section">
          <el-table :data="orderDetail.items || []" border style="width: 100%">
            <el-table-column prop="productName" label="商品名称"></el-table-column>
            <el-table-column label="商品图片" width="100">
               <template #default="scope">
                 <el-image
                   style="width: 60px; height: 60px"
                   :src="scope.row.productImage"
                   :preview-src-list="[scope.row.productImage]"
                   fit="cover"
                   hide-on-click-modal
                   preview-teleported
                 />
               </template>
            </el-table-column>
            <el-table-column prop="skuId" label="SKU ID" width="180"></el-table-column>
             <el-table-column label="SKU属性" width="180">
               <template #default="scope">
                 <div v-for="(value, key) in scope.row.skuProperties" :key="key">
                   {{ key }}: {{ value }}
                 </div>
               </template>
             </el-table-column>
            <el-table-column label="SKU图片" width="100">
               <template #default="scope">
                 <el-image
                   v-if="scope.row.skuImage"
                   style="width: 60px; height: 60px"
                   :src="scope.row.skuImage"
                   :preview-src-list="[scope.row.skuImage]"
                   fit="cover"
                   hide-on-click-modal
                   preview-teleported
                 />
                 <span v-else>无</span>
               </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="120">
              <template #default="scope">
                ¥{{ scope.row.unitPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="总价" width="120">
              <template #default="scope">
                ¥{{ scope.row.totalPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="skuDescription" label="SKU描述"></el-table-column>
            <el-table-column prop="afterSaleNo" label="售后单号" width="180">
              <template #default="scope">
                {{ scope.row.afterSaleNo || '无' }}
              </template>
            </el-table-column>
            <el-table-column prop="afterSaleStatus" label="售后状态" width="180">
              <template #default="scope">
                {{ scope.row.afterSaleStatus || '无' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <div class="action-buttons">
        <!-- 返回按钮已移除，使用页面顶部返回箭头 -->
        <!-- 发货按钮已移至订单状态旁边 -->
        <el-button
          type="warning"
          v-if="false" 
          @click="showRefundDialog"
        >处理退款</el-button>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="未找到订单信息"></el-empty>


    <!-- 发货对话框 -->
    <el-dialog title="订单发货" v-model="shipDialogVisible" width="500px">
      <el-form :model="shipForm" label-width="100px" :rules="getShipRules" ref="shipFormRef">
        <el-form-item label="发货方式">
          <el-radio-group v-model="shipForm.noLogistics">
            <el-radio :label="false">物流发货</el-radio>
            <el-radio :label="true">无物流发货</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="订单编号" prop="orderNumber">
          <el-input v-model="shipForm.orderNumber" placeholder="请输入订单编号" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="订单项" prop="orderItemId">
          <el-select 
            v-model="shipForm.orderItemId" 
            multiple 
            placeholder="已选择全部订单项"
            style="width: 100%;"
            disabled
          >
            <el-option 
              v-for="item in orderDetail?.items" 
              :key="item.id" 
              :label="`${item.productName} x${item.quantity}`" 
              :value="item.id.toString()"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <template v-if="!shipForm.noLogistics">
          <el-form-item label="物流单号" prop="waybillNo">
            <el-input v-model="shipForm.waybillNo" placeholder="请输入物流单号"></el-input>
          </el-form-item>
          <el-form-item label="物流公司" prop="carrier">
            <el-input v-model="shipForm.carrier" placeholder="请输入物流公司"></el-input>
          </el-form-item>
        </template>
        
        <el-alert
          v-if="shipForm.noLogistics"
          title="您已选择无物流发货，适用于距离过近无需物流系统的情况"
          type="info"
          show-icon
          :closable="false"
        />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmShip">确认发货</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退款处理对话框 -->
    <el-dialog title="退款处理" v-model="refundDialogVisible" width="500px">
      <el-form :model="refundForm" label-width="100px" :rules="refundRules" ref="refundFormRef">
        <el-form-item label="退款原因">
          <div>{{ orderDetail?.refundReason || '无' }}</div>
        </el-form-item>
        <el-form-item label="处理结果" prop="approved">
          <el-radio-group v-model="refundForm.approved">
            <el-radio :label="true">同意退款</el-radio>
            <el-radio :label="false">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="comment">
          <el-input type="textarea" v-model="refundForm.comment" rows="3" placeholder="请输入处理备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRefund">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, Ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, FormInstance } from 'element-plus';
import { getOrderDetail, shipOrder, processRefund, OrderDetail } from '@/api/client-web/order';
import { OrderStatusEnum, getOrderStatusText, getOrderStatusType } from '@/enums/orderStatus';
import dayjs from 'dayjs';

interface ShipOrderParams {
  orderNumber: string;
  orderItemId: string[];
  waybillNo?: string;
  carrier?: string;
  noLogistics: boolean;
}

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const orderDetail: Ref<OrderDetail | null> = ref(null);
const orderNumber = ref(route.params.orderNumber as string);

const shipDialogVisible = ref(false);
const refundDialogVisible = ref(false);
const shipFormRef = ref<FormInstance | null>(null);
const refundFormRef = ref<FormInstance | null>(null);

const shipForm = reactive({
  orderNumber: '',
  orderItemId: [] as string[],
  waybillNo: '',
  carrier: '',
  noLogistics: false
});

const refundForm = reactive({
  approved: true,
  comment: ''
});

const refundRules = {
  // 拒绝退款时，备注必填
  comment: [{
    required: true,
    validator: (rule: any, value: string, callback: Function) => {
      if (!refundForm.approved && !value) {
        callback(new Error('拒绝退款时必须填写备注'));
      } else {
        callback();
      }
    },
    trigger: 'blur'
  }]
};

const getShipRules = computed(() => {
  if (shipForm.noLogistics) {
    // 无物流发货时，物流单号和承运商不是必填项
    return {
      orderNumber: [{ required: true, message: '请输入订单编号', trigger: 'blur' }],
      orderItemId: [{ required: true, message: '请至少选择一个订单项', trigger: 'change' }]
    };
  } else {
    // 物流发货时，物流单号和承运商是必填项
    return {
      orderNumber: [{ required: true, message: '请输入订单编号', trigger: 'blur' }],
      orderItemId: [{ required: true, message: '请至少选择一个订单项', trigger: 'change' }],
      waybillNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
      carrier: [{ required: true, message: '请输入物流公司', trigger: 'blur' }]
    };
  }
});

const formatDate = (timestamp?: number): string => {
  if (!timestamp) return '';
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss');
};

// 检查收货信息是否完整
const isAddressComplete = computed(() => {
  if (!orderDetail.value?.address) return false;
  const { recipient, phone, province, city, district, detailAddress } = orderDetail.value.address;
  return !!(recipient && phone && province && city && district && detailAddress);
});

// 获取发货按钮禁用原因
const shipButtonDisabledReason = computed(() => {
  if (!orderDetail.value?.address) return '缺少收货信息';
  const { recipient, phone, province, city, district, detailAddress } = orderDetail.value.address;
  
  const missingFields = [];
  if (!recipient) missingFields.push('收货人');
  if (!phone) missingFields.push('联系电话');
  if (!province || !city || !district || !detailAddress) missingFields.push('收货地址');
  
  return missingFields.length > 0 ? `缺少${missingFields.join('、')}` : '';
});

const fetchOrderDetail = async () => {
  if (!orderNumber.value) return;

  loading.value = true;
  orderDetail.value = null; // 重置
  try {
    const res = await getOrderDetail(orderNumber.value);
    if (res.success && res.data) {
      orderDetail.value = res.data;
    } else {
      ElMessage.error(res.errMessage || '未找到订单信息');
    }
  } catch (error: any) {
    console.error('获取订单详情失败:', error);
    ElMessage.error(`获取订单详情失败: ${error.message || ''}`);
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push({ name: 'OrderList' });
};

const showShipDialog = () => {
  if (!orderDetail.value) return;
  shipForm.orderNumber = orderDetail.value.orderNumber || '';
  shipForm.orderItemId = orderDetail.value.items?.map(item => item.id.toString()) || [];
  shipForm.waybillNo = '';
  shipForm.carrier = '';
  shipForm.noLogistics = false;
  shipDialogVisible.value = true;
  // 使用 nextTick 确保 DOM 更新完成
  shipFormRef.value?.resetFields();
  // 默认选择所有订单项
  shipForm.orderItemId = orderDetail.value.items?.map(item => item.id.toString()) || [];
};

const showRefundDialog = () => {
  refundForm.approved = true;
  refundForm.comment = '';
  refundDialogVisible.value = true;
   refundFormRef.value?.resetFields();
};

const confirmShip = async () => {
  if (!shipFormRef.value || !orderDetail.value?.orderNumber) return;

  await shipFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 构建请求参数，根据是否无物流发货设置相应的参数
        const params: ShipOrderParams = {
          orderNumber: shipForm.orderNumber,
          orderItemId: shipForm.orderItemId,
          noLogistics: shipForm.noLogistics
        };

        // 如果不是无物流发货，则需要添加物流信息
        if (!shipForm.noLogistics) {
          params.waybillNo = shipForm.waybillNo;
          params.carrier = shipForm.carrier;
        }

        await shipOrder(orderDetail.value!.orderNumber, params);
        ElMessage.success('发货成功');
        shipDialogVisible.value = false;
        fetchOrderDetail(); // 刷新详情
      } catch (error: any) {
        console.error('发货失败:', error);
        ElMessage.error(`发货失败: ${error.message || ''}`);
      }
    }
  });
};

const confirmRefund = async () => {
  if (!refundFormRef.value || !orderDetail.value?.orderNumber) return;

  await refundFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await processRefund(orderDetail.value!.orderNumber, { ...refundForm });
        ElMessage.success('退款处理成功');
        refundDialogVisible.value = false;
        fetchOrderDetail(); // 刷新详情
      } catch (error: any) {
        console.error('退款处理失败:', error);
        ElMessage.error(`退款处理失败: ${error.message || ''}`);
      }
    }
  });
};

// 复制支付表单内容
const copyPaymentForm = () => {
  if (orderDetail.value?.paymentForm) {
    navigator.clipboard.writeText(orderDetail.value.paymentForm).then(() => {
      ElMessage.success('支付表单已复制到剪贴板');
    }, () => {
      ElMessage.error('复制失败');
    });
  }
};


onMounted(() => {
  fetchOrderDetail();
});
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.detail-content {
  margin-top: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-section {
  margin: 10px 0;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>