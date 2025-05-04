<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { reservationApi } from '@/api'
import {
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElTag,
  ElSkeleton,
  ElEmpty,
  ElMessage
} from 'element-plus'

// --- 辅助函数 --- 
// 格式化日期 (YYYY-MM-DD)
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) {
      return '无效日期';
    }
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch (e) {
    console.error('Error formatting date:', dateStr, e);
    return '格式化错误';
  }
}

// 格式化日期时间 (YYYY-MM-DD HH:mm)
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  try {
    const date = new Date(dateTimeStr);
    if (isNaN(date.getTime())) {
      return '无效日期时间';
    }
    const datePart = formatDate(dateTimeStr);
    if (datePart.includes('无效') || datePart.includes('错误')) {
      return datePart;
    }
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${datePart} ${hours}:${minutes}`;
  } catch (e) {
    console.error('Error formatting datetime:', dateTimeStr, e);
    return '格式化错误';
  }
}

// 获取预订状态对应的类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'primary',
    'checked-in': 'success',
    completed: 'info',
    cancelled: 'danger'
  };
  return statusMap[status ? status.toLowerCase() : ''];
}

// 获取预订状态对应的文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    'checked-in': '已入住',
    completed: '已完成',
    cancelled: '已取消'
  };
  return statusMap[status ? status.toLowerCase() : ''] || '未知状态';
}
// --- 结束辅助函数 ---

const route = useRoute()
const bookingDetail = ref(null)
const isLoading = ref(true)
const errorMsg = ref('')

// 使用 computed 确保响应性
const bookingId = computed(() => route.params.id)

const fetchBookingDetail = async () => {
  isLoading.value = true
  errorMsg.value = ''
  bookingDetail.value = null // 重置详情

  if (!bookingId.value) {
      errorMsg.value = '无效的预订ID'
      isLoading.value = false
      return
  }

  try {
    // 修改为调用新的 API 函数
    const res = await reservationApi.getReservationDetail(bookingId.value)

    // 检查响应结构并直接使用返回的数据
    if (res && res.success && res.data) {
      bookingDetail.value = res.data // 直接赋值 DTO 对象
      // 检查数据是否包含所需字段 (调试用)
      console.log('Fetched booking detail:', bookingDetail.value);
      if (!bookingDetail.value.roomCount) {
        console.warn('Fetched booking detail is missing roomCount.');
      }
      if (!bookingDetail.value.contactName) {
        console.warn('Fetched booking detail is missing contactName.');
      }
      if (!bookingDetail.value.contactPhone) {
        console.warn('Fetched booking detail is missing contactPhone.');
      }
    } else {
      errorMsg.value = res?.message || '获取预订详情失败'
      console.error('Failed to fetch booking detail or invalid response structure:', res);
      if (res && res.code === 404) { // 假设后端对未找到返回 404 或类似代码
          errorMsg.value = '未找到指定的预订记录';
      }
    }
  } catch (error) {
    console.error('Error fetching booking details:', error)
    errorMsg.value = error?.message || '加载预订详情时发生错误'
    // 根据错误类型显示更友好的消息
    if (error?.response?.status === 404) {
        errorMsg.value = '未找到指定的预订记录';
    } else if (error?.message) {
        errorMsg.value = `加载失败: ${error.message}`;
    } else {
        errorMsg.value = '加载预订详情时发生未知错误';
    }
    ElMessage.error(errorMsg.value); // 在 catch 中显示错误消息可能更及时
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchBookingDetail()
})

</script>

<template>
  <el-card class="booking-detail-card">
    <template #header>
      <div class="card-header">
        <span>预订详情</span>
      </div>
    </template>

    <div v-if="isLoading" class="loading-state">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="errorMsg" class="error-state">
      <el-empty :description="errorMsg" />
    </div>

    <div v-else-if="bookingDetail" class="detail-content">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预订号">{{ bookingDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(bookingDetail.status)">
            {{ getStatusText(bookingDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="房间类型">{{ bookingDetail.roomTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间数量">{{ bookingDetail.roomCount }} 间</el-descriptions-item>
        
        <el-descriptions-item label="入住日期">{{ formatDate(bookingDetail.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="离店日期">{{ formatDate(bookingDetail.checkOutTime) }}</el-descriptions-item>

        <el-descriptions-item label="总价">¥ {{ bookingDetail.totalPrice?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="预订时间">{{ formatDateTime(bookingDetail.createTime) }}</el-descriptions-item>
        
        <el-descriptions-item label="联系人姓名">{{ bookingDetail.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ bookingDetail.contactPhone || '-' }}</el-descriptions-item>

        <el-descriptions-item label="特殊要求" :span="2">
          {{ bookingDetail.specialRequests || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    
    <div v-else>
         <el-empty description="无法加载预订详情" />
    </div>

  </el-card>
</template>

<style scoped>
.booking-detail-card {
  margin: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.loading-state,
.error-state {
  padding: 40px 0;
}

.detail-content {
  padding: 10px 0;
}

/* 调整描述列表标签宽度 */
:deep(.el-descriptions__label) {
  width: 120px; /* 根据需要调整 */
  text-align: right;
  color: #666;
}

/* 调整描述列表内容宽度 */
:deep(.el-descriptions__content) {
  min-width: 200px; /* 确保内容区域有足够宽度 */
}
</style> 