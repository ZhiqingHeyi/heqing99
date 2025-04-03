<template>
  <div class="user-bookings-container">
    <div class="page-header">
      <h2>我的预订</h2>
    </div>

    <el-tabs v-model="activeTab" class="booking-tabs">
      <el-tab-pane label="全部预订" name="all"></el-tab-pane>
      <el-tab-pane label="待确认" name="pending"></el-tab-pane>
      <el-tab-pane label="已确认" name="confirmed"></el-tab-pane>
      <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
      <el-tab-pane label="已完成" name="completed"></el-tab-pane>
    </el-tabs>

    <el-card>
      <el-table :data="bookingList" style="width: 100%" v-loading="loading">
        <el-table-column prop="bookingNo" label="预订号" width="120" />
        <el-table-column prop="roomType" label="房间类型" width="150" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="离店日期" width="120" />
        <el-table-column prop="totalPrice" label="总价" width="120">
          <template #default="{ row }">
            ¥{{ row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预订时间" width="180" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              link 
              @click="cancelBooking(row)"
              v-if="['pending', 'confirmed'].includes(row.status)"
            >取消</el-button>
            <el-button type="primary" link @click="showBookingDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 预订详情对话框 -->
    <el-dialog title="预订详情" v-model="dialogVisible" width="600px">
      <div v-if="currentBooking" class="booking-detail">
        <div class="detail-header">
          <el-tag :type="getStatusType(currentBooking.status)" size="large">
            {{ getStatusText(currentBooking.status) }}
          </el-tag>
          <h3>预订号: {{ currentBooking.bookingNo }}</h3>
        </div>

        <el-divider />

        <div class="detail-section">
          <h4>房间信息</h4>
          <ul class="detail-list">
            <li>
              <span class="detail-label">房间类型:</span>
              <span class="detail-value">{{ currentBooking.roomType }}</span>
            </li>
            <li>
              <span class="detail-label">价格:</span>
              <span class="detail-value">¥{{ currentBooking.price }}/晚</span>
            </li>
            <li>
              <span class="detail-label">入住日期:</span>
              <span class="detail-value">{{ currentBooking.checkInDate }}</span>
            </li>
            <li>
              <span class="detail-label">退房日期:</span>
              <span class="detail-value">{{ currentBooking.checkOutDate }}</span>
            </li>
            <li>
              <span class="detail-label">住宿天数:</span>
              <span class="detail-value">{{ currentBooking.days }}天</span>
            </li>
            <li>
              <span class="detail-label">总价:</span>
              <span class="detail-value price">¥{{ currentBooking.totalPrice }}</span>
            </li>
          </ul>
        </div>

        <el-divider />

        <div class="detail-section">
          <h4>联系人信息</h4>
          <ul class="detail-list">
            <li>
              <span class="detail-label">联系人:</span>
              <span class="detail-value">{{ currentBooking.contactName }}</span>
            </li>
            <li>
              <span class="detail-label">手机号码:</span>
              <span class="detail-value">{{ currentBooking.phone }}</span>
            </li>
            <li>
              <span class="detail-label">备注:</span>
              <span class="detail-value">{{ currentBooking.remarks || '无' }}</span>
            </li>
          </ul>
        </div>

        <el-divider />

        <div class="detail-section">
          <h4>订单信息</h4>
          <ul class="detail-list">
            <li>
              <span class="detail-label">预订时间:</span>
              <span class="detail-value">{{ currentBooking.createTime }}</span>
            </li>
            <li v-if="currentBooking.confirmTime">
              <span class="detail-label">确认时间:</span>
              <span class="detail-value">{{ currentBooking.confirmTime }}</span>
            </li>
            <li v-if="currentBooking.cancelTime">
              <span class="detail-label">取消时间:</span>
              <span class="detail-value">{{ currentBooking.cancelTime }}</span>
            </li>
          </ul>
        </div>

        <div class="actions">
          <el-button 
            type="danger" 
            @click="cancelCurrentBooking"
            v-if="['pending', 'confirmed'].includes(currentBooking.status)"
          >取消预订</el-button>
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentBooking = ref(null)

// 模拟预订数据
const allBookings = ref([
  {
    id: 1,
    bookingNo: 'B2023001',
    roomType: '豪华大床房',
    price: 688,
    days: 2,
    totalPrice: 1376,
    checkInDate: '2023-04-15',
    checkOutDate: '2023-04-17',
    contactName: '张三',
    phone: '13812345678',
    remarks: '希望安排高层房间',
    createTime: '2023-04-01 14:30:25',
    status: 'pending'
  },
  {
    id: 2,
    bookingNo: 'B2023002',
    roomType: '行政套房',
    price: 888,
    days: 2,
    totalPrice: 1776,
    checkInDate: '2023-05-01',
    checkOutDate: '2023-05-03',
    contactName: '张三',
    phone: '13812345678',
    remarks: '',
    createTime: '2023-04-02 09:15:33',
    confirmTime: '2023-04-02 10:20:15',
    status: 'confirmed'
  },
  {
    id: 3,
    bookingNo: 'B2023003',
    roomType: '总统套房',
    price: 1688,
    days: 2,
    totalPrice: 3376,
    checkInDate: '2023-03-10',
    checkOutDate: '2023-03-12',
    contactName: '张三',
    phone: '13812345678',
    remarks: '',
    createTime: '2023-03-01 16:45:12',
    confirmTime: '2023-03-01 17:30:45',
    status: 'completed'
  },
  {
    id: 4,
    bookingNo: 'B2023004',
    roomType: '豪华大床房',
    price: 688,
    days: 1,
    totalPrice: 688,
    checkInDate: '2023-02-15',
    checkOutDate: '2023-02-16',
    contactName: '张三',
    phone: '13812345678',
    remarks: '',
    createTime: '2023-02-10 11:20:33',
    cancelTime: '2023-02-12 14:25:17',
    status: 'cancelled'
  }
])

// 根据标签页筛选预订列表
const bookingList = computed(() => {
  if (activeTab.value === 'all') {
    return allBookings.value
  } else {
    return allBookings.value.filter(booking => booking.status === activeTab.value)
  }
})

// 获取预订状态对应的类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'success',
    cancelled: 'danger',
    completed: 'info'
  }
  return statusMap[status]
}

// 获取预订状态对应的文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    cancelled: '已取消',
    completed: '已完成'
  }
  return statusMap[status]
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

// 获取预订列表
const fetchBookingList = async () => {
  loading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    total.value = bookingList.value.length
    loading.value = false
  } catch (error) {
    console.error('获取预订列表失败:', error)
    loading.value = false
  }
}

// 取消预订
const cancelBooking = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    loading.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    row.status = 'cancelled'
    row.cancelTime = new Date().toLocaleString()
    ElMessage.success('预订已取消')
    loading.value = false
  } catch (error) {
    console.log('取消操作被取消或出错')
  }
}

// 显示预订详情
const showBookingDetail = (row) => {
  currentBooking.value = { ...row }
  dialogVisible.value = true
}

// 取消当前查看的预订
const cancelCurrentBooking = async () => {
  dialogVisible.value = false
  
  // 找到列表中对应的预订
  const booking = allBookings.value.find(item => item.id === currentBooking.value.id)
  if (booking) {
    await cancelBooking(booking)
  }
}

// 监听标签页变化
watch(activeTab, () => {
  fetchBookingList()
})

onMounted(() => {
  fetchBookingList()
})
</script>

<style scoped>
.user-bookings-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.booking-tabs {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.booking-detail {
  padding: 0 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.detail-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.detail-list li {
  margin-bottom: 10px;
  display: flex;
}

.detail-label {
  width: 100px;
  color: #666;
}

.detail-value {
  flex: 1;
  color: #333;
}

.detail-value.price {
  font-weight: 600;
  color: #f56c6c;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}
</style>
</rewritten_file>