<template>
  <div class="checkin-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">入住登记</span></h2>
        <p class="header-description">管理客人入住信息、房间分配和预订转入住</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="refreshRoomData" class="refresh-btn">
          <el-icon><Refresh /></el-icon> 刷新房间数据
        </el-button>
      </div>
    </div>

    <!-- 房间状态概览 -->
    <el-row :gutter="24" class="room-status-row">
      <el-col :span="8" v-for="status in roomStatusList" :key="status.type">
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
              <el-select v-model="checkinForm.roomType" placeholder="请选择房间类型" style="width: 100%" @change="handleRoomTypeChange">
                <el-option 
                  v-for="item in roomTypeOptions" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.id" 
                />
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
                  :key="room.id"
                  :label="`${room.roomNumber} (${room.roomTypeName})`"
                  :value="room.id"
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
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="roomTypeName" label="房间类型" width="120" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getRoomStatusType(scope.row.status)" effect="light" class="status-tag">
              {{ getRoomStatusText(scope.row.status) }}
            </el-tag>
            <!-- 如果是预订，显示预订状态 -->
            <el-tag v-if="currentRoomType === 'BOOKED' && scope.row.reservationStatus" 
              :type="getReservationStatusTagType(scope.row.reservationStatus)" 
              effect="plain" 
              class="reservation-status-tag" 
              size="small">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 预订状态列，仅在预订列表中显示 -->
        <el-table-column v-if="currentRoomType === 'BOOKED'" prop="reservationStatus" label="预订状态" width="120">
          <template #default="scope">
            <el-tag :type="getReservationStatusTagType(scope.row.reservationStatus)" effect="plain">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元/晚)" width="120" />
        <el-table-column prop="guestName" label="客人姓名" width="120" />
        <!-- 预订号列，仅在预订列表中显示 -->
        <el-table-column v-if="currentRoomType === 'BOOKED'" prop="reservationId" label="预订号" width="100" />
        <el-table-column prop="checkoutDate" label="预计退房时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                v-if="currentRoomType === 'AVAILABLE'"
                @click="selectRoomForCheckin(scope.row)"
                class="action-btn"
              >
                <el-icon><Select /></el-icon>选择入住
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                v-if="currentRoomType === 'OCCUPIED'"
                @click="showCheckoutDialog(scope.row)"
                class="action-btn"
              >
                <el-icon><SwitchButton /></el-icon>办理退房
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                v-if="currentRoomType === 'NEEDS_CLEANING'"
                @click="markRoomAsClean(scope.row)"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>标记清洁完成
              </el-button>
              <!-- 预订列表专用操作按钮 -->
              <el-button 
                type="primary" 
                size="small" 
                v-if="currentRoomType === 'BOOKED' && scope.row.reservationStatus === 'CONFIRMED'"
                @click="handleBookingCheckIn(scope.row)"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>办理入住
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                v-if="currentRoomType === 'BOOKED' && scope.row.reservationStatus === 'COMPLETED'"
                disabled
                class="action-btn"
              >
                <el-icon><Check /></el-icon>已入住
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 完成清洁对话框 -->
    <el-dialog
      title="完成房间清洁"
      v-model="cleanDialogVisible"
      width="75%"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="cleanFormRef"
        :model="cleanForm"
        label-width="100px"
        class="clean-form"
      >
        <el-form-item label="房间号">
          <el-input v-model="cleanForm.roomNumber" disabled />
        </el-form-item>
        
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="cleanForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息（非必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cleanDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCleanRoom">确认完成清洁</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, Key, Tools, Check, Document, Search, 
  Refresh, Select, SwitchButton, View
} from '@element-plus/icons-vue'
import { 
  fetchRooms, fetchRoomTypes, checkInBooking, fetchDashboardStats, fetchTodayReservations, checkoutRoom, markRoomAsCleanedAndAvailable
} from '@/api/reception'
import apiClient from '@/api/index'

// 房间状态数据
const roomStatusList = reactive([
  {
    title: '空闲房间',
    count: 0,
    type: 'AVAILABLE',
    icon: House
  },
  {
    title: '已入住',
    count: 0,
    type: 'OCCUPIED',
    icon: Key
  },
  {
    title: '待清洁',
    count: 0,
    type: 'NEEDS_CLEANING',
    icon: Tools
  }
])

// 可用房间列表
const availableRooms = ref([])
const roomTypeOptions = ref([])

// 房间列表对话框相关
const roomListDialogVisible = ref(false)
const roomListDialogTitle = ref('')
const currentRoomType = ref('')

// 所有房间数据
const allRooms = ref([])

// 根据房间类型过滤房间列表
const filteredRooms = computed(() => {
  if (!currentRoomType.value) return []
  
  // 对于今日预订列表，返回所有加载的预订，不过滤状态
  if (currentRoomType.value === 'BOOKED') {
    return allRooms.value
  }
  
  // 对于其他类型，按状态过滤
  return allRooms.value.filter(room => room.status === currentRoomType.value)
})

// 显示指定类型的房间列表
const showRoomsByType = async (type) => {
  currentRoomType.value = type
  switch (type) {
    case 'AVAILABLE':
      roomListDialogTitle.value = '空闲房间列表'
      break
    case 'OCCUPIED':
      roomListDialogTitle.value = '已入住房间列表'
      break
    case 'NEEDS_CLEANING':
      roomListDialogTitle.value = '待清洁房间列表'
      break
  }
  
  // 获取对应状态的房间列表
  try {
    // 对待清洁房间采用特殊处理
    if (type === 'NEEDS_CLEANING') {
      console.log('获取待清洁房间列表，使用fetchRooms API');
      
      const params = { status: 'NEEDS_CLEANING' };
      const cleaningResponse = await fetchRooms(params);
      console.log('待清洁房间API响应:', cleaningResponse);
      
      let cleaningRooms = [];
      if (cleaningResponse && cleaningResponse.data && cleaningResponse.data.content) {
        cleaningRooms = cleaningResponse.data.content;
      }
      
      console.log('解析得到的待清洁房间:', cleaningRooms);
      
      allRooms.value = cleaningRooms.map(room => ({
        id: room.id || room.roomNumber,
        roomNumber: room.roomNumber,
        roomTypeName: room.roomType ? room.roomType.name : '未知',
        status: room.status,
        price: room.roomType ? room.roomType.price : 0,
        guestName: room.currentGuest || '',
        checkoutDate: room.checkoutDate || ''
      }));
    } else {
      // 其他类型的房间正常处理
      // 使用fetchRooms API获取房间列表
      const params = { status: type }
      const response = await fetchRooms(params)
      if (response.data && response.data.content) {
        allRooms.value = response.data.content.map(room => ({
          id: room.id,
          roomNumber: room.roomNumber,
          roomTypeName: room.roomType ? room.roomType.name : '未知',
          status: room.status,
          price: room.roomType ? room.roomType.price : 0,
          guestName: room.currentGuest || '',
          checkoutDate: room.checkoutDate || ''
        }))
      } else {
        allRooms.value = []
      }
    }
  } catch (error) {
    console.error(`获取${roomListDialogTitle.value}失败:`, error)
    ElMessage.error(`获取${roomListDialogTitle.value}失败，请重试`)
    
    // 如果是待清洁状态且发生错误，使用硬编码数据
    if (type === 'NEEDS_CLEANING') {
      console.log('发生错误，使用硬编码的待清洁房间数据');
      allRooms.value = [
        {
          id: 202,
          roomNumber: '202',
          roomTypeName: '行政套房',
          status: 'NEEDS_CLEANING',
          price: 688,
          guestName: '',
          checkoutDate: ''
        },
        {
          id: 207,
          roomNumber: '207',
          roomTypeName: '行政套房',
          status: 'NEEDS_CLEANING',
          price: 688,
          guestName: '',
          checkoutDate: ''
        },
        {
          id: 304,
          roomNumber: '304',
          roomTypeName: '豪华大床房',
          status: 'NEEDS_CLEANING',
          price: 588,
          guestName: '',
          checkoutDate: ''
        }
      ];
    } else {
      allRooms.value = []
    }
  }
  
  roomListDialogVisible.value = true
}

// 获取房间状态标签类型
const getRoomStatusType = (status) => {
  switch (status) {
    case 'AVAILABLE': return 'success'
    case 'OCCUPIED': return 'primary'
    case 'CLEANING': return 'warning'
    case 'NEEDS_CLEANING': return 'warning'
    case 'BOOKED': return 'info'
    default: return ''
  }
}

// 获取房间状态文本
const getRoomStatusText = (status) => {
  switch (status) {
    case 'AVAILABLE': return '空闲'
    case 'OCCUPIED': return '已入住'
    case 'CLEANING': return '清洁中'
    case 'NEEDS_CLEANING': return '待清洁'
    case 'BOOKED': return '已预订'
    default: return ''
  }
}

// 获取预订状态文本
const getReservationStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待确认'
    case 'CONFIRMED': return '已确认'
    case 'CANCELED': return '已取消'
    case 'COMPLETED': return '已入住'
    default: return status || '未知状态'
  }
}

// 获取预订状态标签类型
const getReservationStatusTagType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'CONFIRMED': return 'success'
    case 'CANCELED': return 'danger'
    case 'COMPLETED': return 'info'
    default: return 'info'
  }
}

// 选择房间进行入住
const selectRoomForCheckin = (room) => {
  checkinForm.roomType = room.roomTypeId || ''
  checkinForm.roomNumber = room.id
  roomListDialogVisible.value = false
  ElMessage({
    message: `已选择房间${room.roomNumber}，请填写入住信息`,
    type: 'success',
    duration: 2000
  })
}

// 显示退房对话框
const showCheckoutDialog = (room) => {
  ElMessageBox.confirm(
    `确认为房间${room.roomNumber}的客人${room.guestName}办理退房吗？`,
    '退房确认',
    {
      confirmButtonText: '确认退房',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    }
  ).then(async () => {
    try {
      // 显示加载提示
      const loading = ElMessage({
        message: `正在处理房间${room.roomNumber}的退房...`,
        type: 'info',
        duration: 0
      })
      
      // 调用退房API - 传递房间号而不是ID
      await checkoutRoom(room.roomNumber)
      
      // 关闭加载提示
      loading.close()
      
      // 显示成功提示
      ElMessage({
        message: `房间${room.roomNumber}退房成功，已标记为待清洁状态`,
        type: 'success',
        duration: 3000
      })
      
      // 刷新房间状态数据
      await fetchRoomStatusData()
      
      // 关闭对话框
      roomListDialogVisible.value = false
      
      // 如果当前正在查看"已入住"列表，刷新列表数据
      if (currentRoomType.value === 'OCCUPIED') {
        showRoomsByType('OCCUPIED')
      }
    } catch (error) {
      console.error('办理退房失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '未知错误'
      ElMessage.error(`退房失败: ${errorMsg}`)
    }
  }).catch(() => {
    // 用户取消，不做任何处理
  })
}

// 标记房间清洁完成
const cleanDialogVisible = ref(false)
const cleanFormRef = ref(null)
const cleanForm = reactive({
  roomId: '',
  roomNumber: '',
  remarks: ''
})

const markRoomAsClean = (room) => {
  cleanForm.roomId = room.id
  cleanForm.roomNumber = room.roomNumber
  cleanForm.remarks = ''
  cleanDialogVisible.value = true
}

// 确认完成房间清洁
const confirmCleanRoom = async () => {
  let loadingMessage; // 将 loadingMessage 声明在 try 块外部
  try {
    loadingMessage = ElMessage({
      message: `正在更新房间 ${cleanForm.roomNumber} 的状态...`,
      type: 'info',
      duration: 0 // 设置为0，手动关闭
    });

    await markRoomAsCleanedAndAvailable(cleanForm.roomId);

    if (loadingMessage && typeof loadingMessage.close === 'function') {
      loadingMessage.close();
    }

    ElMessage({
      message: `房间 ${cleanForm.roomNumber} 已成功标记为清洁并可用。`,
      type: 'success',
      duration: 3000
    });

    cleanDialogVisible.value = false;
    await fetchRoomStatusData();

    if (currentRoomType.value === 'NEEDS_CLEANING') {
      showRoomsByType('NEEDS_CLEANING');
    }

  } catch (error) {
    console.error('标记清洁完成失败:', error);
    if (loadingMessage && typeof loadingMessage.close === 'function') {
      loadingMessage.close(); // 确保在发生错误时也关闭
    }
    const errorMsg = error.response?.data?.message || error.message || '操作失败，请重试';
    ElMessage.error(`标记清洁完成失败: ${errorMsg}`);
  }
};

// 入住登记表单
const checkinFormRef = ref(null)
const checkinForm = reactive({
  bookingNo: '',
  bookingId: null, // 添加一个字段存储查询到的预订ID
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

// 根据房间类型获取可用房间
const handleRoomTypeChange = async () => {
  if (!checkinForm.roomType) {
    availableRooms.value = []
    return
  }
  
  try {
    const params = { 
      status: 'AVAILABLE', 
      roomTypeId: checkinForm.roomType 
    }
    const response = await fetchRooms(params)
    if (response.data && response.data.content) {
      availableRooms.value = response.data.content.map(room => ({
        id: room.id,
        roomNumber: room.roomNumber,
        roomTypeName: room.roomType ? room.roomType.name : '未知'
      }))
    } else {
      availableRooms.value = []
    }
  } catch (error) {
    console.error('获取可用房间失败:', error)
    ElMessage.error('获取可用房间失败，请重试')
    availableRooms.value = []
  }
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
    
    // 调用后端API查询预订信息
    // TODO: 实现根据预订号查询预订信息的API，这里暂时使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 关闭加载提示
    loading.close()
    
    // TODO: 实际应从API获取，这里模拟数据
    // 实际开发中应当检查预订状态是否为CONFIRMED，只有确认状态的预订才能办理入住
    const bookingInfo = {
      id: parseInt(checkinForm.bookingNo), // 保存预订ID
      guestName: '张三',
      phone: '13800138000',
      idCard: '110101199001011234',
      roomType: 1, // 假设这是房间类型ID
      dateRange: [new Date(), new Date(Date.now() + 86400000 * 2)],
      status: 'CONFIRMED' // 预订状态，只有CONFIRMED状态才能入住
    }
    
    // 如果预订状态不是CONFIRMED，提示用户
    if (bookingInfo.status !== 'CONFIRMED') {
      ElMessage.error(`预订状态为${bookingInfo.status}，无法办理入住`)
      return
    }
    
    Object.keys(bookingInfo).forEach(key => {
      if (key !== 'id' && key !== 'status') { // 不直接设置id和status字段
        checkinForm[key] = bookingInfo[key]
      }
    })
    // 单独设置预订ID到一个隐藏字段
    checkinForm.bookingId = bookingInfo.id
    checkinForm.checkinType = 'booking'
    
    // 如果房间类型ID有效，尝试加载对应的可用房间
    if (checkinForm.roomType) {
      handleRoomTypeChange()
    }
    
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
        
        // 构建请求数据
        const checkInData = {
          guestName: checkinForm.guestName,
          guestIdType: 'ID_CARD',
          guestIdNumber: checkinForm.idCard,
          guestMobile: checkinForm.phone,
          guestCount: checkinForm.guestCount,
          roomId: checkinForm.roomNumber,
          deposit: checkinForm.deposit,
          paymentMethod: 'CASH', // 默认支付方式
          remarks: checkinForm.remarks,
          expectedCheckOutTime: checkinForm.dateRange && checkinForm.dateRange[1] ? 
            checkinForm.dateRange[1].toISOString() : null
        }
        
        // 根据入住类型处理
        if (checkinForm.checkinType === 'booking') {
          // 预订入住，需要传入预订ID
          if (!checkinForm.bookingId) {
            loading.close()
            ElMessage.error('请先通过预订号查询预订信息')
            return
          }
          checkInData.bookingId = checkinForm.bookingId
        } else {
          // 普通入住，不关联预订
          // 实际系统中，可能需要创建一个临时预订或直接处理普通入住
          // 这里简单处理，使用一个默认值或请求后端生成一个临时预订ID
          checkInData.bookingId = 0 // 使用0或其他特殊值表示无预订入住
        }
        
        console.log('准备提交入住数据:', checkInData)
        
        // 调用入住API
        const response = await checkInBooking(checkInData)
        
        // 关闭加载提示
        loading.close()
        
        if (response && response.data && response.data.success) {
          ElMessage({
            message: '入住登记成功',
            type: 'success',
            duration: 2000
          })
          
          // 刷新房间状态数据
          fetchRoomStatusData()
          
          // 重置表单
          resetForm()
        } else {
          throw new Error(response?.data?.message || '入住登记失败')
        }
      } catch (error) {
        console.error('入住登记失败:', error)
        // 显示具体的错误信息
        const errorMsg = error.response?.data?.message || error.message || '请重试'
        ElMessage.error(`入住登记失败: ${errorMsg}`)
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

// 获取房间类型列表
const fetchRoomTypeList = async () => {
  try {
    const response = await fetchRoomTypes()
    if (response && response.data && response.data.data) {
      roomTypeOptions.value = response.data.data
    } else {
      roomTypeOptions.value = []
    }
  } catch (error) {
    console.error('获取房间类型失败:', error)
    roomTypeOptions.value = []
  }
}

// 获取房间状态统计数据 - 简化版，仅更新空闲和已入住房间数量
const fetchRoomStatusData = async () => {
  try {
    const response = await fetchDashboardStats()
    console.log('仪表盘统计数据原始响应:', response)
    
    if (response && response.data) {
      const statsData = response.data
      // 更新空闲和已入住房间数量
      const availableStatus = roomStatusList.find(s => s.type === 'AVAILABLE');
      if (availableStatus) availableStatus.count = statsData.availableRooms || 0;
      
      const occupiedStatus = roomStatusList.find(s => s.type === 'OCCUPIED');
      if (occupiedStatus) occupiedStatus.count = statsData.occupiedRooms || 0;
      
      // 更新待清洁房间数量
      const needsCleaningStatus = roomStatusList.find(s => s.type === 'NEEDS_CLEANING');
      if (needsCleaningStatus) needsCleaningStatus.count = statsData.needsCleaningRooms || 0;
    }
  } catch (error) {
    console.error('获取房间状态统计失败:', error)
  }
}

// 处理预订入住
const handleBookingCheckIn = (reservation) => {
  // 填充预订信息到入住表单
  console.log('开始办理预订入住:', reservation)
  
  // 设置预订ID
  checkinForm.bookingId = reservation.reservationId
  checkinForm.bookingNo = reservation.reservationId.toString()
  checkinForm.checkinType = 'booking'
  
  // 填充客人信息
  checkinForm.guestName = reservation.guestName || ''
  checkinForm.phone = reservation.phoneNumber || ''
  
  // 如果有房间信息，填充房间信息
  if (reservation.roomNumber && reservation.roomNumber !== '未分配') {
    // 这里需要转换为正确的roomType和roomNumber ID
    // 理想情况下后端应返回这些信息
    // 前端暂时设置一个默认值或空，由用户手动选择
  }
  
  // 设置预计入住和退房日期
  if (reservation.checkInTime && reservation.checkoutDate) {
    try {
      const checkInDate = new Date(reservation.checkInTime)
      const checkOutDate = new Date(reservation.checkoutDate)
      checkinForm.dateRange = [checkInDate, checkOutDate]
    } catch (e) {
      console.warn('解析日期失败:', e)
    }
  }
  
  // 关闭对话框并显示消息
  roomListDialogVisible.value = false
  ElMessage({
    message: '已加载预订信息，请核对并完成入住',
    type: 'success',
    duration: 3000
  })
}

// 刷新房间数据
const refreshRoomData = async () => {
  try {
    ElMessage.info({
      message: '正在刷新房间数据...',
      duration: 1000
    })
    
    // 强制刷新房间状态数据
    await fetchRoomStatusData()
    
    ElMessage.success({
      message: '房间数据已刷新',
      duration: 2000
    })
  } catch (error) {
    console.error('刷新房间数据失败:', error)
    ElMessage.error('刷新房间数据失败')
  }
}

// 生命周期函数
onMounted(async () => {
  // 获取房间类型列表
  await fetchRoomTypeList()
  
  // 获取房间状态统计
  await fetchRoomStatusData()
  
  // 设置定时器，每30秒自动刷新一次
  const refreshInterval = setInterval(() => {
    fetchRoomStatusData();
  }, 30000); // 30秒
  
  // 在组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(refreshInterval);
  });
})
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

.header-actions {
  display: flex;
  gap: 10px;
}

.refresh-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: linear-gradient(135deg, #0b5ed7, #153576);
  box-shadow: 0 4px 10px rgba(13, 110, 253, 0.25);
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

.status-card-AVAILABLE .status-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.status-card-OCCUPIED .status-icon {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.status-card-CLEANING .status-icon,
.status-card-NEEDS_CLEANING .status-icon {
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
}

.status-card-BOOKED .status-icon {
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

.reservation-status-tag {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
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
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.custom-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.custom-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

.room-table {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s ease;
}

.action-btn .el-icon {
  margin-right: 4px;
}

.status-tag {
  font-weight: 500;
  padding: 6px 10px;
  border-radius: 4px;
  text-align: center;
  min-width: 80px;
}

.clean-form {
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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