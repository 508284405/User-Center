<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { afterSalesApi, type AfterSalesItem } from '@/api/client-web/afterSales'
import { AfterSaleStatus } from '@/types/afterSaleStatus'
import { AfterSaleType } from '@/types/afterSaleType'
import RejectDialog from './components/RejectDialog.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('detail')
const showRejectDialog = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

// 售后详情数据
const detailData = ref<AfterSalesItem>({
  id: '',
  orderNo: '',
  userId: '',
  username: '',
  afterSaleNo: '',
  phone: '',
  reason: '',
  status: AfterSaleStatus.APPLYING,
  afterSaleType: AfterSaleType.ONLY_REFUND,
  createTime: '',
  updateTime: '',
  applyAmount: 0,
  attachments: [],
  description: '',
  productId: 0,
  skuId: 0,
  productName: '',
  skuSpec: '',
  skuImage: '',
  quantity: 0,
  isGift: false
})

// 计算属性：单个商品
const product = computed(() => detailData.value);

// 格式化状态
const formatStatus = (status: string) => {
  const statusMap: Record<AfterSaleStatus, { label: string; type: string }> = {
    [AfterSaleStatus.APPLYING]: { label: '申请中', type: 'info' },
    [AfterSaleStatus.APPROVED]: { label: '审核通过', type: 'success' },
    [AfterSaleStatus.REJECTED]: { label: '审核拒绝', type: 'danger' },
    [AfterSaleStatus.CANCELED]: { label: '已撤销', type: 'info' },
    [AfterSaleStatus.WAIT_BUYER_RETURN_GOODS]: { label: '等待用户退货', type: 'warning' },
    [AfterSaleStatus.WAIT_SELLER_CONFIRM]: { label: '商家待收货', type: 'warning' },
    [AfterSaleStatus.WAIT_USER_RETURN_GOODS]: { label: '换货待退货', type: 'warning' },
    [AfterSaleStatus.WAIT_SELLER_RESEND]: { label: '商家待发货', type: 'warning' },
    [AfterSaleStatus.REFUNDING]: { label: '退款中', type: 'primary' },
    [AfterSaleStatus.RESENT]: { label: '已发货', type: 'success' },
    [AfterSaleStatus.COMPLETED]: { label: '已完成', type: 'success' }
  }
  return statusMap[status as AfterSaleStatus] || { label: '未知', type: 'info' }
}

// 售后类型格式化
const formatType = (type: AfterSaleType) => {
  return {
    [AfterSaleType.ONLY_REFUND]: '仅退款',
    [AfterSaleType.RETURN_REFUND]: '退货退款',
    [AfterSaleType.EXCHANGE]: '换货'
  }[type] || '未知';
}

// 获取售后详情
const getDetail = async () => {
  const afterSaleNo = route.params.id as string
  if (!afterSaleNo) {
    ElMessage.error('售后单号不能为空')
    return
  }

  loading.value = true
  try {
    const res: any = await afterSalesApi.getAfterSalesDetail(afterSaleNo)
    // 映射常用字段
    detailData.value = {
      ...detailData.value,
      ...res.data
    }
  } catch (error) {
    console.error('获取售后详情失败:', error)
    ElMessage.error('获取售后详情失败')
  } finally {
    loading.value = false
  }
}

// 审核通过
const handleApprove = async () => {
  try {
    await ElMessageBox.confirm('确认审核通过此售后申请?', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    console.log(detailData.value)
    const res = await afterSalesApi.approveAfterSales({
      afterSaleNo: detailData.value.afterSaleNo
    })
    
    ElMessage.success('审核通过成功')
    getDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 打开拒绝弹窗
const openRejectDialog = () => {
  showRejectDialog.value = true
}

// 拒绝申请
const handleReject = async (reason: string) => {
  try {
    await afterSalesApi.rejectAfterSales({
      afterSaleNo: detailData.value.afterSaleNo,
      reason: reason
    })
    
    ElMessage.success('已拒绝售后申请')
    showRejectDialog.value = false
    getDetail()
  } catch (error) {
    console.error('拒绝售后失败:', error)
    ElMessage.error('操作失败')
  }
}

// 取消售后
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认取消此售后申请?', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await afterSalesApi.cancelAfterSales(detailData.value.id)
    ElMessage.success('已取消售后申请')
    getDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消售后失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 触发文件上传点击
const triggerFileUpload = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

// 处理附件上传
const handleUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  
  const formData = new FormData()
  formData.append('file', input.files[0])
  formData.append('afterSalesId', detailData.value.id)
  
  try {
    await afterSalesApi.uploadAttachment(formData)
    ElMessage.success('上传成功')
    getDetail()
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  } finally {
    // 清空文件输入
    input.value = ''
  }
}

// 返回列表
const goBack = () => {
  router.push('/after-sales')
}

// 在组件挂载时获取详情数据
onMounted(() => {
  getDetail()
})
</script>

<template>
  <div class="after-sales-detail-container" v-loading="loading">
    <div class="detail-header">
      <el-button @click="goBack">返回列表</el-button>
      <h2>售后详情</h2>
    </div>
    
    <!-- 用户订单信息卡片 -->
    <div class="user-order-card">
      <div class="user-info">
        <el-avatar :size="60" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${detailData.username}`"></el-avatar>
        <div class="user-details">
          <h3>{{ detailData.username }}</h3>
          <p>手机号: {{ detailData.phone }}</p>
        </div>
      </div>
      
      <div class="order-info">
        <div class="info-item">
          <span class="label">售后编号:</span>
          <span class="value">{{ detailData.afterSaleNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">关联订单:</span>
          <span class="value">{{ detailData.orderNumber }}</span>
        </div>
        <div class="info-item">
          <span class="label">申请时间:</span>
          <span class="value">{{ detailData.applyTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">申请金额:</span>
          <span class="value">¥{{ detailData.refundAmount }}</span>
        </div>
        <div class="info-item">
          <span class="label">售后状态:</span>
          <el-tag :type="formatStatus(detailData.status).type">
            {{ formatStatus(detailData.status).label }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="label">售后类型:</span>
          <span class="value">{{ formatType(detailData.afterSaleType) }}</span>
        </div>
      </div>
      
      <!-- 操作按钮区域 -->
      <div class="action-buttons" v-if="detailData.status === AfterSaleStatus.APPLYING">
        <el-button type="primary" @click="handleApprove">审核通过</el-button>
        <el-button type="danger" @click="openRejectDialog">审核拒绝</el-button>
        <el-button @click="handleCancel">取消售后</el-button>
      </div>
    </div>
    
    <!-- 详情Tab卡片 -->
    <div class="detail-tab-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="售后详情" name="detail">
          <div class="detail-content">
            <div class="detail-section">
              <h3>售后类型</h3>
              <p>{{ formatType(detailData.afterSaleType) }}</p>
            </div>
            
            <div class="detail-section">
              <h3>申请原因</h3>
              <p>{{ detailData.reason }}</p>
            </div>
            
            <div class="detail-section">
              <h3>详细描述</h3>
              <p>{{ detailData.description }}</p>
            </div>
            
            <div class="detail-section">
              <h3>图片凭证</h3>
              <div class="image-list">
                <div v-for="(image, index) in detailData.attachments" :key="index" class="image-item">
                  <el-image 
                    :src="image" 
                    :preview-src-list="detailData.attachments"
                    fit="cover"
                  />
                </div>
                
                <div class="upload-button" v-if="detailData.status === AfterSaleStatus.REFUNDING">
                  <input 
                    ref="fileInputRef"
                    type="file" 
                    accept="image/*" 
                    @change="handleUpload" 
                    style="display: none"
                  />
                  <el-button @click="triggerFileUpload">
                    上传附件
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="商品信息" name="products">
          <div v-if="product">
            <div class="product-info-card">
              <div><b>商品ID：</b>{{ product.productId }}</div>
              <div><b>商品名称：</b>{{ product.productName }}</div>
              <div><b>SKU图片：</b><el-image :src="product.skuImage" style="width:60px;height:60px" /></div>
              <div><b>单价：</b>¥{{ product.unitPrice?.toFixed(2) }}</div>
              <div><b>数量：</b>{{ product.quantity }}</div>
            </div>
          </div>
          <div v-else>无商品信息</div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 拒绝理由弹窗 -->
    <RejectDialog 
      v-model="showRejectDialog" 
      @confirm="handleReject"
    />
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.after-sales-detail-container {
  padding: $spacing-medium;
  
  .detail-header {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-medium;
    
    h2 {
      margin-left: $spacing-medium;
      margin-bottom: 0;
    }
  }
  
  .user-order-card {
    background-color: $color-white;
    border-radius: $border-radius;
    box-shadow: $box-shadow;
    padding: $spacing-medium;
    margin-bottom: $spacing-medium;
    
    .user-info {
      display: flex;
      align-items: center;
      margin-bottom: $spacing-medium;
      padding-bottom: $spacing-medium;
      border-bottom: 1px solid $color-gray-light;
      
      .user-details {
        margin-left: $spacing-medium;
        
        h3 {
          margin: 0 0 $spacing-small 0;
        }
        
        p {
          margin: 0;
          color: $color-gray;
        }
      }
    }
    
    .order-info {
      display: flex;
      flex-wrap: wrap;
      margin-bottom: $spacing-medium;
      
      .info-item {
        width: 33.33%;
        margin-bottom: $spacing-small;
        
        .label {
          color: $color-gray;
          margin-right: $spacing-small;
        }
        
        .value {
          font-weight: $font-weight-medium;
        }
      }
    }
    
    .action-buttons {
      display: flex;
      justify-content: flex-end;
      gap: $spacing-small;
      padding-top: $spacing-medium;
      border-top: 1px solid $color-gray-light;
    }
  }
  
  .detail-tab-card {
    background-color: $color-white;
    border-radius: $border-radius;
    box-shadow: $box-shadow;
    padding: $spacing-medium;
    
    .detail-content {
      padding: $spacing-medium 0;
      
      .detail-section {
        margin-bottom: $spacing-large;
        
        h3 {
          font-size: $font-size-large;
          margin-bottom: $spacing-small;
          color: $color-black-1;
        }
        
        p {
          margin: 0;
          color: $color-gray;
          line-height: 1.6;
        }
      }
      
      .image-list {
        display: flex;
        flex-wrap: wrap;
        gap: $spacing-medium;
        
        .image-item {
          width: 150px;
          height: 150px;
          border-radius: $border-radius;
          overflow: hidden;
          border: 1px solid $color-gray-light;
          
          .el-image {
            width: 100%;
            height: 100%;
          }
        }
        
        .upload-button {
          width: 150px;
          height: 150px;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 1px dashed $color-gray-light;
          border-radius: $border-radius;
        }
      }
    }
  }
}
</style> 
