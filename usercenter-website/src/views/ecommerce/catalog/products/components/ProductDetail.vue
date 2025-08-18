<template>
  <el-dialog
    title="商品详情"
    v-model="dialogVisible"
    width="60%"
    :before-close="handleClose"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品ID">{{ product?.id }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ product?.name }}</el-descriptions-item>
          <el-descriptions-item label="商品分类">{{ product?.category }}</el-descriptions-item>
          <el-descriptions-item label="商品状态">
            <el-tag :type="product?.status === 1 ? 'success' : product?.status === 0 ? 'info' : 'warning'">
              {{ product?.status === 0 ? '草稿' : product?.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ product?.price }}</el-descriptions-item>
          <el-descriptions-item label="商品描述">{{ product?.description }}</el-descriptions-item>
          <el-descriptions-item label="商品图片">
            <div class="product-images">
              <el-image
                v-for="(image, index) in product?.images"
                :key="index"
                :src="image"
                :preview-src-list="product?.images"
                style="width: 100px; height: 100px; margin-right: 10px;"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
      <el-tab-pane label="SKU管理" name="sku">
        <div class="sku-list">
          <el-table :data="product?.skus || []" border style="width: 100%">
            <el-table-column prop="skuId" label="SKU ID" />
            <el-table-column prop="skuCode" label="SKU编码" />
            <el-table-column label="规格">
              <template #default="scope">
                {{ Object.entries<Record<string, string>>(scope.row.attributes || {}).map(([key, value]) => `${key}:${value}`).join(', ') }}
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column label="总库存">
              <template #default="scope">
                <div class="stock-edit-container">
                  <span>{{ scope.row.stock }}</span>
                  <el-button 
                    type="primary" 
                    size="small" 
                    circle 
                    icon="Edit" 
                    @click="showStockEdit(scope.row)"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="availableStock" label="可用库存" />
            <el-table-column prop="frozenStock" label="冻结库存" />
            <el-table-column prop="weight" label="重量(kg)" />
            <el-table-column prop="volume" label="体积(m³)" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>

  <!-- 库存编辑对话框 -->
  <el-dialog
    title="编辑库存"
    v-model="stockDialogVisible"
    width="400px"
  >
    <div v-if="currentEditSku" class="stock-edit-dialog">
      <div class="stock-info">
        <div class="info-row">
          <span class="label">规格:</span>
          <span class="value">{{ Object.entries(currentEditSku.attributes || {}).map(([key, value]) => `${key}:${value}`).join(', ') }}</span>
        </div>
        <div class="info-row">
          <span class="label">当前库存:</span>
          <span class="value">{{ currentEditSku.stock }}</span>
        </div>
        <div class="info-row">
          <span class="label">可用库存:</span>
          <span class="value">{{ currentEditSku.availableStock }}</span>
        </div>
      </div>
      
      <div class="stock-change">
        <span class="label">库存变化量:</span>
        <el-input-number v-model="stockChange" :min="-currentEditSku.availableStock" placeholder="正数增加库存，负数减少库存" />
      </div>
      
      <div class="stock-result">
        <span class="label">编辑后库存:</span>
        <span class="value result">{{ currentEditSku.stock + stockChange }}</span>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStock">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, computed } from 'vue';
import type { Product } from '@/api/client-web/product';
import { updateSkuInventory } from '@/api/client-web/product';
import { ElMessage } from 'element-plus';

interface Props {
  modelValue: boolean;
  product: Product | null;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

// 对话框可见性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

// 当前激活的标签页
const activeTab = ref('basic');

// 库存编辑对话框可见性
const stockDialogVisible = ref(false);

// 当前编辑的SKU
const currentEditSku = ref<any>(null);

// 库存变化值
const stockChange = ref<number>(0);

// 显示库存编辑对话框
const showStockEdit = (sku: any) => {
  currentEditSku.value = sku;
  stockChange.value = 0; // 重置库存变化值
  stockDialogVisible.value = true;
};

// 确认更新库存
const confirmUpdateStock = async () => {
  if (!currentEditSku.value || !props.product) return;
  
  try {
    // 调用更新库存API函数
    await updateSkuInventory(
      props.product.id as number,
      currentEditSku.value.skuId as number,
      stockChange.value
    );
    
    // 更新前端数据
    currentEditSku.value.stock += stockChange.value;
    stockDialogVisible.value = false;
    
    ElMessage.success('库存更新成功');
  } catch (error) {
    console.error('更新库存失败:', error);
    ElMessage.error('更新库存失败');
  }
};

// 关闭对话框
const handleClose = () => {
  activeTab.value = 'basic';
  emit('update:modelValue', false);
};
</script>

<style scoped>
.product-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sku-list {
  margin-top: 20px;
}

.stock-edit-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stock-edit-dialog {
  padding: 10px;
}

.stock-info, .stock-change, .stock-result {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.label {
  font-weight: bold;
  min-width: 100px;
}

.value {
  flex: 1;
}

.result {
  font-weight: bold;
  color: #409EFF;
}
</style>