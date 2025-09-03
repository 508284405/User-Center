<template>
  <el-dialog
    :model-value="modelValue"
    title="会员价格测算"
    width="500px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="calculator-content">
      <!-- 配置信息展示 -->
      <el-card class="config-info">
        <template #header>
          <div class="config-title">配置信息</div>
        </template>
        <div class="config-item">
          <span class="label">商品ID:</span>
          <span class="value">{{ pricingConfig?.productId }}</span>
        </div>
        <div class="config-item">
          <span class="label">会员等级:</span>
          <el-tag :type="getMemberLevelType(pricingConfig?.memberLevel)">
            {{ getMemberLevelText(pricingConfig?.memberLevel) }}
          </el-tag>
        </div>
        <div class="config-item">
          <span class="label">折扣类型:</span>
          <span class="value">{{ pricingConfig?.discountTypeDesc }}</span>
        </div>
        <div class="config-item">
          <span class="label">折扣值:</span>
          <span class="value highlight">{{ formatDiscountValue(pricingConfig) }}</span>
        </div>
      </el-card>

      <!-- 测算表单 -->
      <el-form :model="calcForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="原价" required>
          <el-input-number
            v-model="calcForm.originalPrice"
            :precision="2"
            :step="0.01"
            :min="0.01"
            style="width: 100%"
          >
            <template #append>元</template>
          </el-input-number>
        </el-form-item>

        <el-form-item label="购买数量" required>
          <el-input-number
            v-model="calcForm.quantity"
            :min="1"
            style="width: 100%"
          >
            <template #append>件</template>
          </el-input-number>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleCalculate" :loading="calculating">
            计算会员价
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 计算结果 -->
      <el-card v-if="calcResult" class="result-card">
        <template #header>
          <div class="result-title">计算结果</div>
        </template>
        <div class="result-content">
          <div class="result-item">
            <span class="label">原价总额:</span>
            <span class="value">¥{{ (calcForm.originalPrice * calcForm.quantity).toFixed(2) }}</span>
          </div>
          <div class="result-item">
            <span class="label">会员单价:</span>
            <span class="value member-price">¥{{ (calcResult / 100).toFixed(2) }}</span>
          </div>
          <div class="result-item">
            <span class="label">会员总价:</span>
            <span class="value member-total">¥{{ ((calcResult / 100) * calcForm.quantity).toFixed(2) }}</span>
          </div>
          <div class="result-item savings">
            <span class="label">节省金额:</span>
            <span class="value">¥{{ calculateSavings().toFixed(2) }}</span>
          </div>
          <div class="result-item savings">
            <span class="label">节省比例:</span>
            <span class="value">{{ calculateSavingsRate().toFixed(1) }}%</span>
          </div>
        </div>
      </el-card>

      <!-- 适用条件检查 -->
      <el-alert
        v-if="!isApplicable()"
        title="注意：当前购买数量不符合配置的限制条件"
        type="warning"
        :closable="false"
        style="margin-top: 15px;"
      >
        <template #default>
          <div v-if="pricingConfig?.minPurchaseQuantity && calcForm.quantity < pricingConfig.minPurchaseQuantity">
            最小购买数量：{{ pricingConfig.minPurchaseQuantity }}
          </div>
          <div v-if="pricingConfig?.maxPurchaseQuantity && calcForm.quantity > pricingConfig.maxPurchaseQuantity">
            最大购买数量：{{ pricingConfig.maxPurchaseQuantity }}
          </div>
        </template>
      </el-alert>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { MemberPricingDTO } from '@/types/member-pricing'
import { memberPricingApi } from '@/api/member-pricing'

interface Props {
  modelValue: boolean
  pricingConfig: MemberPricingDTO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const calculating = ref(false)
const calcResult = ref<number | null>(null)

const calcForm = reactive({
  originalPrice: 100,
  quantity: 1
})

// 监听配置变化，重置计算结果
watch(
  () => props.pricingConfig,
  () => {
    calcResult.value = null
    calcForm.originalPrice = 100
    calcForm.quantity = Math.max(1, props.pricingConfig?.minPurchaseQuantity || 1)
  }
)

// 计算会员价格
const handleCalculate = async () => {
  if (!props.pricingConfig) return

  calculating.value = true
  try {
    const response = await memberPricingApi.calculate({
      productId: props.pricingConfig.productId!,
      memberLevel: props.pricingConfig.memberLevel!,
      originalPrice: Math.round(calcForm.originalPrice * 100), // 转换为分
      quantity: calcForm.quantity
    })

    if (response.success && response.data !== undefined) {
      calcResult.value = response.data
    } else {
      ElMessage.error('计算失败')
    }
  } catch (error) {
    ElMessage.error('计算失败')
  } finally {
    calculating.value = false
  }
}

// 检查是否适用
const isApplicable = () => {
  if (!props.pricingConfig) return true

  const { minPurchaseQuantity, maxPurchaseQuantity } = props.pricingConfig
  const { quantity } = calcForm

  if (minPurchaseQuantity && quantity < minPurchaseQuantity) return false
  if (maxPurchaseQuantity && quantity > maxPurchaseQuantity) return false

  return true
}

// 计算节省金额
const calculateSavings = () => {
  if (!calcResult.value) return 0
  const originalTotal = calcForm.originalPrice * calcForm.quantity
  const memberTotal = (calcResult.value / 100) * calcForm.quantity
  return originalTotal - memberTotal
}

// 计算节省比例
const calculateSavingsRate = () => {
  const savings = calculateSavings()
  const originalTotal = calcForm.originalPrice * calcForm.quantity
  return originalTotal > 0 ? (savings / originalTotal) * 100 : 0
}

// 格式化方法
const getMemberLevelType = (level?: string) => {
  const typeMap: Record<string, string> = {
    SILVER: '',
    GOLD: 'warning',
    PLATINUM: 'success',
    DIAMOND: 'danger'
  }
  return typeMap[level || ''] || ''
}

const getMemberLevelText = (level?: string) => {
  const textMap: Record<string, string> = {
    SILVER: '银卡',
    GOLD: '金卡',
    PLATINUM: '白金',
    DIAMOND: '钻石'
  }
  return textMap[level || ''] || level
}

const formatDiscountValue = (config?: MemberPricingDTO | null) => {
  if (!config) return ''
  
  switch (config.discountType) {
    case 'PERCENTAGE':
      return `${config.discountValue}%`
    case 'FIXED_AMOUNT':
      return `¥${(config.discountValue / 100).toFixed(2)}`
    case 'FIXED_PRICE':
      return `¥${(config.discountValue / 100).toFixed(2)}`
    default:
      return config.discountValue?.toString() || ''
  }
}
</script>

<style scoped>
.calculator-content {
  max-height: 70vh;
  overflow-y: auto;
}

.config-info {
  margin-bottom: 20px;
}

.config-title,
.result-title {
  font-weight: bold;
  font-size: 16px;
}

.config-item,
.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.config-item:last-child,
.result-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  font-weight: bold;
  color: #333;
}

.value.highlight {
  color: #409eff;
  font-size: 16px;
}

.result-card {
  margin-top: 20px;
}

.member-price {
  color: #67c23a;
  font-size: 16px;
}

.member-total {
  color: #67c23a;
  font-size: 18px;
}

.savings {
  border-top: 1px solid #eee;
  padding-top: 10px;
  margin-top: 10px;
}

.savings .value {
  color: #f56c6c;
  font-size: 16px;
}
</style>