<template>
  <div class="friends-list">
    <!-- 头部搜索区域 -->
    <div class="friends-header">
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索好友..."
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>
      
      <div class="header-actions">
        <el-button
          type="primary"
          :icon="Plus"
          @click="showAddFriendDialog = true"
        >
          添加好友
        </el-button>
        
        <el-button
          :icon="Bell"
          @click="showApplicationsDialog = true"
          :badge="pendingApplicationsCount"
        >
          好友申请
        </el-button>
      </div>
    </div>

    <!-- 分组过滤 -->
    <div class="group-filter" v-if="friendGroups.length > 0">
      <el-radio-group v-model="selectedGroup" @change="handleGroupChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button 
          v-for="group in friendGroups" 
          :key="group.id" 
          :label="group.groupName"
        >
          {{ group.groupName }} ({{ group.friendCount }})
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 好友列表 -->
    <div class="friends-content">
      <el-scrollbar height="500px">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 空状态 -->
        <el-empty v-else-if="filteredFriends.length === 0" description="暂无好友">
          <el-button type="primary" @click="showAddFriendDialog = true">
            添加好友
          </el-button>
        </el-empty>

        <!-- 好友列表 -->
        <div v-else class="friends-list-content">
          <div
            v-for="friend in filteredFriends"
            :key="friend.id"
            class="friend-item"
            :class="{ 'friend-offline': friend.onlineStatus === 'OFFLINE' }"
            @click="handleFriendClick(friend)"
          >
            <div class="friend-avatar">
              <el-avatar
                :size="40"
                :src="friend.friendAvatar"
                :alt="friend.displayName"
              >
                {{ friend.displayName?.charAt(0) }}
              </el-avatar>
              
              <!-- 在线状态指示器 -->
              <div
                class="online-indicator"
                :class="`status-${friend.onlineStatus?.toLowerCase()}`"
                v-if="friend.onlineStatus"
              ></div>
            </div>

            <div class="friend-info">
              <div class="friend-name">
                <span class="display-name">{{ friend.displayName }}</span>
                <span class="user-name" v-if="friend.remarkName">
                  ({{ friend.friendUserName }})
                </span>
              </div>
              
              <div class="friend-status">
                <span class="last-message" v-if="friend.lastMessage">
                  {{ friend.lastMessage }}
                </span>
                <span class="last-seen" v-else-if="friend.lastSeenAt">
                  {{ formatLastSeen(friend.lastSeenAt) }}
                </span>
                <span class="group-name" v-if="friend.friendGroup">
                  {{ friend.friendGroup }}
                </span>
              </div>
            </div>

            <div class="friend-actions">
              <!-- 未读消息数 -->
              <el-badge
                v-if="friend.unreadCount && friend.unreadCount > 0"
                :value="friend.unreadCount"
                class="unread-badge"
              />
              
              <!-- 更多操作 -->
              <el-dropdown @command="handleFriendAction" trigger="click">
                <el-button text :icon="MoreFilled" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      :command="{ action: 'chat', friend }"
                      :disabled="!friend.canChat"
                    >
                      <el-icon><ChatDotRound /></el-icon>
                      发起聊天
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'edit', friend }">
                      <el-icon><Edit /></el-icon>
                      编辑好友
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', friend }" divided>
                      <el-icon><Delete /></el-icon>
                      删除好友
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 添加好友对话框 -->
    <add-friend-dialog
      v-model="showAddFriendDialog"
      :current-user-id="currentUserId"
      @friend-added="handleFriendAdded"
    />

    <!-- 好友申请对话框 -->
    <friend-applications-dialog
      v-model="showApplicationsDialog"
      :current-user-id="currentUserId"
      @application-processed="handleApplicationProcessed"
    />

    <!-- 编辑好友对话框 -->
    <edit-friend-dialog
      v-model="showEditFriendDialog"
      :friend="selectedFriend"
      @friend-updated="handleFriendUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Search, Plus, Bell, MoreFilled, ChatDotRound, Edit, Delete 
} from '@element-plus/icons-vue';
import { 
  getFriendsList, 
  getFriendGroups, 
  deleteFriend,
  type FriendInfo,
  type FriendGroup
} from '@/api/smartcs/friend';
import AddFriendDialog from './AddFriendDialog.vue';
import FriendApplicationsDialog from './FriendApplicationsDialog.vue';
import EditFriendDialog from './EditFriendDialog.vue';

// Props
interface Props {
  currentUserId: string;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'start-chat': [friendUserId: string];
}>();

// 响应式数据
const loading = ref(false);
const friends = ref<FriendInfo[]>([]);
const friendGroups = ref<FriendGroup[]>([]);
const searchKeyword = ref('');
const selectedGroup = ref('');
const showAddFriendDialog = ref(false);
const showApplicationsDialog = ref(false);
const showEditFriendDialog = ref(false);
const selectedFriend = ref<FriendInfo | null>(null);
const pendingApplicationsCount = ref(0);

// 计算属性
const filteredFriends = computed(() => {
  let filtered = friends.value;
  
  // 按分组过滤
  if (selectedGroup.value) {
    filtered = filtered.filter(friend => friend.friendGroup === selectedGroup.value);
  }
  
  // 按搜索关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(friend => 
      friend.displayName.toLowerCase().includes(keyword) ||
      friend.friendUserName?.toLowerCase().includes(keyword) ||
      friend.remarkName?.toLowerCase().includes(keyword)
    );
  }
  
  // 按在线状态排序（在线的排在前面）
  return filtered.sort((a, b) => {
    if (a.onlineStatus === 'ONLINE' && b.onlineStatus !== 'ONLINE') return -1;
    if (b.onlineStatus === 'ONLINE' && a.onlineStatus !== 'ONLINE') return 1;
    return 0;
  });
});

// 方法
const loadFriendsList = async () => {
  loading.value = true;
  try {
    const response = await getFriendsList({
      userId: props.currentUserId,
      keyword: searchKeyword.value,
      friendGroup: selectedGroup.value
    });
    
    if (response.success && response.data) {
      friends.value = response.data;
    } else {
      ElMessage.error(response.errMessage || '获取好友列表失败');
    }
  } catch (error) {
    console.error('加载好友列表失败:', error);
    ElMessage.error('加载好友列表失败');
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

const handleSearch = () => {
  loadFriendsList();
};

const handleGroupChange = () => {
  loadFriendsList();
};

const handleFriendClick = (friend: FriendInfo) => {
  if (friend.canChat) {
    emit('start-chat', friend.friendUserId);
  }
};

const handleFriendAction = async (command: { action: string, friend: FriendInfo }) => {
  const { action, friend } = command;
  
  switch (action) {
    case 'chat':
      emit('start-chat', friend.friendUserId);
      break;
      
    case 'edit':
      selectedFriend.value = friend;
      showEditFriendDialog.value = true;
      break;
      
    case 'delete':
      await handleDeleteFriend(friend);
      break;
  }
};

const handleDeleteFriend = async (friend: FriendInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除好友 "${friend.displayName}" 吗？`,
      '删除好友',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const response = await deleteFriend(friend.id, props.currentUserId);
    
    if (response.success) {
      ElMessage.success('删除好友成功');
      loadFriendsList();
      loadFriendGroups();
    } else {
      ElMessage.error(response.errMessage || '删除好友失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除好友失败:', error);
      ElMessage.error('删除好友失败');
    }
  }
};

const handleFriendAdded = () => {
  loadFriendsList();
  loadFriendGroups();
};

const handleApplicationProcessed = () => {
  loadFriendsList();
  loadFriendGroups();
  // TODO: 更新待处理申请数量
  // loadPendingApplicationsCount();
};

const handleFriendUpdated = () => {
  loadFriendsList();
  loadFriendGroups();
};

const formatLastSeen = (timestamp: number) => {
  const now = Date.now();
  const diff = now - timestamp;
  
  if (diff < 60000) { // 1分钟内
    return '刚刚在线';
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前在线`;
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前在线`;
  } else {
    return `${Math.floor(diff / 86400000)}天前在线`;
  }
};

// 生命周期
onMounted(() => {
  loadFriendsList();
  loadFriendGroups();
});

// 监听当前用户变化
watch(() => props.currentUserId, () => {
  if (props.currentUserId) {
    loadFriendsList();
    loadFriendGroups();
  }
});
</script>

<style scoped lang="scss">
.friends-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;

  .friends-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;

    .search-area {
      margin-bottom: 12px;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .group-filter {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f2f5;
    background: #fafafa;
  }

  .friends-content {
    flex: 1;
    min-height: 0;

    .loading-container {
      padding: 16px;
    }

    .friends-list-content {
      .friend-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #f5f7fa;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background-color: #f5f7fa;
        }

        &.friend-offline {
          opacity: 0.7;
        }

        .friend-avatar {
          position: relative;
          margin-right: 12px;

          .online-indicator {
            position: absolute;
            bottom: 2px;
            right: 2px;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            border: 2px solid #fff;

            &.status-online {
              background: #67c23a;
            }

            &.status-busy {
              background: #f56c6c;
            }

            &.status-away {
              background: #e6a23c;
            }

            &.status-invisible {
              background: #909399;
            }
          }
        }

        .friend-info {
          flex: 1;
          min-width: 0;

          .friend-name {
            display: flex;
            align-items: center;
            margin-bottom: 4px;

            .display-name {
              font-weight: 500;
              color: #303133;
            }

            .user-name {
              margin-left: 8px;
              font-size: 12px;
              color: #909399;
            }
          }

          .friend-status {
            font-size: 12px;
            color: #909399;

            .last-message {
              color: #606266;
            }

            .group-name {
              color: #409eff;
            }
          }
        }

        .friend-actions {
          display: flex;
          align-items: center;
          gap: 8px;

          .unread-badge {
            :deep(.el-badge__content) {
              font-size: 10px;
            }
          }
        }
      }
    }
  }
}
</style>