<template>
  <div class="checkout-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">退房管理</span></h2>
        <p class="header-description">管理客人退房、房间状态变更和账单结算</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="add-btn" @click="handleAddCheckout">
          <el-icon><Plus /></el-icon>新增退房
        </el-button>
        <el-button type="success" class="card-checkout-btn" @click="handleCardCheckout">
          <el-icon><CreditCard /></el-icon>刷卡退房
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-form-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房间号">
          <el-input v-model="searchForm.roomNumber" placeholder="请输入房间号" clearable />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.guestName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="退房日期">
          <el-date-picker
            v-model="searchForm.checkoutDate"
            type="date"
            placeholder="选择退房日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
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

    <!-- 数据统计卡片 -->
    <el-row :gutter="24" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="status-card status-card-today">
          <div class="status-content">
            <div class="status-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-count">{{ stats.todayCheckouts }}</div>
              <div class="status-title">今日退房</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="status-card status-card-cleaning">
          <div class="status-content">
            <div class="status-icon">
              <el-icon><Brush /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-count">{{ stats.pendingCleanings }}</div>
              <div class="status-title">待清洁房间</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="status-card status-card-month">
          <div class="status-content">
            <div class="status-icon">
              <el-icon><Histogram /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-count">{{ stats.monthlyCheckouts }}</div>
              <div class="status-title">本月退房</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 退房记录表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><List /></el-icon> 退房记录</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="checkoutRecords"
        style="width: 100%"
        border
        stripe
        highlight-current-row
        class="checkout-table"
      >
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="guestName" label="客户姓名" width="120" />
        <el-table-column prop="checkoutDate" label="退房日期" width="120" />
        <el-table-column prop="checkoutTime" label="退房时间" width="120" />
        <el-table-column prop="stayDuration" label="入住时长" width="120" />
        <el-table-column prop="totalAmount" label="消费总额" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" class="status-tag">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="primary"
                size="small"
                @click="handleConfirmCheckout(scope.row)"
                class="action-btn"
              >
                确认退房
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="handleViewDetails(scope.row)"
                class="action-btn"
              >
                查看详情
              </el-button>
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
        />
      </div>
    </el-card>

    <!-- 退房表单对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="退房登记"
      width="600px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="checkoutFormRef"
        :model="checkoutForm"
        :rules="checkoutRules"
        label-width="100px"
        class="checkout-form"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-select
            v-model="checkoutForm.roomNumber"
            placeholder="请选择房间号"
            filterable
            @change="handleRoomNumberChange"
            style="width: 100%"
          >
            <el-option
              v-for="room in occupiedRooms"
              :key="room.roomNumber"
              :label="`${room.roomNumber} (${room.roomType})`"
              :value="room.roomNumber"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="退房日期" prop="checkoutDate">
          <el-date-picker
            v-model="checkoutForm.checkoutDate"
            type="date"
            placeholder="选择退房日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退房时间" prop="checkoutTime">
          <el-time-picker
            v-model="checkoutForm.checkoutTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择退房时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="checkoutForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkoutDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCheckout">
            <el-icon><Check /></el-icon>确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 刷卡退房对话框 -->
    <el-dialog
      v-model="cardCheckoutDialogVisible"
      title="刷卡退房"
      width="500px"
      destroy-on-close
      class="custom-dialog"
    >
      <div class="card-checkout-container">
        <div class="card-reader-animation">
          <el-icon class="card-icon"><CreditCard /></el-icon>
          <div class="scanner-line" :class="{ scanning: isScanning }"></div>
        </div>
        <div class="card-checkout-text">
          <p>请在刷卡机上刷房卡</p>
          <p class="card-status-text">{{ cardStatusText }}</p>
        </div>
        <div class="card-checkout-actions">
          <el-button type="primary" @click="simulateCardScan" :loading="isScanning">
            模拟刷卡
          </el-button>
          <el-button @click="cardCheckoutDialogVisible = false">取消</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 退房详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="退房详情"
      width="600px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="房间号">{{ selectedRecord.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ selectedRecord.guestName }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ selectedRecord.checkinDate }}</el-descriptions-item>
        <el-descriptions-item label="退房日期">{{ selectedRecord.checkoutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住时长">{{ selectedRecord.stayDuration }}</el-descriptions-item>
        <el-descriptions-item label="消费总额">¥{{ selectedRecord.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(selectedRecord.status)" effect="light">
            {{ getStatusText(selectedRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ selectedRecord.remarks }}</el-descriptions-item>
      </el-descriptions>
      <div class="bill-detail" v-if="selectedRecord.id">
        <h4>账单明细</h4>
        <el-table :data="billDetails" border size="small" class="bill-table">
          <el-table-column prop="name" label="项目名称" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="scope">
              ¥{{ scope.row.amount }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Check, 
  Plus, 
  Refresh, 
  Search, 
  Calendar, 
  Brush, 
  Histogram, 
  List, 
  CreditCard 
} from '@element-plus/icons-vue'
import apiClient from '@/api'

// 数据统计
const stats = reactive({
  todayCheckouts: 0,
  pendingCleanings: 0,
  monthlyCheckouts: 0
})

// 搜索表单
const searchForm = reactive({
  roomNumber: '',
  guestName: '',
  checkoutDate: ''
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const loading = ref(false)
const checkoutRecords = ref([])

// 对话框控制
const checkoutDialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const cardCheckoutDialogVisible = ref(false)

// 刷卡相关状态
const isScanning = ref(false)
const cardStatusText = ref('等待刷卡...')

// 选中的记录
const selectedRecord = ref({})

// 已入住房间列表
const occupiedRooms = ref([])

// 账单明细
const billDetails = ref([])

// 退房表单
const checkoutFormRef = ref(null)
const checkoutForm = reactive({
  roomNumber: '',
  guestName: '',
  checkoutDate: '',
  checkoutTime: '',
  remarks: ''
})

// 表单验证规则
const checkoutRules = {
  roomNumber: [{ required: true, message: '请选择房间号', trigger: 'change' }],
  checkoutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  checkoutTime: [{ required: true, message: '请选择退房时间', trigger: 'change' }]
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    COMPLETED: 'success',
    CLEANING: 'primary'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待退房',
    COMPLETED: '已退房',
    CLEANING: '清洁中'
  }
  return statusMap[status] || status
}

// 加载数据统计
const loadStats = async () => {
  try {
    const response = await apiClient.get('/api/admin/checkout/stats')
    if (response.success) {
      Object.assign(stats, response.data)
    } else {
      ElMessage.error(response.message || '加载统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载退房记录
const loadCheckoutRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端分页从0开始
      pageSize: pageSize.value,
      roomNumber: searchForm.roomNumber,
      guestName: searchForm.guestName,
      checkoutDate: searchForm.checkoutDate
    }
    const response = await apiClient.get('/api/admin/checkout/records', { params })
    
    if (response.success) {
      checkoutRecords.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载退房记录失败')
    }
  } catch (error) {
    console.error('加载退房记录失败:', error)
    ElMessage.error('加载退房记录失败')
  } finally {
    loading.value = false
  }
}

// 加载已入住房间
const loadOccupiedRooms = async () => {
  try {
    const response = await apiClient.get('/api/admin/rooms/occupied')
    console.log('Occupied rooms fetched:', response.data);
    if (response.success) {
      occupiedRooms.value = response.data || []
    } else {
      ElMessage.error(response.message || '加载已入住房间失败')
    }
  } catch (error) {
    console.error('加载已入住房间失败:', error)
    ElMessage.error('加载已入住房间失败')
  }
}

// 加载账单明细
const loadBillDetails = async (checkoutId) => {
  try {
    const response = await apiClient.get(`/api/admin/checkout/${checkoutId}/bill`)
    if (response.success) {
      billDetails.value = response.data || []
    } else {
      billDetails.value = []
      ElMessage.error(response.message || '加载账单明细失败')
    }
  } catch (error) {
    console.error('加载账单明细失败:', error)
    ElMessage.error('加载账单明细失败')
    billDetails.value = []
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadCheckoutRecords()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 新增退房
const handleAddCheckout = async () => {
  // 获取当前日期和时间
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');

  const currentDate = `${year}-${month}-${day}`;
  const currentTime = `${hours}:${minutes}`;

  // 重置表单并设置默认值
  checkoutForm.roomNumber = '';
  checkoutForm.guestName = '';
  checkoutForm.checkoutDate = currentDate;
  checkoutForm.checkoutTime = currentTime;
  checkoutForm.remarks = '';

  // 先加载最新的已入住房间列表，完成后再显示对话框
  try {
    loading.value = true; // 可以添加一个加载状态
    await loadOccupiedRooms(); // 使用 await 等待加载完成
  } catch (error) {
    // 错误处理，例如提示用户加载失败
    ElMessage.error('加载可退房房间列表失败');
    return; // 加载失败则不打开对话框
  } finally {
    loading.value = false; // 结束加载状态
  }

  checkoutDialogVisible.value = true; // 确保在 loadOccupiedRooms 完成后执行
}

// 刷卡退房
const handleCardCheckout = () => {
  cardStatusText.value = '等待刷卡...'
  isScanning.value = false
  cardCheckoutDialogVisible.value = true
}

// 模拟刷卡
const simulateCardScan = async () => {
  if (isScanning.value) return
  
  isScanning.value = true
  cardStatusText.value = '正在读取卡片信息...'
  
  try {
    // 模拟刷卡延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟获取房间信息
    const response = await apiClient.get('/api/admin/rooms/random-occupied')
    if (!response.success) {
      throw new Error(response.message || '获取房间信息失败')
    }
    
    const roomData = response.data
    
    cardStatusText.value = `已识别房间: ${roomData.roomNumber}, 客人: ${roomData.guestName}`
    
    // 模拟处理延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 提交退房请求
    const checkoutResponse = await apiClient.post(`/api/admin/checkout/card-checkout`, {
      roomNumber: roomData.roomNumber
    })
    
    if (!checkoutResponse.success) {
      throw new Error(checkoutResponse.message || '退房处理失败')
    }
    
    isScanning.value = false
    cardStatusText.value = '退房成功！房间已设置为待清洁状态'
    
    ElMessage.success({
      message: `房间 ${roomData.roomNumber} 退房成功！`,
      duration: 3000
    })
    
    // 延迟关闭对话框
    setTimeout(() => {
      cardCheckoutDialogVisible.value = false
      loadCheckoutRecords()
      loadStats()
    }, 1500)
    
  } catch (error) {
    isScanning.value = false
    cardStatusText.value = '刷卡失败，请重试'
    console.error('刷卡退房失败:', error)
    ElMessage.error(error.message || '刷卡退房失败')
  }
}

// 确认退房
const handleConfirmCheckout = async (row) => {
  try {
    await ElMessageBox.confirm('确认办理退房吗？退房后房间状态将变更为待清洁。', '确认提示', {
      type: 'warning',
      confirmButtonText: '确认退房',
      cancelButtonText: '取消'
    })
    
    loading.value = true
    const response = await apiClient.post(`/api/admin/checkout/confirm/${row.id}`)
    
    if (!response.success) {
      throw new Error(response.message || '退房确认失败')
    }
    
    ElMessage.success({
      message: '退房办理成功，房间已设置为待清洁状态',
      duration: 3000
    })
    
    loadCheckoutRecords()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退房确认失败:', error)
      ElMessage.error(error.message || '退房确认失败')
    }
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetails = async (row) => {
  selectedRecord.value = row
  detailsDialogVisible.value = true
  
  if (row.id) {
    await loadBillDetails(row.id)
  }
}

// 提交退房
const submitCheckout = async () => {
  if (!checkoutFormRef.value) return
  
  try {
    await checkoutFormRef.value.validate()
    
    loading.value = true
    const response = await apiClient.post('/api/admin/checkout/create', checkoutForm)
    
    if (!response.success) {
      throw new Error(response.message || '退房登记失败')
    }
    
    ElMessage.success({
      message: '退房登记成功',
      duration: 3000
    })
    
    checkoutDialogVisible.value = false
    loadCheckoutRecords()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退房登记失败:', error)
      ElMessage.error(error.message || '退房登记失败')
    }
  } finally {
    loading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  loadCheckoutRecords()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadCheckoutRecords()
}

// 处理房间号选择变化
const handleRoomNumberChange = async (roomNumber) => {
  if (!roomNumber) {
    checkoutForm.guestName = '';
    return;
  }
}

// 页面加载时初始化数据
onMounted(() => {
  loadStats()
  loadCheckoutRecords()
  loadOccupiedRooms()
})
</script>

<style scoped>
.checkout-container {
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
  gap: 12px;
}

.add-btn, .card-checkout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.add-btn:hover, .card-checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.search-form-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.search-form {
  padding: 5px 0;
  display: flex;
  flex-wrap: wrap;
}

.search-btn, .reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stats-cards {
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

.status-card-today .status-icon {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.status-card-cleaning .status-icon {
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
}

.status-card-month .status-icon {
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

.table-card {
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

.checkout-table {
  border-radius: 8px;
  overflow: hidden;
}

.checkout-table :deep(th) {
  background-color: #f8fafc;
  color: #475569;
  font-weight: 600;
}

.status-tag {
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 6px 10px;
  border-radius: 6px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.custom-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #edf2f7;
  padding: 20px;
  margin: 0;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  color: #1e293b;
  font-size: 18px;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.custom-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid #edf2f7;
  padding: 20px;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.checkout-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
}

.checkout-form :deep(.el-input__wrapper),
.checkout-form :deep(.el-textarea__inner) {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.checkout-form :deep(.el-input__wrapper:hover),
.checkout-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}

.bill-detail {
  margin-top: 24px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 20px;
}

.bill-detail h4 {
  margin: 0 0 16px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
}

.bill-table {
  font-size: 14px;
}

.card-checkout-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.card-reader-animation {
  width: 200px;
  height: 160px;
  background-color: #f8fafc;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  position: relative;
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.card-icon {
  font-size: 48px;
  color: #0d6efd;
  z-index: 2;
}

.scanner-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, transparent, #0d6efd, transparent);
  transform: translateY(-100%);
}

.scanning {
  animation: scan 1.5s ease-in-out infinite;
}

@keyframes scan {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(160px);
  }
}

.card-checkout-text {
  text-align: center;
  margin-bottom: 24px;
}

.card-checkout-text p {
  margin: 8px 0;
  color: #475569;
}

.card-status-text {
  font-weight: 500;
  color: #0d6efd !important;
}

.card-checkout-actions {
  display: flex;
  gap: 12px;
}
</style> 