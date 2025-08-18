<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { afterSalesApi } from '@/api/client-web/afterSales'
import { AfterSaleRefundItem } from '@/api/client-web/afterSaleRefund'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const refundNo = ref(route.params.id as string)
const refundDetail = ref<AfterSaleRefundItem | null>(null)

// 获取退款详情
const getRefundDetail = async () => {
  loading.value = true
  try {
    const res = await afterSalesApi.getRefundDetailById(refundNo.value)
    // 根据API响应结构进行处理
    if (res && res.data) {
      refundDetail.value = res.data
    } else {
      ElMessage.error('获取退款详情失败')
    }
  } catch (error) {
    console.error('获取退款详情失败:', error)
    ElMessage.error('获取退款详情失败')
  } finally {
    loading.value = false
  }
}

// 处理退款
const handleProcessRefund = async () => {
  try {
    ElMessageBox.confirm('确认处理此退款申请吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const res: any = await afterSalesApi.processRefund(refundNo.value)
      if (res && res.success) {
        ElMessage.success('退款处理成功')
        getRefundDetail() // 重新加载详情
      } else {
        ElMessage.error(res.errMessage || '退款处理失败')
      }
    }).catch(() => {
      // 用户取消操作
      console.log('用户取消操作')
    })
  } catch (error) {
    console.error('处理退款失败:', error)
    ElMessage.error('处理退款失败')
  }
}

// 拒绝退款
const handleRejectRefund = async () => {
  try {
    ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝原因不能为空'
    }).then(async ({ value }) => {
      const res: any = await afterSalesApi.rejectRefund(refundNo.value, value)
      if (res && res.success) {
        ElMessage.success('已拒绝退款申请')
        getRefundDetail() // 重新加载详情
      } else {
        ElMessage.error(res.errMessage || '拒绝退款失败')
      }
    }).catch(() => {
      // 用户取消操作
    })
  } catch (error) {
    console.error('拒绝退款失败:', error)
    ElMessage.error('拒绝退款失败')
  }
}

// 返回列表页
const goBack = () => {
  router.push('/after-sales/refund')
}

// 格式化日期
const formatDate = (ts: string | number) => {
  if (!ts) return '';
  const date = new Date(Number(ts));
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  const ss = String(date.getSeconds()).padStart(2, '0');
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
}

onMounted(() => {
  getRefundDetail()
})
</script>

<template>
  <div class="refund-detail-container" v-loading="loading">
    <div class="page-header">
      <div class="left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2>退款详情</h2>
      </div>
      <div class="right" v-if="refundDetail && (refundDetail.refundStatus === 'PENDING' || refundDetail.refundStatus === 'FAILED')">
        <el-button type="primary" @click="handleProcessRefund">
          {{ refundDetail.refundStatus === 'FAILED' ? '重新发起退款' : '同意退款' }}
        </el-button>
        <el-button
          v-if="refundDetail.refundStatus === 'PENDING'"
          type="danger"
          @click="handleRejectRefund"
        >
          拒绝退款
        </el-button>
      </div>
    </div>
    
    <el-card class="detail-card" v-if="refundDetail">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-tag
            :type="refundDetail.refundStatus === 'COMPLETED' ? 'success' : 
                 refundDetail.refundStatus === 'REJECTED' ? 'danger' : 
                 refundDetail.refundStatus === 'PENDING' ? 'info' : 'warning'"
          >
            {{ refundDetail.refundStatus === 'COMPLETED' ? '已完成' : 
               refundDetail.refundStatus === 'REJECTED' ? '已拒绝' : 
               refundDetail.refundStatus === 'PENDING' ? '待处理' : 
               refundDetail.refundStatus === 'PROCESSING' ? '处理中' : 
               refundDetail.refundStatus === 'FAILED' ? '退款失败' : refundDetail.refundStatus }}
          </el-tag>
        </div>
      </template>
      
      <div class="info-section">
        <div class="info-row">
          <span class="label">退款ID：</span>
          <span class="value">{{ refundDetail.id }}</span>
        </div>
        <div class="info-row">
          <span class="label">退款编号：</span>
          <span class="value">{{ refundDetail.refundNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">售后单号：</span>
          <span class="value">{{ refundDetail.afterSaleNo }}</span>
        </div>
        <div class="info-row" v-if="refundDetail.orderNumber">
          <span class="label">订单编号：</span>
          <span class="value">{{ refundDetail.orderNumber }}</span>
        </div>
        <div class="info-row">
          <span class="label">支付渠道：</span>
          <span class="value">{{ refundDetail.paymentChannel }}</span>
        </div>
        <div class="info-row">
          <span class="label">退款金额：</span>
          <span class="value highlight">¥{{ refundDetail.refundAmount?.toFixed(2) }}</span>
        </div>
        <div class="info-row">
          <span class="label">退款状态：</span>
          <span class="value">
            <el-tag
              :type="refundDetail.refundStatus === 'COMPLETED' ? 'success' : 
                   refundDetail.refundStatus === 'REJECTED' ? 'danger' : 
                   refundDetail.refundStatus === 'PENDING' ? 'info' : 'warning'"
            >
              {{ refundDetail.refundStatus === 'COMPLETED' ? '已完成' : 
                 refundDetail.refundStatus === 'REJECTED' ? '已拒绝' : 
                 refundDetail.refundStatus === 'PENDING' ? '待处理' : 
                 refundDetail.refundStatus === 'PROCESSING' ? '处理中' : 
                 refundDetail.refundStatus === 'FAILED' ? '退款失败' : refundDetail.refundStatus }}
            </el-tag>
          </span>
        </div>
        <div class="info-row">
          <span class="label">退款时间：</span>
          <span class="value">{{ formatDate(refundDetail.refundTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">申请原因：</span>
          <span class="value">{{ refundDetail.reason }}</span>
        </div>
        <div class="info-row" v-if="refundDetail.operatorId">
          <span class="label">操作人ID：</span>
          <span class="value">{{ refundDetail.operatorId }}</span>
        </div>
        <div class="info-row" v-if="refundDetail.createdAt">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDate(refundDetail.createdAt) }}</span>
        </div>
        <div class="info-row" v-if="refundDetail.updatedAt">
          <span class="label">更新时间：</span>
          <span class="value">{{ formatDate(refundDetail.updatedAt) }}</span>
        </div>
      </div>
    </el-card>
    
    <el-empty v-else description="未找到退款记录"></el-empty>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.refund-detail-container {
  padding: $spacing-medium;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-medium;
    
    .left {
      display: flex;
      align-items: center;
      gap: $spacing-small;
      
      h2 {
        margin: 0;
      }
    }
    
    .right {
      display: flex;
      gap: $spacing-small;
    }
  }
  
  .detail-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }
    
    .info-section {
      padding: $spacing-small 0;
      
      .info-row {
        display: flex;
        margin-bottom: $spacing-small;
        line-height: 1.8;
        
        .label {
          width: 120px;
          color: $color-gray;
          text-align: right;
          padding-right: $spacing-small;
        }
        
        .value {
          flex: 1;
          
          &.highlight {
            color: $color-primary;
            font-weight: bold;
          }
        }
      }
    }
  }
}
</style> 