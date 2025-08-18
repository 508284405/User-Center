<template>
  <div class="sku-management">
    <div class="sku-list">
      <el-table :data="skuList" border style="width: 100%" class="responsive-table">
        <el-table-column label="规格属性" min-width="200">
          <template #default="scope">
            <div class="attribute-tags">
              <el-tag
                v-for="(value, key) in scope.row.attributes"
                :key="key"
                class="mx-1"
                style="margin: 4px"
              >
                {{ key }}: {{ value }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.image"
              :src="scope.row.image"
              style="width: 60px; height: 60px; object-fit: cover;"
              :preview-src-list="[scope.row.image]"
            />
            <span v-else>无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150">
          <template #default="scope">
            {{ scope.row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" min-width="120">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="总库存" min-width="120">
          <template #default="scope">
            {{ scope.row.stock }}
          </template>
        </el-table-column>
        <el-table-column label="可用库存" min-width="120">
          <template #default="scope">
            {{ scope.row.availableStock }}
          </template>
        </el-table-column>
        <el-table-column label="冻结库存" min-width="120">
          <template #default="scope">
            {{ scope.row.frozenStock }}
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="重量(kg)" min-width="120" />
        <el-table-column prop="volume" label="体积(m³)" min-width="120" />
        <el-table-column label="操作" min-width="150">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="handleEditSku(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteSku(scope.$index)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="sku-actions">
        <el-button type="primary" @click="handleAddSku">添加SKU</el-button>
      </div>
    </div>

    <!-- SKU编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form :model="skuForm" :rules="rules" ref="skuFormRef" label-width="100px">
        <el-form-item label="规格属性" prop="attributes">
          <div v-for="(value, key, idx) in skuForm.attributes" :key="key" class="attribute-item">
            <el-input
              v-model="attributeEditKeys[key]"
              @blur="onAttributeKeyBlur(key, attributeEditKeys[key])"
              placeholder="属性名"
              style="width: 150px; margin-right: 10px"
            />
            <el-input
              v-model="skuForm.attributes[key]"
              :key="'attr-value-' + key"
              placeholder="属性值"
              style="width: 150px; margin-right: 10px"
            />
            <el-button type="danger" icon="Delete" circle @click="removeAttribute(key)" />
          </div>
          <el-button type="primary" plain @click="addAttribute">添加属性</el-button>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="skuForm.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="总库存" prop="stock" class="readonly-form-item">
          <el-input-number v-model="skuForm.stock" :min="0" disabled />
          <span class="form-item-hint">（不可在此编辑，请使用专门的库存管理功能）</span>
        </el-form-item>
        <el-form-item label="可用库存" class="readonly-form-item">
          <el-input-number v-model="skuForm.availableStock" :min="0" disabled />
          <span class="form-item-hint">（不可编辑）</span>
        </el-form-item>
        <el-form-item label="冻结库存" class="readonly-form-item">
          <el-input-number v-model="skuForm.frozenStock" :min="0" disabled />
          <span class="form-item-hint">（不可编辑）</span>
        </el-form-item>
        <el-form-item label="重量(kg)" prop="weight">
          <el-input-number v-model="skuForm.weight" :precision="2" :step="0.01" :min="0" />
        </el-form-item>
        <el-form-item label="体积(m³)" prop="volume">
          <el-input-number v-model="skuForm.volume" :precision="2" :step="0.01" :min="0" />
        </el-form-item>
        <el-form-item label="SKU图片" prop="image">
          <el-upload
            class="image-uploader"
            :auto-upload="false"
            :show-file-list="true"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :file-list="fileList"
            list-type="picture-card"
          >
            <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="SKU描述" prop="description">
          <el-input
            v-model="skuForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入SKU描述，不超过50个字"
            :maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSku">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.sku-management {
  width: 100%;
}

.responsive-table {
  margin-bottom: 1rem;
  overflow-x: auto;
}

.attribute-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sku-actions {
  margin-top: 1rem;
}

.attribute-item {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

@media screen and (max-width: 768px) {
  .attribute-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .attribute-item .el-input {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 8px;
  }

  .action-buttons {
    flex-direction: column;
    width: 100%;
  }

  .action-buttons .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>

<script setup lang="ts">
import { ref, reactive, defineProps, defineEmits, watch, computed, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { uploadImage } from '@/api/client-web/file';

import { ProductSku } from '@/api/client-web/product';

const props = defineProps<{
  modelValue: ProductSku[];
}>();

const emit = defineEmits(['update:modelValue']);

// SKU列表数据
type ProductSkuWithLocal = ProductSku & { localId?: number };
const skuList = ref<ProductSkuWithLocal[]>([]);

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  skuList.value = [...(newValue || [])];
}, { immediate: true, deep: true });

// 对话框相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const skuFormRef = ref();
const attributeEditKeys = reactive<Record<string, string>>({});

let localSkuIdSeed = 1;

// 用于记录编辑前的原始库存值
const originalStock = ref(0);

// SKU表单数据
const skuForm = reactive<ProductSkuWithLocal>({
  skuId: '',
  attributes: {},
  price: 0,
  stock: 0,
  availableStock: 0,
  frozenStock: 0,
  image: '',
  description: '',
  weight: 0,
  volume: 0,
  localId: undefined
});

// 文件列表
const fileList = computed(() => {
  return skuForm.image ? [{ url: skuForm.image, name: skuForm.image.split('/').pop() }] : [];
});

// 表单校验规则
const rules = {
  price: [
    { required: true, message: '请输入SKU价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入SKU库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' }
  ],
  attributes: [
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (Object.keys(value).length === 0) {
          callback(new Error('至少需要添加一个规格属性'));
        } else {
          const hasEmptyValue = Object.entries(value).some(([key, val]) => !key || !val);
          if (hasEmptyValue) {
            callback(new Error('规格属性的名称和值不能为空'));
          } else {
            callback();
          }
        }
      },
      trigger: 'blur'
    }
  ]
};

// 添加SKU
const handleAddSku = () => {
  dialogTitle.value = '添加SKU';
  
  // 重置原始库存值
  originalStock.value = 0;
  
  // 完全重置SKU表单
  // 创建全新对象代替修改原对象，避免引用问题
  const emptyAttributes = {}; // 创建新的空属性对象
  
  // 重置属性编辑键
  Object.keys(attributeEditKeys).forEach(key => {
    delete attributeEditKeys[key];
  });
  
  // 重置表单为全新对象
  Object.assign(skuForm, {
    skuId: '',
    attributes: emptyAttributes,
    price: 0,
    stock: 0,
    availableStock: 0,
    frozenStock: 0,
    image: '', // 这会间接重置fileList computed属性
    description: '',
    weight: 0,
    volume: 0,
    localId: localSkuIdSeed++
  });
  
  dialogVisible.value = true;
};

// 编辑SKU
const handleEditSku = (row: ProductSkuWithLocal) => {
  dialogTitle.value = '编辑SKU';
  
  // 在赋值前记录原始库存值
  originalStock.value = row.stock || 0;
  
  Object.assign(skuForm, JSON.parse(JSON.stringify(row)));
  
  // 确保可用库存和冻结库存字段存在
  if (skuForm.availableStock === undefined) skuForm.availableStock = 0;
  if (skuForm.frozenStock === undefined) skuForm.frozenStock = 0;
  
  // 若无localId则补充
  if (!skuForm.localId) skuForm.localId = localSkuIdSeed++;
  
  dialogVisible.value = true;
};

// 删除SKU
const handleDeleteSku = (index: number) => {
  ElMessageBox.confirm('确认删除该SKU吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    skuList.value.splice(index, 1);
    emit('update:modelValue', skuList.value);
    ElMessage.success('删除成功');
  });
};

// 添加属性
const addAttribute = () => {
  if (Object.keys(skuForm.attributes).length >= 5) {
    ElMessage.warning('最多添加5个规格属性');
    return;
  }
  let index = 1;
  let defaultKey = `属性${index}`;
  while (defaultKey in skuForm.attributes) {
    index++;
    defaultKey = `属性${index}`;
  }
  skuForm.attributes[defaultKey] = '';
  attributeEditKeys[defaultKey] = defaultKey;
  console.log('addAttribute', skuForm.attributes);
};

// 更新属性名
const onAttributeKeyBlur = (oldKey: string, newKey: string) => {
  console.log('onAttributeKeyBlur', oldKey, newKey);
  if (!newKey) {
    ElMessage.warning('属性名不能为空');
    attributeEditKeys[oldKey] = oldKey;
    return;
  }
  if (oldKey === newKey) return;
  if (skuForm.attributes[newKey]) {
    ElMessage.warning('属性名已存在');
    attributeEditKeys[oldKey] = oldKey;
    return;
  }
  // 浅拷贝，保持响应式引用稳定
  const newAttrs: Record<string, string> = {};
  Object.entries(skuForm.attributes).forEach(([k, v]) => {
    if (k === oldKey) {
      newAttrs[newKey] = v;
    } else {
      newAttrs[k] = v;
    }
  });
  skuForm.attributes = newAttrs;
  // 更新编辑key
  attributeEditKeys[newKey] = newKey;
  delete attributeEditKeys[oldKey];
  nextTick(() => {
    // 可选：自动聚焦到新input
  });
};

// 移除属性
const removeAttribute = (key: string) => {
  if (Object.keys(skuForm.attributes).length <= 1) {
    ElMessage.warning('至少保留一个规格属性');
    return;
  }
  delete skuForm.attributes[key];
  delete attributeEditKeys[key];
  console.log('removeAttribute', skuForm.attributes);
};

// 对话框关闭前的处理
const handleDialogClose = (done: () => void) => {
  ElMessageBox.confirm('确认关闭？未保存的数据将会丢失')
    .then(() => {
      done();
    })
    .catch(() => {});
};

// 图片选择处理
const handleImageChange = async (file: any) => {
  if (file && file.raw) {
    try {
      // 上传图片到服务器
      const uploadedImageUrl = await uploadImage(file.raw);
      // 更新表单中的图片
      skuForm.image = uploadedImageUrl;
      ElMessage.success('图片上传成功');
    } catch (error) {
      console.error('图片上传错误:', error);
      ElMessage.error(error instanceof Error ? error.message : '图片上传失败');
    }
  }
};

// 图片移除处理
const handleImageRemove = () => {
  skuForm.image = '';
};

// 提交SKU表单
const handleSubmitSku = async () => {
  if (!skuFormRef.value) return;
  
  await skuFormRef.value.validate((valid: boolean) => {
    if (valid) {
      // 验证属性是否填写完整
      const hasEmptyAttribute = Object.entries(skuForm.attributes).some(([key, value]) => !value);
      if (hasEmptyAttribute) {
        ElMessage.warning('请填写完整的规格属性');
        return;
      }

      // 根据 localId 或 skuId 判断是更新还是新增
      const existIndex = skuList.value.findIndex(item => 
        (skuForm.localId && item.localId === skuForm.localId) ||
        (skuForm.skuId && item.skuId === skuForm.skuId)
      );
      
      if (existIndex !== -1) {
        // 更新现有SKU
        
        // 不再处理库存变化，直接更新其他属性
        const currentSku = skuList.value[existIndex];
        const updatedSku = JSON.parse(JSON.stringify(skuForm));
        
        // 始终保留原始库存值
        delete updatedSku.stock; // 删除库存字段，不进行库存更新
        
        // 更新其他属性
        skuList.value[existIndex] = {
          ...currentSku,
          ...updatedSku
        };
        
        console.log('保持原库存值，库存管理已移至专门功能');
      } else {
        // 新增SKU，本地分配localId，新增时校验是否重复
        const duplicate = skuList.value.some(sku => {
          // 如果是同一个SKU（通过localId或skuId判断），则不算重复
          const isSameSku = (skuForm.localId && sku.localId === skuForm.localId) || 
                           (skuForm.skuId && sku.skuId === skuForm.skuId);
          
          // 只检查不同SKU的属性重复
          return !isSameSku && JSON.stringify(sku.attributes) === JSON.stringify(skuForm.attributes);
        });
        if (duplicate) {
          ElMessage.warning('已存在相同规格的SKU');
          return;
        }
        
        // 新增时使用默认库存值0，库存管理已移至专门功能
        const newSkuData = JSON.parse(JSON.stringify(skuForm));
        newSkuData.stock = 0; // 设置默认库存为0
        
        // 添加到列表
        skuList.value.push({
          ...newSkuData,
          skuId: null,
          localId: skuForm.localId || localSkuIdSeed++
        });
      }

      // 更新父组件中的数据
      emit('update:modelValue', skuList.value);
      dialogVisible.value = false;
      ElMessage.success(skuForm.skuId ? '更新成功' : '添加成功');
    }
  });
};

watch(() => skuForm.attributes, (attrs) => {
  // 保持编辑key与attributes同步
  Object.keys(attrs).forEach(key => {
    if (!(key in attributeEditKeys)) attributeEditKeys[key] = key;
  });
  Object.keys(attributeEditKeys).forEach(key => {
    if (!(key in attrs)) delete attributeEditKeys[key];
  });
}, { immediate: true, deep: true });
</script>

<style scoped>
.sku-management {
  margin-top: 20px;
}

.sku-actions {
  margin-top: 20px;
  text-align: right;
}

.attribute-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.mx-1 {
  margin: 0 4px;
}

.readonly-form-item {
  .el-input-number {
    opacity: 0.8;
  }
}

.form-item-hint {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>