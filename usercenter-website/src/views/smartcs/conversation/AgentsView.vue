<template>
  <div class="agents-container">
    <div class="page-header">
      <h2>在线客服</h2>
      <el-button type="primary" @click="refreshAgents">刷新列表</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="agents"
      style="width: 100%"
      border
    >
      <el-table-column prop="agentId" label="客服ID" width="100" />
      <el-table-column prop="agentName" label="客服名称" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="activeSessions" label="活跃会话数" width="120">
        <template #default="scope">
          <el-badge :value="scope.row.activeSessions" :max="99" :hidden="scope.row.activeSessions <= 0">
            <span>{{ scope.row.activeSessions }}</span>
          </el-badge>
        </template>
      </el-table-column>
      <el-table-column prop="totalSessions" label="总会话数" width="120" />
      <el-table-column prop="lastActiveTime" label="最后活跃时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.lastActiveTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="viewAgentSessions(scope.row.agentId)"
            :disabled="scope.row.activeSessions === 0"
          >
            查看会话
          </el-button>
          <el-button
            :type="scope.row.status === 'ONLINE' ? 'danger' : 'success'"
            size="small"
            @click="toggleAgentStatus(scope.row)"
          >
            {{ scope.row.status === 'ONLINE' ? '设为离线' : '设为在线' }}
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="viewAgentProfile(scope.row.agentId)"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 客服会话对话框 -->
    <el-dialog
      v-model="sessionsDialogVisible"
      :title="`客服${selectedAgent.agentName}的会话列表`"
      width="70%"
    >
      <el-table :data="agentSessions" style="width: 100%" border>
        <el-table-column prop="sessionId" label="会话ID" width="100" />
        <el-table-column prop="customerId" label="客户ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastMessage" label="最后消息" />
        <el-table-column prop="lastMsgTime" label="最后消息时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.lastMsgTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="goToChat(scope.row.sessionId)"
            >
              参与对话
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="confirmCloseSession(scope.row.sessionId)"
              :disabled="scope.row.status === 'CLOSED'"
            >
              关闭会话
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { chatSessionApi, SessionVO } from '@/api/smartcs/chatSession';
import { agentApi, AgentVO } from '@/api/smartcs/agent';
import { formatDate } from '@/utils/format';

// 路由
const router = useRouter();

// 加载状态
const loading = ref(false);
const sessionsLoading = ref(false);

// 客服列表
const agents = ref<AgentVO[]>([]);

// 客服会话对话框
const sessionsDialogVisible = ref(false);
const selectedAgent = ref<AgentVO>({
  agentId: 0,
  agentName: '',
  status: 'OFFLINE',
  activeSessions: 0,
  totalSessions: 0,
  lastActiveTime: new Date()
});
const agentSessions = ref<SessionVO[]>([]);

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'ONLINE':
      return 'success';
    case 'OFFLINE':
      return 'info';
    case 'BUSY':
      return 'warning';
    case 'PENDING':
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
    case 'ONLINE':
      return '在线';
    case 'OFFLINE':
      return '离线';
    case 'BUSY':
      return '忙碌';
    case 'PENDING':
      return '待分配';
    case 'ACTIVE':
      return '活跃';
    case 'CLOSED':
      return '已关闭';
    default:
      return status;
  }
};

// 获取在线客服列表
const fetchAgents = async () => {
  loading.value = true;
  try {
    // 获取所有客服列表
    const allAgents = await agentApi.getAllAgents();
    agents.value = allAgents;
  } catch (error) {
    console.error('获取客服列表失败:', error);
    ElMessage.error('获取客服列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取客服的会话列表
const fetchAgentSessions = async (agentId: number) => {
  sessionsLoading.value = true;
  try {
    const sessions = await chatSessionApi.getAgentActiveSessions(agentId);
    agentSessions.value = sessions;
  } catch (error) {
    console.error('获取客服会话列表失败:', error);
    ElMessage.error('获取客服会话列表失败');
  } finally {
    sessionsLoading.value = false;
  }
};

// 刷新客服列表
const refreshAgents = () => {
  fetchAgents();
};

// 切换客服状态
const toggleAgentStatus = async (agent: AgentVO) => {
  try {
    const newStatus = agent.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
    const success = await agentApi.updateAgentStatus(agent.agentId, newStatus);
    
    if (success) {
      // 更新本地状态
      agent.status = newStatus;
      ElMessage.success(`客服${agent.agentName}状态已更新为${newStatus === 'ONLINE' ? '在线' : '离线'}`);
    }
  } catch (error) {
    console.error('更新客服状态失败:', error);
    ElMessage.error('更新客服状态失败');
  }
};

// 查看客服会话
const viewAgentSessions = async (agentId: number) => {
  // 先找到对应的客服
  const agent = agents.value.find(a => a.agentId === agentId);
  if (!agent) {
    ElMessage.error('客服不存在');
    return;
  }
  
  selectedAgent.value = agent;
  await fetchAgentSessions(agentId);
  sessionsDialogVisible.value = true;
};

// 查看客服详情
const viewAgentProfile = (agentId: number) => {
  // 这里应该跳转到客服详情页面
  ElMessage.info('功能开发中');
};

// 参与对话
const goToChat = (sessionId: string) => {
  router.push(`/customer-service/chat/${sessionId}`);
};

// 确认关闭会话
const confirmCloseSession = async (sessionId: string) => {
  try {
    await ElMessageBox.confirm('确定要关闭该会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result = await chatSessionApi.closeSession(sessionId);
    if (result) {
      ElMessage.success('会话已关闭');
      // 刷新会话列表
      if (selectedAgent.value) {
        fetchAgentSessions(selectedAgent.value.agentId);
      }
    } else {
      ElMessage.error('关闭会话失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭会话失败:', error);
      ElMessage.error('关闭会话失败');
    }
  }
};

// 组件挂载时获取客服列表
onMounted(() => {
  fetchAgents();
});
</script>

<style scoped>
.agents-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
