<template>
  <div class="message-reactions" v-if="reactions && reactions.length > 0">
    <div class="reactions-list">
      <div
        v-for="reaction in reactions"
        :key="reaction.emoji"
        class="reaction-item"
        :class="{ 'user-reacted': reaction.hasReacted }"
        @click="handleReactionClick(reaction)"
        @mouseenter="showReactionTooltip(reaction, $event)"
        @mouseleave="hideReactionTooltip"
      >
        <span class="reaction-emoji">{{ reaction.emoji }}</span>
        <span class="reaction-count">{{ reaction.count }}</span>
      </div>
      
      <!-- 添加表情按钮 -->
      <div class="add-reaction-btn" @click="showReactionPicker = true">
        <el-icon><Plus /></el-icon>
      </div>
    </div>

    <!-- 表情选择器 -->
    <el-popover
      v-model:visible="showReactionPicker"
      placement="top-start"
      width="320"
      trigger="manual"
      :append-to-body="true"
      class="reaction-picker-popover"
    >
      <template #reference>
        <div class="picker-reference"></div>
      </template>
      
      <div class="reaction-picker">
        <div class="picker-header">
          <h4>选择表情反应</h4>
          <el-button 
            type="text" 
            @click="showReactionPicker = false"
            class="close-btn"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        
        <div class="picker-content">
          <!-- 常用表情 -->
          <div class="reaction-category">
            <div class="category-title">常用表情</div>
            <div class="emoji-grid">
              <div
                v-for="emoji in commonEmojis"
                :key="emoji.code"
                class="emoji-item"
                @click="handleAddReaction(emoji)"
                :title="emoji.name"
              >
                {{ emoji.code }}
              </div>
            </div>
          </div>
          
          <!-- 更多表情 -->
          <div class="reaction-category">
            <div class="category-title">更多表情</div>
            <div class="emoji-grid">
              <div
                v-for="emoji in moreEmojis"
                :key="emoji.code"
                class="emoji-item"
                @click="handleAddReaction(emoji)"
                :title="emoji.name"
              >
                {{ emoji.code }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-popover>

    <!-- 反应详情弹窗 -->
    <el-tooltip
      ref="reactionTooltipRef"
      :visible="showTooltip"
      :content="tooltipContent"
      placement="top"
      effect="dark"
      :append-to-body="true"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Close } from '@element-plus/icons-vue';
import { addMessageReaction, getMessageReactions, type ReactionDTO } from '@/api/smartcs/message';

// Props
interface Props {
  msgId: string;
  sessionId: number;
  initialReactions?: ReactionDTO[];
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'reactions-updated': [reactions: ReactionDTO[]];
}>();

// 响应式数据
const reactions = ref<ReactionDTO[]>([]);
const showReactionPicker = ref(false);
const showTooltip = ref(false);
const tooltipContent = ref('');
const reactionTooltipRef = ref();

// 常用表情配置
const commonEmojis = [
  { code: '👍', name: '点赞' },
  { code: '❤️', name: '爱心' },
  { code: '😄', name: '哈哈' },
  { code: '😮', name: '惊讶' },
  { code: '😢', name: '难过' },
  { code: '😡', name: '愤怒' },
  { code: '👏', name: '鼓掌' },
  { code: '🎉', name: '庆祝' }
];

const moreEmojis = [
  { code: '😊', name: '微笑' },
  { code: '😃', name: '开心' },
  { code: '😆', name: '大笑' },
  { code: '😍', name: '心眼' },
  { code: '🥰', name: '可爱' },
  { code: '😘', name: '飞吻' },
  { code: '🤔', name: '思考' },
  { code: '😎', name: '酷' },
  { code: '😴', name: '睡觉' },
  { code: '🤯', name: '震惊' },
  { code: '🙄', name: '翻白眼' },
  { code: '😤', name: '哼' },
  { code: '🔥', name: '火' },
  { code: '💯', name: '百分百' },
  { code: '✨', name: '闪闪' },
  { code: '💪', name: '加油' }
];

// 计算属性
const hasReactions = computed(() => {
  return reactions.value && reactions.value.length > 0;
});

// 方法
const loadReactions = async () => {
  try {
    const response = await getMessageReactions(props.msgId);
    if (response.success && response.data) {
      reactions.value = response.data;
      emit('reactions-updated', reactions.value);
    }
  } catch (error) {
    console.error('加载表情反应失败:', error);
  }
};

const handleReactionClick = async (reaction: ReactionDTO) => {
  try {
    const response = await addMessageReaction({
      msgId: props.msgId,
      sessionId: props.sessionId,
      emoji: reaction.emoji,
      name: reaction.name,
      action: 'toggle' // 切换模式：有则删除，无则添加
    });

    if (response.success) {
      // 重新加载反应列表
      await loadReactions();
      
      const action = reaction.hasReacted ? '取消' : '添加';
      ElMessage.success(`${action}表情反应成功`);
    } else {
      ElMessage.error(response.errMessage || '操作失败');
    }
  } catch (error: any) {
    console.error('切换表情反应失败:', error);
    ElMessage.error('操作失败: ' + (error.message || '未知错误'));
  }
};

const handleAddReaction = async (emoji: { code: string; name: string }) => {
  try {
    const response = await addMessageReaction({
      msgId: props.msgId,
      sessionId: props.sessionId,
      emoji: emoji.code,
      name: emoji.name,
      action: 'add'
    });

    if (response.success) {
      showReactionPicker.value = false;
      await loadReactions();
      ElMessage.success('添加表情反应成功');
    } else {
      ElMessage.error(response.errMessage || '添加失败');
    }
  } catch (error: any) {
    console.error('添加表情反应失败:', error);
    ElMessage.error('添加失败: ' + (error.message || '未知错误'));
  }
};

const showReactionTooltip = (reaction: ReactionDTO, event: MouseEvent) => {
  if (!reaction.userIds || reaction.userIds.length === 0) {
    return;
  }

  // 构建提示内容
  let content = '';
  if (reaction.count === 1) {
    content = `1人觉得${reaction.name}`;
  } else if (reaction.count <= 3) {
    // 显示具体用户（需要用户名映射）
    content = `${reaction.count}人觉得${reaction.name}`;
  } else {
    content = `${reaction.count}人觉得${reaction.name}`;
  }
  
  tooltipContent.value = content;
  
  nextTick(() => {
    if (reactionTooltipRef.value) {
      showTooltip.value = true;
      
      // 设置tooltip位置
      const tooltip = reactionTooltipRef.value;
      if (tooltip && tooltip.$el) {
        const rect = (event.target as Element).getBoundingClientRect();
        tooltip.$el.style.left = rect.left + rect.width / 2 + 'px';
        tooltip.$el.style.top = rect.top - 10 + 'px';
      }
    }
  });
};

const hideReactionTooltip = () => {
  showTooltip.value = false;
};

// 生命周期
onMounted(() => {
  if (props.initialReactions) {
    reactions.value = props.initialReactions;
  } else {
    loadReactions();
  }
});

// 暴露方法给父组件
defineExpose({
  loadReactions,
  reactions
});
</script>

<style scoped lang="scss">
.message-reactions {
  margin-top: 8px;
  
  .reactions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
    
    .reaction-item {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      border: 1px solid #e4e7ed;
      border-radius: 12px;
      background-color: #f8f9fa;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 12px;
      
      &:hover {
        border-color: #409eff;
        background-color: #ecf5ff;
      }
      
      &.user-reacted {
        border-color: #409eff;
        background-color: #ecf5ff;
        color: #409eff;
      }
      
      .reaction-emoji {
        font-size: 14px;
        line-height: 1;
      }
      
      .reaction-count {
        font-size: 12px;
        font-weight: 500;
        min-width: 12px;
        text-align: center;
      }
    }
    
    .add-reaction-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border: 1px dashed #d9d9d9;
      border-radius: 12px;
      background-color: #fafafa;
      cursor: pointer;
      transition: all 0.2s;
      color: #909399;
      
      &:hover {
        border-color: #409eff;
        background-color: #ecf5ff;
        color: #409eff;
      }
      
      .el-icon {
        font-size: 12px;
      }
    }
  }
}

.reaction-picker-popover {
  .reaction-picker {
    .picker-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h4 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
      
      .close-btn {
        color: #909399;
        
        &:hover {
          color: #606266;
        }
      }
    }
    
    .picker-content {
      .reaction-category {
        margin-bottom: 20px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .category-title {
          font-size: 14px;
          font-weight: 500;
          color: #606266;
          margin-bottom: 12px;
        }
        
        .emoji-grid {
          display: grid;
          grid-template-columns: repeat(8, 1fr);
          gap: 8px;
          
          .emoji-item {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 32px;
            height: 32px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 18px;
            transition: background-color 0.2s;
            
            &:hover {
              background-color: #f5f7fa;
            }
            
            &:active {
              background-color: #e4e7ed;
            }
          }
        }
      }
    }
  }
}

:deep(.el-tooltip) {
  max-width: 200px;
}
</style>