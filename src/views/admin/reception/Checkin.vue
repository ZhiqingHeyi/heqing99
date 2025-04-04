<template>
  <div class="checkin-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">入住登记</span></h2>
        <p class="header-description">管理客人入住信息、房间分配和预订转入住</p>
      </div>
    </div>

    <!-- 房间状态概览 -->
    <el-row :gutter="24" class="room-status-row">
      <el-col :span="6" v-for="status in roomStatusList" :key="status.type">
        <el-card class="status-card" shadow="hover" @click="showRoomsByType(status.type)"
          :class="`status-card-${status.type}`">
          <div class="status-content">
            <div class="status-icon">
              <el-icon><component :is="status.icon" /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-count">{{ status.count }}</div>
              <div class="status-title">{{ status.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 入住登记表单 -->
    <el-card class="checkin-form-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Document /></el-icon> 入住信息登记</span>
        </div>
      </template>
      
      <el-form
        ref="checkinFormRef"
        :model="checkinForm"
        :rules="checkinFormRules"
        label-width="100px"
        class="checkin-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预订号" prop="bookingNo">
              <el-input 
                v-model="checkinForm.bookingNo"
                placeholder="如有预订请输入预订号"
                clearable
              >
                <template #append>
                  <el-button @click="handleSearchBooking" class="search-booking-btn">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住类型" prop="checkinType">
              <el-radio-group v-model="checkinForm.checkinType" class="custom-radio-group">
                <el-radio label="normal">普通入住</el-radio>
                <el-radio label="booking">预订入住</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客人姓名" prop="guestName">
              <el-input v-model="checkinForm.guestName" placeholder="请输入客人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="checkinForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="checkinForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住人数" prop="guestCount">
              <el-input-number v-model="checkinForm.guestCount" :min="1" :max="4" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间类型" prop="roomType">
              <el-select v-model="checkinForm.roomType" placeholder="请选择房间类型" style="width: 100%">
                <el-option label="豪华大床房" value="deluxe" />
                <el-option label="行政套房" value="executive" />
                <el-option label="家庭套房" value="family" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间号" prop="roomNumber">
              <el-select 
                v-model="checkinForm.roomNumber"
                placeholder="请选择房间号"
                style="width: 100%"
                :disabled="!checkinForm.roomType"
                filterable
              >
                <el-option
                  v-for="room in availableRooms"
                  :key="room.number"
                  :label="`${room.number} (${room.type})`"
                  :value="room.number"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入住日期" prop="dateRange">
              <el-date-picker
                v-model="checkinForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="入住日期"
                end-placeholder="离店日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金" prop="deposit">
              <el-input-number
                v-model="checkinForm.deposit"
                :min="0"
                :step="100"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="checkinForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>

        <el-form-item class="form-buttons">
          <el-button type="primary" @click="handleSubmit" class="submit-btn">
            <el-icon><Check /></el-icon>确认入住
          </el-button>
          <el-button @click="resetForm" class="reset-btn">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间列表对话框 -->
    <el-dialog
      v-model="roomListDialogVisible"
      :title="roomListDialogTitle"
      width="75%"
      destroy-on-close
      class="custom-dialog"
    >
      <el-table :data="filteredRooms" style="width: 100%" border stripe highlight-current-row class="room-table">
        <el-table-column prop="number" label="房间号" width="100" />
        <el-table-column prop="typeName" label="房间类型" width="120" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getRoomStatusType(scope.row.status)" effect="light" class="status-tag">
              {{ getRoomStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元/晚)" width="120" />
        <el-table-column prop="guestName" label="客人姓名" width="120" />
        <el-table-column prop="checkoutDate" label="预计退房时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                v-if="currentRoomType === 'available'"
                @click="selectRoomForCheckin(scope.row)"
                class="action-btn"
              >
                <el-icon><Select /></el-icon>选择入住
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                v-if="currentRoomType === 'occupied'"
                @click="showCheckoutDialog(scope.row)"
                class="action-btn"
              >
                <el-icon><SwitchButton /></el-icon>办理退房
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                v-if="currentRoomType === 'cleaning'"
                @click="markRoomAsClean(scope.row)"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>标记清洁完成
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, Key, Tools, Check, Document, Search, 
  Refresh, Select, SwitchButton, View
} from '@element-plus/icons-vue'

// 房间状态数据
const roomStatusList = reactive([
  {
    title: '空闲房间',
    count: 25,
    type: 'available',
    icon: House
  },
  {
    title: '已入住',
    count: 45,
    type: 'occupied',
    icon: Key
  },
  {
    title: '待清洁',
    count: 8,
    type: 'cleaning',
    icon: Tools
  },
  {
    title: '今日预订',
    count: 12,
    type: 'booked',
    icon: Check
  }
])

// 可用房间列表
const availableRooms = ref([
  { number: '201', type: '标准单人间' },
  { number: '202', type: '标准单人间' },
  { number: '301', type: '豪华大床房' },
  { number: '302', type: '豪华大床房' },
  { number: '501', type: '行政套房' }
])

// 房间列表对话框相关
const roomListDialogVisible = ref(false)
const roomListDialogTitle = ref('')
const currentRoomType = ref('')

// 模拟所有房间数据
const allRooms = reactive([
  { 
    number: '201', 
    typeName: '标准单人间', 
    type: 'single', 
    status: 'available', 
    price: 198, 
    guestName: '', 
    checkoutDate: '' 
  },
  { 
    number: '202', 
    typeName: '标准单人间', 
    type: 'single', 
    status: 'available', 
    price: 198, 
    guestName: '', 
    checkoutDate: '' 
  },
  { 
    number: '203', 
    typeName: '标准单人间', 
    type: 'single', 
    status: 'occupied', 
    price: 198, 
    guestName: '李四', 
    checkoutDate: '2023-07-15 12:00' 
  },
  { 
    number: '301', 
    typeName: '豪华大床房', 
    type: 'double', 
    status: 'occupied', 
    price: 298, 
    guestName: '王五', 
    checkoutDate: '2023-07-14 12:00' 
  },
  { 
    number: '302', 
    typeName: '豪华大床房', 
    type: 'double', 
    status: 'cleaning', 
    price: 298, 
    guestName: '', 
    checkoutDate: '' 
  },
  { 
    number: '303', 
    typeName: '豪华大床房', 
    type: 'double', 
    status: 'cleaning', 
    price: 298, 
    guestName: '', 
    checkoutDate: '' 
  },
  { 
    number: '401', 
    typeName: '行政套房', 
    type: 'suite', 
    status: 'booked', 
    price: 598, 
    guestName: '赵六', 
    checkoutDate: '2023-07-18 12:00' 
  },
  { 
    number: '402', 
    typeName: '行政套房', 
    type: 'suite', 
    status: 'booked', 
    price: 598, 
    guestName: '钱七', 
    checkoutDate: '2023-07-17 12:00' 
  }
])

// 根据房间类型过滤房间列表
const filteredRooms = computed(() => {
  if (!currentRoomType.value) return []
  return allRooms.filter(room => room.status === currentRoomType.value)
})

// 显示指定类型的房间列表
const showRoomsByType = (type) => {
  currentRoomType.value = type
  switch (type) {
    case 'available':
      roomListDialogTitle.value = '空闲房间列表'
      break
    case 'occupied':
      roomListDialogTitle.value = '已入住房间列表'
      break
    case 'cleaning':
      roomListDialogTitle.value = '待清洁房间列表'
      break
    case 'booked':
      roomListDialogTitle.value = '今日预订房间列表'
      break
  }
  roomListDialogVisible.value = true
}

// 获取房间状态标签类型
const getRoomStatusType = (status) => {
  switch (status) {
    case 'available': return 'success'
    case 'occupied': return 'primary'
    case 'cleaning': return 'warning'
    case 'booked': return 'info'
    default: return ''
  }
}

// 获取房间状态文本
const getRoomStatusText = (status) => {
  switch (status) {
    case 'available': return '空闲'
    case 'occupied': return '已入住'
    case 'cleaning': return '待清洁'
    case 'booked': return '已预订'
    default: return ''
  }
}

// 选择房间进行入住
const selectRoomForCheckin = (room) => {
  checkinForm.roomType = room.type === 'single' ? 'single' : 
                         room.type === 'double' ? 'deluxe' : 'executive'
  checkinForm.roomNumber = room.number
  roomListDialogVisible.value = false
  ElMessage({
    message: `已选择房间${room.number}，请填写入住信息`,
    type: 'success',
    duration: 2000
  })
}

// 显示退房对话框
const showCheckoutDialog = (room) => {
  ElMessageBox.confirm(
    `确认为房间${room.number}的客人${room.guestName}办理退房吗？`,
    '退房确认',
    {
      confirmButtonText: '确认退房',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    }
  ).then(() => {
    // 模拟退房操作
    const index = allRooms.findIndex(r => r.number === room.number)
    if (index !== -1) {
      allRooms[index].status = 'cleaning'
      allRooms[index].guestName = ''
      allRooms[index].checkoutDate = ''
    }
    // 更新房间状态数量
    roomStatusList[1].count--
    roomStatusList[2].count++
    ElMessage({
      message: `房间${room.number}退房成功，已标记为待清洁状态`,
      type: 'success',
      duration: 2000
    })
    roomListDialogVisible.value = false
  }).catch(() => {})
}

// 标记房间清洁完成
const markRoomAsClean = (room) => {
  // 模拟标记清洁完成操作
  const index = allRooms.findIndex(r => r.number === room.number)
  if (index !== -1) {
    allRooms[index].status = 'available'
  }
  // 更新房间状态数量
  roomStatusList[0].count++
  roomStatusList[2].count--
  ElMessage({
    message: `房间${room.number}已清洁完成，标记为空闲状态`,
    type: 'success',
    duration: 2000
  })
  roomListDialogVisible.value = false
}

// 入住登记表单
const checkinFormRef = ref(null)
const checkinForm = reactive({
  bookingNo: '',
  checkinType: 'normal',
  guestName: '',
  phone: '',
  idCard: '',
  guestCount: 1,
  roomType: '',
  roomNumber: '',
  dateRange: null,
  deposit: 500,
  remarks: ''
})

// 表单验证规则
const checkinFormRules = {
  guestName: [
    { required: true, message: '请输入客人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  guestCount: [
    { required: true, message: '请选择入住人数', trigger: 'change' }
  ],
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  roomNumber: [
    { required: true, message: '请选择房间号', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  deposit: [
    { required: true, message: '请输入押金金额', trigger: 'blur' }
  ]
}

// 查询预订信息
const handleSearchBooking = async () => {
  if (!checkinForm.bookingNo) {
    ElMessage.warning('请输入预订号')
    return
  }

  try {
    // 展示查询加载状态
    const loading = ElMessage({
      message: '正在查询预订信息...',
      type: 'info',
      duration: 0
    })
    
    // TODO: 调用后端API查询预订信息
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 关闭加载提示
    loading.close()
    
    // 模拟数据
    const bookingInfo = {
      guestName: '张三',
      phone: '13800138000',
      idCard: '110101199001011234',
      roomType: 'deluxe',
      dateRange: [new Date(), new Date(Date.now() + 86400000 * 2)]
    }
    
    Object.keys(bookingInfo).forEach(key => {
      checkinForm[key] = bookingInfo[key]
    })
    checkinForm.checkinType = 'booking'
    
    ElMessage({
      message: '预订信息查询成功',
      type: 'success',
      duration: 2000
    })
  } catch (error) {
    console.error('查询预订信息失败:', error)
    ElMessage.error('预订信息查询失败')
  }
}

// 提交入住登记
const handleSubmit = async () => {
  if (!checkinFormRef.value) return

  await checkinFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 展示操作加载状态
        const loading = ElMessage({
          message: '正在处理入住登记...',
          type: 'info',
          duration: 0
        })
        
        // TODO: 调用后端API保存入住信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 关闭加载提示
        loading.close()
        
        ElMessage({
          message: '入住登记成功',
          type: 'success',
          duration: 2000
        })
        
        // 更新房间状态
        if (checkinForm.roomNumber) {
          const index = allRooms.findIndex(r => r.number === checkinForm.roomNumber)
          if (index !== -1) {
            allRooms[index].status = 'occupied'
            allRooms[index].guestName = checkinForm.guestName
            
            // 计算退房时间
            if (checkinForm.dateRange && checkinForm.dateRange[1]) {
              const checkoutDate = new Date(checkinForm.dateRange[1])
              allRooms[index].checkoutDate = `${checkoutDate.getFullYear()}-${String(checkoutDate.getMonth() + 1).padStart(2, '0')}-${String(checkoutDate.getDate()).padStart(2, '0')} 12:00`
            }
            
            // 更新房间状态数量
            roomStatusList[0].count--
            roomStatusList[1].count++
          }
        }
        
        resetForm()
      } catch (error) {
        console.error('入住登记失败:', error)
        ElMessage.error('入住登记失败，请重试')
      }
    } else {
      ElMessage.warning('请填写完整的入住信息')
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!checkinFormRef.value) return
  checkinFormRef.value.resetFields()
  checkinForm.checkinType = 'normal'
  checkinForm.guestCount = 1
  checkinForm.deposit = 500
  checkinForm.dateRange = null
}
</script>

<style scoped>
.checkin-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f8fafc;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 24px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.header-content h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.gradient-text {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-description {
  margin: 6px 0 0;
  color: #6c757d;
  font-size: 14px;
}

.room-status-row {
  margin-bottom: 24px;
}

.status-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  overflow: hidden;
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.status-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.status-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.status-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.status-icon .el-icon {
  font-size: 28px;
  color: white;
}

.status-card-available .status-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.status-card-occupied .status-icon {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.status-card-cleaning .status-icon {
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
}

.status-card-booked .status-icon {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.status-card:hover .status-icon {
  transform: scale(1.1);
}

.status-info {
  flex: 1;
}

.status-count {
  font-size: 30px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  transition: all 0.3s ease;
}

.status-card:hover .status-count {
  transform: scale(1.05);
}

.status-title {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.checkin-form-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  padding: 5px 0;
}

.card-header .el-icon {
  color: #0d6efd;
}

.checkin-form {
  padding: 24px 0 0;
}

.checkin-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
}

.checkin-form :deep(.el-input__wrapper),
.checkin-form :deep(.el-textarea__inner) {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.checkin-form :deep(.el-input__wrapper:hover),
.checkin-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}

.custom-radio-group {
  padding: 8px 0;
}

.custom-radio-group :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #0d6efd;
}

.custom-radio-group :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #0d6efd;
  border-color: #0d6efd;
}

.search-booking-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.search-booking-btn:hover {
  background: linear-gradient(135deg, #0b5ed7, #153576);
  box-shadow: 0 4px 10px rgba(13, 110, 253, 0.25);
}

.form-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.submit-btn {
  min-width: 140px;
  height: 44px;
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.25);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #0b5ed7, #153576);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.35);
}

.reset-btn {
  min-width: 120px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  background-color: #fff;
  color: #6c757d;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background-color: #f8f9fa;
  color: #343a40;
  border-color: #ced4da;
}

.room-table {
  border-radius: 8px;
  margin-bottom: 10px;
  overflow: hidden;
}

.room-table :deep(.el-table__header-wrapper) {
  background: #f8fafc;
}

.room-table :deep(.el-table__header) {
  font-weight: 600;
  color: #1e293b;
}

.room-table :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.room-table :deep(.el-table__row:hover) {
  background-color: #f0f5ff !important;
}

.status-tag {
  padding: 6px 14px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 13px;
  letter-spacing: 0.3px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 6px;
  padding: 8px 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.custom-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: #1e293b;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .room-status-row .el-col {
    width: 50%;
    margin-bottom: 16px;
  }
  
  .status-content {
    padding: 16px;
  }
  
  .status-icon {
    width: 50px;
    height: 50px;
    margin-right: 16px;
  }
  
  .status-count {
    font-size: 24px;
  }
  
  .form-buttons {
    flex-direction: column;
    gap: 15px;
  }
  
  .submit-btn, .reset-btn {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .room-status-row .el-col {
    width: 100%;
  }
  
  .status-content {
    padding: 12px;
  }
}
</style>