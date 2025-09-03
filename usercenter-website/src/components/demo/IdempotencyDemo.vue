<template>
  <div class="idempotency-demo">
    <el-card header="幂等性系统演示">
      <!-- 订单创建演示 -->
      <div class="demo-section">
        <h3>订单创建演示</h3>
        <el-form :model="orderForm" inline>
          <el-form-item label="用户ID">
            <el-input v-model="orderForm.userId" placeholder="请输入用户ID" />
          </el-form-item>
          <el-form-item label="商品ID">
            <el-input v-model="orderForm.productId" placeholder="请输入商品ID" />
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="orderForm.quantity" :min="1" />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="orderSubmission.isSubmitting.value"
              :disabled="!orderSubmission.canSubmit.value"
              @click="handleCreateOrder"
            >
              <span v-if="orderSubmission.isInDebounceTime.value">
                请勿重复提交 ({{ remainingDebounceTime }}s)
              </span>
              <span v-else>
                {{ orderSubmission.isSubmitting.value ? '创建中...' : '创建订单' }}
              </span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 结果显示 -->
        <div v-if="orderSubmission.submitResult.value" class="result-section">
          <el-alert type="success" title="创建成功" :closable="false">
            <p>订单号：{{ orderSubmission.submitResult.value }}</p>
          </el-alert>
        </div>
        
        <div v-if="orderSubmission.submitError.value" class="result-section">
          <el-alert type="error" title="创建失败" :closable="false">
            <p>{{ orderSubmission.submitError.value }}</p>
          </el-alert>
        </div>
      </div>
      
      <!-- 手动幂等Token管理 -->
      <el-divider />
      
      <div class="demo-section">
        <h3>手动幂等Token管理</h3>
        <el-form :model="tokenForm" inline>
          <el-form-item label="作用域">
            <el-select v-model="tokenForm.scope" placeholder="请选择作用域">
              <el-option label="订单创建" value="create-order" />
              <el-option label="支付处理" value="payment-process" />
              <el-option label="库存操作" value="inventory-deduct" />
            </el-select>
          </el-form-item>
          <el-form-item label="上下文">
            <el-input v-model="tokenForm.context" placeholder="可选" />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleGenerateToken">生成Token</el-button>
            <el-button @click="handleClearTokens">清理所有Token</el-button>
          </el-form-item>
        </el-form>
        
        <!-- Token显示 -->
        <div v-if="generatedToken" class="token-display">
          <el-input
            v-model="generatedToken"
            readonly
            type="textarea"
            :rows="2"
            placeholder="生成的Token将显示在这里"
          >
            <template #append>
              <el-button @click="copyToken">复制</el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <!-- 系统状态 -->
      <el-divider />
      
      <div class="demo-section">
        <h3>系统状态</h3>
        <div class="status-grid">
          <el-card class="status-card">
            <template #header>
              <span>当前状态</span>
            </template>
            <div class="status-item">
              <span class="label">提交中：</span>
              <el-tag :type="orderSubmission.isSubmitting.value ? 'danger' : 'success'">
                {{ orderSubmission.isSubmitting.value ? '是' : '否' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="label">可提交：</span>
              <el-tag :type="orderSubmission.canSubmit.value ? 'success' : 'warning'">
                {{ orderSubmission.canSubmit.value ? '可以' : '不可以' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="label">防抦中：</span>
              <el-tag :type="orderSubmission.isInDebounceTime.value ? 'warning' : 'info'">
                {{ orderSubmission.isInDebounceTime.value ? '是' : '否' }}
              </el-tag>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useOrderSubmission, useIdempotency } from '@/composables/useIdempotency';

// 订单表单数据
const orderForm = ref({
  userId: '1001',
  productId: 'P001',
  quantity: 1
});

// Token表单数据
const tokenForm = ref({
  scope: 'create-order',
  context: ''
});

// 生成的Token
const generatedToken = ref('');

// 使用订单提交组合式函数
const orderSubmission = useOrderSubmission();

// 通用幂等性组合式函数
const idempotency = useIdempotency();

// 倒计时器
const remainingDebounceTime = ref(0);
let debounceTimer: NodeJS.Timeout | null = null;

// 计算剩余防抦时间
const updateDebounceTimer = () => {
  if (orderSubmission.isInDebounceTime.value) {
    const elapsed = Date.now() - (orderSubmission.lastSubmitTime?.value || 0);
    const remaining = Math.max(0, Math.ceil((orderSubmission.debounceTime.value - elapsed) / 1000));
    remainingDebounceTime.value = remaining;
    
    if (remaining > 0) {
      debounceTimer = setTimeout(updateDebounceTimer, 100);
    }
  }
};

// 创建订单
const handleCreateOrder = async () => {
  try {
    await orderSubmission.createOrder(orderForm.value);
    updateDebounceTimer();
  } catch (error) {
    console.error('订单创建失败:', error);
  }
};

// 生成Token
const handleGenerateToken = async () => {
  try {
    const token = await idempotency.getIdempotencyToken(
      tokenForm.value.scope,
      tokenForm.value.context || undefined
    );
    generatedToken.value = token;
    ElMessage.success('Token生成成功');
  } catch (error) {
    ElMessage.error('Token生成失败');
  }
};

// 清理Token
const handleClearTokens = () => {
  idempotency.clearIdempotencyTokens();
  generatedToken.value = '';
};

// 复制Token
const copyToken = async () => {
  try {
    await navigator.clipboard.writeText(generatedToken.value);
    ElMessage.success('复制成功');
  } catch (error) {
    ElMessage.error('复制失败');
  }
};

// 组件清理
onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
});
</script>

<style scoped>
.idempotency-demo {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.demo-section {
  margin-bottom: 30px;
}

.demo-section h3 {
  color: #409eff;
  margin-bottom: 15px;
}

.result-section {
  margin-top: 15px;
}

.token-display {
  margin-top: 15px;
}

.status-grid {
  display: grid;
  gap: 20px;
}

.status-card {
  .status-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .label {
      font-weight: 500;
      color: #606266;
    }
  }
}

.el-form--inline .el-form-item {
  margin-right: 15px;
}

.el-button:disabled {
  opacity: 0.6;
}
</style>