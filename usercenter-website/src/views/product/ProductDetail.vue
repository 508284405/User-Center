<template>
  <div class="product-detail">
    <el-page-header @back="goBack" :title="product?.name || '商品详情'">
      <template #content>
        <span class="text-large font-600 mr-3"> {{ product?.name }} </span>
      </template>
    </el-page-header>

    <div class="detail-content">
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
          </el-descriptions>

          <div class="product-images">
            <h3>商品图片</h3>
            <div class="image-list">
              <div v-for="(image, index) in product?.images" :key="index" class="image-item">
                <el-image :src="image" :preview-src-list="product?.images">
                  <template #error>
                    <div class="image-slot">加载失败</div>
                  </template>
                </el-image>
                <div class="image-actions">
                  <el-button type="danger" size="small" @click="handleDeleteImage(index)">删除</el-button>
                </div>
              </div>
              <div class="image-upload">
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeImageUpload"
                  :http-request="handleImageUpload"
                >
                  <el-button type="primary">上传图片</el-button>
                </el-upload>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="SKU管理" name="sku">
          <div class="sku-management">
            <div class="sku-header">
              <h3>SKU列表</h3>
              <el-button type="primary" @click="handleAddSku">新增SKU</el-button>
            </div>
            <el-table :data="product?.skus || []" border style="width: 100%">
              <el-table-column label="规格属性" min-width="200">
                <template #default="scope">
                  <div class="attribute-tags">
                    <el-tag
                      v-for="(value, key) in scope.row.attributes"
                      :key="key"
                      class="mx-1"
                      type="info"
                      effect="plain"
                    >
                      {{ key }}: {{ value }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" min-width="120">
                <template #default="scope">
                  <span class="price">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" min-width="120">
                <template #default="scope">
                  <span :class="{ 'warning-stock': scope.row.stock <= (product?.warningStock || 0) }">
                    {{ scope.row.stock }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button-group>
                    <el-button size="small" type="primary" @click="handleEditSku(scope.row)">
                      编辑
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      @click="handleDeleteSku(scope.$index)"
                      :disabled="(product?.skus?.length || 0) <= 1"
                    >
                      删除
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- SKU编辑对话框 -->
    <el-dialog
      :title="skuDialogTitle"
      v-model="skuDialogVisible"
      width="500px"
    >
      <el-form :model="skuForm" :rules="skuRules" ref="skuFormRef" label-width="100px">
        <el-form-item label="规格属性" prop="attributes">
          <div v-for="(value, key) in skuForm.attributes" :key="key" class="attribute-item">
            <el-input :value="key" @input="(val: string) => updateAttributeKey(key, val)" placeholder="属性名" style="width: 150px; margin-right: 10px" />
            <el-input v-model="skuForm.attributes[key]" placeholder="属性值" style="width: 150px; margin-right: 10px" />
            <el-button type="danger" icon="Delete" circle @click="removeAttribute(key)" />
          </div>
          <el-button type="primary" plain @click="addAttribute">添加属性</el-button>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="skuForm.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="skuForm.stock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="skuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSkuSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getProduct, updateProduct, type Product, type ApiResponse, type ProductSku } from '@/api/client-web/product';
import { uploadImage, type FileUploadResponse } from '@/api/client-web/file';

interface FileUploadResult {
  fileUrl: string;
  fileId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
}

const route = useRoute();
const router = useRouter();
const productId = route.params.id as string;

// 商品数据
const product = ref<Product | null>(null);
const activeTab = ref('basic');

// SKU表单相关
const skuDialogVisible = ref(false);
const skuDialogTitle = ref('');
const skuFormRef = ref();
const skuForm = ref<ProductSku>({
  skuId: '',
  attributes: {},
  price: 0,
  stock: 0
});

const skuRules = {
  attributes: [{ required: true, message: '请输入规格信息', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
};

// 获取商品详情
const fetchProductDetail = async () => {
  try {
    const res = await getProduct(Number(productId));
    if (res.success) {
      product.value = res.data;
      if (!product.value.images) {
        product.value.images = [];
      }
      if (!product.value.skus) {
        product.value.skus = [];
      }
    } else {
      ElMessage.error(res.errMessage || '获取商品详情失败');
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败');
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 图片上传相关
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleImageUpload = async (options: any) => {
  try {
    const file = options.file;
    const fileUrl = await uploadImage(file);
    if (product.value) {
      product.value.images.push(fileUrl);
      await updateProduct(Number(productId), product.value);
      ElMessage.success('图片上传成功');
    }
  } catch (error) {
    ElMessage.error('图片上传失败');
  }
};

const handleDeleteImage = async (index: number) => {
  try {
    if (product.value) {
      product.value.images.splice(index, 1);
      await updateProduct(Number(productId), product.value);
      ElMessage.success('图片删除成功');
    }
  } catch (error) {
    ElMessage.error('图片删除失败');
  }
};

// SKU管理相关
const handleAddSku = () => {
  skuDialogTitle.value = '新增SKU';
  skuForm.value = {
    skuId: '',
    attributes: {},
    price: 0,
    stock: 0
  };
  skuDialogVisible.value = true;
};

const handleEditSku = (sku: any) => {
  skuDialogTitle.value = '编辑SKU';
  skuForm.value = { ...sku };
  skuDialogVisible.value = true;
};

const handleDeleteSku = async (index: number) => {
  try {
    if (product.value) {
      product.value.skus.splice(index, 1);
      await updateProduct(Number(productId), product.value);
      ElMessage.success('SKU删除成功');
    }
  } catch (error) {
    ElMessage.error('SKU删除失败');
  }
};

const handleSkuSubmit = async () => {
  if (!skuFormRef.value) return;
  await skuFormRef.value.validate(async (valid: boolean) => {
    if (valid && product.value) {
      try {
        if (skuDialogTitle.value === '新增SKU') {
          product.value.skus.push({ ...skuForm.value });
        } else {
          const index = product.value.skus.findIndex((s: any) => s.skuId === skuForm.value.skuId);
          if (index !== -1) {
            product.value.skus[index] = { ...skuForm.value };
          }
        }
        await updateProduct(Number(productId), product.value);
        ElMessage.success(skuDialogTitle.value === '新增SKU' ? 'SKU添加成功' : 'SKU更新成功');
        skuDialogVisible.value = false;
      } catch (error) {
        ElMessage.error('操作失败');
      }
    }
  });
};

// SKU管理相关方法
const addAttribute = () => {
  if (Object.keys(skuForm.value.attributes).length >= 5) {
    ElMessage.warning('最多添加5个规格属性');
    return;
  }
  const defaultKey = `属性${Object.keys(skuForm.value.attributes).length + 1}`;
  skuForm.value.attributes[defaultKey] = '';
};

const updateAttributeKey = (oldKey: string, newKey: string) => {
  if (!newKey) {
    ElMessage.warning('属性名不能为空');
    return;
  }
  if (oldKey === newKey) return;
  if (skuForm.value.attributes[newKey]) {
    ElMessage.warning('属性名已存在');
    return;
  }
  const value = skuForm.value.attributes[oldKey];
  delete skuForm.value.attributes[oldKey];
  skuForm.value.attributes[newKey] = value;
};

const removeAttribute = (key: string) => {
  if (Object.keys(skuForm.value.attributes).length <= 1) {
    ElMessage.warning('至少保留一个规格属性');
    return;
  }
  delete skuForm.value.attributes[key];
};

onMounted(() => {
  fetchProductDetail();
});
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.detail-content {
  margin-top: 20px;
}

.product-images {
  margin-top: 20px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 10px;
}

.image-item {
  position: relative;
  width: 200px;
}

.image-item .el-image {
  width: 100%;
  height: 200px;
  border-radius: 4px;
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.image-upload {
  width: 200px;
  height: 200px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.sku-management {
  margin-top: 20px;
}

.sku-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.warning-stock {
  color: #e6a23c;
  font-weight: bold;
}

.attribute-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.attribute-tags .el-tag {
  margin: 2px;
}

.el-button-group {
  display: flex;
  gap: 8px;
}
</style>