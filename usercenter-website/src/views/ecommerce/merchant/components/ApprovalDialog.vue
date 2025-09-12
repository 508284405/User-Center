<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isApprove ? '审批通过' : '审批拒绝'"
    width="600px"
    :before-close="handleClose"
  >
    <div v-if="merchant" class="approval-form">
      <!-- 商家信息 -->
      <el-card class="merchant-info">
        <template #header>
          <span>商家信息</span>
        </template>
        <el-descriptions :column="1" size="small">
          <el-descriptions-item label="商家编号">
            {{ merchant.merchantNo }}
          </el-descriptions-item>
          <el-descriptions-item label="商家名称">
            {{ merchant.merchantName }}
          </el-descriptions-item>
          <el-descriptions-item label="法人姓名">
            {{ merchant.legalName }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 审批表单 -->
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="120px"
        class="approval-form-content"
      >
        <el-form-item v-if="isApprove" label="佣金率" prop="commissionRate">
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
        
        <el-form-item v-if="!isApprove" label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="form.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        :type="isApprove ? 'success' : 'danger'"
        :loading="loading"
        @click="handleSubmit"
      >
        {{ isApprove ? '通过' : '拒绝' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, nextTick } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { approveMerchant, type Merchant, type MerchantApprovalCmd } from '@/api/client-web/merchant';

// Props
interface Props {
  modelValue: boolean;
  merchant: Merchant | null;
  isApprove: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  merchant: null,
  isApprove: true
});

// Events
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'submit-success': [];
}>();

// 表单引用
const formRef = ref<FormInstance>();

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 表单数据
const form = reactive<MerchantApprovalCmd>({
  merchantNo: '',
  approved: true,
  rejectReason: '',
  commissionRate: 0.05
});

// 表单验证规则
const rules: FormRules = {
  commissionRate: [
    { required: true, message: '请输入佣金率', trigger: 'blur' },
    { type: 'number', min: 0, max: 1, message: '佣金率必须在0-1之间', trigger: 'blur' }
  ],
  rejectReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 1, max: 500, message: '拒绝原因长度为1-500个字符', trigger: 'blur' }
  ]
};

// 状态
const loading = ref(false);

// 监听商家变化
watch(() => props.merchant, (newMerchant) => {
  if (newMerchant) {
    form.merchantNo = newMerchant.merchantNo || '';
    form.approved = props.isApprove;
    form.commissionRate = newMerchant.commissionRate || 0.05;
    form.rejectReason = '';
  }
});

// 提交处理
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;
    const response = await approveMerchant(form);
    
    if (response.success) {
      ElMessage.success(props.isApprove ? '审批通过成功' : '审批拒绝成功');
      emit('submit-success');
      handleClose();
    } else {
      ElMessage.error(response.errMessage || '操作失败');
    }
  } catch (error) {
    console.error('审批操作出错:', error);
    ElMessage.error('操作失败');
  } finally {
    loading.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  // 重置表单
  nextTick(() => {
    formRef.value?.resetFields();
  });
  emit('update:modelValue', false);
};
</script>

<style scoped>
.approval-form {
  padding: 10px;
}

.merchant-info {
  margin-bottom: 20px;
}

.approval-form-content {
  margin-top: 20px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>