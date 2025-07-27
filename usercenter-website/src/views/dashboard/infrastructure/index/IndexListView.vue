<template>
  <div class="index-list-view">
    <el-row :gutter="20" class="header-row">
      <el-col :span="24">
        <el-button type="primary" @click="openCreateIndexModal">创建索引</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col
        v-for="indexName in indexList"
        :key="indexName"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <el-card class="index-card">
          <template #header>
            <div class="card-header">
              <span>{{ indexName }}</span>
              <el-button type="text" @click="viewIndexDetail(indexName)">详情</el-button>
            </div>
          </template>
          <div class="card-content">
            <!-- 简略信息或统计数据，目前只有名称 -->
            <p>索引名称: {{ indexName }}</p>
          </div>
          <div class="card-actions">
            <el-button type="info" size="small" @click="manageCache(indexName)">管理缓存</el-button>
            <el-button type="danger" size="small" @click="deleteIndexConfirm(indexName)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建索引模态框/抽屉 -->
    <index-create-modal v-model:visible="createModalVisible" @created="fetchIndexList"></index-create-modal>

    <!-- 查看详情模态框/抽屉 -->
    <index-detail-modal v-model:visible="detailModalVisible" :index-name="selectedIndexName"></index-detail-modal>

    <!-- 缓存管理模态框 -->
    <index-cache-modal v-model:visible="cacheModalVisible" :index-name="selectedIndexName"></index-cache-modal>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listIndexes, deleteIndex } from '@/api/smartcs/knowledgeIndex';
import IndexCreateModal from '@/components/knowledge/index/IndexCreateModal.vue';
import IndexDetailModal from '@/components/knowledge/index/IndexDetailModal.vue';
import IndexCacheModal from '@/components/knowledge/cache/IndexCacheModal.vue';

const indexList = ref<string[]>([]);
const createModalVisible = ref(false);
const detailModalVisible = ref(false);
const cacheModalVisible = ref(false);
const selectedIndexName = ref('');

// 获取索引列表
const fetchIndexList = async () => {
  try {
    const res = await listIndexes();
    if (res.success && res.data) {
      indexList.value = res.data;
    } else {
      ElMessage.error('获取索引列表失败: ' + res.errMessage);
    }
  } catch (error: any) {
    ElMessage.error('获取索引列表异常: ' + (error.message || '未知错误'));
  }
};

// 打开创建索引模态框
const openCreateIndexModal = () => {
  createModalVisible.value = true;
};

// 查看索引详情
const viewIndexDetail = (indexName: string) => {
  selectedIndexName.value = indexName;
  detailModalVisible.value = true;
};

// 管理缓存
const manageCache = (indexName: string) => {
  selectedIndexName.value = indexName;
  cacheModalVisible.value = true;
};

// 删除索引确认
const deleteIndexConfirm = (indexName: string) => {
  ElMessageBox.confirm(
    `确定要删除索引 '${indexName}' 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    handleDeleteIndex(indexName);
  }).catch(() => {
    // 用户取消删除
  });
};

// 执行删除索引
const handleDeleteIndex = async (indexName: string) => {
  try {
    const res = await deleteIndex({ indexName: indexName });
    if (res.success) {
      ElMessage.success('删除成功');
      fetchIndexList(); // 删除成功后刷新列表
    } else {
      ElMessage.error('删除失败: ' + res.errMessage);
    }
  } catch (error: any) {
    ElMessage.error('删除异常: ' + (error.message || '未知错误'));
  }
};

// 页面加载时获取索引列表
onMounted(() => {
  fetchIndexList();
});
</script>

<style scoped lang="scss">
@import './index-list.scss';
</style> 