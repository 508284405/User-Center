<template>
  <div class="points-product-list">
    <div class="header">
      <h2>积分商品管理</h2>
      <div class="search-container">
        <el-input
          v-model="searchForm.name"
          placeholder="请输入商品名称"
          clearable
          @clear="handleSearch"
          style="width: 250px; margin-right: 10px"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增积分商品</el-button>
      </div>
    </div>

    <el-table :data="productList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品图片" width="120">
        <template #default="scope">
          <el-image
            v-if="scope.row.product && scope.row.product.images && scope.row.product.images.length > 0"
            :src="scope.row.product.images[0]"
            style="width: 80px; height: 80px"
            :preview-src-list="scope.row.product.images"
          >
            <template #error>
              <div class="image-slot">加载失败</div>
            </template>
          </el-image>
          <div v-else class="no-image">暂无图片</div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.enabled)">
            {{ getStatusText(scope.row.enabled) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button
            size="small"
            :type="scope.row.enabled ? 'warning' : 'success'"
            @click="handleStatusChange(scope.row)"
            v-if="scope.row.enabled !== null"
          >
            {{ scope.row.enabled ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 积分商品表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="650px"
      :before-close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="选择商品" prop="productId">
          <el-select
            v-model="form.productId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入商品名称搜索"
            :remote-method="remoteProductSearch"
            :loading="productSearchLoading"
            @change="handleProductChange"
            @focus="handleProductFocus"
            style="width: 100%"
          >
            <el-option
              v-for="item in productOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <div style="display: flex; align-items: center">
                <el-image
                  v-if="item.images && item.images.length > 0"
                  :src="item.images[0]"
                  style="width: 30px; height: 30px; margin-right: 10px"
                ></el-image>
                <span>{{ item.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择SKU" prop="skuIds" v-if="form.productId && skuOptions.length > 0">
          <el-select
            v-model="selectedSkuIds"
            multiple
            placeholder="请选择商品规格"
            @change="handleSkuChange"
            style="width: 100%"
          >
            <el-option
              v-for="item in skuOptions"
              :key="item.skuId"
              :label="getSkuLabel(item)"
              :value="item.skuId"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>
                  {{ getSkuLabel(item) }}
                </span>
                <span style="color: #ff4500">
                  ¥{{ item.price }} / 库存: {{ item.stock }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <!-- SKU积分价格和兑换限制设置 -->
        <template v-if="form.skuExchangeInfoList && form.skuExchangeInfoList.length > 0">
          <el-divider content-position="left">SKU积分设置</el-divider>
          <div v-for="skuInfo in form.skuExchangeInfoList" :key="skuInfo.skuId" class="sku-points-setting">
            <div class="sku-info">
              <span class="sku-label">{{ getSkuLabel(skuOptions.find(item => item.skuId === skuInfo.skuId)) }}</span>
            </div>
            <el-form-item :label="'积分价格'" :prop="`skuExchangeInfoList.${form.skuExchangeInfoList.indexOf(skuInfo)}.points`">
              <el-input-number
                v-model="skuInfo.points"
                :min="1"
                :step="100"
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item :label="'兑换限制'" :prop="`skuExchangeInfoList.${form.skuExchangeInfoList.indexOf(skuInfo)}.exchangeLimit`">
              <el-input-number
                v-model="skuInfo.exchangeLimit"
                :min="0"
                :step="1"
                style="width: 200px"
              />
              <div class="form-tip">每人可兑换数量限制，0表示不限制</div>
            </el-form-item>
          </div>
        </template>

        <el-form-item label="兑换时间" prop="startTime,endTime">
          <el-date-picker
            v-model="exchangeTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            @change="handleExchangeTimeChange"
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-radio-group v-model="form.enabled">
            <el-radio :label="true">上架</el-radio>
            <el-radio :label="false">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getProductList, getProduct, type Product, type ProductSku } from '@/api/client-web/product';
import { getPointsProductList, createPointsProduct, deletePointsProduct, updatePointsProductStatus, getPointsProduct, type PointsProduct } from '@/api/client-web/pointsProduct';

// 搜索表单
const searchForm = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
});

// 表格数据
const productList = ref<PointsProduct[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 对话框相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const formRef = ref();

// 表单数据
const exchangeTime = ref<[string, string]>(['', '']);

const form = reactive<PointsProduct>({
  productId: 0,
  skuExchangeInfoList: [],
  points: 1000,
  exchangeLimit: 0,
  hasSpec: false,
  enabled: true,
  startTime: '',
  endTime: ''
});

// 处理兑换时间变更
const handleExchangeTimeChange = (val: [string, string] | null) => {
  if (val) {
    form.startTime = val[0];
    form.endTime = val[1];
    // 手动触发表单验证
    if (formRef.value) {
      formRef.value.validateField(['startTime', 'endTime']);
    }
  } else {
    form.startTime = '';
    form.endTime = '';
  }
};

// 表单校验规则
const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  points: [
    { required: true, message: '请输入积分价格', trigger: 'blur' },
    { type: 'number', min: 1, message: '积分价格必须大于0', trigger: 'blur' }
  ],
  exchangeLimit: [
    { type: 'number', min: 0, message: '可兑换数量不能小于0', trigger: 'blur' }
  ],
  enabled: [{ required: true, message: '请选择状态', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
};


// SKU选择变更
const handleSkuChange = (skuIds: number[]) => {
  // 确保skuIds是数组
  const newSkuIds = Array.isArray(skuIds) ? skuIds : [];
  selectedSkuIds.value = newSkuIds;

  // 保留选中的SKU设置，移除未选中的
  form.skuExchangeInfoList = form.skuExchangeInfoList.filter(item => 
    newSkuIds.includes(item.skuId)
  );
  
  // 添加新选中的SKU设置
  const defaultPointsPrice = form.points || 1000;
  const defaultExchangeLimit = form.exchangeLimit || 0;
  
  newSkuIds.forEach(skuId => {
    if (!form.skuExchangeInfoList.some(item => item.skuId === skuId)) {
      const sku = skuOptions.value.find(item => Number(item.skuId) === skuId);
      if (sku) {
        form.skuExchangeInfoList.push({
          skuId,
          skuCode: '',
          points: defaultPointsPrice,
          exchangeLimit: defaultExchangeLimit,
          enabled: true
        });
      }
    }
  });
  
  // 更新hasSpec标志
  form.hasSpec = form.skuExchangeInfoList.length > 0;
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  console.log('表单数据：', form);
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    // 验证SKU设置
    if (form.hasSpec && (!form.skuExchangeInfoList || form.skuExchangeInfoList.length === 0)) {
      ElMessage.error('请至少选择一个商品规格');
      return;
    }

    // 验证兑换时间
    if (!exchangeTime.value || !exchangeTime.value[0] || !exchangeTime.value[1]) {
      ElMessage.error('请设置完整的兑换时间范围');
      return;
    }

    // 构建提交数据
    const submitData = {
      ...form,
      startTime: exchangeTime.value[0],
      endTime: exchangeTime.value[1],
      skuExchangeInfoList: form.skuExchangeInfoList.map(item => ({
        skuId: item.skuId,
        skuCode: item.skuCode || '',
        points: Number(item.points),
        exchangeLimit: Number(item.exchangeLimit || 0),
        enabled: item.enabled !== false
      }))
    };

    // 转换数据结构以匹配 PointsProduct 类型
    const pointsProductData: PointsProduct = {
      ...submitData,
      points: submitData.points, // 确保使用正确的字段名
      skuExchangeInfoList: submitData.skuExchangeInfoList.map(sku => ({
        ...sku,
        points: sku.points, // 将 points 映射到 points
      }))
    };
    const res = await createPointsProduct(pointsProductData);

    if (res.success) {
      ElMessage.success(form.id ? '更新成功' : '创建成功');
      dialogVisible.value = false;
      fetchData();
    } else {
      throw new Error(res.errMessage || (form.id ? '更新失败' : '创建失败'));
    }
  } catch (error) {
    console.error(form.id ? '更新积分商品失败' : '创建积分商品失败', error);
    ElMessage.error(error instanceof Error ? error.message : (form.id ? '更新失败' : '创建失败'));
  }
};

// 商品选择相关
const productOptions = ref<Product[]>([]);
const productSearchLoading = ref(false);
const skuOptions = ref<ProductSku[]>([]);
const selectedSkuIds = ref<number[]>([]);

// 获取积分商品列表
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getPointsProductList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      name: searchForm.name
    });
    
    if (res.success) {
      productList.value = res.data;
      total.value = res.totalCount;
    } else {
      ElMessage.error(res.errMessage || '获取积分商品列表失败');
    }
  } catch (error) {
    console.error('获取积分商品列表失败', error);
    ElMessage.error('获取积分商品列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索商品
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  fetchData();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  fetchData();
};

// 远程搜索商品
const remoteProductSearch = async (query: string) => {
  productSearchLoading.value = true;
  try {
    const res = await getProductList({
      name: query,
      pageIndex: 1,
      pageSize: 20,
      needTotalCount: true
    });
    if (res.success) {
      productOptions.value = res.data;
    } else {
      ElMessage.error(res.errMessage || '搜索商品失败');
    }
  } catch (error) {
    console.error('搜索商品失败', error);
    ElMessage.error('搜索商品失败');
  } finally {
    productSearchLoading.value = false;
  }
};

// 商品选择框获得焦点时加载所有商品
const handleProductFocus = () => {
  if (productOptions.value.length === 0) {
    remoteProductSearch('');
  }
};

// 商品选择变更
const handleProductChange = async (productId: number) => {
  if (!productId) {
    if (!form.id) { // 只在新增模式下清空SKU列表
      form.skuExchangeInfoList = [];
      skuOptions.value = [];
    }
    return;
  }
  
  try {
    const res = await getProduct(productId);
    if (res.success && res.data) {
      // 确保每个SKU都有有效的skuId
      skuOptions.value = (res.data.skus || []).map(sku => ({
        ...sku,
        skuId: sku.skuId || String(Date.now()) // 如果skuId为null，生成一个临时ID
      }));
      
      // 如果是编辑模式，保持已有的SKU积分设置
      if (form.id && form.skuExchangeInfoList.length > 0) {
        // 确保所有选中的SKU都有对应的积分设置
        selectedSkuIds.value.forEach(skuId => {
          if (!form.skuExchangeInfoList.some(item => item.skuId === skuId)) {
            form.skuExchangeInfoList.push({
              skuId,
              skuCode: '',
              points: form.points || 1000,
              exchangeLimit: form.exchangeLimit || 0,
              enabled: true
            });
          }
        });
      }
    } else {
      ElMessage.error(res.errMessage || '获取商品SKU失败');
    }
  } catch (error) {
    console.error('获取商品SKU失败', error);
    ElMessage.error('获取商品SKU失败');
  }
};

// 获取SKU显示标签
const getSkuLabel = (sku: ProductSku) => {
  if (!sku.attributes) return '默认规格';
  return Object.entries(sku.attributes)
    .map(([key, value]) => `${key}: ${value}`)
    .join(', ');
};

// 添加积分商品
const handleAdd = () => {
  dialogTitle.value = '新增积分商品';
  Object.assign(form, {
    id: undefined,
    productId: 0,
    skuExchangeInfoList: [],  // 重置为空数组
    points: 1000,
    exchangeLimit: 0,
    hasSpec: false,
    enabled: true
  });
  selectedSkuIds.value = [];
  productOptions.value = [];
  skuOptions.value = [];
  dialogVisible.value = true;
};

// 编辑积分商品
const handleEdit = async (row: PointsProduct) => {
  dialogTitle.value = '编辑积分商品';
  
  try {
    // 获取积分商品详情
    const res = await getPointsProduct(row.id);
    if (!res.success || !res.data) {
      ElMessage.error(res.errMessage || '获取积分商品详情失败');
      return;
    }
    
    const pointsProduct = res.data;
    
    // 处理skuExchangeInfoList，确保格式正确
    const skuExchangeInfoList = Array.isArray(pointsProduct.skuExchangeInfoList)
      ? pointsProduct.skuExchangeInfoList.map(item => ({
          skuId: item.skuId,
          skuCode: item.skuCode || '',
          points: Number(item.points || item.points || 0),
          exchangeLimit: Number(item.exchangeLimit || 0),
          enabled: item.enabled !== false
        }))
      : [];
    
    // 设置selectedSkuIds为已有的SKU ID列表
    selectedSkuIds.value = skuExchangeInfoList.map(item => item.skuId);
    
    // 设置兑换时间
    exchangeTime.value = pointsProduct.startTime && pointsProduct.endTime
      ? [pointsProduct.startTime, pointsProduct.endTime]
      : ['', ''];
    
    Object.assign(form, {
      id: pointsProduct.id,
      productId: pointsProduct.productId,
      skuExchangeInfoList,
      points: Number(pointsProduct.points || 1000),
      exchangeLimit: Number(pointsProduct.exchangeLimit || 0),
      hasSpec: pointsProduct.hasSpec || skuExchangeInfoList.length > 0,
      enabled: pointsProduct.enabled !== false,
      startTime: pointsProduct.startTime || '',
      endTime: pointsProduct.endTime || ''
    });
    
    // 先加载商品选项
    await remoteProductSearch('');
    // 加载商品选项和SKU选项
    await handleProductChange(pointsProduct.productId);
    dialogVisible.value = true;
  } catch (error) {
    console.error('获取积分商品详情失败', error);
    ElMessage.error('获取积分商品详情失败');
  }
};

// 删除积分商品
const handleDelete = (row: PointsProduct) => {
  const id = row.id;
  if (typeof id !== 'number') return;
  
  ElMessageBox.confirm('确认删除该积分商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePointsProduct(id);
      if (res.success) {
        ElMessage.success('删除成功');
        fetchData();
      } else {
        ElMessage.error(res.errMessage || '删除积分商品失败');
      }
    } catch (error) {
      console.error('删除积分商品失败', error);
      ElMessage.error('删除积分商品失败');
    }
  }).catch(() => {});
};

// 修改积分商品状态
const handleStatusChange = (row: PointsProduct) => {
  const id = row.id;
  if (typeof id !== 'number') return;
  
  const newStatus = row.enabled ? false : true;
  const statusText = newStatus ? '上架' : '下架';
  // 转换状态为后端需要的 enabled 参数
  // 状态为true表示上架，对应 enabled=true，状态为false表示下架，对应 enabled=false
  const enabled = newStatus;
  
  ElMessageBox.confirm(`确认${statusText}该积分商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updatePointsProductStatus(id, enabled);
      if (res.success) {
        ElMessage.success(`${statusText}成功`);
        fetchData();
      } else {
        ElMessage.error(res.errMessage || `${statusText}失败`);
      }
    } catch (error) {
      console.error(`${statusText}积分商品失败`, error);
      ElMessage.error(`${statusText}失败`);
    }
  }).catch(() => {});
};

// 获取状态类型
const getStatusType = (status: boolean) => {
  return status ? 'success' : 'info';
};

// 获取状态文本
const getStatusText = (status: boolean) => {
  return status ? '上架' : '下架';
};

// 对话框关闭前的处理
const handleDialogClose = (done: () => void) => {
  ElMessageBox.confirm('确认关闭？未保存的数据将会丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    done();
  }).catch(() => {});
};

// 初始化
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.points-product-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-container {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.no-image {
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

.mx-1 {
  margin: 0 4px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.sku-points-setting {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.sku-info {
  margin-bottom: 15px;
}

.sku-label {
  font-weight: bold;
  color: #606266;
}
</style>