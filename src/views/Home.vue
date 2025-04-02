<template>
  <div class="home">
    <!-- 酒店介绍区域 -->
    <section class="hero-section">
      <el-carousel height="600px">
        <el-carousel-item v-for="(image, index) in hotelImages" :key="index">
          <div class="carousel-content" :style="{ backgroundImage: `url(${image})` }">
            <div class="carousel-text">
              <h1>鹤清酒店 - 仙居雅境</h1>
              <p>在这里，我们将典雅与禅意完美融合，为您打造一方静谧的仙居之所。让每一位宾客感受超凡脱俗的居停体验。</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 酒店故事 -->
    <section class="story-section">
      <h2 class="section-title">探索鹤清故事</h2>
      <p class="section-desc">在鹤清酒店，我们以仙鹤为意，以典雅为魂，为宾客打造一方超然脱俗的栖居之所。</p>
      
      <div class="story-cards">
        <el-row :gutter="40">
          <el-col :span="8">
            <div class="story-card">
              <i class="el-icon-time"></i>
              <h3>我们的历史</h3>
              <p>创立于2010年，鹤清酒店秉承"仙居雅境"的理念，以典雅高雅的建筑风格和精致考究的中式装潢闻名。我们将传统文化与现代舒适完美融合，创造出独特的东方人居体验。</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="story-card">
              <i class="el-icon-aim"></i>
              <h3>我们的使命</h3>
              <p>我们的使命是为每位宾客营造一方净土，让身心在此获得升华。以东方美学为基调，以细致入微的服务为根本，带来超越期待的居停体验。</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="story-card">
              <i class="el-icon-star-on"></i>
              <h3>我们的价值观</h3>
              <p>我们秉持"和谐、典雅、至诚"的价值观，将东方待客之道发挥极致。以文化为魂，以服务为本，让每位宾客感受超然脱俗的待遇。</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 套餐展示 -->
    <section class="packages-section">
      <h2 class="section-title">我们的套餐</h2>
      <p class="section-desc">探索我们为您精心设计的难忘住宿套餐</p>
      
      <div class="package-cards">
        <el-row :gutter="30">
          <el-col :span="8">
            <div class="package-card">
              <h3>标准房</h3>
              <p class="package-desc">适合单人或双人寻求舒适的旅客</p>
              <div class="package-price">¥500/晚</div>
              <el-button type="primary" class="book-btn">立即预订</el-button>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="package-card featured">
              <h3>豪华房</h3>
              <p class="package-desc">为追求奢华体验的客人精心打造</p>
              <div class="package-price">¥800/晚</div>
              <el-button type="primary" class="book-btn">立即预订</el-button>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="package-card">
              <h3>家庭套房</h3>
              <p class="package-desc">宽敞舒适，适合家庭入住</p>
              <div class="package-price">¥1200/晚</div>
              <el-button type="primary" class="book-btn">立即预订</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 房型展示 -->
    <section class="rooms-section">
      <h2 class="section-title">探索我们精致的客房</h2>
      <p class="section-desc">为您打造难忘的住宿体验</p>
      <el-row :gutter="30">
        <el-col :span="8">
          <div class="room-card">
            <img src="/src/assets/room1.jpg" class="room-image" alt="豪华客房" />
            <h3>豪华客房：享受优雅</h3>
            <p>我们的豪华客房配备豪华床具、现代化设施和令人惊叹的景观。这些房间专为休闲和焕发活力而设计，让您享受难忘的住宿体验。</p>
            <el-button type="primary" class="learn-more-btn">了解更多</el-button>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="room-card">
            <img src="/src/assets/room2.jpg" class="room-image" alt="家庭套房" />
            <h3>家庭套房：为每个人打造空间</h3>
            <p>我们的家庭套房提供充足的空间，让全家人舒适入住。配备齐全的设施和亲子友好的设计，这些套房非常适合创造持久的家庭回忆。</p>
            <el-button type="primary" class="learn-more-btn">了解更多</el-button>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="room-card">
            <img src="/src/assets/room3.jpg" class="room-image" alt="标准客房" />
            <h3>标准客房：舒适与价值</h3>
            <p>体验我们舒适便利的标准客房。配备精选床具和所有基本设施，这些房间为您提供愉快的住宿体验，让您焕然一新。</p>
            <el-button type="primary" class="learn-more-btn">了解更多</el-button>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 体验与设施 -->
    <section class="experience-section">
      <h2 class="section-title">体验无与伦比的舒适与奢华</h2>
      <p class="section-desc">在和庆酒店，我们提供丰富的设施，让您的住宿更加完美。从精致的餐饮选择到放松的休闲设施，我们拥有一切您需要的完美度假体验。我们致力于提供卓越的服务，确保您享受最佳舒适。</p>
      
      <div class="amenities">
        <el-row :gutter="30">
          <el-col :span="12">
            <div class="amenity-card">
              <i class="el-icon-food"></i>
              <h4>餐饮美食</h4>
              <p>在我们的餐厅享受美食盛宴。</p>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="amenity-card">
              <i class="el-icon-sunny"></i>
              <h4>休闲娱乐</h4>
              <p>在泳池放松或享受休闲活动。</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 客户评价 -->
    <section class="reviews-section">
      <h2 class="section-title">客户评价</h2>
      <p class="section-desc">听听我们的客人对和庆酒店的评价</p>
      
      <el-row :gutter="30">
        <el-col :span="8">
          <div class="review-card">
            <div class="review-content">
              <p>"在和庆的住宿体验非常愉快！工作人员热情周到，环境优美宜人，是放松身心的完美之选。期待下次再来！"</p>
            </div>
            <div class="reviewer">
              <img src="/src/assets/avatar1.jpg" alt="评价者" class="reviewer-avatar" />
              <div class="reviewer-info">
                <h4>张先生</h4>
                <p>商务旅客</p>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="review-card">
            <div class="review-content">
              <p>"在和庆度过了美好的时光。房间布置精美，地理位置便利，非常适合探索城市。强烈推荐给寻找宁静度假的旅客！"</p>
            </div>
            <div class="reviewer">
              <img src="/src/assets/avatar2.jpg" alt="评价者" class="reviewer-avatar" />
              <div class="reviewer-info">
                <h4>李女士</h4>
                <p>度假旅客</p>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="review-card">
            <div class="review-content">
              <p>"和庆酒店超出了我的期望！设施一流，服务无可挑剔。整个住宿过程都感受到被精心照顾，真是城市中的一片净土！"</p>
            </div>
            <div class="reviewer">
              <img src="/src/assets/avatar3.jpg" alt="评价者" class="reviewer-avatar" />
              <div class="reviewer-info">
                <h4>王先生</h4>
                <p>家庭出游</p>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 位置信息 -->
    <section class="location-section">
      <h2 class="section-title">位置信息</h2>
      <el-row :gutter="40">
        <el-col :span="12">
          <div class="location-info">
            <p>鹤清酒店位于：江西省南昌市八一公园旁</p>
            <el-button type="primary" class="direction-btn">获取导航</el-button>
          </div>
        </el-col>
        <el-col :span="12">
          <div id="map-container" class="map-container"></div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

// 轮播图数据
const hotelImages = [
  '/src/assets/hotel1.jpg',
  '/src/assets/hotel2.jpg',
  '/src/assets/hotel3.jpg'
]

// 注册表单数据
const registerForm = ref({
  phone: '',
  email: ''
})

// 房型数据
const rooms = [
  {
    id: 1,
    name: '豪华大床房',
    image: '/src/assets/room1.jpg',
    description: '舒适宽敞的空间，配备1.8米大床，让您享受优质睡眠'
  },
  {
    id: 2,
    name: '行政套房',
    image: '/src/assets/room2.jpg',
    description: '独立的客厅和卧室空间，尊享商务人士的理想选择'
  },
  {
    id: 3,
    name: '家庭套房',
    image: '/src/assets/room3.jpg',
    description: '温馨舒适的家庭房，适合亲子出游'
  }
]

// 初始化地图
onMounted(() => {
  AMapLoader.load({
    key: 'your-amap-key',
    version: '2.0'
  }).then((AMap) => {
    const map = new AMap.Map('map-container', {
      center: [115.892151, 28.682892], // 南昌八一公园附近坐标
      zoom: 15
    })
    const marker = new AMap.Marker({
      position: [115.892151, 28.682892],
      title: '鹤清酒店'
    })
    map.add(marker)
  })
})
</script>

<style scoped>
.home {
  background-color: #ffffff;
  color: #333333;
  font-family: "Microsoft YaHei", "SimSun", serif;
}

.section-title {
  color: #1a1a1a;
  font-family: "Microsoft YaHei", "SimSun", serif;
  font-weight: 600;
  letter-spacing: 2px;
}

.carousel-text {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  border-radius: 8px;
}

.story-card {
  background: #ffffff;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.story-card:hover {
  transform: translateY(-5px);
}

.package-card {
  background: #ffffff;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.package-card.featured {
  background: linear-gradient(135deg, #1a1a1a 0%, #333333 100%);
}

.room-card {
  background: #ffffff;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.review-card {
  background: #ffffff;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.location-info {
  background: #ffffff;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.hero-section {
  margin-bottom: 80px;
}

.carousel-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.carousel-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.5));
}

.carousel-text {
  text-align: center;
  color: white;
  padding: 40px;
  border-radius: 4px;
  max-width: 900px;
  position: relative;
  z-index: 2;
}

.carousel-text h1 {
  font-size: 3.5em;
  margin-bottom: 25px;
  font-weight: 600;
  letter-spacing: 3px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.carousel-text p {
  font-size: 1.4em;
  line-height: 1.8;
  letter-spacing: 1px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
}

.section-title {
  text-align: center;
  font-size: 2em;
  margin-bottom: 20px;
}

.section-desc {
  text-align: center;
  color: #666;
  margin-bottom: 40px;
}

.story-section {
  margin-bottom: 80px;
}

.story-card {
  text-align: center;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 12px;
  height: 100%;
}

.story-card i {
  font-size: 2.5em;
  color: #409EFF;
  margin-bottom: 20px;
}

.story-card h3 {
  margin-bottom: 15px;
  color: #333;
}

.packages-section {
  margin-bottom: 80px;
}

.package-card {
  text-align: center;
  padding: 30px;
  border: 1px solid #eee;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.package-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.package-card.featured {
  background: #1d3557;
  color: white;
}

.package-price {
  font-size: 2em;
  font-weight: 600;
  margin: 25px 0;
  color: #1a1a1a;
  font-family: "Times New Roman", serif;
}

.featured .package-price {
  color: #fff;
}

.book-btn {
  width: 80%;
  height: 40px;
  border-radius: 4px;
  font-size: 1.1em;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.intro-section {
  margin-bottom: 60px;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  margin: 10px 0;
  padding-left: 20px;
  position: relative;
}

.feature-list li:before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #409EFF;
}

.register-card {
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-btn {
  width: 100%;
  margin-top: 20px;
}

.features-section {
  margin-bottom: 60px;
}

.feature-card {
  padding: 20px;
  text-align: center;
  height: 100%;
  background: #f5f7fa;
  border-radius: 8px;
}

.rooms-section {
  margin-bottom: 60px;
}

.room-card {
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 40px;
  transition: all 0.4s ease;
  background: #ffffff;
  position: relative;
  overflow: hidden;
}

.room-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.room-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 25px;
  transition: transform 0.4s ease;
}

.room-card:hover .room-image {
  transform: scale(1.05);
}

.learn-more-btn {
  width: 100%;
  margin-top: 20px;
}

.experience-section {
  margin-bottom: 80px;
  background: #f8f9fa;
  padding: 60px 0;
}

.amenity-card {
  text-align: center;
  padding: 40px;
  background: #ffffff;
  border-radius: 8px;
  margin-bottom: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
}

.amenity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}

.amenity-card i {
  font-size: 3em;
  color: #1a1a1a;
  margin-bottom: 25px;
  transition: transform 0.3s ease;
}

.amenity-card:hover i {
  transform: scale(1.1);
}

.reviews-section {
  margin-bottom: 80px;
}

.review-card {
  background: #ffffff;
  padding: 35px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  height: 100%;
  transition: all 0.3s ease;
}

.review-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}

.review-content {
  margin-bottom: 25px;
  font-style: italic;
  line-height: 1.8;
  color: #333333;
  font-size: 1.1em;
}

.reviewer {
  display: flex;
  align-items: center;
}

.reviewer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  margin-right: 15px;
}

.reviewer-info h4 {
  margin: 0;
  color: #333;
}

.reviewer-info p {
  margin: 5px 0 0;
  color: #666;
}

.location-section {
  margin-bottom: 80px;
}

.location-info {
  padding: 30px;
  background: #f8f9fa;
  border-radius: 12px;
}

.direction-btn {
  margin-top: 20px;
}

.map-container {
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
}

.contact-info p {
  margin: 15px 0;
}

.business-hours {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}
</style>