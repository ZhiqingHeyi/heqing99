<template>
  <div class="rooms-page">
    <div class="rooms-header">
      <h1>精致客房 · 尊享体验</h1>
      <div class="header-divider"></div>
      <p>鹤清酒店为您呈现东方美学与奢华体验的完美融合</p>
    </div>
    
    <div class="room-filter">
      <div class="filter-wrapper">
        <span class="filter-label">筛选：</span>
        <el-radio-group v-model="currentFilter" size="large" @change="filterRooms">
          <el-radio-button label="all">全部房型</el-radio-button>
          <el-radio-button label="business">商务房型</el-radio-button>
          <el-radio-button label="family">家庭房型</el-radio-button>
          <el-radio-button label="luxury">奢华套房</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="rooms-container">
      <el-row :gutter="30">
        <el-col :xs="24" :sm="12" :md="8" v-for="room in displayRooms" :key="room.id">
          <div class="luxury-room-card">
            <div class="room-image-container">
              <img :src="room.image" class="room-image" alt="room.name">
              <div class="room-overlay">
                <el-button type="primary" class="details-btn" @click="goToRoomDetail(room.id)">查看详情</el-button>
              </div>
              <div class="room-tag" v-if="room.tag">{{room.tag}}</div>
            </div>
            <div class="room-info">
              <h3 class="room-name">{{ room.name }}</h3>
              <div class="room-meta">
                <span class="room-size"><i class="el-icon-full-screen"></i> {{ room.size }}㎡</span>
                <span class="room-capacity"><i class="el-icon-user"></i> {{ room.capacity }}人</span>
              </div>
              <p class="room-description">{{ room.description }}</p>
              <div class="room-features">
                <el-tag v-for="feature in room.features" :key="feature" size="small" effect="plain" class="feature-tag">{{ feature }}</el-tag>
              </div>
              <div class="room-footer">
                <div class="room-price">
                  <span class="price">¥{{ room.price }}</span>
                  <span class="per-night">/晚</span>
                </div>
                <el-button type="primary" class="book-btn" @click="goToBooking(room.id)">立即预订</el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <div class="booking-benefits">
      <div class="benefit-item">
        <i class="el-icon-circle-check"></i>
        <div class="benefit-info">
          <h4>灵活取消</h4>
          <p>提前24小时取消无额外费用</p>
        </div>
      </div>
      <div class="benefit-item">
        <i class="el-icon-star-on"></i>
        <div class="benefit-info">
          <h4>会员优惠</h4>
          <p>会员可享受房价9折优惠</p>
        </div>
      </div>
      <div class="benefit-item">
        <i class="el-icon-medal-1"></i>
        <div class="benefit-info">
          <h4>积分奖励</h4>
          <p>每次入住可累积积分兑换礼品</p>
        </div>
      </div>
      <div class="benefit-item">
        <i class="el-icon-price-tag"></i>
        <div class="benefit-info">
          <h4>价格保证</h4>
          <p>官网预订确保最优惠价格</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentFilter = ref('all')

const allRooms = ref([
  {
    id: 1,
    name: '豪华大床房',
    image: '/src/assets/room1.jpg',
    description: '舒适宽敞的空间，配备1.8米大床，让您享受优质睡眠',
    price: 688,
    size: 38,
    capacity: 2,
    tag: '优选',
    category: 'business',
    features: ['免费WiFi', '空调', '私人浴室', '24小时热水']
  },
  {
    id: 2,
    name: '行政套房',
    image: '/src/assets/room2.jpg',
    description: '独立的客厅和卧室空间，尊享商务人士的理想选择',
    price: 988,
    size: 55,
    capacity: 2,
    tag: '热门',
    category: 'business',
    features: ['会客区', '迷你吧', '商务办公区', '高层景观']
  },
  {
    id: 3,
    name: '家庭套房',
    image: '/src/assets/room3.jpg',
    description: '温馨舒适的家庭房，适合亲子出游',
    price: 1288,
    size: 68,
    capacity: 4,
    category: 'family',
    features: ['双床', '儿童设施', '观景阳台', '加床服务']
  },
  {
    id: 4,
    name: '总统套房',
    image: '/src/assets/room1.jpg',
    description: '奢华顶级套房，尽享尊贵典雅的入住体验',
    price: 2888,
    size: 120,
    capacity: 4,
    tag: '尊享',
    category: 'luxury',
    features: ['会客厅', '观景露台', '24小时管家', '私人用餐区']
  },
  {
    id: 5,
    name: '豪华双床房',
    image: '/src/assets/room2.jpg',
    description: '两张1.2米舒适双人床，适合商务伙伴同住',
    price: 788,
    size: 40,
    capacity: 2,
    category: 'business',
    features: ['双人床', '工作区域', '高速WiFi', '独立卫浴']
  },
  {
    id: 6,
    name: '景观套房',
    image: '/src/assets/room3.jpg',
    description: '特别设计的套房，拥有绝佳的城市景观',
    price: 1688,
    size: 75,
    capacity: 2,
    category: 'luxury',
    features: ['全景落地窗', '观景阳台', '豪华浴缸', '早餐服务']
  }
])

const displayRooms = computed(() => {
  if (currentFilter.value === 'all') {
    return allRooms.value
  } else {
    return allRooms.value.filter(room => room.category === currentFilter.value)
  }
})

const filterRooms = (value) => {
  currentFilter.value = value
}

const goToRoomDetail = (roomId) => {
  router.push(`/room/${roomId}`)
}

const goToBooking = (roomId) => {
  router.push({
    path: '/booking',
    query: { room: roomId }
  })
}
</script>

<style scoped>
.rooms-page {
  background-color: #f9f7f3;
  padding: 60px 0;
  position: relative;
}

.rooms-header {
  text-align: center;
  margin-bottom: 60px;
  position: relative;
  padding: 0 20px;
}

.rooms-header h1 {
  font-size: 2.8em;
  color: #333;
  margin-bottom: 20px;
  font-family: "SimSun", serif;
  font-weight: 600;
  letter-spacing: 3px;
}

.header-divider {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #c59d5f, transparent);
  margin: 0 auto 30px;
}

.rooms-header p {
  font-size: 1.2em;
  color: #666;
  max-width: 700px;
  margin: 0 auto;
  line-height: 1.6;
}

.room-filter {
  margin-bottom: 40px;
  text-align: center;
}

.filter-wrapper {
  display: inline-flex;
  align-items: center;
  background: #fff;
  padding: 10px 20px;
  border-radius: 50px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  border: 1px solid rgba(197, 157, 95, 0.1);
}

.filter-label {
  margin-right: 15px;
  font-size: 16px;
  color: #666;
}

.rooms-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.luxury-room-card {
  margin-bottom: 40px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: all 0.4s ease;
  border: 1px solid rgba(197, 157, 95, 0.05);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.luxury-room-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border-color: rgba(197, 157, 95, 0.2);
}

.room-image-container {
  position: relative;
  overflow: hidden;
  height: 250px;
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.luxury-room-card:hover .room-image {
  transform: scale(1.1);
}

.room-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.4s ease;
}

.luxury-room-card:hover .room-overlay {
  opacity: 1;
}

.details-btn {
  background: transparent;
  border: 2px solid #fff;
  color: #fff;
  font-weight: 500;
  padding: 10px 20px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.details-btn:hover {
  background: #fff;
  color: #c59d5f;
  transform: translateY(-3px);
}

.room-tag {
  position: absolute;
  top: 20px;
  right: 20px;
  background: linear-gradient(135deg, #c59d5f, #ddbf85);
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 3px 8px rgba(0,0,0,0.1);
}

.room-info {
  padding: 25px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.room-name {
  font-size: 1.5em;
  color: #333;
  margin-bottom: 15px;
  font-family: "SimSun", serif;
  font-weight: 600;
}

.room-meta {
  display: flex;
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.room-size, .room-capacity {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.room-size i, .room-capacity i {
  margin-right: 5px;
  color: #c59d5f;
}

.room-description {
  line-height: 1.6;
  color: #666;
  margin-bottom: 20px;
  flex: 1;
}

.room-features {
  margin-bottom: 20px;
}

.feature-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  background: rgba(197, 157, 95, 0.08);
  border-color: rgba(197, 157, 95, 0.2);
  color: #9a7942;
}

.room-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 15px;
  border-top: 1px solid rgba(197, 157, 95, 0.1);
}

.room-price {
  display: flex;
  align-items: flex-end;
}

.price {
  font-size: 1.8em;
  font-weight: 600;
  color: #c59d5f;
  line-height: 1;
}

.per-night {
  font-size: 14px;
  color: #999;
  margin-left: 5px;
  margin-bottom: 3px;
}

.book-btn {
  background: linear-gradient(135deg, #c59d5f, #ddbf85);
  border-color: transparent;
  padding: 8px 16px;
  font-size: 14px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.book-btn:hover {
  background: linear-gradient(135deg, #b58d40, #c59d5f);
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(197, 157, 95, 0.3);
}

.booking-benefits {
  max-width: 1200px;
  margin: 80px auto 0;
  padding: 40px 20px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.05);
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  border: 1px solid rgba(197, 157, 95, 0.1);
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  padding: 20px 30px;
  margin: 10px;
  flex: 1;
  min-width: 220px;
}

.benefit-item i {
  font-size: 28px;
  color: #c59d5f;
  margin-right: 15px;
  margin-top: 5px;
}

.benefit-info h4 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.benefit-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .filter-wrapper {
    flex-direction: column;
    padding: 15px;
  }
  
  .filter-label {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .booking-benefits {
    flex-direction: column;
  }
  
  .benefit-item {
    padding: 15px;
    margin: 5px 0;
  }
}
</style>