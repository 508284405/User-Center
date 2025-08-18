<script setup lang="ts">
import { ref, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['update:modelValue', 'confirm'])

// 拒绝原因
const rejectReason = ref('')
const dialogVisible = ref(false)

// 监听外部传入的visible
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    // 重置表单
    rejectReason.value = ''
  }
})

// 同步dialog状态到父组件
watch(() => dialogVisible.value, (val) => {
  emits('update:modelValue', val)
})

// 提交
const handleSubmit = () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  emits('confirm', rejectReason.value)
  // 清空表单
  rejectReason.value = ''
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="审核拒绝"
    width="500px"
    append-to-body
    :close-on-click-modal="false"
  >
    <div class="reject-dialog-content">
      <p class="dialog-tip">请填写拒绝售后申请的原因：</p>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因，将会通知给用户"
      />
    </div>
    
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确认</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.reject-dialog-content {
  padding: $spacing-medium 0;
  
  .dialog-tip {
    margin-bottom: $spacing-medium;
    color: $color-gray;
  }
}
</style> 
