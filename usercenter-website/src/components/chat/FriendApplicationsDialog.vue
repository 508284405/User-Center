<template>
  <el-dialog
    v-model="dialogVisible"
    title="好友申请"
    width="700px"
    :append-to-body="true"
  >
    <div class="friend-applications-dialog">
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="收到的申请" name="received">
          <div class="applications-content">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-container">
              <el-skeleton :rows="3" animated />
            </div>

            <!-- 空状态 -->
            <el-empty 
              v-else-if="receivedApplications.length === 0" 
              description="暂无好友申请"
              :image-size="80"
            />

            <!-- 申请列表 -->
            <div v-else class="application-list">
              <div
                v-for="application in receivedApplications"
                :key="application.id"
                class="application-item"
              >
                <div class="application-info">
                  <el-avatar 
                    :size="45" 
                    :src="application.fromUserAvatar" 
                    :alt="application.fromUserName"
                  >
                    {{ application.fromUserName?.charAt(0) }}
                  </el-avatar>
                  
                  <div class="application-details">
                    <div class="user-info">
                      <span class="user-name">{{ application.fromUserName }}</span>
                      <span class="user-id">ID: {{ application.fromUserId }}</span>
                    </div>
                    
                    <div class="apply-message" v-if="application.applyMessage">
                      {{ application.applyMessage }}
                    </div>
                    
                    <div class="apply-time">
                      {{ formatApplyTime(application.appliedAt) }}
                    </div>
                  </div>
                </div>

                <div class="application-actions">
                  <template v-if="application.status === 0">
                    <el-button
                      type="primary"
                      size="small"
                      @click="showProcessDialog(application, 'accept')"
                    >
                      同意
                    </el-button>
                    <el-button
                      size="small"
                      @click="showProcessDialog(application, 'reject')"
                    >
                      拒绝
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="showProcessDialog(application, 'block')"
                    >
                      拉黑
                    </el-button>
                  </template>
                  
                  <el-tag v-else :type="getStatusTagType(application.status)">
                    {{ application.statusText }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="发送的申请" name="sent">
          <div class="applications-content">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-container">
              <el-skeleton :rows="3" animated />
            </div>

            <!-- 空状态 -->
            <el-empty 
              v-else-if="sentApplications.length === 0" 
              description="暂无发送的申请"
              :image-size="80"
            />

            <!-- 申请列表 -->
            <div v-else class="application-list">
              <div
                v-for="application in sentApplications"
                :key="application.id"
                class="application-item"
              >
                <div class="application-info">
                  <el-avatar 
                    :size="45" 
                    :src="getUserAvatar(application.toUserId)" 
                    :alt="getUserName(application.toUserId)"
                  >
                    {{ getUserName(application.toUserId)?.charAt(0) }}
                  </el-avatar>
                  
                  <div class="application-details">
                    <div class="user-info">
                      <span class="user-name">{{ getUserName(application.toUserId) }}</span>
                      <span class="user-id">ID: {{ application.toUserId }}</span>
                    </div>
                    
                    <div class="apply-message" v-if="application.applyMessage">
                      {{ application.applyMessage }}
                    </div>
                    
                    <div class="apply-time">
                      {{ formatApplyTime(application.appliedAt) }}
                    </div>
                  </div>
                </div>

                <div class="application-actions">
                  <el-tag :type="getStatusTagType(application.status)">
                    {{ application.statusText }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 处理申请对话框 -->
    <el-dialog
      v-model="showProcessApplicationDialog"
      :title="getProcessDialogTitle()"
      width="450px"
      :append-to-body="true"
    >
      <div class="process-application-content" v-if="selectedApplication">
        <div class="user-preview">
          <el-avatar 
            :size="50" 
            :src="selectedApplication.fromUserAvatar" 
            :alt="selectedApplication.fromUserName"
          >
            {{ selectedApplication.fromUserName?.charAt(0) }}
          </el-avatar>
          <div class="user-info">
            <div class="user-name">{{ selectedApplication.fromUserName }}</div>
            <div class="user-id">ID: {{ selectedApplication.fromUserId }}</div>
          </div>
        </div>

        <div class="process-form">
          <el-form ref="processFormRef" :model="processForm" :rules="processRules">
            <!-- 同意时的设置 -->
            <template v-if="processAction === 'accept'">
              <el-form-item label="好友备注" prop="remarkName">
                <el-input
                  v-model="processForm.remarkName"
                  placeholder="设置好友备注（可选）"
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="好友分组" prop="friendGroup">
                <el-select
                  v-model="processForm.friendGroup"
                  placeholder="选择好友分组"
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="group in friendGroups"
                    :key="group.id"
                    :label="group.groupName"
                    :value="group.groupName"
                  />
                </el-select>
              </el-form-item>
            </template>

            <!-- 拒绝时的原因 -->
            <el-form-item 
              v-if="processAction === 'reject'"
              label="拒绝原因" 
              prop="rejectReason"
            >
              <el-input
                v-model="processForm.rejectReason"
                type="textarea"
                :rows="3"
                placeholder="请输入拒绝原因"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <template #footer>
        <div class="process-dialog-footer">
          <el-button @click="showProcessApplicationDialog = false">取消</el-button>
          <el-button 
            :type="getProcessButtonType()"
            @click="handleProcessApplication"
            :loading="processing"
          >
            {{ getProcessButtonText() }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { 
  getReceivedApplications,
  getSentApplications,
  processFriendApplication,
  getFriendGroups,
  type FriendApplication,
  type FriendGroup
} from '@/api/smartcs/friend';

// Props
interface Props {
  modelValue: boolean;
  currentUserId: string;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'application-processed': [];
}>();

// 响应式数据
const loading = ref(false);
const processing = ref(false);
const activeTab = ref('received');
const receivedApplications = ref<FriendApplication[]>([]);
const sentApplications = ref<FriendApplication[]>([]);
const friendGroups = ref<FriendGroup[]>([]);
const showProcessApplicationDialog = ref(false);
const selectedApplication = ref<FriendApplication | null>(null);
const processAction = ref<'accept' | 'reject' | 'block'>('accept');

// 表单相关
const processFormRef = ref<FormInstance>();
const processForm = ref({
  remarkName: '',
  friendGroup: '',
  rejectReason: ''
});

const processRules: FormRules = {
  remarkName: [
    { max: 50, message: '备注名称不能超过50字符', trigger: 'blur' }
  ],
  rejectReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { max: 200, message: '拒绝原因不能超过200字符', trigger: 'blur' }
  ]
};

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const loadReceivedApplications = async () => {
  loading.value = true;
  try {
    const response = await getReceivedApplications(props.currentUserId);
    if (response.success && response.data) {
      receivedApplications.value = response.data;
    } else {
      ElMessage.error(response.errMessage || '获取收到的申请失败');
    }
  } catch (error) {
    console.error('加载收到的申请失败:', error);
    ElMessage.error('加载申请列表失败');
  } finally {
    loading.value = false;
  }
};

const loadSentApplications = async () => {
  loading.value = true;
  try {
    const response = await getSentApplications(props.currentUserId);
    if (response.success && response.data) {
      sentApplications.value = response.data;
    } else {
      ElMessage.error(response.errMessage || '获取发送的申请失败');
    }
  } catch (error) {
    console.error('加载发送的申请失败:', error);
    ElMessage.error('加载申请列表失败');
  } finally {
    loading.value = false;
  }
};

const loadFriendGroups = async () => {
  try {
    const response = await getFriendGroups(props.currentUserId);
    if (response.success && response.data) {
      friendGroups.value = response.data;
    }
  } catch (error) {
    console.error('加载好友分组失败:', error);
  }
};

const handleTabChange = (tabName: string) => {
  if (tabName === 'received') {
    loadReceivedApplications();
  } else if (tabName === 'sent') {
    loadSentApplications();
  }
};

const showProcessDialog = (application: FriendApplication, action: 'accept' | 'reject' | 'block') => {
  selectedApplication.value = application;
  processAction.value = action;
  
  // 重置表单
  processForm.value = {
    remarkName: application.fromUserName || '',
    friendGroup: '默认分组',
    rejectReason: ''
  };
  
  showProcessApplicationDialog.value = true;
};

const handleProcessApplication = async () => {
  if (!selectedApplication.value) return;

  // 验证表单
  if (processAction.value === 'reject') {
    try {
      await processFormRef.value?.validate();
    } catch (error) {
      return;
    }
  }

  processing.value = true;
  try {
    const response = await processFriendApplication({
      applicationId: selectedApplication.value.id,
      action: processAction.value,
      processedBy: props.currentUserId,
      rejectReason: processForm.value.rejectReason || undefined,
      remarkName: processForm.value.remarkName || undefined,
      friendGroup: processForm.value.friendGroup || undefined
    });

    if (response.success) {
      ElMessage.success(getProcessSuccessMessage());
      showProcessApplicationDialog.value = false;
      
      // 刷新申请列表
      if (activeTab.value === 'received') {
        loadReceivedApplications();
      }
      
      emit('application-processed');
    } else {
      ElMessage.error(response.errMessage || '处理申请失败');
    }
  } catch (error) {
    console.error('处理申请失败:', error);
    ElMessage.error('处理申请失败');
  } finally {
    processing.value = false;
  }
};

const formatApplyTime = (timestamp: number) => {
  const now = Date.now();
  const diff = now - timestamp;
  
  if (diff < 60000) { // 1分钟内
    return '刚刚';
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`;
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前`;
  } else if (diff < 604800000) { // 7天内
    return `${Math.floor(diff / 86400000)}天前`;
  } else {
    return new Date(timestamp).toLocaleDateString();
  }
};

const getStatusTagType = (status: number) => {
  switch (status) {
    case 0: return 'warning'; // 待审核
    case 1: return 'success'; // 已同意
    case 2: return 'info';    // 已拒绝
    case 3: return 'danger';  // 已拉黑
    default: return 'info';
  }
};

const getUserName = (userId: string) => {
  // TODO: 从用户服务获取用户名
  return userId;
};

const getUserAvatar = (userId: string) => {
  // TODO: 从用户服务获取头像
  return undefined;
};

const getProcessDialogTitle = () => {
  switch (processAction.value) {
    case 'accept': return '同意好友申请';
    case 'reject': return '拒绝好友申请';
    case 'block': return '拉黑用户';
    default: return '处理申请';
  }
};

const getProcessButtonType = () => {
  switch (processAction.value) {
    case 'accept': return 'primary';
    case 'reject': return 'info';
    case 'block': return 'danger';
    default: return 'primary';
  }
};

const getProcessButtonText = () => {
  switch (processAction.value) {
    case 'accept': return '同意';
    case 'reject': return '拒绝';
    case 'block': return '拉黑';
    default: return '确定';
  }
};

const getProcessSuccessMessage = () => {
  switch (processAction.value) {
    case 'accept': return '已同意好友申请';
    case 'reject': return '已拒绝好友申请';
    case 'block': return '已拉黑用户';
    default: return '处理完成';
  }
};

// 监听对话框打开
watch(dialogVisible, (newVal) => {
  if (newVal) {
    loadFriendGroups();
    if (activeTab.value === 'received') {
      loadReceivedApplications();
    } else {
      loadSentApplications();
    }
  } else {
    // 重置状态
    showProcessApplicationDialog.value = false;
    selectedApplication.value = null;
  }
});
</script>

<style scoped lang="scss">
.friend-applications-dialog {
  .applications-content {
    min-height: 300px;

    .loading-container {
      padding: 20px;
    }

    .application-list {
      .application-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 0;
        border-bottom: 1px solid #f0f2f5;

        &:last-child {
          border-bottom: none;
        }

        .application-info {
          display: flex;
          align-items: center;
          flex: 1;

          .application-details {
            margin-left: 15px;
            flex: 1;

            .user-info {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 6px;

              .user-name {
                font-weight: 500;
                color: #303133;
              }

              .user-id {
                font-size: 12px;
                color: #909399;
              }
            }

            .apply-message {
              color: #606266;
              font-size: 13px;
              margin-bottom: 4px;
              line-height: 1.4;
            }

            .apply-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }

        .application-actions {
          margin-left: 16px;
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}

.process-application-content {
  .user-preview {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    margin-bottom: 20px;

    .user-info {
      margin-left: 15px;

      .user-name {
        font-weight: 500;
        color: #303133;
        font-size: 16px;
        margin-bottom: 4px;
      }

      .user-id {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .process-form {
    :deep(.el-form-item__label) {
      font-weight: 500;
    }
  }
}

.process-dialog-footer {
  text-align: right;
}
</style>