<template>
  <div class="merchant-table">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商家列表</span>
          <el-button type="primary" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="merchants"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="merchantNo" label="商家编号" width="160" />
        <el-table-column prop="merchantName" label="商家名称" width="200" />
        <el-table-column prop="legalName" label="法人姓名" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="contactEmail" label="联系邮箱" width="180" />
        <el-table-column prop="businessLicense" label="营业执照号" width="180" />
        <el-table-column prop="commissionRate" label="佣金率" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ (row.commissionRate * 100).toFixed(2) }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            
            <el-button 
              v-if="row.status === 'PENDING'"
              size="small" 
              type="success" 
              @click="handleApprove(row)"
            >
              <el-icon><Check /></el-icon>
              通过
            </el-button>
            
            <el-button 
              v-if="row.status === 'PENDING'"
              size="small" 
              type="danger" 
              @click="handleReject(row)"
            >
              <el-icon><Close /></el-icon>
              拒绝
            </el-button>
            
            <el-button 
              v-if="row.status === 'APPROVED'"
              size="small" 
              type="warning" 
              @click="handleViewShops(row)"
            >
              <el-icon><Shop /></el-icon>
              店铺
            </el-button>
            
            <el-button 
              v-if="row.status === 'APPROVED'"
              size="small" 
              type="info" 
              @click="handleViewAnalytics(row)"
            >
              <el-icon><DataAnalysis /></el-icon>
              分析
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Refresh, View, Check, Close, Shop, DataAnalysis } from '@element-plus/icons-vue';
import { MerchantStatus, type Merchant } from '@/api/client-web/merchant';

// Props
interface Props {
  merchants: Merchant[];
  total: number;
  currentPage: number;
  pageSize: number;
}

const props = withDefaults(defineProps<Props>(), {
  merchants: () => [],
  total: 0,
  currentPage: 1,
  pageSize: 10
});

// Events
const emit = defineEmits<{
  detail: [merchant: Merchant];
  approve: [merchant: Merchant];
  reject: [merchant: Merchant];
  shops: [merchant: Merchant];
  analytics: [merchant: Merchant];
  refresh: [];
  'update:current-page': [page: number];
  'update:page-size': [size: number];
}>();

// 计算属性
const currentPage = computed({
  get: () => props.currentPage,
  set: (value) => emit('update:current-page', value)
});

const pageSize = computed({
  get: () => props.pageSize,
  set: (value) => emit('update:page-size', value)
});

// 获取状态类型
const getStatusType = (status: string) => {
  return MerchantStatus[status as keyof typeof MerchantStatus]?.color || 'info';
};

// 获取状态标签
const getStatusLabel = (status: string) => {
  return MerchantStatus[status as keyof typeof MerchantStatus]?.label || status;
};

// 格式化日期
const formatDate = (timestamp: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

// 事件处理
const handleDetail = (merchant: Merchant) => {
  emit('detail', merchant);
};

const handleApprove = (merchant: Merchant) => {
  emit('approve', merchant);
};

const handleReject = (merchant: Merchant) => {
  emit('reject', merchant);
};

const handleViewShops = (merchant: Merchant) => {
  emit('shops', merchant);
};

const handleViewAnalytics = (merchant: Merchant) => {
  emit('analytics', merchant);
};

const handleRefresh = () => {
  emit('refresh');
};

const handleSizeChange = (size: number) => {
  emit('update:page-size', size);
};

const handleCurrentChange = (page: number) => {
  emit('update:current-page', page);
};
</script>

<style scoped>
.merchant-table {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: right;
}
</style>