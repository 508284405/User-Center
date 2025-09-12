<template>
  <div class="merchant-management">
    <!-- 搜索组件 -->
    <merchant-search 
      @search="handleSearchSubmit" 
      @reset="resetSearch"
    />

    <!-- 表格组件 -->
    <merchant-table 
      :merchants="tableData" 
      :total="total" 
      :current-page="currentPage" 
      :page-size="pageSize"
      @detail="handleDetail" 
      @approve="handleApprove"
      @reject="handleReject"
      @shops="handleViewShops"
      @analytics="handleViewAnalytics"
      @refresh="fetchData" 
      @update:current-page="currentPage = $event" 
      @update:page-size="pageSize = $event"
    />

    <!-- 商家详情对话框 -->
    <merchant-detail 
      v-model="detailDialogVisible" 
      :merchant="detailData"
    />

    <!-- 审批对话框 -->
    <approval-dialog 
      v-model="approvalDialogVisible" 
      :merchant="currentMerchant"
      :is-approve="isApprove"
      @submit-success="fetchData"
    />

    <!-- 店铺列表对话框 -->
    <shop-list-dialog
      v-model="shopListDialogVisible"
      :merchant-no="currentMerchantNo"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import MerchantSearch from './components/MerchantSearch.vue';
import MerchantTable from './components/MerchantTable.vue';
import MerchantDetail from './components/MerchantDetail.vue';
import ApprovalDialog from './components/ApprovalDialog.vue';
import ShopListDialog from './components/ShopListDialog.vue';
import {
  pageMerchants,
  getMerchantByNo,
  type Merchant,
  type MerchantPageQuery
} from '@/api/client-web/merchant';

// Router
const router = useRouter();

// 搜索表单
const searchForm = reactive<MerchantPageQuery>({
  pageNo: 1,
  pageSize: 10,
  status: '',
  merchantName: ''
});

// 表格数据
const tableData = ref<Merchant[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 对话框相关
const detailDialogVisible = ref(false);
const detailData = ref<Merchant | null>(null);

// 审批对话框相关
const approvalDialogVisible = ref(false);
const currentMerchant = ref<Merchant | null>(null);
const isApprove = ref(true);

// 店铺列表对话框
const shopListDialogVisible = ref(false);
const currentMerchantNo = ref('');

// 获取数据
const fetchData = async () => {
  try {
    const query = {
      ...searchForm,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    };
    
    const response = await pageMerchants(query);
    if (response.success) {
      tableData.value = response.data.data;
      total.value = response.data.total;
    } else {
      ElMessage.error(response.errMessage || '获取商家列表失败');
    }
  } catch (error) {
    console.error('获取商家列表出错:', error);
    ElMessage.error('获取商家列表失败');
  }
};

// 搜索提交
const handleSearchSubmit = (searchParams: any) => {
  Object.assign(searchForm, searchParams);
  currentPage.value = 1;
  fetchData();
};

// 重置搜索
const resetSearch = () => {
  searchForm.status = '';
  searchForm.merchantName = '';
  currentPage.value = 1;
  fetchData();
};

// 查看详情
const handleDetail = async (merchant: Merchant) => {
  try {
    if (merchant.merchantNo) {
      const response = await getMerchantByNo(merchant.merchantNo);
      if (response.success) {
        detailData.value = response.data;
        detailDialogVisible.value = true;
      } else {
        ElMessage.error(response.errMessage || '获取商家详情失败');
      }
    }
  } catch (error) {
    console.error('获取商家详情出错:', error);
    ElMessage.error('获取商家详情失败');
  }
};

// 审批通过
const handleApprove = (merchant: Merchant) => {
  currentMerchant.value = merchant;
  isApprove.value = true;
  approvalDialogVisible.value = true;
};

// 拒绝申请
const handleReject = (merchant: Merchant) => {
  currentMerchant.value = merchant;
  isApprove.value = false;
  approvalDialogVisible.value = true;
};

// 查看店铺
const handleViewShops = (merchant: Merchant) => {
  if (merchant.merchantNo) {
    currentMerchantNo.value = merchant.merchantNo;
    shopListDialogVisible.value = true;
  }
};

// 查看分析
const handleViewAnalytics = (merchant: Merchant) => {
  if (merchant.merchantNo) {
    router.push({
      name: 'MerchantAnalytics',
      query: {
        merchantNo: merchant.merchantNo,
        merchantName: merchant.merchantName
      }
    });
  }
};

// 初始化
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.merchant-management {
  padding: 20px;
}
</style>