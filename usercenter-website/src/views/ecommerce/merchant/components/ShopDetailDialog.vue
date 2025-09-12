<template>
  <el-dialog
    v-model="dialogVisible"
    title="店铺详情"
    width="700px"
    :before-close="handleClose"
  >
    <div v-if="shop" class="shop-detail">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><Shop /></el-icon>
            <span>基本信息</span>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="店铺编号">
            <el-tag type="info">{{ shop.shopNo }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="店铺名称">
            {{ shop.shopName }}
          </el-descriptions-item>
          <el-descriptions-item label="商家编号">
            <el-tag>{{ shop.merchantNo }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getShopStatusType(shop.status)">
              {{ getShopStatusLabel(shop.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item 
            v-if="shop.description" 
            label="店铺描述"
            :span="2"
          >
            {{ shop.description }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 联系信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><Phone /></el-icon>
            <span>联系信息</span>
          </div>
        </template>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="店铺地址">
            {{ shop.address || '暂无地址' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            <el-link v-if="shop.contactPhone" type="primary">
              {{ shop.contactPhone }}
            </el-link>
            <span v-else class="no-data">暂无电话</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- LOGO展示 -->
      <el-card v-if="shop.logoUrl" class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><Picture /></el-icon>
            <span>店铺LOGO</span>
          </div>
        </template>
        
        <div class="logo-container">
          <el-image
            :src="shop.logoUrl"
            :preview-src-list="[shop.logoUrl]"
            fit="contain"
            style="width: 200px; height: 200px"
            preview-teleported
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
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
            {{ formatDate(shop.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(shop.updatedAt) }}
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
import { Shop as ShopIcon, Phone, Picture, Clock } from '@element-plus/icons-vue';
import { ShopStatus, type Shop } from '@/api/client-web/merchant';

// Props
interface Props {
  modelValue: boolean;
  shop: Shop | null;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  shop: null
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

// 获取店铺状态类型
const getShopStatusType = (status?: string) => {
  if (!status) return 'info';
  return ShopStatus[status as keyof typeof ShopStatus]?.color || 'info';
};

// 获取店铺状态标签
const getShopStatusLabel = (status?: string) => {
  if (!status) return '';
  return ShopStatus[status as keyof typeof ShopStatus]?.label || status;
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
.shop-detail {
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

.logo-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
  background-color: #f5f7fa;
}

.no-data {
  color: #909399;
  font-style: italic;
}
</style>