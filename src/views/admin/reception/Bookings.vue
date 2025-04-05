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
          <el-card shadow="hover" class="stats-card pending-card" @click="showPendingBookings">
            <div class="stats-icon"><el-icon><Clock /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">{{ pendingCount }}</div>
              <div class="stats-label">待确认预订</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card confirmed-card" @click="showTodayCheckins">
            <div class="stats-icon"><el-icon><Calendar /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">{{ todayCheckinCount }}</div>
              <div class="stats-label">今日入住</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card checkin-card" @click="showOccupancyDetails">
            <div class="stats-icon"><el-icon><User /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">{{ occupancyRate }}%</div>
              <div class="stats-label">入住率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card revenue-card" @click="showTodayTasks">
            <div class="stats-icon"><el-icon><List /></el-icon></div>
            <div class="stats-info">
              <div class="stats-value">{{ todayTaskCount }}</div>
              <div class="stats-label">今日待办</div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, Edit, Check, Close, View, Key, Money, Picture, Calendar,
  Clock, User, Download, Right, House, List, Monitor, Connection, Message, Star,
  TrendCharts, Platform, Timer, StarFilled, Warning, Brush, Tools, Document, Loading
} from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  bookingNo: '',
  customerName: '',
  phone: '',
  status: '',
  dateRange: null
})

// 统计数据
const pendingCount = ref(8)
const todayCheckinCount = ref(15)
const occupancyRate = ref(76)
const todayTaskCount = ref(12)

// 房态数据
const totalRooms = ref(120)
const occupiedRooms = ref(91)

// 今日待办
const todayTasks = [
  { id: 1, type: 'vip', content: 'VIP客户接待 - 李先生 11:30到店', status: 'pending' },
  { id: 2, type: 'checkout', content: '302房延迟退房申请处理', status: 'pending' },
  { id: 3, type: 'service', content: '401房送加床服务', status: 'completed' },
  { id: 4, type: 'complaint', content: '处理305房噪音投诉', status: 'processing' },
  { id: 5, type: 'cleaning', content: '安排508房紧急清洁', status: 'completed' }
]

// 房型数据
const roomTypes = [
  { id: 1, name: '标准单人间', totalCount: 30, availableCount: 8, price: 480 },
  { id: 2, name: '标准双人间', totalCount: 40, availableCount: 12, price: 580 },
  { id: 3, name: '豪华大床房', totalCount: 25, availableCount: 5, price: 880 },
  { id: 4, name: '行政套房', totalCount: 15, availableCount: 3, price: 1280 },
  { id: 5, name: '总统套房', totalCount: 5, availableCount: 1, price: 3280 }
]

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

// 根据支付状态获取显示文本
const getPaymentStatusText = (status) => {
  switch(status) {
    case 'paid':
      return '已付款';
    case 'deposit':
      return '已付定金';
    case 'unpaid':
      return '未付款';
    default:
      return '未知状态';
  }
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

// 显示房态一览
const showRoomStatus = () => {
  // 生成房间状态数据
  const roomStatusData = generateRoomStatusData();
  
  ElMessageBox.alert(
    `<div class="premium-dialog-content room-status-container">
      <div class="premium-dialog-header">
        <div class="premium-icon-container blue">
          <el-icon class="premium-icon"><House /></el-icon>
        </div>
        <div class="premium-title-container">
          <h3>房态一览</h3>
          <p class="premium-subtitle">酒店实时房态概览，共${totalRooms.value}间房，当前入住${occupiedRooms.value}间</p>
        </div>
      </div>
      
      <div class="premium-tabs room-status-tabs">
        <div class="premium-tab active">所有房间</div>
        <div class="premium-tab">按楼层</div>
        <div class="premium-tab">按房型</div>
        <div class="premium-tab">待清洁</div>
      </div>
      
      <div class="room-status-summary">
        <div class="room-status-legend">
          <div class="legend-item">
            <span class="status-indicator available"></span>
            <span class="legend-text">可预订 (${roomStatusData.statusCounts.available})</span>
          </div>
          <div class="legend-item">
            <span class="status-indicator occupied"></span>
            <span class="legend-text">已入住 (${roomStatusData.statusCounts.occupied})</span>
          </div>
          <div class="legend-item">
            <span class="status-indicator reserved"></span>
            <span class="legend-text">已预订 (${roomStatusData.statusCounts.reserved})</span>
          </div>
          <div class="legend-item">
            <span class="status-indicator cleaning"></span>
            <span class="legend-text">清洁中 (${roomStatusData.statusCounts.cleaning})</span>
          </div>
          <div class="legend-item">
            <span class="status-indicator maintenance"></span>
            <span class="legend-text">维护中 (${roomStatusData.statusCounts.maintenance})</span>
          </div>
        </div>
        
        <div class="room-status-filters">
          <div class="filter-group">
            <div class="filter-label">楼层:</div>
            <div class="filter-options">
              <div class="filter-option active">全部</div>
              <div class="filter-option">2楼</div>
              <div class="filter-option">3楼</div>
              <div class="filter-option">4楼</div>
              <div class="filter-option">5楼</div>
              <div class="filter-option">6楼</div>
            </div>
          </div>
          
          <div class="filter-group">
            <div class="filter-label">房型:</div>
            <div class="filter-options">
              <div class="filter-option active">全部</div>
              <div class="filter-option">标准间</div>
              <div class="filter-option">大床房</div>
              <div class="filter-option">套房</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="premium-room-status-grid">
        ${roomStatusData.floors.map(floor => `
          <div class="floor-container">
            <div class="floor-header">
              <div class="floor-title">${floor.name}</div>
              <div class="floor-stats">
                <div class="floor-occupancy">
                  <div class="occupancy-chart" style="--occupied-percent: ${floor.occupancyRate}%;">
                    <div class="occupancy-fill"></div>
                  </div>
                  <div class="occupancy-text">${floor.occupancyRate}%</div>
                </div>
                <div class="rooms-count">${floor.occupiedCount}/${floor.totalRooms}间</div>
              </div>
            </div>
            
            <div class="rooms-grid">
              ${floor.rooms.map(room => `
                <div class="room-cell ${room.status}" data-room-number="${room.number}">
                  <div class="room-info">
                    <div class="room-number">${room.number}</div>
                    <div class="room-type-indicator">${room.typeShort}</div>
                  </div>
                  ${room.status === 'occupied' ? `
                    <div class="room-badge">
                      <span class="checkout-date">${room.checkoutDate}</span>
                    </div>
                  ` : ''}
                  ${room.isVIP ? '<div class="vip-marker"></div>' : ''}
                </div>
              `).join('')}
            </div>
          </div>
        `).join('')}
      </div>
      
      <div class="room-detail-preview">
        <div class="detail-preview-header">
          <h4>客房详情预览</h4>
          <div class="detail-preview-close">×</div>
        </div>
        <div class="detail-preview-content">
          <div class="room-detail-header">
            <div class="room-detail-number">303</div>
            <div class="room-detail-type">豪华大床房</div>
            <div class="room-detail-status occupied">已入住</div>
          </div>
          
          <div class="room-detail-image">
            <img src="https://example.com/deluxe-king-1.jpg" alt="房间图片">
          </div>
          
          <div class="room-detail-information">
            <div class="detail-group">
              <div class="detail-group-title">入住信息</div>
              <div class="detail-item">
                <div class="detail-label">客人姓名:</div>
                <div class="detail-value">张先生</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">入住日期:</div>
                <div class="detail-value">2024-04-03</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">离店日期:</div>
                <div class="detail-value">2024-04-06</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">预订号:</div>
                <div class="detail-value">BK20240403003</div>
              </div>
            </div>
            
            <div class="detail-group">
              <div class="detail-group-title">房间信息</div>
              <div class="detail-item">
                <div class="detail-label">价格:</div>
                <div class="detail-value">¥880/晚</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">床型:</div>
                <div class="detail-value">特大床</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">楼层:</div>
                <div class="detail-value">3楼</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">最近清洁:</div>
                <div class="detail-value">今日 08:30</div>
              </div>
            </div>
          </div>
          
          <div class="room-detail-actions">
            <button class="detail-action-button check-out">
              <el-icon><Right /></el-icon> 办理退房
            </button>
            <button class="detail-action-button room-service">
              <el-icon><Message /></el-icon> 客房服务
            </button>
            <button class="detail-action-button room-maintenance">
              <el-icon><Tools /></el-icon> 维修请求
            </button>
          </div>
        </div>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true,
      customClass: 'premium-dialog-box wide-dialog room-status-dialog'
    }
  )
  
  // 模拟客房点击事件
  setTimeout(() => {
    const roomCells = document.querySelectorAll('.room-cell');
    const detailPreview = document.querySelector('.room-detail-preview');
    const closeButton = document.querySelector('.detail-preview-close');
    
    if (roomCells && detailPreview && closeButton) {
      roomCells.forEach(cell => {
        cell.addEventListener('click', () => {
          detailPreview.classList.add('show');
        });
      });
      
      closeButton.addEventListener('click', () => {
        detailPreview.classList.remove('show');
      });
    }
  }, 100);
}

// 生成房态数据
const generateRoomStatusData = () => {
  const floors = [
    { id: 2, name: '2楼', totalRooms: 24 },
    { id: 3, name: '3楼', totalRooms: 24 },
    { id: 4, name: '4楼', totalRooms: 24 },
    { id: 5, name: '5楼', totalRooms: 24 },
    { id: 6, name: '6楼', totalRooms: 24 }
  ];
  
  const statusCounts = {
    available: 0,
    occupied: 0,
    reserved: 0,
    cleaning: 0,
    maintenance: 0
  };
  
  const roomTypes = {
    standard: { name: '标准间', short: 'STD' },
    twin: { name: '标准双人间', short: 'TWN' },
    deluxe: { name: '豪华大床房', short: 'DLX' },
    executive: { name: '行政套房', short: 'EXE' },
    suite: { name: '总统套房', short: 'SUT' }
  };
  
  // 根据楼层生成房间
  const floorsData = floors.map(floor => {
    const rooms = [];
    let occupiedCount = 0;
    
    for (let i = 1; i <= floor.totalRooms; i++) {
      const roomNumber = `${floor.id}${i.toString().padStart(2, '0')}`;
      let roomType;
      
      // 根据房号分配房型
      if (i <= 8) roomType = roomTypes.twin;
      else if (i <= 16) roomType = roomTypes.deluxe;
      else if (i <= 22) roomType = roomTypes.executive;
      else roomType = roomTypes.suite;
      
      // 设置房间状态
      let status = getWeightedRandomStatus();
      let checkoutDate = null;
      let isVIP = false;
      
      // 统计状态
      statusCounts[status]++;
      
      // 如果是已入住，随机生成退房日期
      if (status === 'occupied') {
        occupiedCount++;
        const today = new Date();
        const daysToAdd = Math.floor(Math.random() * 5) + 1;
        const checkoutDay = new Date(today);
        checkoutDay.setDate(today.getDate() + daysToAdd);
        checkoutDate = `${checkoutDay.getMonth() + 1}/${checkoutDay.getDate()}`;
        
        // 随机设置VIP
        isVIP = Math.random() < 0.15;
      }
      
      rooms.push({
        number: roomNumber,
        type: roomType.name,
        typeShort: roomType.short,
        status,
        checkoutDate,
        isVIP
      });
    }
    
    return {
      ...floor,
      rooms,
      occupiedCount,
      occupancyRate: Math.round((occupiedCount / floor.totalRooms) * 100)
    };
  });
  
  return {
    floors: floorsData,
    statusCounts
  };
}

// 获取加权随机房间状态
const getWeightedRandomStatus = () => {
  const statuses = ['available', 'occupied', 'reserved', 'cleaning', 'maintenance'];
  const weights = [0.25, 0.60, 0.08, 0.05, 0.02]; // 权重分布
  
  const rand = Math.random();
  let sum = 0;
  for (let i = 0; i < weights.length; i++) {
    sum += weights[i];
    if (rand < sum) return statuses[i];
  }
  return statuses[0];
}

// 显示今日待办
const showTodayTasks = () => {
  // 扩展更多待办事项数据
  const todayTasksExtended = [
    { 
      id: 1, 
      type: 'vip', 
      content: 'VIP客户接待 - 李先生', 
      status: 'pending', 
      priority: 'high',
      time: '11:30',
      assignee: '张经理',
      details: 'VIP客户李先生于11:30到店，需提前准备好欢迎水果篮并安排升级房间'
    },
    { 
      id: 2, 
      type: 'checkout', 
      content: '302房延迟退房处理', 
      status: 'pending', 
      priority: 'medium',
      time: '13:00',
      assignee: '王前台',
      details: '客人需要延迟至下午2点退房，已收取半天房费'
    },
    { 
      id: 3, 
      type: 'service', 
      content: '401房送加床服务', 
      status: 'completed', 
      priority: 'normal',
      time: '09:15',
      assignee: '李服务员',
      completedTime: '09:45',
      details: '加床服务已完成，收取加床费用200元'
    },
    { 
      id: 4, 
      type: 'complaint', 
      content: '处理305房噪音投诉', 
      status: 'processing', 
      priority: 'high',
      time: '10:30',
      assignee: '赵经理',
      details: '305房客人投诉隔壁房间噪音问题，已联系隔壁房间客人'
    },
    { 
      id: 5, 
      type: 'cleaning', 
      content: '安排508房紧急清洁', 
      status: 'completed', 
      priority: 'high',
      time: '08:45',
      assignee: '清洁部王组长',
      completedTime: '09:30',
      details: '508房今日有重要客人入住，需要额外清洁检查'
    },
    { 
      id: 6, 
      type: 'maintenance', 
      content: '修理605房空调', 
      status: 'pending', 
      priority: 'medium',
      time: '14:00',
      assignee: '维修部李师傅',
      details: '客人反映空调制冷效果不佳，需检修'
    },
    { 
      id: 7, 
      type: 'reservation', 
      content: '确认团队预订事宜', 
      status: 'processing', 
      priority: 'medium',
      time: '11:00',
      assignee: '销售部陈经理',
      details: '华为公司团队预订20间房，需确认最终房型分配和接送机安排'
    }
  ]

  ElMessageBox.alert(
    `<div class="premium-dialog-content tasks-container">
      <div class="premium-dialog-header">
        <div class="premium-icon-container red">
          <el-icon class="premium-icon"><List /></el-icon>
        </div>
        <div class="premium-title-container">
          <h3>今日待办事项</h3>
          <p class="premium-subtitle">共 ${todayTasksExtended.length} 项任务，${todayTasksExtended.filter(t => t.status === 'completed').length} 项已完成</p>
        </div>
      </div>
      
      <div class="premium-tabs tasks-tabs">
        <div class="premium-tab active">全部 (${todayTasksExtended.length})</div>
        <div class="premium-tab">待处理 (${todayTasksExtended.filter(t => t.status === 'pending').length})</div>
        <div class="premium-tab">处理中 (${todayTasksExtended.filter(t => t.status === 'processing').length})</div>
        <div class="premium-tab">已完成 (${todayTasksExtended.filter(t => t.status === 'completed').length})</div>
      </div>
      
      <div class="tasks-summary">
        <div class="task-summary-item">
          <div class="task-summary-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="task-summary-info">
            <div class="task-summary-value">${todayTasksExtended.filter(t => t.status === 'pending').length}</div>
            <div class="task-summary-label">待处理</div>
          </div>
        </div>
        <div class="task-summary-item">
          <div class="task-summary-icon processing">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="task-summary-info">
            <div class="task-summary-value">${todayTasksExtended.filter(t => t.status === 'processing').length}</div>
            <div class="task-summary-label">处理中</div>
          </div>
        </div>
        <div class="task-summary-item">
          <div class="task-summary-icon completed">
            <el-icon><Check /></el-icon>
          </div>
          <div class="task-summary-info">
            <div class="task-summary-value">${todayTasksExtended.filter(t => t.status === 'completed').length}</div>
            <div class="task-summary-label">已完成</div>
          </div>
        </div>
        <div class="task-summary-item">
          <div class="task-summary-icon high-priority">
            <el-icon><Star /></el-icon>
          </div>
          <div class="task-summary-info">
            <div class="task-summary-value">${todayTasksExtended.filter(t => t.priority === 'high').length}</div>
            <div class="task-summary-label">高优先级</div>
          </div>
        </div>
      </div>
      
      <div class="premium-tasks-list">
        ${todayTasksExtended.map(task => `
          <div class="premium-task-card ${task.status}">
            <div class="task-card-header">
              <div class="task-type-container">
                <div class="task-type-badge ${task.type}">
                  ${getTaskTypeIcon(task.type)}
                </div>
                <div class="task-time">
                  <el-icon><Timer /></el-icon> ${task.time}
                  ${task.status === 'completed' ? `<span class="completed-time">(已完成: ${task.completedTime})</span>` : ''}
                </div>
              </div>
              
              <div class="task-priority ${task.priority}">
                ${task.priority === 'high' ? '<el-icon><Star /></el-icon> 高' : 
                  task.priority === 'medium' ? '<el-icon><StarFilled /></el-icon> 中' : '普通'}
              </div>
            </div>
            
            <div class="task-card-body">
              <div class="task-content">
                <div class="task-title">${task.content}</div>
                <div class="task-description">${task.details}</div>
              </div>
            </div>
            
            <div class="task-card-footer">
              <div class="task-assignee">
                <div class="assignee-label">负责人:</div>
                <div class="assignee-avatar">${task.assignee.substring(0, 1)}</div>
                <div class="assignee-name">${task.assignee}</div>
              </div>
              
              <div class="task-actions">
                ${task.status === 'pending' ? `
                  <el-button type="primary" size="small" class="task-action-btn">
                    <el-icon><Check /></el-icon> 开始处理
                  </el-button>
                ` : task.status === 'processing' ? `
                  <el-button type="success" size="small" class="task-action-btn">
                    <el-icon><Check /></el-icon> 标记完成
                  </el-button>
                ` : `
                  <el-button type="info" size="small" plain class="task-action-btn">
                    <el-icon><Document /></el-icon> 查看详情
                  </el-button>
                `}
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true,
      customClass: 'premium-dialog-box wide-dialog tasks-dialog'
    }
  )
}

// 获取任务类型图标
const getTaskTypeIcon = (type) => {
  switch(type) {
    case 'vip':
      return '<el-icon><Star /></el-icon>';
    case 'checkout':
      return '<el-icon><Right /></el-icon>';
    case 'service':
      return '<el-icon><Message /></el-icon>';
    case 'complaint':
      return '<el-icon><Warning /></el-icon>';
    case 'cleaning':
      return '<el-icon><Brush /></el-icon>';
    case 'maintenance':
      return '<el-icon><Tools /></el-icon>';
    case 'reservation':
      return '<el-icon><Calendar /></el-icon>';
    default:
      return '<el-icon><List /></el-icon>';
  }
}

// 显示待确认预订详情
const showPendingBookings = () => {
  const pendingBookings = [
    {
      bookingNo: 'BK20240403001',
      customerName: '张小明',
      phone: '13611112222',
      roomType: '标准双人间',
      checkInDate: '2024-04-05',
      checkOutDate: '2024-04-07',
      createTime: '2024-04-03 14:30',
      source: '电话预订',
      price: 580,
      nights: 2,
      guestCount: 2,
      specialRequests: '希望安排靠窗房间'
    },
    {
      bookingNo: 'BK20240403002',
      customerName: '李华',
      phone: '13922223333',
      roomType: '豪华大床房',
      checkInDate: '2024-04-06',
      checkOutDate: '2024-04-08',
      createTime: '2024-04-03 16:15',
      source: '在线平台',
      price: 880,
      nights: 2,
      guestCount: 1,
      specialRequests: '需要接机服务'
    },
    {
      bookingNo: 'BK20240404001',
      customerName: '王强',
      phone: '13833334444',
      roomType: '行政套房',
      checkInDate: '2024-04-10',
      checkOutDate: '2024-04-15',
      createTime: '2024-04-04 09:25',
      source: '旅行社',
      price: 1280,
      nights: 5,
      guestCount: 2,
      specialRequests: 'VIP客户，需提前准备水果和红酒'
    }
  ]
  
  ElMessageBox.alert(
    `<div class="premium-dialog-content pending-bookings-container">
      <div class="premium-dialog-header">
        <div class="premium-icon-container">
          <el-icon class="premium-icon"><Clock /></el-icon>
        </div>
        <div class="premium-title-container">
          <h3>待确认预订</h3>
          <p class="premium-subtitle">共 ${pendingBookings.length} 条待处理预订需要确认</p>
        </div>
      </div>
      
      <div class="premium-tabs">
        <div class="premium-tab active">全部(${pendingBookings.length})</div>
        <div class="premium-tab">今日新增(1)</div>
        <div class="premium-tab">明日到店(2)</div>
      </div>
      
      <div class="premium-card-list">
        ${pendingBookings.map(booking => `
          <div class="premium-booking-card">
            <div class="premium-card-header">
              <div class="premium-booking-info">
                <div class="premium-booking-no">${booking.bookingNo}</div>
                <div class="premium-booking-time">
                  <el-icon><Calendar /></el-icon>
                  <span>创建于 ${booking.createTime}</span>
                </div>
              </div>
              <div class="premium-booking-tag">${booking.source}</div>
            </div>
            
            <div class="premium-card-body">
              <div class="premium-guest-info">
                <div class="premium-avatar">
                  ${booking.customerName.substring(0, 1)}
                </div>
                <div class="premium-guest-details">
                  <div class="premium-customer-name">${booking.customerName}</div>
                  <div class="premium-phone">${booking.phone}</div>
                </div>
              </div>
              
              <div class="premium-booking-details">
                <div class="premium-detail-item">
                  <div class="premium-detail-label">房型</div>
                  <div class="premium-detail-value">${booking.roomType}</div>
                </div>
                <div class="premium-detail-item">
                  <div class="premium-detail-label">价格</div>
                  <div class="premium-detail-value price">¥${booking.price} × ${booking.nights}晚</div>
                </div>
                <div class="premium-detail-item">
                  <div class="premium-detail-label">入住日期</div>
                  <div class="premium-detail-value">${booking.checkInDate}</div>
                </div>
                <div class="premium-detail-item">
                  <div class="premium-detail-label">离店日期</div>
                  <div class="premium-detail-value">${booking.checkOutDate}</div>
                </div>
                <div class="premium-detail-item full-width">
                  <div class="premium-detail-label">特殊要求</div>
                  <div class="premium-detail-value request">${booking.specialRequests || '无'}</div>
                </div>
              </div>
            </div>
            
            <div class="premium-card-footer">
              <el-button type="primary" size="small" class="premium-action-btn confirm-btn">
                <el-icon><Check /></el-icon> 确认预订
              </el-button>
              <el-button type="warning" size="small" class="premium-action-btn">
                <el-icon><Edit /></el-icon> 修改信息
              </el-button>
              <el-button type="danger" size="small" class="premium-action-btn">
                <el-icon><Close /></el-icon> 取消预订
              </el-button>
            </div>
          </div>
        `).join('')}
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true,
      customClass: 'premium-dialog-box wide-dialog'
    }
  )
}

// 显示今日入住详情
const showTodayCheckins = () => {
  // 模拟今日入住数据
  const todayBookings = [
    {
      bookingNo: 'BK20240404001',
      customerName: '王五',
      phone: '13912345678',
      roomType: '豪华大床房',
      checkInTime: '14:30',
      checkOutDate: '2024-04-06',
      status: 'confirmed',
      paymentStatus: 'deposit',
      nights: 2,
      price: 880,
      totalAmount: 1760,
      paidAmount: 500,
      source: '官网预订',
      createTime: '2024-04-01 10:30',
      specialRequests: '希望安排高层房间',
      guestCount: 2,
      estimatedArrival: '14:30',
      hasMessage: true,
      isVIP: true
    },
    {
      bookingNo: 'BK20240404002',
      customerName: '赵六',
      phone: '13887654321',
      roomType: '标准双人间',
      checkInTime: '15:00',
      checkOutDate: '2024-04-07',
      status: 'confirmed',
      paymentStatus: 'unpaid',
      nights: 3,
      price: 580,
      totalAmount: 1740,
      paidAmount: 0,
      source: '电话预订',
      createTime: '2024-04-02 15:45',
      specialRequests: '',
      guestCount: 2,
      estimatedArrival: '15:00',
      hasMessage: false,
      isVIP: false
    },
    {
      bookingNo: 'BK20240404003',
      customerName: '李明',
      phone: '13765432198',
      roomType: '行政套房',
      checkInTime: '16:30',
      checkOutDate: '2024-04-08',
      status: 'confirmed',
      paymentStatus: 'paid',
      nights: 4,
      price: 1280,
      totalAmount: 5120,
      paidAmount: 5120,
      source: '旅行社',
      createTime: '2024-04-02 09:15',
      specialRequests: '需要婴儿床',
      guestCount: 3,
      estimatedArrival: '16:30',
      isVIP: true,
      hasMessage: true
    }
  ]
  
  ElMessageBox.alert(
    `<div class="premium-dialog-content checkin-bookings-container">
      <div class="premium-dialog-header">
        <div class="premium-icon-container blue">
          <el-icon class="premium-icon"><Calendar /></el-icon>
        </div>
        <div class="premium-title-container">
          <h3>今日入住</h3>
          <p class="premium-subtitle">共 ${todayBookings.length} 组客人预计今日入住</p>
        </div>
      </div>
      
      <div class="premium-data-overview">
        <div class="premium-data-item">
          <div class="premium-data-value">${todayBookings.length}</div>
          <div class="premium-data-label">入住总数</div>
        </div>
        <div class="premium-data-item">
          <div class="premium-data-value">1</div>
          <div class="premium-data-label">VIP客人</div>
        </div>
        <div class="premium-data-item">
          <div class="premium-data-value">2</div>
          <div class="premium-data-label">待收款</div>
        </div>
        <div class="premium-data-item">
          <div class="premium-data-value">2</div>
          <div class="premium-data-label">有特殊要求</div>
        </div>
      </div>
      
      <div class="premium-checkin-timeline">
        <div class="premium-timeline-header">
          <h4>今日入住时间线</h4>
          <div class="premium-timeline-legend">
            <span class="timeline-legend-item">
              <span class="timeline-status pending"></span> 待入住
            </span>
            <span class="timeline-legend-item">
              <span class="timeline-status completed"></span> 已入住
            </span>
          </div>
        </div>
        
        <div class="premium-timeline-container">
          ${todayBookings.map((booking, index) => `
            <div class="premium-timeline-item ${index === 0 ? 'completed' : 'pending'}">
              <div class="timeline-time">${booking.estimatedArrival}</div>
              <div class="timeline-connector">
                <div class="timeline-dot"></div>
                ${index < todayBookings.length - 1 ? '<div class="timeline-line"></div>' : ''}
              </div>
              <div class="timeline-card">
                <div class="timeline-card-header">
                  <div class="booking-customer-info">
                    <div class="premium-avatar ${booking.isVIP ? 'vip' : ''}">
                      ${booking.customerName.substring(0, 1)}
                    </div>
                    <div class="customer-details">
                      <div class="customer-name">
                        ${booking.customerName}
                        ${booking.isVIP ? '<span class="vip-badge">VIP</span>' : ''}
                      </div>
                      <div class="booking-info">
                        <span class="booking-no">${booking.bookingNo}</span>
                        <span class="booking-nights">${booking.nights}晚</span>
                      </div>
                    </div>
                  </div>
                  <div class="booking-status-tag ${booking.paymentStatus}">
                    ${getPaymentStatusText(booking.paymentStatus)}
                  </div>
                </div>
                
                <div class="timeline-card-body">
                  <div class="booking-detail-row">
                    <div class="booking-detail-item">
                      <span class="detail-icon"><el-icon><House /></el-icon></span>
                      <span class="detail-label">房型:</span>
                      <span class="detail-value">${booking.roomType}</span>
                    </div>
                    <div class="booking-detail-item">
                      <span class="detail-icon"><el-icon><User /></el-icon></span>
                      <span class="detail-label">人数:</span>
                      <span class="detail-value">${booking.guestCount}人</span>
                    </div>
                  </div>
                  
                  <div class="booking-detail-row">
                    <div class="booking-detail-item">
                      <span class="detail-icon"><el-icon><Money /></el-icon></span>
                      <span class="detail-label">房费:</span>
                      <span class="detail-value">¥${booking.totalAmount}</span>
                    </div>
                    <div class="booking-detail-item">
                      <span class="detail-icon"><el-icon><Phone /></el-icon></span>
                      <span class="detail-label">电话:</span>
                      <span class="detail-value">${booking.phone}</span>
                    </div>
                  </div>
                  
                  ${booking.specialRequests ? `
                    <div class="booking-special-requests">
                      <el-icon><InfoFilled /></el-icon>
                      <span>${booking.specialRequests}</span>
                    </div>
                  ` : ''}
                </div>
                
                <div class="timeline-card-footer">
                  ${index === 0 ? `
                    <div class="checkin-status completed">
                      <el-icon><Check /></el-icon> 已办理入住 (12:45)
                    </div>
                    <el-button type="primary" size="small" plain class="timeline-action-btn">
                      <el-icon><Document /></el-icon> 查看详情
                    </el-button>
                  ` : `
                    <el-button type="primary" size="small" class="timeline-action-btn">
                      <el-icon><Key /></el-icon> 办理入住
                    </el-button>
                    <el-button type="warning" size="small" plain class="timeline-action-btn">
                      <el-icon><Message /></el-icon> 联系客人
                    </el-button>
                  `}
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true,
      customClass: 'premium-dialog-box wide-dialog'
    }
  )
}

// 显示入住率详情
const showOccupancyDetails = () => {
  // 模拟每日入住率数据
  const dailyOccupancy = [
    { date: '4月1日', rate: 65 },
    { date: '4月2日', rate: 70 },
    { date: '4月3日', rate: 72 },
    { date: '4月4日', rate: 76 },
    { date: '4月5日', rate: 85 },
    { date: '4月6日', rate: 90 },
    { date: '4月7日', rate: 88 }
  ];
  
  // 模拟楼层入住率数据
  const floorOccupancy = [
    { floor: '2楼', total: 24, occupied: 20, rate: 83 },
    { floor: '3楼', total: 24, occupied: 19, rate: 79 },
    { floor: '4楼', total: 24, occupied: 18, rate: 75 },
    { floor: '5楼', total: 24, occupied: 18, rate: 75 },
    { floor: '6楼', total: 24, occupied: 16, rate: 67 }
  ];

  ElMessageBox.alert(
    `<div class="premium-dialog-content occupancy-container">
      <div class="premium-dialog-header">
        <div class="premium-icon-container green">
          <el-icon class="premium-icon"><User /></el-icon>
        </div>
        <div class="premium-title-container">
          <h3>入住率分析</h3>
          <p class="premium-subtitle">当前酒店整体入住率 ${occupancyRate.value}%</p>
        </div>
      </div>
      
      <div class="premium-occupancy-overview">
        <div class="premium-overview-section">
          <div class="premium-overview-chart">
            <div class="premium-chart-container">
              <div class="donut-chart-container">
                <div class="donut-chart">
                  <div class="donut-fill" style="--percentage: ${occupancyRate.value}%;"></div>
                </div>
                <div class="donut-content">
                  <div class="donut-percentage">${occupancyRate.value}%</div>
                  <div class="donut-label">入住率</div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="premium-overview-stats">
            <div class="premium-stat-item">
              <div class="premium-stat-value">${totalRooms.value}</div>
              <div class="premium-stat-label">总房间数</div>
            </div>
            <div class="premium-stat-item">
              <div class="premium-stat-value">${occupiedRooms.value}</div>
              <div class="premium-stat-label">已入住</div>
            </div>
            <div class="premium-stat-item">
              <div class="premium-stat-value">${totalRooms.value - occupiedRooms.value}</div>
              <div class="premium-stat-label">空闲房间</div>
            </div>
            <div class="premium-stat-item">
              <div class="premium-stat-value">15</div>
              <div class="premium-stat-label">今日入住</div>
            </div>
            <div class="premium-stat-item">
              <div class="premium-stat-value">12</div>
              <div class="premium-stat-label">今日离店</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="premium-tabs occupancy-tabs">
        <div class="premium-tab active">入住率概览</div>
        <div class="premium-tab">房型分布</div>
        <div class="premium-tab">历史对比</div>
      </div>
      
      <div class="premium-flex-container">
        <div class="premium-flex-item">
          <div class="premium-card">
            <h4 class="premium-card-title">
              <el-icon><TrendCharts /></el-icon> 近7天入住率趋势
            </h4>
            <div class="trend-chart-container">
              <div class="trend-chart">
                ${dailyOccupancy.map(day => `
                  <div class="trend-bar-container">
                    <div class="trend-bar-label">${day.date}</div>
                    <div class="trend-bar">
                      <div class="trend-bar-fill" style="height: ${day.rate}%;"></div>
                    </div>
                    <div class="trend-bar-value">${day.rate}%</div>
                  </div>
                `).join('')}
              </div>
            </div>
          </div>
        </div>
        
        <div class="premium-flex-item">
          <div class="premium-card">
            <h4 class="premium-card-title">
              <el-icon><Platform /></el-icon> 各楼层入住率
            </h4>
            <div class="floor-occupancy-list">
              ${floorOccupancy.map(floor => `
                <div class="floor-occupancy-item">
                  <div class="floor-name">${floor.floor}</div>
                  <div class="floor-stats">
                    <div class="floor-occupied">${floor.occupied}/${floor.total}</div>
                    <div class="floor-rate">${floor.rate}%</div>
                  </div>
                  <div class="floor-progress">
                    <div class="floor-progress-bar" style="width: ${floor.rate}%;"></div>
                  </div>
                </div>
              `).join('')}
            </div>
          </div>
        </div>
      </div>
      
      <div class="premium-card room-type-card">
        <h4 class="premium-card-title">
          <el-icon><House /></el-icon> 各房型入住情况
        </h4>
        <div class="room-type-occupancy">
          <table class="premium-table">
            <thead>
              <tr>
                <th>房型名称</th>
                <th>总数量</th>
                <th>已入住</th>
                <th>可用数量</th>
                <th>房价</th>
                <th>入住率</th>
              </tr>
            </thead>
            <tbody>
              ${roomTypes.map(type => `
                <tr>
                  <td>
                    <div class="room-type-name">
                      <div class="room-type-icon ${type.name.includes('套房') ? 'suite' : type.name.includes('大床') ? 'deluxe' : 'standard'}"></div>
                      ${type.name}
                    </div>
                  </td>
                  <td>${type.totalCount}</td>
                  <td>${type.totalCount - type.availableCount}</td>
                  <td>${type.availableCount}</td>
                  <td class="price-cell">¥${type.price}</td>
                  <td>
                    <div class="table-progress">
                      <div class="table-progress-bar" style="width: ${Math.round((type.totalCount - type.availableCount) / type.totalCount * 100)}%;"></div>
                      <span>${Math.round((type.totalCount - type.availableCount) / type.totalCount * 100)}%</span>
                    </div>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showClose: true,
      customClass: 'premium-dialog-box wide-dialog occupancy-dialog'
    }
  )
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
  cursor: pointer;
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

/* 详情弹窗样式 */
.detail-dialog-content {
  padding: 5px 0;
}

.detail-dialog-content h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 600;
  color: #333;
  font-size: 18px;
  text-align: center;
}

.detail-dialog-content h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #444;
  font-size: 16px;
}

.detail-list {
  max-height: 400px;
  overflow-y: auto;
}

.detail-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  border-left: 4px solid #0d6efd;
}

.detail-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.booking-no {
  font-weight: 600;
  color: #0d6efd;
}

.booking-date, .booking-time {
  color: #6c757d;
}

.detail-item-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  font-size: 14px;
}

/* 入住率详情样式 */
.occupancy-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.occupancy-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
}

.occupancy-label {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 5px;
}

.occupancy-value {
  font-size: 24px;
  font-weight: 600;
  color: #0d6efd;
}

.room-type-table {
  width: 100%;
  border-collapse: collapse;
}

.room-type-table th,
.room-type-table td {
  padding: 10px;
  border: 1px solid #dee2e6;
  text-align: center;
}

.room-type-table th {
  background: #f8f9fa;
  font-weight: 600;
}

/* 今日待办样式 */
.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  align-items: flex-start;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  position: relative;
}

.task-item.pending {
  border-left: 4px solid #ffc107;
}

.task-item.processing {
  border-left: 4px solid #0d6efd;
}

.task-item.completed {
  border-left: 4px solid #28a745;
  opacity: 0.7;
}

.task-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 12px;
  flex-shrink: 0;
}

.task-icon.vip {
  background-color: rgba(255, 193, 7, 0.2);
  color: #ffc107;
}

.task-icon.checkout {
  background-color: rgba(13, 110, 253, 0.15);
  color: #0d6efd;
}

.task-icon.service {
  background-color: rgba(40, 167, 69, 0.15);
  color: #28a745;
}

.task-icon.complaint {
  background-color: rgba(220, 53, 69, 0.15);
  color: #dc3545;
}

.task-icon.cleaning {
  background-color: rgba(111, 66, 193, 0.15);
  color: #6f42c1;
}

.task-content {
  flex-grow: 1;
}

.task-text {
  font-size: 14px;
  margin-bottom: 5px;
}

.task-status {
  font-size: 12px;
  color: #6c757d;
}

/* 房态一览样式 */
.room-status-dialog {
  min-width: 500px;
}

.room-status-tabs {
  display: flex;
  border-bottom: 1px solid #dee2e6;
  margin-bottom: 15px;
}

.room-status-tab {
  padding: 8px 15px;
  cursor: pointer;
  font-size: 14px;
  color: #6c757d;
}

.room-status-tab.active {
  border-bottom: 2px solid #0d6efd;
  color: #0d6efd;
  font-weight: 500;
}

.room-status-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaedf3;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #495057;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 5px;
}

.status-dot.available {
  background-color: #28a745;
}

.status-dot.occupied {
  background-color: #dc3545;
}

.status-dot.reserved {
  background-color: #ffc107;
}

.status-dot.cleaning {
  background-color: #17a2b8;
}

.status-dot.maintenance {
  background-color: #6c757d;
}

.room-status-grid {
  max-height: 400px;
  overflow-y: auto;
}

.floor-label {
  font-weight: 600;
  color: #495057;
  padding: 5px 0;
  margin-top: 10px;
}

.floor-rooms {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  margin-bottom: 15px;
}

.room-cell {
  padding: 8px;
  border-radius: 5px;
  text-align: center;
  position: relative;
  height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: all 0.2s;
  cursor: pointer;
}

.room-cell:hover {
  transform: scale(1.05);
}

.room-cell.available {
  background-color: rgba(40, 167, 69, 0.2);
  border: 1px solid rgba(40, 167, 69, 0.3);
}

.room-cell.occupied {
  background-color: rgba(220, 53, 69, 0.2);
  border: 1px solid rgba(220, 53, 69, 0.3);
}

.room-cell.reserved {
  background-color: rgba(255, 193, 7, 0.2);
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.room-cell.cleaning {
  background-color: rgba(23, 162, 184, 0.2);
  border: 1px solid rgba(23, 162, 184, 0.3);
}

.room-cell.maintenance {
  background-color: rgba(108, 117, 125, 0.2);
  border: 1px solid rgba(108, 117, 125, 0.3);
}

.room-number {
  font-weight: 600;
  font-size: 16px;
}

.room-type-short {
  font-size: 12px;
  color: #6c757d;
}

:deep(.large-message-box) {
  width: 650px;
  max-width: 90vw;
}

/* 高级弹窗共享样式 */
:deep(.premium-dialog-box) {
  max-width: 95vw;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.premium-dialog-box .el-dialog__header) {
  display: none; /* 隐藏原始标题，使用自定义标题 */
}

:deep(.premium-dialog-box .el-dialog__body) {
  padding: 0;
}

:deep(.wide-dialog) {
  width: 1000px !important;
}

:deep(.premium-dialog-box .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #eaedf3;
  background-color: #f8fafc;
}

.premium-dialog-content {
  position: relative;
  padding-bottom: 20px;
}

.premium-dialog-header {
  display: flex;
  align-items: center;
  padding: 24px 30px;
  background: linear-gradient(135deg, #f8fafc, #edf2f7);
  border-bottom: 1px solid #eaedf3;
}

.premium-icon-container {
  width: 58px;
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 20px;
  background: linear-gradient(135deg, #FFC107, #FF9800);
  color: white;
  box-shadow: 0 6px 16px rgba(255, 193, 7, 0.3);
}

.premium-icon-container.blue {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.premium-icon-container.green {
  background: linear-gradient(135deg, #28a745, #20c997);
  box-shadow: 0 6px 16px rgba(40, 167, 69, 0.3);
}

.premium-icon-container.red {
  background: linear-gradient(135deg, #dc3545, #c71f37);
  box-shadow: 0 6px 16px rgba(220, 53, 69, 0.3);
}

.premium-icon {
  font-size: 28px;
}

.premium-title-container h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.premium-subtitle {
  margin: 5px 0 0;
  color: #6c757d;
  font-size: 14px;
}

.premium-tabs {
  display: flex;
  border-bottom: 1px solid #eaedf3;
  padding: 0 30px;
  margin-bottom: 20px;
  background-color: white;
}

.premium-tab {
  padding: 16px 24px;
  font-size: 14px;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.premium-tab.active {
  color: #0d6efd;
  font-weight: 500;
}

.premium-tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #0d6efd;
}

.premium-tab:hover:not(.active) {
  color: #495057;
  background-color: #f8f9fa;
}

.premium-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #eaedf3;
}

.premium-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

/* 待确认预订样式 */
.pending-bookings-container {
  padding-bottom: 30px;
}

.premium-card-list {
  padding: 0 30px;
}

.premium-booking-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  overflow: hidden;
  border: 1px solid #eaedf3;
  transition: all 0.3s ease;
}

.premium-booking-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.premium-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.premium-booking-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.premium-booking-no {
  font-weight: 600;
  color: #0d6efd;
  font-size: 15px;
}

.premium-booking-time {
  color: #6c757d;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.premium-booking-tag {
  padding: 5px 10px;
  background-color: #e6f7ff;
  color: #1890ff;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.premium-card-body {
  padding: 20px;
}

.premium-guest-info {
  display: flex;
  margin-bottom: 20px;
}

.premium-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #0d6efd;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 500;
  margin-right: 15px;
}

.premium-avatar.vip {
  background: linear-gradient(135deg, #FFC107, #FF9800);
  box-shadow: 0 4px 10px rgba(255, 193, 7, 0.3);
}

.premium-guest-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.premium-customer-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
}

.premium-phone {
  font-size: 14px;
  color: #6c757d;
}

.premium-booking-details {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  background-color: #f8fafc;
  padding: 15px;
  border-radius: 8px;
}

.premium-detail-item {
  flex: 1 1 calc(50% - 15px);
  min-width: 180px;
}

.premium-detail-item.full-width {
  flex: 1 1 100%;
}

.premium-detail-label {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 5px;
}

.premium-detail-value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.premium-detail-value.price {
  color: #f56c6c;
}

.premium-detail-value.request {
  font-weight: normal;
  font-style: italic;
  color: #555;
}

.premium-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 20px;
  background-color: #f8fafc;
  border-top: 1px solid #eaedf3;
}

.premium-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.confirm-btn {
  background: linear-gradient(135deg, #28a745, #20c997);
  border: none;
}

/* 今日入住样式 */
.checkin-bookings-container {
  padding-bottom: 30px;
}

.premium-data-overview {
  display: flex;
  justify-content: space-between;
  padding: 0 30px 20px;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaedf3;
}

.premium-data-item {
  flex: 1;
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border: 1px solid #eaedf3;
}

.premium-data-value {
  font-size: 28px;
  font-weight: 600;
  color: #0d6efd;
  margin-bottom: 5px;
}

.premium-data-label {
  font-size: 14px;
  color: #6c757d;
}

.premium-checkin-timeline {
  padding: 0 30px;
}

.premium-timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.premium-timeline-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.premium-timeline-legend {
  display: flex;
  gap: 15px;
}

.timeline-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6c757d;
}

.timeline-status {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.timeline-status.pending {
  background-color: #ffc107;
}

.timeline-status.completed {
  background-color: #28a745;
}

.premium-timeline-container {
  position: relative;
}

.premium-timeline-item {
  display: flex;
  margin-bottom: 30px;
}

.timeline-time {
  width: 70px;
  text-align: right;
  font-size: 14px;
  font-weight: 500;
  color: #495057;
  padding-top: 15px;
}

.timeline-connector {
  margin: 0 15px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #ffc107;
  border: 3px solid white;
  box-shadow: 0 0 0 1px #ffc107;
  z-index: 1;
}

.premium-timeline-item.completed .timeline-dot {
  background-color: #28a745;
  box-shadow: 0 0 0 1px #28a745;
}

.timeline-line {
  position: absolute;
  top: 16px;
  width: 2px;
  height: calc(100% + 30px);
  background-color: #e9ecef;
}

.timeline-card {
  flex: 1;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1px solid #eaedf3;
}

.timeline-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.booking-customer-info {
  display: flex;
  align-items: center;
}

.customer-details {
  margin-left: 15px;
}

.customer-name {
  font-weight: 600;
  font-size: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 3px;
}

.vip-badge {
  padding: 2px 6px;
  background: linear-gradient(135deg, #FFC107, #FF9800);
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.booking-info {
  display: flex;
  gap: 10px;
  font-size: 13px;
  color: #6c757d;
}

.booking-status-tag {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.booking-status-tag.unpaid {
  background-color: #fef2f2;
  color: #ef4444;
}

.booking-status-tag.deposit {
  background-color: #fff7ed;
  color: #f59e0b;
}

.booking-status-tag.paid {
  background-color: #f0fdf4;
  color: #22c55e;
}

.timeline-card-body {
  padding: 15px 20px;
}

.booking-detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.booking-detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-icon {
  color: #6c757d;
}

.detail-label {
  font-size: 13px;
  color: #6c757d;
}

.detail-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.booking-special-requests {
  margin-top: 10px;
  padding: 10px;
  background-color: #fffbeb;
  border-radius: 6px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  font-size: 13px;
  color: #92400e;
}

.booking-special-requests .el-icon {
  color: #f59e0b;
  flex-shrink: 0;
  margin-top: 2px;
}

.timeline-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px;
  background-color: #f8fafc;
  border-top: 1px solid #eaedf3;
}

.checkin-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-right: auto;
  font-size: 14px;
}

.checkin-status.completed {
  color: #28a745;
}

.timeline-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 入住率详情样式 */
.occupancy-container {
  padding-bottom: 30px;
}

.premium-occupancy-overview {
  padding: 0 30px 20px;
  margin-bottom: 20px;
}

.premium-overview-section {
  display: flex;
  gap: 20px;
}

.premium-overview-chart {
  flex: 0 0 250px;
}

.premium-chart-container {
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #eaedf3;
}

.donut-chart-container {
  position: relative;
  width: 200px;
  height: 200px;
}

.donut-chart {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #eaedf3;
  position: relative;
  overflow: hidden;
}

.donut-fill {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: conic-gradient(#28a745 0% var(--percentage, 0%), transparent var(--percentage, 0%) 100%);
  border-radius: 50%;
}

.donut-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background-color: white;
  width: 70%;
  height: 70%;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 0 5px #f8fafc;
}

.donut-percentage {
  font-size: 32px;
  font-weight: 700;
  color: #28a745;
}

.donut-label {
  font-size: 14px;
  color: #6c757d;
}

.premium-overview-stats {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.premium-stat-item {
  flex: 1 0 calc(33.333% - 15px);
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border: 1px solid #eaedf3;
}

.premium-stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #0d6efd;
  margin-bottom: 4px;
}

.premium-stat-label {
  font-size: 13px;
  color: #6c757d;
}

.occupancy-tabs {
  padding: 0 30px;
}

.premium-flex-container {
  display: flex;
  gap: 20px;
  padding: 0 30px;
  margin-bottom: 20px;
}

.premium-flex-item {
  flex: 1;
}

.trend-chart-container {
  padding-top: 15px;
}

.trend-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 180px;
  gap: 5px;
}

.trend-bar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.trend-bar-label {
  font-size: 12px;
  color: #6c757d;
  margin-top: 8px;
}

.trend-bar {
  width: 16px;
  height: 150px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.trend-bar-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: linear-gradient(to top, #28a745, #20c997);
  border-radius: 4px;
}

.trend-bar-value {
  font-size: 12px;
  font-weight: 500;
  color: #495057;
  margin-bottom: 5px;
}

.floor-occupancy-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.floor-occupancy-item {
  background-color: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.floor-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  width: 50px;
}

.floor-stats {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 120px;
}

.floor-occupied {
  font-size: 14px;
  color: #333;
}

.floor-rate {
  font-weight: 500;
  color: #0d6efd;
}

.floor-progress {
  flex: 1;
  height: 10px;
  background-color: #e9ecef;
  border-radius: 5px;
  overflow: hidden;
}

.floor-progress-bar {
  height: 100%;
  background: linear-gradient(to right, #0d6efd, #1a3e8f);
  border-radius: 5px;
}

.room-type-card {
  margin: 0 30px;
}

.room-type-occupancy {
  overflow-x: auto;
}

.premium-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.premium-table th {
  padding: 12px 15px;
  text-align: left;
  background-color: #f8fafc;
  color: #6c757d;
  font-weight: 500;
  font-size: 13px;
  border-bottom: 1px solid #eaedf3;
}

.premium-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eaedf3;
  font-size: 14px;
  color: #333;
}

.premium-table tr:last-child td {
  border-bottom: none;
}

.room-type-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.room-type-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background-color: #e9ecef;
}

.room-type-icon.standard {
  background-color: #8bc34a;
}

.room-type-icon.deluxe {
  background-color: #2196f3;
}

.room-type-icon.suite {
  background-color: #9c27b0;
}

.price-cell {
  color: #f56c6c;
  font-weight: 500;
}

.table-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-progress-bar {
  height: 6px;
  background-color: #28a745;
  border-radius: 3px;
}

/* 今日待办样式 */
.tasks-container {
  padding-bottom: 30px;
}

.tasks-tabs {
  margin-bottom: 0;
}

.tasks-summary {
  display: flex;
  gap: 15px;
  padding: 20px 30px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.task-summary-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex: 1;
}

.task-summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.task-summary-icon.pending {
  background-color: #fff7ed;
  color: #f59e0b;
}

.task-summary-icon.processing {
  background-color: #eff6ff;
  color: #0d6efd;
}

.task-summary-icon.completed {
  background-color: #f0fdf4;
  color: #22c55e;
}

.task-summary-icon.high-priority {
  background-color: #fef2f2;
  color: #ef4444;
}

.task-summary-info {
  display: flex;
  flex-direction: column;
}

.task-summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 2px;
}

.task-summary-label {
  font-size: 13px;
  color: #6c757d;
}

.premium-tasks-list {
  padding: 20px 30px;
}

.premium-task-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  margin-bottom: 15px;
  overflow: hidden;
  border-left: 5px solid #6c757d;
  transition: all 0.3s ease;
}

.premium-task-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.premium-task-card.pending {
  border-left-color: #f59e0b;
}

.premium-task-card.processing {
  border-left-color: #0d6efd;
}

.premium-task-card.completed {
  border-left-color: #22c55e;
  opacity: 0.8;
}

.task-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.task-type-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.task-type-badge {
  padding: 6px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.task-type-badge.vip {
  background: linear-gradient(135deg, #FFC107, #FF9800);
}

.task-type-badge.checkout {
  background: linear-gradient(135deg, #0d6efd, #0a58ca);
}

.task-type-badge.service {
  background: linear-gradient(135deg, #20c997, #198754);
}

.task-type-badge.complaint {
  background: linear-gradient(135deg, #dc3545, #c71f37);
}

.task-type-badge.cleaning {
  background: linear-gradient(135deg, #17a2b8, #138496);
}

.task-type-badge.maintenance {
  background: linear-gradient(135deg, #6c757d, #5a6268);
}

.task-type-badge.reservation {
  background: linear-gradient(135deg, #9c27b0, #7b1fa2);
}

.task-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #6c757d;
}

.completed-time {
  color: #22c55e;
  margin-left: 5px;
}

.task-priority {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
}

.task-priority.high {
  background-color: #fef2f2;
  color: #ef4444;
}

.task-priority.medium {
  background-color: #fff7ed;
  color: #f59e0b;
}

.task-priority.normal {
  background-color: #f8fafc;
  color: #6c757d;
}

.task-card-body {
  padding: 15px 20px;
}

.task-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.task-description {
  font-size: 14px;
  color: #6c757d;
  line-height: 1.5;
}

.task-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background-color: #f8fafc;
  border-top: 1px solid #eaedf3;
}

.task-assignee {
  display: flex;
  align-items: center;
  gap: 10px;
}

.assignee-label {
  font-size: 13px;
  color: #6c757d;
}

.assignee-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: #0d6efd;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.assignee-name {
  font-size: 14px;
  color: #333;
}

.task-actions {
  display: flex;
  gap: 10px;
}

.task-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 房态一览样式 */
.room-status-container {
  padding-bottom: 30px;
}

.room-status-summary {
  padding: 20px 30px;
  border-bottom: 1px solid #eaedf3;
  background-color: #f8fafc;
}

.room-status-legend {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6c757d;
}

.status-indicator {
  width: 18px;
  height: 18px;
  border-radius: 4px;
}

.status-indicator.available {
  background-color: #28a745;
}

.status-indicator.occupied {
  background-color: #dc3545;
}

.status-indicator.reserved {
  background-color: #fd7e14;
}

.status-indicator.cleaning {
  background-color: #17a2b8;
}

.status-indicator.maintenance {
  background-color: #6c757d;
}

.room-status-filters {
  display: flex;
  gap: 30px;
  margin-top: 15px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.filter-options {
  display: flex;
  gap: 8px;
}

.filter-option {
  padding: 5px 12px;
  font-size: 13px;
  color: #6c757d;
  background-color: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-option.active {
  background-color: #0d6efd;
  color: white;
}

.filter-option:hover:not(.active) {
  background-color: #e9ecef;
}

.premium-room-status-grid {
  padding: 20px 30px;
}

.floor-container {
  margin-bottom: 30px;
}

.floor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaedf3;
}

.floor-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.floor-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.floor-occupancy {
  display: flex;
  align-items: center;
  gap: 10px;
}

.occupancy-chart {
  width: 80px;
  height: 8px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.occupancy-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: var(--occupied-percent, 0%);
  background: linear-gradient(to right, #28a745, #20c997);
  border-radius: 4px;
}

.occupancy-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.rooms-count {
  font-size: 14px;
  color: #6c757d;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 12px;
}

.room-cell {
  height: 85px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #eaedf3;
  position: relative;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.room-cell:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.room-cell.available {
  border-left: 5px solid #28a745;
}

.room-cell.occupied {
  border-left: 5px solid #dc3545;
}

.room-cell.reserved {
  border-left: 5px solid #fd7e14;
}

.room-cell.cleaning {
  border-left: 5px solid #17a2b8;
}

.room-cell.maintenance {
  border-left: 5px solid #6c757d;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.room-number {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.room-type-indicator {
  font-size: 11px;
  padding: 2px 5px;
  background-color: #e9ecef;
  border-radius: 4px;
  color: #495057;
}

.room-badge {
  align-self: flex-end;
  margin-bottom: 5px;
}

.checkout-date {
  font-size: 12px;
  padding: 2px 6px;
  background-color: #fef2f2;
  border-radius: 4px;
  color: #ef4444;
}

.vip-marker {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  background-color: #ffc107;
  border-radius: 50%;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  border: 2px solid white;
}

.room-detail-preview {
  position: fixed;
  top: 50%;
  right: -350px;
  transform: translateY(-50%);
  width: 350px;
  max-height: 80vh;
  background-color: white;
  border-radius: 12px 0 0 12px;
  box-shadow: -5px 0 20px rgba(0, 0, 0, 0.15);
  overflow-y: auto;
  transition: right 0.4s ease;
  z-index: 9999;
  border: 1px solid #eaedf3;
  border-right: none;
}

.room-detail-preview.show {
  right: 0;
}

.detail-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.detail-preview-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.detail-preview-close {
  font-size: 24px;
  color: #6c757d;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.detail-preview-close:hover {
  background-color: #e9ecef;
  color: #333;
}

.detail-preview-content {
  padding: 20px;
}

.room-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.room-detail-number {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.room-detail-type {
  font-size: 14px;
  color: #6c757d;
}

.room-detail-status {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.room-detail-status.occupied {
  background-color: #fef2f2;
  color: #ef4444;
}

.room-detail-image {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.room-detail-image img {
  width: 100%;
  height: auto;
  display: block;
}

.room-detail-information {
  margin-bottom: 20px;
}

.detail-group {
  margin-bottom: 20px;
}

.detail-group-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eaedf3;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.detail-label {
  font-size: 13px;
  color: #6c757d;
}

.detail-value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  text-align: right;
}

.room-detail-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.detail-action-button.check-out {
  background-color: #0d6efd;
  color: white;
}

.detail-action-button.room-service {
  background-color: #f8fafc;
  color: #333;
  border: 1px solid #eaedf3;
}

.detail-action-button.room-maintenance {
  background-color: #f8fafc;
  color: #333;
  border: 1px solid #eaedf3;
}

.detail-action-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}
</style>