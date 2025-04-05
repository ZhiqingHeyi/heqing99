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
  { 
    id: 1, 
    type: 'vip', 
    content: 'VIP客户接待 - 李先生 11:30到店', 
    status: 'pending',
    priority: 'high',
    time: '11:30',
    location: '前台',
    assignee: '张秘书',
    details: '金卡会员，需准备水果篮和欢迎卡片',
    deadline: '11:15'
  },
  { 
    id: 2, 
    type: 'checkout', 
    content: '302房延迟退房申请处理', 
    status: 'pending',
    priority: 'medium',
    time: '13:00',
    location: '302房',
    assignee: '王经理',
    details: '客人申请延迟至下午14:00退房，需核实房间状态',
    deadline: '12:00'
  },
  { 
    id: 3, 
    type: 'service', 
    content: '401房送加床服务', 
    status: 'completed',
    priority: 'medium',
    time: '09:30',
    location: '401房',
    assignee: '李服务员',
    details: '客人增加1位入住，需加1张床',
    deadline: '09:15',
    completedTime: '09:25',
    completedBy: '李服务员'
  },
  { 
    id: 4, 
    type: 'complaint', 
    content: '处理305房噪音投诉', 
    status: 'processing',
    priority: 'high',
    time: '10:15',
    location: '305房',
    assignee: '陈经理',
    details: '客人投诉隔壁房间噪音太大，影响休息',
    deadline: '10:30',
    startTime: '10:20'
  },
  { 
    id: 5, 
    type: 'cleaning', 
    content: '安排508房紧急清洁', 
    status: 'completed',
    priority: 'high',
    time: '08:45',
    location: '508房',
    assignee: '清洁部门',
    details: '贵宾即将入住，需提前准备房间',
    deadline: '10:00',
    completedTime: '09:50',
    completedBy: '赵清洁员'
  },
  { 
    id: 6, 
    type: 'maintenance', 
    content: '检修603房空调故障', 
    status: 'pending',
    priority: 'medium',
    time: '14:00',
    location: '603房',
    assignee: '维修部门',
    details: '客人报修空调不制冷，需尽快处理',
    deadline: '15:30'
  },
  { 
    id: 7, 
    type: 'meal', 
    content: '准备15人商务会议午餐', 
    status: 'completed',
    priority: 'high',
    time: '11:00',
    location: '钻石会议室',
    assignee: '餐饮部',
    details: '准备15人商务自助午餐，含2位素食',
    deadline: '12:00',
    completedTime: '11:45',
    completedBy: '餐饮部'
  }
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
  // 更详细的楼层和房间数据
  const floors = [2, 3, 4, 5, 6, 7, 8];
  
  // 生成更丰富的房态数据
  const roomStatus = {};
  
  floors.forEach(floor => {
    roomStatus[floor] = {};
    
    // 每层楼的每个房间
    for (let i = 1; i <= 16; i++) {
      const roomNumber = `${floor}${i.toString().padStart(2, '0')}`;
      
      // 根据规则确定房型
      let roomType = '';
      if (i <= 4) roomType = 'standard'; // 标准单人间
      else if (i <= 10) roomType = 'double'; // 标准双人间
      else if (i <= 14) roomType = 'deluxe'; // 豪华大床房
      else if (i <= 15) roomType = 'executive'; // 行政套房
      else roomType = 'presidential'; // 总统套房
      
      // 确定状态 (模拟数据)
      let status = '';
      const rand = Math.random();
      
      if (rand < 0.65) status = 'occupied';
      else if (rand < 0.75) status = 'reserved';
      else if (rand < 0.85) status = 'available';
      else if (rand < 0.95) status = 'cleaning';
      else status = 'maintenance';
      
      // 添加其他房间信息
      roomStatus[floor][roomNumber] = {
        number: roomNumber,
        type: roomType,
        status: status,
        guestName: status === 'occupied' ? generateRandomName() : '',
        checkIn: status === 'occupied' ? generateRandomDate(-3, 0) : '',
        checkOut: status === 'occupied' ? generateRandomDate(1, 5) : '',
        price: getRoomPrice(roomType),
        specialRequests: status === 'occupied' && Math.random() > 0.7 ? '需要加床' : '',
        vip: status === 'occupied' && Math.random() > 0.85,
        clean: status !== 'cleaning' && Math.random() > 0.3,
        maintenance: status === 'maintenance' ? getRandomMaintenance() : ''
      };
    }
  });
  
  // 计算统计数据
  const totalRooms = Object.values(roomStatus).reduce((sum, floor) => sum + Object.keys(floor).length, 0);
  const occupiedRooms = Object.values(roomStatus).reduce((sum, floor) => {
    return sum + Object.values(floor).filter(room => room.status === 'occupied').length;
  }, 0);
  const reservedRooms = Object.values(roomStatus).reduce((sum, floor) => {
    return sum + Object.values(floor).filter(room => room.status === 'reserved').length;
  }, 0);
  const availableRooms = Object.values(roomStatus).reduce((sum, floor) => {
    return sum + Object.values(floor).filter(room => room.status === 'available').length;
  }, 0);
  const cleaningRooms = Object.values(roomStatus).reduce((sum, floor) => {
    return sum + Object.values(floor).filter(room => room.status === 'cleaning').length;
  }, 0);
  const maintenanceRooms = Object.values(roomStatus).reduce((sum, floor) => {
    return sum + Object.values(floor).filter(room => room.status === 'maintenance').length;
  }, 0);
  
  const occupancyRate = Math.round(occupiedRooms / totalRooms * 100);
  
  ElMessageBox.alert(
    `<div class="detail-dialog-content premium-dialog room-status-dialog">
      <div class="premium-dialog-header">
        <div class="premium-header-icon room-status-icon">
          <i class="el-icon-house"></i>
        </div>
        <div class="premium-header-title">
          <h3>酒店房态一览</h3>
          <div class="premium-header-subtitle">实时房态 · 更新于 ${new Date().toLocaleTimeString()}</div>
        </div>
      </div>
      
      <div class="room-status-overview">
        <div class="overview-stats">
          <div class="overview-stat">
            <div class="stat-value">${totalRooms}</div>
            <div class="stat-label">总房间数</div>
          </div>
          <div class="overview-stat">
            <div class="stat-value">${occupiedRooms}</div>
            <div class="stat-label">已入住</div>
          </div>
          <div class="overview-stat">
            <div class="stat-value">${reservedRooms}</div>
            <div class="stat-label">已预订</div>
          </div>
          <div class="overview-stat">
            <div class="stat-value">${availableRooms}</div>
            <div class="stat-label">可预订</div>
          </div>
          <div class="overview-stat">
            <div class="stat-value">${cleaningRooms}</div>
            <div class="stat-label">清洁中</div>
          </div>
          <div class="overview-stat">
            <div class="stat-value">${maintenanceRooms}</div>
            <div class="stat-label">维护中</div>
          </div>
        </div>
        
        <div class="overview-chart">
          <div class="chart-title">入住率: ${occupancyRate}%</div>
          <div class="occupancy-bar">
            <div class="occupancy-progress" style="width: ${occupancyRate}%"></div>
          </div>
          <div class="room-status-legend">
            <div class="legend-item">
              <span class="status-dot available"></span>
              <span>可预订</span>
            </div>
            <div class="legend-item">
              <span class="status-dot occupied"></span>
              <span>已入住</span>
            </div>
            <div class="legend-item">
              <span class="status-dot reserved"></span>
              <span>已预订</span>
            </div>
            <div class="legend-item">
              <span class="status-dot cleaning"></span>
              <span>清洁中</span>
            </div>
            <div class="legend-item">
              <span class="status-dot maintenance"></span>
              <span>维护中</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="room-status-controls">
        <div class="status-view-tabs">
          <div class="view-tab active">按楼层查看</div>
          <div class="view-tab">按房型查看</div>
          <div class="view-tab">按状态查看</div>
          <div class="view-tab">列表视图</div>
        </div>
        
        <div class="status-actions">
          <select class="status-filter">
            <option value="all">所有楼层</option>
            ${floors.map(floor => `<option value="${floor}">第${floor}层</option>`).join('')}
          </select>
          <select class="status-filter">
            <option value="all">所有房型</option>
            <option value="standard">标准单人间</option>
            <option value="double">标准双人间</option>
            <option value="deluxe">豪华大床房</option>
            <option value="executive">行政套房</option>
            <option value="presidential">总统套房</option>
          </select>
          <select class="status-filter">
            <option value="all">所有状态</option>
            <option value="available">可预订</option>
            <option value="occupied">已入住</option>
            <option value="reserved">已预订</option>
            <option value="cleaning">清洁中</option>
            <option value="maintenance">维护中</option>
          </select>
          <button class="status-action-btn print-btn"><i class="el-icon-printer"></i></button>
          <button class="status-action-btn refresh-btn"><i class="el-icon-refresh"></i></button>
        </div>
      </div>
      
      <div class="room-status-grid-container">
        ${floors.map(floor => `
          <div class="floor-section">
            <div class="floor-header">
              <div class="floor-title">第${floor}层</div>
              <div class="floor-stats">
                <div class="floor-stat">
                  <span>总房间: ${Object.keys(roomStatus[floor]).length}</span>
                </div>
                <div class="floor-stat">
                  <span>已入住: ${Object.values(roomStatus[floor]).filter(r => r.status === 'occupied').length}</span>
                </div>
                <div class="floor-stat">
                  <span>可用: ${Object.values(roomStatus[floor]).filter(r => r.status === 'available').length}</span>
                </div>
              </div>
            </div>
            
            <div class="floor-layout">
              <div class="floor-corridor">
                <div class="elevator">
                  <i class="el-icon-s-operation"></i>
                  <span>电梯</span>
                </div>
                <div class="corridor-line"></div>
                <div class="service-room">
                  <i class="el-icon-s-cooperation"></i>
                  <span>服务间</span>
                </div>
              </div>
              
              <div class="floor-rooms">
                ${Object.values(roomStatus[floor]).map(room => `
                  <div class="room-cell ${room.status}" data-room="${room.number}">
                    <div class="room-cell-header">
                      <div class="room-number">${room.number}</div>
                      <div class="room-indicators">
                        ${room.vip ? '<span class="room-vip-indicator">VIP</span>' : ''}
                        ${!room.clean && room.status === 'occupied' ? '<span class="room-dirty-indicator"><i class="el-icon-warning-outline"></i></span>' : ''}
                      </div>
                    </div>
                    
                    <div class="room-type-indicator">
                      ${room.type === 'standard' ? 'STA' : 
                        room.type === 'double' ? 'DBL' : 
                        room.type === 'deluxe' ? 'DLX' : 
                        room.type === 'executive' ? 'EXE' : 'PRE'}
                    </div>
                    
                    ${room.status === 'occupied' ? `
                      <div class="room-guest">${room.guestName}</div>
                      <div class="room-date">${room.checkOut.split('-')[1]}/${room.checkOut.split('-')[2]}</div>
                    ` : room.status === 'reserved' ? `
                      <div class="room-reserved">预定</div>
                    ` : room.status === 'maintenance' ? `
                      <div class="room-maintenance"><i class="el-icon-warning"></i></div>
                    ` : room.status === 'cleaning' ? `
                      <div class="room-cleaning"><i class="el-icon-brush"></i></div>
                    ` : `
                      <div class="room-available">空闲</div>
                    `}
                  </div>
                `).join('')}
              </div>
            </div>
          </div>
        `).join('')}
      </div>
      
      <div class="room-detail-panel">
        <div class="detail-panel-header">
          <h4>房间详情</h4>
          <span class="detail-close">&times;</span>
        </div>
        <div class="detail-panel-content">
          <div class="detail-placeholder">
            <i class="el-icon-house"></i>
            <p>点击房间查看详情</p>
          </div>
        </div>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      showClose: true,
      customClass: 'premium-message-box room-status-message-box full-width-dialog large-dialog'
    }
  )
  
  // 点击房间显示详情的事件（不是实际代码，只用于演示）
  // 实际上这种动态事件绑定应该在mounted或updated钩子中进行
  setTimeout(() => {
    const roomCells = document.querySelectorAll('.room-cell');
    const detailPanel = document.querySelector('.room-detail-panel');
    const detailContent = document.querySelector('.detail-panel-content');
    const detailClose = document.querySelector('.detail-close');
    
    roomCells.forEach(cell => {
      cell.addEventListener('click', () => {
        const roomNumber = cell.getAttribute('data-room');
        const floorNumber = roomNumber.charAt(0);
        const room = roomStatus[floorNumber][roomNumber];
        
        detailPanel.classList.add('active');
        
        detailContent.innerHTML = `
          <div class="room-detail-card ${room.status}">
            <div class="detail-header">
              <div class="detail-room-number">${room.number}</div>
              <div class="detail-room-type">
                ${room.type === 'standard' ? '标准单人间' : 
                  room.type === 'double' ? '标准双人间' : 
                  room.type === 'deluxe' ? '豪华大床房' : 
                  room.type === 'executive' ? '行政套房' : '总统套房'}
              </div>
              <div class="detail-status">${
                room.status === 'occupied' ? '已入住' : 
                room.status === 'reserved' ? '已预订' : 
                room.status === 'available' ? '可入住' : 
                room.status === 'cleaning' ? '清洁中' : '维护中'
              }</div>
            </div>
            
            ${room.status === 'occupied' ? `
              <div class="detail-section guest-section">
                <h5>入住信息</h5>
                <div class="detail-info">
                  <div class="info-row">
                    <div class="info-label">客人姓名:</div>
                    <div class="info-value">${room.guestName} ${room.vip ? '<span class="vip-tag">VIP</span>' : ''}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">入住日期:</div>
                    <div class="info-value">${room.checkIn}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">退房日期:</div>
                    <div class="info-value">${room.checkOut}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">房价:</div>
                    <div class="info-value">¥${room.price}/晚</div>
                  </div>
                  ${room.specialRequests ? `
                    <div class="info-row">
                      <div class="info-label">特殊要求:</div>
                      <div class="info-value">${room.specialRequests}</div>
                    </div>
                  ` : ''}
                </div>
              </div>
              <div class="detail-section status-section">
                <h5>房间状态</h5>
                <div class="detail-info">
                  <div class="info-row">
                    <div class="info-label">清洁状态:</div>
                    <div class="info-value">${room.clean ? '已清洁' : '<span class="warning">未清洁</span>'}</div>
                  </div>
                </div>
              </div>
            ` : room.status === 'maintenance' ? `
              <div class="detail-section maintenance-section">
                <h5>维护信息</h5>
                <div class="detail-info">
                  <div class="info-row">
                    <div class="info-label">问题描述:</div>
                    <div class="info-value">${room.maintenance}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">开始时间:</div>
                    <div class="info-value">${generateRandomTime()}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">预计完成:</div>
                    <div class="info-value">${generateRandomTime(3)}</div>
                  </div>
                </div>
              </div>
            ` : room.status === 'cleaning' ? `
              <div class="detail-section cleaning-section">
                <h5>清洁信息</h5>
                <div class="detail-info">
                  <div class="info-row">
                    <div class="info-label">开始时间:</div>
                    <div class="info-value">${generateRandomTime(-1)}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">预计完成:</div>
                    <div class="info-value">${generateRandomTime(1)}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">负责人:</div>
                    <div class="info-value">刘清洁</div>
                  </div>
                </div>
              </div>
            ` : `
              <div class="detail-section available-section">
                <h5>房间信息</h5>
                <div class="detail-info">
                  <div class="info-row">
                    <div class="info-label">房型:</div>
                    <div class="info-value">${
                      room.type === 'standard' ? '标准单人间' : 
                      room.type === 'double' ? '标准双人间' : 
                      room.type === 'deluxe' ? '豪华大床房' : 
                      room.type === 'executive' ? '行政套房' : '总统套房'
                    }</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">价格:</div>
                    <div class="info-value">¥${room.price}/晚</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">最大入住:</div>
                    <div class="info-value">${
                      room.type === 'standard' ? '1人' : 
                      room.type === 'double' ? '2人' : 
                      room.type === 'deluxe' ? '2人' : 
                      room.type === 'executive' ? '3人' : '4人'
                    }</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">床型:</div>
                    <div class="info-value">${
                      room.type === 'standard' ? '单床' : 
                      room.type === 'double' ? '双床' : 
                      room.type === 'deluxe' ? '大床' : 
                      room.type === 'executive' ? '特大床' : '豪华特大床'
                    }</div>
                  </div>
                </div>
              </div>
            `}
            
            <div class="detail-actions">
              ${room.status === 'occupied' ? `
                <button class="detail-action-btn checkout-btn">办理退房</button>
                <button class="detail-action-btn service-btn">客房服务</button>
                <button class="detail-action-btn extend-btn">延长入住</button>
              ` : room.status === 'available' ? `
                <button class="detail-action-btn checkin-btn">办理入住</button>
                <button class="detail-action-btn reserve-btn">预订房间</button>
              ` : room.status === 'reserved' ? `
                <button class="detail-action-btn checkin-btn">办理入住</button>
                <button class="detail-action-btn cancel-btn">取消预订</button>
              ` : room.status === 'cleaning' ? `
                <button class="detail-action-btn complete-btn">完成清洁</button>
                <button class="detail-action-btn schedule-btn">安排清洁</button>
              ` : `
                <button class="detail-action-btn repair-btn">完成维修</button>
                <button class="detail-action-btn report-btn">问题报告</button>
              `}
              <button class="detail-action-btn edit-btn">编辑信息</button>
            </div>
          </div>
        `;
      });
    });
    
    if (detailClose) {
      detailClose.addEventListener('click', () => {
        detailPanel.classList.remove('active');
      });
    }
  }, 500);
}

// 生成随机名字（仅用于演示）
function generateRandomName() {
  const firstNames = ['张', '王', '李', '赵', '刘', '陈', '杨', '黄', '周', '吴', '郑', '孙'];
  const lastNames = ['伟', '芳', '娜', '秀英', '敏', '静', '丽', '强', '磊', '军', '洋', '勇'];
  
  return firstNames[Math.floor(Math.random() * firstNames.length)] + 
         lastNames[Math.floor(Math.random() * lastNames.length)];
}

// 生成随机日期（仅用于演示）
function generateRandomDate(minDays, maxDays) {
  const today = new Date();
  const days = minDays + Math.floor(Math.random() * (maxDays - minDays + 1));
  const date = new Date(today);
  date.setDate(date.getDate() + days);
  
  return date.toISOString().split('T')[0];
}

// 获取房间价格（仅用于演示）
function getRoomPrice(type) {
  switch(type) {
    case 'standard': return 480;
    case 'double': return 580;
    case 'deluxe': return 880;
    case 'executive': return 1280;
    case 'presidential': return 3280;
    default: return 580;
  }
}

// 获取随机维修问题（仅用于演示）
function getRandomMaintenance() {
  const issues = [
    '空调不制冷', '淋浴水压低', '电视故障', '门锁损坏', 
    '热水器问题', '窗户无法关闭', '灯光故障', '墙壁修补'
  ];
  
  return issues[Math.floor(Math.random() * issues.length)];
}

// 生成随机时间（仅用于演示）
function generateRandomTime(hourOffset = 0) {
  const now = new Date();
  now.setHours(now.getHours() + hourOffset);
  
  let hours = now.getHours();
  let minutes = now.getMinutes();
  
  // 添加随机分钟
  if (hourOffset !== 0) {
    minutes = Math.floor(Math.random() * 60);
  }
  
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

// 显示今日待办
const showTodayTasks = () => {
  // 扩展今日待办数据
  const todayTasks = [
    { 
      id: 1, 
      type: 'vip', 
      content: 'VIP客户接待 - 李先生 11:30到店', 
      status: 'pending',
      priority: 'high',
      time: '11:30',
      location: '前台',
      assignee: '张秘书',
      details: '金卡会员，需准备水果篮和欢迎卡片',
      deadline: '11:15'
    },
    { 
      id: 2, 
      type: 'checkout', 
      content: '302房延迟退房申请处理', 
      status: 'pending',
      priority: 'medium',
      time: '13:00',
      location: '302房',
      assignee: '王经理',
      details: '客人申请延迟至下午14:00退房，需核实房间状态',
      deadline: '12:00'
    },
    { 
      id: 3, 
      type: 'service', 
      content: '401房送加床服务', 
      status: 'completed',
      priority: 'medium',
      time: '09:30',
      location: '401房',
      assignee: '李服务员',
      details: '客人增加1位入住，需加1张床',
      deadline: '09:15',
      completedTime: '09:25',
      completedBy: '李服务员'
    },
    { 
      id: 4, 
      type: 'complaint', 
      content: '处理305房噪音投诉', 
      status: 'processing',
      priority: 'high',
      time: '10:15',
      location: '305房',
      assignee: '陈经理',
      details: '客人投诉隔壁房间噪音太大，影响休息',
      deadline: '10:30',
      startTime: '10:20'
    },
    { 
      id: 5, 
      type: 'cleaning', 
      content: '安排508房紧急清洁', 
      status: 'completed',
      priority: 'high',
      time: '08:45',
      location: '508房',
      assignee: '清洁部门',
      details: '贵宾即将入住，需提前准备房间',
      deadline: '10:00',
      completedTime: '09:50',
      completedBy: '赵清洁员'
    },
    { 
      id: 6, 
      type: 'maintenance', 
      content: '检修603房空调故障', 
      status: 'pending',
      priority: 'medium',
      time: '14:00',
      location: '603房',
      assignee: '维修部门',
      details: '客人报修空调不制冷，需尽快处理',
      deadline: '15:30'
    },
    { 
      id: 7, 
      type: 'meal', 
      content: '准备15人商务会议午餐', 
      status: 'completed',
      priority: 'high',
      time: '11:00',
      location: '钻石会议室',
      assignee: '餐饮部',
      details: '准备15人商务自助午餐，含2位素食',
      deadline: '12:00',
      completedTime: '11:45',
      completedBy: '餐饮部'
    }
  ]
  
  const pendingTasks = todayTasks.filter(task => task.status === 'pending').length;
  const processingTasks = todayTasks.filter(task => task.status === 'processing').length;
  const completedTasks = todayTasks.filter(task => task.status === 'completed').length;
  const highPriorityTasks = todayTasks.filter(task => task.priority === 'high' && task.status !== 'completed').length;
  
  ElMessageBox.alert(
    `<div class="detail-dialog-content premium-dialog tasks-dialog">
      <div class="premium-dialog-header">
        <div class="premium-header-icon tasks-icon">
          <i class="el-icon-tickets"></i>
        </div>
        <div class="premium-header-title">
          <h3>今日待办事项</h3>
          <div class="premium-header-subtitle">实时任务追踪 · ${new Date().toLocaleDateString()}</div>
        </div>
      </div>
      
      <div class="premium-stats tasks-stats">
        <div class="premium-stat-item">
          <div class="stat-value">${todayTasks.length}</div>
          <div class="stat-label">总任务数</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value ${highPriorityTasks > 0 ? 'alert' : ''}">${highPriorityTasks}</div>
          <div class="stat-label">高优先级</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${pendingTasks}</div>
          <div class="stat-label">待处理</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${processingTasks}</div>
          <div class="stat-label">处理中</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${completedTasks}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${Math.round(completedTasks/todayTasks.length*100)}%</div>
          <div class="stat-label">完成率</div>
        </div>
      </div>
      
      <div class="tasks-time-tracking">
        <div class="time-tracking-header">
          <div class="current-time">${new Date().toLocaleTimeString()}</div>
          <div class="time-progress">
            <div class="time-progress-bar" style="width: ${Math.round((new Date().getHours() * 60 + new Date().getMinutes()) / (24 * 60) * 100)}%"></div>
          </div>
          <div class="day-period">
            ${new Date().getHours() < 12 ? '上午' : new Date().getHours() < 18 ? '下午' : '晚上'}
          </div>
        </div>
      </div>
      
      <div class="premium-filter-bar tasks-filter">
        <div class="filter-tabs">
          <div class="filter-tab active">全部任务</div>
          <div class="filter-tab">待处理</div>
          <div class="filter-tab">处理中</div>
          <div class="filter-tab">已完成</div>
        </div>
        <div class="filter-actions">
          <select class="filter-select">
            <option value="all">所有类型</option>
            <option value="vip">VIP接待</option>
            <option value="checkout">退房处理</option>
            <option value="service">客房服务</option>
            <option value="complaint">投诉处理</option>
            <option value="cleaning">清洁任务</option>
          </select>
          <select class="filter-select">
            <option value="all">所有优先级</option>
            <option value="high">高优先级</option>
            <option value="medium">中优先级</option>
            <option value="low">低优先级</option>
          </select>
          <button class="filter-action-btn add-task"><i class="el-icon-plus"></i> 添加任务</button>
        </div>
      </div>
      
      <div class="tasks-list-container">
        <div class="tasks-timeline">
          <div class="timeline-now-indicator" style="top: ${Math.round((new Date().getHours() * 60 + new Date().getMinutes() - 8 * 60) / (14 * 60) * 100)}%">
            <div class="now-time">${new Date().getHours()}:${new Date().getMinutes().toString().padStart(2, '0')}</div>
            <div class="now-marker"></div>
          </div>
          <div class="timeline-hours">
            ${Array.from({length: 15}, (_, i) => i + 8).map(hour => `
              <div class="timeline-hour">
                <div class="hour-label">${hour}:00</div>
                <div class="hour-line"></div>
              </div>
            `).join('')}
          </div>
          
          ${todayTasks.map(task => {
            const taskHour = parseInt(task.time.split(':')[0]);
            const taskMinute = parseInt(task.time.split(':')[1]);
            const position = ((taskHour * 60 + taskMinute) - 8 * 60) / (14 * 60) * 100;
            return `
              <div class="timeline-task ${task.status} ${task.priority}" style="top: ${position}%">
                <div class="task-time-marker">
                  <div class="task-time">${task.time}</div>
                  <div class="task-type-icon ${task.type}"></div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
        
        <div class="tasks-list">
          ${todayTasks.map(task => `
            <div class="premium-list-item task-item ${task.status} ${task.priority}">
              <div class="task-priority-indicator"></div>
              
              <div class="task-main">
                <div class="task-header">
                  <div class="task-type ${task.type}">
                    <span class="task-type-icon"></span>
                    <span class="task-type-text">
                      ${task.type === 'vip' ? 'VIP接待' : 
                        task.type === 'checkout' ? '退房处理' : 
                        task.type === 'service' ? '客房服务' : 
                        task.type === 'complaint' ? '投诉处理' : 
                        task.type === 'cleaning' ? '清洁任务' : 
                        task.type === 'maintenance' ? '维修任务' : '餐饮服务'}
                    </span>
                  </div>
                  <div class="task-meta">
                    <span class="task-priority">
                      ${task.priority === 'high' ? '高优先级' : 
                        task.priority === 'medium' ? '中优先级' : '低优先级'}
                    </span>
                    <span class="task-id">#${task.id}</span>
                  </div>
                </div>
                
                <div class="task-content">
                  <div class="task-description">${task.content}</div>
                  <div class="task-details">${task.details}</div>
                </div>
                
                <div class="task-info">
                  <div class="info-item">
                    <span class="info-icon"><i class="el-icon-time"></i></span>
                    <span class="info-text">${task.time}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-icon"><i class="el-icon-location"></i></span>
                    <span class="info-text">${task.location}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-icon"><i class="el-icon-user"></i></span>
                    <span class="info-text">${task.assignee}</span>
                  </div>
                  <div class="info-item deadline ${new Date().toLocaleTimeString() > task.deadline && task.status === 'pending' ? 'overdue' : ''}">
                    <span class="info-icon"><i class="el-icon-alarm-clock"></i></span>
                    <span class="info-text">截止: ${task.deadline}</span>
                  </div>
                </div>
                
                <div class="task-status-bar">
                  <div class="status-indicator ${task.status}">
                    ${task.status === 'pending' ? '待处理' : 
                      task.status === 'processing' ? '处理中' : '已完成'}
                  </div>
                  
                  <div class="task-actions">
                    ${task.status === 'pending' ? `
                      <button class="task-action-btn start-btn">开始处理</button>
                      <button class="task-action-btn assign-btn">转交</button>
                    ` : task.status === 'processing' ? `
                      <button class="task-action-btn complete-btn">完成任务</button>
                      <button class="task-action-btn issue-btn">报告问题</button>
                    ` : `
                      <div class="task-completed-info">
                        <span class="completed-by">由 ${task.completedBy} 完成</span>
                        <span class="completed-time">于 ${task.completedTime}</span>
                      </div>
                    `}
                    <button class="task-action-btn more-btn"><i class="el-icon-more"></i></button>
                  </div>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
      
      <div class="premium-footer-actions tasks-footer">
        <button class="premium-footer-btn refresh-btn"><i class="el-icon-refresh"></i> 刷新列表</button>
        <button class="premium-footer-btn export-btn"><i class="el-icon-download"></i> 导出任务</button>
        <button class="premium-footer-btn close-btn">关闭</button>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      showClose: true,
      customClass: 'premium-message-box tasks-message-box full-width-dialog'
    }
  )
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
      source: '直接预订',
      time: '09:30:25',
      guestCount: 2,
      roomCount: 1,
      total: 1160,
      specialRequests: '希望有安静房间'
    },
    {
      bookingNo: 'BK20240403002',
      customerName: '李华',
      phone: '13922223333',
      roomType: '豪华大床房',
      checkInDate: '2024-04-06',
      checkOutDate: '2024-04-08',
      source: '携程',
      time: '14:15:40',
      guestCount: 2,
      roomCount: 1,
      total: 1760,
      specialRequests: '靠近电梯'
    },
    {
      bookingNo: 'BK20240404001',
      customerName: '王强',
      phone: '13833334444',
      roomType: '行政套房',
      checkInDate: '2024-04-10',
      checkOutDate: '2024-04-15',
      source: '美团',
      time: '18:22:10',
      guestCount: 3,
      roomCount: 1,
      total: 6400,
      specialRequests: '需要加床，有小孩'
    }
  ]
  
  ElMessageBox.alert(
    `<div class="detail-dialog-content premium-dialog pending-bookings-dialog">
      <div class="premium-dialog-header">
        <div class="premium-header-icon">
          <i class="el-icon-clock"></i>
        </div>
        <div class="premium-header-title">
          <h3>待确认预订列表</h3>
          <div class="premium-header-subtitle">共 ${pendingBookings.length} 条待处理的预订需要确认</div>
        </div>
      </div>
      
      <div class="premium-stats">
        <div class="premium-stat-item">
          <div class="stat-value">${pendingBookings.length}</div>
          <div class="stat-label">待确认</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">¥${pendingBookings.reduce((sum, booking) => sum + booking.total, 0)}</div>
          <div class="stat-label">预计收入</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${pendingBookings.reduce((sum, booking) => sum + booking.roomCount, 0)}</div>
          <div class="stat-label">房间数</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${Math.round(pendingBookings.reduce((sum, booking) => {
            const days = new Date(booking.checkOutDate) - new Date(booking.checkInDate);
            return sum + days / (1000 * 60 * 60 * 24);
          }, 0))}</div>
          <div class="stat-label">总天数</div>
        </div>
      </div>
      
      <div class="premium-action-bar">
        <div class="action-search">
          <input type="text" placeholder="搜索预订号/客户名称" class="premium-search-input" />
        </div>
        <div class="action-buttons">
          <button class="premium-action-btn confirm-all">一键确认全部</button>
          <button class="premium-action-btn export-data">导出数据</button>
        </div>
      </div>
      
      <div class="premium-list">
        ${pendingBookings.map(booking => `
          <div class="premium-list-item">
            <div class="premium-item-badge">待确认</div>
            
            <div class="premium-item-main">
              <div class="premium-item-header">
                <div class="premium-booking-info">
                  <div class="premium-booking-no">${booking.bookingNo}</div>
                  <div class="premium-booking-time">预订时间: ${booking.time}</div>
                </div>
                <div class="premium-booking-source">${booking.source}</div>
              </div>
              
              <div class="premium-item-content">
                <div class="premium-content-col customer-info">
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-user"></i> 客户信息</div>
                    <div class="info-value">${booking.customerName} | ${booking.phone}</div>
                  </div>
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-s-home"></i> 房型信息</div>
                    <div class="info-value">${booking.roomType} x ${booking.roomCount}间</div>
                  </div>
                </div>
                
                <div class="premium-content-col stay-info">
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-date"></i> 入住时间</div>
                    <div class="info-value">${booking.checkInDate} 至 ${booking.checkOutDate}</div>
                  </div>
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-user-solid"></i> 入住人数</div>
                    <div class="info-value">${booking.guestCount}人</div>
                  </div>
                </div>
                
                <div class="premium-content-col price-info">
                  <div class="info-group">
                    <div class="info-label">总价</div>
                    <div class="price-value">¥${booking.total}</div>
                  </div>
                </div>
              </div>
              
              <div class="premium-item-footer ${booking.specialRequests ? 'has-requests' : ''}">
                ${booking.specialRequests ? `
                  <div class="special-requests">
                    <i class="el-icon-warning-outline"></i> 特殊要求: ${booking.specialRequests}
                  </div>
                ` : ''}
                <div class="premium-item-actions">
                  <button class="premium-btn confirm">确认预订</button>
                  <button class="premium-btn view">查看详情</button>
                  <button class="premium-btn cancel">取消预订</button>
                </div>
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      showClose: true,
      customClass: 'premium-message-box pending-message-box'
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
      guestCount: 2,
      paymentStatus: 'deposit',
      assignedRoom: '',
      specialRequests: '靠窗房间',
      vip: true,
      source: '官网预订',
      days: 2,
      totalAmount: 1760
    },
    {
      bookingNo: 'BK20240404002',
      customerName: '赵六',
      phone: '13887654321',
      roomType: '标准双人间',
      checkInTime: '15:00',
      checkOutDate: '2024-04-07',
      status: 'confirmed',
      guestCount: 2,
      paymentStatus: 'paid',
      assignedRoom: '308',
      specialRequests: '',
      vip: false,
      source: '携程',
      days: 3,
      totalAmount: 1740
    },
    {
      bookingNo: 'BK20240404003',
      customerName: '李明',
      phone: '13765432198',
      roomType: '行政套房',
      checkInTime: '16:30',
      checkOutDate: '2024-04-08',
      status: 'confirmed',
      guestCount: 3,
      paymentStatus: 'unpaid',
      assignedRoom: '',
      specialRequests: '需要加床',
      vip: false,
      source: '美团',
      days: 4,
      totalAmount: 5120
    }
  ]
  
  ElMessageBox.alert(
    `<div class="detail-dialog-content premium-dialog checkin-dialog">
      <div class="premium-dialog-header">
        <div class="premium-header-icon checkin-icon">
          <i class="el-icon-calendar"></i>
        </div>
        <div class="premium-header-title">
          <h3>今日入住预订</h3>
          <div class="premium-header-subtitle">今日共有 ${todayBookings.length} 位客人将办理入住</div>
        </div>
      </div>
      
      <div class="premium-stats checkin-stats">
        <div class="premium-stat-item">
          <div class="stat-value">${todayBookings.length}</div>
          <div class="stat-label">今日入住</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${todayBookings.filter(b => b.vip).length}</div>
          <div class="stat-label">VIP客户</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">${todayBookings.reduce((sum, booking) => sum + booking.guestCount, 0)}</div>
          <div class="stat-label">入住人数</div>
        </div>
        <div class="premium-stat-item">
          <div class="stat-value">¥${todayBookings.reduce((sum, booking) => sum + booking.totalAmount, 0)}</div>
          <div class="stat-label">总收入</div>
        </div>
      </div>
      
      <div class="checkin-timeline">
        <div class="timeline-header">
          <div class="timeline-title">入住时间安排</div>
          <div class="timeline-hours">
            <div>08:00</div>
            <div>10:00</div>
            <div>12:00</div>
            <div>14:00</div>
            <div>16:00</div>
            <div>18:00</div>
            <div>20:00</div>
            <div>22:00</div>
          </div>
        </div>
        <div class="timeline-content">
          ${todayBookings.map(booking => {
            // 转换时间到位置
            const timeStr = booking.checkInTime.split(':');
            const hour = parseInt(timeStr[0]);
            const minute = parseInt(timeStr[1]);
            const position = ((hour - 8) * 60 + minute) / (14 * 60) * 100;
            
            return `
              <div class="timeline-item" style="left: ${position}%">
                <div class="timeline-marker ${booking.vip ? 'vip-marker' : ''}"></div>
                <div class="timeline-item-content">
                  <div class="timeline-time">${booking.checkInTime}</div>
                  <div class="timeline-name">${booking.customerName}</div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>
      
      <div class="premium-filter-bar">
        <div class="filter-label">筛选：</div>
        <div class="filter-options">
          <span class="filter-option active">全部</span>
          <span class="filter-option">已分配房间</span>
          <span class="filter-option">未分配房间</span>
          <span class="filter-option">VIP客户</span>
        </div>
        <div class="filter-search">
          <input type="text" placeholder="搜索客户名/预订号" class="premium-search-input" />
        </div>
      </div>
      
      <div class="premium-list checkin-list">
        ${todayBookings.map(booking => `
          <div class="premium-list-item checkin-item ${booking.vip ? 'vip-item' : ''}">
            <div class="premium-item-time">
              <div class="time-value">${booking.checkInTime}</div>
              <div class="time-label">预计到店</div>
            </div>
            
            <div class="premium-item-main">
              <div class="premium-item-header">
                <div class="premium-booking-info">
                  <div class="premium-customer-name">
                    ${booking.customerName} 
                    ${booking.vip ? '<span class="vip-badge">VIP</span>' : ''}
                  </div>
                  <div class="premium-booking-no">${booking.bookingNo}</div>
                </div>
                <div class="premium-payment-status ${booking.paymentStatus}">
                  ${booking.paymentStatus === 'paid' ? '已付全款' : 
                    booking.paymentStatus === 'deposit' ? '已付定金' : '未付款'}
                </div>
              </div>
              
              <div class="premium-item-content">
                <div class="premium-content-col">
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-phone"></i> 联系电话</div>
                    <div class="info-value">${booking.phone}</div>
                  </div>
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-s-home"></i> 房型信息</div>
                    <div class="info-value">${booking.roomType}</div>
                  </div>
                </div>
                
                <div class="premium-content-col">
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-date"></i> 入住时长</div>
                    <div class="info-value">${booking.days}晚 (${booking.checkOutDate}离店)</div>
                  </div>
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-user-solid"></i> 入住人数</div>
                    <div class="info-value">${booking.guestCount}人</div>
                  </div>
                </div>
                
                <div class="premium-content-col">
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-office-building"></i> 房间安排</div>
                    <div class="info-value room-assign">
                      ${booking.assignedRoom ? 
                        `<span class="assigned-room">${booking.assignedRoom}房间</span>` : 
                        '<span class="unassigned-room">未分配</span>'}
                    </div>
                  </div>
                  <div class="info-group">
                    <div class="info-label"><i class="el-icon-shopping-cart-full"></i> 预订来源</div>
                    <div class="info-value">${booking.source}</div>
                  </div>
                </div>
              </div>
              
              <div class="premium-item-footer ${booking.specialRequests ? 'has-requests' : ''}">
                ${booking.specialRequests ? `
                  <div class="special-requests">
                    <i class="el-icon-warning-outline"></i> 特殊要求: ${booking.specialRequests}
                  </div>
                ` : ''}
                <div class="premium-item-actions">
                  <button class="premium-btn checkin-btn">办理入住</button>
                  ${!booking.assignedRoom ? `<button class="premium-btn assign-btn">分配房间</button>` : ''}
                  <button class="premium-btn view-btn">查看详情</button>
                  ${booking.paymentStatus !== 'paid' ? `<button class="premium-btn payment-btn">收款</button>` : ''}
                </div>
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      showClose: true,
      customClass: 'premium-message-box checkin-message-box full-width-dialog'
    }
  )
}

// 显示入住率详情
const showOccupancyDetails = () => {
  ElMessageBox.alert(
    `<div class="detail-dialog-content premium-dialog occupancy-dialog">
      <div class="premium-dialog-header">
        <div class="premium-header-icon occupancy-icon">
          <i class="el-icon-data-analysis"></i>
        </div>
        <div class="premium-header-title">
          <h3>酒店入住率分析</h3>
          <div class="premium-header-subtitle">实时数据 · 更新于 ${new Date().toLocaleTimeString()}</div>
        </div>
      </div>
      
      <div class="occupancy-overview">
        <div class="occupancy-chart-container">
          <div class="occupancy-chart">
            <div class="chart-donut">
              <div class="donut-hole"></div>
              <div class="donut-ring"></div>
              <div class="donut-segment" style="transform: rotate(0); clip-path: inset(0 0 0 50%);"></div>
              <div class="donut-segment" style="transform: rotate(180deg); clip-path: inset(0 0 0 ${100-occupancyRate.value/2}%);"></div>
              <div class="donut-center">
                <div class="donut-value">${occupancyRate.value}%</div>
                <div class="donut-label">入住率</div>
              </div>
            </div>
          </div>
          <div class="occupancy-info">
            <div class="occupancy-metric">
              <div class="metric-value">${totalRooms.value}</div>
              <div class="metric-label">总房间数</div>
            </div>
            <div class="occupancy-metric">
              <div class="metric-value">${occupiedRooms.value}</div>
              <div class="metric-label">已入住</div>
            </div>
            <div class="occupancy-metric">
              <div class="metric-value">${totalRooms.value - occupiedRooms.value}</div>
              <div class="metric-label">可用房间</div>
            </div>
            <div class="occupancy-metric">
              <div class="metric-value">¥${(occupiedRooms.value * 680).toLocaleString()}</div>
              <div class="metric-label">今日收入</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="occupancy-detail-tabs">
        <div class="occupancy-tab active">按房型</div>
        <div class="occupancy-tab">按楼层</div>
        <div class="occupancy-tab">按价格</div>
        <div class="occupancy-tab">趋势分析</div>
      </div>
      
      <div class="occupancy-table-section">
        <div class="occupancy-table-header">
          <h4>各房型入住情况</h4>
          <div class="table-actions">
            <button class="table-action-btn refresh-btn"><i class="el-icon-refresh"></i></button>
            <button class="table-action-btn export-btn"><i class="el-icon-download"></i> 导出</button>
          </div>
        </div>
        
        <table class="premium-table room-type-table">
          <thead>
            <tr>
              <th>房型名称</th>
              <th>房间数量</th>
              <th>已入住</th>
              <th>可用数量</th>
              <th>入住率</th>
              <th>平均价格</th>
              <th>收入</th>
            </tr>
          </thead>
          <tbody>
            ${roomTypes.map(type => {
              const occupiedCount = type.totalCount - type.availableCount;
              const occupancyPercent = Math.round(occupiedCount / type.totalCount * 100);
              return `
                <tr>
                  <td class="room-type-name">${type.name}</td>
                  <td>${type.totalCount}</td>
                  <td>${occupiedCount}</td>
                  <td>${type.availableCount}</td>
                  <td>
                    <div class="table-progress-bar">
                      <div class="progress-fill" style="width: ${occupancyPercent}%"></div>
                      <div class="progress-text">${occupancyPercent}%</div>
                    </div>
                  </td>
                  <td>¥${type.price}</td>
                  <td>¥${(occupiedCount * type.price).toLocaleString()}</td>
                </tr>
              `;
            }).join('')}
          </tbody>
          <tfoot>
            <tr>
              <td colspan="2">合计</td>
              <td>${occupiedRooms.value}</td>
              <td>${totalRooms.value - occupiedRooms.value}</td>
              <td>
                <div class="table-progress-bar">
                  <div class="progress-fill" style="width: ${occupancyRate.value}%"></div>
                  <div class="progress-text">${occupancyRate.value}%</div>
                </div>
              </td>
              <td>¥${Math.round(roomTypes.reduce((sum, type) => sum + type.price * (type.totalCount - type.availableCount), 0) / occupiedRooms.value)}</td>
              <td>¥${roomTypes.reduce((sum, type) => sum + type.price * (type.totalCount - type.availableCount), 0).toLocaleString()}</td>
            </tr>
          </tfoot>
        </table>
      </div>
      
      <div class="occupancy-comparison">
        <div class="comparison-header">
          <h4>入住率历史对比</h4>
          <div class="comparison-period">
            <span class="period-option active">周对比</span>
            <span class="period-option">月对比</span>
            <span class="period-option">年对比</span>
          </div>
        </div>
        <div class="comparison-chart">
          <div class="chart-bars">
            <div class="chart-period">
              <div class="period-bar-group">
                <div class="period-name">上周</div>
                <div class="period-bar-container">
                  <div class="period-bar" style="height: 68%"><span>68%</span></div>
                </div>
              </div>
              <div class="period-bar-group">
                <div class="period-name">本周</div>
                <div class="period-bar-container">
                  <div class="period-bar current-period" style="height: ${occupancyRate.value}%"><span>${occupancyRate.value}%</span></div>
                </div>
              </div>
            </div>
            <div class="chart-data-points">
              <div class="data-point">
                <div class="point-value ${occupancyRate.value > 68 ? 'increase' : 'decrease'}">${occupancyRate.value > 68 ? '+' : ''}${(occupancyRate.value - 68)}%</div>
                <div class="point-label">环比变化</div>
              </div>
              <div class="data-point">
                <div class="point-value increase">+8%</div>
                <div class="point-label">同比增长</div>
              </div>
              <div class="data-point">
                <div class="point-value">82%</div>
                <div class="point-label">历史最高</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="premium-footer-actions">
        <button class="premium-footer-btn report-btn"><i class="el-icon-document"></i> 生成报表</button>
        <button class="premium-footer-btn close-btn">关闭</button>
      </div>
    </div>`,
    ' ',
    {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      showClose: true,
      customClass: 'premium-message-box occupancy-message-box full-width-dialog'
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

/* 高级对话框样式 */
.premium-dialog {
  padding: 0;
}

.premium-dialog-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eaedf3;
  background: linear-gradient(135deg, #f8fafc, #eef2f7);
}

.premium-header-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0d6efd, #0a58ca);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.premium-header-icon.occupancy-icon {
  background: linear-gradient(135deg, #28a745, #198754);
}

.premium-header-icon.tasks-icon {
  background: linear-gradient(135deg, #dc3545, #b02a37);
}

.premium-header-icon.checkin-icon {
  background: linear-gradient(135deg, #17a2b8, #0a97b0);
}

.premium-header-icon.room-status-icon {
  background: linear-gradient(135deg, #6f42c1, #5e37a6);
}

.premium-header-title h3 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #2c3e50;
}

.premium-header-subtitle {
  font-size: 14px;
  color: #6c757d;
}

.premium-stats {
  display: flex;
  padding: 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
  gap: 15px;
  flex-wrap: wrap;
}

.premium-stat-item {
  flex: 1;
  min-width: 100px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f8fafc, #eef2f7);
  padding: 15px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.premium-stat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.premium-stat-item .stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #0d6efd;
  margin-bottom: 5px;
}

.premium-stat-item .stat-label {
  font-size: 13px;
  color: #6c757d;
  font-weight: 500;
}

.checkin-stats .premium-stat-item:nth-child(1) .stat-value {
  color: #17a2b8;
}

.checkin-stats .premium-stat-item:nth-child(2) .stat-value {
  color: #f0ad4e;
}

.checkin-stats .premium-stat-item:nth-child(3) .stat-value {
  color: #0d6efd;
}

.checkin-stats .premium-stat-item:nth-child(4) .stat-value {
  color: #28a745;
}

.tasks-stats .premium-stat-item .stat-value.alert {
  color: #dc3545;
}

.premium-action-bar, .premium-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.action-search {
  flex: 1;
  max-width: 300px;
}

.premium-search-input {
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  outline: none;
  transition: all 0.3s ease;
  font-size: 14px;
}

.premium-search-input:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 3px rgba(13, 110, 253, 0.15);
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.premium-action-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.premium-action-btn.confirm-all {
  background-color: #28a745;
  color: white;
}

.premium-action-btn.export-data {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.premium-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.premium-list {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
  background-color: #f8fafc;
}

.premium-list-item {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
}

.premium-list-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.premium-item-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 4px 10px;
  border-radius: 6px;
  background-color: #f0ad4e;
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.premium-item-main {
  flex: 1;
  padding: 15px;
}

.premium-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.premium-booking-info {
  display: flex;
  flex-direction: column;
}

.premium-booking-no {
  font-size: 15px;
  font-weight: 600;
  color: #0d6efd;
}

.premium-booking-time, .premium-booking-source {
  font-size: 13px;
  color: #6c757d;
}

.premium-booking-source {
  padding: 4px 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
  font-weight: 500;
  height: fit-content;
}

.premium-item-content {
  display: flex;
  margin-bottom: 12px;
  gap: 20px;
}

.premium-content-col {
  flex: 1;
}

.info-group {
  margin-bottom: 10px;
}

.info-label {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-value {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.price-value {
  font-size: 18px;
  font-weight: 600;
  color: #28a745;
}

.premium-item-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eaedf3;
}

.premium-item-footer.has-requests {
  justify-content: space-between;
}

.special-requests {
  font-size: 13px;
  color: #f0ad4e;
  display: flex;
  align-items: center;
  gap: 5px;
}

.premium-item-actions {
  display: flex;
  gap: 10px;
}

.premium-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.premium-btn.confirm {
  background-color: #28a745;
  color: white;
}

.premium-btn.view {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.premium-btn.cancel {
  background-color: #f8f9fa;
  color: #dc3545;
  border: 1px solid #dee2e6;
}

.premium-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.premium-message-box :deep(.el-message-box__header) {
  display: none !important;
}

.premium-message-box :deep(.el-message-box__content) {
  padding: 0 !important;
}

.pending-message-box :deep(.el-message-box) {
  width: 800px !important;
  max-width: 90vw !important;
  border-radius: 16px !important;
  overflow: hidden !important;
}

.full-width-dialog :deep(.el-message-box) {
  width: 1000px !important;
  max-width: 95vw !important;
  border-radius: 16px !important;
  overflow: hidden !important;
}

.large-dialog :deep(.el-message-box) {
  width: 1200px !important;
  max-width: 95vw !important;
  height: 80vh !important;
  max-height: 800px !important;
  display: flex !important;
  flex-direction: column !important;
}

.large-dialog :deep(.el-message-box__content) {
  flex: 1 !important;
  overflow: hidden !important;
}

.large-dialog .detail-dialog-content {
  height: 100% !important;
  display: flex !important;
  flex-direction: column !important;
}

/* 入住时间轴样式 */
.checkin-timeline {
  padding: 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.timeline-hours {
  display: flex;
  justify-content: space-between;
  width: 90%;
  padding-left: 20px;
}

.timeline-hours div {
  font-size: 12px;
  color: #6c757d;
}

.timeline-content {
  position: relative;
  height: 70px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eaedf3;
}

.timeline-item {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
}

.timeline-marker {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: #0d6efd;
  margin-bottom: 5px;
  position: relative;
  top: -7px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 0 0 3px rgba(13, 110, 253, 0.2);
}

.timeline-marker.vip-marker {
  background-color: #f0ad4e;
  box-shadow: 0 0 0 3px rgba(240, 173, 78, 0.2);
}

.timeline-item-content {
  width: 80px;
  text-align: center;
  transform: translateX(-50%);
}

.timeline-time {
  font-size: 12px;
  font-weight: 600;
  color: #2c3e50;
}

.timeline-name {
  font-size: 11px;
  color: #6c757d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 入住列表项样式 */
.checkin-item {
  padding-left: 85px;
  position: relative;
}

.premium-item-time {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 70px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0d6efd, #0a58ca);
  color: white;
  border-top-left-radius: 12px;
  border-bottom-left-radius: 12px;
}

.time-value {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 3px;
}

.time-label {
  font-size: 11px;
  opacity: 0.8;
}

.vip-item .premium-item-time {
  background: linear-gradient(135deg, #f0ad4e, #ec971f);
}

.premium-customer-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.vip-badge {
  background-color: #f0ad4e;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.premium-payment-status {
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.premium-payment-status.paid {
  background-color: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.premium-payment-status.deposit {
  background-color: rgba(240, 173, 78, 0.1);
  color: #f0ad4e;
}

.premium-payment-status.unpaid {
  background-color: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.room-assign .assigned-room {
  color: #28a745;
  font-weight: 600;
}

.room-assign .unassigned-room {
  color: #f0ad4e;
  font-weight: 600;
}

.premium-btn.checkin-btn {
  background-color: #17a2b8;
  color: white;
}

.premium-btn.assign-btn {
  background-color: #f0ad4e;
  color: white;
}

.premium-btn.view-btn {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.premium-btn.payment-btn {
  background-color: #0d6efd;
  color: white;
}

/* 入住率详情样式 */
.occupancy-overview {
  padding: 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.occupancy-chart-container {
  display: flex;
  gap: 30px;
  align-items: center;
}

.occupancy-chart {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  position: relative;
}

.chart-donut {
  width: 100%;
  height: 100%;
  position: relative;
}

.donut-hole {
  width: 60%;
  height: 60%;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
}

.donut-ring {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #f0f5ff;
  position: absolute;
}

.donut-segment {
  width: 100%;
  height: 100%;
  position: absolute;
  border-radius: 50%;
  background-color: #28a745;
}

.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 4;
  text-align: center;
}

.donut-value {
  font-size: 32px;
  font-weight: 700;
  color: #28a745;
  line-height: 1;
  margin-bottom: 5px;
}

.donut-label {
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
}

.occupancy-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  flex: 1;
}

.occupancy-metric {
  flex: 1;
  min-width: 100px;
  background-color: #f8f9fa;
  padding: 15px;
  text-align: center;
  border-radius: 10px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.metric-label {
  font-size: 14px;
  color: #6c757d;
}

.occupancy-detail-tabs {
  display: flex;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.occupancy-tab {
  padding: 15px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.occupancy-tab.active {
  color: #0d6efd;
}

.occupancy-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: #0d6efd;
}

.occupancy-tab:hover {
  color: #0d6efd;
}

.occupancy-table-section {
  padding: 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.occupancy-table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.occupancy-table-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.table-action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 5px;
}

.table-action-btn:hover {
  background-color: #e9ecef;
}

.premium-table {
  width: 100%;
  border-collapse: collapse;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.premium-table th,
.premium-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eaedf3;
}

.premium-table thead th {
  background-color: #f8fafc;
  font-weight: 600;
  color: #6c757d;
  font-size: 13px;
}

.premium-table tbody td {
  font-size: 14px;
  color: #2c3e50;
}

.premium-table tbody tr:hover {
  background-color: #f8f9fa;
}

.premium-table tfoot {
  font-weight: 600;
  background-color: #f8fafc;
}

.table-progress-bar {
  height: 8px;
  background-color: #f1f3f5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  width: 100%;
  margin-top: 5px;
}

.progress-fill {
  height: 100%;
  background-color: #28a745;
  border-radius: 4px;
}

.progress-text {
  position: absolute;
  right: 0;
  top: -18px;
  font-size: 12px;
  color: #28a745;
  font-weight: 600;
}

.room-type-name {
  font-weight: 500;
  color: #0d6efd;
}

.occupancy-comparison {
  padding: 20px;
  background-color: #fff;
}

.comparison-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.comparison-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.comparison-period {
  display: flex;
  gap: 10px;
}

.period-option {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.period-option.active {
  background-color: #f8f9fa;
  color: #0d6efd;
  font-weight: 500;
}

.comparison-chart {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.chart-bars {
  display: flex;
  gap: 60px;
}

.chart-period {
  display: flex;
  gap: 40px;
  align-items: flex-end;
}

.period-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.period-name {
  font-size: 14px;
  color: #6c757d;
}

.period-bar-container {
  width: 60px;
  height: 200px;
  position: relative;
  display: flex;
  align-items: flex-end;
}

.period-bar {
  width: 100%;
  background-color: #0d6efd;
  border-radius: 4px 4px 0 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 10px;
  position: relative;
  transition: all 0.5s ease;
}

.period-bar span {
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.period-bar.current-period {
  background-color: #28a745;
}

.chart-data-points {
  display: flex;
  gap: 20px;
  margin-left: 40px;
  align-self: center;
}

.data-point {
  text-align: center;
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
  min-width: 100px;
}

.point-value {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 5px;
}

.point-value.increase {
  color: #28a745;
}

.point-value.decrease {
  color: #dc3545;
}

.point-label {
  font-size: 13px;
  color: #6c757d;
}

.premium-footer-actions {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background-color: #fff;
  border-top: 1px solid #eaedf3;
}

.premium-footer-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.premium-footer-btn.report-btn {
  background-color: #0d6efd;
  color: white;
  border: none;
}

.premium-footer-btn.close-btn {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.premium-footer-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 房态一览样式 */
.room-status-overview {
  display: flex;
  padding: 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
  gap: 30px;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  flex: 1;
}

.overview-stat {
  background-color: #f8f9fa;
  padding: 15px;
  text-align: center;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.overview-stat .stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.overview-stat .stat-label {
  font-size: 13px;
  color: #6c757d;
}

.overview-chart {
  width: 350px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
  text-align: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
}

.occupancy-bar {
  height: 16px;
  background-color: #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
}

.occupancy-progress {
  height: 100%;
  background: linear-gradient(90deg, #28a745, #20c997);
  border-radius: 8px;
}

.room-status-legend {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #6c757d;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-dot.available {
  background-color: #28a745;
}

.status-dot.occupied {
  background-color: #dc3545;
}

.status-dot.reserved {
  background-color: #f0ad4e;
}

.status-dot.cleaning {
  background-color: #17a2b8;
}

.status-dot.maintenance {
  background-color: #6c757d;
}

.room-status-controls {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.status-view-tabs {
  display: flex;
  gap: 20px;
}

.view-tab {
  padding: 8px 0;
  font-size: 14px;
  color: #6c757d;
  position: relative;
  cursor: pointer;
}

.view-tab.active {
  color: #0d6efd;
  font-weight: 500;
}

.view-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #0d6efd;
}

.status-actions {
  display: flex;
  gap: 10px;
}

.status-filter {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  font-size: 14px;
  color: #495057;
  outline: none;
}

.status-action-btn {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  background-color: #f8f9fa;
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.status-action-btn:hover {
  background-color: #e9ecef;
}

.room-status-grid-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.floor-section {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}

.floor-header {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.floor-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.floor-stats {
  display: flex;
  gap: 15px;
}

.floor-stat {
  font-size: 13px;
  color: #6c757d;
}

.floor-layout {
  padding: 20px;
}

.floor-corridor {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
  height: 40px;
}

.elevator, .service-room {
  width: 50px;
  height: 40px;
  background-color: #f8f9fa;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #6c757d;
  position: relative;
  z-index: 2;
}

.corridor-line {
  flex: 1;
  height: 6px;
  background-color: #e9ecef;
  position: relative;
  z-index: 1;
}

.floor-rooms {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 15px;
}

.room-cell {
  height: 85px;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
}

.room-cell:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.room-cell.available {
  background-color: rgba(40, 167, 69, 0.15);
  border: 1px solid rgba(40, 167, 69, 0.2);
}

.room-cell.occupied {
  background-color: rgba(220, 53, 69, 0.15);
  border: 1px solid rgba(220, 53, 69, 0.2);
}

.room-cell.reserved {
  background-color: rgba(240, 173, 78, 0.15);
  border: 1px solid rgba(240, 173, 78, 0.2);
}

.room-cell.cleaning {
  background-color: rgba(23, 162, 184, 0.15);
  border: 1px solid rgba(23, 162, 184, 0.2);
}

.room-cell.maintenance {
  background-color: rgba(108, 117, 125, 0.15);
  border: 1px solid rgba(108, 117, 125, 0.2);
}

.room-cell-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.room-number {
  font-weight: 600;
  font-size: 14px;
  color: #2c3e50;
}

.room-indicators {
  display: flex;
  gap: 5px;
}

.room-vip-indicator {
  font-size: 10px;
  font-weight: 600;
  color: white;
  background-color: #f0ad4e;
  padding: 1px 3px;
  border-radius: 2px;
}

.room-dirty-indicator {
  color: #dc3545;
  font-size: 12px;
}

.room-type-indicator {
  font-size: 11px;
  color: #6c757d;
  margin-bottom: 5px;
}

.room-guest, .room-date, .room-reserved, .room-available, .room-maintenance, .room-cleaning {
  font-size: 12px;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.room-detail-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 300px;
  background-color: white;
  box-shadow: -3px 0 15px rgba(0, 0, 0, 0.1);
  z-index: 5;
  transform: translateX(100%);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.room-detail-panel.active {
  transform: translateX(0);
}

.detail-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eaedf3;
}

.detail-panel-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.detail-close {
  font-size: 20px;
  color: #6c757d;
  cursor: pointer;
}

.detail-panel-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.detail-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
}

.detail-placeholder i {
  font-size: 40px;
  margin-bottom: 10px;
  opacity: 0.3;
}

.room-detail-card {
  background-color: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
}

.room-detail-card.occupied {
  border-top: 3px solid #dc3545;
}

.room-detail-card.available {
  border-top: 3px solid #28a745;
}

.room-detail-card.reserved {
  border-top: 3px solid #f0ad4e;
}

.room-detail-card.cleaning {
  border-top: 3px solid #17a2b8;
}

.room-detail-card.maintenance {
  border-top: 3px solid #6c757d;
}

.detail-header {
  padding: 15px;
  border-bottom: 1px solid #eaedf3;
  text-align: center;
}

.detail-room-number {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}

.detail-room-type {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 5px;
}

.detail-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
  background-color: #6c757d;
}

.room-detail-card.occupied .detail-status {
  background-color: #dc3545;
}

.room-detail-card.available .detail-status {
  background-color: #28a745;
}

.room-detail-card.reserved .detail-status {
  background-color: #f0ad4e;
}

.room-detail-card.cleaning .detail-status {
  background-color: #17a2b8;
}

.detail-section {
  padding: 15px;
  border-bottom: 1px solid #eaedf3;
}

.detail-section h5 {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.detail-info {
  font-size: 13px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 80px;
  color: #6c757d;
}

.info-value {
  flex: 1;
  color: #2c3e50;
  font-weight: 500;
}

.vip-tag {
  background-color: #f0ad4e;
  color: white;
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 10px;
  margin-left: 5px;
}

.warning {
  color: #dc3545;
}

.detail-actions {
  padding: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-action-btn {
  flex: 1;
  min-width: 80px;
  padding: 8px 10px;
  border-radius: 6px;
  border: none;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  text-align: center;
}

.detail-action-btn.checkin-btn {
  background-color: #28a745;
  color: white;
}

.detail-action-btn.checkout-btn {
  background-color: #dc3545;
  color: white;
}

.detail-action-btn.service-btn, .detail-action-btn.reserve-btn {
  background-color: #17a2b8;
  color: white;
}

.detail-action-btn.extend-btn {
  background-color: #f0ad4e;
  color: white;
}

.detail-action-btn.cancel-btn, .detail-action-btn.complete-btn, .detail-action-btn.repair-btn {
  background-color: #6c757d;
  color: white;
}

.detail-action-btn.edit-btn, .detail-action-btn.schedule-btn, .detail-action-btn.report-btn {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  color: #6c757d;
}

/* 今日待办任务样式 */
.tasks-time-tracking {
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaedf3;
}

.time-tracking-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-time {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  width: 80px;
}

.time-progress {
  flex: 1;
  height: 8px;
  background-color: #f1f3f5;
  border-radius: 4px;
  overflow: hidden;
}

.time-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #0d6efd, #0a58ca);
  border-radius: 4px;
}

.day-period {
  width: 60px;
  text-align: right;
  font-size: 14px;
  color: #6c757d;
}

.filter-tabs {
  display: flex;
  gap: 20px;
}

.filter-tab {
  padding: 8px 0;
  font-size: 14px;
  color: #6c757d;
  position: relative;
  cursor: pointer;
}

.filter-tab.active {
  color: #0d6efd;
  font-weight: 500;
}

.filter-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #0d6efd;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.filter-select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  font-size: 13px;
  color: #495057;
  outline: none;
}

.filter-action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.filter-action-btn.add-task {
  background-color: #28a745;
  color: white;
  border: none;
}

.tasks-list-container {
  flex: 1;
  display: flex;
  overflow: hidden;
  background-color: #f8fafc;
}

.tasks-timeline {
  width: 150px;
  background-color: white;
  padding: 20px 10px;
  border-right: 1px solid #eaedf3;
  position: relative;
}

.timeline-now-indicator {
  position: absolute;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
}

.now-time {
  font-size: 12px;
  font-weight: 600;
  color: #dc3545;
  margin-right: 5px;
  width: 45px;
  text-align: right;
}

.now-marker {
  flex: 1;
  height: 2px;
  background-color: #dc3545;
  position: relative;
}

.now-marker::after {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #dc3545;
  border-radius: 50%;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
}

.timeline-hours {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.timeline-hour {
  display: flex;
  align-items: center;
  gap: 5px;
}

.hour-label {
  font-size: 12px;
  color: #6c757d;
  width: 45px;
  text-align: right;
}

.hour-line {
  flex: 1;
  height: 1px;
  background-color: #eaedf3;
}

.timeline-task {
  position: absolute;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
}

.task-time-marker {
  display: flex;
  align-items: center;
  gap: 5px;
}

.task-time {
  font-size: 12px;
  color: #0d6efd;
  font-weight: 500;
  width: 45px;
  text-align: right;
}

.task-type-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #0d6efd;
  position: relative;
}

.timeline-task.pending .task-type-icon {
  background-color: #f0ad4e;
}

.timeline-task.processing .task-type-icon {
  background-color: #0d6efd;
}

.timeline-task.completed .task-type-icon {
  background-color: #28a745;
}

.timeline-task.high .task-time {
  color: #dc3545;
  font-weight: 600;
}

.timeline-task.high .task-type-icon {
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.2);
}

.task-item {
  position: relative;
  padding-left: 15px;
}

.task-priority-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: 2px;
  background-color: #6c757d;
}

.task-item.high .task-priority-indicator {
  background-color: #dc3545;
}

.task-item.medium .task-priority-indicator {
  background-color: #f0ad4e;
}

.task-item.low .task-priority-indicator {
  background-color: #6c757d;
}

.task-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.task-type {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.task-type.vip {
  background-color: rgba(240, 173, 78, 0.1);
  color: #f0ad4e;
}

.task-type.checkout {
  background-color: rgba(13, 110, 253, 0.1);
  color: #0d6efd;
}

.task-type.service {
  background-color: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.task-type.complaint {
  background-color: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.task-type.cleaning {
  background-color: rgba(23, 162, 184, 0.1);
  color: #17a2b8;
}

.task-type.maintenance {
  background-color: rgba(108, 117, 125, 0.1);
  color: #6c757d;
}

.task-type.meal {
  background-color: rgba(111, 66, 193, 0.1);
  color: #6f42c1;
}

.task-type-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-priority {
  font-size: 12px;
  color: #6c757d;
}

.task-id {
  font-size: 12px;
  color: #adb5bd;
}

.task-content {
  margin-bottom: 12px;
}

.task-description {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 5px;
}

.task-details {
  font-size: 13px;
  color: #6c757d;
}

.task-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #6c757d;
}

.info-icon {
  color: #0d6efd;
}

.info-item.deadline {
  color: #28a745;
}

.info-item.deadline.overdue {
  color: #dc3545;
  font-weight: 500;
}

.task-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eaedf3;
}

.status-indicator {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-indicator.pending {
  background-color: rgba(240, 173, 78, 0.1);
  color: #f0ad4e;
}

.status-indicator.processing {
  background-color: rgba(13, 110, 253, 0.1);
  color: #0d6efd;
}

.status-indicator.completed {
  background-color: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.task-actions {
  display: flex;
  gap: 8px;
}

.task-action-btn {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.task-action-btn.start-btn {
  background-color: #0d6efd;
  color: white;
}

.task-action-btn.assign-btn {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.task-action-btn.complete-btn {
  background-color: #28a745;
  color: white;
}

.task-action-btn.issue-btn {
  background-color: #f0ad4e;
  color: white;
}

.task-action-btn.more-btn {
  width: 30px;
  height: 24px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.task-completed-info {
  font-size: 11px;
  color: #28a745;
  display: flex;
  flex-direction: column;
}

.tasks-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>