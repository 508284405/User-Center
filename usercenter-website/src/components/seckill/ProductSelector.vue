<template>
  <div class="product-selector">
    <el-select
      v-model="selectedProduct"
      filterable
      remote
      :remote-method="fetchProducts"
      :loading="loading"
      placeholder="请输入商品名称搜索"
      style="width: 100%"
      @change="handleProductChange"
    >
      <el-option
        v-for="item in productList"
        :key="item.id"
        :label="item.name"
        :value="item.id"
      >
        <div class="product-option">
          <span>{{ item.name }}</span>
          <span class="product-price">¥{{ item.price }}</span>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getProductList, type Product } from '@/api/client-web/product';

const emit = defineEmits<{
  (e: 'product-selected', product: Product): void;
}>();

const selectedProduct = defineModel<number>();

const productList = ref<Product[]>([]);
const loading = ref(false);

// 获取商品列表
const fetchProducts = async (query: string = '') => {
  loading.value = true;
  try {
    const res = await getProductList({
      pageIndex: 1,
      pageSize: 20,
      name: query,
      needTotalCount: false
    });
    
    if (res.success) {
      productList.value = res.data;
    } else {
      ElMessage.error(res.errMessage || '获取商品列表失败');
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 处理商品选择变化
const handleProductChange = (productId: number) => {
  const product = productList.value.find(p => p.id === productId);
  if (product) {
    emit('product-selected', product);
  }
};

// 初始化时获取商品列表
onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.product-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #f56c6c;
  font-weight: bold;
}
</style>