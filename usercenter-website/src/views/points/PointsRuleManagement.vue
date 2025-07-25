<template>
  <div class="points-rule-management">
    <div class="header">
      <h2>积分规则管理</h2>
      <el-button type="primary" @click="openDialog()">创建规则</el-button>
    </div>

    <el-table :data="ruleList" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="ruleName" label="规则名称" width="150" />
      <el-table-column prop="ruleDescription" label="规则描述" />
      <el-table-column prop="earnType" label="获取类型" width="120" />
      <el-table-column prop="consumeType" label="消费类型" width="120" />
      <el-table-column prop="pointValue" label="积分值" width="100" />
      <el-table-column prop="validDays" label="有效期(天)" width="120" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
            {{ scope.row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
          <el-popconfirm
            title="确定删除该规则吗？"
            @confirm="handleDelete(scope.row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑规则对话框 -->
    <el-dialog
      :title="dialogType === 'create' ? '创建积分规则' : '编辑积分规则'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="ruleForm" :rules="rules" ref="ruleFormRef" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则描述" prop="ruleDescription">
          <el-input
            v-model="ruleForm.ruleDescription"
            type="textarea"
            placeholder="请输入规则描述"
          />
        </el-form-item>
        <el-form-item label="获取类型" prop="earnType">
          <el-input v-model="ruleForm.earnType" placeholder="请输入获取类型" />
        </el-form-item>
        <el-form-item label="消费类型" prop="consumeType">
          <el-input v-model="ruleForm.consumeType" placeholder="请输入消费类型" />
        </el-form-item>
        <el-form-item label="积分值" prop="pointValue">
          <el-input-number v-model="ruleForm.pointValue" :min="1" />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="validDays">
          <el-input-number v-model="ruleForm.validDays" :min="1" />
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
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAllPointsRules,
  createPointsRule,
  updatePointsRule,
  deletePointsRule
} from '../../api/points'

const ruleList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('create')
const ruleFormRef = ref(null)

const ruleForm = reactive({
  id: null,
  ruleName: '',
  ruleDescription: '',
  earnType: '',
  consumeType: '',
  pointValue: 1,
  validDays: 30
})

const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  earnType: [{ required: true, message: '请输入获取类型', trigger: 'blur' }],
  consumeType: [{ required: true, message: '请输入消费类型', trigger: 'blur' }],
  pointValue: [{ required: true, message: '请输入积分值', trigger: 'blur' }],
  validDays: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
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
    Object.assign(ruleForm, row)
  } else {
    dialogType.value = 'create'
    Object.assign(ruleForm, {
      id: null,
      ruleName: '',
      ruleDescription: '',
      earnType: '',
      consumeType: '',
      pointValue: 1,
      validDays: 30
    })
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!ruleFormRef.value) return
  
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'create') {
          await createPointsRule(ruleForm)
          ElMessage.success('创建成功')
        } else {
          await updatePointsRule(ruleForm)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchRules()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除规则
const handleDelete = async (id) => {
  try {
    await deletePointsRule(id)
    ElMessage.success('删除成功')
    fetchRules()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchRules()
})
</script>

<style scoped>
.points-rule-management {
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