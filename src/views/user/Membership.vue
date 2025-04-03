<template>
  <div class="membership-container">
    <div class="page-background"></div>
    <h1 class="page-title">尊享会员服务</h1>
    <div class="page-subtitle">探索鹤清酒店专属会员礼遇与权益</div>
    
    <div class="current-membership luxury-card" :class="userLevelClass">
      <div class="membership-header">
        <div class="level-icon">
          <el-icon>
            <component :is="levelIcon" />
          </el-icon>
        </div>
        <div class="level-info">
          <h2>{{ userLevel }}</h2>
          <p v-if="userLevel === '普通用户'">再消费 <span class="highlight-amount">{{ needSpend }}</span>元 即可升级为铜牌会员</p>
          <p v-else-if="userLevel === '铜牌会员'">再消费 <span class="highlight-amount">{{ needSpend }}</span>元 即可升级为银牌会员</p>
          <p v-else-if="userLevel === '银牌会员'">再消费 <span class="highlight-amount">{{ needSpend }}</span>元 即可升级为金牌会员</p>
          <p v-else-if="userLevel === '金牌会员'">再消费 <span class="highlight-amount">{{ needSpend }}</span>元 即可升级为钻石会员</p>
          <p v-else>您已达到最高会员等级，尊享酒店所有礼遇</p>
        </div>
      </div>
      
      <div class="membership-details">
        <div class="detail-item">
          <div class="detail-icon accumulate"></div>
          <div class="detail-content">
            <div class="detail-label">累计消费</div>
            <div class="detail-value">¥{{ userTotalSpent }}</div>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon points"></div>
          <div class="detail-content">
            <div class="detail-label">会员积分</div>
            <div class="detail-value">{{ userPoints }} 分</div>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon discount"></div>
          <div class="detail-content">
            <div class="detail-label">会员折扣</div>
            <div class="detail-value">{{ discountDisplay }}</div>
          </div>
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
      <h2 class="section-title">会员等级与尊享权益</h2>
      
      <el-table :data="membershipLevels" style="width: 100%" :row-class-name="tableRowClassName" class="luxury-table">
        <el-table-column prop="level" label="会员等级" width="160">
          <template #default="scope">
            <div class="level-name" :class="'level-' + scope.row.className">
              {{ scope.row.level }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="requirement" label="升级条件" width="220" />
        <el-table-column prop="discount" label="消费折扣" width="160" />
        <el-table-column prop="points" label="积分比例" width="160" />
        <el-table-column prop="prepayment" label="预付金选项" width="180" />
        <el-table-column prop="other" label="其它权益" />
      </el-table>
    </div>
    
    <div class="faq-section luxury-card">
      <h2 class="section-title">会员常见问题</h2>
      
      <el-collapse class="luxury-collapse">
        <el-collapse-item title="如何计算会员等级？" name="1">
          <div class="collapse-content">
            <p>会员等级根据您在本酒店的累计消费金额自动计算。消费金额包括房费、餐饮和其他服务费用。</p>
            <ul class="elegant-list">
              <li><span class="level-badge bronze">铜牌</span> 累计消费满1500元</li>
              <li><span class="level-badge silver">银牌</span> 累计消费满5000元</li>
              <li><span class="level-badge gold">金牌</span> 累计消费满10000元</li>
              <li><span class="level-badge diamond">钻石</span> 累计消费满30000元</li>
            </ul>
          </div>
        </el-collapse-item>
        
        <el-collapse-item title="会员积分如何获取和使用？" name="2">
          <div class="collapse-content">
            <p>会员在酒店消费时可获得相应积分，获取比例根据会员等级有所不同：</p>
            <ul class="elegant-list">
              <li><span class="level-badge regular">普通</span> 不获得积分</li>
              <li><span class="level-badge bronze">铜牌</span> 消费1元获得1分</li>
              <li><span class="level-badge silver">银牌</span> 消费1元获得1.2分</li>
              <li><span class="level-badge gold">金牌</span> 消费1元获得1.5分</li>
              <li><span class="level-badge diamond">钻石</span> 消费1元获得2分</li>
            </ul>
            <p>积分可用于抵扣房费、餐饮费用或兑换酒店礼品。100积分可抵扣10元消费。</p>
          </div>
        </el-collapse-item>
        
        <el-collapse-item title="会员等级有效期是多久？" name="3">
          <div class="collapse-content">
            <p>会员等级长期有效，无需每年重新达标。但钻石会员需要每年消费20000元维持资格，否则次年降为金牌会员。</p>
          </div>
        </el-collapse-item>
        
        <el-collapse-item title="如何享受会员预订特权？" name="4">
          <div class="collapse-content">
            <p>不同会员等级享有不同的预订特权：</p>
            <ul class="elegant-list">
              <li><span class="level-badge bronze">铜牌</span> 及以上：可选择到店支付</li>
              <li><span class="level-badge silver">银牌</span> 及以上：可选择免预付金</li>
              <li><span class="level-badge gold">金牌</span> 及以上：享有免费房型升级（视房间情况而定）</li>
              <li><span class="level-badge diamond">钻石</span> 专享：优先预订权和保留房间服务</li>
            </ul>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, Medal, Trophy, Star, StarFilled } from '@element-plus/icons-vue'

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
      return Medal
    case '银牌会员':
      return Trophy
    case '金牌会员':
      return Star
    case '钻石会员':
      return StarFilled
    default:
      return User
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
  margin: 40px auto;
  padding: 30px;
  position: relative;
  background-color: #f9f7f3;
  min-height: 100vh;
}

.page-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('@/assets/hotel1.jpg');
  background-size: cover;
  background-position: center;
  opacity: 0.07;
  z-index: 0;
}

.page-title {
  font-family: "Playfair Display", "Times New Roman", serif;
  text-align: center;
  font-size: 2.8em;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.page-subtitle {
  text-align: center;
  color: #666;
  font-size: 1.2em;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.luxury-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.luxury-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.current-membership {
  padding: 40px;
  margin-bottom: 40px;
  position: relative;
}

.current-membership::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #d4af37, #e5c17f, #d4af37);
}

.membership-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.level-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 25px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #f3efe7, #f9f7f3);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
}

.level-icon :deep(svg) {
  width: 40px;
  height: 40px;
  color: #8a6d3b;
}

.level-info h2 {
  font-family: "Playfair Display", "Times New Roman", serif;
  font-size: 2em;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}

.level-info p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.highlight-amount {
  font-weight: 600;
  color: #8a6d3b;
}

.membership-details {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  margin-bottom: 30px;
}

.detail-item {
  flex: 1;
  min-width: 200px;
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(to right, #f9f7f3, #f3efe7);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.detail-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
}

.detail-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  margin-right: 15px;
  flex-shrink: 0;
  background-size: 30px;
  background-position: center;
  background-repeat: no-repeat;
  background-color: rgba(255, 255, 255, 0.7);
}

.detail-icon.accumulate {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>');
}

.detail-icon.points {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>');
}

.detail-icon.discount {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z"/></svg>');
}

.detail-content {
  flex: 1;
}

.detail-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.detail-value {
  font-size: 22px;
  font-weight: 600;
  color: #8a6d3b;
}

.level-progress {
  height: 20px;
  margin-top: 20px;
}

.level-progress :deep(.el-progress-bar__outer) {
  background-color: rgba(138, 109, 59, 0.1);
  border-radius: 10px;
}

.level-progress :deep(.el-progress-bar__inner) {
  border-radius: 10px;
}

.benefits-section, .faq-section {
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.faq-section {
  padding: 40px;
}

.section-title {
  font-family: "Playfair Display", "Times New Roman", serif;
  font-size: 1.8em;
  color: #333;
  margin-bottom: 25px;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #8a6d3b, #ab8a62);
}

.luxury-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
}

.luxury-table :deep(.el-table__header) {
  background: linear-gradient(to right, #f3efe7, #f9f7f3);
}

.luxury-table :deep(.el-table__header-wrapper th) {
  background: transparent;
  color: #665744;
  font-weight: 600;
  padding: 16px 0;
}

.luxury-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.luxury-table :deep(.el-table__row:hover) {
  background-color: rgba(193, 170, 137, 0.05);
}

.level-name {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.level-regular {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.level-bronze {
  background-color: rgba(205, 127, 50, 0.1);
  color: #cd7f32;
}

.level-silver {
  background-color: rgba(192, 192, 192, 0.15);
  color: #8c8c8c;
}

.level-gold {
  background-color: rgba(255, 215, 0, 0.15);
  color: #b5980d;
}

.level-diamond {
  background: linear-gradient(135deg, rgba(185, 242, 255, 0.2), rgba(154, 206, 255, 0.2));
  color: #4aa3cc;
}

.bronze-level .level-icon {
  background: linear-gradient(135deg, #f7eee3, #e8d4be);
}

.bronze-level .level-icon :deep(svg) {
  color: #cd7f32;
}

.silver-level .level-icon {
  background: linear-gradient(135deg, #f5f5f5, #e6e6e6);
}

.silver-level .level-icon :deep(svg) {
  color: #8c8c8c;
}

.gold-level .level-icon {
  background: linear-gradient(135deg, #fff8e1, #ffe082);
}

.gold-level .level-icon :deep(svg) {
  color: #b5980d;
}

.diamond-level .level-icon {
  background: linear-gradient(135deg, #e6f7fb, #d1edf6);
}

.diamond-level .level-icon :deep(svg) {
  color: #4aa3cc;
}

.luxury-collapse {
  border-radius: 8px;
  overflow: hidden;
}

.luxury-collapse :deep(.el-collapse-item__header) {
  background-color: #f9f7f3;
  color: #8a6d3b;
  font-weight: 600;
  font-size: 16px;
  padding: 15px 20px;
  border-bottom: 1px solid rgba(138, 109, 59, 0.1);
}

.luxury-collapse :deep(.el-collapse-item__content) {
  padding: 20px 40px;
  background-color: rgba(255, 255, 255, 0.6);
}

.collapse-content {
  color: #666;
  line-height: 1.6;
}

.elegant-list {
  list-style: none;
  padding: 0;
  margin: 15px 0;
}

.elegant-list li {
  margin-bottom: 12px;
  padding-left: 20px;
  position: relative;
  display: flex;
  align-items: center;
}

.elegant-list li::before {
  content: "•";
  position: absolute;
  left: 0;
  color: #8a6d3b;
  font-size: 18px;
}

.level-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 15px;
  font-size: 13px;
  font-weight: 600;
  margin-right: 10px;
}

.level-badge.regular {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.level-badge.bronze {
  background-color: rgba(205, 127, 50, 0.1);
  color: #cd7f32;
}

.level-badge.silver {
  background-color: rgba(192, 192, 192, 0.15);
  color: #8c8c8c;
}

.level-badge.gold {
  background-color: rgba(255, 215, 0, 0.15);
  color: #b5980d;
}

.level-badge.diamond {
  background: linear-gradient(135deg, rgba(185, 242, 255, 0.2), rgba(154, 206, 255, 0.2));
  color: #4aa3cc;
}

@media (max-width: 768px) {
  .membership-container {
    padding: 20px;
  }
  
  .page-title {
    font-size: 2em;
  }
  
  .current-membership {
    padding: 25px;
  }
  
  .membership-header {
    flex-direction: column;
    text-align: center;
  }
  
  .level-icon {
    margin: 0 auto 20px;
  }
  
  .membership-details {
    flex-direction: column;
  }
  
  .section-title {
    font-size: 1.5em;
  }
}
</style>