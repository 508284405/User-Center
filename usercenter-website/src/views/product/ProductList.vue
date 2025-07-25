<template>
  <div class="product-list">
    <!-- 搜索组件 -->
    <product-search 
      :category-tree="categoryTree" 
      @search="handleSearchSubmit" 
      @reset="resetSearch" 
      @add="handleAdd"
    />

    <!-- 表格组件 -->
    <product-table 
      :products="tableData" 
      :total="total" 
      :current-page="currentPage" 
      :page-size="pageSize"
      @detail="handleDetail" 
      @edit="handleEdit" 
      @refresh="fetchData" 
      @update:current-page="currentPage = $event" 
      @update:page-size="pageSize = $event"
    />

    <!-- 商品表单组件 -->
    <product-form 
      v-model="dialogVisible" 
      :product="currentProduct" 
      :category-tree="categoryTree" 
      :title="dialogTitle"
      @submit-success="fetchData"
    />

    <!-- 商品详情组件 -->
    <product-detail 
      v-model="detailDialogVisible" 
      :product="detailData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import ProductSearch from './components/ProductSearch.vue';
import ProductTable from './components/ProductTable.vue';
import ProductForm from './components/ProductForm.vue';
import ProductDetail from './components/ProductDetail.vue';
import { getCategoryTree } from '@/api/client-web/category';
import {
  getProductList,
  getProduct,
  type Product,
  type ProductQuery
} from '@/api/client-web/product';

// 搜索表单
const searchForm = reactive<ProductQuery>({
  pageIndex: 1,
  pageSize: 10,
  name: '',
  categoryId: '',
  status: undefined,
  needTotalCount: true
});

// 表格数据
const tableData = ref<Product[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 对话框相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const currentProduct = ref<Product | null>(null);

// 详情对话框相关
const detailDialogVisible = ref(false);
const detailData = ref<Product | null>(null);

// 分类数据
const categoryTree = ref([]);

// 获取分类树数据
const fetchCategoryTree = async () => {
  try {
    const res = await getCategoryTree();
    if (res.success) {
      categoryTree.value = res.data;
    } else {
      ElMessage.error(res.errMessage || '获取分类数据失败');
    }
  } catch (error) {
    ElMessage.error('获取分类数据失败');
  }
};

// 根据分类ID获取分类名称
const getCategoryNameById = (categoryId: string | number) => {
  const findCategory = (categories: any[], targetId: string | number): string => {
    for (const category of categories) {
      if (category.categoryId === targetId) {
        return category.name;
      }
      if (category.children && category.children.length > 0) {
        const name = findCategory(category.children, targetId);
        if (name) return name;
      }
    }
    return '';
  };
  return findCategory(categoryTree.value, categoryId) || '未知分类';
};

// 在组件挂载时获取分类数据和商品列表
onMounted(() => {
  fetchCategoryTree();
  fetchData();
});

// 获取商品列表
const fetchData = async () => {
  try {
    // 处理请求参数，移除undefined值
    const params = {
      ...searchForm,
      pageIndex: currentPage.value,
      pageSize: pageSize.value
    };
    
    // 如果categoryId或status是undefined，则不传递这些参数
    if (params.categoryId === '' || params.categoryId === undefined) {
      delete params.categoryId;
    }
    
    if (params.status === undefined) {
      delete params.status;
    }
    
    const res = await getProductList(params);
    if (res.success) {
      // 处理商品数据，映射分类名称
      tableData.value = res.data.map(item => ({
        ...item,
        category: getCategoryNameById(item.categoryId)
      }));
      total.value = res.totalCount;
    } else {
      ElMessage.error(res.errMessage || '获取商品列表失败');
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败');
  }
};

// 搜索相关方法
const handleSearchSubmit = (formData: ProductQuery) => {
  Object.assign(searchForm, formData);
  currentPage.value = 1;
  fetchData();
};

const resetSearch = () => {
  searchForm.name = '';
  searchForm.categoryId = '';
  searchForm.status = undefined;
  currentPage.value = 1;
  fetchData();
};

// 新增商品
const handleAdd = () => {
  dialogTitle.value = '新增商品';
  currentProduct.value = null;
  // 确保表单重置
  nextTick(() => {
    dialogVisible.value = true;
  });
};

// 查看商品详情
const handleDetail = async (row: Product) => {
  try {
    // 获取商品详情
    const res = await getProduct(row.id!);
    if (res.success) {
      detailData.value = {
        ...res.data,
        category: getCategoryNameById(res.data.categoryId)
      };
      detailDialogVisible.value = true;
    } else {
      ElMessage.error(res.errMessage || '获取商品详情失败');
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败');
  }
};

// 编辑商品
const handleEdit = async (row: Product) => {
  dialogTitle.value = '编辑商品';
  try {
    // 获取商品详情
    const res = await getProduct(row.id!);
    if (res.success) {
      currentProduct.value = res.data;
      dialogVisible.value = true;
    } else {
      ElMessage.error(res.errMessage || '获取商品详情失败');
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败');
  }
};
</script>

<style scoped>
:root {
  --product-padding: 20px;
  --spacing-sm: 10px;
  --spacing-md: 20px;
  --spacing-lg: 30px;
}

.product-list {
  padding: var(--product-padding);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

/* 响应式布局优化 */
@media screen and (max-width: 768px) {
  .product-list {
    padding: calc(var(--product-padding) / 2);
  }
}
</style>