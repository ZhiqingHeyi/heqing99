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

    <!-- ADD BACK Room Status Dialog -->
    <el-dialog
      title="酒店房态一览"
      v-model="roomStatusVisible"
      width="90%"
      class="custom-dialog room-status-dialog"
      destroy-on-close
    >
      <!-- 临时使用内联简易版房态一览 -->
      <div class="simplified-room-status" v-loading="roomStatusLoading">
        <div class="status-header">
          <h3>房态一览</h3> 
        </div>
        
        <div class="room-stats-cards">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper" style="background-color: #28a745;">
                    <el-icon class="stat-icon"><Check /></el-icon>
                  </div>
                  <div class="stat-info">
                    <!-- UPDATE: Calculate available rooms -->
                    <div class="stat-value">{{ availableRoomCount }}</div>
                    <div class="stat-title">可用房间</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper" style="background-color: #dc3545;">
                    <el-icon class="stat-icon"><User /></el-icon>
                  </div>
                  <div class="stat-info">
                    <!-- UPDATE: Calculate occupied rooms -->
                    <div class="stat-value">{{ occupiedRoomCount }}</div>
                    <div class="stat-title">已入住</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper" style="background-color: #ffc107;">
                    <el-icon class="stat-icon"><Calendar /></el-icon>
                  </div>
                  <div class="stat-info">
                    <!-- TODO: Need data for reserved count -->
                    <div class="stat-value">?</div> 
                    <div class="stat-title">已预订</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-card-content">
                  <div class="stat-icon-wrapper" style="background-color: #6f42c1;">
                    <el-icon class="stat-icon"><Histogram /></el-icon>
                  </div>
                  <div class="stat-info">
                    <!-- UPDATE: Use computed occupancy rate -->
                    <div class="stat-value">{{ currentOccupancyRate }}%</div>
                    <div class="stat-title">入住率</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <div class="floor-selector">
          <span>选择楼层：</span>
          <el-radio-group v-model="selectedFloor">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="2">2层</el-radio-button>
            <el-radio-button label="3">3层</el-radio-button>
            <el-radio-button label="4">4层</el-radio-button>
            <el-radio-button label="5">5层</el-radio-button>
            <!-- TODO: Dynamically generate floors if needed -->
          </el-radio-group>
        </div>
        
        <!-- UPDATE Room Grid Loop -->
        <div class="simple-room-grid">
          <div v-if="!roomStatusList.length && !roomStatusLoading">
              <el-empty description="暂无房间数据" />
          </div>
          <div v-for="room in roomStatusList" :key="room.id" 
               class="simple-room-cell" 
               :class="getRoomStatusClass(room.status)"> 
            <div class="room-number">{{ room.roomNumber }}</div>
            <!-- Adjust room.roomType.name based on actual data structure -->
            <div class="room-type">{{ room.roomType?.name || '未知房型' }}</div> 
             <!-- Adjust room.price or room.roomType.price based on actual data structure -->
            <div class="room-price">¥{{ room.price || room.roomType?.basePrice || '-' }}</div>
            <div class="room-status-tag" :class="getRoomStatusClass(room.status)">
              {{ getRoomStatusText(room.status) }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, Edit, Check, Close, View, Key, Money, Picture, Calendar,
  Clock, User, Download, Right, House, List, Monitor, Connection, Message, Star,
  Histogram, Sugar
} from '@element-plus/icons-vue'
// ADD API Imports
import {
  fetchBookings,
  getBookingDetails,
  createBooking,
  updateBooking,
  confirmBooking,
  cancelBooking,
  checkInBooking,
  fetchTodayCheckinStats,
  fetchRooms,
  fetchDashboardStats,
  fetchPendingBookingCount
} from '@/api/reception';

// 搜索表单
const searchForm = reactive({
  bookingNo: '',
  customerName: '',
  phone: '',
  status: '',
  dateRange: null
})

// 统计数据 - Initialize with default values
const pendingCount = ref(0)
const todayCheckinCount = ref(0)
const occupancyRate = ref(0)
const totalRooms = ref(0) // Initialize, will be fetched
const occupiedRooms = ref(0) // Initialize, will be fetched

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

// 预订列表数据 - Initialize as empty
const loading = ref(false)
const bookingList = ref([]) // Initialize as empty array

// 分页 - Initialize total as 0
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0) // Initialize total as 0

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

// 可用房间列表 - Initialize as empty
const availableRooms = ref([]) // Initialize as empty array

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

// ADD fetchData function and related logic
const fetchData = async () => {
  loading.value = true;
  try {
    // Fetch bookings
    const bookingParams = {
      page: currentPage.value,
      pageSize: pageSize.value,
      status: searchForm.status || undefined,
      // roomType: searchForm.roomType || undefined, // Assuming search form might have roomType filter later
      guestName: searchForm.customerName || undefined,
      phone: searchForm.phone || undefined,
      bookingNo: searchForm.bookingNo || undefined,
      // Handle date range - convert to backend expected format if needed (e.g., ISO string)
      startDate: searchForm.dateRange ? searchForm.dateRange[0] : undefined,
      endDate: searchForm.dateRange ? searchForm.dateRange[1] : undefined,
    };
    // Remove undefined params
    Object.keys(bookingParams).forEach(key => bookingParams[key] === undefined && delete bookingParams[key]);
    
    const bookingRes = await fetchBookings(bookingParams);
    // Adjust based on actual API response structure
    bookingList.value = bookingRes.data?.data?.list || []; 
    total.value = bookingRes.data?.data?.total || 0;

    // Fetch statistics in parallel
    const [statsRes, todayCheckinRes, pendingCountRes] = await Promise.all([
      fetchDashboardStats(),
      fetchTodayCheckinStats(),
      fetchPendingBookingCount() // Use the helper function
    ]);

    // Update dashboard stats
    totalRooms.value = statsRes.data?.totalRooms || 0;
    occupiedRooms.value = statsRes.data?.occupiedRooms || 0;
    // Calculate occupancy rate
    occupancyRate.value = totalRooms.value > 0 
      ? Math.round((occupiedRooms.value / totalRooms.value) * 100) 
      : 0;

    // Update today checkin stats (adjust key based on actual API response)
    todayCheckinCount.value = todayCheckinRes.data?.data?.todayCheckIns || 0; 

    // Update pending count
    pendingCount.value = pendingCountRes; // Already processed in helper

  } catch (error) {
    console.error("Error fetching data:", error);
    ElMessage.error('获取数据失败，请稍后重试');
    // Reset data on error?
    bookingList.value = [];
    total.value = 0;
    pendingCount.value = 0;
    todayCheckinCount.value = 0;
    occupancyRate.value = 0;
    totalRooms.value = 0;
    occupiedRooms.value = 0;
  } finally {
    loading.value = false;
  }
};

// Initial data fetch
onMounted(() => {
  fetchData();
});

// UPDATE Search and Pagination handlers to call fetchData
const handleSearch = () => {
  currentPage.value = 1; // Reset page to 1 on new search
  fetchData();
};

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = '';
  });
  searchForm.dateRange = null;
  handleSearch(); // handleSearch will reset page and call fetchData
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchData();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchData();
};

// --- CRUD Operations Handlers ---

const handleAdd = () => {
  dialogType.value = 'add';
  // Reset form fields
  Object.assign(bookingForm, {
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
    // Add other fields if any
  });
  dialogVisible.value = true;
  // Clear validation state if formRef exists
  if (bookingFormRef.value) {
    bookingFormRef.value.clearValidate();
  }
};

const handleEdit = async (row) => {
  dialogType.value = 'edit';
  try {
    // Fetch full details if needed, or use list data
    // const detailsRes = await getBookingDetails(row.id); // Assuming booking list might lack some fields
    // const bookingData = detailsRes.data?.data || row;
    const bookingData = row; // Assuming list data is sufficient for now

    // Populate form - Adjust field names and data types as needed
    Object.assign(bookingForm, {
      id: bookingData.id, // Need ID for update
      customerName: bookingData.customerName,
      phone: bookingData.phone,
      idCard: bookingData.idCard, // Assuming this exists in data
      roomType: bookingData.roomType, // Assuming name is used. If ID needed, adjust
      dateRange: [bookingData.checkInDate, bookingData.checkOutDate], // Adjust format if needed
      roomCount: bookingData.roomCount, // Assuming this exists
      specialRequests: bookingData.specialRequests,
      isGroup: bookingData.isGroup,
      guestCount: bookingData.guestCount,
      estimatedArrivalTime: bookingData.estimatedArrivalTime, // Assuming this exists
      source: bookingData.source // Assuming this exists
    });
    dialogVisible.value = true;
    if (bookingFormRef.value) {
      bookingFormRef.value.clearValidate();
    }
  } catch (error) {;
    console.error("Error preparing edit form:", error);
    ElMessage.error('加载编辑数据失败');
  }
};

const handleSubmit = async () => {
  if (!bookingFormRef.value) return;
  await bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        // Prepare data for API - adjust field names/types
        const apiData = {
          ...bookingForm,
          userId: 1, // TODO: Get current logged-in user ID
          // Map roomType name to ID if backend expects ID
          // roomTypeId: getRoomTypeIdFromName(bookingForm.roomType),
          checkIn: bookingForm.dateRange ? bookingForm.dateRange[0].toISOString() : null,
          checkOut: bookingForm.dateRange ? bookingForm.dateRange[1].toISOString() : null,
          // Calculate totalAmount for creation
          totalAmount: dialogType.value === 'add' ? calculateTotalAmount(bookingForm) : undefined,
        };
        delete apiData.dateRange; // Remove original dateRange
        // Delete other fields not expected by backend

        if (dialogType.value === 'add') {
          await createBooking(apiData);
          ElMessage.success('新增预订成功');
        } else {
          // Assuming updateBooking requires ID in the path and data in body
          await updateBooking(bookingForm.id, apiData);
          ElMessage.success('更新预订成功');
        }
        dialogVisible.value = false;
        fetchData(); // Refresh data
      } catch (error) {
        console.error("Error saving booking:", error);
        ElMessage.error(error.response?.data?.message || '保存预订失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

// TODO: Implement helper function if needed
// const getRoomTypeIdFromName = (name) => { ... };

// TODO: Implement total amount calculation logic
const calculateTotalAmount = (form) => {
  // Basic example: needs room price logic
  const pricePerNight = 580; // Placeholder - fetch actual price based on roomType
  const nights = form.dateRange 
    ? (new Date(form.dateRange[1]) - new Date(form.dateRange[0])) / (1000 * 60 * 60 * 24) 
    : 0;
  return pricePerNight * nights * form.roomCount;
};

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要将预订号 ${row.bookingNo} 的状态更新为"已确认"吗?`, 
      '确认预订', 
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    loading.value = true;
    await confirmBooking(row.id);
    ElMessage.success('预订已确认');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Error confirming booking:", error);
      ElMessage.error(error.response?.data?.message || '确认预订失败');
    }
  } finally {
    loading.value = false;
  }
};

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要取消预订号 ${row.bookingNo} 吗? 此操作不可恢复。`, 
      '取消预订', 
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留预订',
        type: 'error',
      }
    );
    loading.value = true;
    await cancelBooking(row.id);
    ElMessage.success('预订已取消');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Error cancelling booking:", error);
      ElMessage.error(error.response?.data?.message || '取消预订失败');
    }
  } finally {
    loading.value = false;
  }
};

const currentBookingForCheckin = ref(null); // Store the booking being checked in

const handleCheckIn = async (row) => {
  currentBookingForCheckin.value = row; // Store the booking context
  // Reset check-in form
  Object.assign(checkInForm, {
    roomNumber: '',
    deposit: 500,
    remarks: ''
  });
  checkInVisible.value = true;
  availableRooms.value = []; // Clear previous list
  if (checkInFormRef.value) {
    checkInFormRef.value.clearValidate();
  }
  // Fetch available rooms for the booking's room type
  try {
    // TODO: Get roomTypeId from row.roomType (may need mapping)
    const roomTypeId = getRoomTypeIdFromName(row.roomType); // Placeholder
    if (!roomTypeId) throw new Error('无法确定房间类型ID');

    const roomParams = { status: 'AVAILABLE', roomTypeId: roomTypeId }; 
    const res = await fetchRooms(roomParams);
    availableRooms.value = res.data?.data?.list || [];
    if (!availableRooms.value.length) {
         ElMessage.warning('该房型当前无可用房间');
    }
  } catch (error) {
      console.error("Error fetching available rooms:", error);
      ElMessage.error('获取可用房间列表失败');
  }
};

const handleCheckInSubmit = async () => {
  if (!checkInFormRef.value) return;
  await checkInFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const apiData = {
          reservationId: currentBookingForCheckin.value.id,
          roomNumber: checkInForm.roomNumber,
          deposit: checkInForm.deposit,
          remarks: checkInForm.remarks,
          // Add other fields required by CheckInRecord if necessary
          guestName: currentBookingForCheckin.value.customerName,
          guestPhone: currentBookingForCheckin.value.phone,
          checkInTime: new Date().toISOString(), // Or use booking checkin time?
          expectedCheckOutTime: currentBookingForCheckin.value.checkOutDate, // Adjust format?
        };
        await checkInBooking(apiData);
        ElMessage.success('办理入住成功');
        checkInVisible.value = false;
        fetchData();
      } catch (error) {
        console.error("Error submitting check-in:", error);
        ElMessage.error(error.response?.data?.message || '办理入住失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

// TODO: Implement or fetch room type ID mapping
const roomTypeMap = ref({ // Placeholder - Should be fetched or configured
  '标准双人间': 2,
  '豪华大床房': 3,
  '行政套房': 4,
});
const getRoomTypeIdFromName = (name) => {
    return roomTypeMap.value[name];
};

// 查看详情 (Placeholder - can open a new dialog or route)
const handleView = (row) => {
  console.log('View booking:', row);
  // Option 1: Fetch details and show in a dialog
  // Option 2: Navigate to a details route: router.push(`/admin/booking/${row.id}`)
  ElMessage.info('查看功能待实现');
};

// 房型预览
const handleRoomPreview = (row) => {
  currentRoomType.value = row.roomType;
  roomPreviewVisible.value = true;
};

// Other handlers for quick actions (Placeholder)
const openTodayCheckin = () => { ElMessage.info('今日入住列表待实现'); };
const openTodayCheckout = () => { ElMessage.info('今日离店列表待实现'); };
const exportBookings = () => { ElMessage.info('导出数据功能待实现'); };
const showRoomStatus = async () => { 
  await fetchRoomStatusData(); // Fetch initial data (all floors)
  roomStatusVisible.value = true; 
};

// Quick Actions related functions (Placeholder if functionality needed beyond buttons)
const showPendingBookings = () => {
  searchForm.status = 'pending';
  handleSearch();
};
const showTodayCheckins = () => {
  // Needs specific logic/API or filter adjustment
  ElMessage.info('按今日入住筛选待实现');
};
const showOccupancyDetails = () => {
  ElMessage.info('显示入住详情待实现');
};

// Simplified room status for dialog (Keep as is for now)
const selectedFloor = ref('all');

// Helper to format date display if needed
const formatDate = (dateString) => {
  if (!dateString) return '-';
  try {
    // More robust date formatting
    const date = new Date(dateString);
    if (isNaN(date.getTime())) { // Check if date is valid
      return dateString;
    }
    return date.toLocaleDateString(); // Or use a specific format like 'yyyy-MM-dd'
  } catch (e) {
    console.error("Error formatting date:", dateString, e);
    return dateString; // Return original if parsing fails
  }
};

// Make sure to add formatDate where needed in template, e.g., for checkInDate/checkOutDate columns

// 移除支付相关方法
// const handlePayment = (row) => { ... };
// const handlePaymentSubmit = async () => { ... };

// 房态预览相关数据 (Ensure these are defined only once)
const viewType = ref('grid')
const selectedDate = ref(new Date())
// selectedFloor is defined above
const selectedRoomType = ref('all')
const selectedStatus = ref('all')
const roomStatusVisible = ref(false) // Ensure this is defined only once

// ADD Room Status State
const roomStatusList = ref([]);
const roomStatusLoading = ref(false);

// ADD Fetch Room Status Data function
const fetchRoomStatusData = async (floor = 'all') => {
  roomStatusLoading.value = true;
  try {
    const params = {
      // Assuming API needs a large size to get all, or implement pagination later
      pageSize: 1000, 
      page: 1 // Assuming 1-based page index
    };
    if (floor !== 'all') {
      params.floor = floor;
    }
    // Add other filters if needed, e.g., status
    
    // Clean params (remove undefined/null)
    Object.keys(params).forEach(key => (params[key] == null || params[key] === '') && delete params[key]);

    const res = await fetchRooms(params);
    // Adjust access based on actual API response structure for room list
    roomStatusList.value = res.data?.data?.list || []; 
    // Optionally, fetch total rooms count here if not available elsewhere reliably
    // totalRooms.value = res.data?.data?.total || 0; 

  } catch (error) {
    console.error("Error fetching room status data:", error);
    ElMessage.error('获取房态数据失败');
    roomStatusList.value = []; // Clear list on error
  } finally {
    roomStatusLoading.value = false;
  }
};

// ADD Watcher for selectedFloor
watch(selectedFloor, (newFloor) => {
  // Fetch data only if the dialog is visible or intended to be shown
  // This check might be optional depending on desired behavior
  if (roomStatusVisible.value) { 
      fetchRoomStatusData(newFloor);
  }
});

// ADD Computed properties for stats in dialog
const availableRoomCount = computed(() => 
  roomStatusList.value.filter(room => room.status === 'AVAILABLE').length
);
const occupiedRoomCount = computed(() => 
  roomStatusList.value.filter(room => room.status === 'OCCUPIED').length
);
// Use totalRooms from main stats, assuming it's accurate enough for dialog
const currentOccupancyRate = computed(() => 
  totalRooms.value > 0 
    ? Math.round((occupiedRoomCount.value / totalRooms.value) * 100) 
    : 0
);

// ADD Helper functions for room status display
const getRoomStatusClass = (status) => {
  // Assuming backend status matches these keys (adjust if needed)
  const statusMap = {
    AVAILABLE: 'available',
    OCCUPIED: 'occupied',
    RESERVED: 'reserved', // Assuming this status exists
    CLEANING: 'cleaning', // Assuming this status exists
    MAINTENANCE: 'maintenance' // Assuming this status exists
  };
  return statusMap[status] || 'unknown'; // Default class for unknown status
};

const getRoomStatusText = (status) => {
  const statusMap = {
    AVAILABLE: '可用',
    OCCUPIED: '已入住',
    RESERVED: '已预订',
    CLEANING: '清洁中',
    MAINTENANCE: '维护中'
  };
  return statusMap[status] || '未知状态';
};

// ... (The rest of the code if any)

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
  padding: 20px 0;
}

.detail-dialog-content h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #eee;
}

.detail-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
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
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
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
  border: 1px solid #eee;
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

.room-type-stats h4 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}

.room-type-table {
  width: 100%;
  border-collapse: collapse;
}

.room-type-table th, .room-type-table td {
  padding: 10px;
  text-align: center;
  border: 1px solid #eee;
}

.room-type-table th {
  background: #f8f9fa;
  font-weight: 600;
}

/* 今日待办样式 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.task-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eee;
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

.task-status-tag {
  background-color: #6c757d;
}

/* 房态一览样式 */
.room-status-dialog {
  min-width: 500px;
}

.room-status-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: 100%;
  overflow: hidden;
}

.room-status-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 80vh;
  overflow: auto;
}

.room-status-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #eaedf3;
  padding: 16px 20px;
}

:deep(.room-status-dialog .el-dialog) {
  margin: 0 !important;
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.room-status-dialog .el-dialog__body) {
  flex: 1;
  overflow: auto;
}

.simplified-room-status {
  padding: 20px;
}

.status-header {
  margin-bottom: 24px;
  text-align: center;
}

.status-header h3 {
  font-size: 24px;
  margin-bottom: 8px;
  color: #333;
}

.room-stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  border-radius: 8px;
  overflow: hidden;
}

.stat-card-content {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.floor-selector {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.simple-room-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.simple-room-cell {
  padding: 16px;
  border-radius: 8px;
  position: relative;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.3s;
}

.simple-room-cell:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.simple-room-cell.available {
  background-color: rgba(40, 167, 69, 0.1);
  border: 1px solid rgba(40, 167, 69, 0.2);
}

.simple-room-cell.occupied {
  background-color: rgba(220, 53, 69, 0.1);
  border: 1px solid rgba(220, 53, 69, 0.2);
}

.simple-room-cell.reserved {
  background-color: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.2);
}

.simple-room-cell.cleaning {
  background-color: rgba(23, 162, 184, 0.1);
  border: 1px solid rgba(23, 162, 184, 0.2);
}

.simple-room-cell.maintenance {
  background-color: rgba(108, 117, 125, 0.1);
  border: 1px solid rgba(108, 117, 125, 0.2);
}

.room-number {
  font-weight: 600;
  font-size: 18px;
}

.room-type {
  font-size: 14px;
  color: #666;
}

.room-price {
  font-size: 14px;
  font-weight: 500;
  color: #e6a23c;
}

.room-status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  color: white;
}

.room-status-tag.available {
  background-color: #28a745;
}

.room-status-tag.occupied {
  background-color: #dc3545;
}

.room-status-tag.reserved {
  background-color: #ffc107;
  color: #333;
}

.room-status-tag.cleaning {
  background-color: #17a2b8;
}

.room-status-tag.maintenance {
  background-color: #6c757d;
}
</style>