<template>
  <div class="mobile-cleaning-records">
    <!-- 页面头部 -->
    <div class="mobile-header">
      <h2>清洁记录</h2>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <!-- 简化的筛选器 -->
    <div class="filter-section">
      <div class="filter-tabs">
        <div 
          class="tab-item" 
          :class="{ active: filterForm.status === '' }"
          @click="quickFilter('')"
        >全部</div>
        <div 
          class="tab-item" 
          :class="{ active: filterForm.status === 'COMPLETED' }"
          @click="quickFilter('COMPLETED')"
        >已完成</div>
        <div 
          class="tab-item" 
          :class="{ active: filterForm.status === 'IN_PROGRESS' }"
          @click="quickFilter('IN_PROGRESS')"
        >进行中</div>
      </div>
      
      <div class="date-filter">
        <el-date-picker
          v-model="filterForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          size="large"
        />
      </div>
    </div>

    <!-- 记录卡片列表 -->
    <div class="records-list" v-loading="loading">
      <div class="record-card" v-for="record in records" :key="record.id" @click="viewDetails(record)">
        <div class="record-header">
          <div class="room-number">{{ record.roomNumber }}</div>
          <div class="record-status" :class="record.status">
            {{ getStatusText(record.status) }}
          </div>
        </div>
        
        <div class="record-body">
          <div class="record-info">
            <div class="info-item">
              <div class="item-label">保洁员</div>
              <div class="item-value">{{ record.cleaner }}</div>
            </div>
            
            <div class="info-item">
              <div class="item-label">开始时间</div>
              <div class="item-value">{{ formatTime(record.startTime) }}</div>
            </div>
            
            <div class="info-item">
              <div class="item-label">完成时间</div>
              <div class="item-value">{{ formatTime(record.endTime) }}</div>
            </div>
            
            <div class="info-item">
              <div class="item-label">耗时</div>
              <div class="item-value highlight">{{ record.duration }}</div>
            </div>
          </div>
          
          <div class="record-remarks" v-if="record.remarks">
            <div class="remarks-label">备注：</div>
            <div class="remarks-content">{{ record.remarks }}</div>
          </div>
        </div>
        
        <div class="record-footer">
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="view-btn"
            @click.stop="viewDetails(record)"
          >查看详情</el-button>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="records.length === 0" class="empty-records">
        <el-empty description="暂无记录" :image-size="120">
          <div class="empty-tip">
            {{ filterForm.status ? `暂无${getStatusText(filterForm.status)}的清洁记录` : '暂无清洁记录' }}
          </div>
        </el-empty>
      </div>
    </div>
    
    <!-- 简化的分页 -->
    <div class="simple-pagination">
      <el-button 
        :disabled="currentPage <= 1" 
        @click="handlePrevPage" 
        round 
        plain
        size="large"
      >上一页</el-button>
      <div class="page-info">{{ currentPage }} / {{ totalPages }}</div>
      <el-button 
        :disabled="currentPage >= totalPages" 
        @click="handleNextPage" 
        round 
        plain
        size="large"
      >下一页</el-button>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="清洁记录详情"
      width="90%"
      class="mobile-dialog"
    >
      <div class="record-detail-card">
        <div class="detail-header">
          <div class="detail-room">房间 {{ currentRecord.roomNumber }}</div>
          <div class="detail-status" :class="currentRecord.status">
            {{ getStatusText(currentRecord.status) }}
          </div>
        </div>
        
        <div class="detail-section">
          <div class="detail-title">基本信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <div class="detail-label">保洁员</div>
              <div class="detail-value">{{ currentRecord.cleaner }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">耗时</div>
              <div class="detail-value highlight">{{ currentRecord.duration }}</div>
            </div>
            <div class="detail-item full-width">
              <div class="detail-label">开始时间</div>
              <div class="detail-value">{{ currentRecord.startTime }}</div>
            </div>
            <div class="detail-item full-width">
              <div class="detail-label">完成时间</div>
              <div class="detail-value">{{ currentRecord.endTime }}</div>
            </div>
          </div>
        </div>
        
        <div class="detail-section" v-if="currentRecord.remarks">
          <div class="detail-title">备注信息</div>
          <div class="detail-content">
            {{ currentRecord.remarks }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="detailsVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 刷新按钮 -->
    <div class="floating-action">
      <el-button type="primary" circle size="large" @click="refreshRecords" class="refresh-button">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { cleaningApi } from '@/api/index'

// 当前时间
const currentTime = ref('')
const updateCurrentTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

// 初始化当前时间并每分钟更新
onMounted(() => {
  updateCurrentTime()
  setInterval(updateCurrentTime, 60000)
})

// 表格加载状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  status: '',
  dateRange: []
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 记录数据
const records = ref([])

// 详情对话框
const detailsVisible = ref(false)
const currentRecord = ref({})

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'info',
    processing: 'warning',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待处理',
    ASSIGNED: '已分配',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    VERIFIED: '已验证'
  }
  return statusMap[status] || status
}

// 格式化时间 - 只显示时间部分
const formatTime = (datetime) => {
  if (!datetime) return '';
  // 如果包含日期和时间, 只返回时间部分
  if (datetime.includes(' ')) {
    return datetime.split(' ')[1].substring(0, 5); // 只保留小时和分钟
  }
  return datetime;
}

// 查询记录
const fetchRecords = async () => {
  loading.value = true
  try {
    // 构建请求参数
    const params = {}
    
    // 处理状态过滤
    if (filterForm.status) {
      params.status = filterForm.status
    }
    
    // 处理日期范围过滤
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    
    // 调用API获取记录
    const response = await cleaningApi.getCleaningRecords(params)
    
    if (response && response.success) {
      records.value = response.records || []
      total.value = response.total || 0
    } else {
      ElMessage.error('获取清洁记录失败')
    }
  } catch (error) {
    console.error('获取记录失败:', error)
    ElMessage.error(error.message || '获取记录失败')
  } finally {
    loading.value = false
  }
}

// 快速筛选
const quickFilter = (status) => {
  filterForm.status = status ? status.toUpperCase() : '';
  currentPage.value = 1;
  fetchRecords();
}

// 查看详情
const viewDetails = async (row) => {
  try {
    loading.value = true
    const response = await cleaningApi.getCleaningRecordById(row.id)
    
    if (response && response.success) {
      currentRecord.value = response.record || row
    } else {
      // 如果API调用失败，使用列表中的数据作为备选
      currentRecord.value = { ...row }
      ElMessage.warning('获取详情失败，显示基础信息')
    }
    
    detailsVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    // 如果出错，使用列表中的数据作为备选
  currentRecord.value = { ...row }
  detailsVisible.value = true
  } finally {
    loading.value = false
  }
}

// 刷新记录
const refreshRecords = () => {
  currentPage.value = 1
  fetchRecords()
}

// 分页控制
const handlePrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchRecords()
  }
}

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    fetchRecords()
  }
}

// 初始化
fetchRecords()
</script>

<style scoped>
/* 移动端优化样式 */
.mobile-cleaning-records {
  padding: 10px;
  max-width: 100%;
  background-color: #f5f5f5;
  min-height: 100vh;
  font-family: "Microsoft YaHei", Arial, sans-serif;
}

/* 页面头部 */
.mobile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 5px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.mobile-header h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
  font-weight: 600;
}

.current-time {
  font-size: 18px;
  font-weight: 500;
  color: #666;
}

/* 筛选部分 */
.filter-section {
  margin-bottom: 15px;
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-tabs {
  display: flex;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 5px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.tab-item.active {
  color: #c59d5f;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 40%;
  height: 3px;
  background-color: #c59d5f;
  border-radius: 3px;
}

.date-filter {
  margin-top: 15px;
}

/* 记录列表 */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.room-number {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}

.record-status {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 500;
}

.record-status.COMPLETED {
  background-color: #f0f9eb;
  color: #67c23a;
}

.record-status.IN_PROGRESS {
  background-color: #ebf5ff;
  color: #409eff;
}

.record-status.PENDING {
  background-color: #f4f4f5;
  color: #909399;
}

.record-body {
  padding: 15px;
}

.record-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.item-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.item-value {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.item-value.highlight {
  color: #c59d5f;
  font-weight: 600;
}

.record-remarks {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}

.remarks-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.remarks-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.5;
}

.record-footer {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}

.view-btn {
  font-size: 15px;
}

/* 空状态 */
.empty-records {
  padding: 40px 0;
  background: white;
  border-radius: 12px;
  margin-bottom: 15px;
}

.empty-tip {
  color: #909399;
  font-size: 15px;
  margin-top: 15px;
}

/* 简化分页 */
.simple-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 10px;
}

.page-info {
  font-size: 18px;
  color: #606266;
  font-weight: 500;
}

/* 浮动刷新按钮 */
.floating-action {
  position: fixed;
  right: 20px;
  bottom: 30px;
  z-index: 10;
}

.refresh-button {
  width: 60px;
  height: 60px;
  background-color: #c59d5f;
  border-color: #c59d5f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.refresh-button:hover,
.refresh-button:focus {
  background-color: #b58d40;
  border-color: #b58d40;
}

.refresh-button .el-icon {
  font-size: 24px;
}

/* 详情对话框 */
.record-detail-card {
  background: #fff;
  border-radius: 10px;
}

.detail-header {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-room {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.detail-status {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 500;
}

.detail-status.COMPLETED {
  background-color: #f0f9eb;
  color: #67c23a;
}

.detail-status.IN_PROGRESS {
  background-color: #ebf5ff;
  color: #409eff;
}

.detail-section {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #c59d5f;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-item.full-width {
  grid-column: span 2;
}

.detail-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.detail-value {
  font-size: 16px;
  color: #303133;
}

.detail-value.highlight {
  color: #c59d5f;
  font-weight: 600;
}

.detail-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  justify-content: center;
}

.mobile-dialog :deep(.el-dialog__title) {
  font-size: 20px;
}

@media screen and (max-width: 480px) {
  .record-info {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-item.full-width {
    grid-column: span 1;
  }
}
</style>