<template>
  <div class="bookings-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">预订管理</span></h2>
        <p class="header-description">管理客户预订信息、确认和入住安排</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="btn-add">
        <el-icon><Plus /></el-icon>新增预订
      </el-button>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card pending-card">
            <div class="stats-icon"><el-icon><Clock /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">8</div>
              <div class="stats-label">待确认预订</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card confirmed-card">
            <div class="stats-icon"><el-icon><Calendar /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">15</div>
              <div class="stats-label">今日入住</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card checkin-card">
            <div class="stats-icon"><el-icon><User /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">76%</div>
              <div class="stats-label">入住率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card revenue-card">
            <div class="stats-icon"><el-icon><Money /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">¥8,650</div>
              <div class="stats-label">今日预订收入</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作区 -->
    <el-card class="quick-actions-card" shadow="hover">
      <div class="quick-actions-header">
        <h3>快速操作</h3>
      </div>
      <div class="quick-actions">
        <div class="quick-action-item" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增预订</span>
        </div>
        <div class="quick-action-item" @click="openTodayCheckin">
          <el-icon><Calendar /></el-icon>
          <span>今日入住</span>
        </div>
        <div class="quick-action-item" @click="openTodayCheckout">
          <el-icon><Right /></el-icon>
          <span>今日离店</span>
        </div>
        <div class="quick-action-item" @click="exportBookings">
          <el-icon><Download /></el-icon>
          <span>导出数据</span>
        </div>
        <div class="quick-action-item" @click="showRoomStatus">
          <el-icon><House /></el-icon>
          <span>房态一览</span>
        </div>
      </div>
    </el-card>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预订号">
          <el-input v-model="searchForm.bookingNo" placeholder="请输入预订号" clearable />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="预订状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="pending" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已入住" value="checked-in" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="search-btn">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch" class="reset-btn">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预订列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table 
        :data="bookingList" 
        style="width: 100%" 
        v-loading="loading"
        border
        stripe
        highlight-current-row
        class="booking-table"
      >
        <el-table-column prop="bookingNo" label="预订号" width="120" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="roomType" label="房间类型" />
        <el-table-column prop="roomNumber" label="房间号" />
        <el-table-column prop="roomPrice" label="房价" width="100">
          <template #default="{ row }">
            <span>¥{{ row.roomPrice || '580' }}/晚</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" />
        <el-table-column prop="checkOutDate" label="离店日期" />
        <el-table-column label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus || 'unpaid')" effect="light">
              {{ getPaymentStatusText(row.paymentStatus || 'unpaid') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                link 
                @click="handleEdit(row)"
                v-if="row.status === 'pending'"
                class="action-btn"
              ><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button 
                type="success" 
                link 
                @click="handleConfirm(row)"
                v-if="row.status === 'pending'"
                class="action-btn"
              ><el-icon><Check /></el-icon>确认</el-button>
              <el-button 
                type="primary" 
                link 
                @click="handleCheckIn(row)"
                v-if="row.status === 'confirmed'"
                class="action-btn"
              ><el-icon><Key /></el-icon>办理入住</el-button>
              <el-button 
                type="danger" 
                link 
                @click="handleCancel(row)"
                v-if="['pending', 'confirmed'].includes(row.status)"
                class="action-btn"
              ><el-icon><Close /></el-icon>取消</el-button>
              <el-button 
                type="info" 
                link 
                @click="handleView(row)"
                class="action-btn"
              ><el-icon><View /></el-icon>查看</el-button>
              <el-button 
                type="primary" 
                link 
                @click="handlePayment(row)"
                v-if="['pending', 'confirmed'].includes(row.status) && (row.paymentStatus !== 'paid')"
                class="action-btn"
              ><el-icon><Money /></el-icon>收款</el-button>
              <el-button 
                type="success" 
                link 
                @click="handleRoomPreview(row)"
                v-if="['pending', 'confirmed'].includes(row.status)"
                class="action-btn"
              ><el-icon><Picture /></el-icon>房型预览</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="custom-pagination"
        />
      </div>
    </el-card>

    <!-- 预订表单对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '新增预订' : '编辑预订'"
      v-model="dialogVisible"
      width="650px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="bookingFormRef"
        :model="bookingForm"
        :rules="bookingFormRules"
        label-width="100px"
        class="booking-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户姓名" prop="customerName">
              <el-input v-model="bookingForm.customerName" placeholder="请输入客户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="bookingForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="bookingForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间类型" prop="roomType">
              <el-select v-model="bookingForm.roomType" placeholder="请选择房间类型" style="width: 100%">
                <el-option label="标准双人间" value="标准双人间" />
                <el-option label="豪华大床房" value="豪华大床房" />
                <el-option label="行政套房" value="行政套房" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间数量" prop="roomCount">
              <el-input-number v-model="bookingForm.roomCount" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="入住日期" prop="dateRange">
              <el-date-picker
                v-model="bookingForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="入住日期"
                end-placeholder="离店日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入住人数" prop="guestCount">
              <el-input-number v-model="bookingForm.guestCount" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计到店" prop="estimatedArrivalTime">
              <el-time-select
                v-model="bookingForm.estimatedArrivalTime"
                start="08:00"
                step="00:30"
                end="22:00"
                placeholder="请选择预计到店时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预订来源" prop="source">
              <el-select v-model="bookingForm.source" placeholder="请选择预订来源" style="width: 100%">
                <el-option label="直接预订" value="直接预订" />
                <el-option label="电话预订" value="电话预订" />
                <el-option label="在线平台" value="在线平台" />
                <el-option label="旅行社" value="旅行社" />
                <el-option label="合作单位" value="合作单位" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队预订">
              <el-switch
                v-model="bookingForm.isGroup"
                active-text="是"
                inactive-text="否"
                class="custom-switch"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="特殊要求" prop="specialRequests">
          <el-input
            v-model="bookingForm.specialRequests"
            type="textarea"
            rows="3"
            placeholder="请输入特殊要求（如：需要加床、安静房等）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="submit-btn">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 入住登记对话框 -->
    <el-dialog
      title="办理入住"
      v-model="checkInVisible"
      width="550px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="checkInFormRef"
        :model="checkInForm"
        :rules="checkInFormRules"
        label-width="100px"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-select v-model="checkInForm.roomNumber" placeholder="请选择房间号" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.number"
              :label="`${room.number} (${room.type})`"
              :value="room.number"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="押金" prop="deposit">
          <el-input-number v-model="checkInForm.deposit" :min="0" :step="100" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="checkInForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkInVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleCheckInSubmit" class="submit-btn">确认入住</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 房型预览对话框 -->
    <el-dialog
      title="房型预览"
      v-model="roomPreviewVisible"
      width="700px"
      class="custom-dialog room-preview-dialog"
    >
      <el-carousel height="350px" class="room-carousel">
        <el-carousel-item v-for="(image, index) in roomTypeImages[currentRoomType] || []" :key="index">
          <div class="carousel-content">
            <img :src="image.url" alt="房间图片" class="room-image" />
            <div class="image-title">{{ image.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
      
      <div class="room-info">
        <h3>{{ currentRoomType }} 详情</h3>
        <div class="room-details">
          <p><el-icon><User /></el-icon> 可住2人</p>
          <p><el-icon><Monitor /></el-icon> 50寸智能电视</p>
          <p><el-icon><Sugar /></el-icon> 免费早餐</p>
          <p><el-icon><Connection /></el-icon> 高速WiFi</p>
        </div>
        <div class="room-description">
          宽敞舒适的客房配备高品质床垫和床品，提供最佳睡眠体验。配有现代化设施，满足商务和休闲旅客需求。
        </div>
      </div>
    </el-dialog>

    <!-- 支付对话框 -->
    <el-dialog
      title="收款处理"
      v-model="paymentVisible"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="paymentForm" label-width="100px">
        <el-form-item label="支付类型">
          <el-radio-group v-model="paymentForm.type">
            <el-radio label="deposit">收取定金</el-radio>
            <el-radio label="full">收取全款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input-number 
            v-model="paymentForm.amount" 
            :min="100" 
            :step="100" 
            :precision="2" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="paymentForm.method" style="width: 100%">
            <el-option label="现金" value="cash" />
            <el-option label="银行卡" value="card" />
            <el-option label="微信" value="wechat" />
            <el-option label="支付宝" value="alipay" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="paymentForm.remarks" 
            type="textarea" 
            rows="3" 
            placeholder="请输入备注信息" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handlePaymentSubmit" class="submit-btn">确认收款</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, Edit, Check, Close, View, Key, Money, Picture, Calendar,
  Clock, User, Download, Right, House
} from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  bookingNo: '',
  customerName: '',
  phone: '',
  status: '',
  dateRange: null
})

// 预订列表数据
const loading = ref(false)
const bookingList = ref([
  {
    bookingNo: 'BK20240331001',
    customerName: '张三',
    phone: '13800138000',
    roomType: '标准双人间',
    roomNumber: '203',
    roomPrice: 580,
    checkInDate: '2024-03-31',
    checkOutDate: '2024-04-02',
    status: 'confirmed',
    paymentStatus: 'deposit',
    isGroup: false,
    guestCount: 2
  },
  // 更多模拟数据...
])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 预订表单对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const bookingFormRef = ref(null)
const bookingForm = reactive({
  customerName: '',
  phone: '',
  idCard: '',
  roomType: '',
  dateRange: null,
  roomCount: 1,
  specialRequests: '',
  isGroup: false,
  guestCount: 2,
  estimatedArrivalTime: '',
  source: '直接预订'
})

// 入住登记对话框
const checkInVisible = ref(false)
const checkInFormRef = ref(null)
const checkInForm = reactive({
  roomNumber: '',
  deposit: 500,
  remarks: ''
})

// 可用房间列表
const availableRooms = ref([
  { number: '201', type: '标准单人间' },
  { number: '202', type: '标准单人间' },
  { number: '301', type: '豪华大床房' },
  { number: '302', type: '豪华大床房' },
  { number: '401', type: '行政套房' }
])

// 房型预览对话框
const roomPreviewVisible = ref(false)
const currentRoomType = ref('')
const roomTypeImages = {
  '标准双人间': [
    { url: 'https://example.com/standard-twin-1.jpg', title: '标准双人间-主视图' },
    { url: 'https://example.com/standard-twin-2.jpg', title: '标准双人间-浴室' }
  ],
  '豪华大床房': [
    { url: 'https://example.com/deluxe-king-1.jpg', title: '豪华大床房-主视图' },
    { url: 'https://example.com/deluxe-king-2.jpg', title: '豪华大床房-浴室' }
  ],
  '行政套房': [
    { url: 'https://example.com/executive-suite-1.jpg', title: '行政套房-客厅' },
    { url: 'https://example.com/executive-suite-2.jpg', title: '行政套房-卧室' },
    { url: 'https://example.com/executive-suite-3.jpg', title: '行政套房-浴室' }
  ]
}

// 支付对话框
const paymentVisible = ref(false)
const paymentForm = reactive({
  amount: 0,
  method: 'cash',
  type: 'deposit', // deposit, full
  remarks: ''
})

// 表单验证规则
const bookingFormRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  roomCount: [
    { required: true, message: '请选择房间数量', trigger: 'change' }
  ],
  guestCount: [
    { required: true, message: '请输入入住人数', trigger: 'blur' }
  ],
  estimatedArrivalTime: [
    { required: true, message: '请选择预计到店时间', trigger: 'change' }
  ],
  source: [
    { required: true, message: '请选择预订来源', trigger: 'change' }
  ]
}

const checkInFormRules = {
  roomNumber: [
    { required: true, message: '请选择房间号', trigger: 'change' }
  ],
  deposit: [
    { required: true, message: '请输入押金金额', trigger: 'blur' }
  ]
}

// 状态处理函数
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'primary',
    'checked-in': 'success',
    cancelled: 'info',
    completed: ''
  }
  return statusMap[status]
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    'checked-in': '已入住',
    cancelled: '已取消',
    completed: '已完成'
  }
  return statusMap[status]
}

// 支付状态处理函数
const getPaymentStatusType = (status) => {
  const statusMap = {
    unpaid: 'danger',
    deposit: 'warning',
    paid: 'success'
  }
  return statusMap[status]
}

const getPaymentStatusText = (status) => {
  const statusMap = {
    unpaid: '未支付',
    deposit: '已付定金',
    paid: '已付全款'
  }
  return statusMap[status]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchBookingList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.dateRange = null
  handleSearch()
}

// 获取预订列表
const fetchBookingList = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取预订列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    // 这里可以添加过滤逻辑，根据searchForm中的条件过滤预订列表
    loading.value = false
  } catch (error) {
    console.error('获取预订列表失败:', error)
    ElMessage.error('获取预订列表失败')
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBookingList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBookingList()
}

// 新增预订
const handleAdd = () => {
  dialogType.value = 'add'
  Object.keys(bookingForm).forEach(key => {
    bookingForm[key] = key === 'roomCount' ? 1 : ''
  })
  bookingForm.dateRange = null
  dialogVisible.value = true
}

// 编辑预订
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(bookingForm).forEach(key => {
    if (key === 'dateRange') {
      bookingForm[key] = [row.checkInDate, row.checkOutDate]
    } else {
      bookingForm[key] = row[key]
    }
  })
  dialogVisible.value = true
}

// 提交预订表单
const handleSubmit = async () => {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API保存预订信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success({
          message: dialogType.value === 'add' ? '预订创建成功' : '预订信息已更新',
          type: 'success',
          duration: 2000
        })
        dialogVisible.value = false
        fetchBookingList()
      } catch (error) {
        console.error('保存预订信息失败:', error)
        ElMessage.error('操作失败，请重试')
      }
    }
  })
}

// 确认预订
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确定要确认该预订吗？', '预订确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // TODO: 调用后端API确认预订
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success({
      message: '预订已确认',
      type: 'success',
      duration: 2000
    })
    row.status = 'confirmed'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预订失败:', error)
      ElMessage.error('确认操作失败')
    }
  }
}

// 取消预订
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗？此操作不可恢复。', '取消预订', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    
    // TODO: 调用后端API取消预订
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success({
      message: '预订已取消',
      type: 'success',
      duration: 2000
    })
    row.status = 'cancelled'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预订失败:', error)
      ElMessage.error('取消操作失败')
    }
  }
}

// 办理入住
const handleCheckIn = (row) => {
  checkInForm.roomNumber = ''
  checkInForm.deposit = 500
  checkInForm.remarks = ''
  
  // 根据预订房间类型过滤可用房间
  const filteredRooms = availableRooms.value.filter(room => {
    if (row.roomType === '标准双人间') return room.type.includes('双人');
    if (row.roomType === '豪华大床房') return room.type.includes('大床');
    if (row.roomType === '行政套房') return room.type.includes('套房');
    return true;
  });
  
  availableRooms.value = filteredRooms.length > 0 ? filteredRooms : availableRooms.value;
  checkInVisible.value = true
}

// 提交入住登记
const handleCheckInSubmit = async () => {
  if (!checkInFormRef.value) return
  
  await checkInFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API办理入住
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success({
          message: '入住登记成功',
          type: 'success',
          duration: 2000
        })
        checkInVisible.value = false
        fetchBookingList()
      } catch (error) {
        console.error('入住登记失败:', error)
        ElMessage.error('入住登记失败')
      }
    }
  })
}

// 处理房型预览
const handleRoomPreview = (row) => {
  currentRoomType.value = row.roomType
  roomPreviewVisible.value = true
}

// 处理支付
const handlePayment = (row) => {
  paymentForm.amount = row.roomPrice || 580
  paymentForm.method = 'cash'
  paymentForm.type = 'deposit'
  paymentForm.remarks = ''
  paymentVisible.value = true
}

// 提交支付
const handlePaymentSubmit = async () => {
  try {
    // TODO: 调用后端API处理支付
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success({
      message: '支付处理成功',
      type: 'success',
      duration: 2000
    })
    paymentVisible.value = false
    fetchBookingList()
  } catch (error) {
    console.error('支付处理失败:', error)
    ElMessage.error('支付处理失败')
  }
}

// 查看详情
const handleView = (row) => {
  // 实现查看详情功能
  ElMessageBox.alert(
    `<div class="booking-detail">
      <p><strong>预订号：</strong>${row.bookingNo}</p>
      <p><strong>客户姓名：</strong>${row.customerName}</p>
      <p><strong>联系电话：</strong>${row.phone}</p>
      <p><strong>房间类型：</strong>${row.roomType}</p>
      <p><strong>房价：</strong>¥${row.roomPrice || '580'}/晚</p>
      <p><strong>房间号：</strong>${row.roomNumber || '未分配'}</p>
      <p><strong>入住人数：</strong>${row.guestCount || 2}人</p>
      <p><strong>入住日期：</strong>${row.checkInDate}</p>
      <p><strong>离店日期：</strong>${row.checkOutDate}</p>
      <p><strong>支付状态：</strong>${getPaymentStatusText(row.paymentStatus || 'unpaid')}</p>
      <p><strong>预订状态：</strong>${getStatusText(row.status)}</p>
      <p><strong>预订来源：</strong>${row.source || '直接预订'}</p>
      <p><strong>创建时间：</strong>${row.createTime || '2024-03-31 10:30:00'}</p>
      <p><strong>特殊需求：</strong>${row.specialRequests || '无'}</p>
    </div>`,
    '预订详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 快速操作区功能
const openTodayCheckin = () => {
  searchForm.dateRange = [new Date(), new Date()]
  searchForm.status = 'confirmed'
  handleSearch()
  ElMessage.success('已筛选今日入住预订')
}

const openTodayCheckout = () => {
  searchForm.dateRange = [new Date(), new Date()]
  searchForm.status = 'checked-in'
  handleSearch()
  ElMessage.success('已筛选今日离店预订')
}

const exportBookings = () => {
  ElMessage.success('预订数据导出成功')
}

const showRoomStatus = () => {
  ElMessage.info('房态一览功能正在开发中')
}

// 初始化
fetchBookingList()
</script>

<style scoped>
.bookings-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f8fafc;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.btn-add {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.25);
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.35);
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 5px;
}

.search-btn, .reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.reset-btn {
  border: 1px solid #dee2e6;
  background-color: #fff;
  color: #6c757d;
}

.reset-btn:hover {
  background-color: #f8f9fa;
  color: #343a40;
}

.list-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
  overflow: hidden;
}

.booking-table {
  margin-bottom: 20px;
}

.booking-table :deep(.el-table__header-wrapper) {
  background: #f8fafc;
}

.booking-table :deep(.el-table__header) {
  font-weight: 600;
  color: #343a40;
}

.booking-table :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.booking-table :deep(.el-table__row:hover) {
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
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  padding: 4px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 5px 5px;
}

.custom-pagination {
  padding: 8px;
  border-radius: 8px;
  background: #f8fafc;
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
  color: #333;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.booking-form {
  padding: 10px 0;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn, .submit-btn {
  min-width: 90px;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.submit-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #0a5ad1, #14307a);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.booking-detail {
  line-height: 1.8;
}

.booking-detail p {
  margin: 10px 0;
  border-bottom: 1px dashed #eaedf3;
  padding-bottom: 10px;
  display: flex;
}

.booking-detail p strong {
  min-width: 100px;
  color: #495057;
}

.booking-detail p:last-child {
  border-bottom: none;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding: 20px;
  }
  
  .btn-add {
    width: 100%;
    justify-content: center;
  }
  
  .search-form {
    flex-direction: column;
    gap: 10px;
  }
  
  .search-form .el-form-item {
    margin-bottom: 10px;
    width: 100%;
  }
}

.room-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.room-carousel {
  margin-bottom: 20px;
}

.carousel-content {
  position: relative;
  height: 100%;
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 10px;
  font-size: 14px;
}

.room-info {
  padding: 0 24px 24px;
}

.room-info h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.room-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 15px;
}

.room-details p {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #555;
}

.room-description {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  color: #555;
  line-height: 1.6;
}

/* 数据统计卡片样式 */
.stats-container {
  margin-bottom: 20px;
}

.stats-card {
  border-radius: 12px;
  height: 100px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: none;
  display: flex;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
}

.stats-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}

.stats-icon {
  font-size: 20px;
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  position: absolute;
  top: 15px;
  left: 15px;
}

.stats-info {
  padding: 15px 15px 15px 75px;
  width: 100%;
}

.stats-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #6c757d;
}

.pending-card {
  background: linear-gradient(135deg, #fff, #f8f8ff);
}

.pending-card .stats-icon {
  background-color: rgba(255, 193, 7, 0.2);
  color: #f0ad4e;
}

.pending-card .stats-value {
  color: #f0ad4e;
}

.confirmed-card {
  background: linear-gradient(135deg, #fff, #f0f8ff);
}

.confirmed-card .stats-icon {
  background-color: rgba(13, 110, 253, 0.15);
  color: #0d6efd;
}

.confirmed-card .stats-value {
  color: #0d6efd;
}

.checkin-card {
  background: linear-gradient(135deg, #fff, #f0fff0);
}

.checkin-card .stats-icon {
  background-color: rgba(40, 167, 69, 0.15);
  color: #28a745;
}

.checkin-card .stats-value {
  color: #28a745;
}

.revenue-card {
  background: linear-gradient(135deg, #fff, #fff0f5);
}

.revenue-card .stats-icon {
  background-color: rgba(220, 53, 69, 0.15);
  color: #dc3545;
}

.revenue-card .stats-value {
  color: #dc3545;
}

/* 快速操作区样式 */
.quick-actions-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.quick-actions-header {
  margin-bottom: 15px;
}

.quick-actions-header h3 {
  font-size: 16px;
  margin: 0;
  color: #333;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 90px;
  background-color: #f8fafc;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-action-item:hover {
  background-color: #f0f5ff;
  transform: translateY(-3px);
}

.quick-action-item .el-icon {
  font-size: 24px;
  color: #0d6efd;
  margin-bottom: 8px;
}

.quick-action-item span {
  font-size: 14px;
  color: #495057;
  text-align: center;
}

.custom-switch {
  --el-switch-on-color: #0d6efd;
}
</style>