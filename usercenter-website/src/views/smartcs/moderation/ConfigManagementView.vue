<template>
  <div class="config-management">
    <div class="header-section">
      <h2>审核配置管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="batchSave" :loading="saving">
          <el-icon><Check /></el-icon>
          批量保存
        </el-button>
        <el-button @click="refreshConfigs">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="warning" @click="resetToDefaults">
          <el-icon><RefreshLeft /></el-icon>
          重置默认
        </el-button>
      </div>
    </div>

    <!-- 配置分类标签 -->
    <el-card class="category-tabs-card">
      <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange" class="config-tabs">
        <el-tab-pane label="通用配置" name="GENERAL">
          <template #label>
            <span class="tab-label">
              <el-icon><Setting /></el-icon>
              通用配置
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="AI模型" name="AI_MODEL">
          <template #label>
            <span class="tab-label">
              <el-icon><Cpu /></el-icon>
              AI模型
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="关键词过滤" name="KEYWORD_FILTER">
          <template #label>
            <span class="tab-label">
              <el-icon><Filter /></el-icon>
              关键词过滤
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="阈值设置" name="THRESHOLD">
          <template #label>
            <span class="tab-label">
              <el-icon><DataAnalysis /></el-icon>
              阈值设置
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="缓存配置" name="CACHE">
          <template #label>
            <span class="tab-label">
              <el-icon><Coin /></el-icon>
              缓存配置
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 配置列表 -->
    <el-card class="configs-card">
      <template #header>
        <div class="configs-header">
          <span>{{ getCategoryTitle(activeCategory) }}</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索配置项"
              size="small"
              style="width: 200px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="configs-list">
        <div
          v-for="config in filteredConfigs"
          :key="config.configKey"
          class="config-item"
          :class="{ 'modified': isModified(config) }"
        >
          <div class="config-header">
            <div class="config-info">
              <div class="config-name">
                {{ config.configName }}
                <el-tag v-if="config.isSystem" type="info" size="small">系统</el-tag>
                <el-tag v-if="!config.isActive" type="danger" size="small">禁用</el-tag>
              </div>
              <div class="config-key">{{ config.configKey }}</div>
            </div>
            <div class="config-actions">
              <el-button
                v-if="isModified(config)"
                type="text"
                size="small"
                @click="saveConfig(config)"
                :loading="config.saving"
              >
                <el-icon><Check /></el-icon>
                保存
              </el-button>
              <el-button
                v-if="isModified(config)"
                type="text"
                size="small"
                @click="resetConfig(config)"
              >
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="resetToDefault(config)"
                :disabled="!config.defaultValue"
              >
                <el-icon><Refresh /></el-icon>
                默认值
              </el-button>
            </div>
          </div>

          <div class="config-content">
            <div class="config-description" v-if="config.description">
              {{ config.description }}
            </div>

            <div class="config-editor">
              <!-- 布尔类型 -->
              <el-switch
                v-if="config.configType === 'BOOLEAN'"
                v-model="config.editValue"
                :true-value="'true'"
                :false-value="'false'"
                active-text="开启"
                inactive-text="关闭"
                :disabled="!config.isActive || config.isSystem"
              />

              <!-- 整数类型 -->
              <el-input-number
                v-else-if="config.configType === 'INTEGER'"
                v-model="config.editValue"
                :min="getValidationMin(config)"
                :max="getValidationMax(config)"
                :step="1"
                :disabled="!config.isActive || config.isSystem"
                style="width: 200px"
              />

              <!-- 小数类型 -->
              <el-input-number
                v-else-if="config.configType === 'DECIMAL'"
                v-model="config.editValue"
                :min="getValidationMin(config)"
                :max="getValidationMax(config)"
                :step="0.1"
                :precision="2"
                :disabled="!config.isActive || config.isSystem"
                style="width: 200px"
              />

              <!-- JSON类型 -->
              <div v-else-if="config.configType === 'JSON'" class="json-editor">
                <el-input
                  v-model="config.editValue"
                  type="textarea"
                  :rows="6"
                  :disabled="!config.isActive || config.isSystem"
                  placeholder="请输入有效的JSON格式"
                />
                <div class="json-actions">
                  <el-button type="text" size="small" @click="formatJSON(config)">
                    <el-icon><Star /></el-icon>
                    格式化
                  </el-button>
                  <el-button type="text" size="small" @click="validateJSON(config)">
                    <el-icon><Check /></el-icon>
                    验证
                  </el-button>
                </div>
              </div>

              <!-- 字符串类型 -->
              <el-input
                v-else
                v-model="config.editValue"
                :disabled="!config.isActive || config.isSystem"
                :placeholder="getPlaceholder(config)"
                style="width: 400px"
                maxlength="500"
                show-word-limit
              />
            </div>

            <div class="config-meta">
              <div class="current-value">
                <span class="label">当前值:</span>
                <code class="value">{{ config.configValue || '(空)' }}</code>
              </div>
              <div class="default-value" v-if="config.defaultValue">
                <span class="label">默认值:</span>
                <code class="value">{{ config.defaultValue }}</code>
              </div>
              <div class="validation-rule" v-if="config.validationRule">
                <span class="label">验证规则:</span>
                <code class="rule">{{ config.validationRule }}</code>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="filteredConfigs.length === 0" description="暂无配置项" />
      </div>
    </el-card>

    <!-- 配置统计 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalConfigs }}</div>
            <div class="stat-label">总配置数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.modifiedConfigs }}</div>
            <div class="stat-label">已修改</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.systemConfigs }}</div>
            <div class="stat-label">系统配置</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.activeConfigs }}</div>
            <div class="stat-label">启用配置</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Check, Refresh, RefreshLeft, Setting, Cpu, Filter, DataAnalysis, 
  Coin, Search, Star 
} from '@element-plus/icons-vue'
import moderationApi, { type ModerationConfig } from '@/api/smartcs/moderation'

// 扩展配置接口以支持编辑
interface EditableConfig extends ModerationConfig {
  editValue?: any
  saving?: boolean
  originalValue?: string
}

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const activeCategory = ref('GENERAL')
const searchKeyword = ref('')
const configs = ref<EditableConfig[]>([])

// 统计数据
const stats = reactive({
  totalConfigs: 0,
  modifiedConfigs: 0,
  systemConfigs: 0,
  activeConfigs: 0
})

// 计算属性
const filteredConfigs = computed(() => {
  let filtered = configs.value.filter(config => config.category === activeCategory.value)
  
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(config => 
      config.configName.toLowerCase().includes(keyword) ||
      config.configKey.toLowerCase().includes(keyword) ||
      config.description?.toLowerCase().includes(keyword)
    )
  }
  
  return filtered.sort((a, b) => {
    // 系统配置排在前面
    if (a.isSystem !== b.isSystem) return a.isSystem ? -1 : 1
    // 按配置名称排序
    return a.configName.localeCompare(b.configName)
  })
})

// 监听器
watch(
  () => configs.value,
  () => {
    calculateStats()
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  loadConfigs()
})

// 方法定义
const loadConfigs = async () => {
  loading.value = true
  try {
    const response = await moderationApi.getConfigs()
    configs.value = (response.data || []).map(config => ({
      ...config,
      editValue: parseConfigValue(config),
      originalValue: config.configValue
    }))
  } catch (error) {
    console.error('Failed to load configs:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const refreshConfigs = () => {
  loadConfigs()
}

const calculateStats = () => {
  stats.totalConfigs = configs.value.length
  stats.modifiedConfigs = configs.value.filter(isModified).length
  stats.systemConfigs = configs.value.filter(config => config.isSystem).length
  stats.activeConfigs = configs.value.filter(config => config.isActive).length
}

const handleCategoryChange = () => {
  // 切换分类时可以做一些清理工作
}

const parseConfigValue = (config: ModerationConfig) => {
  const value = config.configValue
  if (!value) return getDefaultValueByType(config.configType)
  
  switch (config.configType) {
    case 'BOOLEAN':
      return value
    case 'INTEGER':
      return parseInt(value) || 0
    case 'DECIMAL':
      return parseFloat(value) || 0
    case 'JSON':
      try {
        return JSON.stringify(JSON.parse(value), null, 2)
      } catch {
        return value
      }
    default:
      return value
  }
}

const getDefaultValueByType = (type: string) => {
  switch (type) {
    case 'BOOLEAN': return 'false'
    case 'INTEGER': return 0
    case 'DECIMAL': return 0.0
    case 'JSON': return '{}'
    default: return ''
  }
}

const isModified = (config: EditableConfig) => {
  const currentValue = formatConfigValue(config, config.editValue)
  return currentValue !== config.originalValue
}

const formatConfigValue = (config: EditableConfig, value: any) => {
  if (value === null || value === undefined) return ''
  
  switch (config.configType) {
    case 'BOOLEAN':
      return String(value)
    case 'INTEGER':
    case 'DECIMAL':
      return String(value)
    case 'JSON':
      try {
        // 压缩JSON格式
        return JSON.stringify(JSON.parse(value))
      } catch {
        return String(value)
      }
    default:
      return String(value)
  }
}

const saveConfig = async (config: EditableConfig) => {
  const newValue = formatConfigValue(config, config.editValue)
  
  // 验证配置值
  if (!validateConfigValue(config, newValue)) {
    return
  }
  
  config.saving = true
  try {
    await moderationApi.updateConfig(config.configKey, newValue)
    config.configValue = newValue
    config.originalValue = newValue
    ElMessage.success('配置保存成功')
  } catch (error: any) {
    console.error('Save config failed:', error)
    ElMessage.error('保存失败: ' + (error.response?.data?.message || '未知错误'))
  } finally {
    config.saving = false
  }
}

const resetConfig = (config: EditableConfig) => {
  config.editValue = parseConfigValue(config)
}

const resetToDefault = async (config: EditableConfig) => {
  if (!config.defaultValue) {
    ElMessage.warning('该配置项没有默认值')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认将 "${config.configName}" 重置为默认值吗？`,
      '重置确认',
      { type: 'warning' }
    )
    
    await moderationApi.resetConfig(config.configKey)
    await loadConfigs() // 重新加载配置
    ElMessage.success('重置成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Reset config failed:', error)
      ElMessage.error('重置失败')
    }
  }
}

const batchSave = async () => {
  const modifiedConfigs = configs.value.filter(isModified)
  
  if (modifiedConfigs.length === 0) {
    ElMessage.warning('没有需要保存的配置修改')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认保存 ${modifiedConfigs.length} 项配置修改吗？`,
      '批量保存确认',
      { type: 'warning' }
    )
    
    saving.value = true
    
    const updates = modifiedConfigs.map(config => ({
      configKey: config.configKey,
      configValue: formatConfigValue(config, config.editValue)
    }))
    
    await moderationApi.batchUpdateConfigs(updates)
    
    // 更新本地状态
    modifiedConfigs.forEach(config => {
      const newValue = formatConfigValue(config, config.editValue)
      config.configValue = newValue
      config.originalValue = newValue
    })
    
    ElMessage.success('批量保存成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Batch save failed:', error)
      ElMessage.error('批量保存失败')
    }
  } finally {
    saving.value = false
  }
}

const resetToDefaults = async () => {
  try {
    await ElMessageBox.confirm(
      '确认将所有配置重置为默认值吗？此操作不可撤销！',
      '重置警告',
      { type: 'error' }
    )
    
    // 这里应该调用重置所有配置的API
    ElMessage.info('重置功能正在开发中')
  } catch (error) {
    // 用户取消操作
  }
}

const validateConfigValue = (config: EditableConfig, value: string) => {
  // 基本类型验证
  switch (config.configType) {
    case 'INTEGER':
      if (!/^\d+$/.test(value)) {
        ElMessage.error('请输入有效的整数')
        return false
      }
      break
    case 'DECIMAL':
      if (!/^\d+(\.\d+)?$/.test(value)) {
        ElMessage.error('请输入有效的数字')
        return false
      }
      break
    case 'JSON':
      try {
        JSON.parse(value)
      } catch {
        ElMessage.error('请输入有效的JSON格式')
        return false
      }
      break
  }
  
  // 自定义验证规则
  if (config.validationRule) {
    try {
      const regex = new RegExp(config.validationRule)
      if (!regex.test(value)) {
        ElMessage.error('配置值不符合验证规则')
        return false
      }
    } catch {
      console.warn('Invalid validation rule:', config.validationRule)
    }
  }
  
  return true
}

const formatJSON = (config: EditableConfig) => {
  try {
    const parsed = JSON.parse(config.editValue)
    config.editValue = JSON.stringify(parsed, null, 2)
    ElMessage.success('JSON格式化成功')
  } catch {
    ElMessage.error('JSON格式无效，无法格式化')
  }
}

const validateJSON = (config: EditableConfig) => {
  try {
    JSON.parse(config.editValue)
    ElMessage.success('JSON格式验证通过')
  } catch (error: any) {
    ElMessage.error('JSON格式错误: ' + error.message)
  }
}

const getValidationMin = (config: EditableConfig) => {
  if (!config.validationRule) return undefined
  const match = config.validationRule.match(/min:(\d+)/)
  return match ? parseInt(match[1]) : undefined
}

const getValidationMax = (config: EditableConfig) => {
  if (!config.validationRule) return undefined
  const match = config.validationRule.match(/max:(\d+)/)
  return match ? parseInt(match[1]) : undefined
}

const getPlaceholder = (config: EditableConfig) => {
  if (config.defaultValue) {
    return `默认值: ${config.defaultValue}`
  }
  return '请输入配置值'
}

const getCategoryTitle = (category: string) => {
  const titles: Record<string, string> = {
    GENERAL: '通用配置',
    AI_MODEL: 'AI模型配置',
    KEYWORD_FILTER: '关键词过滤配置',
    THRESHOLD: '阈值设置',
    CACHE: '缓存配置'
  }
  return titles[category] || category
}
</script>

<style scoped lang="scss">
.config-management {
  padding: 20px;

  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .category-tabs-card,
  .configs-card {
    margin-bottom: 20px;
  }

  .config-tabs {
    .tab-label {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }

  .configs-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .configs-list {
    .config-item {
      border: 1px solid #ebeef5;
      border-radius: 6px;
      margin-bottom: 16px;
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &.modified {
        border-color: #e6a23c;
        background-color: #fdf6ec;
      }

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .config-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid #ebeef5;
        background-color: #fafafa;

        .config-info {
          .config-name {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .config-key {
            font-size: 12px;
            color: #909399;
            font-family: 'Courier New', monospace;
          }
        }

        .config-actions {
          display: flex;
          gap: 8px;
        }
      }

      .config-content {
        padding: 20px;

        .config-description {
          color: #606266;
          margin-bottom: 16px;
          line-height: 1.5;
        }

        .config-editor {
          margin-bottom: 16px;

          .json-editor {
            .json-actions {
              margin-top: 8px;
              display: flex;
              gap: 12px;
            }
          }
        }

        .config-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 16px;
          font-size: 12px;

          .current-value,
          .default-value,
          .validation-rule {
            display: flex;
            align-items: center;
            gap: 6px;

            .label {
              color: #909399;
            }

            .value,
            .rule {
              background-color: #f5f7fa;
              padding: 2px 6px;
              border-radius: 3px;
              font-family: 'Courier New', monospace;
              color: #606266;
            }
          }
        }
      }
    }
  }

  .stats-section {
    .stat-item {
      text-align: center;
      padding: 20px;

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .config-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .config-meta {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .config-management {
    padding: 12px;
  }

  .config-tabs {
    :deep(.el-tabs__nav-wrap) {
      .el-tabs__nav {
        .el-tabs__item {
          padding: 0 12px;
          font-size: 12px;
        }
      }
    }
  }

  .configs-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>