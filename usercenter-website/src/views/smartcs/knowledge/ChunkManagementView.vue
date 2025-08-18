<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Search, Plus, Edit, Delete, ArrowLeft, Cpu } from '@element-plus/icons-vue';
import { chunkApi } from '@/api/smartcs/chunk';
import { contentApi } from '@/api/smartcs/content';

// 定义切片数据类型
interface Chunk {
  id?: number;
  contentId?: number;
  chunkIndex?: number;
  content?: string;
  tokenSize?: number;
  metadata?: string;
  createTime?: number;
  updateTime?: number;
}

// 定义内容数据类型
interface Content {
  id?: number;
  title?: string;
}

// 路由
const route = useRoute();
const router = useRouter();

// 获取contentId
const contentId = computed(() => {
  return parseInt(route.params.contentId as string);
});

// 查询参数
const searchParams = reactive({
  contentId: contentId.value,
  keyword: '',
  chunkIndex: undefined as number | undefined,
  pageIndex: 1,
  pageSize: 10,
  needTotalCount: true,
});

// 表格数据和加载状态
const tableData = ref<Chunk[]>([]);
const totalCount = ref(0);
const loading = ref(false);

// 内容信息
const contentInfo = ref<Content>({});

// 新增/编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);
const chunkForm = reactive({
  id: undefined as number | undefined,
  contentId: contentId.value,
  chunkIndex: 1,
  content: '',
  tokenSize: undefined as number | undefined,
  metadata: '',
});

// 获取内容信息
const fetchContentInfo = async () => {
  try {
    const response = await contentApi.getById(contentId.value);
    if (response && response.success) {
      contentInfo.value = response.data || {};
    }
  } catch (error) {
    console.error('获取内容信息出错:', error);
  }
};

// 查询切片列表
const fetchChunks = async () => {
  loading.value = true;
  try {
    const validSearchParams: Record<string, any> = {};
    for (const key in searchParams) {
      const value = (searchParams as any)[key];
      if (value !== undefined && value !== null && value !== '') {
        validSearchParams[key] = value;
      }
    }

    const response = await chunkApi.list(validSearchParams);
    
    if (response && response.success) {
      tableData.value = response.data || [];
      totalCount.value = response.totalCount || 0;
    } else {
      ElMessage.error('获取切片列表失败');
    }
  } catch (error) {
    console.error('获取切片列表出错:', error);
    ElMessage.error('获取切片列表出错');
  } finally {
    loading.value = false;
  }
};

// 搜索切片
const handleSearch = () => {
  searchParams.pageIndex = 1;
  fetchChunks();
};

// 重置搜索
const resetSearch = () => {
  searchParams.keyword = '';
  searchParams.chunkIndex = undefined;
  searchParams.pageIndex = 1;
  fetchChunks();
};

// 分页变化
const handleCurrentChange = (current: number) => {
  searchParams.pageIndex = current;
  fetchChunks();
};

const handleSizeChange = (size: number) => {
  searchParams.pageSize = size;
  searchParams.pageIndex = 1;
  fetchChunks();
};

// 打开新增对话框
const openCreateDialog = () => {
  dialogTitle.value = '新增切片';
  isEdit.value = false;
  chunkForm.id = undefined;
  chunkForm.contentId = contentId.value;
  chunkForm.chunkIndex = 1;
  chunkForm.content = '';
  chunkForm.tokenSize = undefined;
  chunkForm.metadata = '';
  dialogVisible.value = true;
};

// 打开编辑对话框
const openEditDialog = (row: Chunk) => {
  dialogTitle.value = '编辑切片';
  isEdit.value = true;
  chunkForm.id = row.id;
  chunkForm.contentId = row.contentId || contentId.value;
  chunkForm.chunkIndex = row.chunkIndex || 1;
  chunkForm.content = row.content || '';
  chunkForm.tokenSize = row.tokenSize;
  chunkForm.metadata = row.metadata || '';
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!chunkForm.content || !chunkForm.chunkIndex) {
    ElMessage.warning('请填写切片内容和段落序号');
    return;
  }

  const loadingInstance = ElLoading.service({
    lock: true,
    text: isEdit.value ? '更新中...' : '创建中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    let response;
    if (isEdit.value && chunkForm.id) {
      response = await chunkApi.update({
        id: chunkForm.id,
        chunkIndex: chunkForm.chunkIndex,
        content: chunkForm.content,
        tokenSize: chunkForm.tokenSize,
        metadata: chunkForm.metadata,
      });
    } else {
      response = await chunkApi.create({
        contentId: chunkForm.contentId,
        chunkIndex: chunkForm.chunkIndex,
        content: chunkForm.content,
        tokenSize: chunkForm.tokenSize,
        metadata: chunkForm.metadata,
      });
    }

    if (response && response.success) {
      ElMessage.success(isEdit.value ? '切片更新成功' : '切片创建成功');
      dialogVisible.value = false;
      fetchChunks();
    } else {
      ElMessage.error(response?.errMessage || '操作失败');
    }
  } catch (error: any) {
    console.error('操作切片出错:', error);
    ElMessage.error('操作切片出错:' + error.message);
  } finally {
    loadingInstance.close();
  }
};

// 删除切片
const handleDelete = (row: Chunk) => {
  if (!row.id) return;
  
  ElMessageBox.confirm('确定要删除这个切片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await chunkApi.delete(row.id!);
      
      if (response && response.success) {
        ElMessage.success('切片删除成功');
        fetchChunks();
      } else {
        ElMessage.error('切片删除失败');
      }
    } catch (error) {
      console.error('删除切片出错:', error);
      ElMessage.error('删除切片出错');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 向量化切片
const handleVectorize = async (row: Chunk) => {
  if (!row.id) return;
  
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '向量化中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    const response = await chunkApi.vectorize(row.id);
    
    if (response && response.success) {
      ElMessage.success('切片向量化已触发，请稍后刷新查看状态');
      setTimeout(() => {
        fetchChunks();
      }, 2000);
    } else {
      ElMessage.error('触发切片向量化失败');
    }
  } catch (error) {
    console.error('向量化切片出错:', error);
    ElMessage.error('向量化切片出错');
  } finally {
    loadingInstance.close();
  }
};

// 返回上一页
const goBack = () => {
  router.push('/platform/smartcs/knowledge/content');
};

// 格式化时间戳
const formatTimestamp = (timestamp: number | string | undefined): string => {
  if (!timestamp) return '';
  const numericTimestamp = typeof timestamp === 'string' ? parseInt(timestamp, 10) : timestamp;
  const date = new Date(numericTimestamp);
  if (isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  const pad = (num: number): string => (num < 10 ? '0' + num : String(num));

  return `${year}-${month}-${day} ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

// 页面加载时获取数据
onMounted(() => {
  searchParams.contentId = contentId.value;
  fetchContentInfo();
  fetchChunks();
});
</script>

<template>
  <div class="chunk-management">
    <div class="page-header">
      <div class="header-actions">
        <el-button @click="goBack" icon="ArrowLeft">返回内容管理</el-button>
      </div>
      <h2>切片管理</h2>
      <p>内容: {{ contentInfo.title || `ID: ${contentId}` }}</p>
    </div>

    <!-- 搜索和工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="searchParams.keyword"
        placeholder="搜索切片内容"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-input-number
        v-model="searchParams.chunkIndex"
        placeholder="段落序号"
        class="index-input"
        :min="1"
        controls-position="right"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="primary" icon="Plus" @click="openCreateDialog">新增切片</el-button>
    </div>

    <!-- 切片表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="chunkIndex" label="段落序号" width="100" />
      <el-table-column label="切片内容" min-width="300">
        <template #default="scope">
          <el-tooltip
            effect="dark"
            :content="scope.row.content"
            placement="top"
            :disabled="!scope.row.content"
          >
            <div class="content-ellipsis">{{ scope.row.content }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="tokenSize" label="Token数" width="100" />
      <el-table-column label="元信息" width="200">
        <template #default="scope">
          <el-tooltip
            effect="dark"
            :content="scope.row.metadata"
            placement="top"
            :disabled="!scope.row.metadata"
          >
            <div class="content-ellipsis">{{ scope.row.metadata || '-' }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatTimestamp(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <div style="display: flex; gap: 5px;">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="success" size="small" @click="handleVectorize(scope.row)">
              <el-icon><Cpu /></el-icon>
              向量化
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="searchParams.pageIndex"
        v-model:page-size="searchParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalCount"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
    >
      <el-form label-width="100px">
        <el-form-item label="段落序号">
          <el-input-number
            v-model="chunkForm.chunkIndex"
            :min="1"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="切片内容">
          <el-input
            v-model="chunkForm.content"
            type="textarea"
            placeholder="请输入切片内容"
            :rows="8"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Token数">
          <el-input-number
            v-model="chunkForm.tokenSize"
            :min="0"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="元信息">
          <el-input
            v-model="chunkForm.metadata"
            type="textarea"
            placeholder="请输入元信息（JSON格式）"
            :rows="3"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chunk-management {
  min-height: 70vh;
}

.page-header {
  margin-bottom: 20px;
  position: relative;
}

.header-actions {
  position: absolute;
  top: 0;
  right: 0;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #666;
}

.toolbar {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 250px;
}

.index-input {
  width: 150px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.content-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
</style> 