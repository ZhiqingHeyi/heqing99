<template>
  <div class="dashboard-container">
    <!-- 管理员才能看到的内容 -->
    <div v-if="userRole === 'ADMIN'">
      <el-skeleton :rows="5" animated v-if="isLoading" />

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        :closable="false"
        class="error-alert"
      />

      <el-row :gutter="24" v-if="!isLoading && !error">
        <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="card in dataCards" :key="card.title">
          <el-card class="data-card luxury-card" :class="card.type" :body-style="{ padding: '0' }" hover shadow="hover">
            <div class="card-content">
              <div class="card-icon-wrapper" :class="card.type">
                <el-icon class="card-icon">
                  <component :is="card.icon" />
                </el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">{{ card.title }}</div>
                <div class="card-value">{{ card.value }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="24" class="chart-row">
        <!-- 入住率趋势图 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="luxury-chart-card">
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
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="luxury-chart-card">
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

      <el-row :gutter="24" class="chart-row">
        <!-- 房间状态统计 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="luxury-chart-card">
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
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="luxury-chart-card">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <span class="header-title">清洁和维护任务完成情况</span>
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
        class="luxury-dialog"
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
            <el-descriptions-item label="本月总收入">{{ currentCard?.value }}</el-descriptions-item>
            <el-descriptions-item label="上月总收入">¥{{ formatNumber(stats.lastMonthRevenue || 0) }}</el-descriptions-item>
            <el-descriptions-item label="本月入住收入">¥{{ formatNumber(stats.monthlyCheckInRevenue || 0) }}</el-descriptions-item>
            <el-descriptions-item label="上月入住收入">¥{{ formatNumber(stats.lastMonthCheckInRevenue || 0) }}</el-descriptions-item>
            <el-descriptions-item label="本月预订收入">¥{{ formatNumber(stats.monthlyReservationRevenue || 0) }}</el-descriptions-item>
            <el-descriptions-item label="上月预订收入">¥{{ formatNumber(stats.lastMonthReservationRevenue || 0) }}</el-descriptions-item>
            <el-descriptions-item label="同比增长">{{ currentCard?.change }}</el-descriptions-item>
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
    
    <!-- 前台接待人员的默认看板 -->
    <div v-else-if="userRole === 'receptionist'" class="role-dashboard">
      <el-empty description="前台接待人员请使用左侧菜单访问预订管理、入住登记和访客登记功能"></el-empty>
    </div>
    
    <!-- 保洁人员的默认看板 -->
    <div v-else-if="userRole === 'cleaner'" class="role-dashboard">
      <el-empty description="保洁人员请使用左侧菜单访问清洁任务和清洁记录功能"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { House, Money, User, Calendar } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { adminApi } from '../../api'

// 获取用户角色
const userRole = ref(localStorage.getItem('userRole') || 'admin')

// 状态变量
const isLoading = ref(false)
const error = ref(null)

// 数据概览卡片数据
const dataCards = ref([
  {
    title: '今日入住率',
    value: '0%',
    icon: 'TrendCharts',
    type: 'success'
  },
  {
    title: '本月收入',
    value: '¥0',
    icon: 'Money',
    type: 'primary'
  },
  {
    title: '用户数量',
    value: '0',
    icon: 'User',
    type: 'warning'
  },
  {
    title: '今日预订',
    value: '0',
    icon: 'Calendar',
    type: 'info'
  }
])

// 详细数据对话框
const detailsVisible = ref(false)
const currentCard = ref(null)

// 详细数据
const occupancyDetails = ref([
  { roomType: '标准间', total: 0, occupied: 0, available: 0, rate: '0%' },
  { roomType: '豪华间', total: 0, occupied: 0, available: 0, rate: '0%' },
  { roomType: '套房', total: 0, occupied: 0, available: 0, rate: '0%' },
  { roomType: '总统套房', total: 0, occupied: 0, available: 0, rate: '0%' }
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

// 图表时间范围选择
const occupancyTimeRange = ref('week')
const revenueTimeRange = ref('week')

// 图表引用
const occupancyChart = ref(null)
const revenueChart = ref(null)
const roomStatusChart = ref(null)
const cleaningTaskChart = ref(null)

// 图表实例
let occupancyChartInstance = null
let revenueChartInstance = null
let roomStatusChartInstance = null
let cleaningTaskChartInstance = null

// 图表数据
const chartData = {
  occupancy: {
    week: {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      data: [80, 85, 75, 90, 95, 88, 85]
    },
    month: {
      xAxis: ['1日', '5日', '10日', '15日', '20日', '25日', '30日'],
      data: [75, 82, 88, 92, 86, 78, 83]
    }
  },
  revenue: {
    week: {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      data: [15000, 18000, 16000, 20000, 25000, 28000, 22000]
    },
    month: {
      xAxis: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      data: [95000, 88000, 110000, 128500, 145000, 160000, 182000, 195000, 168000, 152000, 138000, 165000]
    }
  }
}

// 更新入住率图表 (仅作为备用方法，API失败时使用)
const updateOccupancyChart = () => {
  if (!occupancyChartInstance) return
  
  const data = chartData.occupancy[occupancyTimeRange.value]
  occupancyChartInstance.setOption({
    xAxis: {
      data: data.xAxis
    },
    series: [{
      data: data.data,
      type: 'line',
      smooth: true
    }]
  })
}

// 更新收入统计图表 (仅作为备用方法，API失败时使用)
const updateRevenueChart = () => {
  if (!revenueChartInstance) return
  
  const data = chartData.revenue[revenueTimeRange.value]
  
  // 计算平均值
  let avg = 0;
  if (data.data && data.data.length > 0) {
    avg = data.data.reduce((a, b) => a + b, 0) / data.data.length;
  }
  
  revenueChartInstance.setOption({
    xAxis: {
      data: data.xAxis
    },
    series: [{
      name: '总收入',
      data: data.data,
      type: 'bar',
      markLine: {
        data: [
          { 
            type: 'average', 
            name: '平均值',
            label: {
              formatter: '平均: ¥' + Math.round(avg).toLocaleString('zh-CN')
            }
          }
        ]
      }
    }]
  })
}

// 监听时间范围变化
watch(occupancyTimeRange, () => {
  fetchOccupancyTrendData();
})

watch(revenueTimeRange, () => {
  fetchRevenueStatsData();
})

// 获取入住率趋势数据
const fetchOccupancyTrendData = async () => {
  try {
    const response = await adminApi.getOccupancyTrend(occupancyTimeRange.value);
    
    // 更新图表数据
    if (occupancyChartInstance) {
      occupancyChartInstance.setOption({
        xAxis: {
          data: response.xAxis || []
        },
        series: [{
          data: response.data || [],
          type: 'line',
          smooth: true
        }]
      });
    }
  } catch (e) {
    console.error("获取入住率趋势数据失败:", e);
    // 使用模拟数据作为备用
    updateOccupancyChart();
  }
}

// 获取收入统计数据
const fetchRevenueStatsData = async () => {
  try {
    const response = await adminApi.getRevenueStats(revenueTimeRange.value);

    // 计算平均值
    let avg = 0;
    if (response.data && response.data.length > 0) {
      avg = response.data.reduce((a, b) => a + b, 0) / response.data.length;
    }

    // 更新图表数据
    if (revenueChartInstance) {
      revenueChartInstance.setOption({
        xAxis: {
          data: response.xAxis || []
        },
        series: [
          {
            name: '总收入',
            data: response.data || [],
            type: 'bar',
            markLine: {
              data: [
                { 
                  type: 'average', 
                  name: '平均值',
                  label: {
                    formatter: '平均: ¥' + Math.round(avg).toLocaleString('zh-CN')
                  }
                }
              ]
            }
          }
        ]
      });
    }
  } catch (e) {
    console.error("获取收入统计数据失败:", e);
    // 使用模拟数据作为备用
    updateRevenueChart();
  }
}

// 定义 fetchDashboardData 函数
const fetchDashboardData = async () => {
  try {
    isLoading.value = true
    error.value = ''
    
    // 加载仪表盘统计数据
    const stats = await adminApi.getDashboardStats()
    console.log('获取到仪表盘统计数据:', stats)
    
    if (!stats) {
      throw new Error('未能获取仪表盘数据')
    }
    
    // 更新数据卡片
    // 1. 入住率卡片
    const occupancyRate = stats.occupancyRate || 0
    const occupancyChange = stats.occupancyRateChange || 0
    dataCards.value[0].value = `${occupancyRate}%`
    
    // 2. 收入卡片
    const monthlyRevenue = stats.monthlyRevenue || 0
    const revenueChange = stats.revenueChange || 0
    dataCards.value[1].value = `¥${formatNumber(monthlyRevenue)}`
    
    // 更新收入明细
    const checkInRevenue = stats.monthlyCheckInRevenue || 0
    const reservationRevenue = stats.monthlyReservationRevenue || 0
    const lastMonthRevenue = stats.lastMonthRevenue || 0
    
    // 计算百分比
    const checkInPercentage = monthlyRevenue > 0 ? ((checkInRevenue / monthlyRevenue) * 100).toFixed(1) : '0.0'
    const reservationPercentage = monthlyRevenue > 0 ? ((reservationRevenue / monthlyRevenue) * 100).toFixed(1) : '0.0'
    
    // 更新收入明细表格
    revenueDetails.value = [
      { 
        source: '入住结算收入', 
        amount: `¥${formatNumber(checkInRevenue)}`, 
        percentage: `${checkInPercentage}%`,
        change: `${checkInRevenue > 0 ? '↑' : '↓'} ${Math.abs(stats.revenueChange || 0).toFixed(1)}%`
      },
      { 
        source: '预订收入', 
        amount: `¥${formatNumber(reservationRevenue)}`, 
        percentage: `${reservationPercentage}%`,
        change: `${reservationRevenue > 0 ? '↑' : '↓'} ${Math.abs(stats.revenueChange || 0).toFixed(1)}%` 
      }
    ]
    
    // 3. 用户数量卡片
    const totalUsers = stats.totalUsers || 0
    const newUsersThisMonth = stats.newUsersThisMonth || 0
    const userGrowthRate = stats.userGrowthRate || 0
    dataCards.value[2].value = formatNumber(totalUsers)
    
    // 4. 预订卡片
    const todayReservations = stats.todayReservations || 0
    const reservationsChange = stats.reservationsChange || 0
    dataCards.value[3].value = formatNumber(todayReservations)
    
    // 更新房间状态图表数据
    if (roomStatusChartInstance) {
      // 从API响应获取房间状态数据
      const roomStatusData = [
        { value: stats.occupiedRooms || 0, name: '已入住' },
        { value: stats.needsCleaningRooms || 0, name: '待清洁' },
        { value: stats.availableRooms || 0, name: '空闲可用' },
        { value: stats.maintenanceRooms || 0, name: '维护中' },
        { value: stats.cleaningRooms || 0, name: '清洁中' },
        { value: stats.reservedRooms || 0, name: '已预订' }
      ]
      
      // 过滤掉值为0的状态，避免图表显示空项
      const filteredRoomStatusData = roomStatusData.filter(item => item.value > 0)
      
      roomStatusChartInstance.setOption({
        series: [{
          data: filteredRoomStatusData
        }]
      })
    }
    
    // 更新清洁任务图表数据
    if (cleaningTaskChartInstance) {
      // 获取统计数据并记录日志
      const completedTasks = stats.cleaningTasksCompleted || 0;
      const inProgressTasks = stats.cleaningTasksInProgress || 0;
      const pendingTasks = stats.cleaningTasksPending || 0;
      const totalTasks = stats.cleaningTasksTotal || 0;
      
      console.log('更新清洁任务图表数据:');
      console.log('- 已完成任务:', completedTasks);
      console.log('- 进行中任务:', inProgressTasks);
      console.log('- 待处理任务:', pendingTasks);
      console.log('- 总记录数:', totalTasks);
      
      // 验证数据一致性
      const sumByStatus = completedTasks + inProgressTasks + pendingTasks;
      if (sumByStatus !== totalTasks) {
        console.warn('警告: 清洁任务数据不一致!');
        console.warn('- 状态总和:', sumByStatus);
        console.warn('- 记录总数:', totalTasks);
      }
      
      // 清除并重新设置图表
      cleaningTaskChartInstance.clear();
      
      cleaningTaskChartInstance.setOption({
        backgroundColor: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(249, 251, 255, 0.9)' },
            { offset: 1, color: 'rgba(245, 247, 250, 0.9)' }
          ]
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            return `<div style="font-weight:bold;margin-bottom:4px;">${params[0].name}</div>
                    <div>
                      <span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${params[0].color};"></span>
                      数量：${params[0].value}
                    </div>`;
          },
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderColor: '#eee',
          textStyle: {
            color: '#333'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '8%',
          top: '8%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['已完成', '进行中', '待处理'],
          axisLine: {
            lineStyle: {
              color: '#ddd'
            }
          },
          axisLabel: {
            color: '#555',
            fontWeight: 'bold',
            fontSize: 12,
            margin: 16
          },
          axisTick: {
            alignWithLabel: true,
            lineStyle: {
              color: '#ddd'
            }
          }
        },
        yAxis: {
          type: 'value',
          splitLine: {
            lineStyle: {
              color: '#eee',
              type: 'dashed'
            }
          },
          axisLabel: {
            color: '#666',
            fontWeight: 'bold'
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: [{
          data: [
            {
              value: completedTasks,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(
                  0, 0, 0, 1,
                  [
                    {offset: 0, color: '#FFD86E'},
                    {offset: 0.5, color: '#FDAA48'},
                    {offset: 1, color: '#FA8C35'}
                  ]
                )
              }
            },
            {
              value: inProgressTasks,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(
                  0, 0, 0, 1,
                  [
                    {offset: 0, color: '#58B9E3'},
                    {offset: 0.5, color: '#3A97D4'},
                    {offset: 1, color: '#2C7FC9'}
                  ]
                )
              }
            },
            {
              value: pendingTasks,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(
                  0, 0, 0, 1,
                  [
                    {offset: 0, color: '#FF9FA3'},
                    {offset: 0.5, color: '#FA7F85'},
                    {offset: 1, color: '#F56267'}
                  ]
                )
              }
            }
          ],
          type: 'bar',
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(220, 220, 220, 0.1)'
          },
          barWidth: '50%',
          itemStyle: {
            borderRadius: [8, 8, 0, 0],
            shadowColor: 'rgba(0, 0, 0, 0.2)',
            shadowBlur: 10
          },
          emphasis: {
            itemStyle: {
              shadowColor: 'rgba(0, 0, 0, 0.3)',
              shadowBlur: 15
            }
          },
          label: {
            show: true,
            position: 'top',
            fontSize: 14,
            fontWeight: 'bold',
            color: '#606266',
            formatter: '{c}',
            distance: 10
          },
          animation: true,
          animationDuration: 1500,
          animationEasing: 'elasticOut'
        }]
      }, true); // 使用true参数强制完全重新渲染图表
      
      // 强制重绘
      setTimeout(() => {
        cleaningTaskChartInstance.resize();
      }, 200);
    }
    
    // 更新详情对话框中的数据
    // 入住率详情
    occupancyDetails.value = [
      { 
        roomType: '标准间', 
        total: stats.totalRooms > 0 ? Math.round(stats.totalRooms * 0.4) : 0,
        occupied: stats.occupiedRooms > 0 ? Math.round(stats.occupiedRooms * 0.4) : 0,
        available: stats.availableRooms > 0 ? Math.round(stats.availableRooms * 0.4) : 0,
        rate: `${calculateRate(Math.round(stats.occupiedRooms * 0.4), Math.round(stats.totalRooms * 0.4))}%`
      },
      { 
        roomType: '豪华间', 
        total: stats.totalRooms > 0 ? Math.round(stats.totalRooms * 0.3) : 0,
        occupied: stats.occupiedRooms > 0 ? Math.round(stats.occupiedRooms * 0.3) : 0,
        available: stats.availableRooms > 0 ? Math.round(stats.availableRooms * 0.3) : 0,
        rate: `${calculateRate(Math.round(stats.occupiedRooms * 0.3), Math.round(stats.totalRooms * 0.3))}%`
      },
      { 
        roomType: '套房', 
        total: stats.totalRooms > 0 ? Math.round(stats.totalRooms * 0.2) : 0,
        occupied: stats.occupiedRooms > 0 ? Math.round(stats.occupiedRooms * 0.2) : 0,
        available: stats.availableRooms > 0 ? Math.round(stats.availableRooms * 0.2) : 0,
        rate: `${calculateRate(Math.round(stats.occupiedRooms * 0.2), Math.round(stats.totalRooms * 0.2))}%`
      },
      { 
        roomType: '总统套房', 
        total: stats.totalRooms > 0 ? Math.round(stats.totalRooms * 0.1) : 0,
        occupied: stats.occupiedRooms > 0 ? Math.round(stats.occupiedRooms * 0.1) : 0,
        available: stats.availableRooms > 0 ? Math.round(stats.availableRooms * 0.1) : 0,
        rate: `${calculateRate(Math.round(stats.occupiedRooms * 0.1), Math.round(stats.totalRooms * 0.1))}%`
      }
    ]
    
    isLoading.value = false
  } catch (e) {
    console.error('获取仪表盘数据失败:', e)
    error.value = '获取数据失败，请刷新页面重试: ' + (e.message || e)
    isLoading.value = false
  }
}

// 辅助函数：数字格式化
const formatNumber = (num) => {
  if (typeof num !== 'number') {
    num = Number(num) || 0
  }
  return num.toLocaleString('zh-CN')
}

// 辅助函数：计算入住率
const calculateRate = (occupied, total) => {
  if (!total) return 0
  return Math.round((occupied / total) * 100)
}

// 用户角色和初始化函数
const getUserRoleAndInitialize = () => {
  // 仅对管理员用户初始化图表
  if (userRole.value === 'ADMIN') {
    // 初始化各种图表
    
    // 入住率趋势图
    occupancyChartInstance = echarts.init(occupancyChart.value)
    occupancyChartInstance.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: [] // 初始为空，稍后通过API获取
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
        data: [], // 初始为空，稍后通过API获取
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#5b86e5'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(91, 134, 229, 0.3)'
            }, {
              offset: 1, color: 'rgba(91, 134, 229, 0.1)'
            }]
          }
        }
      }]
    })
    
    // 获取入住率趋势数据
    fetchOccupancyTrendData();

    // 收入统计图
    revenueChartInstance = echarts.init(revenueChart.value)
    revenueChartInstance.setOption({
      backgroundColor: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(249, 251, 255, 0.9)' },
          { offset: 1, color: 'rgba(245, 247, 250, 0.9)' }
        ]
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
          shadowStyle: {
            color: 'rgba(91, 134, 229, 0.1)'
          }
        },
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderColor: '#eee',
        borderWidth: 1,
        textStyle: {
          color: '#333',
          fontSize: 13
        },
        formatter: function(params) {
          let value = params[0].value;
          return `<div style="font-weight:bold;margin-bottom:4px;">${params[0].name}</div>
                 <div style="color:#5b86e5;">
                   <span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:#5b86e5;"></span>
                   总收入：¥${value.toLocaleString('zh-CN')}
                 </div>`;
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '8%',
        top: '8%',
        containLabel: true
      },
      legend: {
        show: false
      },
      xAxis: {
        type: 'category',
        data: [], // 初始为空，稍后通过API获取
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        axisLabel: {
          color: '#666',
          fontSize: 12,
          margin: 16,
          fontWeight: 'bold'
        },
        axisTick: {
          alignWithLabel: true,
          lineStyle: {
            color: '#ddd'
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: function(value) {
            if (value >= 10000) {
              return '¥' + (value / 10000) + '万';
            }
            return '¥' + value;
          },
          color: '#666',
          fontSize: 12,
          fontWeight: 'bold'
        },
        splitLine: {
          lineStyle: {
            color: '#eee',
            type: 'dashed',
            width: 1
          }
        },
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        }
      },
      series: [
        {
          name: '总收入',
          data: [], // 初始为空，稍后通过API获取
          type: 'bar',
          barWidth: '50%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(
              0, 0, 0, 1,
              [
                {offset: 0, color: '#83bff6'},
                {offset: 0.5, color: '#5b86e5'},
                {offset: 1, color: '#4862d6'}
              ]
            ),
            borderRadius: [8, 8, 0, 0],
            shadowColor: 'rgba(72, 98, 214, 0.3)',
            shadowBlur: 10
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(
                0, 0, 0, 1,
                [
                  {offset: 0, color: '#5b86e5'},
                  {offset: 0.7, color: '#4862d6'},
                  {offset: 1, color: '#3447c6'}
                ]
              ),
              shadowColor: 'rgba(72, 98, 214, 0.5)',
              shadowBlur: 15
            }
          },
          label: {
            show: true,
            position: 'top',
            formatter: function(params) {
              return '¥' + params.value.toLocaleString('zh-CN');
            },
            fontSize: 12,
            fontWeight: 'bold',
            color: '#5b86e5',
            distance: 10
          },
          markLine: {
            silent: true,
            lineStyle: {
              color: '#ff9f7f',
              type: 'dashed',
              width: 1
            },
            data: [
              { 
                type: 'average', 
                name: '平均值',
                label: {
                  formatter: '平均: ¥{c}',
                  position: 'end'
                }
              }
            ]
          },
          animation: true,
          animationDuration: 1500,
          animationEasing: 'elasticOut'
        }
      ]
    })
    
    // 获取收入统计数据
    fetchRevenueStatsData();

    // 房间状态统计图
    roomStatusChartInstance = echarts.init(roomStatusChart.value)
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
        data: [], // 初始化为空数组，稍后会通过API数据更新
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        itemStyle: {
          borderRadius: 6,
          borderWidth: 2,
          borderColor: '#fff'
        },
        label: {
          formatter: '{b}: {d}%'
        }
      }]
    })

    // 清洁任务完成情况图
    cleaningTaskChartInstance = echarts.init(cleaningTaskChart.value)
    cleaningTaskChartInstance.setOption({
      backgroundColor: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(249, 251, 255, 0.9)' },
          { offset: 1, color: 'rgba(245, 247, 250, 0.9)' }
        ]
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function(params) {
          return `<div style="font-weight:bold;margin-bottom:4px;">${params[0].name}</div>
                  <div>
                    <span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${params[0].color};"></span>
                    数量：${params[0].value}
                  </div>`;
        },
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderColor: '#eee',
        textStyle: {
          color: '#333'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '8%',
        top: '8%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['已完成', '进行中', '待处理'],
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        axisLabel: {
          color: '#555',
          fontWeight: 'bold',
          fontSize: 12,
          margin: 16
        },
        axisTick: {
          alignWithLabel: true,
          lineStyle: {
            color: '#ddd'
          }
        }
      },
      yAxis: {
        type: 'value',
        splitLine: {
          lineStyle: {
            color: '#eee',
            type: 'dashed'
          }
        },
        axisLabel: {
          color: '#666',
          fontWeight: 'bold'
        },
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        }
      },
      series: [{
        data: [
          {
            value: 0,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(
                0, 0, 0, 1,
                [
                  {offset: 0, color: '#FFD86E'},
                  {offset: 0.5, color: '#FDAA48'},
                  {offset: 1, color: '#FA8C35'}
                ]
              )
            }
          },
          {
            value: 0,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(
                0, 0, 0, 1,
                [
                  {offset: 0, color: '#58B9E3'},
                  {offset: 0.5, color: '#3A97D4'},
                  {offset: 1, color: '#2C7FC9'}
                ]
              )
            }
          },
          {
            value: 0,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(
                0, 0, 0, 1,
                [
                  {offset: 0, color: '#FF9FA3'},
                  {offset: 0.5, color: '#FA7F85'},
                  {offset: 1, color: '#F56267'}
                ]
              )
            }
          }
        ],
        type: 'bar',
        showBackground: true,
        backgroundStyle: {
          color: 'rgba(220, 220, 220, 0.1)'
        },
        barWidth: '50%',
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          shadowColor: 'rgba(0, 0, 0, 0.2)',
          shadowBlur: 10
        },
        emphasis: {
          itemStyle: {
            shadowColor: 'rgba(0, 0, 0, 0.3)',
            shadowBlur: 15
          }
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 14,
          fontWeight: 'bold',
          color: '#606266',
          formatter: '{c}',
          distance: 10
        },
        animation: true,
        animationDuration: 1500,
        animationEasing: 'elasticOut'
      }]
    })

    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', () => {
      occupancyChartInstance.resize()
      revenueChartInstance.resize()
      roomStatusChartInstance.resize()
      cleaningTaskChartInstance.resize()
    })
  }
}

// 确保在onMounted中调用fetchDashboardData函数
onMounted(() => {
  // 确认用户角色和权限
  getUserRoleAndInitialize()

  // 加载仪表盘数据
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px 0;
  height: 100%;
  overflow-y: auto;
}

/* 高级豪华卡片样式 */
.luxury-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  height: 130px;
  position: relative;
  background: #fff;
  margin-bottom: 16px;
}

.luxury-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}

.luxury-chart-card {
  border-radius: 12px;
  overflow: hidden;
  border: none;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

.luxury-chart-card:hover {
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
}

.card-content::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 60%;
  height: 60%;
  background: linear-gradient(135deg, transparent 0%, rgba(255, 255, 255, 0.15) 100%);
  z-index: 1;
  border-radius: 50% 0 0 0;
}

.card-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 70px;
  height: 70px;
  border-radius: 18px;
  margin-right: 20px;
  position: relative;
  z-index: 2;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.card-icon-wrapper.success {
  background: linear-gradient(135deg, #36d1dc, #5b86e5);
  box-shadow: 0 4px 12px rgba(91, 134, 229, 0.3);
}

.card-icon-wrapper.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 4px 12px rgba(118, 75, 162, 0.3);
}

.card-icon-wrapper.warning {
  background: linear-gradient(135deg, #f6d365, #fda085);
  box-shadow: 0 4px 12px rgba(253, 160, 133, 0.3);
}

.card-icon-wrapper.info {
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  box-shadow: 0 4px 12px rgba(194, 233, 251, 0.3);
}

.card-icon {
  font-size: 30px;
  color: white;
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 为Money图标单独设置放大比例 */
.card-icon-wrapper.primary .card-icon {
  transform: scale(1.3);
}

.card-info {
  flex-grow: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  position: relative;
  z-index: 2;
}

.card-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 500;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
  font-family: 'Helvetica Neue', sans-serif;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.8);
}

.card-change {
  font-size: 14px;
  display: flex;
  align-items: center;
  font-weight: 500;
}

.card-change.up {
  color: #67c23a;
}

.card-change.down {
  color: #f56c6c;
}

.luxury-card.success {
  background: linear-gradient(135deg, rgba(54, 209, 220, 0.2), rgba(91, 134, 229, 0.25));
  border-left: 4px solid #5b86e5;
  box-shadow: 0 8px 20px rgba(91, 134, 229, 0.15);
}

.luxury-card.primary {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2), rgba(118, 75, 162, 0.25));
  border-left: 4px solid #764ba2;
  box-shadow: 0 8px 20px rgba(118, 75, 162, 0.15);
}

.luxury-card.warning {
  background: linear-gradient(135deg, rgba(246, 211, 101, 0.2), rgba(253, 160, 133, 0.25));
  border-left: 4px solid #fda085;
  box-shadow: 0 8px 20px rgba(253, 160, 133, 0.15);
}

.luxury-card.info {
  background: linear-gradient(135deg, rgba(161, 196, 253, 0.2), rgba(194, 233, 251, 0.25));
  border-left: 4px solid #a1c4fd;
  box-shadow: 0 8px 20px rgba(194, 233, 251, 0.15);
}

.chart-row {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 10px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-subtitle {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}

.time-filter {
  background-color: #f5f7fa;
  border-radius: 18px;
  padding: 3px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.chart-container {
  padding: 20px;
  position: relative;
}

.luxury-dialog .el-dialog__header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(to right, #f7f8fb, #f4f5f7);
}

.luxury-dialog .el-dialog__body {
  padding: 24px;
}

.details-content h3 {
  margin-top: 30px;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #5b86e5;
  padding-left: 12px;
  position: relative;
}

.details-content h3::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 40px;
  height: 2px;
  background: #5b86e5;
}

.room-details, .revenue-details, .visitor-details, .booking-details {
  margin-top: 24px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .luxury-card {
    margin-bottom: 16px;
  }
}

/* 角色专用页面样式 */
.role-dashboard {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}

.role-dashboard .el-empty {
  padding: 40px;
  background-color: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.role-dashboard .el-empty__description {
  font-size: 16px;
  color: #606266;
  max-width: 500px;
  line-height: 1.8;
  margin-top: 20px;
}
</style>