<template>
  <div class="page-container">
    <div class="background-image"></div>
    <div class="bookings-container">
      <div class="bookings-header">
        <h1>我的预订</h1>
        <p>查看和管理您的所有酒店预订</p>
    </div>

      <div class="bookings-content">
        <!-- 预订过滤器 -->
        <div class="filter-bar">
          <el-radio-group v-model="statusFilter" @change="filterBookings" class="filter-group">
            <el-radio-button label="all">全部预订</el-radio-button>
            <el-radio-button label="upcoming">即将到来</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="cancelled">已取消</el-radio-button>
          </el-radio-group>
          
          <el-input
            v-model="searchQuery"
            placeholder="搜索预订号或房间类型"
            clearable
            @input="filterBookings"
            class="search-input"
          >
            <template #prefix>
              <i class="el-icon-search"></i>
          </template>
          </el-input>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 预订列表 -->
        <div v-else-if="filteredBookings.length > 0" class="bookings-list">
          <el-card 
            v-for="booking in filteredBookings" 
            :key="booking.id" 
            class="booking-card"
            :class="{ 'upcoming': isUpcoming(booking), 'completed': isCompleted(booking), 'cancelled': isCancelled(booking) }"
          >
            <div class="booking-card-header">
              <div class="booking-id">预订号: #{{ booking.id }}</div>
              <div class="booking-status" :class="getStatusClass(booking.status)">
                {{ getStatusText(booking.status) }}
              </div>
            </div>
            
            <div class="booking-details">
              <div class="booking-room">
                <h3>{{ booking.room.roomType.name }}</h3>
                <div class="room-info">
                  <span>{{ booking.roomCount }}间</span>
                  <span class="price">¥{{ booking.totalPrice }}</span>
                </div>
        </div>

              <div class="booking-date">
                <div class="date-range">
                  <div class="check-date">
                    <div class="date-label">入住日期</div>
                    <div class="date-value">{{ formatDate(booking.checkInTime) }}</div>
                  </div>
                  <div class="divider">
                    <i class="el-icon-right"></i>
                  </div>
                  <div class="check-date">
                    <div class="date-label">退房日期</div>
                    <div class="date-value">{{ formatDate(booking.checkOutTime) }}</div>
                  </div>
                </div>
                <div class="booking-created">
                  预订于: {{ formatDateTime(booking.createTime) }}
                </div>
              </div>
        </div>

            <div class="booking-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewBookingDetails(booking.id)"
              >
                查看详情
              </el-button>
          <el-button 
                v-if="canCancel(booking)" 
            type="danger" 
                size="small" 
                @click="cancelBooking(booking.id)"
              >
                取消预订
              </el-button>
            </div>
          </el-card>
        </div>
        
        <!-- 无预订提示 -->
        <el-empty 
          v-else 
          description="您还没有预订记录" 
          :image-size="200"
          class="no-bookings"
        >
          <el-button type="primary" @click="goToBooking">立即预订</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const bookings = ref([])
const filteredBookings = ref([])
const loading = ref(true)
const statusFilter = ref('all')
const searchQuery = ref('')
const userId = localStorage.getItem('userId')

// 页面加载时获取用户预订
onMounted(async () => {
  if (!userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  await fetchUserBookings()
})

// 从后端获取预订数据
const fetchUserBookings = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/reservations/user/${userId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('userToken')}`
      }
    })
    
    const result = await response.json()
    
    if (result.success) {
      bookings.value = result.data
      filterBookings()
    } else {
      ElMessage.error('获取预订记录失败: ' + result.message)
    }
  } catch (error) {
    console.error('获取预订记录错误:', error)
    ElMessage.error('获取预订记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 过滤预订记录
const filterBookings = () => {
  let filtered = [...bookings.value]
  
  // 根据状态过滤
  if (statusFilter.value !== 'all') {
    if (statusFilter.value === 'upcoming') {
      filtered = filtered.filter(booking => 
        ['PENDING', 'CONFIRMED'].includes(booking.status)
      )
    } else if (statusFilter.value === 'completed') {
      filtered = filtered.filter(booking => 
        ['CHECKED_IN', 'COMPLETED'].includes(booking.status)
      )
    } else if (statusFilter.value === 'cancelled') {
      filtered = filtered.filter(booking => 
        booking.status === 'CANCELLED'
      )
    }
  }
  
  // 根据搜索关键词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(booking => 
      booking.id.toString().includes(query) || 
      booking.room.roomType.name.toLowerCase().includes(query)
    )
  }
  
  // 按创建时间倒序排序
  filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  
  filteredBookings.value = filtered
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${formatDate(dateTimeStr)} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取预订状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CHECKED_IN': '已入住',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 获取状态类名
const getStatusClass = (status) => {
  return `status-${status.toLowerCase()}`
}

// 判断预订状态
const isUpcoming = (booking) => ['PENDING', 'CONFIRMED'].includes(booking.status)
const isCompleted = (booking) => ['CHECKED_IN', 'COMPLETED'].includes(booking.status)
const isCancelled = (booking) => booking.status === 'CANCELLED'

// 判断是否可以取消预订
const canCancel = (booking) => {
  return ['PENDING', 'CONFIRMED'].includes(booking.status)
}

// 查看预订详情
const viewBookingDetails = (bookingId) => {
  router.push(`/user/booking/${bookingId}`)
}

// 取消预订
const cancelBooking = async (bookingId) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个预订吗？取消后将无法恢复。',
      '取消预订',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
      type: 'warning'
      }
    )
    
    const response = await fetch(`/api/reservations/${bookingId}/cancel`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('userToken')}`
      }
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success('预订已成功取消')
      // 重新获取最新预订列表
      await fetchUserBookings()
    } else {
      ElMessage.error('取消预订失败: ' + result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预订错误:', error)
      ElMessage.error('取消预订失败，请稍后重试')
    }
  }
}

// 跳转到预订页面
const goToBooking = () => {
  router.push('/booking')
}
</script>

<style scoped>
.page-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  padding: 60px 20px;
  font-family: "Microsoft YaHei", "SimSun", serif;
}

.background-image {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('../../assets/hotel3.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: brightness(0.7);
  z-index: -1;
}

.bookings-container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.bookings-header {
  text-align: center;
  color: #ffffff;
  margin-bottom: 30px;
}

.bookings-header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 2px;
  font-family: "Times New Roman", "SimSun", serif;
}

.bookings-header p {
  font-size: 1.2em;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  letter-spacing: 1px;
}

.bookings-content {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-group {
  flex-grow: 1;
}

.search-input {
  max-width: 300px;
}

.loading-container {
  padding: 20px;
}

.bookings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.booking-card {
  transition: all 0.3s ease;
}

.booking-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.booking-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.booking-id {
  font-weight: bold;
  color: #666;
}

.booking-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.9em;
  font-weight: 500;
}

.status-pending {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-confirmed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-checked_in {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-completed {
  background-color: #f9f0ff;
  color: #722ed1;
}

.status-cancelled {
  background-color: #fff1f0;
  color: #f5222d;
}

.booking-details {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.booking-room h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.room-info {
  color: #666;
  display: flex;
  gap: 10px;
}

.price {
  font-weight: bold;
  color: #ff4d4f;
}

.booking-date {
  text-align: right;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.date-label {
  font-size: 0.8em;
  color: #999;
}

.date-value {
  font-weight: 500;
}

.booking-created {
  font-size: 0.8em;
  color: #999;
}

.booking-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.no-bookings {
  padding: 40px 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
  }
  
  .search-input {
    max-width: 100%;
  }
  
  .booking-details {
    flex-direction: column;
    gap: 15px;
  }
  
  .booking-date {
    text-align: left;
  }
  
  .booking-actions {
    justify-content: flex-start;
  }
}
</style>