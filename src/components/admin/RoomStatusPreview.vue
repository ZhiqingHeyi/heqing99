<template>
  <div class="room-status-preview">
    <!-- 组件状态指示 -->
    <div class="status-indicator">组件已成功加载</div>
    
    <!-- 顶部控制栏 -->
    <div class="preview-header">
      <div class="header-left">
        <div class="header-title">
          <el-icon class="title-icon"><House /></el-icon>
          <h3>房态一览</h3>
        </div>
        <div class="header-subtitle">实时监控酒店房间状态，优化资源配置</div>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="viewType" size="small" class="view-type-selector">
          <el-radio-button label="grid">网格视图</el-radio-button>
          <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          size="small"
          :disabled-date="disablePastDates"
          @change="handleDateChange"
        />
      </div>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="(stat, index) in roomStats" :key="index">
          <el-card class="stat-card" :class="stat.type" shadow="hover">
            <div class="stat-card-content">
              <div class="stat-icon-wrapper" :class="stat.type">
                <el-icon class="stat-icon">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-title">{{ stat.title }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选器区域 -->
    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">楼层：</span>
        <el-radio-group v-model="selectedFloor" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button v-for="floor in floors" :key="floor" :label="floor">{{ floor }}层</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="filter-group">
        <span class="filter-label">房型：</span>
        <el-radio-group v-model="selectedRoomType" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button v-for="type in roomTypes" :key="type.value" :label="type.value">{{ type.label }}</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="filter-group">
        <span class="filter-label">状态：</span>
        <el-select v-model="selectedStatus" placeholder="选择状态" clearable size="small">
          <el-option label="全部" value="all" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value">
            <div class="status-option">
              <span class="status-dot" :class="item.value"></span>
              <span>{{ item.label }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
    </div>

    <!-- 网格视图 -->
    <div v-if="viewType === 'grid'" class="room-grid-view">
      <div v-for="floor in filteredFloors" :key="floor" class="floor-container">
        <div class="floor-header">
          <div class="floor-title">
            <span class="floor-label">{{ floor }}层</span>
            <el-tag size="small" type="info" effect="plain" class="floor-tag">{{ getRoomCountByFloor(floor) }}间房</el-tag>
          </div>
          <div class="floor-stats">
            <div class="floor-stat-item">
              <span class="stat-label">可用：</span>
              <span class="stat-value available">{{ getAvailableRoomCountByFloor(floor) }}</span>
            </div>
            <div class="floor-stat-item">
              <span class="stat-label">入住率：</span>
              <span class="stat-value">{{ getOccupancyRateByFloor(floor) }}%</span>
            </div>
          </div>
        </div>
        <div class="floor-rooms">
          <div 
            v-for="room in getFilteredRoomsByFloor(floor)" 
            :key="room.number" 
            class="room-cell" 
            :class="[room.status, {'highlight': isHighlighted(room)}]"
            @click="handleRoomClick(room)"
          >
            <div class="room-number">{{ room.number }}</div>
            <div class="room-type">{{ room.typeShort }}</div>
            <div class="room-price">¥{{ room.price }}</div>
            <div class="room-status-tag" :class="room.status">{{ getStatusText(room.status) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="room-list-view">
      <el-table 
        :data="filteredRooms" 
        style="width: 100%" 
        border 
        stripe 
        :max-height="600"
        class="room-table"
      >
        <el-table-column prop="number" label="房间号" width="100" sortable />
        <el-table-column prop="floor" label="楼层" width="80" sortable />
        <el-table-column prop="type" label="房型" width="150" sortable />
        <el-table-column prop="price" label="房价" width="100" sortable>
          <template #default="{ row }">
            <span class="price-tag">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="guest" label="客人信息" min-width="120">
          <template #default="{ row }">
            <span v-if="row.guest">{{ row.guest }}</span>
            <span v-else class="empty-value">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkIn" label="入住时间" width="180" sortable>
          <template #default="{ row }">
            <span v-if="row.checkIn">{{ row.checkIn }}</span>
            <span v-else class="empty-value">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkOut" label="离店时间" width="180" sortable>
          <template #default="{ row }">
            <span v-if="row.checkOut">{{ row.checkOut }}</span>
            <span v-else class="empty-value">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                link 
                size="small"
                @click="handleRoomAction(row, 'view')"
              >
                <el-icon><View /></el-icon>查看
              </el-button>
              <el-button 
                type="success" 
                link 
                size="small"
                v-if="row.status === 'available'"
                @click="handleRoomAction(row, 'checkin')"
              >
                <el-icon><Key /></el-icon>入住
              </el-button>
              <el-button 
                type="warning" 
                link 
                size="small"
                v-if="row.status === 'occupied'"
                @click="handleRoomAction(row, 'checkout')"
              >
                <el-icon><Right /></el-icon>退房
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 房间详情对话框 -->
    <el-dialog
      v-model="roomDetailVisible"
      :title="`房间详情 - ${selectedRoom.number}`"
      width="800px"
      destroy-on-close
      class="room-detail-dialog"
    >
      <div class="room-detail-content">
        <div class="room-detail-header">
          <div class="room-image-container">
            <el-carousel height="240px" indicator-position="outside" arrow="always" class="room-carousel">
              <el-carousel-item v-for="(image, index) in roomImages" :key="index">
                <img :src="image" alt="房间图片" class="room-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
          <div class="room-basic-info">
            <div class="info-group">
              <div class="info-item">
                <span class="info-label">房间号：</span>
                <span class="info-value">{{ selectedRoom.number }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">楼层：</span>
                <span class="info-value">{{ selectedRoom.floor }}层</span>
              </div>
            </div>
            
            <div class="info-group">
              <div class="info-item">
                <span class="info-label">房型：</span>
                <span class="info-value">{{ selectedRoom.type }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">状态：</span>
                <el-tag :type="getStatusTagType(selectedRoom.status)" effect="light">
                  {{ getStatusText(selectedRoom.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="info-group">
              <div class="info-item">
                <span class="info-label">房价：</span>
                <span class="info-value price">¥{{ selectedRoom.price }}/晚</span>
              </div>
              <div class="info-item">
                <span class="info-label">面积：</span>
                <span class="info-value">{{ getRoomArea(selectedRoom.type) }}㎡</span>
              </div>
            </div>
          </div>
        </div>

        <el-divider content-position="center">房间设施</el-divider>

        <div class="room-features">
          <div class="feature-item" v-for="(feature, index) in roomFeatures" :key="index">
            <el-icon class="feature-icon"><component :is="feature.icon" /></el-icon>
            <span>{{ feature.label }}</span>
          </div>
        </div>

        <div v-if="selectedRoom.status === 'occupied'" class="guest-info">
          <el-divider content-position="center">入住信息</el-divider>
          <el-descriptions :column="2" border size="large">
            <el-descriptions-item label="客人姓名">{{ selectedRoom.guest || '张三' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ selectedRoom.phone || '13800138000' }}</el-descriptions-item>
            <el-descriptions-item label="入住时间">{{ selectedRoom.checkIn || '2024-04-01 14:30' }}</el-descriptions-item>
            <el-descriptions-item label="预计离店">{{ selectedRoom.checkOut || '2024-04-03 12:00' }}</el-descriptions-item>
            <el-descriptions-item label="房间押金">¥{{ selectedRoom.deposit || '500' }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">已付款</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ selectedRoom.remarks || '无特殊要求' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="room-actions">
          <el-button 
            type="primary" 
            v-if="selectedRoom.status === 'available'"
            @click="handleRoomAction(selectedRoom, 'checkin')"
          >
            <el-icon><Key /></el-icon>办理入住
          </el-button>
          <el-button 
            type="warning" 
            v-if="selectedRoom.status === 'occupied'"
            @click="handleRoomAction(selectedRoom, 'checkout')"
          >
            <el-icon><Right /></el-icon>办理退房
          </el-button>
          <el-button 
            type="info" 
            v-if="selectedRoom.status === 'available'"
            @click="handleRoomAction(selectedRoom, 'maintenance')"
          >
            <el-icon><Tools /></el-icon>设置维护
          </el-button>
          <el-button 
            type="success" 
            v-if="selectedRoom.status === 'cleaning'"
            @click="handleRoomAction(selectedRoom, 'complete-cleaning')"
          >
            <el-icon><Check /></el-icon>完成清洁
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { 
  House, View, Key, Right, Tools, Check, User, Iphone, Refrigerator, 
  Monitor, Connection, Sugar, Suitcase, Umbrella, Smoking, MagicStick,
  Calendar, Checked, WarningFilled, Brush, Setting
} from '@element-plus/icons-vue'

// 视图类型
const viewType = ref('grid')

// 选择日期
const selectedDate = ref(new Date())

// 禁用过去的日期
const disablePastDates = (time) => {
  return time.getTime() < Date.now() - 86400000 // 禁用今天之前的日期
}

// 选择楼层
const selectedFloor = ref('all')

// 选择房型
const selectedRoomType = ref('all')

// 选择状态
const selectedStatus = ref('all')

// 楼层列表
const floors = ['1', '2', '3', '4', '5']

// 房型列表
const roomTypes = [
  { value: 'all', label: '全部房型' },
  { value: 'STA', label: '标准单人间' },
  { value: 'STW', label: '标准双人间' },
  { value: 'DEL', label: '豪华大床房' },
  { value: 'EXE', label: '行政套房' },
  { value: 'PRE', label: '总统套房' }
]

// 状态选项
const statusOptions = [
  { value: 'available', label: '可用' },
  { value: 'occupied', label: '已入住' },
  { value: 'reserved', label: '已预订' },
  { value: 'cleaning', label: '清洁中' },
  { value: 'maintenance', label: '维护中' }
]

// 房间数据
const rooms = ref([])

// 初始化房间数据
const initRooms = () => {
  const generatedRooms = []
  // 生成房间数据
  for (let floor of floors) {
    for (let i = 1; i <= 12; i++) {
      const roomNumber = `${floor}${String(i).padStart(2, '0')}`
      const typeIndex = Math.floor(i / 3) % roomTypes.length
      const roomType = roomTypes[typeIndex === 0 ? 1 : typeIndex]
      const statusIndex = Math.floor(Math.random() * statusOptions.length)
      const status = statusOptions[statusIndex].value
      
      const room = {
        number: roomNumber,
        floor: floor,
        type: roomType.label,
        typeShort: roomType.value,
        price: getRoomPrice(roomType.value),
        status: status,
        guest: status === 'occupied' ? getRandomGuest() : null,
        checkIn: status === 'occupied' ? getRandomDate(-5, 0) : null,
        checkOut: status === 'occupied' ? getRandomDate(1, 5) : null
      }
      
      generatedRooms.push(room)
    }
  }
  
  rooms.value = generatedRooms
}

// 获取随机客人姓名
const getRandomGuest = () => {
  const names = ['张先生', '王女士', '李先生', '赵女士', '陈先生', '刘女士']
  return names[Math.floor(Math.random() * names.length)]
}

// 获取随机日期
const getRandomDate = (minDays, maxDays) => {
  const today = new Date()
  const dayOffset = Math.floor(Math.random() * (maxDays - minDays + 1)) + minDays
  const date = new Date(today)
  date.setDate(date.getDate() + dayOffset)
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

// 获取房间价格
const getRoomPrice = (type) => {
  const priceMap = {
    'STA': 380,
    'STW': 480,
    'DEL': 680,
    'EXE': 980,
    'PRE': 1880
  }
  return priceMap[type] || 480
}

// 房间统计数据
const roomStats = computed(() => [
  {
    title: '可用房间',
    value: rooms.value.filter(room => room.status === 'available').length,
    type: 'available',
    icon: 'Checked'
  },
  {
    title: '已入住',
    value: rooms.value.filter(room => room.status === 'occupied').length,
    type: 'occupied',
    icon: 'User'
  },
  {
    title: '已预订',
    value: rooms.value.filter(room => room.status === 'reserved').length,
    type: 'reserved',
    icon: 'Calendar'
  },
  {
    title: '入住率',
    value: calculateOccupancyRate() + '%',
    type: 'occupancy',
    icon: 'WarningFilled'
  }
])

// 计算入住率
const calculateOccupancyRate = () => {
  const totalRooms = rooms.value.length
  const occupiedRooms = rooms.value.filter(room => room.status === 'occupied').length
  return Math.round((occupiedRooms / totalRooms) * 100)
}

// 筛选后的楼层
const filteredFloors = computed(() => {
  if (selectedFloor.value === 'all') return floors
  return [selectedFloor.value]
})

// 筛选后的房间
const filteredRooms = computed(() => {
  return rooms.value.filter(room => {
    // 楼层筛选
    if (selectedFloor.value !== 'all' && room.floor !== selectedFloor.value) return false
    
    // 房型筛选
    if (selectedRoomType.value !== 'all' && room.typeShort !== selectedRoomType.value) return false
    
    // 状态筛选
    if (selectedStatus.value !== 'all' && room.status !== selectedStatus.value) return false
    
    return true
  })
})

// 根据楼层获取房间
const getRoomsByFloor = (floor) => {
  return rooms.value.filter(room => room.floor === floor)
}

// 根据楼层获取筛选后的房间
const getFilteredRoomsByFloor = (floor) => {
  return filteredRooms.value.filter(room => room.floor === floor)
}

// 获取指定楼层的房间数量
const getRoomCountByFloor = (floor) => {
  return getRoomsByFloor(floor).length
}

// 获取指定楼层的可用房间数量
const getAvailableRoomCountByFloor = (floor) => {
  return getRoomsByFloor(floor).filter(room => room.status === 'available').length
}

// 获取指定楼层的入住率
const getOccupancyRateByFloor = (floor) => {
  const floorRooms = getRoomsByFloor(floor)
  const occupiedRooms = floorRooms.filter(room => room.status === 'occupied').length
  return Math.round((occupiedRooms / floorRooms.length) * 100)
}

// 获取指定状态的房间数量
const getRoomCountByStatus = (status) => {
  return rooms.value.filter(room => room.status === status).length
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'available': '可用',
    'occupied': '已入住',
    'reserved': '已预订',
    'cleaning': '清洁中',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'available': 'success',
    'occupied': 'danger',
    'reserved': 'warning',
    'cleaning': 'info',
    'maintenance': 'info'
  }
  return typeMap[status] || 'info'
}

// 房间详情对话框
const roomDetailVisible = ref(false)
const selectedRoom = ref({})

// 房间图片
const roomImages = [
  'https://via.placeholder.com/500x300.png?text=房间图片1',
  'https://via.placeholder.com/500x300.png?text=房间图片2',
  'https://via.placeholder.com/500x300.png?text=房间图片3'
]

// 获取房间面积
const getRoomArea = (type) => {
  const areaMap = {
    '标准单人间': 25,
    '标准双人间': 30,
    '豪华大床房': 40,
    '行政套房': 60,
    '总统套房': 120
  }
  return areaMap[type] || 30
}

// 房间设施
const roomFeatures = [
  { icon: 'User', label: '可住2人' },
  { icon: 'Monitor', label: '50寸智能电视' },
  { icon: 'Connection', label: '高速WiFi' },
  { icon: 'Sugar', label: '免费早餐' },
  { icon: 'Refrigerator', label: '迷你冰箱' },
  { icon: 'Umbrella', label: '24小时热水' },
  { icon: 'Suitcase', label: '行李寄存' },
  { icon: 'MagicStick', label: '每日客房清洁' }
]

// 处理房间点击
const handleRoomClick = (room) => {
  selectedRoom.value = { ...room }
  roomDetailVisible.value = true
}

// 处理房间操作
const handleRoomAction = (room, action) => {
  console.log(`对房间 ${room.number} 执行操作: ${action}`)
  // 这里可以根据不同的操作执行不同的逻辑
  // 例如：入住、退房、维护等
}

// 处理日期变更
const handleDateChange = (date) => {
  console.log('选择日期:', date)
  // 这里可以根据选择的日期重新加载房态数据
}

// 判断房间是否高亮显示
const isHighlighted = (room) => {
  return selectedStatus.value !== 'all' && room.status === selectedStatus.value
}

// 初始化
onMounted(() => {
  initRooms()
})
</script>

<style scoped>
.room-status-preview {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.status-indicator {
  background-color: #28a745;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  text-align: center;
  margin-bottom: 16px;
  font-weight: bold;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
}
</style>