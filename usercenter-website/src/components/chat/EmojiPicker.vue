<template>
  <el-popover
    placement="top-start"
    :width="320"
    trigger="click"
    v-model:visible="visible"
  >
    <template #reference>
      <el-button
        type="text"
        size="small"
        class="emoji-trigger"
        @click="visible = !visible"
      >
        <el-icon><More /></el-icon>
        <span>😊</span>
      </el-button>
    </template>

    <!-- 表情选择器内容 -->
    <div class="emoji-picker">
      <!-- 表情分类标签 -->
      <div class="emoji-categories">
        <el-button
          v-for="category in emojiCategories"
          :key="category.key"
          :type="activeCategory === category.key ? 'primary' : 'text'"
          size="small"
          @click="activeCategory = category.key"
        >
          {{ category.icon }}
        </el-button>
      </div>

      <!-- 表情列表 -->
      <div class="emoji-list">
        <div
          v-for="emoji in filteredEmojis"
          :key="emoji.code"
          class="emoji-item"
          :title="emoji.name"
          @click="selectEmoji(emoji)"
        >
          {{ emoji.emoji }}
        </div>
      </div>
      
      <!-- 最近使用的表情 -->
      <div v-if="recentEmojis.length > 0" class="recent-emojis">
        <div class="recent-title">最近使用</div>
        <div class="recent-list">
          <div
            v-for="emoji in recentEmojis"
            :key="emoji.code"
            class="emoji-item"
            :title="emoji.name"
            @click="selectEmoji(emoji)"
          >
            {{ emoji.emoji }}
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { More } from '@element-plus/icons-vue';

// 表情数据接口
interface EmojiItem {
  code: string;
  emoji: string;
  name: string;
  category: string;
}

// 表情分类接口
interface EmojiCategory {
  key: string;
  name: string;
  icon: string;
}

interface Props {
  modelValue?: boolean;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'emoji-selected', emoji: EmojiItem): void;
}

const props = defineProps<Props>();
const emits = defineEmits<Emits>();

// 响应式数据
const visible = ref(false);
const activeCategory = ref('smileys');
const recentEmojis = ref<EmojiItem[]>([]);

// 表情分类
const emojiCategories: EmojiCategory[] = [
  { key: 'smileys', name: '表情', icon: '😀' },
  { key: 'people', name: '人物', icon: '👋' },
  { key: 'nature', name: '自然', icon: '🌱' },
  { key: 'food', name: '食物', icon: '🍎' },
  { key: 'activities', name: '活动', icon: '⚽' },
  { key: 'travel', name: '旅行', icon: '✈️' },
  { key: 'objects', name: '物品', icon: '💡' },
  { key: 'symbols', name: '符号', icon: '❤️' },
  { key: 'flags', name: '旗帜', icon: '🏁' }
];

// 表情数据（简化版，实际项目中可以使用更完整的表情库）
const allEmojis: EmojiItem[] = [
  // 表情类
  { code: 'grinning', emoji: '😀', name: '开心', category: 'smileys' },
  { code: 'smiley', emoji: '😃', name: '笑脸', category: 'smileys' },
  { code: 'smile', emoji: '😄', name: '微笑', category: 'smileys' },
  { code: 'grin', emoji: '😁', name: '露齿笑', category: 'smileys' },
  { code: 'laughing', emoji: '😆', name: '大笑', category: 'smileys' },
  { code: 'sweat_smile', emoji: '😅', name: '苦笑', category: 'smileys' },
  { code: 'joy', emoji: '😂', name: '喜极而泣', category: 'smileys' },
  { code: 'wink', emoji: '😉', name: '眨眼', category: 'smileys' },
  { code: 'blush', emoji: '😊', name: '害羞', category: 'smileys' },
  { code: 'yum', emoji: '😋', name: '好吃', category: 'smileys' },
  { code: 'heart_eyes', emoji: '😍', name: '爱心眼', category: 'smileys' },
  { code: 'kissing_heart', emoji: '😘', name: '飞吻', category: 'smileys' },
  { code: 'smiling_face_with_3_hearts', emoji: '🥰', name: '满心欢喜', category: 'smileys' },
  { code: 'thinking', emoji: '🤔', name: '思考', category: 'smileys' },
  { code: 'neutral_face', emoji: '😐', name: '面无表情', category: 'smileys' },
  { code: 'expressionless', emoji: '😑', name: '无语', category: 'smileys' },
  { code: 'confused', emoji: '😕', name: '困惑', category: 'smileys' },
  { code: 'worried', emoji: '😟', name: '担心', category: 'smileys' },
  { code: 'cry', emoji: '😢', name: '哭泣', category: 'smileys' },
  { code: 'sob', emoji: '😭', name: '痛哭', category: 'smileys' },
  
  // 人物类
  { code: 'wave', emoji: '👋', name: '挥手', category: 'people' },
  { code: 'raised_hand', emoji: '✋', name: '举手', category: 'people' },
  { code: 'ok_hand', emoji: '👌', name: 'OK', category: 'people' },
  { code: 'thumbsup', emoji: '👍', name: '点赞', category: 'people' },
  { code: 'thumbsdown', emoji: '👎', name: '踩', category: 'people' },
  { code: 'clap', emoji: '👏', name: '鼓掌', category: 'people' },
  { code: 'pray', emoji: '🙏', name: '祈祷', category: 'people' },
  
  // 自然类
  { code: 'seedling', emoji: '🌱', name: '幼苗', category: 'nature' },
  { code: 'evergreen_tree', emoji: '🌲', name: '常青树', category: 'nature' },
  { code: 'deciduous_tree', emoji: '🌳', name: '落叶树', category: 'nature' },
  { code: 'cherry_blossom', emoji: '🌸', name: '樱花', category: 'nature' },
  { code: 'rose', emoji: '🌹', name: '玫瑰', category: 'nature' },
  { code: 'sunflower', emoji: '🌻', name: '向日葵', category: 'nature' },
  { code: 'sunny', emoji: '☀️', name: '晴天', category: 'nature' },
  { code: 'partly_sunny', emoji: '⛅', name: '多云', category: 'nature' },
  { code: 'rainbow', emoji: '🌈', name: '彩虹', category: 'nature' },
  
  // 食物类
  { code: 'apple', emoji: '🍎', name: '苹果', category: 'food' },
  { code: 'banana', emoji: '🍌', name: '香蕉', category: 'food' },
  { code: 'grapes', emoji: '🍇', name: '葡萄', category: 'food' },
  { code: 'strawberry', emoji: '🍓', name: '草莓', category: 'food' },
  { code: 'watermelon', emoji: '🍉', name: '西瓜', category: 'food' },
  { code: 'pizza', emoji: '🍕', name: '披萨', category: 'food' },
  { code: 'hamburger', emoji: '🍔', name: '汉堡', category: 'food' },
  { code: 'fries', emoji: '🍟', name: '薯条', category: 'food' },
  { code: 'cake', emoji: '🍰', name: '蛋糕', category: 'food' },
  
  // 活动类
  { code: 'soccer', emoji: '⚽', name: '足球', category: 'activities' },
  { code: 'basketball', emoji: '🏀', name: '篮球', category: 'activities' },
  { code: 'tennis', emoji: '🎾', name: '网球', category: 'activities' },
  { code: 'guitar', emoji: '🎸', name: '吉他', category: 'activities' },
  { code: 'microphone', emoji: '🎤', name: '麦克风', category: 'activities' },
  { code: 'game_die', emoji: '🎲', name: '骰子', category: 'activities' },
  
  // 符号类
  { code: 'heart', emoji: '❤️', name: '红心', category: 'symbols' },
  { code: 'yellow_heart', emoji: '💛', name: '黄心', category: 'symbols' },
  { code: 'green_heart', emoji: '💚', name: '绿心', category: 'symbols' },
  { code: 'blue_heart', emoji: '💙', name: '蓝心', category: 'symbols' },
  { code: 'purple_heart', emoji: '💜', name: '紫心', category: 'symbols' },
  { code: 'fire', emoji: '🔥', name: '火', category: 'symbols' },
  { code: 'star', emoji: '⭐', name: '星星', category: 'symbols' },
  { code: 'sparkles', emoji: '✨', name: '闪光', category: 'symbols' },
  { code: 'boom', emoji: '💥', name: '爆炸', category: 'symbols' },
];

// 过滤后的表情列表
const filteredEmojis = computed(() => {
  return allEmojis.filter(emoji => emoji.category === activeCategory.value);
});

// 选择表情
const selectEmoji = (emoji: EmojiItem) => {
  // 添加到最近使用
  addToRecentEmojis(emoji);
  
  // 发出选择事件
  emits('emoji-selected', emoji);
  
  // 关闭弹出框
  visible.value = false;
};

// 添加到最近使用的表情
const addToRecentEmojis = (emoji: EmojiItem) => {
  // 移除已存在的相同表情
  const index = recentEmojis.value.findIndex(item => item.code === emoji.code);
  if (index !== -1) {
    recentEmojis.value.splice(index, 1);
  }
  
  // 添加到最前面
  recentEmojis.value.unshift(emoji);
  
  // 保持最多12个最近使用的表情
  if (recentEmojis.value.length > 12) {
    recentEmojis.value.pop();
  }
  
  // 保存到本地存储
  saveRecentEmojis();
};

// 保存最近使用的表情到本地存储
const saveRecentEmojis = () => {
  try {
    localStorage.setItem('recentEmojis', JSON.stringify(recentEmojis.value));
  } catch (error) {
    console.error('保存最近使用表情失败:', error);
  }
};

// 从本地存储加载最近使用的表情
const loadRecentEmojis = () => {
  try {
    const saved = localStorage.getItem('recentEmojis');
    if (saved) {
      recentEmojis.value = JSON.parse(saved);
    }
  } catch (error) {
    console.error('加载最近使用表情失败:', error);
    recentEmojis.value = [];
  }
};

// 组件挂载时加载最近使用的表情
onMounted(() => {
  loadRecentEmojis();
});
</script>

<style scoped>
.emoji-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.emoji-trigger:hover {
  background-color: var(--el-color-primary-light-9);
}

.emoji-picker {
  width: 100%;
  max-height: 300px;
}

.emoji-categories {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-light);
  overflow-x: auto;
}

.emoji-categories .el-button {
  min-width: auto;
  padding: 4px 8px;
  font-size: 16px;
}

.emoji-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background-color: var(--el-color-primary-light-9);
}

.recent-emojis {
  border-top: 1px solid var(--el-border-color-light);
  padding-top: 8px;
}

.recent-title {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.recent-list {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.recent-list .emoji-item {
  width: 28px;
  height: 28px;
  font-size: 18px;
}
</style>