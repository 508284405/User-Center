<template>
  <el-dialog
    v-model="dialogVisible"
    title="商家详情"
    width="800px"
    :before-close="handleClose"
  >
    <div v-if="merchant" class="merchant-detail">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家编号">
            <el-tag type="info">{{ merchant.merchantNo }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="商家名称">
            {{ merchant.merchantName }}
          </el-descriptions-item>
          <el-descriptions-item label="法人姓名">
            {{ merchant.legalName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            <el-link type="primary">{{ merchant.contactPhone }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱">
            <el-link type="primary">{{ merchant.contactEmail }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="营业执照号">
            {{ merchant.businessLicense }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 状态信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><Flag /></el-icon>
            <span>状态信息</span>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getStatusType(merchant.status)">
              {{ getStatusLabel(merchant.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="佣金率">
            <el-tag type="success">{{ (merchant.commissionRate * 100).toFixed(2) }}%</el-tag>
          </el-descriptions-item>
          <el-descriptions-item 
            v-if="merchant.rejectReason" 
            label="拒绝原因"
            :span="2"
          >
            <el-alert
              :title="merchant.rejectReason"
              type="error"
              :closable="false"
            />
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 时间信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>时间信息</span>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="创建时间">
            {{ formatDate(merchant.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(merchant.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { User, Flag, Clock } from '@element-plus/icons-vue';
import { MerchantStatus, type Merchant } from '@/api/client-web/merchant';

// Props
interface Props {
  modelValue: boolean;
  merchant: Merchant | null;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  merchant: null
});

// Events
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 获取状态类型
const getStatusType = (status?: string) => {
  if (!status) return 'info';
  return MerchantStatus[status as keyof typeof MerchantStatus]?.color || 'info';
};

// 获取状态标签
const getStatusLabel = (status?: string) => {
  if (!status) return '';
  return MerchantStatus[status as keyof typeof MerchantStatus]?.label || status;
};

// 格式化日期
const formatDate = (timestamp?: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false);
};
</script>

<style scoped>
.merchant-detail {
  padding: 10px;
}

.info-card {
  margin-bottom: 20px;
}

.info-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}
</style>