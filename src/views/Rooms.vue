<template>
  <div class="rooms-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" v-for="room in rooms" :key="room.id">
        <el-card class="room-card" :body-style="{ padding: '0px' }">
          <img :src="room.image" class="room-image">
          <div class="room-info">
            <h3>{{ room.name }}</h3>
            <p class="room-description">{{ room.description }}</p>
            <div class="room-features">
              <el-tag size="small" v-for="feature in room.features" :key="feature">{{ feature }}</el-tag>
            </div>
            <div class="room-price">
              <span class="price">¥{{ room.price }}</span>
              <span class="per-night">/晚</span>
            </div>
            <el-button type="primary" @click="goToRoomDetail(room.id)">了解更多</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const rooms = ref([
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
])

const goToRoomDetail = (roomId) => {
  router.push(`/room/${roomId}`)
}
</script>

<style scoped>
.rooms-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.room-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
}

.room-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.room-info {
  padding: 20px;
}

.room-description {
  color: #666;
  margin: 10px 0;
}

.room-features {
  margin: 15px 0;
}

.room-features .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.room-price {
  margin: 15px 0;
}

.price {
  font-size: 24px;
  color: #409EFF;
  font-weight: bold;
}

.per-night {
  color: #999;
  font-size: 14px;
}
</style>