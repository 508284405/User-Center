<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量操作商家"
    width="600px"
    :before-close="handleClose"
  >
    <div class="batch-operation">
      <!-- 选中的商家列表 -->
      <el-card class="selected-merchants">
        <template #header>
          <div class="card-header">
            <span>已选中的商家 ({{ selectedMerchants.length }})</span>
          </div>
        </template>
        <div class="merchant-list">
          <el-tag 
            v-for="merchant in selectedMerchants" 
            :key="merchant.merchantNo"
            class="merchant-tag"
            closable
            @close="removeMerchant(merchant)"
          >
            {{ merchant.merchantName }}
          </el-tag>
          <div v-if="selectedMerchants.length === 0" class="empty-text">
            暂无选中商家
          </div>
        </div>
      </el-card>

      <!-- 操作表单 -->
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="120px"
        class="operation-form"
      >
        <el-form-item label="操作类型" prop="action">
          <el-select 
            v-model="form.action" 
            placeholder="请选择操作类型"
            style="width: 100%"
            @change="handleActionChange"
          >
            <el-option 
              v-for="action in actionOptions" 
              :key="action.value"
              :label="action.label" 
              :value="action.value"
              :disabled="action.disabled"
            >
              <div class="action-option">
                <span>{{ action.label }}</span>
                <span class="action-desc">{{ action.description }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item 
          v-if="form.action === 'REJECT' || form.action === 'SUSPEND'" 
          label="操作原因" 
          prop="reason"
        >
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入操作原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item 
          v-if="form.action === 'UPDATE_COMMISSION'" 
          label="新佣金率" 
          prop="commissionRate"
        >
          <el-input-number
            v-model="form.commissionRate"
            :min="0"
            :max="1"
            :step="0.0001"
            :precision="4"
            placeholder="请输入佣金率"
            style="width: 200px"
          />
          <span class="form-tip">范围：0-1（例如：0.05 表示 5%）</span>
        </el-form-item>
      </el-form>

      <!-- 操作预览 -->
      <el-card v-if="form.action" class="preview-card">
        <template #header>
          <span>操作预览</span>
        </template>
        <div class="preview-content">
          <el-alert
            :title="getPreviewText()"
            :type="getAlertType()"
            :closable="false"
          />
          <div class="preview-stats">
            <span>影响商家数：{{ selectedMerchants.length }}</span>
            <span v-if="form.action === 'UPDATE_COMMISSION' && form.commissionRate">
              新佣金率：{{ (form.commissionRate * 100).toFixed(2) }}%
            </span>
          </div>
        </div>
      </el-card>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        :loading="loading"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        执行操作
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { batchProcessMerchants, type MerchantAnalytics, type MerchantBatchCmd } from '@/api/client-web/merchant';

// Props
interface Props {
  modelValue: boolean;
  selectedMerchants: MerchantAnalytics[];
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  selectedMerchants: () => []
});

// Events
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'success': [];
}>();

// Form
const formRef = ref<FormInstance>();
const form = reactive<{
  action: string;
  reason: string;
  commissionRate: number | null;
}>({
  action: '',
  reason: '',
  commissionRate: null
});

const rules: FormRules = {
  action: [
    { required: true, message: '请选择操作类型', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入操作原因', trigger: 'blur' },
    { min: 5, max: 200, message: '操作原因长度为5-200个字符', trigger: 'blur' }
  ],
  commissionRate: [
    { required: true, message: '请输入佣金率', trigger: 'blur' },
    { type: 'number', min: 0, max: 1, message: '佣金率必须在0-1之间', trigger: 'blur' }
  ]
};

// Data
const loading = ref(false);

// Computed
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const actionOptions = computed(() => [
  { 
    value: 'APPROVE', 
    label: '批量审批通过', 
    description: '将待审核商家设为已通过',
    disabled: false
  },
  { 
    value: 'REJECT', 
    label: '批量拒绝', 
    description: '拒绝待审核商家申请',
    disabled: false
  },
  { 
    value: 'SUSPEND', 
    label: '批量暂停', 
    description: '暂停已通过的商家',
    disabled: false
  },
  { 
    value: 'ACTIVATE', 
    label: '批量激活', 
    description: '激活已暂停/冻结的商家',
    disabled: false
  },
  { 
    value: 'UPDATE_COMMISSION', 
    label: '批量更新佣金率', 
    description: '统一调整商家佣金率',
    disabled: false
  }
]);

const canSubmit = computed(() => {
  if (!form.action || props.selectedMerchants.length === 0) return false;
  
  if ((form.action === 'REJECT' || form.action === 'SUSPEND') && !form.reason) {
    return false;
  }
  
  if (form.action === 'UPDATE_COMMISSION' && (form.commissionRate === null || form.commissionRate === undefined)) {
    return false;
  }
  
  return true;
});

// Methods
const removeMerchant = (merchant: MerchantAnalytics) => {
  const index = props.selectedMerchants.findIndex(m => m.merchantNo === merchant.merchantNo);
  if (index > -1) {
    props.selectedMerchants.splice(index, 1);
  }
};

const handleActionChange = () => {
  // 清空相关字段
  form.reason = '';
  form.commissionRate = null;
};

const getPreviewText = () => {
  const count = props.selectedMerchants.length;
  
  switch (form.action) {
    case 'APPROVE':
      return `将批量审批通过 ${count} 个商家`;
    case 'REJECT':
      return `将批量拒绝 ${count} 个商家申请`;
    case 'SUSPEND':
      return `将批量暂停 ${count} 个商家`;
    case 'ACTIVATE':
      return `将批量激活 ${count} 个商家`;
    case 'UPDATE_COMMISSION':
      return `将批量更新 ${count} 个商家的佣金率`;
    default:
      return '';
  }
};

const getAlertType = () => {
  switch (form.action) {
    case 'APPROVE':
    case 'ACTIVATE':
      return 'success';
    case 'REJECT':
    case 'SUSPEND':
      return 'warning';
    case 'UPDATE_COMMISSION':
      return 'info';
    default:
      return 'info';
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    
    // 确认操作
    const confirmText = getPreviewText() + '，此操作不可撤销，是否继续？';
    await ElMessageBox.confirm(confirmText, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    loading.value = true;

    const batchCmd: MerchantBatchCmd = {
      merchantNos: props.selectedMerchants.map(m => m.merchantNo),
      action: form.action as any,
      reason: form.reason || undefined,
      commissionRate: form.commissionRate || undefined
    };

    const response = await batchProcessMerchants(batchCmd);
    
    if (response.success) {
      ElMessage.success('批量操作执行成功');
      emit('success');
      handleClose();
    } else {
      ElMessage.error(response.errMessage || '批量操作失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量操作出错:', error);
      ElMessage.error('批量操作失败');
    }
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  // 重置表单
  form.action = '';
  form.reason = '';
  form.commissionRate = null;
  formRef.value?.resetFields();
  
  emit('update:modelValue', false);
};
</script>

<style scoped>
.batch-operation {
  padding: 10px;
}

.selected-merchants {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
}

.merchant-list {
  min-height: 60px;
}

.merchant-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.empty-text {
  color: #909399;
  text-align: center;
  padding: 20px;
  font-style: italic;
}

.operation-form {
  margin-bottom: 20px;
}

.action-option {
  display: flex;
  flex-direction: column;
}

.action-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.preview-card {
  margin-bottom: 20px;
}

.preview-content {
  padding-top: 10px;
}

.preview-stats {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}
</style>