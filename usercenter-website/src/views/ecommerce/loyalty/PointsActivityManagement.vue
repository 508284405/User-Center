<template>
  <div class="points-activity-management">
    <div class="header">
      <h2>积分活动管理</h2>
      <el-button type="primary" @click="openDialog()">创建活动</el-button>
    </div>

    <el-table :data="activityList" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="activityName" label="活动名称" width="150" />
      <el-table-column prop="activityDescription" label="活动描述" />
      <el-table-column prop="activityType" label="活动类型" width="120">
        <template #default="scope">
          <el-tag>
            {{ getActivityTypeText(scope.row.activityType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="pointValue" label="积分奖励" width="100" />
      <el-table-column label="活动时间" width="240">
        <template #default="scope">
          <div>{{ formatDate(scope.row.startTime) }}</div>
          <div>至</div>
          <div>{{ formatDate(scope.row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
          <el-popconfirm
            title="确定删除该活动吗？"
            @confirm="handleDelete(scope.row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑活动对话框 -->
    <el-dialog
      :title="dialogType === 'create' ? '创建积分活动' : '编辑积分活动'"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form :model="activityForm" :rules="rules" ref="activityFormRef" label-width="120px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="activityForm.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动描述" prop="activityDescription">
          <el-input
            v-model="activityForm.activityDescription"
            type="textarea"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="activityForm.activityType" placeholder="请选择活动类型" style="width: 100%">
            <el-option label="签到" value="SIGN_IN" />
            <el-option label="购物" value="PURCHASE" />
            <el-option label="评价" value="REVIEW" />
            <el-option label="分享" value="SHARE" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分奖励" prop="pointValue">
          <el-input-number v-model="activityForm.pointValue" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="关联规则" prop="ruleId">
          <el-select v-model="activityForm.ruleId" placeholder="请选择关联规则" style="width: 100%">
            <el-option
              v-for="rule in ruleList"
              :key="rule.id"
              :label="rule.ruleName"
              :value="rule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            @change="handleTimeRangeChange"
          />
        </el-form-item>
        <el-form-item label="参与次数限制" prop="participationLimit">
          <el-input-number
            v-model="activityForm.participationLimit"
            :min="0"
            :placeholder="'0表示不限制'"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动状态" prop="status" v-if="dialogType === 'edit'">
          <el-select v-model="activityForm.status" placeholder="请选择活动状态" style="width: 100%">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
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

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAllPointsActivities,
  createPointsActivity,
  updatePointsActivity,
  deletePointsActivity,
  getAllPointsRules
} from '../../api/points'

const activityList = ref([])
const ruleList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('create')
const activityFormRef = ref(null)
const timeRange = ref([])

const activityForm = reactive({
  id: null,
  activityName: '',
  activityDescription: '',
  activityType: '',
  pointValue: 1,
  ruleId: null,
  startTime: null,
  endTime: null,
  participationLimit: 0,
  status: 0,
  creatorId: 1, // 假设当前用户ID为1，实际应从用户会话中获取
  updaterId: 1
})

const rules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  pointValue: [{ required: true, message: '请输入积分奖励', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

// 获取所有积分活动
const fetchActivities = async () => {
  try {
    const res = await getAllPointsActivities()
    activityList.value = res.data || []
  } catch (error) {
    console.error('获取积分活动失败:', error)
    ElMessage.error('获取积分活动失败')
  }
}

// 获取所有积分规则
const fetchRules = async () => {
  try {
    const res = await getAllPointsRules()
    ruleList.value = res.data || []
  } catch (error) {
    console.error('获取积分规则失败:', error)
    ElMessage.error('获取积分规则失败')
  }
}

// 打开对话框
const openDialog = (row) => {
  if (row) {
    dialogType.value = 'edit'
    Object.assign(activityForm, row)
    // 设置时间范围
    if (row.startTime && row.endTime) {
      timeRange.value = [
        new Date(typeof row.startTime === 'string' ? row.startTime : row.startTime),
        new Date(typeof row.endTime === 'string' ? row.endTime : row.endTime)
      ]
    } else {
      timeRange.value = []
    }
  } else {
    dialogType.value = 'create'
    Object.assign(activityForm, {
      id: null,
      activityName: '',
      activityDescription: '',
      activityType: '',
      pointValue: 1,
      ruleId: null,
      startTime: null,
      endTime: null,
      participationLimit: 0,
      status: 0,
      creatorId: 1,
      updaterId: 1
    })
    timeRange.value = []
  }
  dialogVisible.value = true
}

// 处理时间范围变化
const handleTimeRangeChange = (val) => {
  if (val && val.length === 2) {
    activityForm.startTime = val[0]
    activityForm.endTime = val[1]
  } else {
    activityForm.startTime = null
    activityForm.endTime = null
  }
}

// 提交表单
const submitForm = async () => {
  if (!activityFormRef.value) return
  
  await activityFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'create') {
          await createPointsActivity(activityForm)
          ElMessage.success('创建成功')
        } else {
          await updatePointsActivity(activityForm)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchActivities()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除活动
const handleDelete = async (id) => {
  try {
    await deletePointsActivity(id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  if (typeof date === 'number') {
    date = new Date(date)
  } else if (typeof date === 'string') {
    return date
  }
  return date.toLocaleString()
}

// 获取活动类型文本
const getActivityTypeText = (type) => {
  const typeMap = {
    'SIGN_IN': '签到',
    'PURCHASE': '购物',
    'REVIEW': '评价',
    'SHARE': '分享'
  }
  return typeMap[type] || type
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || ''
}

onMounted(() => {
  fetchActivities()
  fetchRules()
})
</script>

<style scoped>
.points-activity-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>