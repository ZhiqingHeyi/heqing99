<template>
  <div class="mobile-cleaning-tasks">
    <!-- 页面头部 -->
    <div class="mobile-header">
      <h2>清洁任务</h2>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <!-- 任务统计卡片 - 大图标风格 -->
    <div class="stat-cards-grid">
      <div 
        v-for="stat in taskStats" 
        :key="stat.type" 
        @click="handleFilterByType(stat.type)" 
        class="stat-card" 
        :class="{ active: isActiveFilter(stat.type), [stat.type]: true }"
      >
        <div class="stat-icon">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-count">{{ stat.count }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </div>
    </div>

    <!-- 简化过滤选项 - 更大的按钮 -->
    <div class="filter-buttons">
      <el-button 
        size="large" 
        :type="taskFilter === 'pending' ? 'primary' : 'default'"
        @click="taskFilter = 'pending'; updateTaskFilterTitle()"
        class="filter-button"
      >
        待处理
      </el-button>
      <el-button 
        size="large" 
        :type="taskFilter === 'processing' ? 'primary' : 'default'"
        @click="taskFilter = 'processing'; updateTaskFilterTitle()"
        class="filter-button"
      >
        进行中
      </el-button>
      <el-button 
        size="large" 
        :type="taskFilter === 'completed' ? 'primary' : 'default'"
        @click="taskFilter = 'completed'; updateTaskFilterTitle()"
        class="filter-button"
      >
        已完成
      </el-button>
    </div>

    <!-- 任务卡片网格布局 -->
    <div class="task-grid" v-loading="loading">
      <div 
        v-for="task in filteredTasks" 
        :key="task.roomNumber" 
        class="task-card"
        :class="{ 
          'high-priority': task.priority === 'high',
          'medium-priority': task.priority === 'medium',
          'low-priority': task.priority === 'low',
          'status-pending': task.status === 'pending',
          'status-processing': task.status === 'processing',
          'status-completed': task.status === 'completed'
        }"
      >
        <div class="card-room-number">{{ task.roomNumber }}</div>
        <div class="card-room-type">{{ task.roomType }}</div>
        
        <div class="card-status" :class="task.status">
          {{ getStatusLabel(task.status) }}
        </div>
        
        <div class="card-priority" :class="`priority-${task.priority}`">
          {{ getPriorityLabel(task.priority) }}
        </div>
        
        <div class="card-time">
          <el-icon><Clock /></el-icon>
          {{ task.expectedTime }}
        </div>
        
        <div class="card-action">
          <el-button 
            v-if="task.status === 'pending'"
            type="primary" 
            size="large" 
            round
            @click="handleStart(task)"
          >
            开始
          </el-button>
          
          <el-button 
            v-if="task.status === 'processing'"
            type="success" 
            size="large" 
            round
            @click="handleComplete(task)"
          >
            完成
          </el-button>
          
          <el-button 
            v-if="task.status === 'completed'"
            type="info" 
            size="large" 
            round
            @click="handleInspect(task)"
          >
            查看
          </el-button>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="filteredTasks.length === 0" class="empty-tasks">
        <el-empty description="暂无任务" :image-size="120">
          <span class="empty-tip">当前没有{{ taskFilterTitle }}，请查看其他分类</span>
        </el-empty>
      </div>
    </div>

    <!-- 管理员才能看到的分配任务按钮 -->
    <div v-if="userRole === 'admin'" class="floating-action">
      <el-button type="primary" circle size="large" @click="handleAdd" class="add-button">
        <el-icon><Plus /></el-icon>
      </el-button>
      <el-button type="success" circle size="large" @click="handleGenerateTasks" class="generate-button">
        <el-icon><Loading /></el-icon>
      </el-button>
    </div>

    <!-- 分配任务对话框 -->
    <el-dialog
      title="分配清洁任务"
      v-model="dialogVisible"
      width="90%"
      class="mobile-dialog"
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
        <div class="dialog-footer">
          <el-button size="large" @click="dialogVisible = false">取消</el-button>
          <el-button size="large" type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 完成清洁对话框 - 增大尺寸更易于操作 -->
    <el-dialog
      title="完成清洁"
      v-model="completeDialogVisible"
      width="90%"
      class="mobile-dialog complete-dialog"
    >
      <el-form
        ref="completeFormRef"
        :model="completeForm"
        :rules="completeFormRules"
        label-position="top"
      >
        <el-form-item label="实际用时(分钟)" prop="actualDuration">
          <el-slider
            v-model="completeForm.actualDuration"
            :min="5"
            :max="120"
            :step="5"
            :marks="{
              5: '5',
              30: '30',
              60: '60',
              90: '90',
              120: '120'
            }"
            height="40px"
          />
          <div class="duration-display">{{ completeForm.actualDuration }} 分钟</div>
        </el-form-item>
        
        <el-form-item label="清洁项目" prop="cleanItems" class="checkbox-item">
          <div class="checkbox-grid">
            <el-checkbox label="bedding" size="large" border v-model="completeForm.cleanItems">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-bed"></i>
                <span>床品更换</span>
              </div>
            </el-checkbox>
            <el-checkbox label="bathroom" size="large" border v-model="completeForm.cleanItems">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-bathroom"></i>
                <span>浴室清洁</span>
              </div>
            </el-checkbox>
            <el-checkbox label="floor" size="large" border v-model="completeForm.cleanItems">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-floor"></i>
                <span>地面清洁</span>
              </div>
            </el-checkbox>
            <el-checkbox label="furniture" size="large" border v-model="completeForm.cleanItems">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-furniture"></i>
                <span>家具除尘</span>
              </div>
            </el-checkbox>
            <el-checkbox label="garbage" size="large" border v-model="completeForm.cleanItems">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-garbage"></i>
                <span>垃圾清理</span>
              </div>
            </el-checkbox>
          </div>
        </el-form-item>
        
        <el-form-item label="物品补充" prop="supplies" class="checkbox-item">
          <div class="checkbox-grid">
            <el-checkbox label="toiletries" size="large" border v-model="completeForm.supplies">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-toiletries"></i>
                <span>洗漱用品</span>
              </div>
            </el-checkbox>
            <el-checkbox label="towels" size="large" border v-model="completeForm.supplies">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-towels"></i>
                <span>毛巾浴巾</span>
              </div>
            </el-checkbox>
            <el-checkbox label="water" size="large" border v-model="completeForm.supplies">
              <div class="checkbox-content">
                <i class="checkbox-icon icon-water"></i>
                <span>饮用水</span>
              </div>
            </el-checkbox>
          </div>
        </el-form-item>
        
        <el-form-item label="特殊情况" prop="issues">
          <el-input
            v-model="completeForm.issues"
            type="textarea"
            rows="3"
            placeholder="请记录房间内发现的问题(如有)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="completeDialogVisible = false">取消</el-button>
          <el-button size="large" type="primary" @click="handleCompleteSubmit">确认完成</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Loading, Check, Warning, Plus } from '@element-plus/icons-vue'
import { cleaningApi } from '@/api/index'

// 获取用户角色
const userRole = ref(localStorage.getItem('userRole') || 'cleaner')

// 当前时间
const currentTime = ref('')
const updateCurrentTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

// 初始化当前时间并每分钟更新
onMounted(() => {
  updateCurrentTime()
  setInterval(updateCurrentTime, 60000)
})

// 任务统计数据
const taskStats = reactive([
  {
    title: '待处理任务',
    count: 0,
    type: 'pending',
    icon: Clock
  },
  {
    title: '进行中任务',
    count: 0,
    type: 'processing',
    icon: Loading
  },
  {
    title: '已完成任务',
    count: 0,
    type: 'completed',
    icon: Check
  },
  {
    title: '高优先级',
    count: 0,
    type: 'high-priority',
    icon: Warning
  }
])

// 任务列表数据
const loading = ref(false)
const taskFilter = ref('all')
const taskList = ref([])

// 可用房间列表
const availableRooms = ref([])

// 保洁员列表
const cleaners = ref([])

// 过滤后的任务列表
const filteredTasks = computed(() => {
  if (taskFilter.value === 'all') {
    return taskList.value
  } else if (taskFilter.value === 'high-priority') {
    return taskList.value.filter(task => task.priority === 'high')
  } else {
    return taskList.value.filter(task => task.status === taskFilter.value)
  }
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

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '暂无'
  
  try {
    const date = new Date(dateTimeStr)
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (e) {
    console.error('日期格式化错误:', e)
    return dateTimeStr || '暂无'
  }
}

// 分配任务
const handleAdd = () => {
  Object.keys(taskForm).forEach(key => {
    taskForm[key] = key === 'priority' ? 'medium' : ''
  })
  taskForm.expectedTime = null
  dialogVisible.value = true
  
  // 获取可用房间和保洁员列表
  fetchAvailableRooms()
  fetchAvailableCleaners()
}

// 提交任务分配
const handleSubmit = async () => {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        // 调用后端API保存任务信息
        await cleaningApi.createTask(taskForm)
        
        ElMessage.success('任务分配成功')
        dialogVisible.value = false
        // 刷新任务列表
        await fetchTaskList()
      } catch (error) {
        console.error('任务分配失败:', error)
        ElMessage.error('任务分配失败')
      } finally {
        loading.value = false
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
    
    // 调用后端API更新任务状态
    await cleaningApi.updateTaskStatus(row.id, 'processing')
    
    // 更新本地状态
    row.status = 'processing'
    
    // 更新任务统计数据
    const pendingIndex = taskStats.findIndex(s => s.type === 'pending')
    const processingIndex = taskStats.findIndex(s => s.type === 'processing')
    if (pendingIndex !== -1 && processingIndex !== -1) {
      taskStats[pendingIndex].count--
      taskStats[processingIndex].count++
    }
    
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
        // 调用后端API更新任务状态和完成信息
        await cleaningApi.updateTaskStatus(currentTask.value.id, 'completed', {
          actualDuration: completeForm.actualDuration,
          cleanItems: completeForm.cleanItems.join(','),
          supplies: completeForm.supplies.join(','),
          issues: completeForm.issues
        })
        
        // 更新任务状态
        currentTask.value.status = 'completed'
        
        // 更新任务计数
        const processingIndex = taskStats.findIndex(s => s.type === 'processing')
        const completedIndex = taskStats.findIndex(s => s.type === 'completed')
        if (processingIndex !== -1 && completedIndex !== -1) {
          taskStats[processingIndex].count--
          taskStats[completedIndex].count++
        }
        
        ElMessage.success('清洁任务已完成，房间状态已更新为可入住')
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
  // 跳转到查看详情页面或显示详情对话框
  ElMessage.info(`查看任务详情: ${row.roomNumber}`)
}

// 获取任务列表
const fetchTaskList = async () => {
  loading.value = true
  try {
    // 调用后端API获取任务列表
    const response = await cleaningApi.getTasks()
    
    // 更新任务列表
    if (response) {
      if (response.content) {
        // 后端直接返回的分页数据
        taskList.value = response.content
      } else {
        // 如果是包装的响应
        if (response.data && response.data.content) {
          taskList.value = response.data.content
        } else {
          // 直接使用响应数据
          taskList.value = response
        }
      }
      
      console.log('获取到任务列表:', taskList.value)
    }
    
    // 获取任务统计数据
    await fetchTaskStatistics()
    
    // 更新任务筛选标题
    updateTaskFilterTitle()
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 获取任务统计数据
const fetchTaskStatistics = async () => {
  try {
    const response = await cleaningApi.getTaskStatistics()
    if (response && response.data) {
      taskStats[0].count = response.data.pendingCount || 0
      taskStats[1].count = response.data.processingCount || 0
      taskStats[2].count = response.data.completedCount || 0
      taskStats[3].count = response.data.highPriorityCount || 0
    }
  } catch (error) {
    console.error('获取任务统计信息失败:', error)
  }
}

// 获取可用房间列表
const fetchAvailableRooms = async () => {
  try {
    const response = await cleaningApi.getAvailableRooms()
    if (response && response.data) {
      availableRooms.value = response.data.map(room => ({
        number: room.roomNumber,
        type: room.roomType
      }))
    }
  } catch (error) {
    console.error('获取可用房间列表失败:', error)
    ElMessage.error('获取可用房间列表失败')
  }
}

// 获取可用保洁员列表
const fetchAvailableCleaners = async () => {
  try {
    const response = await cleaningApi.getAvailableCleaners()
    if (response && response.data) {
      cleaners.value = response.data.map(cleaner => ({
        id: cleaner.id,
        name: cleaner.name
      }))
    }
  } catch (error) {
    console.error('获取可用保洁员列表失败:', error)
    ElMessage.error('获取可用保洁员列表失败')
  }
}

// 处理卡片点击筛选
const handleFilterByType = (type) => {
  if (type === 'high-priority') {
    // 高优先级筛选
    taskFilter.value = 'high-priority'
  } else {
    // 普通状态筛选
    taskFilter.value = type
  }
  // 更新表格标题
  updateTaskFilterTitle()
}

// 判断是否为当前激活的筛选器
const isActiveFilter = (type) => {
  if (type === 'high-priority' && taskFilter.value === 'high-priority') {
    return true
  }
  return taskFilter.value === type
}

// 更新任务筛选标题
const updateTaskFilterTitle = () => {
  const filterTitles = {
    'all': '全部',
    'pending': '待处理任务',
    'processing': '进行中任务',
    'completed': '已完成任务',
    'high-priority': '高优先级任务'
  }
  taskFilterTitle.value = filterTitles[taskFilter.value] || '任务列表'
}

// 任务筛选标题
const taskFilterTitle = ref('任务列表')

// 手动生成清洁任务
const handleGenerateTasks = async () => {
  ElMessageBox.confirm('确定要从需要清洁的房间自动生成任务吗?', '生成任务', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    loading.value = true
    try {
      const response = await cleaningApi.generateTasks()
      if (response && response.data) {
        ElMessage.success(`成功生成 ${response.data.generatedTasksCount} 个清洁任务`)
        // 刷新任务列表
        await fetchTaskList()
      }
    } catch (error) {
      console.error('生成任务失败:', error)
      ElMessage.error('生成任务失败: ' + (error.message || '未知错误'))
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  fetchTaskList()
})
</script>

<style scoped>
/* 移动端样式优化 */
.mobile-cleaning-tasks {
  padding: 0;
  min-height: 100vh;
}

.mobile-header {
  background-color: #f0f6ff;
  color: #333;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
}

.mobile-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.current-time {
  font-size: 16px;
  font-weight: 500;
  color: #666;
}

/* 统计卡片网格布局 */
.stat-cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 0 15px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  height: 110px;
}

.stat-card.active {
  transform: scale(0.98);
  border: 2px solid;
}

.stat-card.pending {
  border-color: #e6a23c;
}

.stat-card.processing {
  border-color: #409eff;
}

.stat-card.completed {
  border-color: #67c23a;
}

.stat-card.high-priority {
  border-color: #f56c6c;
}

.stat-icon {
  font-size: 30px;
  margin-bottom: 10px;
  color: #409eff;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stat-icon .el-icon {
  font-size: 30px;
}

.stat-card.pending .stat-icon {
  color: #e6a23c;
}

.stat-card.processing .stat-icon {
  color: #409eff;
}

.stat-card.completed .stat-icon {
  color: #67c23a;
}

.stat-card.high-priority .stat-icon {
  color: #f56c6c;
}

.stat-count {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

/* 过滤按钮 */
.filter-buttons {
  display: flex;
  justify-content: space-between;
  padding: 0 15px;
  margin-bottom: 20px;
}

.filter-button {
  flex: 1;
  margin: 0 5px;
  height: 46px;
  font-size: 16px;
}

/* 任务卡片网格 */
.task-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 0 15px 80px;
}

.task-card {
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 180px;
}

.task-card.high-priority {
  border-top: 4px solid #f56c6c;
}

.task-card.medium-priority {
  border-top: 4px solid #e6a23c;
}

.task-card.low-priority {
  border-top: 4px solid #909399;
}

.card-room-number {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 5px;
}

.card-room-type {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin-bottom: 15px;
}

.card-status {
  padding: 5px;
  text-align: center;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 10px;
}

.card-status.pending {
  background-color: #fef6e9;
  color: #e6a23c;
}

.card-status.processing {
  background-color: #ebf5ff;
  color: #409eff;
}

.card-status.completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.card-priority {
  display: inline-block;
  text-align: center;
  width: 100%;
  font-weight: 600;
  margin-bottom: 8px;
}

.priority-high {
  color: #f56c6c;
}

.priority-medium {
  color: #e6a23c;
}

.priority-low {
  color: #909399;
}

.card-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: #606266;
  margin-bottom: 15px;
}

.card-action {
  margin-top: auto;
  display: flex;
  justify-content: center;
}

.card-action .el-button {
  min-width: 0;
  width: 100%;
}

/* 空状态 */
.empty-tasks {
  grid-column: span 2;
  padding: 30px 0;
  text-align: center;
}

.empty-tip {
  color: #999;
  font-size: 14px;
  margin-top: 10px;
  display: block;
}

/* 浮动按钮 */
.floating-action {
  position: fixed;
  right: 20px;
  bottom: 30px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.add-button, .generate-button {
  width: 60px;
  height: 60px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.add-button .el-icon, .generate-button .el-icon {
  font-size: 24px;
}

/* 对话框优化 */
.mobile-dialog :deep(.el-dialog__title) {
  font-size: 20px;
}

.mobile-dialog :deep(.el-form-item__label) {
  font-size: 16px;
}

.mobile-dialog :deep(.el-input__inner),
.mobile-dialog :deep(.el-textarea__inner) {
  font-size: 16px;
  height: 46px;
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.checkbox-content {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.checkbox-icon {
  width: 24px;
  height: 24px;
  margin-right: 8px;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
}

.checkbox-item :deep(.el-checkbox__input) {
  transform: scale(1.2);
}

.checkbox-item :deep(.el-checkbox__label) {
  font-size: 16px;
}

.checkbox-item :deep(.el-checkbox) {
  height: 50px;
  margin-right: 0;
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.duration-display {
  text-align: center;
  font-size: 20px;
  color: #333;
  font-weight: 600;
  margin-top: 15px;
}

/* 完成清洁对话框优化 */
.complete-dialog :deep(.el-slider__runway) {
  height: 10px;
  margin: 20px 0;
}

.complete-dialog :deep(.el-slider__bar) {
  height: 10px;
}

.complete-dialog :deep(.el-slider__button) {
  width: 24px;
  height: 24px;
}

.complete-dialog :deep(.el-slider__marks-text) {
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  padding: 0 10px;
}

.dialog-footer .el-button {
  flex: 1;
  margin: 0 5px;
  font-size: 16px;
  height: 46px;
}

@media screen and (max-width: 480px) {
  .task-grid {
    grid-template-columns: 1fr;
  }
  
  .empty-tasks {
    grid-column: span 1;
  }
  
  .stat-cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-button {
    font-size: 14px;
    padding: 0 5px;
  }
  
  .checkbox-grid {
    grid-template-columns: 1fr;
  }
}
</style>