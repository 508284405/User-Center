<template>
  <el-dialog
    v-model="dialogVisible"
    title="添加好友"
    width="600px"
    :append-to-body="true"
  >
    <div class="add-friend-dialog">
      <!-- 搜索用户 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="输入用户ID、昵称或邮箱搜索用户"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" :loading="searching">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results" v-if="searchKeyword && searchResults.length > 0">
        <h4>搜索结果</h4>
        <div class="user-list">
          <div
            v-for="user in searchResults"
            :key="user.userId"
            class="user-item"
          >
            <div class="user-info">
              <el-avatar :size="40" :src="user.avatar" :alt="user.userName">
                {{ user.userName?.charAt(0) }}
              </el-avatar>
              
              <div class="user-details">
                <div class="user-name">{{ user.userName }}</div>
                <div class="user-id">ID: {{ user.userId }}</div>
                <div class="user-email" v-if="user.email">{{ user.email }}</div>
              </div>
            </div>

            <div class="user-actions">
              <el-tag v-if="user.isFriend" type="success">已是好友</el-tag>
              <el-tag v-else-if="user.hasApplication" type="warning">已发送申请</el-tag>
              <el-button
                v-else
                type="primary"
                size="small"
                @click="showApplyDialog(user)"
              >
                添加好友
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-else-if="searchKeyword && searchResults.length === 0 && !searching"
        description="未找到用户"
        :image-size="80"
      />

      <!-- 搜索提示 -->
      <div v-else-if="!searchKeyword" class="search-tips">
        <el-icon size="48" color="#c0c4cc"><Search /></el-icon>
        <p>输入关键词搜索用户</p>
        <div class="tips-list">
          <p>• 可以通过用户ID精确搜索</p>
          <p>• 可以通过昵称模糊搜索</p>
          <p>• 可以通过邮箱搜索注册用户</p>
        </div>
      </div>
    </div>

    <!-- 申请好友对话框 -->
    <el-dialog
      v-model="showApplyFriendDialog"
      title="发送好友申请"
      width="450px"
      :append-to-body="true"
    >
      <div class="apply-friend-content" v-if="selectedUser">
        <div class="user-preview">
          <el-avatar :size="50" :src="selectedUser.avatar" :alt="selectedUser.userName">
            {{ selectedUser.userName?.charAt(0) }}
          </el-avatar>
          <div class="user-info">
            <div class="user-name">{{ selectedUser.userName }}</div>
            <div class="user-id">ID: {{ selectedUser.userId }}</div>
          </div>
        </div>

        <div class="apply-form">
          <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules">
            <el-form-item label="申请消息" prop="applyMessage">
              <el-input
                v-model="applyForm.applyMessage"
                type="textarea"
                :rows="3"
                placeholder="请输入申请消息（可选）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <template #footer>
        <div class="apply-dialog-footer">
          <el-button @click="showApplyFriendDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleSendApplication"
            :loading="sending"
          >
            发送申请
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
import { Search } from '@element-plus/icons-vue';
import { 
  searchUsers, 
  sendFriendRequest,
  type UserSearchResult 
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
  'friend-added': [];
}>();

// 响应式数据
const searching = ref(false);
const searchKeyword = ref('');
const searchResults = ref<UserSearchResult[]>([]);
const showApplyFriendDialog = ref(false);
const selectedUser = ref<UserSearchResult | null>(null);
const sending = ref(false);

// 表单相关
const applyFormRef = ref<FormInstance>();
const applyForm = ref({
  applyMessage: ''
});

const applyRules: FormRules = {
  applyMessage: [
    { max: 200, message: '申请消息不能超过200字符', trigger: 'blur' }
  ]
};

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }

  searching.value = true;
  try {
    const response = await searchUsers({
      keyword: searchKeyword.value.trim(),
      searchUserId: props.currentUserId,
      page: 1,
      size: 20
    });

    if (response.success && response.data) {
      searchResults.value = response.data;
      
      if (response.data.length === 0) {
        ElMessage.info('未找到匹配的用户');
      }
    } else {
      ElMessage.error(response.errMessage || '搜索用户失败');
      searchResults.value = [];
    }
  } catch (error) {
    console.error('搜索用户失败:', error);
    ElMessage.error('搜索用户失败');
    searchResults.value = [];
  } finally {
    searching.value = false;
  }
};

const showApplyDialog = (user: UserSearchResult) => {
  selectedUser.value = user;
  applyForm.value.applyMessage = `我是 ${props.currentUserId}，想和您成为好友`;
  showApplyFriendDialog.value = true;
};

const handleSendApplication = async () => {
  if (!selectedUser.value) return;

  try {
    await applyFormRef.value?.validate();
  } catch (error) {
    return;
  }

  sending.value = true;
  try {
    const response = await sendFriendRequest({
      fromUserId: props.currentUserId,
      toUserId: selectedUser.value.userId,
      applyMessage: applyForm.value.applyMessage
    });

    if (response.success) {
      ElMessage.success('好友申请发送成功');
      showApplyFriendDialog.value = false;
      
      // 更新搜索结果中的状态
      const userIndex = searchResults.value.findIndex(
        user => user.userId === selectedUser.value?.userId
      );
      if (userIndex !== -1) {
        searchResults.value[userIndex].hasApplication = true;
      }
      
      emit('friend-added');
    } else {
      ElMessage.error(response.errMessage || '发送好友申请失败');
    }
  } catch (error) {
    console.error('发送好友申请失败:', error);
    ElMessage.error('发送好友申请失败');
  } finally {
    sending.value = false;
  }
};

// 监听对话框关闭
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    // 重置搜索状态
    searchKeyword.value = '';
    searchResults.value = [];
    showApplyFriendDialog.value = false;
    selectedUser.value = null;
    applyForm.value.applyMessage = '';
  }
});
</script>

<style scoped lang="scss">
.add-friend-dialog {
  .search-section {
    margin-bottom: 20px;
  }

  .search-results {
    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 14px;
    }

    .user-list {
      max-height: 400px;
      overflow-y: auto;

      .user-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid #f0f2f5;

        &:last-child {
          border-bottom: none;
        }

        .user-info {
          display: flex;
          align-items: center;
          flex: 1;

          .user-details {
            margin-left: 12px;

            .user-name {
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
            }

            .user-id {
              font-size: 12px;
              color: #909399;
              margin-bottom: 2px;
            }

            .user-email {
              font-size: 12px;
              color: #606266;
            }
          }
        }

        .user-actions {
          margin-left: 16px;
        }
      }
    }
  }

  .search-tips {
    text-align: center;
    padding: 40px 20px;
    color: #909399;

    p {
      margin: 8px 0;
    }

    .tips-list {
      margin-top: 20px;
      text-align: left;
      
      p {
        margin: 4px 0;
        font-size: 13px;
      }
    }
  }
}

.apply-friend-content {
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

  .apply-form {
    :deep(.el-form-item__label) {
      font-weight: 500;
    }
  }
}

.apply-dialog-footer {
  text-align: right;
}
</style>