<template>
  <div class="order-list-container">
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable></el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择订单状态" clearable>
            <el-option
              v-for="item in orderStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="['00:00:00', '23:59:59']"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="statistics-card">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card shadow="hover">
            <div class="statistic-item">
              <div class="statistic-title">总订单数</div>
              <div class="statistic-value">{{ statistics.totalOrders }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div class="statistic-item">
              <div class="statistic-title">总金额</div>
              <div class="statistic-value">¥{{ statistics.totalAmount.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div class="statistic-item">
              <div class="statistic-title">待付款</div>
              <div class="statistic-value">{{ statistics.pendingPayment }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div class="statistic-item">
              <div class="statistic-title">待发货</div>
              <div class="statistic-value">{{ statistics.pendingDelivery }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div class="statistic-item">
              <div class="statistic-title">已发货</div>
              <div class="statistic-value">{{ statistics.delivered }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div class="statistic-item">
              <div class="statistic-title">已完成</div>
              <div class="statistic-value">{{ statistics.completed }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="orderList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="订单ID" width="80"></el-table-column>
        <el-table-column prop="orderNumber" label="订单编号" width="180"></el-table-column>
        <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100">
          <template #default="scope">
            <el-tag :type="getOrderStatusType(scope.row.orderStatus)">
              {{ getOrderStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.payTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewOrderDetail(scope.row)">查看详情</el-button>
            <!-- 发货按钮已移至订单详情页 -->
            <!-- 退款按钮已禁用 -->
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="searchForm.pageSize"
          :current-page="searchForm.pageIndex"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <!-- 发货对话框已移除 -->

    <!-- 退款处理对话框 -->
    <el-dialog title="退款处理" v-model="refundDialogVisible" width="500px">
      <el-form :model="refundForm" label-width="100px" :rules="refundRules" ref="refundFormRef">
        <el-form-item label="退款原因">
          <div>{{ currentOrder?.refundReason || '无' }}</div>
        </el-form-item>
        <el-form-item label="处理结果" prop="approved">
          <el-radio-group v-model="refundForm.approved">
            <el-radio :label="true">同意退款</el-radio>
            <el-radio :label="false">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="comment">
          <el-input type="textarea" v-model="refundForm.comment" rows="3" placeholder="请输入处理备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRefund">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus';
import { getOrderList, processRefund, getOrderStatistics, OrderQuery, OrderStatisticsQuery, Order } from '@/api/client-web/order';
import { OrderStatusEnum, getOrderStatusText, getOrderStatusType, orderStatusOptions } from '@/enums/orderStatus';
import dayjs from 'dayjs';

const router = useRouter();
const loading = ref(false);
const orderList = ref<Order[]>([]);
const total = ref(0);
const dateRange = ref<string[]>([]);
const refundDialogVisible = ref(false);
const refundFormRef = ref<FormInstance | null>(null);
const currentOrder = ref<Order | null>(null);

const statistics = reactive({
  totalOrders: 0,
  totalAmount: 0,
  pendingPayment: 0,
  pendingDelivery: 0,
  delivered: 0,
  completed: 0,
  cancelled: 0
});

const searchForm = reactive({
  pageSize: 10,
  pageIndex: 1,
  needTotalCount: true,
  orderNo: '',
  userId: '',
  status: '',
  startTime: '',
  endTime: ''
});

const refundForm = reactive({
  approved: true,
  comment: ''
});

const refundRules = {
  comment: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
};

const formatDate = (timestamp?: number | string): string => {
  if (!timestamp) return '';
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss');
};

const fetchOrderList = async () => {
  loading.value = true;
  try {
    const params: OrderQuery = {
      pageSize: searchForm.pageSize,
      pageIndex: searchForm.pageIndex,
      needTotalCount: searchForm.needTotalCount
    };
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo;
    if (searchForm.userId) params.userId = Number(searchForm.userId);
    if (searchForm.status) params.status = Number(searchForm.status);
    if (dateRange.value.length === 2) {
      params.startTime = dateRange.value[0];
      params.endTime = dateRange.value[1];
    }
    const res = await getOrderList(params);
    orderList.value = res.data;
    total.value = res.totalCount;
  } catch (error) {
    console.error('获取订单列表失败:', error);
    ElMessage.error('获取订单列表失败');
  } finally {
    loading.value = false;
  }
};

const fetchOrderStatistics = async () => {
  try {
    const params: OrderStatisticsQuery = {};
    if (dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    if (searchForm.userId) {
      params.userId = Number(searchForm.userId);
    }
    const res = await getOrderStatistics(params);
    if (res.data) Object.assign(statistics, res.data);
  } catch (error) {
    console.error('获取订单统计信息失败:', error);
  }
};

const handleSearch = () => {
  searchForm.pageIndex = 1;
  fetchOrderList();
  fetchOrderStatistics();
};

const resetSearch = () => {
  searchForm.orderNo = '';
  searchForm.userId = '';
  searchForm.status = '';
  dateRange.value = [];
  searchForm.startTime = '';
  searchForm.endTime = '';
  searchForm.pageIndex = 1;
  fetchOrderList();
  fetchOrderStatistics();
};

const handleSizeChange = (val: number) => {
  searchForm.pageSize = val;
  fetchOrderList();
};

const handleCurrentChange = (val: number) => {
  searchForm.pageIndex = val;
  fetchOrderList();
};

const viewOrderDetail = (row: Order) => {
  router.push({ name: 'OrderDetail', params: { orderNumber: row.orderNumber } });
};

const showRefundDialog = (row: Order) => {
  currentOrder.value = row;
  refundForm.approved = true;
  refundForm.comment = '';
  refundDialogVisible.value = true;
  refundFormRef.value?.resetFields();
};

const confirmRefund = async () => {
  if (!refundFormRef.value || !currentOrder.value) return;
  await refundFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await processRefund(currentOrder.value!.orderNumber, refundForm);
        ElMessage.success('退款处理成功');
        refundDialogVisible.value = false;
        fetchOrderList();
        fetchOrderStatistics();
      } catch (error) {
        console.error('退款处理失败:', error);
        ElMessage.error('退款处理失败');
      }
    }
  });
};

onMounted(() => {
  fetchOrderList();
  fetchOrderStatistics();
});
</script>

<style scoped>
.order-list-container {
  padding: 20px;
}

.search-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.statistics-card {
  margin-bottom: 20px;
}

.statistic-item {
  text-align: center;
}

.statistic-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.statistic-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>