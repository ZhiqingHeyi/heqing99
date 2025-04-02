<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 数据概览卡片 -->
      <el-col :span="6" v-for="card in dataCards" :key="card.title">
        <el-card class="data-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <el-icon class="card-icon" :class="card.type">
              <component :is="card.icon" />
            </el-icon>
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
        <el-card>
          <template #header>
            <div class="card-header">
              <span>入住率趋势</span>
              <el-radio-group v-model="occupancyTimeRange" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里将使用echarts绘制图表 -->
            <div ref="occupancyChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 收入统计图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>收入统计</span>
              <el-radio-group v-model="revenueTimeRange" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里将使用echarts绘制图表 -->
            <div ref="revenueChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 房间状态统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>房间状态统计</span>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里将使用echarts绘制饼图 -->
            <div ref="roomStatusChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 清洁任务完成情况 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>清洁任务完成情况</span>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里将使用echarts绘制进度图 -->
            <div ref="cleaningTaskChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
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
  padding: 20px;
}

.chart-row {
  margin-top: 20px;
}

.data-card {
  height: 120px;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  font-size: 48px;
  margin-right: 20px;
}

.card-icon.success {
  color: #67c23a;
}

.card-icon.primary {
  color: #409eff;
}

.card-icon.warning {
  color: #e6a23c;
}

.card-icon.info {
  color: #909399;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  margin: 8px 0;
}

.card-change {
  font-size: 12px;
}

.card-change.up {
  color: #67c23a;
}

.card-change.down {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  padding: 10px;
}
</style>