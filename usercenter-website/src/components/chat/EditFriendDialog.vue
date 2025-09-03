<template>
  <el-dialog
    v-model="dialogVisible"
    title="编辑好友"
    width="500px"
    :append-to-body="true"
  >
    <div class="edit-friend-dialog" v-if="friend">
      <!-- 好友信息预览 -->
      <div class="friend-preview">
        <el-avatar :size="50" :src="friend.friendAvatar" :alt="friend.displayName">
          {{ friend.displayName?.charAt(0) }}
        </el-avatar>
        <div class="friend-info">
          <div class="friend-name">{{ friend.friendUserName }}</div>
          <div class="friend-id">ID: {{ friend.friendUserId }}</div>
          <div class="current-remark" v-if="friend.remarkName">
            当前备注: {{ friend.remarkName }}
          </div>
        </div>
      </div>

      <!-- 编辑表单 -->
      <el-form 
        ref="editFormRef" 
        :model="editForm" 
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="好友备注" prop="remarkName">
          <el-input
            v-model="editForm.remarkName"
            placeholder="设置好友备注名"
            maxlength="50"
            show-word-limit
            clearable
          />
          <div class="form-tip">不设置备注将显示好友的用户名</div>
        </el-form-item>

        <el-form-item label="好友分组" prop="friendGroup">
          <el-select
            v-model="editForm.friendGroup"
            placeholder="选择好友分组"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="group in friendGroups"
              :key="group.id"
              :label="group.groupName"
              :value="group.groupName"
            >
              <span>{{ group.groupName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ group.friendCount }} 人
              </span>
            </el-option>
          </el-select>
          <div class="form-tip">选择分组方便管理好友</div>
        </el-form-item>
      </el-form>

      <!-- 操作区域 -->
      <div class="operation-area">
        <el-alert
          title="其他操作"
          type="info"
          :closable="false"
          show-icon
        >
          <div class="operation-buttons">
            <el-button 
              type="warning" 
              size="small"
              @click="handleMoveToGroup"
              :disabled="!editForm.friendGroup"
            >
              移动到分组
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click="handleDeleteFriend"
            >
              删除好友
            </el-button>
            <el-button 
              type="info" 
              size="small"
              @click="handleBlockFriend"
            >
              拉入黑名单
            </el-button>
          </div>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSave"
          :loading="saving"
        >
          保存修改
        </el-button>
      </div>
    </template>

    <!-- 确认删除对话框 -->
    <el-dialog
      v-model="showDeleteConfirm"
      title="确认删除"
      width="400px"
      :append-to-body="true"
    >
      <div class="delete-confirm-content">
        <el-icon size="48" color="#f56c6c" style="margin-bottom: 16px">
          <WarningFilled />
        </el-icon>
        <p>确定要删除好友 <strong>{{ friend?.displayName }}</strong> 吗？</p>
        <p class="warning-text">删除后将无法接收对方消息，且聊天记录将被清空</p>
      </div>
      
      <template #footer>
        <div class="delete-confirm-footer">
          <el-button @click="showDeleteConfirm = false">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmDeleteFriend"
            :loading="deleting"
          >
            确定删除
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 确认拉黑对话框 -->
    <el-dialog
      v-model="showBlockConfirm"
      title="确认拉黑"
      width="400px"
      :append-to-body="true"
    >
      <div class="block-confirm-content">
        <el-icon size="48" color="#f56c6c" style="margin-bottom: 16px">
          <CircleCloseFilled />
        </el-icon>
        <p>确定要拉黑用户 <strong>{{ friend?.displayName }}</strong> 吗？</p>
        <p class="warning-text">拉黑后对方无法向您发送消息或添加好友申请</p>
        
        <el-form :model="blockForm" style="margin-top: 20px">
          <el-form-item label="拉黑原因">
            <el-input
              v-model="blockForm.blockReason"
              type="textarea"
              :rows="3"
              placeholder="请输入拉黑原因（可选）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="block-confirm-footer">
          <el-button @click="showBlockConfirm = false">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmBlockFriend"
            :loading="blocking"
          >
            确定拉黑
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue';
import { 
  updateFriend,
  deleteFriend,
  getFriendGroups,
  type FriendInfo,
  type FriendGroup
} from '@/api/smartcs/friend';

// Props
interface Props {
  modelValue: boolean;
  friend: FriendInfo | null;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'friend-updated': [];
}>();

// 响应式数据
const saving = ref(false);
const deleting = ref(false);
const blocking = ref(false);
const friendGroups = ref<FriendGroup[]>([]);
const showDeleteConfirm = ref(false);
const showBlockConfirm = ref(false);

// 表单相关
const editFormRef = ref<FormInstance>();
const editForm = ref({
  remarkName: '',
  friendGroup: ''
});

const blockForm = ref({
  blockReason: ''
});

const editRules: FormRules = {
  remarkName: [
    { max: 50, message: '备注名称不能超过50字符', trigger: 'blur' }
  ]
};

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const loadFriendGroups = async () => {
  if (!props.friend) return;
  
  try {
    const response = await getFriendGroups('current_user_id'); // TODO: 获取当前用户ID
    if (response.success && response.data) {
      friendGroups.value = response.data;
    }
  } catch (error) {
    console.error('加载好友分组失败:', error);
  }
};

const initForm = () => {
  if (props.friend) {
    editForm.value = {
      remarkName: props.friend.remarkName || '',
      friendGroup: props.friend.friendGroup || ''
    };
  }
};

const handleSave = async () => {
  if (!props.friend) return;

  try {
    await editFormRef.value?.validate();
  } catch (error) {
    return;
  }

  saving.value = true;
  try {
    const response = await updateFriend(props.friend.id, {
      remarkName: editForm.value.remarkName || undefined,
      friendGroup: editForm.value.friendGroup || undefined
    });

    if (response.success) {
      ElMessage.success('好友信息更新成功');
      dialogVisible.value = false;
      emit('friend-updated');
    } else {
      ElMessage.error(response.errMessage || '更新好友信息失败');
    }
  } catch (error) {
    console.error('更新好友信息失败:', error);
    ElMessage.error('更新好友信息失败');
  } finally {
    saving.value = false;
  }
};

const handleMoveToGroup = async () => {
  if (!props.friend || !editForm.value.friendGroup) return;

  try {
    const response = await updateFriend(props.friend.id, {
      friendGroup: editForm.value.friendGroup
    });

    if (response.success) {
      ElMessage.success(`已移动到分组 "${editForm.value.friendGroup}"`);
      emit('friend-updated');
    } else {
      ElMessage.error(response.errMessage || '移动分组失败');
    }
  } catch (error) {
    console.error('移动分组失败:', error);
    ElMessage.error('移动分组失败');
  }
};

const handleDeleteFriend = () => {
  showDeleteConfirm.value = true;
};

const confirmDeleteFriend = async () => {
  if (!props.friend) return;

  deleting.value = true;
  try {
    const response = await deleteFriend(props.friend.id, 'current_user_id'); // TODO: 获取当前用户ID

    if (response.success) {
      ElMessage.success('删除好友成功');
      showDeleteConfirm.value = false;
      dialogVisible.value = false;
      emit('friend-updated');
    } else {
      ElMessage.error(response.errMessage || '删除好友失败');
    }
  } catch (error) {
    console.error('删除好友失败:', error);
    ElMessage.error('删除好友失败');
  } finally {
    deleting.value = false;
  }
};

const handleBlockFriend = () => {
  blockForm.value.blockReason = '';
  showBlockConfirm.value = true;
};

const confirmBlockFriend = async () => {
  if (!props.friend) return;

  blocking.value = true;
  try {
    // TODO: 调用拉黑API
    // const response = await blockUser({
    //   userId: 'current_user_id',
    //   blockedUserId: props.friend.friendUserId,
    //   blockReason: blockForm.value.blockReason
    // });

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    ElMessage.success('已拉入黑名单');
    showBlockConfirm.value = false;
    dialogVisible.value = false;
    emit('friend-updated');
  } catch (error) {
    console.error('拉黑用户失败:', error);
    ElMessage.error('拉黑用户失败');
  } finally {
    blocking.value = false;
  }
};

// 监听对话框打开
watch(dialogVisible, (newVal) => {
  if (newVal && props.friend) {
    loadFriendGroups();
    initForm();
  }
});

// 监听好友数据变化
watch(() => props.friend, () => {
  if (props.friend && dialogVisible.value) {
    initForm();
  }
});
</script>

<style scoped lang="scss">
.edit-friend-dialog {
  .friend-preview {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    margin-bottom: 24px;

    .friend-info {
      margin-left: 15px;

      .friend-name {
        font-weight: 500;
        color: #303133;
        font-size: 16px;
        margin-bottom: 4px;
      }

      .friend-id {
        color: #909399;
        font-size: 14px;
        margin-bottom: 4px;
      }

      .current-remark {
        color: #606266;
        font-size: 13px;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .operation-area {
    margin-top: 24px;

    .operation-buttons {
      display: flex;
      gap: 8px;
      margin-top: 12px;
    }
  }
}

.dialog-footer {
  text-align: right;
}

.delete-confirm-content,
.block-confirm-content {
  text-align: center;
  padding: 20px;

  p {
    margin: 8px 0;
    color: #606266;
  }

  .warning-text {
    color: #f56c6c;
    font-size: 13px;
  }
}

.delete-confirm-footer,
.block-confirm-footer {
  text-align: right;
}
</style>