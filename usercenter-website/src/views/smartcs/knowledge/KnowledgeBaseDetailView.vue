<template>
  <div class="knowledge-base-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="kb-info">
          <h2>{{ knowledgeBaseData?.name || '加载中...' }}</h2>
          <div class="kb-meta">
            <span class="meta-item">{{ knowledgeBaseData?.description || 'useful for when you want to answer queries about the product information' }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="文档" name="documents">
          <el-icon><Document /></el-icon>
        </el-tab-pane>
        <el-tab-pane label="召回测试" name="recall-test">
          <el-icon><Search /></el-icon>
        </el-tab-pane>
        <el-tab-pane label="设置" name="settings">
          <el-icon><Setting /></el-icon>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Tab 内容 -->
    <div class="tab-content" v-loading="loading">
      <!-- 文档列表 -->
      <div v-show="activeTab === 'documents'" class="documents-content">
        <div class="content-header">
          <h3>文档管理</h3>
          <el-button type="primary" @click="handleAddDocument">
            <el-icon><Plus /></el-icon>
            添加文档
          </el-button>
        </div>
        
        <!-- 这里可以嵌入ContentListView或者重新实现文档列表 -->
        <div class="documents-list">
          <el-empty 
            v-if="documentCount === 0"
            description="暂无文档" 
            :image-size="80"
          >
            <el-button type="primary" @click="handleAddDocument">
              添加第一个文档
            </el-button>
          </el-empty>
          
          <div v-else class="document-stats">
            <div class="stats-card">
              <div class="stats-value">{{ documentCount }}</div>
              <div class="stats-label">文档总数</div>
            </div>
            <div class="stats-card">
              <div class="stats-value">{{ formatNumber(charCount) }}</div>
              <div class="stats-label">字符总数</div>
            </div>
            <div class="stats-card">
              <div class="stats-value">{{ chunkCount }}</div>
              <div class="stats-label">分块总数</div>
            </div>
          </div>
          
          <div class="view-documents-action">
            <el-button @click="handleViewDocuments">
              查看文档列表
            </el-button>
          </div>
        </div>
      </div>

      <!-- 召回测试 -->
      <div v-show="activeTab === 'recall-test'" class="recall-test-content">
        <RecallTestPanel 
          :knowledge-base-id="knowledgeBaseId"
          ref="recallTestRef"
        />
      </div>

      <!-- 设置 -->
      <div v-show="activeTab === 'settings'" class="settings-content">
        <KnowledgeBaseSettings 
          :knowledge-base-id="knowledgeBaseId"
          ref="settingsRef"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Refresh,
  Document,
  Search,
  Setting,
  Plus
} from '@element-plus/icons-vue'
import { 
  getKnowledgeBase,
  type KnowledgeBaseDTO
} from '@/api/smartcs/knowledgeBase'
import RecallTestPanel from './components/RecallTestPanel.vue'
import KnowledgeBaseSettings from './components/KnowledgeBaseSettings.vue'

const route = useRoute()
const router = useRouter()

// 路由参数
const knowledgeBaseId = computed(() => Number(route.params.id))

// 状态管理
const loading = ref(true)
const activeTab = ref('documents')
const knowledgeBaseData = ref<KnowledgeBaseDTO | null>(null)

// Mock数据 - 实际项目中应该从API获取
const documentCount = ref(0)
const charCount = ref(0)
const chunkCount = ref(0)

// 组件引用
const recallTestRef = ref()
const settingsRef = ref()

// 获取知识库详情
const fetchKnowledgeBaseDetail = async () => {
  try {
    loading.value = true
    const response = await getKnowledgeBase(knowledgeBaseId.value)
    
    if (response.success && response.data) {
      knowledgeBaseData.value = response.data
      
      // 这里应该从API获取文档统计信息
      // 暂时使用Mock数据
      documentCount.value = response.data.documentCount || 2
      charCount.value = response.data.charCount || 12347
      chunkCount.value = response.data.chunkCount || 27
    } else {
      throw new Error(response.errMessage || '获取知识库详情失败')
    }
  } catch (error: any) {
    console.error('获取知识库详情失败:', error)
    ElMessage.error(error.message || '获取知识库详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchKnowledgeBaseDetail()
  
  // 刷新当前Tab的数据
  if (activeTab.value === 'recall-test' && recallTestRef.value) {
    recallTestRef.value.resetTest?.()
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理Tab切换
const handleTabChange = (tabName: string) => {
  console.log('切换到Tab:', tabName)
}

// 添加文档
const handleAddDocument = () => {
  // 跳转到文档上传页面或打开上传对话框
  ElMessage.info('添加文档功能开发中...')
}

// 查看文档列表
const handleViewDocuments = () => {
  router.push({
    name: 'ContentList',
    params: {
      knowledgeBaseId: knowledgeBaseId.value
    }
  })
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

// 监听路由参数变化
watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchKnowledgeBaseDetail()
  }
}, { immediate: true })

onMounted(() => {
  fetchKnowledgeBaseDetail()
})
</script>

<style scoped lang="scss">
.knowledge-base-detail {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: white;
    border-bottom: 1px solid #e5e7eb;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .kb-info {
        h2 {
          margin: 0 0 4px 0;
          font-size: 20px;
          font-weight: 600;
          color: #1f2937;
        }

        .kb-meta {
          font-size: 14px;
          color: #6b7280;
          max-width: 600px;
          line-height: 1.4;
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .tabs-container {
    background: white;
    border-bottom: 1px solid #e5e7eb;

    :deep(.el-tabs) {
      .el-tabs__header {
        margin: 0;
        padding: 0 24px;
      }

      .el-tabs__nav-wrap::after {
        display: none;
      }

      .el-tabs__item {
        display: flex;
        align-items: center;
        gap: 8px;
        height: 48px;
        line-height: 48px;
        font-size: 14px;
        font-weight: 500;

        .el-icon {
          font-size: 16px;
        }

        &.is-active {
          color: #409eff;
        }
      }

      .el-tabs__active-bar {
        background: #409eff;
      }
    }
  }

  .tab-content {
    flex: 1;
    min-height: 0;

    .documents-content,
    .recall-test-content,
    .settings-content {
      height: 100%;
      background: white;
      margin: 24px;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .documents-content {
      padding: 32px;

      .content-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 32px;
        padding-bottom: 16px;
        border-bottom: 2px solid #f0f2f5;

        h3 {
          margin: 0;
          font-size: 20px;
          font-weight: 700;
          color: #1f2937;
          display: flex;
          align-items: center;
          gap: 8px;
          
          &::before {
            content: '📚';
            font-size: 22px;
          }
        }
        
        .el-button {
          border-radius: 8px;
          font-weight: 600;
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
          }
        }
      }

      .documents-list {
        .document-stats {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
          gap: 24px;
          margin-bottom: 32px;

          .stats-card {
            padding: 24px;
            background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
            border-radius: 12px;
            text-align: center;
            border: 1px solid #e2e8f0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
            
            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              height: 3px;
              background: linear-gradient(90deg, #409eff 0%, #36cfc9 100%);
            }
            
            &:hover {
              box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
              transform: translateY(-2px);
            }

            .stats-value {
              font-size: 28px;
              font-weight: 700;
              color: #1f2937;
              margin-bottom: 12px;
              background: linear-gradient(135deg, #409eff 0%, #36cfc9 100%);
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
              background-clip: text;
            }

            .stats-label {
              font-size: 14px;
              color: #6b7280;
              font-weight: 600;
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }
          }
        }

        .view-documents-action {
          text-align: center;
          margin-top: 32px;
          
          .el-button {
            border-radius: 8px;
            font-weight: 600;
            padding: 12px 24px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
            
            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }
          }
        }
      }
    }

    .recall-test-content,
    .settings-content {
      padding: 0;
      overflow: auto;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .knowledge-base-detail {
    .page-header {
      padding: 12px 16px;

      .header-left {
        gap: 12px;

        .kb-info {
          h2 {
            font-size: 18px;
          }

          .kb-meta {
            font-size: 13px;
          }
        }
      }
    }

    .tabs-container {
      :deep(.el-tabs) {
        .el-tabs__header {
          padding: 0 16px;
        }
      }
    }

    .tab-content {
      margin: 16px;

      .documents-content {
        padding: 16px;

        .document-stats {
          grid-template-columns: 1fr;
          gap: 16px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .knowledge-base-detail {
    .page-header {
      .header-left {
        .kb-info {
          .kb-meta {
            display: none;
          }
        }
      }

      .header-actions {
        gap: 8px;

        .el-button {
          padding: 8px 12px;
        }
      }
    }

    .tab-content {
      margin: 12px;

      .documents-content {
        padding: 12px;
      }
    }
  }
}
</style>