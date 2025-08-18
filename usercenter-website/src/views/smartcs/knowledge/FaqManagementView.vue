<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus, Delete, Edit } from '@element-plus/icons-vue';
import { knowledgeApi } from '@/api/smartcs/knowledge';

// 定义FAQ数据类型
interface FaqItem {
  id?: string;
  question: string;
  answer: string;
  hitCount?: string;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
}

// 格式化时间戳函数
const formatTimestamp = (timestamp: string | number | undefined): string => {
  if (!timestamp) return '';
  const date = new Date(Number(timestamp));
  const year = date.getFullYear();
  const month = date.getMonth() + 1; // 月份是从0开始的
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  const pad = (num: number): string => (num < 10 ? '0' + num : String(num));

  return `${year}-${month}-${day} ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

// 查询参数
const searchParams = reactive({
  keyword: '',
  page: 1,
  size: 10,
});

// 表格数据和加载状态
const tableData = ref<FaqItem[]>([]);
const total = ref(0);
const loading = ref(false);

// 编辑表单
const editDialogVisible = ref(false);
const editForm = reactive<FaqItem>({
  question: '',
  answer: '',
});
const isCreating = ref(true);

// 查询FAQ列表
const fetchFaqs = async () => {
  loading.value = true;
  try {
    const response = await knowledgeApi.listFaqs(
      searchParams.keyword,
      searchParams.page,
      searchParams.size
    );
    
    if (response && response.success) {
      tableData.value = response.data || [];
      total.value = response.total || 0;
    } else {
      ElMessage.error('获取FAQ列表失败');
    }
  } catch (error) {
    console.error('获取FAQ列表出错:', error);
    ElMessage.error('获取FAQ列表出错');
  } finally {
    loading.value = false;
  }
};

// 搜索FAQ
const handleSearch = () => {
  searchParams.page = 1;
  fetchFaqs();
};

// 重置搜索
const resetSearch = () => {
  searchParams.keyword = '';
  searchParams.page = 1;
  fetchFaqs();
};

// 分页变化
const handleCurrentChange = (current: number) => {
  searchParams.page = current;
  fetchFaqs();
};

const handleSizeChange = (size: number) => {
  searchParams.size = size;
  searchParams.page = 1;
  fetchFaqs();
};

// 打开创建FAQ对话框
const openCreateDialog = () => {
  isCreating.value = true;
  editForm.id = undefined;
  editForm.question = '';
  editForm.answer = '';
  editDialogVisible.value = true;
};

// 打开编辑FAQ对话框
const openEditDialog = (row: FaqItem) => {
  isCreating.value = false;
  editForm.id = row.id;
  editForm.question = row.question;
  editForm.answer = row.answer;
  editDialogVisible.value = true;
};

// 保存FAQ
const saveFaq = async () => {
  if (!editForm.question || !editForm.answer) {
    ElMessage.warning('问题和回答不能为空');
    return;
  }

  try {
    const response = await knowledgeApi.addFaq({
      id: editForm.id,
      question: editForm.question,
      answer: editForm.answer
    });

    if (response && response.success) {
      ElMessage.success(isCreating.value ? 'FAQ创建成功' : 'FAQ更新成功');
      editDialogVisible.value = false;
      fetchFaqs();
    } else {
      ElMessage.error(isCreating.value ? 'FAQ创建失败' : 'FAQ更新失败');
    }
  } catch (error) {
    console.error('保存FAQ出错:', error);
    ElMessage.error('保存FAQ出错');
  }
};

// 删除FAQ
const handleDelete = (row: FaqItem) => {
  if (!row.id) return;
  
  ElMessageBox.confirm('确定要删除这个FAQ吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await knowledgeApi.deleteFaq(row.id!);
      
      if (response && response.success) {
        ElMessage.success('FAQ删除成功');
        fetchFaqs();
      } else {
        ElMessage.error('FAQ删除失败');
      }
    } catch (error) {
      console.error('删除FAQ出错:', error);
      ElMessage.error('删除FAQ出错');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 页面加载时获取数据
onMounted(() => {
  fetchFaqs();
});
</script>

<template>
  <div class="faq-management">
    <div class="page-header">
      <h2>FAQ管理</h2>
      <p>管理知识库FAQ问题与回答</p>
    </div>

    <!-- 搜索和工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="searchParams.keyword"
        placeholder="搜索问题或回答"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="primary" icon="Plus" @click="openCreateDialog">新建FAQ</el-button>
    </div>

    <!-- FAQ表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="回答" min-width="300" show-overflow-tooltip />
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatTimestamp(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="scope">
          {{ formatTimestamp(scope.row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="searchParams.page"
        v-model:page-size="searchParams.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 编辑FAQ对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isCreating ? '创建FAQ' : '编辑FAQ'"
      width="50%"
    >
      <el-form label-width="80px">
        <el-form-item label="问题">
          <el-input v-model="editForm.question" placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="回答">
          <el-input
            v-model="editForm.answer"
            type="textarea"
            :rows="6"
            placeholder="请输入回答"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveFaq">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.faq-management {
  min-height: 70vh;
}

.page-header {
  margin-bottom: 20px;
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
}

.search-input {
  width: 300px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 