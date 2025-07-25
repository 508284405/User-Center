<template>
  <el-dialog
    :title="title"
    v-model="dialogVisible"
    width="50%"
    :before-close="handleDialogClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品分类" prop="category">
        <el-tree-select
          v-model="form.category"
          :data="categoryTree"
          node-key="categoryId"
          :props="{
            label: 'name',
            children: 'children'
          }"
          placeholder="请选择商品分类"
          clearable
          default-expand-all
          @change="handleCategoryChange"
        />
      </el-form-item>
      <el-form-item label="商品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入商品描述"
        />
      </el-form-item>
      <el-form-item label="商品图片" prop="images">
        <el-upload
          class="image-uploader"
          :auto-upload="false"
          :show-file-list="true"
          :on-change="handleImageChange"
          :on-remove="handleImageRemove"
          :file-list="fileList"
          multiple
          list-type="picture-card"
        >
          <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number
          v-model="form.price"
          :precision="2"
          :step="0.1"
          :min="0"
        />
      </el-form-item>
      <el-form-item label="SKU管理">
        <sku-management v-model="form.skus" />
      </el-form-item>
      <el-form-item label="标签" prop="tags">
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请输入商品标签"
        >
          <el-option
            v-for="item in form.tags"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, defineProps, defineEmits, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import SkuManagement from './SkuManagement.vue';
import { uploadImage } from '@/api/client-web/file';
import { createProduct, updateProduct, type Product } from '@/api/client-web/product';

interface Props {
  modelValue: boolean;
  product?: Product | null;
  categoryTree: any[];
  title: string;
}

const props = defineProps<Props>();

const emit = defineEmits(['update:modelValue', 'submit-success']);

// 对话框可见性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

// 表单引用
const formRef = ref();

// 表单数据
const form = reactive<Product>({
  name: '',
  description: '',
  price: 0,
  category: '',
  categoryId: '',
  status: 1,
  images: [], 
  warningStock: 0,
  tags: [],
  specifications: [],
  skus: []
});

// 文件列表
const fileList = computed(() => {
  return (form.images || []).map(url => ({ url, name: url.split('/').pop() }));
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择商品状态', trigger: 'change' }]
};

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    description: '',
    price: 0,
    stock: 0,
    category: '',
    categoryId: '',
    status: 1,
    images: [],
    warningStock: 0,
    tags: [],
    specifications: [],
    skus: []
  });
};

// 监听产品数据变化
watch(() => props.product, (newProduct) => {
  if (newProduct) {
    // 重置表单数据
    resetForm();
    
    // 设置新的表单数据
    Object.assign(form, {
      ...newProduct,
      category: newProduct.categoryId, // 使用categoryId作为分类值
      categoryId: newProduct.categoryId,
      images: newProduct.images || [], // 确保images是一个数组
      skus: newProduct.skus || [] // 确保skus数据正确传递
    });
  } else {
    resetForm();
  }
}, { immediate: true, deep: true });

// 监听对话框打开状态变化
watch(() => props.modelValue, (visible) => {
  if (visible && !props.product) {
    // 如果是打开对话框且没有传入商品数据（新增模式），则重置表单
    resetForm();
    // 文件列表会通过resetForm自动清空，因为它依赖于form.images
  }
});

// 分类变更处理
const handleCategoryChange = (value: string | number) => {
  form.categoryId = value;
};

// 定义上传文件类型接口
interface UploadFile {
  raw: File;
}

// 图片选择处理
const handleImageChange = async (file: UploadFile) => {
  if (file && file.raw) {
    try {
      // 上传图片到服务器
      const uploadedImageUrl = await uploadImage(file.raw);
      // 更新表单中的图片数组
      if (!form.images) {
        form.images = [];
      }
      form.images.push(uploadedImageUrl);
      ElMessage.success('图片上传成功');
    } catch (error) {
      console.error('图片上传错误:', error);
      ElMessage.error(error instanceof Error ? error.message : '图片上传失败');
    }
  }
};

// 图片移除处理
const handleImageRemove = (file: any) => {
  const index = form.images.indexOf(file.url);
  if (index > -1) {
    form.images.splice(index, 1);
  }
};

// 对话框关闭前的处理
const handleDialogClose = (done: () => void) => {
  ElMessageBox.confirm('确认关闭？未保存的数据将会丢失')
    .then(() => {
      resetForm();
      done();
    })
    .catch(() => {
      // 取消关闭
    });
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
};

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 验证必填字段
        if (!form.name || !form.category || !form.price || form.status === undefined) {
          ElMessage.warning('请填写所有必填字段');
          return;
        }

        // 准备提交的数据
        const submitData = {
          ...form,
          tags: Array.isArray(form.tags) ? form.tags : [],
          specifications: Array.isArray(form.specifications) ? form.specifications : []
        };

        if (form.id) {
          await updateProduct(form.id, submitData);
          ElMessage.success('更新成功');
        } else {
          await createProduct(submitData);
          ElMessage.success('创建成功');
        }
        
        dialogVisible.value = false;
        emit('submit-success');
      } catch (error) {
        ElMessage.error('操作失败');
      }
    }
  });
};
</script>

<style scoped>
.image-uploader .el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.image-uploader .el-upload-list--picture-card .el-upload-list__item {
  width: 100px;
  height: 100px;
}

.image-uploader .avatar-uploader-icon {
  font-size: 24px;
  width: 100px;
  height: 100px;
  line-height: 100px;
}
</style>