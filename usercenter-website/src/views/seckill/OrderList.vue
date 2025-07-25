<template>
  <div class="seckill-order-list">
    <div class="page-header">
      <h2>秒杀订单管理</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="活动ID">
          <el-input v-model="searchForm.activityId" placeholder="请输入活动ID" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
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
        <el-table-column prop="id" label="订单ID" min-width="100" />
        <el-table-column prop="activityId" label="活动ID" min-width="100" />
        <el-table-column prop="userId" label="用户ID" min-width="100" />
        <el-table-column prop="orderId" label="订单号" min-width="180" />
        <el-table-column prop="productCount" label="商品数量" min-width="100" />
        <el-table-column prop="orderAmount" label="订单金额" min-width="120">
          <template #default="{ row }">
            ¥{{ row.orderAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">待支付</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已支付</el-tag>
            <el-tag v-else type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status === 0" 
              size="small" 
              type="success" 
              @click="handleUpdateStatus(row, 1)"
            >
              设为已支付
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              size="small" 
              type="warning" 
              @click="handleUpdateStatus(row, 2)"
            >
              设为已取消
            </el-button>
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="活动ID">{{ detailData.activityId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ detailData.orderId }}</el-descriptions-item>
        <el-descriptions-item label="商品数量">{{ detailData.productCount }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ detailData.orderAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="info">待支付</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="success">已支付</el-tag>
          <el-tag v-else type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { seckillApi, type SeckillOrder, type SeckillOrderQuery } from '@/api/client-web/seckill';

const route = useRoute();

// 表格数据
const tableData = ref<SeckillOrder[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchForm = reactive<SeckillOrderQuery>({
  pageNum: 1,
  pageSize: 10,
  activityId: route.query.activityId ? Number(route.query.activityId) : undefined,
  userId: undefined,
  status: undefined
});

// 详情对话框
const detailDialogVisible = ref(false);
const detailData = ref<SeckillOrder>({
  id: undefined,
  activityId: 0,
  userId: 0,
  orderId: '',
  productCount: 0,
  orderAmount: 0,
  status: 0,
  createdAt: '',
  updatedAt: ''
});

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
    
    const res = await seckillApi.getOrderList(params);
    tableData.value = res.data;
    total.value = res.total;
  } catch (error) {
    ElMessage.error('获取数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

// 重置搜索
const handleReset = () => {
  searchForm.activityId = route.query.activityId ? Number(route.query.activityId) : undefined;
  searchForm.userId = undefined;
  searchForm.status = undefined;
  currentPage.value = 1;
  fetchData();
};

// 查看详情
const handleViewDetail = (row: SeckillOrder) => {
  detailData.value = { ...row };
  detailDialogVisible.value = true;
};

// 更新订单状态
const handleUpdateStatus = async (row: SeckillOrder, status: number) => {
  try {
    const statusText = status === 1 ? '已支付' : '已取消';
    await ElMessageBox.confirm(`确认将订单状态设为${statusText}吗？`, '提示', {
      type: 'warning'
    });
    
    await seckillApi.updateOrderStatus(row.id!, status);
    ElMessage.success('更新成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('更新失败');
      console.error(error);
    }
  }
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
.seckill-order-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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