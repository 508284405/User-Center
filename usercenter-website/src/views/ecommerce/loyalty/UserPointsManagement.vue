<template>
  <div class="user-points-management">
    <div class="header">
      <h2>用户积分管理</h2>
    </div>

    <!-- 用户查询表单 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户">
        <el-select
          v-model="searchForm.userId"
          filterable
          remote
          reserve-keyword
          placeholder="请输入用户名"
          :remote-method="remoteSearch"
          :loading="loading"
          @focus="handleFocus"
        >
          <el-option
            v-for="item in userOptions"
            :key="item.userId"
            :label="item.username"
            :value="item.userId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchUser">查询</el-button>
        <el-button @click="resetSearchForm">重置</el-button>
        <el-button type="primary" @click="openRechargeDialog">充值</el-button>
      </el-form-item>
    </el-form>

    <!-- 用户积分列表 -->
    <el-table :data="userPointsList" border style="width: 100%" v-if="!userPointsDetail">
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="username" label="用户名称" width="120" />
      <el-table-column prop="totalPoints" label="总积分" width="120" />
      <el-table-column prop="availablePoints" label="可用积分" width="120" />
      <el-table-column prop="frozenPoints" label="冻结积分" width="120" />
      <el-table-column label="最近获得时间" width="180">
        <template #default="scope">
          {{ scope.row.lastEarnTime ? formatTimestamp(scope.row.lastEarnTime) : '无' }}
        </template>
      </el-table-column>
      <el-table-column label="最近消费时间" width="180">
        <template #default="scope">
          {{ scope.row.lastConsumeTime ? formatTimestamp(scope.row.lastConsumeTime) : '无' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" size="small" @click="viewUserDetail(scope.row.userId)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 用户积分列表分页 -->
    <div class="pagination" v-if="!userPointsDetail">
      <el-pagination
        v-model:current-page="listQuery.pageIndex"
        v-model:page-size="listQuery.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="listTotal"
        @size-change="handleListSizeChange"
        @current-change="handleListCurrentChange"
      />
    </div>

    <!-- 用户积分详情 -->
    <el-card v-if="userPointsDetail" class="points-detail-card">
      <template #header>
        <div class="card-header">
          <span>用户积分详情</span>
          <el-button type="primary" @click="openAdjustDialog">调整积分</el-button>
        </div>
      </template>
      <div class="points-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ userPointsDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="总积分">{{ userPointsDetail.totalPoints }}</el-descriptions-item>
          <el-descriptions-item label="可用积分">{{ userPointsDetail.availablePoints }}</el-descriptions-item>
          <el-descriptions-item label="冻结积分">{{ userPointsDetail.frozenPoints }}</el-descriptions-item>
          <el-descriptions-item label="最近获得时间">{{ userPointsDetail.lastEarnTime || '无' }}</el-descriptions-item>
          <el-descriptions-item label="最近消费时间">{{ userPointsDetail.lastConsumeTime || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 积分历史记录 -->
    <div v-if="userPointsDetail" class="points-history">
      <div class="history-header">
        <h3>积分历史记录</h3>
        <el-form :inline="true" :model="historySearchForm" class="history-search-form">
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="historySearchForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              value-format="x"
            />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="historySearchForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              value-format="x"
            />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="historySearchForm.type" placeholder="选择类型">
              <el-option label="全部" value="" />
              <el-option label="获得积分" value="EARN" />
              <el-option label="消费积分" value="CONSUME" />
              <el-option label="积分过期" value="EXPIRE" />
              <el-option label="管理员调整" value="ADJUST" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchHistory">查询</el-button>
            <el-button @click="resetHistoryForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 积分历史记录表格 -->
      <el-table :data="pointsHistoryList" border style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="points" label="积分变动" width="100">
          <template #default="scope">
            <span :class="{ 'text-success': scope.row.points > 0, 'text-danger': scope.row.points < 0 }">
              {{ scope.row.points > 0 ? '+' : '' }}{{ scope.row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">{{ getTypeText(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="scope">
            {{ formatTimestamp(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 积分历史记录分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="historySearchForm.pageNum"
          v-model:page-size="historySearchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="historyTotal"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 调整积分对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="调整积分"
      width="500px"
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="adjustForm.userId" disabled />
        </el-form-item>
        <el-form-item label="调整积分" prop="points">
          <el-input-number
            v-model="adjustForm.points"
            :min="-999999"
            :max="999999"
            placeholder="请输入调整积分数量"
          />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="adjustForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="adjustDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdjust">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="积分充值"
      width="500px"
    >
      <el-form
        ref="rechargeFormRef"
        :model="rechargeForm"
        :rules="rechargeRules"
        label-width="100px"
      >
        <el-form-item label="用户" prop="userId">
          <el-select
            v-model="rechargeForm.userId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入用户名"
            :remote-method="remoteSearch"
            :loading="loading"
            @focus="handleFocus"
          >
            <el-option
              v-for="item in rechargeUserOptions"
              :key="item.userId"
              :label="`${item.username || ''} (ID:${item.userId})`"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="充值积分" prop="points">
          <el-input-number
            v-model="rechargeForm.points"
            :min="1"
            :max="999999"
            placeholder="请输入充值积分数量"
          />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="validDays">
          <el-input-number
            v-model="rechargeForm.validDays"
            :min="1"
            :max="3650"
            placeholder="不填则永久有效"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="rechargeForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRecharge">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import {
  getUserPointsDetails,
  getUserPointsHistory,
  adjustUserPoints,
  getUserPointsList,
  rechargeUserPoints
} from '../../../api/client-web/points'
import { userApi } from '../../../api/usercenter/user'

const userPointsDetail = ref(null)
const pointsHistoryList = ref([])
const historyTotal = ref(0)
const adjustDialogVisible = ref(false)
const adjustFormRef = ref(null)
const rechargeDialogVisible = ref(false)
const rechargeFormRef = ref(null)

// 用户积分列表相关
const userPointsList = ref([])
// 列表查询参数
const listQuery = reactive({
  pageIndex: 1,
  pageSize: 10,
  userId: undefined
})

// 列表总数
const listTotal = ref(0)

// 搜索表单
const searchForm = reactive({
  userId: ''
})

// 历史记录搜索表单
const historySearchForm = reactive({
  userId: '',
  startTime: null,
  endTime: null,
  type: '',
  pageSize: 10,
  pageNum: 1
})

// 调整积分表单
const adjustForm = reactive({
  userId: '',
  points: 0,
  reason: '',
  operatorId: 1, // 假设当前操作员ID为1，实际应从用户会话中获取
  remark: ''
})

// 充值表单
const rechargeForm = reactive({
  userId: '',
  points: 0,
  validDays: undefined,
  remark: ''
})

// 充值表单验证规则
const rechargeRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  points: [
    { required: true, message: '请输入充值积分数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '充值积分必须大于0', trigger: 'blur' }
  ],
  validDays: [
    { type: 'number', min: 1, message: '有效期天数必须大于0', trigger: 'blur' }
  ]
}

// 调整积分表单验证规则
const adjustRules = {
  points: [
    { required: true, message: '请输入调整积分数量', trigger: 'blur' },
    { type: 'number', message: '积分必须为数字', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' },
    { max: 200, message: '调整原因不能超过200个字符', trigger: 'blur' }
  ],
  remark: [
    { max: 500, message: '备注不能超过500个字符', trigger: 'blur' }
  ]
}

// 查询用户积分列表
const searchUser = async () => {
  const loading = ElLoading.service({
    target: '.user-points-management',
    text: '加载中...'
  })

  try {
    // 更新查询参数
    listQuery.userId = searchForm.userId || undefined
    const res = await getUserPointsList(listQuery)
    // 获取用户列表数据
    const userIds = res.data.map(item => item.userId)
    if (userIds.length > 0) {
      // 使用userApi.page获取用户信息 - 使用最大pageSize确保获取所有用户
      const userRes = await userApi.page({
        pageNum: 1,
        pageSize: 2147483647, // 使用Integer.MAX_VALUE，确保能获取所有用户
        username: searchForm.userId ? String(searchForm.userId) : undefined
      })
      const userMap = new Map(userRes.data.map(user => [user.id, user.username]))
      // 将用户名称添加到积分列表数据中
      userPointsList.value = res.data.map(item => ({
        ...item,
        username: userMap.get(item.userId) || '未知用户'
      }))
    } else {
      userPointsList.value = res.data
    }
    listTotal.value = res.total || 0
  } catch (error) {
    console.error('获取用户积分列表失败:', error)
    ElMessage.error('获取用户积分列表失败')
    userPointsList.value = []
    listTotal.value = 0
  } finally {
    loading.close()
  }
}

// 查看用户详情
const viewUserDetail = (userId) => {
  searchForm.userId = userId
  searchUser()
}

// 获取用户积分列表
const fetchUserPointsList = async () => {
  const loading = ElLoading.service({
    target: '.user-points-management',
    text: '加载中...'
  })

  try {
    const res = await getUserPointsList(listQuery)
    let pointsData = [];
    if (res.data && Array.isArray(res.data)) {
      pointsData = res.data;
      listTotal.value = res.totalCount || res.data.length;
    } else if (res.data && res.data.list && Array.isArray(res.data.list)) {
      pointsData = res.data.list;
      listTotal.value = res.data.totalCount || res.data.list.length;
    }

    if (pointsData.length > 0) {
      // 获取所有用户ID
      const userIds = pointsData.map(item => item.userId);
      // 获取用户信息
      const userRes = await userApi.page({
        pageNum: 1,
        pageSize: userIds.length
      });
      // 创建用户ID到用户名的映射
      const userMap = new Map(userRes.data.map(user => [user.id, user.username]));
      // 将用户名称添加到积分列表数据中
      userPointsList.value = pointsData.map(item => ({
        ...item,
        username: userMap.get(item.userId) || '未知用户'
      }));
    } else {
      userPointsList.value = [];
      listTotal.value = 0;
    }
  } catch (error) {
    console.error('获取用户积分列表失败:', error)
    ElMessage.error('获取用户积分列表失败')
    userPointsList.value = []
    listTotal.value = 0
  } finally {
    loading.close()
  }
}

// 处理用户积分列表分页大小变化
const handleListSizeChange = (size) => {
  listQuery.pageSize = size
  fetchUserPointsList()
}

// 处理用户积分列表页码变化
const handleListCurrentChange = (page) => {
  listQuery.pageIndex = page
  fetchUserPointsList()
}

// 查询积分历史记录
const searchHistory = async () => {
  if (!historySearchForm.userId) {
    return
  }

  const params = { ...historySearchForm }
  
  try {
    const res = await getUserPointsHistory(params.userId, params)
    if (res.data && Array.isArray(res.data)) {
      pointsHistoryList.value = res.data
      historyTotal.value = res.totalCount || res.data.length
    } else if (res.data && res.data.list && Array.isArray(res.data.list)) {
      pointsHistoryList.value = res.data.list
      historyTotal.value = res.data.totalCount || res.data.list.length
    } else {
      pointsHistoryList.value = []
      historyTotal.value = 0
    }
  } catch (error) {
    console.error('获取积分历史记录失败:', error)
    ElMessage.error('获取积分历史记录失败')
    pointsHistoryList.value = []
    historyTotal.value = 0
  }
}

// 打开调整积分对话框
const openAdjustDialog = () => {
  if (!userPointsDetail.value) return
  
  adjustForm.userId = userPointsDetail.value.userId
  adjustForm.points = 0
  adjustForm.reason = ''
  adjustForm.remark = ''
  
  adjustDialogVisible.value = true
}

// 提交调整积分
const submitAdjust = async () => {
  if (!adjustFormRef.value) return
  
  await adjustFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await adjustUserPoints(adjustForm)
        ElMessage.success('积分调整成功')
        adjustDialogVisible.value = false
        // 重新查询用户积分详情和历史记录
        searchUser()
      } catch (error) {
        console.error('积分调整失败:', error)
        ElMessage.error('积分调整失败')
      }
    }
  })
}

// 处理历史记录分页大小变化
const handleSizeChange = (size) => {
  historySearchForm.pageSize = size
  searchHistory()
}

// 处理历史记录页码变化
const handleCurrentChange = (page) => {
  historySearchForm.pageNum = page
  searchHistory()
}

// 格式化时间戳
const formatTimestamp = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString()
}

// 获取类型文本
const getTypeText = (type) => {
  const typeMap = {
    'EARN': '获得积分',
    'CONSUME': '消费积分',
    'EXPIRE': '积分过期',
    'ADJUST': '管理员调整'
  }
  return typeMap[type] || type
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  const typeMap = {
    'EARN': 'success',
    'CONSUME': 'warning',
    'EXPIRE': 'danger',
    'ADJUST': 'info'
  }
  return typeMap[type] || ''
}

// 重置用户搜索表单
const resetSearchForm = () => {
  searchForm.userId = ''
  userPointsDetail.value = null
  pointsHistoryList.value = []
  fetchUserPointsList()
}

// 打开充值对话框
const openRechargeDialog = () => {
  rechargeForm.userId = ''
  rechargeForm.points = 0
  rechargeForm.validDays = undefined
  rechargeForm.remark = ''
  rechargeDialogVisible.value = true
}

// 提交充值
const submitRecharge = async () => {
  if (!rechargeFormRef.value) return

  await rechargeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await rechargeUserPoints(rechargeForm)
        ElMessage.success('积分充值成功')
        rechargeDialogVisible.value = false
        // 重新加载列表
        fetchUserPointsList()
        // 如果当前正在查看该用户的详情，则刷新详情
        if (userPointsDetail.value && userPointsDetail.value.userId === rechargeForm.userId) {
          searchUser()
        }
      } catch (error) {
        console.error('积分充值失败:', error)
        ElMessage.error('积分充值失败')
      }
    }
  })
}

// 初始化加载
onMounted(async () => {
  try {
    await fetchUserPointsList()
  } catch (error) {
    console.error('初始化加载用户积分列表失败:', error)
    ElMessage.error('加载用户积分列表失败')
  }
})
// 用户搜索相关
const loading = ref(false)
const userOptions = ref([])
const rechargeUserOptions = ref([])

// 远程搜索用户
const remoteSearch = async (query) => {
  if (query === '') {
    // 当搜索关键词为空时，显示所有用户
    await handleFocus()
    return
  }

  loading.value = true
  try {
    // 使用userApi进行用户搜索
    const res = await userApi.page({
      pageNum: 1,
      pageSize: 1000000,
      username: query
    })
    
    if (res.data && Array.isArray(res.data)) {
      const mappedUsers = res.data.map(user => ({
        userId: user.id,
        username: user.username
      }))
      userOptions.value = mappedUsers
      rechargeUserOptions.value = mappedUsers
    } else {
      // 如果没有找到用户名匹配的，尝试使用ID搜索
      try {
        if (!isNaN(Number(query))) {
          const pointsRes = await getUserPointsList({
            pageNum: 1,
            pageSize: 10,
            userId: Number(query)
          })
          
          if (pointsRes.data && Array.isArray(pointsRes.data)) {
            userOptions.value = pointsRes.data
            rechargeUserOptions.value = pointsRes.data
          }
        }
      } catch (error) {
        console.error('按ID搜索用户失败:', error)
      }
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索用户失败')
    userOptions.value = []
    rechargeUserOptions.value = []
  } finally {
    loading.value = false
  }
}

// 处理输入框获得焦点事件
const handleFocus = async () => {
  loading.value = true
  try {
    // 获取所有用户列表
    const res = await userApi.page({
      pageNum: 1,
      pageSize: 100 // 加载较多用户以供选择
    })    
    
    if (res.data && Array.isArray(res.data)) {
      const mappedUsers = res.data.map(user => ({
        userId: user.id,
        username: user.username
      }))
      userOptions.value = mappedUsers
      rechargeUserOptions.value = mappedUsers
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
    userOptions.value = []
    rechargeUserOptions.value = []
  } finally {
    loading.value = false
  }
}

// 重置历史记录搜索表单
const resetHistoryForm = () => {
  historySearchForm.startTime = null
  historySearchForm.endTime = null
  historySearchForm.type = ''
  searchHistory()
}
</script>