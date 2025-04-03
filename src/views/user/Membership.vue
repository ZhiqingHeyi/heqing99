<template>
  <div class="membership-container">
    <h1 class="page-title">会员中心</h1>
    
    <div class="current-membership" :class="userLevelClass">
      <div class="membership-header">
        <div class="level-icon">
          <i :class="levelIcon"></i>
        </div>
        <div class="level-info">
          <h2>{{ userLevel }}</h2>
          <p v-if="userLevel === '普通用户'">再消费 {{ needSpend }}元 即可升级为铜牌会员</p>
          <p v-else-if="userLevel === '铜牌会员'">再消费 {{ needSpend }}元 即可升级为银牌会员</p>
          <p v-else-if="userLevel === '银牌会员'">再消费 {{ needSpend }}元 即可升级为金牌会员</p>
          <p v-else-if="userLevel === '金牌会员'">再消费 {{ needSpend }}元 即可升级为钻石会员</p>
          <p v-else>您已达到最高会员等级</p>
        </div>
      </div>
      
      <div class="membership-details">
        <div class="detail-item">
          <div class="detail-label">累计消费：</div>
          <div class="detail-value">¥{{ userTotalSpent }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">会员积分：</div>
          <div class="detail-value">{{ userPoints }} 分</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">会员折扣：</div>
          <div class="detail-value">{{ discountDisplay }}</div>
        </div>
      </div>
      
      <el-progress
        v-if="userLevel !== '钻石会员'"
        :percentage="progressPercentage"
        :color="levelColor"
        :format="progressFormat"
        :stroke-width="20"
        class="level-progress"
      />
    </div>
    
    <div class="benefits-section">
      <h2 class="section-title">会员等级与权益</h2>
      
      <el-table :data="membershipLevels" style="width: 100%" :row-class-name="tableRowClassName">
        <el-table-column prop="level" label="会员等级" width="150">
          <template #default="scope">
            <div class="level-name" :class="'level-' + scope.row.className">
              {{ scope.row.level }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="requirement" label="升级条件" width="220" />
        <el-table-column prop="discount" label="消费折扣" width="150" />
        <el-table-column prop="points" label="积分比例" width="150" />
        <el-table-column prop="prepayment" label="预付金选项" width="180" />
        <el-table-column prop="other" label="其它权益" />
      </el-table>
    </div>
    
    <div class="faq-section">
      <h2 class="section-title">会员常见问题</h2>
      
      <el-collapse>
        <el-collapse-item title="如何计算会员等级？" name="1">
          <p>会员等级根据您在本酒店的累计消费金额自动计算。消费金额包括房费、餐饮和其他服务费用。</p>
          <ul>
            <li>铜牌会员：累计消费满1500元</li>
            <li>银牌会员：累计消费满5000元</li>
            <li>金牌会员：累计消费满10000元</li>
            <li>钻石会员：累计消费满30000元</li>
          </ul>
        </el-collapse-item>
        
        <el-collapse-item title="会员积分如何获取和使用？" name="2">
          <p>会员在酒店消费时可获得相应积分，获取比例根据会员等级有所不同：</p>
          <ul>
            <li>普通用户：不获得积分</li>
            <li>铜牌会员：消费1元获得1分</li>
            <li>银牌会员：消费1元获得1.2分</li>
            <li>金牌会员：消费1元获得1.5分</li>
            <li>钻石会员：消费1元获得2分</li>
          </ul>
          <p>积分可用于抵扣房费、餐饮费用或兑换酒店礼品。100积分可抵扣10元消费。</p>
        </el-collapse-item>
        
        <el-collapse-item title="会员等级有效期是多久？" name="3">
          <p>会员等级长期有效，无需每年重新达标。但钻石会员需要每年消费20000元维持资格，否则次年降为金牌会员。</p>
        </el-collapse-item>
        
        <el-collapse-item title="如何享受会员预订特权？" name="4">
          <p>不同会员等级享有不同的预订特权：</p>
          <ul>
            <li>铜牌会员及以上：可选择到店支付</li>
            <li>银牌会员及以上：可选择免预付金</li>
            <li>金牌会员及以上：享有免费房型升级（视房间情况而定）</li>
            <li>钻石会员：享有优先预订权和保留房间服务</li>
          </ul>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 用户信息
const userLevel = ref('普通用户')
const userTotalSpent = ref(0)
const userPoints = ref(0)

// 会员等级信息表
const membershipLevels = [
  {
    level: '普通用户',
    className: 'regular',
    requirement: '注册成为用户',
    discount: '无折扣',
    points: '不累积积分',
    prepayment: '必须支付预付金',
    other: '基础预订服务'
  },
  {
    level: '铜牌会员',
    className: 'bronze',
    requirement: '累计消费满1500元',
    discount: '98折',
    points: '消费1元=1积分',
    prepayment: '可选择到店支付',
    other: '生日礼券'
  },
  {
    level: '银牌会员',
    className: 'silver',
    requirement: '累计消费满5000元',
    discount: '95折',
    points: '消费1元=1.2积分',
    prepayment: '可选择免预付金',
    other: '延迟退房2小时'
  },
  {
    level: '金牌会员',
    className: 'gold',
    requirement: '累计消费满10000元',
    discount: '9折',
    points: '消费1元=1.5积分',
    prepayment: '可选择免预付金',
    other: '免费房型升级，欢迎饮品'
  },
  {
    level: '钻石会员',
    className: 'diamond',
    requirement: '累计消费满30000元',
    discount: '85折',
    points: '消费1元=2积分',
    prepayment: '无需预付金',
    other: '专属客服，保留房间，机场接送'
  }
]

// 计算下一等级需要消费的金额
const needSpend = computed(() => {
  if (userLevel.value === '普通用户') {
    return Math.max(0, 1500 - userTotalSpent.value)
  } else if (userLevel.value === '铜牌会员') {
    return Math.max(0, 5000 - userTotalSpent.value)
  } else if (userLevel.value === '银牌会员') {
    return Math.max(0, 10000 - userTotalSpent.value)
  } else if (userLevel.value === '金牌会员') {
    return Math.max(0, 30000 - userTotalSpent.value)
  } else {
    return 0
  }
})

// 计算会员等级进度
const progressPercentage = computed(() => {
  if (userLevel.value === '普通用户') {
    return Math.min(100, (userTotalSpent.value / 1500) * 100)
  } else if (userLevel.value === '铜牌会员') {
    return Math.min(100, ((userTotalSpent.value - 1500) / (5000 - 1500)) * 100)
  } else if (userLevel.value === '银牌会员') {
    return Math.min(100, ((userTotalSpent.value - 5000) / (10000 - 5000)) * 100)
  } else if (userLevel.value === '金牌会员') {
    return Math.min(100, ((userTotalSpent.value - 10000) / (30000 - 10000)) * 100)
  } else {
    return 100
  }
})

// 进度条格式化
const progressFormat = (percentage) => {
  if (userLevel.value === '钻石会员') {
    return '已达最高等级'
  }
  if (percentage === 100) {
    return '可升级'
  }
  return `${percentage.toFixed(0)}%`
}

// 会员等级对应的图标和颜色
const levelIcon = computed(() => {
  switch (userLevel.value) {
    case '铜牌会员':
      return 'el-icon-medal'
    case '银牌会员':
      return 'el-icon-trophy'
    case '金牌会员':
      return 'el-icon-first-aid-kit'
    case '钻石会员':
      return 'el-icon-star-on'
    default:
      return 'el-icon-user'
  }
})

const levelColor = computed(() => {
  switch (userLevel.value) {
    case '铜牌会员':
      return '#cd7f32'
    case '银牌会员':
      return '#c0c0c0'
    case '金牌会员':
      return '#ffd700'
    case '钻石会员':
      return '#b9f2ff'
    default:
      return '#409EFF'
  }
})

const userLevelClass = computed(() => {
  switch (userLevel.value) {
    case '铜牌会员':
      return 'bronze-level'
    case '银牌会员':
      return 'silver-level'
    case '金牌会员':
      return 'gold-level'
    case '钻石会员':
      return 'diamond-level'
    default:
      return 'regular-level'
  }
})

// 折扣显示
const discountDisplay = computed(() => {
  switch (userLevel.value) {
    case '铜牌会员':
      return '98折'
    case '银牌会员':
      return '95折'
    case '金牌会员':
      return '9折'
    case '钻石会员':
      return '85折'
    default:
      return '无折扣'
  }
})

// 设置表格行的类名
const tableRowClassName = ({ row }) => {
  return 'level-row-' + row.className
}

// 初始化
onMounted(() => {
  console.log('Membership组件已挂载')
  // 从localStorage获取用户信息
  if (localStorage.getItem('userLevel')) {
    userLevel.value = localStorage.getItem('userLevel')
    userTotalSpent.value = parseInt(localStorage.getItem('userTotalSpent') || '0')
    userPoints.value = parseInt(localStorage.getItem('userPoints') || '0')
  }
  
  // 确保路由配置和组件渲染正常
  document.title = '会员中心 - 会员权益'
})
</script>

<style scoped>
.membership-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  text-align: center;
  color: #333;
  font-size: 28px;
}

.current-membership {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.membership-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.level-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 30px;
  color: white;
}

.regular-level .level-icon {
  background-color: #909399;
}

.bronze-level {
  background-color: #fcf2e8;
}

.bronze-level .level-icon {
  background-color: #cd7f32;
}

.silver-level {
  background-color: #f5f5f5;
}

.silver-level .level-icon {
  background-color: #c0c0c0;
}

.gold-level {
  background-color: #fffbe6;
}

.gold-level .level-icon {
  background-color: #ffd700;
}

.diamond-level {
  background-color: #e8f4ff;
}

.diamond-level .level-icon {
  background-color: #b9f2ff;
}

.level-info h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.level-info p {
  margin: 0;
  color: #606266;
}

.membership-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
}

.detail-label {
  width: 100px;
  color: #606266;
}

.detail-value {
  font-weight: bold;
  color: #303133;
}

.level-progress {
  margin-top: 20px;
}

.section-title {
  margin: 40px 0 20px;
  font-size: 22px;
  color: #333;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.benefits-section {
  margin-bottom: 40px;
}

.level-name {
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 4px;
  text-align: center;
}

.level-regular {
  background-color: #f4f4f5;
  color: #909399;
}

.level-bronze {
  background-color: #fcf2e8;
  color: #cd7f32;
}

.level-silver {
  background-color: #f5f5f5;
  color: #606266;
}

.level-gold {
  background-color: #fffbe6;
  color: #e6a23c;
}

.level-diamond {
  background-color: #e8f4ff;
  color: #409EFF;
}

.faq-section {
  margin-bottom: 40px;
}

/* 表格样式 */
:deep(.level-row-regular) {
  background-color: #f4f4f5;
}

:deep(.level-row-bronze) {
  background-color: #fcf2e8;
}

:deep(.level-row-silver) {
  background-color: #f5f5f5;
}

:deep(.level-row-gold) {
  background-color: #fffbe6;
}

:deep(.level-row-diamond) {
  background-color: #e8f4ff;
}
</style> 