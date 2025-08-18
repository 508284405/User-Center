<template>
  <div class="moderation-layout">
    <div class="moderation-sidebar">
      <div class="sidebar-header">
        <h3>
          <el-icon><Lock /></el-icon>
          审核管理
        </h3>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :router="true"
        class="moderation-menu"
      >
        <el-menu-item index="/dashboard/moderation/review">
          <el-icon><UserFilled /></el-icon>
          <span>人工审核处理</span>
          <el-badge 
            v-if="pendingCount > 0" 
            :value="pendingCount" 
            :max="99" 
            class="menu-badge"
          />
        </el-menu-item>
        
        <el-menu-item index="/dashboard/moderation/records">
          <el-icon><Document /></el-icon>
          <span>审核记录管理</span>
        </el-menu-item>
        
        <el-menu-item index="/dashboard/moderation/categories">
          <el-icon><Menu /></el-icon>
          <span>违规分类管理</span>
        </el-menu-item>
        
        <el-menu-item index="/dashboard/moderation/rules">
          <el-icon><Key /></el-icon>
          <span>关键词规则管理</span>
        </el-menu-item>
        
        <el-menu-item index="/dashboard/moderation/config">
          <el-icon><Setting /></el-icon>
          <span>审核配置管理</span>
        </el-menu-item>
        
        <el-menu-item index="/dashboard/moderation/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>审核统计报表</span>
        </el-menu-item>
      </el-menu>
      
      <!-- 快速操作 -->
      <div class="quick-actions">
        <div class="actions-header">快速操作</div>
        <el-button 
          type="primary" 
          size="small" 
          @click="goToReview"
          :disabled="pendingCount === 0"
          class="action-button"
        >
          <el-icon><Check /></el-icon>
          处理待审核 ({{ pendingCount }})
        </el-button>
        
        <el-button 
          type="success" 
          size="small" 
          @click="refreshStats"
          :loading="refreshing"
          class="action-button"
        >
          <el-icon><Refresh /></el-icon>
          刷新统计
        </el-button>
        
        <el-button 
          type="info" 
          size="small" 
          @click="exportReport"
          class="action-button"
        >
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
      
      <!-- 实时统计 -->
      <div class="real-time-stats">
        <div class="stats-header">实时统计</div>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ stats.todayTotal }}</div>
            <div class="stat-label">今日总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value text-success">{{ stats.todayApproved }}</div>
            <div class="stat-label">今日通过</div>
          </div>
          <div class="stat-item">
            <div class="stat-value text-danger">{{ stats.todayRejected }}</div>
            <div class="stat-label">今日拒绝</div>
          </div>
          <div class="stat-item">
            <div class="stat-value text-warning">{{ stats.pendingReview }}</div>
            <div class="stat-label">待人工审核</div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="moderation-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Lock, UserFilled, Document, Menu, Key, Setting, DataAnalysis,
  Check, Refresh, Download
} from '@element-plus/icons-vue'
import moderationApi from '@/api/smartcs/moderation'

// 响应式数据
const route = useRoute()
const router = useRouter()
const refreshing = ref(false)
const pendingCount = ref(0)

// 实时统计数据
const stats = reactive({
  todayTotal: 0,
  todayApproved: 0,
  todayRejected: 0,
  pendingReview: 0
})

// 计算属性
const activeMenu = computed(() => {
  return route.path
})

// 定时器
let statsTimer: NodeJS.Timeout | null = null

// 生命周期
onMounted(() => {
  loadRealTimeStats()
  // 每30秒刷新一次统计数据
  statsTimer = setInterval(() => {
    loadRealTimeStats()
  }, 30000)
})

onUnmounted(() => {
  if (statsTimer) {
    clearInterval(statsTimer)
  }
})

// 方法定义
const loadRealTimeStats = async () => {
  try {
    // 加载待审核数量
    const pendingResponse = await moderationApi.getPendingReviews(1)
    pendingCount.value = pendingResponse.data?.length || 0
    
    // 加载今日统计数据（模拟）
    Object.assign(stats, {
      todayTotal: 1240,
      todayApproved: 956,
      todayRejected: 186,
      pendingReview: pendingCount.value
    })
  } catch (error) {
    console.error('Failed to load real-time stats:', error)
  }
}

const refreshStats = async () => {
  refreshing.value = true
  try {
    await loadRealTimeStats()
    ElMessage.success('统计数据已刷新')
  } catch (error) {
    console.error('Failed to refresh stats:', error)
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

const goToReview = () => {
  router.push('/dashboard/moderation/review')
}

const exportReport = () => {
  ElMessage.info('报告导出功能正在开发中')
}
</script>

<style scoped lang="scss">
.moderation-layout {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;

  .moderation-sidebar {
    width: 280px;
    background: white;
    border-right: 1px solid #e4e7ed;
    overflow-y: auto;
    flex-shrink: 0;

    .sidebar-header {
      padding: 20px;
      border-bottom: 1px solid #e4e7ed;

      h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        color: #303133;
        font-size: 18px;

        .el-icon {
          font-size: 20px;
          color: #409eff;
        }
      }
    }

    .moderation-menu {
      border: none;

      .el-menu-item {
        position: relative;

        .menu-badge {
          position: absolute;
          top: 10px;
          right: 15px;
        }
      }
    }

    .quick-actions {
      padding: 20px;
      border-top: 1px solid #e4e7ed;

      .actions-header {
        font-size: 14px;
        font-weight: 500;
        color: #606266;
        margin-bottom: 12px;
      }

      .action-button {
        width: 100%;
        margin-bottom: 8px;
        justify-content: flex-start;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }

    .real-time-stats {
      padding: 20px;
      border-top: 1px solid #e4e7ed;

      .stats-header {
        font-size: 14px;
        font-weight: 500;
        color: #606266;
        margin-bottom: 12px;
      }

      .stats-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;

        .stat-item {
          text-align: center;
          padding: 8px;
          background-color: #f8f9fa;
          border-radius: 4px;

          .stat-value {
            font-size: 18px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;

            &.text-success {
              color: #67c23a;
            }

            &.text-danger {
              color: #f56565;
            }

            &.text-warning {
              color: #e6a23c;
            }
          }

          .stat-label {
            font-size: 11px;
            color: #909399;
          }
        }
      }
    }
  }

  .moderation-content {
    flex: 1;
    overflow: hidden;
    background-color: #f5f7fa;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .moderation-layout {
    .moderation-sidebar {
      width: 240px;

      .quick-actions,
      .real-time-stats {
        padding: 16px;
      }
    }
  }
}

@media (max-width: 768px) {
  .moderation-layout {
    flex-direction: column;

    .moderation-sidebar {
      width: 100%;
      height: auto;
      border-right: none;
      border-bottom: 1px solid #e4e7ed;

      .moderation-menu {
        display: flex;
        overflow-x: auto;

        .el-menu-item {
          white-space: nowrap;
          min-width: 120px;
        }
      }

      .quick-actions,
      .real-time-stats {
        display: none;
      }
    }

    .moderation-content {
      height: calc(100vh - 120px);
    }
  }
}
</style>