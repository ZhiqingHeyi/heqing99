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
  Clock, User, Download, Right, House, List, Monitor, Connection, Message, Star
} from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  bookingNo: '',
  customerName: '',
  phone: '',
  status: '',
  dateRange: []
})

// 统计数据
const pendingCount = ref(5)
const todayCheckinCount = ref(3)
const occupancyRate = ref(78)
const totalRooms = ref(100)
const occupiedRooms = ref(78)
const todayTaskCount = ref(8)

// 房态数据
const totalRooms = ref(120)
const occupiedRooms = ref(91)

// 今日待办
const todayTasks = [
  {
    id: 'T001',
    time: '09:30',
    category: 'vip',
    content: 'VIP客人王总预计今日下午14:00到店，需准备水果篮和欢迎饮品',
    status: 'pending',
    priority: 'high',
    room: '801'
  },
  {
    id: 'T002',
    time: '10:00',
    category: 'checkout',
    content: '605房需延迟退房至14:00，已确认并通知客房部',
    status: 'processing',
    priority: 'medium',
    room: '605'
  },
  {
    id: 'T003',
    time: '11:30',
    category: 'service',
    content: '303房客人需额外毛巾和洗漱用品，请尽快送达',
    status: 'completed',
    priority: 'low',
    room: '303'
  },
  {
    id: 'T004',
    time: '13:00',
    category: 'complaint',
    content: '508房客人投诉房间空调噪音大，需技术部门检查处理',
    status: 'pending',
    priority: 'high',
    room: '508'
  },
  {
    id: 'T005',
    time: '14:30',
    category: 'cleaning',
    content: '302房需进行特殊清洁，客人上午退房，下午有新客人入住',
    status: 'processing',
    priority: 'medium',
    room: '302'
  },
  {
    id: 'T006',
    time: '15:00',
    category: 'vip',
    content: '商务合作客户李总预订晚餐包间，需安排专人接待',
    status: 'pending',
    priority: 'medium',
    room: '餐厅'
  },
  {
    id: 'T007',
    time: '16:30',
    category: 'checkout',
    content: '今日共有12间房需退房，请提前做好安排',
    status: 'pending',
    priority: 'low',
    room: '多间'
  },
  {
    id: 'T008',
    time: '17:00',
    category: 'service',
    content: '明日早餐包房服务确认，共12位客人',
    status: 'completed',
    priority: 'low',
    room: '餐厅'
  }
]

// 模拟房型数据
const roomTypes = [
  { name: '标准单人间', totalCount: 30, availableCount: 8 },
  { name: '标准双人间', totalCount: 40, availableCount: 10 },
  { name: '豪华大床房', totalCount: 20, availableCount: 4 },
  { name: '行政套房', totalCount: 8, availableCount: 0 },
  { name: '总统套房', totalCount: 2, availableCount: 0 }
]

// 预订列表数据
const loading = ref(false)
const bookingList = ref([])
const total = ref(100)
const currentPage = ref(1)
const pageSize = ref(10)

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
  { number: '301', type: '标准双人间' },
  { number: '303', type: '标准双人间' },
  { number: '305', type: '标准双人间' },
  { number: '308', type: '豪华大床房' },
  { number: '310', type: '豪华大床房' },
  { number: '401', type: '行政套房' },
  { number: '501', type: '标准双人间' },
  { number: '505', type: '标准双人间' },
  { number: '508', type: '豪华大床房' },
  { number: '510', type: '豪华大床房' }
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

// 显示房态一览
const showRoomStatus = () => {
  // 设置临时变量，避免与已声明变量冲突
  const roomTotal = 100
  const occupiedCount = 78
  
  ElMessageBox.alert(
    `<div class="modern-dialog-content room-status-dialog">
      <div class="dialog-header">
        <div class="dialog-title">
          <i class="el-icon-house"></i>
          <h3>酒店房态一览</h3>
        </div>
        <div class="dialog-summary">
          <span>日期: ${new Date().toLocaleDateString()}</span>
          <span>|</span>
          <span>总房间数: ${roomTotal}</span>
        </div>
      </div>
      
      <div class="room-status-container">
        <!-- 筛选和图例工具栏 -->
        <div class="room-status-toolbar">
          <!-- 视图切换 -->
          <div class="view-tabs">
            <div class="view-tab active">楼层视图</div>
            <div class="view-tab">房型视图</div>
            <div class="view-tab">列表视图</div>
          </div>
          
          <!-- 图例 -->
          <div class="status-legend">
            <div class="legend-item"><span class="status-dot available"></span> 可预订</div>
            <div class="legend-item"><span class="status-dot occupied"></span> 已入住</div>
            <div class="legend-item"><span class="status-dot reserved"></span> 已预订</div>
            <div class="legend-item"><span class="status-dot cleaning"></span> 清洁中</div>
            <div class="legend-item"><span class="status-dot maintenance"></span> 维护中</div>
          </div>
          
          <!-- 日期选择器 -->
          <div class="date-selector">
            <button class="date-nav prev"><i class="el-icon-arrow-left"></i></button>
            <span class="current-date">${new Date().toLocaleDateString()}</span>
            <button class="date-nav next"><i class="el-icon-arrow-right"></i></button>
          </div>
        </div>
        
        <!-- 房态统计卡片 -->
        <div class="room-status-stats">
          <div class="status-card available">
            <div class="status-value">${Math.round(roomTotal * 0.3)}</div>
            <div class="status-label">可预订</div>
          </div>
          <div class="status-card occupied">
            <div class="status-value">${occupiedCount}</div>
            <div class="status-label">已入住</div>
          </div>
          <div class="status-card reserved">
            <div class="status-value">${Math.round(roomTotal * 0.1)}</div>
            <div class="status-label">已预订</div>
          </div>
          <div class="status-card cleaning">
            <div class="status-value">${Math.round(roomTotal * 0.05)}</div>
            <div class="status-label">清洁中</div>
          </div>
          <div class="status-card maintenance">
            <div class="status-value">${Math.round(roomTotal * 0.05)}</div>
            <div class="status-label">维护中</div>
          </div>
        </div>
        
        <!-- 楼层视图 -->
        <div class="floor-view">
          ${generateModernRoomStatusHTML()}
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="footer-btn">打印房态表</button>
        <button class="footer-btn">导出报表</button>
        <button class="footer-btn">关闭</button>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showConfirmButton: false,
      customClass: 'borderless-dialog room-status-dialog large-dialog'
    }
  )
}

// 生成现代化的房态HTML
const generateModernRoomStatusHTML = () => {
  // 模拟房间数据
  const floors = [2, 3, 4, 5, 6]
  let html = ''

  floors.forEach(floor => {
    html += `
      <div class="floor-section">
        <div class="floor-header">
          <div class="floor-number">0${floor}层</div>
          <div class="floor-summary">
            <span class="floor-stat">共20间</span>
            <span class="floor-stat available">空闲: ${Math.round(20 * 0.3)}</span>
            <span class="floor-stat occupied">已住: ${Math.round(20 * 0.55)}</span>
            <span class="floor-stat reserved">预订: ${Math.round(20 * 0.1)}</span>
            <span class="floor-stat">其他: ${Math.round(20 * 0.05)}</span>
          </div>
        </div>
        <div class="room-grid">
          ${generateRoomCells(floor, 20)}
        </div>
      </div>
    `
  })
  
  return html
}

// 生成房间单元格
const generateRoomCells = (floor, count) => {
  let cells = ''
  for (let i = 1; i <= count; i++) {
    const roomNumber = `${floor}${i.toString().padStart(2, '0')}`
    const statusClass = getRandomRoomStatus()
    const roomTypeShort = getRoomTypeShort(i)
    const guestName = statusClass === 'occupied' ? getRandomGuestName() : ''
    const checkOut = statusClass === 'occupied' ? getRandomCheckoutDate() : ''
    
    cells += `
      <div class="room-cell ${statusClass}">
        <div class="room-number">${roomNumber}</div>
        <div class="room-info">
          <div class="room-type">${getFullRoomType(roomTypeShort)}</div>
          ${statusClass === 'occupied' ? `
            <div class="guest-info">
              <div class="guest-name">${guestName}</div>
              <div class="checkout-date">退房: ${checkOut}</div>
            </div>
          ` : ''}
          ${statusClass === 'reserved' ? `
            <div class="reservation-info">
              <div class="checkin-time">预抵达: ${getRandomTime()}</div>
            </div>
          ` : ''}
          ${statusClass === 'cleaning' ? `
            <div class="cleaning-info">
              <div class="cleaning-status">预计完成: ${getRandomTime()}</div>
            </div>
          ` : ''}
        </div>
      </div>
    `
  }
  return cells
}

// 获取随机房间状态（仅供演示）
const getRandomRoomStatus = () => {
  const statuses = ['available', 'occupied', 'reserved', 'cleaning', 'maintenance']
  const weights = [0.25, 0.55, 0.1, 0.05, 0.05] // 权重分布
  
  const rand = Math.random()
  let sum = 0
  for (let i = 0; i < weights.length; i++) {
    sum += weights[i]
    if (rand < sum) return statuses[i]
  }
  return statuses[0]
}

// 获取房型简写（仅供演示）
const getRoomTypeShort = (position) => {
  if (position <= 4) return 'STA' // 标准单人间
  if (position <= 8) return 'STW' // 标准双人间
  if (position <= 10) return 'DEL' // 豪华大床房
  if (position <= 11) return 'EXE' // 行政套房
  return 'PRE' // 总统套房
}

// 获取完整房型名称
const getFullRoomType = (shortCode) => {
  const types = {
    'STA': '标准单人间',
    'STW': '标准双人间',
    'DEL': '豪华大床房',
    'EXE': '行政套房',
    'PRE': '总统套房'
  }
  return types[shortCode] || shortCode
}

// 获取随机客人姓名
const getRandomGuestName = () => {
  const surnames = ['张', '王', '李', '赵', '刘', '陈', '杨', '黄', '周', '吴']
  const names = ['明', '华', '强', '伟', '芳', '娜', '秀英', '军', '杰', '磊', '敏']
  
  return surnames[Math.floor(Math.random() * surnames.length)] + 
         names[Math.floor(Math.random() * names.length)]
}

// 获取随机退房日期
const getRandomCheckoutDate = () => {
  const today = new Date()
  const daysAhead = Math.floor(Math.random() * 3) + 1
  const checkoutDate = new Date(today)
  checkoutDate.setDate(today.getDate() + daysAhead)
  
  return `${checkoutDate.getMonth() + 1}/${checkoutDate.getDate()}`
}

// 获取随机时间
const getRandomTime = () => {
  const hours = Math.floor(Math.random() * 10) + 8 // 8点到18点
  const minutes = ['00', '15', '30', '45'][Math.floor(Math.random() * 4)]
  return `${hours}:${minutes}`
}

// 初始化
fetchBookingList()

// 显示今日待办
const showTodayTasks = () => {
  ElMessageBox.alert(
    `<div class="modern-dialog-content task-dialog">
      <div class="dialog-header">
        <div class="dialog-title">
          <i class="el-icon-list"></i>
          <h3>今日待办事项</h3>
        </div>
        <div class="dialog-summary">
          <span>今日日期: ${new Date().toLocaleDateString()}</span>
          <span>|</span>
          <span>共 ${todayTasks.length} 个待办事项</span>
        </div>
      </div>
      
      <div class="tasks-container">
        <div class="task-status-tabs">
          <div class="status-tab active">全部</div>
          <div class="status-tab">待处理</div>
          <div class="status-tab">进行中</div>
          <div class="status-tab">已完成</div>
        </div>
        
        <div class="tasks-grid">
          ${todayTasks.map(task => `
            <div class="task-card ${task.status}">
              <div class="task-priority ${task.priority}"></div>
              <div class="task-header">
                <div class="task-time">${task.time}</div>
                <div class="task-category ${task.category}">${getCategoryName(task.category)}</div>
              </div>
              <div class="task-content">${task.content}</div>
              <div class="task-footer">
                <div class="task-status-badge ${task.status}">${getStatusName(task.status)}</div>
                <div class="task-room">${task.room || '未分配'}</div>
                <div class="task-actions">
                  ${task.status === 'pending' ? '<button class="action-btn start-btn">开始处理</button>' : ''}
                  ${task.status === 'processing' ? '<button class="action-btn complete-btn">完成</button>' : ''}
                  <button class="action-btn view-btn">查看</button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="footer-btn primary-btn">添加待办</button>
        <button class="footer-btn">关闭</button>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showConfirmButton: false,
      customClass: 'borderless-dialog task-dialog large-dialog'
    }
  )
}

// 显示入住率详情
const showOccupancyDetails = () => {
  // 设置临时变量，避免与已声明变量冲突
  const roomTotal = 100
  const occupiedCount = 78
  const rate = 78

  ElMessageBox.alert(
    `<div class="modern-dialog-content occupancy-dialog">
      <div class="dialog-header">
        <div class="dialog-title">
          <i class="el-icon-data-analysis"></i>
          <h3>酒店入住率统计</h3>
        </div>
        <div class="dialog-summary">
          <span>统计日期: ${new Date().toLocaleDateString()}</span>
        </div>
      </div>
      
      <div class="occupancy-panels">
        <!-- 入住率概览卡片 -->
        <div class="occupancy-overview">
          <div class="overview-card total-card">
            <div class="overview-title">总房间数</div>
            <div class="overview-value">${roomTotal}</div>
          </div>
          <div class="overview-card occupied-card">
            <div class="overview-title">已入住</div>
            <div class="overview-value">${occupiedCount}</div>
          </div>
          <div class="overview-card available-card">
            <div class="overview-title">空闲房间</div>
            <div class="overview-value">${roomTotal - occupiedCount}</div>
          </div>
          <div class="overview-card rate-card">
            <div class="overview-title">当前入住率</div>
            <div class="overview-value highlight">${rate}%</div>
            <div class="rate-visual">
              <div class="rate-bar" style="width: ${rate}%"></div>
            </div>
          </div>
        </div>
        
        <!-- 各房型入住详情 -->
        <div class="room-type-details">
          <h4>各房型入住情况</h4>
          <div class="room-types-grid">
            ${roomTypes.map(type => `
              <div class="room-type-card">
                <div class="room-type-header">
                  <span class="room-type-name">${type.name}</span>
                  <span class="room-type-rate">${Math.round((type.totalCount - type.availableCount) / type.totalCount * 100)}%</span>
                </div>
                <div class="room-type-body">
                  <div class="room-type-progress">
                    <div class="progress-bar" style="width: ${Math.round((type.totalCount - type.availableCount) / type.totalCount * 100)}%"></div>
                  </div>
                  <div class="room-type-stats">
                    <div>总数: ${type.totalCount}</div>
                    <div>可用: ${type.availableCount}</div>
                    <div>已住: ${type.totalCount - type.availableCount}</div>
                  </div>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
        
        <!-- 入住率趋势图 -->
        <div class="occupancy-trend">
          <h4>近7日入住率趋势</h4>
          <div class="trend-chart">
            <div class="chart-bars">
              <div class="chart-bar" style="height: 65%;">
                <div class="bar-label">65%</div>
                <div class="date-label">3/28</div>
              </div>
              <div class="chart-bar" style="height: 70%;">
                <div class="bar-label">70%</div>
                <div class="date-label">3/29</div>
              </div>
              <div class="chart-bar" style="height: 60%;">
                <div class="bar-label">60%</div>
                <div class="date-label">3/30</div>
              </div>
              <div class="chart-bar" style="height: 68%;">
                <div class="bar-label">68%</div>
                <div class="date-label">3/31</div>
              </div>
              <div class="chart-bar" style="height: 75%;">
                <div class="bar-label">75%</div>
                <div class="date-label">4/1</div>
              </div>
              <div class="chart-bar" style="height: 78%;">
                <div class="bar-label">78%</div>
                <div class="date-label">4/2</div>
              </div>
              <div class="chart-bar active" style="height: ${rate}%;">
                <div class="bar-label">${rate}%</div>
                <div class="date-label">今日</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="footer-btn">导出报表</button>
        <button class="footer-btn">关闭</button>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      showConfirmButton: false,
      customClass: 'borderless-dialog occupancy-dialog large-dialog'
    }
  )
}
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

.room-info {
  font-size: 13px;
}

.room-type {
  margin-bottom: 5px;
  color: #495057;
}

.guest-info, .reservation-info, .cleaning-info {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #dee2e6;
  font-size: 12px;
  color: #6c757d;
}

.guest-name {
  font-weight: 500;
  color: #495057;
  margin-bottom: 4px;
}

/* 现代化弹窗样式 */
:deep(.borderless-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

:deep(.borderless-dialog .el-message-box__header) {
  display: none;
}

:deep(.borderless-dialog .el-message-box__content) {
  padding: 0;
}

:deep(.borderless-dialog .el-message-box__message) {
  padding: 0;
}

:deep(.borderless-dialog .el-message-box__message p) {
  margin: 0;
}

.modern-dialog-content {
  position: relative;
  min-width: 700px;
  max-width: 90vw;
}

.large-dialog .modern-dialog-content {
  min-width: 900px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #1a3e8f, #0d6efd);
  color: white;
}

.dialog-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dialog-title h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.dialog-summary {
  font-size: 14px;
  display: flex;
  gap: 8px;
  align-items: center;
  opacity: 0.9;
}

.data-table-container {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-bottom: 2px solid #e9ecef;
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  font-size: 14px;
}

.data-table tr:hover {
  background-color: #f8f9fa;
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.customer-name {
  font-weight: 500;
  color: #212529;
}

.customer-phone {
  color: #6c757d;
  font-size: 13px;
}

.booking-id {
  font-family: monospace;
  color: #0d6efd;
  font-weight: 500;
}

.action-cell {
  display: flex;
  gap: 8px;
}

.action-btn {
  border: none;
  background: none;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  color: #fff;
}

.confirm-btn {
  background-color: #28a745;
}

.confirm-btn:hover {
  background-color: #218838;
}

.edit-btn {
  background-color: #0d6efd;
}

.edit-btn:hover {
  background-color: #0a58ca;
}

.checkin-btn {
  background-color: #0d6efd;
}

.checkin-btn:hover {
  background-color: #0a58ca;
}

.view-btn {
  background-color: #6c757d;
}

.view-btn:hover {
  background-color: #5a6268;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.status-badge.confirmed {
  background-color: #0d6efd;
}

.dialog-footer {
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.footer-btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #ced4da;
  background-color: white;
  color: #495057;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.footer-btn:hover {
  background-color: #f1f3f5;
}

.footer-btn.primary-btn {
  background-color: #0d6efd;
  border-color: #0d6efd;
  color: white;
}

.footer-btn.primary-btn:hover {
  background-color: #0a58ca;
  border-color: #0a58ca;
}

/* 入住率详情样式 */
.occupancy-panels {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.occupancy-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.overview-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
  text-align: center;
}

.overview-title {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #212529;
}

.overview-value.highlight {
  color: #0d6efd;
}

.rate-visual {
  margin-top: 10px;
  background-color: #e9ecef;
  height: 8px;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.rate-bar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(to right, #0d6efd, #1a3e8f);
  border-radius: 4px;
}

.room-type-details {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  background-color: white;
}

.room-type-details h4 {
  margin-top: 0;
  margin-bottom: 16px;
  font-weight: 600;
  color: #343a40;
  font-size: 16px;
}

.room-types-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.room-type-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

.room-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.room-type-name {
  font-weight: 500;
  color: #343a40;
}

.room-type-rate {
  font-weight: 600;
  color: #0d6efd;
}

.room-type-body {
  padding: 16px;
}

.room-type-progress {
  height: 6px;
  background-color: #e9ecef;
  border-radius: 3px;
  margin-bottom: 12px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(to right, #0d6efd, #1a3e8f);
  border-radius: 3px;
}

.room-type-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  font-size: 13px;
  color: #6c757d;
}

.occupancy-trend {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  background-color: white;
}

.occupancy-trend h4 {
  margin-top: 0;
  margin-bottom: 16px;
  font-weight: 600;
  color: #343a40;
  font-size: 16px;
}

.trend-chart {
  height: 200px;
  position: relative;
  padding-top: 20px;
}

.chart-bars {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 100%;
  padding: 0 10px;
}

.chart-bar {
  width: 40px;
  background: linear-gradient(to top, rgba(13, 110, 253, 0.2), rgba(13, 110, 253, 0.6));
  border-radius: 4px 4px 0 0;
  position: relative;
  margin: 0 10px;
}

.chart-bar.active {
  background: linear-gradient(to top, rgba(13, 110, 253, 0.4), rgba(13, 110, 253, 0.9));
}

.bar-label {
  position: absolute;
  top: -20px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  font-weight: 500;
  color: #343a40;
}

.date-label {
  position: absolute;
  bottom: -25px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  color: #6c757d;
}

/* 待办任务样式 */
.tasks-container {
  padding: 20px;
}

.task-status-tabs {
  display: flex;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 20px;
}

.status-tab {
  padding: 8px 16px;
  margin-right: 10px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
  color: #6c757d;
}

.status-tab.active {
  color: #0d6efd;
  font-weight: 500;
}

.status-tab.active:after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #0d6efd;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.task-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  background-color: white;
}

.task-priority {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.task-priority.high {
  background-color: #dc3545;
}

.task-priority.medium {
  background-color: #fd7e14;
}

.task-priority.low {
  background-color: #20c997;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.task-time {
  font-size: 13px;
  font-weight: 500;
  color: #495057;
}

.task-category {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  color: white;
}

.task-category.vip {
  background-color: #ffc107;
  color: #212529;
}

.task-category.checkout {
  background-color: #0dcaf0;
}

.task-category.service {
  background-color: #198754;
}

.task-category.complaint {
  background-color: #dc3545;
}

.task-category.cleaning {
  background-color: #6f42c1;
}

.task-content {
  padding: 12px 16px;
  color: #212529;
  font-size: 14px;
  line-height: 1.5;
  border-bottom: 1px solid #e9ecef;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-size: 13px;
}

.task-status-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.task-status-badge.pending {
  background-color: #ffc107;
  color: #212529;
}

.task-status-badge.processing {
  background-color: #0d6efd;
  color: white;
}

.task-status-badge.completed {
  background-color: #198754;
  color: white;
}

.task-room {
  color: #6c757d;
}

.task-actions {
  display: flex;
  gap: 8px;
}

.start-btn {
  background-color: #ffc107;
  color: #212529;
}

.start-btn:hover {
  background-color: #e0a800;
}

.complete-btn {
  background-color: #198754;
}

.complete-btn:hover {
  background-color: #157347;
}

/* 房态一览样式 */
.room-status-container {
  padding: 20px;
}

.room-status-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-tabs {
  display: flex;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  overflow: hidden;
}

.view-tab {
  padding: 8px 16px;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.view-tab:not(:last-child) {
  border-right: 1px solid #e9ecef;
}

.view-tab:hover {
  background-color: #f8f9fa;
}

.view-tab.active {
  background-color: #0d6efd;
  color: white;
}

.status-legend {
  display: flex;
  gap: 16px;
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

.date-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-nav {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: white;
  border: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.date-nav:hover {
  background-color: #f8f9fa;
}

.current-date {
  font-size: 14px;
  font-weight: 500;
  color: #343a40;
}

.room-status-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.status-card {
  background-color: white;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
}

.status-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.status-label {
  font-size: 14px;
  color: #6c757d;
}

.status-card.available .status-value {
  color: #28a745;
}

.status-card.occupied .status-value {
  color: #dc3545;
}

.status-card.reserved .status-value {
  color: #ffc107;
}

.status-card.cleaning .status-value {
  color: #17a2b8;
}

.status-card.maintenance .status-value {
  color: #6c757d;
}

.floor-view {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  background-color: white;
}

.floor-section:not(:last-child) {
  border-bottom: 1px solid #e9ecef;
}

.floor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
}

.floor-number {
  font-weight: 600;
  color: #343a40;
}

.floor-summary {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6c757d;
}

.floor-stat.available {
  color: #28a745;
}

.floor-stat.occupied {
  color: #dc3545;
}

.floor-stat.reserved {
  color: #ffc107;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  padding: 16px;
}

.room-cell {
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  min-height: 80px;
}

.room-cell:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.room-cell.available {
  background-color: rgba(40, 167, 69, 0.1);
  border: 1px solid rgba(40, 167, 69, 0.2);
}

.room-cell.occupied {
  background-color: rgba(220, 53, 69, 0.1);
  border: 1px solid rgba(220, 53, 69, 0.2);
}

.room-cell.reserved {
  background-color: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.2);
}

.room-cell.cleaning {
  background-color: rgba(23, 162, 184, 0.1);
  border: 1px solid rgba(23, 162, 184, 0.2);
}

.room-cell.maintenance {
  background-color: rgba(108, 117, 125, 0.1);
  border: 1px solid rgba(108, 117, 125, 0.2);
}

.room-number {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
  color: #343a40;
}

.room-info {
  font-size: 13px;
}

.room-type {
  margin-bottom: 5px;
  color: #495057;
}

.guest-info, .reservation-info, .cleaning-info {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #dee2e6;
  font-size: 12px;
  color: #6c757d;
}

.guest-name {
  font-weight: 500;
  color: #495057;
  margin-bottom: 4px;
}
</style>