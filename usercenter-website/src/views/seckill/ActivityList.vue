<template>
  <div class="seckill-activity-list">
    <div class="page-header">
      <h2>秒杀活动管理</h2>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">新增活动</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="searchForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="searchForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="productPrice" label="原价" min-width="100">
          <template #default="{ row }">
            ¥{{ row.productPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="seckillPrice" label="秒杀价" min-width="100">
          <template #default="{ row }">
            ¥{{ row.seckillPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="总库存" min-width="100" />
        <el-table-column prop="availableStock" label="剩余库存" min-width="100" />
        <el-table-column prop="limitPerUser" label="每人限购" min-width="100" />
        <el-table-column prop="startTime" label="开始时间" min-width="180" />
        <el-table-column prop="endTime" label="结束时间" min-width="180" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">未开始</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">进行中</el-tag>
            <el-tag v-else type="danger">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button size="small" @click="handleViewOrders(row)">订单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <product-selector 
            v-model="form.productId" 
            @product-selected="handleProductSelected"
          />
        </el-form-item>
        <el-form-item label="原价" prop="productPrice">
          <el-input-number
            v-model="form.productPrice"
            :min="0"
            :precision="2"
            controls-position="right"
            placeholder="请输入原价"
          />
        </el-form-item>
        <el-form-item label="秒杀价" prop="seckillPrice">
          <el-input-number
            v-model="form.seckillPrice"
            :min="0"
            :precision="2"
            controls-position="right"
            placeholder="请输入秒杀价"
          />
        </el-form-item>
        <el-form-item label="总库存" prop="totalStock">
          <el-input-number
            v-model="form.totalStock"
            :min="1"
            controls-position="right"
            placeholder="请输入总库存"
          />
        </el-form-item>
        <el-form-item label="每人限购" prop="limitPerUser">
          <el-input-number
            v-model="form.limitPerUser"
            :min="1"
            controls-position="right"
            placeholder="请输入每人限购数量"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="活动状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { seckillApi, type SeckillActivity, type SeckillActivityQuery } from '@/api/client-web/seckill';
import { useRouter } from 'vue-router';
import ProductSelector from '@/components/seckill/ProductSelector.vue';
import type { Product } from '@/api/client-web/product';

const router = useRouter();

// 表格数据
const tableData = ref<SeckillActivity[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchForm = reactive<SeckillActivityQuery>({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: undefined,
  startTime: '',
  endTime: ''
});

// 对话框相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref();

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 0,
  productId: undefined as number | undefined,
  productPrice: undefined as number | undefined,
  seckillPrice: undefined as number | undefined,
  totalStock: undefined as number | undefined,
  limitPerUser: undefined as number | undefined
});

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  productPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'blur' }],
  limitPerUser: [{ required: true, message: '请输入每人限购数量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
};

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };
    
    // 清理空值参数
    Object.keys(params).forEach(key => {
      if (params[key as keyof typeof params] === '' || params[key as keyof typeof params] === undefined) {
        delete params[key as keyof typeof params];
      }
    });
    
    const res = await seckillApi.getActivityList(params);
    tableData.value = res.data;
    total.value = res.total;
  } catch (error) {
    ElMessage.error('获取数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 处理商品选择
const handleProductSelected = (product: Product) => {
  form.productPrice = product.price;
  form.productId = product.id;
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

// 重置搜索
const handleReset = () => {
  searchForm.name = '';
  searchForm.status = undefined;
  searchForm.startTime = '';
  searchForm.endTime = '';
  currentPage.value = 1;
  fetchData();
};

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增秒杀活动';
  isEdit.value = false;
  
  // 重置表单
  Object.assign(form, {
    id: undefined,
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    status: 0,
    productId: undefined,
    productPrice: undefined,
    seckillPrice: undefined,
    totalStock: undefined,
    limitPerUser: undefined
  });
  
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: SeckillActivity) => {
  dialogTitle.value = '编辑秒杀活动';
  isEdit.value = true;
  
  // 填充表单数据
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description,
    startTime: row.startTime,
    endTime: row.endTime,
    status: row.status,
    productId: row.productId,
    productPrice: row.productPrice,
    seckillPrice: row.seckillPrice,
    totalStock: row.totalStock,
    limitPerUser: row.limitPerUser
  });
  
  dialogVisible.value = true;
};

// 删除
const handleDelete = async (row: SeckillActivity) => {
  try {
    await ElMessageBox.confirm('确认删除该秒杀活动吗？', '提示', {
      type: 'warning'
    });
    
    await seckillApi.deleteActivity(row.id!);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    submitLoading.value = true;
    
    if (isEdit.value && form.id) {
      // 编辑
      await seckillApi.updateActivity(form.id, {
        name: form.name,
        description: form.description,
        startTime: form.startTime,
        endTime: form.endTime,
        productId: form.productId,
        productPrice: form.productPrice,
        seckillPrice: form.seckillPrice,
        totalStock: form.totalStock,
        limitPerUser: form.limitPerUser,
        status: form.status
      });
      ElMessage.success('更新成功');
    } else {
      // 新增
      await seckillApi.createActivity({
        name: form.name,
        description: form.description,
        startTime: form.startTime,
        endTime: form.endTime,
        productId: form.productId!,
        productPrice: form.productPrice!,
        seckillPrice: form.seckillPrice!,
        totalStock: form.totalStock!,
        limitPerUser: form.limitPerUser!
      });
      ElMessage.success('创建成功');
    }
    
    dialogVisible.value = false;
    fetchData();
  } catch (error) {
    console.error(error);
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
  } finally {
    submitLoading.value = false;
  }
};

// 查看订单
const handleViewOrders = (row: SeckillActivity) => {
  router.push(`/dashboard/seckill/orders?activityId=${row.id}`);
};

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchData();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  fetchData();
};

// 初始化数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.seckill-activity-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>