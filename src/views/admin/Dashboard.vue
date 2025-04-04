<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 数据概览卡片 -->
      <el-col :span="6" v-for="card in dataCards" :key="card.title">
        <el-card class="data-card premium-card" :class="card.type" :body-style="{ padding: '0' }" @click="showCardDetails(card)" hover>
          <div class="card-content">
            <div class="card-icon-wrapper" :class="card.type">
              <el-icon class="card-icon">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value">{{ card.value }}</div>
              <div class="card-change" :class="card.trend">
                {{ card.change }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 入住率趋势图 -->
      <el-col :span="12">
        <el-card class="premium-chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">入住率趋势</span>
                <span class="header-subtitle">实时动态监控，优化房间配置</span>
              </div>
              <el-radio-group v-model="occupancyTimeRange" size="small" class="time-filter">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里将使用echarts绘制图表 -->
            <div ref="occupancyChart" style="height: 320px;"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 收入统计图 -->
      <el-col :span="12">
        <el-card class="premium-chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">收入统计</span>
                <span class="header-subtitle">财务分析与预测</span>
              </div>
              <el-radio-group v-model="revenueTimeRange" size="small" class="time-filter">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里将使用echarts绘制图表 -->
            <div ref="revenueChart" style="height: 320px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 房间状态统计 -->
      <el-col :span="12">
        <el-card class="premium-chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">房间状态统计</span>
                <span class="header-subtitle">实时房态管理</span>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div ref="roomStatusChart" style="height: 320px;"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 清洁任务完成情况 -->
      <el-col :span="12">
        <el-card class="premium-chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">清洁任务完成情况</span>
                <span class="header-subtitle">服务质量监控</span>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div ref="cleaningTaskChart" style="height: 320px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据对话框 -->
    <el-dialog 
      v-model="detailsVisible" 
      :title="currentCard?.title + '详细数据'" 
      width="70%"
      destroy-on-close
      class="premium-dialog"
    >
      <div v-if="currentCard?.type === 'success'" class="details-content">
        <el-descriptions title="入住率详情" :column="2" border>
          <el-descriptions-item label="今日入住率">{{ currentCard?.value }}</el-descriptions-item>
          <el-descriptions-item label="昨日入住率">78%</el-descriptions-item>
          <el-descriptions-item label="本周平均">82%</el-descriptions-item>
          <el-descriptions-item label="上周平均">75%</el-descriptions-item>
          <el-descriptions-item label="本月平均">80%</el-descriptions-item>
          <el-descriptions-item label="同比变化">{{ currentCard?.change }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="room-details">
          <h3>房间入住详情</h3>
          <el-table :data="occupancyDetails" border stripe>
            <el-table-column prop="roomType" label="房型" />
            <el-table-column prop="total" label="总数" />
            <el-table-column prop="occupied" label="已入住" />
            <el-table-column prop="available" label="可用" />
            <el-table-column prop="rate" label="入住率" />
          </el-table>
        </div>
      </div>
      
      <div v-if="currentCard?.type === 'primary'" class="details-content">
        <el-descriptions title="收入详情" :column="2" border>
          <el-descriptions-item label="本月收入">{{ currentCard?.value }}</el-descriptions-item>
          <el-descriptions-item label="上月收入">¥115,200</el-descriptions-item>
          <el-descriptions-item label="同比增长">{{ currentCard?.change }}</el-descriptions-item>
          <el-descriptions-item label="去年同期">¥98,500</el-descriptions-item>
          <el-descriptions-item label="本年累计">¥458,700</el-descriptions-item>
          <el-descriptions-item label="年度目标完成率">48%</el-descriptions-item>
        </el-descriptions>
        
        <div class="revenue-details">
          <h3>收入来源分析</h3>
          <el-table :data="revenueDetails" border stripe>
            <el-table-column prop="source" label="收入来源" />
            <el-table-column prop="amount" label="金额" />
            <el-table-column prop="percentage" label="占比" />
            <el-table-column prop="change" label="同比变化" />
          </el-table>
        </div>
      </div>
      
      <div v-if="currentCard?.type === 'warning'" class="details-content">
        <el-descriptions title="访客详情" :column="2" border>
          <el-descriptions-item label="今日访客">{{ currentCard?.value }}</el-descriptions-item>
          <el-descriptions-item label="昨日访客">230</el-descriptions-item>
          <el-descriptions-item label="本周累计">1,250</el-descriptions-item>
          <el-descriptions-item label="上周累计">1,180</el-descriptions-item>
          <el-descriptions-item label="同比变化">{{ currentCard?.change }}</el-descriptions-item>
          <el-descriptions-item label="平均停留时间">2.5小时</el-descriptions-item>
        </el-descriptions>
        
        <div class="visitor-details">
          <h3>访客来源分析</h3>
          <el-table :data="visitorDetails" border stripe>
            <el-table-column prop="source" label="来源渠道" />
            <el-table-column prop="count" label="访客数" />
            <el-table-column prop="percentage" label="占比" />
            <el-table-column prop="conversion" label="转化率" />
          </el-table>
        </div>
      </div>
      
      <div v-if="currentCard?.type === 'info'" class="details-content">
        <el-descriptions title="预订详情" :column="2" border>
          <el-descriptions-item label="总预订数">{{ currentCard?.value }}</el-descriptions-item>
          <el-descriptions-item label="昨日预订">12</el-descriptions-item>
          <el-descriptions-item label="本周累计">35</el-descriptions-item>
          <el-descriptions-item label="上周累计">48</el-descriptions-item>
          <el-descriptions-item label="同比变化">{{ currentCard?.change }}</el-descriptions-item>
          <el-descriptions-item label="预订完成率">92%</el-descriptions-item>
        </el-descriptions>
        
        <div class="booking-details">
          <h3>预订详情列表</h3>
          <el-table :data="bookingDetails" border stripe>
            <el-table-column prop="id" label="预订号" width="100" />
            <el-table-column prop="customer" label="客户" />
            <el-table-column prop="roomType" label="房型" />
            <el-table-column prop="checkIn" label="入住日期" />
            <el-table-column prop="nights" label="入住天数" />
            <el-table-column prop="amount" label="金额" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === '已确认' ? 'success' : row.status === '待付款' ? 'warning' : 'info'">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { House, Money, User, Calendar } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 数据概览卡片数据
const dataCards = reactive([
  {
    title: '今日入住率',
    value: '85%',
    change: '↑ 5%',
    trend: 'up',
    icon: House,
    type: 'success'
  },
  {
    title: '本月收入',
    value: '¥128,500',
    change: '↑ 12%',
    trend: 'up',
    icon: Money,
    type: 'primary'
  },
  {
    title: '访客数量',
    value: '256',
    change: '↑ 8%',
    trend: 'up',
    icon: User,
    type: 'warning'
  },
  {
    title: '预订数量',
    value: '45',
    change: '↓ 3%',
    trend: 'down',
    icon: Calendar,
    type: 'info'
  }
])

// 详细数据对话框
const detailsVisible = ref(false)
const currentCard = ref(null)

// 详细数据
const occupancyDetails = ref([
  { roomType: '豪华大床房', total: 40, occupied: 35, available: 5, rate: '87.5%' },
  { roomType: '行政套房', total: 25, occupied: 21, available: 4, rate: '84%' },
  { roomType: '家庭套房', total: 35, occupied: 30, available: 5, rate: '85.7%' }
])

const revenueDetails = ref([
  { source: '房费收入', amount: '¥98,500', percentage: '76.7%', change: '↑ 10%' },
  { source: '餐饮收入', amount: '¥18,000', percentage: '14.0%', change: '↑ 15%' },
  { source: 'SPA服务', amount: '¥8,500', percentage: '6.6%', change: '↑ 20%' },
  { source: '会议室租赁', amount: '¥3,500', percentage: '2.7%', change: '↓ 5%' }
])

const visitorDetails = ref([
  { source: '直接访问', count: 120, percentage: '46.9%', conversion: '35%' },
  { source: '搜索引擎', count: 80, percentage: '31.3%', conversion: '28%' },
  { source: '社交媒体', count: 40, percentage: '15.6%', conversion: '20%' },
  { source: '邮件推广', count: 16, percentage: '6.2%', conversion: '40%' }
])

const bookingDetails = ref([
  { id: 'B2404001', customer: '张三', roomType: '豪华套房', checkIn: '2024-04-05', nights: 2, amount: '¥2,680', status: '已确认' },
  { id: 'B2404002', customer: '李四', roomType: '商务套房', checkIn: '2024-04-06', nights: 3, amount: '¥2,940', status: '待付款' },
  { id: 'B2404003', customer: '王五', roomType: '标准双人间', checkIn: '2024-04-04', nights: 1, amount: '¥680', status: '已确认' },
  { id: 'B2404004', customer: '赵六', roomType: '标准单人间', checkIn: '2024-04-07', nights: 4, amount: '¥2,160', status: '已取消' },
  { id: 'B2404005', customer: '钱七', roomType: '豪华套房', checkIn: '2024-04-10', nights: 2, amount: '¥2,680', status: '已确认' }
])

// 显示卡片详细数据
const showCardDetails = (card) => {
  currentCard.value = card
  detailsVisible.value = true
}

// 图表时间范围选择
const occupancyTimeRange = ref('week')
const revenueTimeRange = ref('week')

// 图表引用
const occupancyChart = ref(null)
const revenueChart = ref(null)
const roomStatusChart = ref(null)
const cleaningTaskChart = ref(null)

// 初始化图表
onMounted(() => {
  // 入住率趋势图
  const occupancyChartInstance = echarts.init(occupancyChart.value)
  occupancyChartInstance.setOption({
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [{
      data: [80, 85, 75, 90, 95, 88, 85],
      type: 'line',
      smooth: true
    }]
  })

  // 收入统计图
  const revenueChartInstance = echarts.init(revenueChart.value)
  revenueChartInstance.setOption({
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [{
      data: [15000, 18000, 16000, 20000, 25000, 28000, 22000],
      type: 'bar'
    }]
  })

  // 房间状态统计图
  const roomStatusChartInstance = echarts.init(roomStatusChart.value)
  roomStatusChartInstance.setOption({
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 60, name: '已入住' },
        { value: 20, name: '待清洁' },
        { value: 15, name: '空闲' },
        { value: 5, name: '维护中' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })

  // 清洁任务完成情况图
  const cleaningTaskChartInstance = echarts.init(cleaningTaskChart.value)
  cleaningTaskChartInstance.setOption({
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['已完成', '进行中', '待处理']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [25, 8, 5],
      type: 'bar',
      showBackground: true,
      backgroundStyle: {
        color: 'rgba(180, 180, 180, 0.2)'
      }
    }]
  })

  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    occupancyChartInstance.resize()
    revenueChartInstance.resize()
    roomStatusChartInstance.resize()
    cleaningTaskChartInstance.resize()
  })
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px 0;
}

/* 高级卡片样式 */
.premium-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  height: 120px;
}

.premium-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.premium-chart-card {
  border-radius: 12px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 100%;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.6));
}

.card-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  border-radius: 15px;
  margin-right: 15px;
  background: rgba(0, 0, 0, 0.1);
}

.card-icon-wrapper.success {
  background: linear-gradient(135deg, #36d1dc, #5b86e5);
}

.card-icon-wrapper.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.card-icon-wrapper.warning {
  background: linear-gradient(135deg, #f6d365, #fda085);
}

.card-icon-wrapper.info {
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
}

.card-icon {
  font-size: 24px;
  color: white;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
  font-family: 'Arial', sans-serif;
}

.card-change {
  font-size: 13px;
  display: flex;
  align-items: center;
}

.card-change.up {
  color: #67c23a;
}

.card-change.down {
  color: #f56c6c;
}

.premium-card.success {
  background: linear-gradient(to right, rgba(54, 209, 220, 0.1), rgba(91, 134, 229, 0.1));
}

.premium-card.primary {
  background: linear-gradient(to right, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
}

.premium-card.warning {
  background: linear-gradient(to right, rgba(246, 211, 101, 0.1), rgba(253, 160, 133, 0.1));
}

.premium-card.info {
  background: linear-gradient(to right, rgba(161, 196, 253, 0.1), rgba(194, 233, 251, 0.1));
}

.chart-row {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-subtitle {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.time-filter {
  background-color: #f5f7fa;
  border-radius: 15px;
  padding: 2px;
}

.chart-container {
  padding: 15px;
  position: relative;
}

.premium-dialog .el-dialog__header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.premium-dialog .el-dialog__body {
  padding: 20px;
}

.details-content h3 {
  margin-top: 30px;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 3px solid #5b86e5;
  padding-left: 10px;
}

.room-details, .revenue-details, .visitor-details, .booking-details {
  margin-top: 20px;
}

.room-details h3, .revenue-details h3, .visitor-details h3, .booking-details h3 {
  margin-bottom: 15px;
  font-weight: 500;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .el-col {
    width: 100% !important;
  }
  
  .premium-card {
    margin-bottom: 15px;
  }
}
</style>