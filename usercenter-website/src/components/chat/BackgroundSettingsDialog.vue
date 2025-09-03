<template>
  <el-dialog
    v-model="dialogVisible"
    title="设置聊天背景"
    width="700px"
    :append-to-body="true"
    class="background-settings-dialog"
  >
    <div class="background-container">
      <!-- 当前背景预览 -->
      <div class="current-background-section">
        <div class="section-title">
          <h4>当前背景</h4>
          <el-button
            v-if="currentBackground"
            type="text"
            @click="handleResetBackground"
            class="reset-btn"
          >
            恢复默认
          </el-button>
        </div>
        
        <div class="current-preview">
          <div 
            class="preview-area"
            :style="getBackgroundStyle(currentBackground)"
          >
            <div class="mock-message-container">
              <div class="mock-message sent">
                <div class="message-content">这是一条示例消息</div>
                <div class="message-time">14:32</div>
              </div>
              <div class="mock-message received">
                <div class="message-content">聊天背景预览效果</div>
                <div class="message-time">14:33</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 背景选项 -->
      <div class="background-options-section">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 预设背景 -->
          <el-tab-pane label="预设背景" name="presets">
            <div class="presets-grid">
              <div
                v-for="preset in presetBackgrounds"
                :key="preset.id"
                class="preset-item"
                :class="{ active: selectedBackground === preset.value }"
                @click="handleSelectPreset(preset)"
              >
                <div 
                  class="preset-preview"
                  :style="getBackgroundStyle(preset.value)"
                >
                  <div class="preview-overlay">
                    <el-icon v-if="selectedBackground === preset.value">
                      <Check />
                    </el-icon>
                  </div>
                </div>
                <div class="preset-name">{{ preset.name }}</div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 纯色背景 -->
          <el-tab-pane label="纯色背景" name="colors">
            <div class="color-section">
              <div class="color-picker-wrapper">
                <div class="color-picker-area">
                  <el-color-picker
                    v-model="customColor"
                    :predefine="colorPresets"
                    @change="handleColorChange"
                    size="large"
                  />
                  <span class="color-label">选择颜色</span>
                </div>
                
                <div class="preset-colors">
                  <div
                    v-for="(color, index) in colorPresets"
                    :key="index"
                    class="color-preset"
                    :class="{ active: selectedBackground === color }"
                    :style="{ backgroundColor: color }"
                    @click="handleSelectColor(color)"
                  >
                    <el-icon v-if="selectedBackground === color">
                      <Check />
                    </el-icon>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 自定义上传 -->
          <el-tab-pane label="自定义上传" name="upload">
            <div class="upload-section">
              <el-upload
                ref="uploadRef"
                class="background-upload"
                :auto-upload="false"
                :on-change="handleFileChange"
                :show-file-list="false"
                accept="image/*"
                drag
              >
                <div class="upload-content">
                  <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                  <div class="el-upload__text">
                    将图片文件拖拽到此处，或
                    <em>点击上传</em>
                  </div>
                  <div class="upload-tips">
                    <p>支持 JPG、PNG、GIF 格式</p>
                    <p>建议尺寸：1920x1080，文件大小不超过5MB</p>
                  </div>
                </div>
              </el-upload>

              <!-- 上传的图片预览 -->
              <div v-if="uploadedImage" class="uploaded-preview">
                <div class="preview-title">上传的背景图片：</div>
                <div 
                  class="uploaded-image-preview"
                  :style="getBackgroundStyle(uploadedImage)"
                  @click="handleSelectUploaded"
                >
                  <div class="preview-overlay" :class="{ active: selectedBackground === uploadedImage }">
                    <el-icon v-if="selectedBackground === uploadedImage">
                      <Check />
                    </el-icon>
                    <span v-else>点击选择</span>
                  </div>
                </div>
              </div>

              <!-- 上传历史 -->
              <div v-if="uploadHistory.length > 0" class="upload-history">
                <div class="history-title">上传历史：</div>
                <div class="history-grid">
                  <div
                    v-for="(item, index) in uploadHistory"
                    :key="index"
                    class="history-item"
                    :class="{ active: selectedBackground === item.url }"
                    @click="handleSelectHistory(item)"
                  >
                    <div 
                      class="history-preview"
                      :style="getBackgroundStyle(item.url)"
                    >
                      <div class="preview-overlay">
                        <el-icon v-if="selectedBackground === item.url">
                          <Check />
                        </el-icon>
                      </div>
                    </div>
                    <div class="history-name">{{ item.name }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 背景设置选项 -->
      <div class="background-settings-section">
        <el-collapse>
          <el-collapse-item title="高级设置" name="advanced">
            <div class="advanced-settings">
              <div class="setting-item">
                <label>背景透明度</label>
                <el-slider
                  v-model="backgroundOpacity"
                  :min="10"
                  :max="100"
                  :step="5"
                  show-stops
                  @change="handleOpacityChange"
                />
              </div>

              <div class="setting-item">
                <label>背景模糊</label>
                <el-slider
                  v-model="backgroundBlur"
                  :min="0"
                  :max="20"
                  :step="1"
                  show-stops
                  @change="handleBlurChange"
                />
              </div>

              <div class="setting-item">
                <el-checkbox v-model="backgroundFit" @change="handleFitChange">
                  适应屏幕尺寸
                </el-checkbox>
                <div class="setting-desc">
                  开启后背景图片会自动适应聊天窗口大小
                </div>
              </div>

              <div class="setting-item">
                <el-checkbox v-model="backgroundRepeat" @change="handleRepeatChange">
                  重复平铺
                </el-checkbox>
                <div class="setting-desc">
                  当图片小于窗口时重复显示
                </div>
              </div>
            </div>
          </el-collapse>
        </el-collapse>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="handlePreview">预览效果</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirm"
          :loading="updating"
        >
          应用背景
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Check, UploadFilled } from '@element-plus/icons-vue';
import {
  setConversationBackground
} from '@/api/smartcs/conversation';

// Props
interface Props {
  modelValue: boolean;
  userId: string;
  sessionId: number;
  currentBackground?: string;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'background-updated': [background: string];
}>();

// 响应式数据
const updating = ref(false);
const activeTab = ref('presets');
const selectedBackground = ref('');
const customColor = ref('#f0f9ff');
const uploadedImage = ref('');
const backgroundOpacity = ref(100);
const backgroundBlur = ref(0);
const backgroundFit = ref(true);
const backgroundRepeat = ref(false);

// 上传历史记录
const uploadHistory = ref<Array<{ name: string; url: string }>>([]);

// 预设背景
const presetBackgrounds = [
  {
    id: 1,
    name: '默认',
    value: '',
  },
  {
    id: 2,
    name: '星空',
    value: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 3,
    name: '海洋',
    value: 'linear-gradient(135deg, #74b9ff 0%, #0984e3 100%)'
  },
  {
    id: 4,
    name: '森林',
    value: 'linear-gradient(135deg, #55efc4 0%, #81ecec 100%)'
  },
  {
    id: 5,
    name: '日落',
    value: 'linear-gradient(135deg, #fd79a8 0%, #fdcb6e 100%)'
  },
  {
    id: 6,
    name: '极光',
    value: 'linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%)'
  },
  {
    id: 7,
    name: '樱花',
    value: 'linear-gradient(135deg, #fd79a8 0%, #e84393 100%)'
  },
  {
    id: 8,
    name: '薄雾',
    value: 'linear-gradient(135deg, #ddd6fe 0%, #e0e7ff 100%)'
  }
];

// 预设颜色
const colorPresets = [
  '#f0f9ff', '#ecfeff', '#f0fdf4', '#fffbeb',
  '#fef7f7', '#fdf2f8', '#f8fafc', '#fafafa',
  '#e6f3ff', '#e6fffa', '#f0fff4', '#fffacd',
  '#ffe4e1', '#f8e8ff', '#f5f5f5', '#ffffff'
];

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const currentBackground = computed(() => {
  return props.currentBackground || '';
});

// 方法
const getBackgroundStyle = (background: string) => {
  if (!background) {
    return { backgroundColor: '#f0f9ff' };
  }

  const style: Record<string, any> = {};
  
  if (background.startsWith('linear-gradient')) {
    style.background = background;
  } else if (background.startsWith('#') || background.startsWith('rgb')) {
    style.backgroundColor = background;
  } else {
    style.backgroundImage = `url(${background})`;
    style.backgroundSize = backgroundFit.value ? 'cover' : 'auto';
    style.backgroundRepeat = backgroundRepeat.value ? 'repeat' : 'no-repeat';
    style.backgroundPosition = 'center';
  }

  // 应用透明度和模糊效果
  if (backgroundOpacity.value < 100) {
    style.opacity = backgroundOpacity.value / 100;
  }
  
  if (backgroundBlur.value > 0) {
    style.filter = `blur(${backgroundBlur.value}px)`;
  }

  return style;
};

const handleSelectPreset = (preset: typeof presetBackgrounds[0]) => {
  selectedBackground.value = preset.value;
};

const handleColorChange = (color: string) => {
  if (color) {
    selectedBackground.value = color;
  }
};

const handleSelectColor = (color: string) => {
  customColor.value = color;
  selectedBackground.value = color;
};

const handleFileChange = (file: any) => {
  const fileObj = file.raw;
  if (!fileObj) return;

  // 验证文件类型
  if (!fileObj.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return;
  }

  // 验证文件大小 (5MB)
  if (fileObj.size > 5 * 1024 * 1024) {
    ElMessage.error('图片文件不能超过5MB');
    return;
  }

  // 创建预览URL
  const reader = new FileReader();
  reader.onload = (e) => {
    const result = e.target?.result as string;
    uploadedImage.value = result;
    
    // 添加到历史记录
    const historyItem = {
      name: fileObj.name,
      url: result
    };
    
    // 检查是否已存在
    const existingIndex = uploadHistory.value.findIndex(item => item.name === fileObj.name);
    if (existingIndex > -1) {
      uploadHistory.value.splice(existingIndex, 1);
    }
    
    uploadHistory.value.unshift(historyItem);
    
    // 限制历史记录数量
    if (uploadHistory.value.length > 6) {
      uploadHistory.value = uploadHistory.value.slice(0, 6);
    }
    
    // 自动选择上传的图片
    selectedBackground.value = result;
    
    ElMessage.success('图片上传成功');
  };
  reader.readAsDataURL(fileObj);
};

const handleSelectUploaded = () => {
  selectedBackground.value = uploadedImage.value;
};

const handleSelectHistory = (item: { name: string; url: string }) => {
  selectedBackground.value = item.url;
};

const handleOpacityChange = () => {
  // 实时预览透明度变化
};

const handleBlurChange = () => {
  // 实时预览模糊变化
};

const handleFitChange = () => {
  // 实时预览适应变化
};

const handleRepeatChange = () => {
  // 实时预览重复变化
};

const handleTabChange = () => {
  // 清除之前的选择状态
};

const handleResetBackground = async () => {
  selectedBackground.value = '';
  await handleConfirm();
};

const handlePreview = () => {
  // TODO: 实现预览功能
  ElMessage.info('预览功能开发中');
};

const handleConfirm = async () => {
  updating.value = true;
  
  try {
    let backgroundValue = selectedBackground.value;
    
    // 如果是上传的图片，需要先上传到服务器
    if (backgroundValue && backgroundValue.startsWith('data:image/')) {
      // TODO: 调用图片上传API
      // const uploadResponse = await uploadImage(backgroundValue);
      // backgroundValue = uploadResponse.url;
      
      // 模拟上传成功
      ElMessage.info('图片上传中...');
      // 这里应该实现真正的图片上传逻辑
    }

    const response = await setConversationBackground({
      userId: props.userId,
      sessionId: props.sessionId,
      background: backgroundValue
    });

    if (response.success) {
      ElMessage.success('背景设置成功');
      emit('background-updated', backgroundValue);
      dialogVisible.value = false;
    } else {
      ElMessage.error(response.errMessage || '设置背景失败');
    }
  } catch (error) {
    console.error('设置背景失败:', error);
    ElMessage.error('设置背景失败');
  } finally {
    updating.value = false;
  }
};

// 生命周期
onMounted(() => {
  // 从本地存储加载上传历史
  const savedHistory = localStorage.getItem('chat-background-history');
  if (savedHistory) {
    try {
      uploadHistory.value = JSON.parse(savedHistory);
    } catch (error) {
      console.warn('加载上传历史失败:', error);
    }
  }
});

// 监听对话框打开
watch(dialogVisible, (newVal) => {
  if (newVal) {
    selectedBackground.value = currentBackground.value;
    
    // 重置设置
    backgroundOpacity.value = 100;
    backgroundBlur.value = 0;
    backgroundFit.value = true;
    backgroundRepeat.value = false;
  }
});

// 监听上传历史变化，保存到本地存储
watch(uploadHistory, (newHistory) => {
  localStorage.setItem('chat-background-history', JSON.stringify(newHistory));
}, { deep: true });
</script>

<style scoped lang="scss">
.background-settings-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.background-container {
  .current-background-section {
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
      
      .reset-btn {
        color: #909399;
        
        &:hover {
          color: #409eff;
        }
      }
    }
    
    .current-preview {
      .preview-area {
        height: 200px;
        border-radius: 8px;
        border: 1px solid #e4e7ed;
        position: relative;
        overflow: hidden;
        
        .mock-message-container {
          position: absolute;
          bottom: 20px;
          left: 20px;
          right: 20px;
          
          .mock-message {
            display: flex;
            margin-bottom: 12px;
            
            &.sent {
              justify-content: flex-end;
              
              .message-content {
                background-color: #409eff;
                color: white;
                border-radius: 18px 18px 4px 18px;
                padding: 8px 16px;
                font-size: 14px;
                max-width: 200px;
              }
              
              .message-time {
                font-size: 12px;
                color: #909399;
                align-self: flex-end;
                margin-left: 8px;
              }
            }
            
            &.received {
              justify-content: flex-start;
              
              .message-content {
                background-color: white;
                color: #303133;
                border-radius: 18px 18px 18px 4px;
                padding: 8px 16px;
                font-size: 14px;
                max-width: 200px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
              }
              
              .message-time {
                font-size: 12px;
                color: #909399;
                align-self: flex-end;
                margin-right: 8px;
              }
            }
          }
        }
      }
    }
  }

  .background-options-section {
    margin-bottom: 24px;
    
    .presets-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      gap: 16px;
      padding: 16px 0;
      
      .preset-item {
        cursor: pointer;
        text-align: center;
        
        .preset-preview {
          height: 80px;
          border-radius: 8px;
          border: 2px solid transparent;
          position: relative;
          transition: all 0.3s;
          
          .preview-overlay {
            position: absolute;
            inset: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: rgba(0, 0, 0, 0.1);
            border-radius: 6px;
            opacity: 0;
            transition: opacity 0.3s;
            
            .el-icon {
              color: white;
              font-size: 24px;
            }
          }
        }
        
        .preset-name {
          margin-top: 8px;
          font-size: 13px;
          color: #606266;
        }
        
        &:hover .preset-preview {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
          
          .preview-overlay {
            opacity: 1;
          }
        }
        
        &.active .preset-preview {
          border-color: #409eff;
          
          .preview-overlay {
            opacity: 1;
            background-color: rgba(64, 158, 255, 0.3);
          }
        }
      }
    }
    
    .color-section {
      padding: 16px 0;
      
      .color-picker-wrapper {
        .color-picker-area {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 24px;
          
          .color-label {
            font-size: 14px;
            color: #606266;
          }
        }
        
        .preset-colors {
          display: grid;
          grid-template-columns: repeat(8, 1fr);
          gap: 12px;
          
          .color-preset {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            border: 2px solid transparent;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
            
            .el-icon {
              color: #606266;
              font-size: 20px;
            }
            
            &:hover {
              transform: scale(1.1);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
            }
            
            &.active {
              border-color: #409eff;
              transform: scale(1.1);
            }
          }
        }
      }
    }
    
    .upload-section {
      padding: 16px 0;
      
      .background-upload {
        margin-bottom: 24px;
        
        :deep(.el-upload-dragger) {
          width: 100%;
          height: 180px;
          
          .upload-content {
            padding: 20px;
            
            .el-icon--upload {
              font-size: 48px;
              color: #c0c4cc;
              margin-bottom: 16px;
            }
            
            .el-upload__text {
              font-size: 14px;
              color: #606266;
              margin-bottom: 12px;
            }
            
            .upload-tips {
              p {
                margin: 4px 0;
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }
      }
      
      .uploaded-preview,
      .upload-history {
        margin-bottom: 24px;
        
        .preview-title,
        .history-title {
          font-size: 14px;
          color: #606266;
          margin-bottom: 12px;
        }
        
        .uploaded-image-preview {
          height: 120px;
          border-radius: 8px;
          border: 2px solid #e4e7ed;
          position: relative;
          cursor: pointer;
          transition: all 0.3s;
          
          .preview-overlay {
            position: absolute;
            inset: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: rgba(0, 0, 0, 0.3);
            border-radius: 6px;
            color: white;
            font-size: 14px;
            transition: all 0.3s;
            
            &.active {
              background-color: rgba(64, 158, 255, 0.3);
            }
          }
          
          &:hover {
            border-color: #409eff;
          }
        }
        
        .history-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
          gap: 12px;
          
          .history-item {
            text-align: center;
            cursor: pointer;
            
            .history-preview {
              height: 80px;
              border-radius: 6px;
              border: 2px solid transparent;
              position: relative;
              transition: all 0.3s;
              
              .preview-overlay {
                position: absolute;
                inset: 0;
                display: flex;
                align-items: center;
                justify-content: center;
                background-color: rgba(0, 0, 0, 0.2);
                border-radius: 4px;
                opacity: 0;
                transition: opacity 0.3s;
                
                .el-icon {
                  color: white;
                  font-size: 20px;
                }
              }
            }
            
            .history-name {
              margin-top: 8px;
              font-size: 12px;
              color: #909399;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            &:hover .history-preview {
              transform: translateY(-2px);
              
              .preview-overlay {
                opacity: 1;
              }
            }
            
            &.active .history-preview {
              border-color: #409eff;
              
              .preview-overlay {
                opacity: 1;
                background-color: rgba(64, 158, 255, 0.3);
              }
            }
          }
        }
      }
    }
  }

  .background-settings-section {
    border-top: 1px solid #ebeef5;
    padding-top: 16px;
    
    .advanced-settings {
      .setting-item {
        margin-bottom: 24px;
        
        label {
          display: block;
          font-size: 14px;
          color: #606266;
          margin-bottom: 12px;
        }
        
        .setting-desc {
          margin-top: 8px;
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

.dialog-footer {
  text-align: right;
}
</style>