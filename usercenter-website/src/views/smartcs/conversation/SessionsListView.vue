<template>
  <div class="sessions-list-container">
    <div class="page-header">
      <h2>会话列表</h2>
      <div class="header-actions">
        <el-input
          v-model="searchInput"
          placeholder="搜索会话ID或客户ID"
          clearable
          style="width: 240px; margin-right: 16px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="statusFilter"
          placeholder="状态过滤"
          clearable
          style="width: 160px; margin-right: 16px"
          @change="handleStatusChange"
        >
          <el-option label="全部" value="" />
          <el-option label="待分配" value="WAITING" />
          <el-option label="活跃" value="ACTIVE" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="refreshSessions">刷新列表</el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="sessions"
      style="width: 100%"
      border
    >
      <el-table-column prop="sessionId" label="会话ID" width="100" />
      <el-table-column prop="customerId" label="客户ID" width="100" />
      <el-table-column prop="agentId" label="客服ID" width="100" />
      <el-table-column prop="agentName" label="客服名称" width="120" />
      <el-table-column prop="sessionState" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.sessionState)">
            {{ getStatusText(scope.row.sessionState) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastMessage" label="最后消息" />
      <el-table-column prop="lastMsgTime" label="最后消息时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.lastMsgTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button
            v-if="scope.row.sessionState === 'WAITING'"
            type="primary"
            size="small"
            @click="openAssignDialog(scope.row)"
          >
            分配客服
          </el-button>
          <el-button
            v-if="scope.row.sessionState === 'ACTIVE'"
            type="success"
            size="small"
            @click="goToChat(scope.row.sessionId)"
          >
            参与对话
          </el-button>
          <el-button
            v-if="scope.row.sessionState !== 'CLOSED'"
            type="warning"
            size="small"
            @click="closeSession(scope.row.sessionId)"
          >
            关闭会话
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="viewSessionDetail(scope.row.sessionId)"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 分配客服对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配客服"
      width="30%"
    >
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="会话ID">
          <span>{{ assignForm.sessionId }}</span>
        </el-form-item>
        <el-form-item label="客户ID">
          <span>{{ assignForm.customerId }}</span>
        </el-form-item>
        <el-form-item label="客服">
          <el-select v-model="assignForm.agentId" placeholder="请选择客服" style="width: 100%">
            <el-option
              v-for="agent in agents"
              :key="agent.agentId"
              :label="agent.agentName"
              :value="agent.agentId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="assignAgent" :loading="assignLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { chatSessionApi, SessionVO, SessionPageQuery } from '@/api/smartcs/chatSession';
import { agentApi, AgentVO } from '@/api/smartcs/agent';
import { formatDate } from '@/utils/format';

// 路由
const router = useRouter();

// 加载状态
const loading = ref(false);
const assignLoading = ref(false);

// 会话列表
const sessions = ref<SessionVO[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchInput = ref('');
const statusFilter = ref('');

// 分配客服对话框
const assignDialogVisible = ref(false);
const assignForm = ref({
  sessionId: '',
  customerId: 0,
  agentId: undefined as number | undefined
});

// 客服列表
const agents = ref<AgentVO[]>([]);

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'WAITING':
      return 'warning';
    case 'ACTIVE':
      return 'success';
    case 'CLOSED':
      return 'info';
    default:
      return '';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'WAITING':
      return '待分配';
    case 'ACTIVE':
      return '活跃';
    case 'CLOSED':
      return '已关闭';
    default:
      return status;
  }
};

// 解析搜索输入内容
const parseSearchInput = (): { sessionId?: string; customerId?: number } => {
  const result: { sessionId?: string; customerId?: number } = {};
  
  if (searchInput.value) {
    // 判断是否为数字
    const searchNumber = Number(searchInput.value);
    if (!isNaN(searchNumber)) {
      // 默认优先作为会话ID处理，但作为字符串处理
      result.sessionId = searchInput.value;
    }
  }
  
  return result;
};

// 获取会话列表
const fetchSessions = async () => {
  loading.value = true;
  try {
    // 构建查询参数
    const searchParams = parseSearchInput();
    
    const query: SessionPageQuery = {
      ...searchParams,
      status: statusFilter.value || undefined,
      pageIndex: currentPage.value,
      pageSize: pageSize.value
    };
    
    // 调用分页查询API
    const response = await chatSessionApi.getSessionsPage(query);
    // 更新数据
    sessions.value = response.data;
    total.value = response.total;
  } catch (error) {
    ElMessage.error('获取会话列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取客服用户列表
const fetchAgentsByRole = async () => {
  try {
    // 获取所有AGENT角色的用户
    const agentUsers = await agentApi.listUsersByRoleCode('AGENT');
    agents.value = agentUsers;
  } catch (error) {
    console.error('获取客服用户列表失败:', error);
    ElMessage.error('获取客服用户列表失败');
  }
};

// 刷新会话列表
const refreshSessions = () => {
  fetchSessions();
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchSessions();
};

// 处理状态变化
const handleStatusChange = () => {
  currentPage.value = 1;
  fetchSessions();
};

// 处理页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchSessions();
};

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchSessions();
};

// 打开分配客服对话框
const openAssignDialog = async (session: SessionVO) => {
  assignForm.value = {
    sessionId: String(session.sessionId),
    customerId: session.customerId,
    agentId: undefined
  };
  
  // 打开对话框前先获取最新的客服列表
  await fetchAgentsByRole();
  
  assignDialogVisible.value = true;
};

// 分配客服
const assignAgent = async () => {
  if (!assignForm.value.agentId) {
    ElMessage.warning('请选择客服');
    return;
  }

  assignLoading.value = true;
  try {
    // 查找选中的客服对象，获取其名称
    const selectedAgent = agents.value.find(agent => agent.agentId === assignForm.value.agentId);
    const agentName = selectedAgent ? selectedAgent.agentName : '';
    
    const result = await chatSessionApi.assignAgent(
      assignForm.value.sessionId,
      assignForm.value.agentId,
      agentName
    );

    if (result) {
      ElMessage.success('分配客服成功');
      assignDialogVisible.value = false;
      refreshSessions();
    } else {
      ElMessage.error('分配客服失败');
    }
  } catch (error) {
    console.error('分配客服失败:', error);
    ElMessage.error('分配客服失败');
  } finally {
    assignLoading.value = false;
  }
};

// 关闭会话
const closeSession = async (sessionId: string) => {
  try {
    await ElMessageBox.confirm('确定要关闭该会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const result = await chatSessionApi.closeSession(sessionId);
    if (result) {
      ElMessage.success('关闭会话成功');
      refreshSessions();
    } else {
      ElMessage.error('关闭会话失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭会话失败:', error);
      ElMessage.error('关闭会话失败');
    }
  } finally {
    loading.value = false;
  }
};

// 查看会话详情
const viewSessionDetail = (sessionId: string) => {
  router.push(`/customer-service/chat/${sessionId}`);
};

// 参与对话
const goToChat = (sessionId: string) => {
  router.push(`/customer-service/chat/${sessionId}`);
};

// 组件挂载时获取会话列表
onMounted(() => {
  fetchSessions();
});
</script>

<style scoped>
.sessions-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
