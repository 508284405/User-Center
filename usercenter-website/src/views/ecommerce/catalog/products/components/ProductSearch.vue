<template>
  <div class="search-bar">
    <el-form :inline="true" :model="searchForm" class="demo-form-inline">
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-tree-select
          v-model="searchForm.categoryId"
          :data="categoryTree"
          node-key="categoryId"
          :props="{
            label: 'name',
            children: 'children'
          }"
          placeholder="全部"
          clearable
          filterable
          :default-expanded-keys="[]"
          check-strictly
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="displayStatus"
          placeholder="请选择状态"
          class="status-select"
          @change="handleStatusChange"
        >
          <el-option label="全部" value="all" />
          <el-option label="草稿" value="draft" />
          <el-option label="上架" value="on" />
          <el-option label="下架" value="off" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" @click="handleAdd">新增商品</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, defineProps, defineEmits, onMounted, watch } from 'vue';
import type { ProductQuery } from '@/api/client-web/product';

interface Props {
  initialQuery?: ProductQuery;
  categoryTree: any[];
}

const props = defineProps<Props>();
const emit = defineEmits(['search', 'reset', 'add']);

const searchForm = reactive<ProductQuery>({
  pageIndex: 1,
  pageSize: 10,
  name: '',
  categoryId: '',
  status: undefined,
  needTotalCount: true,
  ...props.initialQuery
});

// 用于显示的状态值
const displayStatus = ref('all');

// 状态值映射
const statusMap = {
  'all': undefined,
  'draft': 0,
  'on': 1,
  'off': 2
};

const reverseStatusMap: Record<string, string> = {
  'undefined': 'all',
  '0': 'draft',
  '1': 'on',
  '2': 'off'
};

// 同步显示状态和表单状态
function syncDisplayStatusFromForm() {
  const statusKey = String(searchForm.status);
  displayStatus.value = reverseStatusMap[statusKey] ?? 'all';
}

onMounted(syncDisplayStatusFromForm);

// 监听 searchForm.status 变化，保持 UI 和数据同步
watch(() => searchForm.status, (newVal) => {
  displayStatus.value = reverseStatusMap[String(newVal)] ?? 'all';
});

// 监听 props.initialQuery 变化，保持 UI 和数据同步
watch(() => props.initialQuery, (newVal) => {
  if (newVal && typeof newVal.status !== 'undefined') {
    searchForm.status = newVal.status;
    syncDisplayStatusFromForm();
  }
}, { immediate: true, deep: true });

// 处理状态变化
const handleStatusChange = (val: string) => {
  searchForm.status = statusMap[val as keyof typeof statusMap];
};

// 搜索方法
const handleSearch = () => {
  emit('search', { ...searchForm });
};

// 重置方法
const handleReset = () => {
  searchForm.name = '';
  searchForm.categoryId = '';
  searchForm.status = undefined;
  displayStatus.value = 'all';
  emit('reset');
};

// 新增商品
const handleAdd = () => {
  emit('add');
};
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.status-select {
  width: 100%;
}
</style>