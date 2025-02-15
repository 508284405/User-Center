<script setup lang="ts">
import { ref } from 'vue'
import { ElDatePicker } from 'element-plus'
import { ElMessage } from 'element-plus'
import { onMounted } from 'vue'
import { logsApi } from '@/api/logs'

interface LogEntry {
  id: number
  userId: number
  username: string
  operation: string
  module: string
  description: string
  ip: string
  status: string
  createTime: string
}

interface PageQuery {
  pageNum: number
  pageSize: number
  userId?: number
  operation?: string
  startTime?: string
  endTime?: string
}

const logs = ref<LogEntry[]>([])
const total = ref(0)
const loading = ref(false)
const searchQuery = ref('')
const dateRange = ref<[Date, Date] | null>(null)

const pageQuery = ref<PageQuery>({
  pageNum: 1,
  pageSize: 10
})

// 获取日志列表
async function fetchLogs() {
  loading.value = true
  try {
    const { data } = await logsApi.page({
      ...pageQuery.value,
      operation: searchQuery.value || undefined,
      startTime: dateRange.value ? dateRange.value[0].toISOString() : undefined,
      endTime: dateRange.value ? dateRange.value[1].toISOString() : undefined
    })
    logs.value = data.data
    total.value = data.total
  } catch (error) {
    console.error('获取日志列表错误:', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索日志
async function handleSearch() {
  pageQuery.value.pageNum = 1
  await fetchLogs()
}

// 处理分页变化
async function handlePageChange(page: number) {
  pageQuery.value.pageNum = page
  await fetchLogs()
}

// 处理每页数量变化
async function handleSizeChange(size: number) {
  pageQuery.value.pageSize = size
  pageQuery.value.pageNum = 1
  await fetchLogs()
}

// 处理日期范围变化
async function handleDateRangeChange() {
  pageQuery.value.pageNum = 1
  await fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="operation-log">
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="search-bar">
        <el-date-picker
          v-model="dateRange as any"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateRangeChange"
          class="date-picker"
        />
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索操作内容"
          class="search-input"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <div class="table-container">
      <el-table v-loading="loading" :data="logs" class="log-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="operation" label="操作内容" />
        <el-table-column prop="description" label="操作描述" />
        <el-table-column prop="module" label="操作模块" />
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <span :class="['status-badge', row.status === '成功' ? 'success' : 'error']">
              {{ row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageQuery.pageNum"
          v-model:page-size="pageQuery.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.operation-log {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #333;
}

.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.date-picker {
  width: 320px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 250px;
}

.search-btn {
  padding: 8px 16px;
  background-color: #0055cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #0044aa;
}

.table-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.875rem;
}

.status-badge.success {
  background-color: #e6f4ea;
  color: #1e7e34;
}

.status-badge.error {
  background-color: #feeced;
  color: #dc3545;
}
</style>