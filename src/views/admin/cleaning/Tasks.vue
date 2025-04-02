<template>
  <div class="tasks-container">
    <div class="page-header">
      <h2>清洁任务</h2>
      <el-button type="primary" @click="handleAdd">分配任务</el-button>
    </div>

    <!-- 任务统计卡片 -->
    <el-row :gutter="20" class="task-stats">
      <el-col :span="6" v-for="stat in taskStats" :key="stat.type">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :class="stat.type">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-count">{{ stat.count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card class="task-list-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">任务列表</div>
          <div class="header-filter">
            <el-radio-group v-model="taskFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="pending">待处理</el-radio-button>
              <el-radio-button label="processing">进行中</el-radio-button>
              <el-radio-button label="completed">已完成</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table :data="filteredTasks" style="width: 100%" v-loading="loading">
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="roomType" label="房间类型" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" effect="dark">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cleaner" label="保洁员" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedTime" label="预计完成时间" width="180" />
        <el-table-column prop="notes" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleStart(row)"
              v-if="row.status === 'pending'"
            >开始清洁</el-button>
            <el-button 
              type="success" 
              link 
              @click="handleComplete(row)"
              v-if="row.status === 'processing'"
            >完成清洁</el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleInspect(row)"
              v-if="row.status === 'completed'"
            >查看检查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分配任务对话框 -->
    <el-dialog
      title="分配清洁任务"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskFormRules"
        label-width="100px"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-select v-model="taskForm.roomNumber" placeholder="请选择房间" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.number"
              :label="`${room.number} (${room.type})`"
              :value="room.number"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="保洁员" prop="cleaner">
          <el-select v-model="taskForm.cleaner" placeholder="请选择保洁员" style="width: 100%">
            <el-option
              v-for="cleaner in cleaners"
              :key="cleaner.id"
              :label="cleaner.name"
              :value="cleaner.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计时间" prop="expectedTime">
          <el-time-picker
            v-model="taskForm.expectedTime"
            format="HH:mm"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="taskForm.notes"
            type="textarea"
            rows="3"
            placeholder="请输入任务备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 完成清洁对话框 -->
    <el-dialog
      title="完成清洁确认"
      v-model="completeDialogVisible"
      width="500px"
    >
      <el-form
        ref="completeFormRef"
        :model="completeForm"
        :rules="completeFormRules"
        label-width="100px"
      >
        <el-form-item label="实际用时" prop="actualDuration">
          <el-input-number
            v-model="completeForm.actualDuration"
            :min="5"
            :max="120"
            :step="5"
          />
          <span class="unit-text">分钟</span>
        </el-form-item>
        <el-form-item label="清洁项目" prop="cleanItems">
          <el-checkbox-group v-model="completeForm.cleanItems">
            <el-checkbox label="bedding">床品更换</el-checkbox>
            <el-checkbox label="bathroom">浴室清洁</el-checkbox>
            <el-checkbox label="floor">地面清洁</el-checkbox>
            <el-checkbox label="furniture">家具除尘</el-checkbox>
            <el-checkbox label="garbage">垃圾清理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="物品补充" prop="supplies">
          <el-checkbox-group v-model="completeForm.supplies">
            <el-checkbox label="toiletries">洗漱用品</el-checkbox>
            <el-checkbox label="towels">毛巾浴巾</el-checkbox>
            <el-checkbox label="water">饮用水</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="特殊情况" prop="issues">
          <el-input
            v-model="completeForm.issues"
            type="textarea"
            rows="3"
            placeholder="请记录房间内发现的任何问题或特殊情况"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCompleteSubmit">确认完成</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Loading, Check, Warning } from '@element-plus/icons-vue'

// 任务统计数据
const taskStats = reactive([
  {
    title: '待处理任务',
    count: 8,
    type: 'pending',
    icon: Clock
  },
  {
    title: '进行中任务',
    count: 3,
    type: 'processing',
    icon: Loading
  },
  {
    title: '已完成任务',
    count: 25,
    type: 'completed',
    icon: Check
  },
  {
    title: '高优先级',
    count: 2,
    type: 'high-priority',
    icon: Warning
  }
])

// 任务列表数据
const loading = ref(false)
const taskFilter = ref('all')
const taskList = ref([
  {
    roomNumber: '301',
    roomType: '标准双人间',
    priority: 'high',
    cleaner: '李阿姨',
    status: 'pending',
    expectedTime: '10:30',
    notes: '客人即将入住，请优先处理'
  },
  // 更多模拟数据...
])

// 可用房间列表
const availableRooms = ref([
  { number: '201', type: '标准单人间' },
  { number: '202', type: '标准单人间' },
  { number: '301', type: '标准双人间' },
  { number: '302', type: '标准双人间' }
])

// 保洁员列表
const cleaners = ref([
  { id: 1, name: '张阿姨' },
  { id: 2, name: '李阿姨' },
  { id: 3, name: '王阿姨' }
])

// 过滤后的任务列表
const filteredTasks = computed(() => {
  if (taskFilter.value === 'all') return taskList.value
  return taskList.value.filter(task => task.status === taskFilter.value)
})

// 任务表单
const dialogVisible = ref(false)
const taskFormRef = ref(null)
const taskForm = reactive({
  roomNumber: '',
  cleaner: '',
  priority: 'medium',
  expectedTime: null,
  notes: ''
})

// 完成清洁表单
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const completeForm = reactive({
  actualDuration: 30,
  cleanItems: [],
  supplies: [],
  issues: ''
})
const currentTask = ref(null)

// 表单验证规则
const taskFormRules = {
  roomNumber: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  cleaner: [
    { required: true, message: '请选择保洁员', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  expectedTime: [
    { required: true, message: '请选择预计完成时间', trigger: 'change' }
  ]
}

const completeFormRules = {
  actualDuration: [
    { required: true, message: '请输入实际用时', trigger: 'blur' }
  ],
  cleanItems: [
    { type: 'array', required: true, message: '请至少选择一个清洁项目', trigger: 'change' }
  ],
  supplies: [
    { type: 'array', required: true, message: '请至少选择一个物品补充项目', trigger: 'change' }
  ]
}

// 状态和优先级处理函数
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success'
  }
  return statusMap[status]
}

const getStatusLabel = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成'
  }
  return statusMap[status]
}

const getPriorityType = (priority) => {
  const priorityMap = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return priorityMap[priority]
}

const getPriorityLabel = (priority) => {
  const priorityMap = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return priorityMap[priority]
}

// 分配任务
const handleAdd = () => {
  Object.keys(taskForm).forEach(key => {
    taskForm[key] = key === 'priority' ? 'medium' : ''
  })
  taskForm.expectedTime = null
  dialogVisible.value = true
}

// 提交任务分配
const handleSubmit = async () => {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API保存任务信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('任务分配成功')
        dialogVisible.value = false
        // 刷新任务列表
        fetchTaskList()
      } catch (error) {
        console.error('任务分配失败:', error)
        ElMessage.error('任务分配失败')
      }
    }
  })
}

// 开始清洁
const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确定要开始处理该清洁任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // TODO: 调用后端API更新任务状态
    await new Promise(resolve => setTimeout(resolve, 1000))
    row.status = 'processing'
    ElMessage.success('已开始处理任务')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始任务失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 完成清洁
const handleComplete = (row) => {
  currentTask.value = row
  completeForm.actualDuration = 30
  completeForm.cleanItems = []
  completeForm.supplies = []
  completeForm.issues = ''
  completeDialogVisible.value = true
}

// 提交完成清洁
const handleCompleteSubmit = async () => {
  if (!completeFormRef.value || !currentTask.value) return

  await completeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API更新任务状态和完成信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        currentTask.value.status = 'completed'
        ElMessage.success('清洁任务已完成')
        completeDialogVisible.value = false
      } catch (error) {
        console.error('提交完成信息失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 查看检查
const handleInspect = (row) => {
  // TODO: 实现查看检查功能
  console.log('查看任务检查详情:', row)
}

// 获取任务列表
const fetchTaskList = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取任务列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    loading.value = false
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
    loading.value = false
  }
}

// 初始化
fetchTaskList()
</script>

<style scoped>
.tasks-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.task-stats {
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: #fff;
}

.stat-icon.pending {
  background-color: #e6a23c;
}

.stat-icon.processing {
  background-color: #409eff;
}

.stat-icon.completed {
  background-color: #67c23a;
}

.stat-icon.high-priority {
  background-color: #f56c6c;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-count {
  font-size: 24px;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
}

.unit-text {
  margin-left: 10px;
  color: #606266;
}
</style>