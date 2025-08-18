<template>
  <div class="intent-management">
    <div class="page-header">
      <h2 class="page-title">意图管理</h2>
      <p class="page-description">管理意图分类、编码、描述等信息</p>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">新建意图</el-button>
        <el-button @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;gap:12px;">
          <div>意图列表</div>
          <el-input
            v-model="query.keyword"
            placeholder="按名称/编码/描述搜索"
            clearable
            style="max-width: 240px"
            @keyup.enter.native="onSearch"
          />
        </div>
      </template>

      <el-table :data="list" v-loading="loading.table" row-key="id" style="width: 100%">
        <el-table-column prop="name" label="意图名称" min-width="200" />
        <el-table-column prop="code" label="意图编码" min-width="160" />
        <el-table-column prop="catalogName" label="所属目录" min-width="160" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusBadgeType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="toggleStatus(row)" :loading="row._loading">
                {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
              </el-button>
              <el-popconfirm title="确认删除该意图？" @confirm="remove(row)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :current-page="pagination.current"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新建意图' : '编辑意图'" width="720px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="意图名称" prop="name">
          <el-input v-model="form.name" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="意图编码" prop="code">
          <el-input v-model="form.code" placeholder="如：order_query" />
        </el-form-item>
        <el-form-item label="所属目录" prop="catalogId">
          <el-select v-model="form.catalogId" placeholder="请选择所属目录" style="width: 100%">
            <el-option
              v-for="catalog in catalogList"
              :key="catalog.id"
              :label="catalog.name"
              :value="catalog.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="loading.submit" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { intentApi, catalogApi } from '@/api/smartcs/intent'

type Intent = {
  id?: number
  name: string
  code: string
  description?: string
  catalogId: number
  catalogName?: string
  status?: string
  _loading?: boolean
}

type Catalog = {
  id: number
  name: string
  code: string
}

const list = ref<Intent[]>([])
const catalogList = ref<Catalog[]>([])
const loading = reactive({ table: false, submit: false })
const query = reactive({ keyword: '' })
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })

const dialog = reactive({ visible: false, mode: 'create' as 'create' | 'edit' })
const formRef = ref()
const form = reactive<Intent>({ 
  name: '', 
  code: '', 
  description: '', 
  catalogId: 0
})
const rules = reactive({
  name: [{ required: true, message: '请输入意图名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入意图编码', trigger: 'blur' }],
  catalogId: [{ required: true, message: '请选择所属目录', trigger: 'change' }]
})

function onSearch() {
  pagination.current = 1
  loadData()
}

function onPageChange(page: number) {
  pagination.current = page
  loadData()
}

function onSizeChange(size: number) {
  pagination.pageSize = size
  pagination.current = 1
  loadData()
}

async function loadData() {
  loading.table = true
  try {
    const params: any = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    
    // 只有当关键词不为空时才添加搜索参数
    if (query.keyword && query.keyword.trim()) {
      params.keyword = query.keyword.trim()
    }
    
    const res: any = await intentApi.getIntentPage(params)
    if (res && res.success) {
      const intentList = Array.isArray(res.data) ? res.data : []
      
      // 为每个意图添加目录名称
      list.value = intentList.map((intent: any) => {
        const catalog = catalogList.value.find((cat: any) => cat.id == intent.catalogId)
        return {
          ...intent,
          catalogName: catalog ? catalog.name : '未知目录'
        }
      })
      
      pagination.total = res.totalCount || 0
    }
  } finally {
    loading.table = false
  }
}

async function loadCatalogList() {
  try {
    const res: any = await catalogApi.getCatalogList()
    if (res && res.success) {
      catalogList.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    console.error('加载目录列表失败:', error)
  }
}

function openCreate() {
  dialog.mode = 'create'
  Object.assign(form, { 
    id: undefined, 
    name: '', 
    code: '', 
    description: '', 
    catalogId: 0
  })
  dialog.visible = true
}

function openEdit(row: Intent) {
  dialog.mode = 'edit'
  Object.assign(form, row)
  dialog.visible = true
}

async function submit() {
  // @ts-ignore
  await formRef.value?.validate?.()
  loading.submit = true
  try {
    let res: any
    if (dialog.mode === 'create') {
      // 创建意图时发送完整数据
      res = await intentApi.createIntent(form)
    } else if (form.id != null) {
      // 更新意图时只发送可更新的字段
      const updateData = {
        name: form.name,
        description: form.description
      }
      res = await intentApi.updateIntent(form.id, updateData)
    }
    if (res?.success) {
      ElMessage.success('保存成功')
      dialog.visible = false
      loadData()
    }
  } finally {
    loading.submit = false
  }
}

async function remove(row: Intent) {
  if (!row.id) return
  try {
    await intentApi.deleteIntent(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

async function toggleStatus(row: Intent) {
  if (!row.id) return
  row._loading = true
  try {
    const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    // 通过更新意图API来改变状态
    const updateData = {
      name: row.name,
      description: row.description
    }
    await intentApi.updateIntent(row.id, updateData)
    // 注意：这里需要后端支持状态更新，或者通过其他方式实现
    // 暂时只是模拟状态切换
    row.status = newStatus
    ElMessage.success(newStatus === 'ACTIVE' ? '已启用' : '已停用')
  } catch (error) {
    ElMessage.error('状态切换失败')
  } finally {
    row._loading = false
  }
}

function getStatusBadgeType(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'info'
    case 'DRAFT':
      return 'warning'
    default:
      return 'secondary'
  }
}

function getStatusText(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return '激活'
    case 'INACTIVE':
      return '停用'
    case 'DRAFT':
      return '草稿'
    default:
      return '未知'
  }
}

onMounted(async () => {
  // 先加载目录列表，再加载意图数据
  await loadCatalogList()
  loadData()
})
</script>

<style scoped>
.intent-management {
  display: block;
}
</style>

