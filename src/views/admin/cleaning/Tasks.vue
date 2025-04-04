<template>
  <div class="mobile-cleaning-tasks">
    <!-- 页面头部 -->
    <div class="mobile-header">
      <h2>清洁任务</h2>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <!-- 任务统计卡片 - 水平滚动 -->
    <div class="stat-cards-wrapper">
      <div class="stat-cards">
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
    </div>

    <!-- 任务列表 -->
    <div class="task-list-section">
      <div class="section-header">
        <h3>{{ taskFilterTitle }}</h3>
        <div class="filter-toggle">
          <el-radio-group v-model="taskFilter" size="large" @change="updateTaskFilterTitle">
            <el-radio-button label="pending">待处理</el-radio-button>
            <el-radio-button label="processing">进行中</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 移动版任务卡片列表 -->
      <div class="task-cards" v-loading="loading">
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
          <div class="task-header">
            <div class="room-info">
              <span class="room-number">{{ task.roomNumber }}</span>
              <span class="room-type">{{ task.roomType }}</span>
            </div>
            <div class="task-status-tag" :class="task.status">
              {{ getStatusLabel(task.status) }}
            </div>
          </div>
          
          <div class="task-body">
            <div class="task-detail">
              <div class="detail-item">
                <div class="item-label">优先级：</div>
                <div class="item-value priority" :class="`priority-${task.priority}`">
                  {{ getPriorityLabel(task.priority) }}
                </div>
              </div>
              
              <div class="detail-item">
                <div class="item-label">预计完成：</div>
                <div class="item-value">{{ task.expectedTime }}</div>
              </div>
              
              <div class="detail-item">
                <div class="item-label">保洁员：</div>
                <div class="item-value">{{ task.cleaner }}</div>
              </div>
              
              <div class="detail-item notes">
                <div class="item-label">备注：</div>
                <div class="item-value">{{ task.notes }}</div>
              </div>
            </div>
            
            <div class="task-actions">
              <!-- 待处理任务 -->
              <el-button 
                v-if="task.status === 'pending'"
                type="primary" 
                size="large" 
                round
                @click="handleStart(task)"
                class="action-button start-button"
              >
                开始清洁
              </el-button>
              
              <!-- 进行中任务 -->
              <el-button 
                v-if="task.status === 'processing'"
                type="success" 
                size="large" 
                round
                @click="handleComplete(task)"
                class="action-button complete-button"
              >
                完成清洁
              </el-button>
              
              <!-- 已完成任务 -->
              <el-button 
                v-if="task.status === 'completed'"
                type="info" 
                size="large" 
                round
                @click="handleInspect(task)"
                class="action-button inspect-button"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="filteredTasks.length === 0" class="empty-tasks">
          <el-empty description="暂无任务" :image-size="120">
            <span class="empty-tip">当前没有{{ taskFilterTitle }}，请查看其他分类</span>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 管理员才能看到的分配任务按钮 -->
    <div v-if="userRole === 'admin'" class="floating-action">
      <el-button type="primary" circle size="large" @click="handleAdd" class="add-button">
        <el-icon><Plus /></el-icon>
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

    <!-- 完成清洁对话框 -->
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
  {
    roomNumber: '302',
    roomType: '标准双人间',
    priority: 'medium',
    cleaner: '张阿姨',
    status: 'processing',
    expectedTime: '11:00',
    notes: '请更换浴室毛巾'
  },
  {
    roomNumber: '401',
    roomType: '豪华套房',
    priority: 'high',
    cleaner: '王阿姨',
    status: 'pending',
    expectedTime: '10:45',
    notes: 'VIP客人，请注意细节清洁'
  },
  {
    roomNumber: '201',
    roomType: '标准单人间',
    priority: 'low',
    cleaner: '李阿姨',
    status: 'completed',
    expectedTime: '09:30',
    notes: '已完成'
  },
  {
    roomNumber: '202',
    roomType: '标准单人间',
    priority: 'medium',
    cleaner: '张阿姨',
    status: 'completed',
    expectedTime: '09:45',
    notes: '已完成'
  },
  {
    roomNumber: '303',
    roomType: '标准双人间',
    priority: 'low',
    cleaner: '王阿姨',
    status: 'processing',
    expectedTime: '11:15',
    notes: '一般清洁'
  }
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
        
        // 更新任务状态
        currentTask.value.status = 'completed'
        
        // 更新任务计数
        const processingIndex = taskStats.findIndex(s => s.type === 'processing');
        const completedIndex = taskStats.findIndex(s => s.type === 'completed');
        if (processingIndex !== -1 && completedIndex !== -1) {
          taskStats[processingIndex].count--;
          taskStats[completedIndex].count++;
        }
        
        // 更新房间状态为可入住（这里只是模拟，实际应该调用API）
        updateRoomStatus(currentTask.value.roomNumber, 'available');
        
        ElMessage.success('清洁任务已完成，房间状态已更新为可入住')
        completeDialogVisible.value = false
      } catch (error) {
        console.error('提交完成信息失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 更新房间状态
const updateRoomStatus = (roomNumber, status) => {
  // 这里模拟房间状态更新，实际项目中应该调用后端API
  console.log(`房间${roomNumber}状态已更新为${status}`);
  
  // 如果有房间管理相关的状态，也可以在此更新
  const room = availableRooms.value.find(r => r.number === roomNumber);
  if (room) {
    room.status = status;
  }
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
    
    // 更新任务统计数据
    updateTaskStats();
    
    // 更新任务筛选标题
    updateTaskFilterTitle();
    
    loading.value = false
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
    loading.value = false
  }
}

// 更新任务统计数据
const updateTaskStats = () => {
  const pendingCount = taskList.value.filter(task => task.status === 'pending').length;
  const processingCount = taskList.value.filter(task => task.status === 'processing').length;
  const completedCount = taskList.value.filter(task => task.status === 'completed').length;
  const highPriorityCount = taskList.value.filter(task => task.priority === 'high').length;
  
  taskStats[0].count = pendingCount;
  taskStats[1].count = processingCount;
  taskStats[2].count = completedCount;
  taskStats[3].count = highPriorityCount;
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

// 初始化
fetchTaskList()
</script>

<style scoped>
/* 移动端优化样式 */
.mobile-cleaning-tasks {
  padding: 10px;
  max-width: 100%;
  background-color: #f5f5f5;
  min-height: 100vh;
  font-family: "Microsoft YaHei", Arial, sans-serif;
}

/* 页面头部 */
.mobile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 5px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.mobile-header h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
  font-weight: 600;
}

.current-time {
  font-size: 18px;
  font-weight: 500;
  color: #666;
}

/* 统计卡片 */
.stat-cards-wrapper {
  margin-bottom: 20px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding: 5px 0;
}

.stat-cards {
  display: flex;
  gap: 10px;
  padding: 5px;
  min-width: max-content;
}

.stat-card {
  flex: 0 0 150px;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
  border-top: 5px solid transparent;
}

.stat-card.active {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  border-top-color: #e6a23c;
}

.stat-card.processing {
  border-top-color: #409eff;
}

.stat-card.completed {
  border-top-color: #67c23a;
}

.stat-card.high-priority {
  border-top-color: #f56c6c;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.stat-card.pending .stat-icon {
  background-color: #e6a23c;
}

.stat-card.processing .stat-icon {
  background-color: #409eff;
}

.stat-card.completed .stat-icon {
  background-color: #67c23a;
}

.stat-card.high-priority .stat-icon {
  background-color: #f56c6c;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: #fff;
}

.stat-info {
  text-align: center;
}

.stat-count {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 5px;
  line-height: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

/* 任务列表区域 */
.task-list-section {
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.filter-toggle {
  margin-bottom: 15px;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.filter-toggle .el-radio-button {
  margin-right: 5px;
}

/* 任务卡片 */
.task-cards {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.task-card {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  border-left: 5px solid #ddd;
}

.task-card.high-priority {
  border-left-color: #f56c6c;
}

.task-card.medium-priority {
  border-left-color: #e6a23c;
}

.task-card.low-priority {
  border-left-color: #909399;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.room-info {
  display: flex;
  align-items: baseline;
}

.room-number {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin-right: 10px;
}

.room-type {
  font-size: 14px;
  color: #666;
}

.task-status-tag {
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 13px;
  font-weight: 500;
}

.task-status-tag.pending {
  background-color: #fef6e9;
  color: #e6a23c;
}

.task-status-tag.processing {
  background-color: #ebf5ff;
  color: #409eff;
}

.task-status-tag.completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.task-body {
  display: flex;
  flex-direction: column;
}

.task-detail {
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
  font-size: 16px;
}

.item-label {
  width: 90px;
  color: #666;
  flex-shrink: 0;
}

.item-value {
  flex: 1;
  color: #333;
}

.item-value.priority {
  font-weight: 600;
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

.detail-item.notes {
  align-items: flex-start;
}

.task-actions {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.action-button {
  width: 100%;
  max-width: 200px;
  height: 50px;
  font-size: 18px;
}

.start-button {
  background-color: #409eff;
  border-color: #409eff;
}

.complete-button {
  background-color: #67c23a;
  border-color: #67c23a;
}

.inspect-button {
  background-color: #909399;
  border-color: #909399;
}

/* 空状态 */
.empty-tasks {
  padding: 30px 0;
  text-align: center;
}

.empty-tip {
  color: #999;
  font-size: 14px;
  margin-top: 10px;
  display: block;
}

/* 浮动操作按钮 */
.floating-action {
  position: fixed;
  right: 20px;
  bottom: 30px;
  z-index: 10;
}

.add-button {
  width: 60px;
  height: 60px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.add-button .el-icon {
  font-size: 24px;
}

/* 完成清洁对话框自定义样式 */
.complete-dialog :deep(.el-dialog__body) {
  padding: 20px 15px;
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
}

.checkbox-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox-icon {
  width: 24px;
  height: 24px;
  margin-right: 8px;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
}

.duration-display {
  text-align: center;
  font-size: 18px;
  color: #333;
  font-weight: 500;
  margin-top: 10px;
}

.checkbox-item :deep(.el-checkbox) {
  margin-right: 0;
  margin-bottom: 0;
  height: 46px;
}

.checkbox-item :deep(.el-checkbox__label) {
  font-size: 16px;
}

/* 对话框样式 */
.mobile-dialog :deep(.el-dialog__title) {
  font-size: 20px;
}

.mobile-dialog :deep(.el-form-item__label) {
  font-size: 16px;
}

.mobile-dialog :deep(.el-input__inner),
.mobile-dialog :deep(.el-textarea__inner) {
  font-size: 16px;
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
}

@media screen and (max-width: 480px) {
  .room-number {
    font-size: 20px;
  }
  
  .detail-item {
    font-size: 15px;
  }
  
  .item-label {
    width: 80px;
  }
  
  .action-button {
    height: 46px;
    font-size: 16px;
  }
}
</style>