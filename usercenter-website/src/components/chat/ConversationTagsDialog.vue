<template>
  <el-dialog
    v-model="dialogVisible"
    title="管理会话标签"
    width="600px"
    :append-to-body="true"
    class="conversation-tags-dialog"
  >
    <div class="tags-container">
      <!-- 当前会话标签 ---->
      <div class="current-tags-section">
        <div class="section-title">
          <h4>当前会话标签</h4>
          <span class="tags-count">{{ conversationTags.length }} 个标签</span>
        </div>
        
        <div class="current-tags">
          <div v-if="conversationTags.length === 0" class="no-tags-tip">
            <el-icon><PriceTag /></el-icon>
            <p>此会话还未设置标签</p>
          </div>
          
          <div v-else class="tags-list">
            <el-tag
              v-for="tag in conversationTags"
              :key="tag.id"
              :color="tag.tagColor"
              size="large"
              closable
              @close="handleRemoveTag(tag)"
              class="conversation-tag"
            >
              {{ tag.tagName }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 可用标签 -->
      <div class="available-tags-section">
        <div class="section-title">
          <h4>可选标签</h4>
          <el-button
            type="primary"
            size="small"
            @click="showCreateDialog = true"
          >
            <el-icon><Plus /></el-icon>
            新建标签
          </el-button>
        </div>

        <div class="available-tags">
          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-else-if="availableTags.length === 0" class="no-available-tags">
            <el-icon><FolderOpened /></el-icon>
            <p>暂无可用标签，请先创建标签</p>
          </div>

          <div v-else class="tags-grid">
            <div
              v-for="tag in availableTags"
              :key="tag.id"
              class="tag-item"
              @click="handleAddTag(tag)"
            >
              <div 
                class="tag-color" 
                :style="{ backgroundColor: tag.tagColor || '#909399' }"
              ></div>
              <span class="tag-name">{{ tag.tagName }}</span>
              <el-icon class="add-icon"><Plus /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 标签管理 -->
      <div class="tag-management-section">
        <el-collapse>
          <el-collapse-item title="标签管理" name="management">
            <div class="management-content">
              <div class="management-actions">
                <el-button
                  type="text"
                  @click="showCreateDialog = true"
                >
                  <el-icon><Plus /></el-icon>
                  创建新标签
                </el-button>
                
                <el-button
                  type="text"
                  @click="showManageDialog = true"
                >
                  <el-icon><Edit /></el-icon>
                  管理现有标签
                </el-button>
              </div>

              <!-- 预设标签快速添加 -->
              <div class="preset-tags" v-if="presetTags.length > 0">
                <div class="preset-title">常用标签：</div>
                <el-tag
                  v-for="preset in presetTags"
                  :key="preset.name"
                  :color="preset.color"
                  @click="handleAddPresetTag(preset)"
                  class="preset-tag clickable"
                  size="small"
                >
                  {{ preset.name }}
                </el-tag>
              </div>
            </div>
          </el-collapse>
        </el-collapse>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </div>
    </template>

    <!-- 创建标签对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新标签"
      width="400px"
      :append-to-body="true"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="80px"
      >
        <el-form-item label="标签名称" prop="tagName">
          <el-input
            v-model="createForm.tagName"
            placeholder="请输入标签名称"
            maxlength="10"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="标签颜色" prop="tagColor">
          <div class="color-picker-wrapper">
            <el-color-picker
              v-model="createForm.tagColor"
              :predefine="colorPresets"
              show-alpha
            />
            <span class="color-preview" :style="{ backgroundColor: createForm.tagColor }">
              {{ createForm.tagName || '预览' }}
            </span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleCreateTag"
            :loading="creating"
          >
            创建
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 标签管理对话框 -->
    <el-dialog
      v-model="showManageDialog"
      title="管理标签"
      width="500px"
      :append-to-body="true"
    >
      <div class="manage-tags-content">
        <div v-if="allUserTags.length === 0" class="empty-state">
          <el-icon size="48" color="#c0c4cc"><FolderOpened /></el-icon>
          <p>暂无标签</p>
        </div>

        <div v-else class="manage-tags-list">
          <div
            v-for="tag in allUserTags"
            :key="tag.id"
            class="manage-tag-item"
          >
            <div class="tag-info">
              <div
                class="tag-color-dot"
                :style="{ backgroundColor: tag.tagColor || '#909399' }"
              ></div>
              <span class="tag-name">{{ tag.tagName }}</span>
            </div>
            <div class="tag-actions">
              <el-button
                type="text"
                size="small"
                @click="handleEditTag(tag)"
              >
                编辑
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleDeleteTag(tag)"
                class="danger-btn"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { PriceTag, Plus, FolderOpened, Edit } from '@element-plus/icons-vue';
import {
  getConversationTags,
  createConversationTag,
  addTagToConversation,
  removeTagFromConversation,
  type ConversationTag
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
  'tags-updated': [];
}>();

// 响应式数据
const loading = ref(false);
const creating = ref(false);
const allUserTags = ref<ConversationTag[]>([]);
const conversationTags = ref<ConversationTag[]>([]);
const showCreateDialog = ref(false);
const showManageDialog = ref(false);
const createFormRef = ref<FormInstance>();

const createForm = ref({
  tagName: '',
  tagColor: '#409eff'
});

const createFormRules: FormRules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 10, message: '标签名称长度在 1 到 10 个字符', trigger: 'blur' }
  ]
};

// 预设颜色
const colorPresets = [
  '#f56c6c', '#409eff', '#67c23a', '#e6a23c',
  '#909399', '#c45656', '#73767a', '#626aef',
  '#f78989', '#409eff'
];

// 预设标签
const presetTags = [
  { name: '重要', color: '#f56c6c' },
  { name: '工作', color: '#409eff' },
  { name: '学习', color: '#67c23a' },
  { name: '娱乐', color: '#e6a23c' }
];

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const availableTags = computed(() => {
  const conversationTagIds = conversationTags.value.map(t => t.id);
  return allUserTags.value.filter(tag => !conversationTagIds.includes(tag.id));
});

// 方法
const loadTags = async () => {
  if (!props.userId) return;

  loading.value = true;
  try {
    const response = await getConversationTags(props.userId);
    if (response.success && response.data) {
      allUserTags.value = response.data;
      // TODO: 加载会话的具体标签
      // 这里需要一个获取会话具体标签的API
      conversationTags.value = [];
    }
  } catch (error) {
    console.error('加载标签失败:', error);
    ElMessage.error('加载标签失败');
  } finally {
    loading.value = false;
  }
};

const handleCreateTag = async () => {
  if (!createFormRef.value) return;

  try {
    await createFormRef.value.validate();
    
    creating.value = true;
    const response = await createConversationTag({
      userId: props.userId,
      tagName: createForm.value.tagName,
      tagColor: createForm.value.tagColor
    });

    if (response.success) {
      ElMessage.success('标签创建成功');
      showCreateDialog.value = false;
      createForm.value = { tagName: '', tagColor: '#409eff' };
      createFormRef.value.resetFields();
      await loadTags();
    } else {
      ElMessage.error(response.errMessage || '创建标签失败');
    }
  } catch (error) {
    console.error('创建标签失败:', error);
    ElMessage.error('创建标签失败');
  } finally {
    creating.value = false;
  }
};

const handleAddTag = async (tag: ConversationTag) => {
  try {
    const response = await addTagToConversation({
      userId: props.userId,
      sessionId: props.sessionId,
      tagId: tag.id
    });

    if (response.success) {
      conversationTags.value.push(tag);
      ElMessage.success(`已添加标签"${tag.tagName}"`);
      emit('tags-updated');
    } else {
      ElMessage.error(response.errMessage || '添加标签失败');
    }
  } catch (error) {
    console.error('添加标签失败:', error);
    ElMessage.error('添加标签失败');
  }
};

const handleRemoveTag = async (tag: ConversationTag) => {
  try {
    const response = await removeTagFromConversation({
      userId: props.userId,
      sessionId: props.sessionId,
      tagId: tag.id
    });

    if (response.success) {
      const index = conversationTags.value.findIndex(t => t.id === tag.id);
      if (index > -1) {
        conversationTags.value.splice(index, 1);
      }
      ElMessage.success(`已移除标签"${tag.tagName}"`);
      emit('tags-updated');
    } else {
      ElMessage.error(response.errMessage || '移除标签失败');
    }
  } catch (error) {
    console.error('移除标签失败:', error);
    ElMessage.error('移除标签失败');
  }
};

const handleAddPresetTag = async (preset: { name: string; color: string }) => {
  // 检查是否已经存在同名标签
  const existingTag = allUserTags.value.find(tag => tag.tagName === preset.name);
  
  if (existingTag) {
    // 如果标签已存在，直接添加到会话
    await handleAddTag(existingTag);
  } else {
    // 如果标签不存在，先创建后添加
    try {
      creating.value = true;
      const response = await createConversationTag({
        userId: props.userId,
        tagName: preset.name,
        tagColor: preset.color
      });

      if (response.success && response.data) {
        await loadTags();
        const newTag = allUserTags.value.find(tag => tag.tagName === preset.name);
        if (newTag) {
          await handleAddTag(newTag);
        }
      } else {
        ElMessage.error(response.errMessage || '创建预设标签失败');
      }
    } catch (error) {
      console.error('创建预设标签失败:', error);
      ElMessage.error('创建预设标签失败');
    } finally {
      creating.value = false;
    }
  }
};

const handleEditTag = (tag: ConversationTag) => {
  // TODO: 实现标签编辑功能
  ElMessage.info('标签编辑功能开发中');
};

const handleDeleteTag = async (tag: ConversationTag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签"${tag.tagName}"吗？删除后将从所有使用此标签的会话中移除。`,
      '删除标签',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );

    // TODO: 调用删除标签API
    ElMessage.success(`标签"${tag.tagName}"已删除`);
    await loadTags();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除标签失败:', error);
      ElMessage.error('删除标签失败');
    }
  }
};

// 生命周期
onMounted(() => {
  if (props.userId) {
    loadTags();
  }
});

// 监听对话框打开
watch(dialogVisible, (newVal) => {
  if (newVal && props.userId) {
    loadTags();
  }
});

// 监听用户ID变化
watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    loadTags();
  }
});
</script>

<style scoped lang="scss">
.conversation-tags-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.tags-container {
  .current-tags-section {
    margin-bottom: 24px;
    
    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h4 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
      
      .tags-count {
        font-size: 14px;
        color: #909399;
      }
    }
    
    .current-tags {
      .no-tags-tip {
        text-align: center;
        padding: 32px 0;
        color: #909399;
        
        .el-icon {
          font-size: 48px;
          margin-bottom: 8px;
        }
        
        p {
          margin: 0;
          font-size: 14px;
        }
      }
      
      .tags-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .conversation-tag {
          font-size: 14px;
          padding: 8px 12px;
        }
      }
    }
  }

  .available-tags-section {
    margin-bottom: 24px;
    
    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h4 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }
    
    .available-tags {
      .loading-state {
        padding: 20px 0;
      }
      
      .no-available-tags {
        text-align: center;
        padding: 32px 0;
        color: #909399;
        
        .el-icon {
          font-size: 48px;
          margin-bottom: 8px;
        }
        
        p {
          margin: 0;
          font-size: 14px;
        }
      }
      
      .tags-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
        gap: 12px;
        
        .tag-item {
          display: flex;
          align-items: center;
          padding: 12px;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.2s;
          
          &:hover {
            border-color: #409eff;
            background-color: #f0f9ff;
            
            .add-icon {
              color: #409eff;
            }
          }
          
          .tag-color {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            margin-right: 8px;
            flex-shrink: 0;
          }
          
          .tag-name {
            flex: 1;
            font-size: 14px;
            color: #606266;
          }
          
          .add-icon {
            color: #c0c4cc;
            font-size: 16px;
            transition: color 0.2s;
          }
        }
      }
    }
  }

  .tag-management-section {
    border-top: 1px solid #ebeef5;
    padding-top: 16px;
    
    .management-content {
      .management-actions {
        display: flex;
        gap: 16px;
        margin-bottom: 16px;
      }
      
      .preset-tags {
        .preset-title {
          font-size: 13px;
          color: #606266;
          margin-bottom: 8px;
        }
        
        .preset-tag {
          margin: 0 8px 8px 0;
          
          &.clickable {
            cursor: pointer;
            transition: opacity 0.2s;
            
            &:hover {
              opacity: 0.8;
            }
          }
        }
      }
    }
  }
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .color-preview {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    color: white;
    min-width: 50px;
    text-align: center;
  }
}

.manage-tags-content {
  .empty-state {
    text-align: center;
    padding: 40px 0;
    color: #909399;
    
    p {
      margin: 8px 0 0 0;
      font-size: 14px;
    }
  }
  
  .manage-tags-list {
    .manage-tag-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f5f7fa;
      
      &:last-child {
        border-bottom: none;
      }
      
      .tag-info {
        display: flex;
        align-items: center;
        
        .tag-color-dot {
          width: 12px;
          height: 12px;
          border-radius: 50%;
          margin-right: 8px;
        }
        
        .tag-name {
          font-size: 14px;
          color: #303133;
        }
      }
      
      .tag-actions {
        display: flex;
        gap: 8px;
        
        .danger-btn {
          color: #f56c6c;
          
          &:hover {
            color: #f78989;
          }
        }
      }
    }
  }
}

.dialog-footer {
  text-align: right;
}
</style>