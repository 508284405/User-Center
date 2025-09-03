<template>
  <el-dialog
    v-model="dialogVisible"
    title="搜索消息"
    width="800px"
    :append-to-body="true"
    class="message-search-dialog"
  >
    <div class="search-container">
      <!-- 搜索区域 -->
      <div class="search-header">
        <div class="search-input-area">
          <el-input
            v-model="searchKeyword"
            placeholder="输入关键词搜索消息..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleClear"
            size="large"
          >
            <template #append>
              <el-button 
                :icon="Search" 
                @click="handleSearch"
                :loading="searching"
              >
                搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <!-- 高级筛选 -->
        <div class="advanced-filters">
          <el-collapse>
            <el-collapse-item title="高级筛选" name="filters">
              <el-form :model="searchFilters" size="small" :inline="true">
                <el-form-item label="消息类型">
                  <el-select v-model="searchFilters.messageType" placeholder="全部" clearable>
                    <el-option label="全部" value="" />
                    <el-option label="文本消息" value="text" />
                    <el-option label="图片消息" value="image" />
                    <el-option label="文件消息" value="file" />
                    <el-option label="语音消息" value="audio" />
                  </el-select>
                </el-form-item>

                <el-form-item label="时间范围">
                  <el-date-picker
                    v-model="dateRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY-MM-DD HH:mm"
                    value-format="x"
                    clearable
                  />
                </el-form-item>

                <el-form-item>
                  <el-button @click="resetFilters">重置</el-button>
                </el-form-item>
              </el-form>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results">
        <!-- 搜索状态 -->
        <div v-if="searching" class="search-status">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 空状态 -->
        <div v-else-if="!hasSearched && !searchResults.length" class="empty-state">
          <el-icon size="64" color="#c0c4cc"><Search /></el-icon>
          <p>输入关键词开始搜索消息</p>
          <div class="search-tips">
            <p>搜索技巧:</p>
            <ul>
              <li>支持消息内容关键词搜索</li>
              <li>可按消息类型筛选结果</li>
              <li>可设置时间范围精确查找</li>
              <li>点击搜索结果可跳转到原消息</li>
            </ul>
          </div>
        </div>

        <!-- 无结果 -->
        <div v-else-if="hasSearched && !searchResults.length" class="no-results">
          <el-icon size="64" color="#c0c4cc"><DocumentDelete /></el-icon>
          <p>未找到相关消息</p>
          <p class="tips">尝试更换关键词或调整筛选条件</p>
        </div>

        <!-- 搜索结果列表 -->
        <div v-else class="results-list">
          <div class="results-header">
            <span class="results-count">
              找到 {{ totalResults }} 条相关消息
            </span>
            <div class="sort-options">
              <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
                <el-radio-button label="time">按时间</el-radio-button>
                <el-radio-button label="relevance">按相关性</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <div class="results-content">
            <div
              v-for="result in searchResults"
              :key="result.msgId"
              class="result-item"
              @click="handleJumpToMessage(result)"
            >
              <div class="result-content">
                <div class="message-text" v-html="highlightKeyword(result.content)"></div>
                <div class="message-meta">
                  <span class="sender-name" v-if="result.senderName">{{ result.senderName }}</span>
                  <span class="message-time">{{ formatTime(result.createdAt) }}</span>
                  <span class="message-type">{{ getMessageTypeText(result.messageType) }}</span>
                </div>
              </div>
              <div class="result-actions">
                <el-button
                  type="text"
                  size="small"
                  @click.stop="handleJumpToMessage(result)"
                >
                  跳转
                </el-button>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-wrapper" v-if="totalResults > searchFilters.size">
              <el-pagination
                v-model:current-page="searchFilters.page"
                :page-size="searchFilters.size"
                :total="totalResults"
                layout="prev, pager, next, jumper"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, DocumentDelete } from '@element-plus/icons-vue';
import {
  searchMessages,
  type MessageSearchResult,
  type SearchOptions
} from '@/api/smartcs/conversation';

// Props
interface Props {
  modelValue: boolean;
  userId: string;
  sessionId: number;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'jump-to-message': [msgId: string];
}>();

// 响应式数据
const searching = ref(false);
const hasSearched = ref(false);
const searchKeyword = ref('');
const searchResults = ref<MessageSearchResult[]>([]);
const totalResults = ref(0);
const sortBy = ref<'time' | 'relevance'>('time');
const dateRange = ref<[number, number] | null>(null);

const searchFilters = ref({
  messageType: '',
  page: 1,
  size: 20
});

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const handleSearch = async (resetPage = true) => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }

  if (resetPage) {
    searchFilters.value.page = 1;
  }

  searching.value = true;
  hasSearched.value = true;

  try {
    const searchOptions: SearchOptions = {
      keyword: searchKeyword.value.trim(),
      userId: props.userId,
      sessionId: props.sessionId,
      messageType: searchFilters.value.messageType || undefined,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1],
      page: searchFilters.value.page,
      size: searchFilters.value.size
    };

    const response = await searchMessages(searchOptions);

    if (response.success && response.data) {
      searchResults.value = response.data.results;
      totalResults.value = response.data.total;
      
      if (response.data.results.length === 0 && resetPage) {
        ElMessage.info('未找到相关消息');
      }
    } else {
      ElMessage.error(response.errMessage || '搜索失败');
      searchResults.value = [];
      totalResults.value = 0;
    }
  } catch (error) {
    console.error('搜索消息失败:', error);
    ElMessage.error('搜索失败');
    searchResults.value = [];
    totalResults.value = 0;
  } finally {
    searching.value = false;
  }
};

const handleClear = () => {
  searchKeyword.value = '';
  searchResults.value = [];
  totalResults.value = 0;
  hasSearched.value = false;
};

const resetFilters = () => {
  searchFilters.value = {
    messageType: '',
    page: 1,
    size: 20
  };
  dateRange.value = null;
  sortBy.value = 'time';
  
  if (hasSearched.value) {
    handleSearch();
  }
};

const handleSortChange = () => {
  if (hasSearched.value) {
    handleSearch();
  }
};

const handlePageChange = (page: number) => {
  searchFilters.value.page = page;
  handleSearch(false);
};

const handleJumpToMessage = (result: MessageSearchResult) => {
  emit('jump-to-message', result.msgId);
  dialogVisible.value = false;
};

const highlightKeyword = (content: string) => {
  if (!searchKeyword.value.trim()) {
    return content;
  }

  const keyword = searchKeyword.value.trim();
  const regex = new RegExp(`(${keyword})`, 'gi');
  
  return content.replace(regex, '<mark class="highlight">$1</mark>');
};

const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    // 今天
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    });
  } else if (diffDays === 1) {
    // 昨天
    return `昨天 ${date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })}`;
  } else if (diffDays < 7) {
    // 本周内
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return `${weekdays[date.getDay()]} ${date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })}`;
  } else {
    // 更早
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
};

const getMessageTypeText = (messageType: string) => {
  const typeMap: Record<string, string> = {
    'text': '文本',
    'image': '图片',
    'file': '文件',
    'audio': '语音',
    'video': '视频'
  };
  return typeMap[messageType] || '消息';
};

// 监听对话框关闭
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    // 重置搜索状态
    searchKeyword.value = '';
    searchResults.value = [];
    totalResults.value = 0;
    hasSearched.value = false;
    resetFilters();
  }
});
</script>

<style scoped lang="scss">
.message-search-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.search-container {
  .search-header {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;

    .search-input-area {
      margin-bottom: 16px;
    }

    .advanced-filters {
      :deep(.el-collapse-item__header) {
        font-size: 14px;
        color: #606266;
      }

      :deep(.el-collapse-item__content) {
        padding-bottom: 0;
      }
    }
  }

  .search-results {
    min-height: 400px;
    max-height: 600px;

    .search-status {
      padding: 20px;
    }

    .empty-state,
    .no-results {
      text-align: center;
      padding: 60px 20px;

      p {
        margin: 16px 0;
        color: #909399;
        font-size: 16px;
      }

      .tips {
        font-size: 14px;
        color: #c0c4cc;
      }

      .search-tips {
        margin-top: 24px;
        text-align: left;
        max-width: 300px;
        margin-left: auto;
        margin-right: auto;

        p {
          font-size: 14px;
          color: #606266;
          margin-bottom: 8px;
        }

        ul {
          font-size: 13px;
          color: #909399;
          
          li {
            margin-bottom: 4px;
          }
        }
      }
    }

    .results-list {
      .results-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid #f0f2f5;
        background: #fafafa;

        .results-count {
          font-size: 14px;
          color: #606266;
        }
      }

      .results-content {
        .result-item {
          display: flex;
          align-items: center;
          padding: 16px 20px;
          border-bottom: 1px solid #f5f7fa;
          cursor: pointer;
          transition: background-color 0.2s;

          &:hover {
            background-color: #f5f7fa;
          }

          .result-content {
            flex: 1;
            min-width: 0;

            .message-text {
              margin-bottom: 8px;
              line-height: 1.5;
              word-break: break-word;

              :deep(.highlight) {
                background-color: #fffbe6;
                color: #d48806;
                padding: 2px 4px;
                border-radius: 2px;
              }
            }

            .message-meta {
              display: flex;
              align-items: center;
              gap: 12px;
              font-size: 12px;
              color: #909399;

              .sender-name {
                color: #606266;
                font-weight: 500;
              }
            }
          }

          .result-actions {
            margin-left: 16px;
          }
        }

        .pagination-wrapper {
          padding: 20px;
          text-align: center;
        }
      }
    }
  }
}
</style>