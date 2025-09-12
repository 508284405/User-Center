<template>
  <el-dialog
    v-model="dialogVisible"
    title="店铺列表"
    width="1000px"
    :before-close="handleClose"
  >
    <div class="shop-list">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="fetchShops">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <!-- 店铺表格 -->
      <el-table
        :data="shops"
        stripe
        border
        v-loading="loading"
      >
        <el-table-column prop="shopNo" label="店铺编号" width="160" />
        <el-table-column prop="shopName" label="店铺名称" width="200" />
        <el-table-column prop="description" label="店铺描述" min-width="200">
          <template #default="{ row }">
            <el-text class="description" truncated>
              {{ row.description || '暂无描述' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="店铺地址" width="200">
          <template #default="{ row }">
            {{ row.address || '暂无地址' }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="140">
          <template #default="{ row }">
            {{ row.contactPhone || '暂无电话' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getShopStatusType(row.status)">
              {{ getShopStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleShopDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <el-empty v-if="!loading && shops.length === 0" description="暂无店铺数据" />
    </div>
    
    <!-- 店铺详情对话框 -->
    <shop-detail-dialog
      v-model="shopDetailVisible"
      :shop="currentShop"
    />
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh, View } from '@element-plus/icons-vue';
import ShopDetailDialog from './ShopDetailDialog.vue';
import { getShopsByMerchant, ShopStatus, type Shop } from '@/api/client-web/merchant';

// Props
interface Props {
  modelValue: boolean;
  merchantNo: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  merchantNo: ''
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

// 数据状态
const shops = ref<Shop[]>([]);
const loading = ref(false);

// 店铺详情对话框
const shopDetailVisible = ref(false);
const currentShop = ref<Shop | null>(null);

// 获取店铺状态类型
const getShopStatusType = (status: string) => {
  return ShopStatus[status as keyof typeof ShopStatus]?.color || 'info';
};

// 获取店铺状态标签
const getShopStatusLabel = (status: string) => {
  return ShopStatus[status as keyof typeof ShopStatus]?.label || status;
};

// 格式化日期
const formatDate = (timestamp: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

// 获取店铺列表
const fetchShops = async () => {
  if (!props.merchantNo) return;
  
  try {
    loading.value = true;
    const response = await getShopsByMerchant(props.merchantNo);
    
    if (response.success) {
      shops.value = response.data;
    } else {
      ElMessage.error(response.errMessage || '获取店铺列表失败');
    }
  } catch (error) {
    console.error('获取店铺列表出错:', error);
    ElMessage.error('获取店铺列表失败');
  } finally {
    loading.value = false;
  }
};

// 查看店铺详情
const handleShopDetail = (shop: Shop) => {
  currentShop.value = shop;
  shopDetailVisible.value = true;
};

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false);
};

// 监听商家编号变化，自动获取店铺列表
watch([() => props.modelValue, () => props.merchantNo], ([visible, merchantNo]) => {
  if (visible && merchantNo) {
    fetchShops();
  }
});
</script>

<style scoped>
.shop-list {
  padding: 10px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.description {
  max-width: 180px;
}
</style>