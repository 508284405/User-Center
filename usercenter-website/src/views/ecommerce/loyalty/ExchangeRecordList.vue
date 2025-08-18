<template>
  <div class="exchange-record-list">
    <el-card class="box-card" :shadow="'never'">
      <template #header>
        <span>商品兑换记录</span>
      </template>
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户昵称">
          <el-input v-model="searchForm.userNickname" placeholder="请输入用户昵称" clearable />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="兑换状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="records"
        :loading="loading"
        :pagination="false"
      >
        <el-table-column
          prop="userNickname"
          label="用户昵称"
          width="120"
        />
        <el-table-column
          prop="productName"
          label="商品名称"
          min-width="120"
        />
        <el-table-column
          prop="productImage"
          label="商品图片"
          width="80"
        >
          <template #default="{ row }">
            <el-image
              style="width: 50px"
              :src="row.productImage"
              :preview-src-list="[row.productImage]"
              fallback="https://via.placeholder.com/50"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="specifications"
          label="规格信息"
          min-width="120"
        />
        <el-table-column
          prop="quantity"
          label="兑换数量"
          width="80"
        />
        <el-table-column
          prop="pointsConsumed"
          label="消耗积分"
          width="100"
        />
        <el-table-column
          prop="exchangeTime"
          label="兑换时间"
          width="180"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'APPROVED'"
              size="small"
              type="success"
              @click="handleComplete(row)"
            >
              标记完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getExchangeList } from '@/api/client-web/exchange'

const records = ref([])
const loading = ref(false)
const total = ref(0)

// 状态选项
const statusOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

// 搜索表单数据
const searchForm = reactive({
  userNickname: '',
  productName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 获取列表数据
const fetchData = async () => {
  try {
    loading.value = true
    const response = await getExchangeList({
      ...searchForm,
      needTotalCount: true
    })
    records.value = response.data || []
    total.value = response.totalCount || 0
  } catch (error) {
    console.error('获取兑换记录失败:', error)
    ElMessage.error('获取兑换记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索方法
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchData()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.userNickname = ''
  searchForm.productName = ''
  searchForm.status = ''
  searchForm.pageNum = 1
  fetchData()
}

const handleComplete = async (row) => {
  try {
    loading.value = true
    // TODO: 调用接口更新状态
    // await updateExchangeStatus(row.id, 'COMPLETED')
    ElMessage.success('状态更新成功')
    fetchData()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchData()
}

const handleSizeChange = (val) => {
  searchForm.pageSize = val
  searchForm.pageNum = 1
  fetchData()
}

// 初始化加载数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.exchange-record-list {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>