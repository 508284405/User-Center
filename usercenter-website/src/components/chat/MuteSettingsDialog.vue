<template>
  <el-dialog
    v-model="dialogVisible"
    title="消息免打扰设置"
    width="500px"
    :append-to-body="true"
  >
    <div class="mute-settings-dialog">
      <!-- 当前状态显示 -->
      <div class="current-status" v-if="currentSettings?.isMuted">
        <el-alert
          title="当前状态：已开启免打扰"
          :description="getMuteStatusDescription()"
          type="info"
          :closable="false"
          show-icon
        />
      </div>

      <!-- 设置选项 -->
      <div class="mute-options">
        <el-radio-group v-model="selectedOption" @change="handleOptionChange">
          <el-radio :label="'off'" :disabled="!currentSettings?.isMuted">
            <div class="option-content">
              <div class="option-title">关闭免打扰</div>
              <div class="option-desc">正常接收此会话的消息通知</div>
            </div>
          </el-radio>

          <el-radio :label="'1hour'">
            <div class="option-content">
              <div class="option-title">免打扰1小时</div>
              <div class="option-desc">1小时内不会收到此会话的消息通知</div>
            </div>
          </el-radio>

          <el-radio :label="'4hours'">
            <div class="option-content">
              <div class="option-title">免打扰4小时</div>
              <div class="option-desc">4小时内不会收到此会话的消息通知</div>
            </div>
          </el-radio>

          <el-radio :label="'8hours'">
            <div class="option-content">
              <div class="option-title">免打扰8小时</div>
              <div class="option-desc">8小时内不会收到此会话的消息通知</div>
            </div>
          </el-radio>

          <el-radio :label="'24hours'">
            <div class="option-content">
              <div class="option-title">免打扰24小时</div>
              <div class="option-desc">24小时内不会收到此会话的消息通知</div>
            </div>
          </el-radio>

          <el-radio :label="'permanent'">
            <div class="option-content">
              <div class="option-title">永久免打扰</div>
              <div class="option-desc">永远不会收到此会话的消息通知</div>
            </div>
          </el-radio>

          <el-radio :label="'custom'">
            <div class="option-content">
              <div class="option-title">自定义时间</div>
              <div class="option-desc">设置自定义的免打扰结束时间</div>
            </div>
          </el-radio>
        </el-radio-group>

        <!-- 自定义时间选择 -->
        <div v-if="selectedOption === 'custom'" class="custom-time-picker">
          <el-date-picker
            v-model="customEndTime"
            type="datetime"
            placeholder="选择免打扰结束时间"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
            :disabled-minutes="disabledMinutes"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
          <div class="time-picker-tip">
            请选择未来的时间作为免打扰结束时间
          </div>
        </div>
      </div>

      <!-- 高级选项 -->
      <div class="advanced-options">
        <el-collapse>
          <el-collapse-item title="高级选项" name="advanced">
            <div class="advanced-content">
              <el-checkbox v-model="allowImportantMessages">
                允许重要消息突破免打扰
              </el-checkbox>
              <div class="checkbox-desc">
                系统消息、@我的消息等重要消息仍会发送通知
              </div>

              <el-checkbox v-model="showUnreadBadge">
                显示未读消息徽章
              </el-checkbox>
              <div class="checkbox-desc">
                虽然不发送通知，但会在会话列表显示未读数量
              </div>
            </div>
          </el-collapse>
        </el-collapse>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirm"
          :loading="updating"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import {
  toggleMuteConversation,
  type ConversationSettings
} from '@/api/smartcs/conversation';

// Props
interface Props {
  modelValue: boolean;
  sessionId: number;
  currentSettings: ConversationSettings | null;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'settings-updated': [settings: ConversationSettings];
}>();

// 响应式数据
const updating = ref(false);
const selectedOption = ref('off');
const customEndTime = ref<string>('');
const allowImportantMessages = ref(true);
const showUnreadBadge = ref(true);

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const getMuteStatusDescription = () => {
  if (!props.currentSettings?.isMuted) {
    return '';
  }

  if (!props.currentSettings.muteEndAt) {
    return '永久免打扰，不会收到此会话的消息通知';
  }

  const endTime = new Date(props.currentSettings.muteEndAt);
  const now = new Date();
  
  if (endTime <= now) {
    return '免打扰时间已过期';
  }

  const diffMs = endTime.getTime() - now.getTime();
  const diffHours = Math.ceil(diffMs / (1000 * 60 * 60));
  
  if (diffHours < 24) {
    return `还有约 ${diffHours} 小时解除免打扰`;
  } else {
    const diffDays = Math.ceil(diffHours / 24);
    return `还有约 ${diffDays} 天解除免打扰`;
  }
};

const handleOptionChange = () => {
  if (selectedOption.value === 'custom') {
    // 设置默认的自定义时间（1小时后）
    const defaultTime = new Date();
    defaultTime.setHours(defaultTime.getHours() + 1);
    customEndTime.value = defaultTime.toISOString().slice(0, 16).replace('T', ' ');
  }
};

const disabledDate = (date: Date) => {
  // 禁用今天之前的日期
  return date < new Date(new Date().setHours(0, 0, 0, 0));
};

const disabledHours = () => {
  const hours = [];
  const now = new Date();
  const selectedDate = new Date(customEndTime.value);
  
  // 如果是今天，禁用当前小时之前的时间
  if (selectedDate.toDateString() === now.toDateString()) {
    for (let i = 0; i < now.getHours(); i++) {
      hours.push(i);
    }
  }
  
  return hours;
};

const disabledMinutes = (hour: number) => {
  const minutes = [];
  const now = new Date();
  const selectedDate = new Date(customEndTime.value);
  
  // 如果是今天且是当前小时，禁用当前分钟之前的时间
  if (selectedDate.toDateString() === now.toDateString() && hour === now.getHours()) {
    for (let i = 0; i <= now.getMinutes(); i++) {
      minutes.push(i);
    }
  }
  
  return minutes;
};

const handleConfirm = async () => {
  updating.value = true;
  
  try {
    let muteEndAt: number | undefined;
    let mute = selectedOption.value !== 'off';

    // 计算免打扰结束时间
    if (mute && selectedOption.value !== 'permanent') {
      const now = Date.now();
      
      switch (selectedOption.value) {
        case '1hour':
          muteEndAt = now + 1 * 60 * 60 * 1000;
          break;
        case '4hours':
          muteEndAt = now + 4 * 60 * 60 * 1000;
          break;
        case '8hours':
          muteEndAt = now + 8 * 60 * 60 * 1000;
          break;
        case '24hours':
          muteEndAt = now + 24 * 60 * 60 * 1000;
          break;
        case 'custom':
          if (!customEndTime.value) {
            ElMessage.warning('请选择自定义结束时间');
            return;
          }
          muteEndAt = new Date(customEndTime.value).getTime();
          
          if (muteEndAt <= now) {
            ElMessage.warning('结束时间必须晚于当前时间');
            return;
          }
          break;
      }
    }

    const response = await toggleMuteConversation({
      userId: props.currentSettings?.userId || '',
      sessionId: props.sessionId,
      mute,
      muteEndAt
    });

    if (response.success) {
      // 更新设置对象
      const updatedSettings: ConversationSettings = {
        ...props.currentSettings!,
        isMuted: mute,
        mutedAt: mute ? Date.now() : undefined,
        muteEndAt: mute ? muteEndAt : undefined,
        updatedAt: Date.now()
      };

      ElMessage.success(mute ? '免打扰设置成功' : '已关闭免打扰');
      emit('settings-updated', updatedSettings);
      dialogVisible.value = false;
    } else {
      ElMessage.error(response.errMessage || '设置失败');
    }
  } catch (error) {
    console.error('更新免打扰设置失败:', error);
    ElMessage.error('设置失败');
  } finally {
    updating.value = false;
  }
};

// 监听对话框打开
watch(dialogVisible, (newVal) => {
  if (newVal) {
    // 根据当前设置初始化选项
    if (props.currentSettings?.isMuted) {
      if (!props.currentSettings.muteEndAt) {
        selectedOption.value = 'permanent';
      } else {
        const remainingMs = props.currentSettings.muteEndAt - Date.now();
        const remainingHours = remainingMs / (1000 * 60 * 60);
        
        if (remainingHours <= 1) {
          selectedOption.value = '1hour';
        } else if (remainingHours <= 4) {
          selectedOption.value = '4hours';
        } else if (remainingHours <= 8) {
          selectedOption.value = '8hours';
        } else if (remainingHours <= 24) {
          selectedOption.value = '24hours';
        } else {
          selectedOption.value = 'custom';
          customEndTime.value = new Date(props.currentSettings.muteEndAt)
            .toISOString().slice(0, 16).replace('T', ' ');
        }
      }
    } else {
      selectedOption.value = 'off';
    }
  }
});
</script>

<style scoped lang="scss">
.mute-settings-dialog {
  .current-status {
    margin-bottom: 20px;
  }

  .mute-options {
    .option-content {
      .option-title {
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }

      .option-desc {
        font-size: 13px;
        color: #909399;
        line-height: 1.4;
      }
    }

    :deep(.el-radio) {
      display: block;
      height: auto;
      margin: 16px 0;
      
      .el-radio__label {
        padding-left: 8px;
      }
    }

    .custom-time-picker {
      margin-top: 16px;
      padding-left: 30px;

      .time-picker-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 8px;
      }
    }
  }

  .advanced-options {
    margin-top: 24px;

    .advanced-content {
      :deep(.el-checkbox) {
        display: block;
        margin-bottom: 12px;
      }

      .checkbox-desc {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
        margin-left: 24px;
        margin-bottom: 16px;
      }
    }
  }
}

.dialog-footer {
  text-align: right;
}
</style>