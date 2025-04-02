<template>
  <div class="room-detail-container" v-if="room">
    <el-card class="room-detail-card">
      <div class="room-image-container">
        <img :src="room.image" :alt="room.name" class="room-image">
      </div>
      
      <div class="room-info">
        <h1 class="room-title">{{ room.name }}</h1>
        <div class="room-price">
          <span class="price">¥{{ room.price }}</span>
          <span class="per-night">/晚</span>
        </div>
        
        <div class="room-description">
          <h2>房间描述</h2>
          <p>{{ room.description }}</p>
        </div>
        
        <div class="room-features">
          <h2>房间设施</h2>
          <div class="features-list">
            <el-tag
              v-for="feature in room.features"
              :key="feature"
              size="large"
              class="feature-tag"
            >
              {{ feature }}
            </el-tag>
          </div>
        </div>
        
        <div class="booking-section">
          <el-button type="primary" size="large" @click="handleBooking">立即预订</el-button>
        </div>
      </div>
    </el-card>
  </div>
  <div v-else class="not-found">
    <h2>未找到房间信息</h2>
    <el-button type="primary" @click="goBack">返回房间列表</el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const room = ref(null)

onMounted(() => {
  const roomId = parseInt(route.params.id)
  // 模拟从Rooms.vue中获取房间数据
  const rooms = [
    {
      id: 1,
      name: '豪华大床房',
      image: '/src/assets/room1.jpg',
      description: '舒适宽敞的空间，配备1.8米大床，让您享受优质睡眠',
      price: 688,
      features: ['免费WiFi', '空调', '私人浴室', '24小时热水']
    },
    {
      id: 2,
      name: '行政套房',
      image: '/src/assets/room2.jpg',
      description: '独立的客厅和卧室空间，尊享商务人士的理想选择',
      price: 988,
      features: ['会客区', '迷你吧', '商务办公区', '高层景观']
    },
    {
      id: 3,
      name: '家庭套房',
      image: '/src/assets/room3.jpg',
      description: '温馨舒适的家庭房，适合亲子出游',
      price: 1288,
      features: ['双床', '儿童设施', '观景阳台', '加床服务']
    }
  ]
  
  room.value = rooms.find(r => r.id === roomId)
})

const handleBooking = () => {
  router.push({
    path: '/booking',
    query: { roomId: room.value.id }
  })
}

const goBack = () => {
  router.push('/rooms')
}
</script>

<style scoped>
.room-detail-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.room-detail-card {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.room-image-container {
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 8px;
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-info {
  padding: 20px;
}

.room-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #2c3e50;
}

.room-price {
  margin-bottom: 30px;
}

.price {
  font-size: 32px;
  color: #409EFF;
  font-weight: bold;
}

.per-night {
  font-size: 16px;
  color: #909399;
}

.room-description,
.room-features {
  margin-bottom: 30px;
}

.room-description h2,
.room-features h2 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #2c3e50;
}

.room-description p {
  color: #5c6b7f;
  line-height: 1.6;
}

.features-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.feature-tag {
  padding: 8px 16px;
}

.booking-section {
  margin-top: 30px;
}

.not-found {
  text-align: center;
  padding: 50px;
}

.not-found h2 {
  margin-bottom: 20px;
  color: #909399;
}

@media (max-width: 768px) {
  .room-image-container {
    height: 300px;
  }
  
  .room-title {
    font-size: 24px;
  }
  
  .price {
    font-size: 28px;
  }
}
</style>