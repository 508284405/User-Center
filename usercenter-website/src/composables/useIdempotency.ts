import { ref, computed, readonly } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getIdempotencyTokenForScope, clearAllIdempotencyTokens } from '@/utils/idempotencyInterceptor';
import type { IdempotentState, IdempotentStatus } from '@/api/idempotency';
import { createOrder } from '@/api/client-web/order';

/**
 * 幂等性相关的Vue组合式函数
 */
export function useIdempotency() {
  // 响应式状态
  const isSubmitting = ref(false);
  const lastSubmitTime = ref<number | null>(null);
  const submitResult = ref<any>(null);
  const submitError = ref<string | null>(null);
  
  // 防抦时间（毫秒）
  const debounceTime = ref(500);
  
  /**
   * 是否在防抦期内
   */
  const isInDebounceTime = computed(() => {
    if (!lastSubmitTime.value) return false;
    return Date.now() - lastSubmitTime.value < debounceTime.value;
  });
  
  /**
   * 是否可以提交
   */
  const canSubmit = computed(() => {
    return !isSubmitting.value && !isInDebounceTime.value;
  });
  
  /**
   * 执行幂等操作
   */
  const executeIdempotentAction = async <T>(
    actionFn: () => Promise<T>,
    options?: {
      scope?: string;
      context?: string;
      showLoading?: boolean;
      successMessage?: string;
      errorMessage?: string;
      onSuccess?: (result: T) => void;
      onError?: (error: any) => void;
    }
  ): Promise<T | null> => {
    // 检查是否可以提交
    if (!canSubmit.value) {
      console.warn('操作被防抦限制');
      return null;
    }
    
    // 重置状态
    submitResult.value = null;
    submitError.value = null;
    
    try {
      isSubmitting.value = true;
      lastSubmitTime.value = Date.now();
      
      if (options?.showLoading !== false) {
        // 可以在这里显示加载状态
      }
      
      // 执行实际操作
      const result = await actionFn();
      
      submitResult.value = result;
      
      // 成功回调
      if (options?.onSuccess) {
        options.onSuccess(result);
      }
      
      // 成功提示
      if (options?.successMessage) {
        ElMessage.success(options.successMessage);
      }
      
      return result;
      
    } catch (error: any) {
      submitError.value = error.message || '操作失败';
      
      // 错误回调
      if (options?.onError) {
        options.onError(error);
      }
      
      // 错误提示
      const errorMsg = options?.errorMessage || error.message || '操作失败';
      ElMessage.error(errorMsg);
      
      throw error;
      
    } finally {
      isSubmitting.value = false;
    }
  };
  
  /**
   * 手动获取幂等Token
   */
  const getIdempotencyToken = async (scope: string, context?: string): Promise<string> => {
    try {
      return await getIdempotencyTokenForScope(scope, context);
    } catch (error) {
      console.error('获取幂等Token失败:', error);
      throw error;
    }
  };
  
  /**
   * 清理所有幂等Token
   */
  const clearIdempotencyTokens = (): void => {
    clearAllIdempotencyTokens();
    ElMessage.info('幂等Token已清理');
  };
  
  /**
   * 重置状态
   */
  const resetState = (): void => {
    isSubmitting.value = false;
    lastSubmitTime.value = null;
    submitResult.value = null;
    submitError.value = null;
  };
  
  /**
   * 安全提交表单（防止重复提交）
   */
  const safeSubmit = async <T>(
    submitFn: () => Promise<T>,
    options?: {
      scope?: string;
      context?: string;
      confirmMessage?: string;
      successMessage?: string;
      errorMessage?: string;
    }
  ): Promise<T | null> => {
    // 确认对话框
    if (options?.confirmMessage) {
      try {
        await ElMessageBox.confirm(options.confirmMessage, '确认操作', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
      } catch {
        return null; // 用户取消
      }
    }
    
    return executeIdempotentAction(submitFn, {
      ...options,
      showLoading: true
    });
  };
  
  return {
    // 状态
    isSubmitting: readonly(isSubmitting),
    canSubmit: readonly(canSubmit),
    isInDebounceTime: readonly(isInDebounceTime),
    submitResult: readonly(submitResult),
    submitError: readonly(submitError),
    debounceTime,
    
    // 方法
    executeIdempotentAction,
    getIdempotencyToken,
    clearIdempotencyTokens,
    resetState,
    safeSubmit
  };
}

/**
 * 专用于订单创建的组合式函数
 */
export function useOrderSubmission() {
  const idempotency = useIdempotency();
  
  const createOrderIdempotent = async (orderData: any) => {
    return idempotency.executeIdempotentAction(
      () => {
        // 这里会自动添加幂等头
        return createOrder(orderData);
      },
      {
        scope: 'create-order',
        context: `user_${orderData.userId}`,
        successMessage: '订单创建成功',
        errorMessage: '订单创建失败'
      }
    );
  };
  
  return {
    ...idempotency,
    createOrder: createOrderIdempotent
  };
}

/**
 * 专用于支付的组合式函数
 */
export function usePaymentSubmission() {
  const idempotency = useIdempotency();
  
  const processPayment = async (paymentData: any) => {
    return idempotency.safeSubmit(
      () => {
        return processPayment(paymentData);
      },
      {
        scope: 'payment-process',
        context: `order_${paymentData.orderId}`,
        confirmMessage: '确认要进行支付操作吗？',
        successMessage: '支付成功',
        errorMessage: '支付失败'
      }
    );
  };
  
  return {
    ...idempotency,
    processPayment
  };
}