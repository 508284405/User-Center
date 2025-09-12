<template>
  <div class="merchant-search">
    <el-card>
      <el-form 
        :model="searchForm" 
        label-width="100px" 
        inline
      >
        <el-form-item label="商家名称">
          <el-input
            v-model="searchForm.merchantName"
            placeholder="请输入商家名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { Search, Refresh } from '@element-plus/icons-vue';
import { MerchantStatus } from '@/api/client-web/merchant';

// 定义事件
const emit = defineEmits<{
  search: [params: any];
  reset: [];
}>();

// 搜索表单
const searchForm = reactive({
  merchantName: '',
  status: ''
});

// 状态选项
const statusOptions = Object.values(MerchantStatus).map(status => ({
  value: status.value,
  label: status.label
}));

// 搜索处理
const handleSearch = () => {
  emit('search', { ...searchForm });
};

// 重置处理
const handleReset = () => {
  searchForm.merchantName = '';
  searchForm.status = '';
  emit('reset');
};
</script>

<style scoped>
.merchant-search {
  margin-bottom: 20px;
}
</style>