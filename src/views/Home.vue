<template>
  <div class="home" ref="homeContainer">
    <!-- 酒店介绍区域 -->
    <section class="hero-section">
      <el-carousel height="600px" arrow="always" indicator-position="none">
        <el-carousel-item v-for="(image, index) in hotelImages" :key="index">
          <div class="carousel-content" :style="{ backgroundImage: `url(${image})` }">
            <div class="overlay"></div>
            <div class="carousel-text">
              <h1>鹤清酒店 - 仙居雅境</h1>
              <p>在这里，我们将典雅与禅意完美融合，为您打造一方静谧的仙居之所。让每一位宾客感受超凡脱俗的居停体验。</p>
              <el-button type="primary" class="hero-btn" @click="router.push('/booking')">立即预订</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 酒店故事 -->
    <section class="story-section">
      <div class="section-header">
        <h2 class="section-title">探索鹤清故事</h2>
        <div class="title-underline"></div>
        <p class="section-desc">在鹤清酒店，我们以仙鹤为意，以典雅为魂，为宾客打造一方超然脱俗的栖居之所。</p>
      </div>
      
      <div class="story-cards">
        <el-row :gutter="40">
          <el-col :span="8">
            <div class="story-card">
              <div class="story-icon"><i class="el-icon-time"></i></div>
              <h3>我们的历史</h3>
              <p>创立于2010年，鹤清酒店秉承"仙居雅境"的理念，以典雅高雅的建筑风格和精致考究的中式装潢闻名。我们将传统文化与现代舒适完美融合，创造出独特的东方居家体验。</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="story-card">
              <div class="story-icon"><i class="el-icon-aim"></i></div>
              <h3>我们的使命</h3>
              <p>我们的使命是为每位宾客营造一方净土，让身心在此获得升华。以东方美学为基调，以细致入微的服务为根本，带来超越期待的居停体验。</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="story-card">
              <div class="story-icon"><i class="el-icon-star-on"></i></div>
              <h3>我们的价值观</h3>
              <p>我们秉持"和谐、典雅、至诚"的价值观，将东方待客之道发挥极致。以文化为魂，以服务为本，让每位宾客感受超然脱俗的待遇。</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 套餐展示 -->
    <section class="packages-section">
      <div class="section-header">
        <h2 class="section-title">尊享套餐</h2>
        <div class="title-underline"></div>
        <p class="section-desc">探索我们为您精心设计的难忘住宿套餐</p>
      </div>
      
      <div class="package-cards">
        <el-row :gutter="30">
          <el-col :span="8">
            <div class="package-card">
              <div class="package-header">
                <h3>标准房</h3>
                <div class="package-price">¥500<span>/晚</span></div>
              </div>
              <p class="package-desc">适合单人或双人寻求舒适的旅客</p>
              <ul class="package-features">
                <li>高品质床品</li>
                <li>免费Wi-Fi</li>
                <li>24小时客房服务</li>
              </ul>
              <el-button type="primary" class="book-btn" @click="router.push('/booking')">立即预订</el-button>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="package-card featured">
              <div class="package-ribbon">推荐</div>
              <div class="package-header">
                <h3>豪华房</h3>
                <div class="package-price">¥800<span>/晚</span></div>
              </div>
              <p class="package-desc">为追求奢华体验的客人精心打造</p>
              <ul class="package-features">
                <li>豪华特大床</li>
                <li>免费迷你吧</li>
                <li>私人管家服务</li>
                <li>高级SPA体验</li>
              </ul>
              <el-button type="primary" class="book-btn" @click="router.push('/booking')">立即预订</el-button>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="package-card">
              <div class="package-header">
                <h3>家庭套房</h3>
                <div class="package-price">¥1200<span>/晚</span></div>
              </div>
              <p class="package-desc">宽敞舒适，适合家庭入住</p>
              <ul class="package-features">
                <li>连通客房选项</li>
                <li>儿童活动区</li>
                <li>专属家庭餐饮服务</li>
              </ul>
              <el-button type="primary" class="book-btn" @click="router.push('/booking')">立即预订</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 房型展示 -->
    <section class="rooms-section">
      <div class="section-header">
        <h2 class="section-title">精致客房</h2>
        <div class="title-underline"></div>
        <p class="section-desc">为您打造难忘的住宿体验</p>
      </div>
      <el-row :gutter="30">
        <el-col :span="8" v-for="(room, index) in rooms" :key="room.id">
          <div class="room-card">
            <div class="room-image-container">
              <img :src="room.image" class="room-image" :alt="room.name" />
              <div class="room-overlay">
                <el-button type="primary" class="view-details-btn" @click="router.push('/rooms')">查看详情</el-button>
              </div>
            </div>
            <div class="room-info">
              <h3>{{ room.name }}</h3>
              <p>{{ room.description }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 客户评价 -->
    <section class="reviews-section">
      <div class="section-header">
        <h2 class="section-title">宾客评价</h2>
        <div class="title-underline"></div>
        <p class="section-desc">聆听我们的宾客对和庆酒店的真实评价</p>
      </div>
      
      <el-row :gutter="30">
        <el-col :span="8">
          <div class="review-card">
            <div class="review-quote">"</div>
            <div class="review-content">
              <p>在和庆的住宿体验非常愉快！工作人员热情周到，环境优美宜人，是放松身心的完美之选。期待下次再来！</p>
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
            <div class="review-quote">"</div>
            <div class="review-content">
              <p>在和庆度过了美好的时光。房间布置精美，地理位置便利，非常适合探索城市。强烈推荐给寻找宁静度假的旅客！</p>
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
            <div class="review-quote">"</div>
            <div class="review-content">
              <p>和庆酒店超出了我的期望！设施一流，服务无可挑剔。整个住宿过程都感受到被精心照顾，真是城市中的一片净土！</p>
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
      <div class="section-header">
        <h2 class="section-title">位置信息</h2>
        <div class="title-underline"></div>
      </div>
      <el-row :gutter="40">
        <el-col :span="12">
          <div class="location-info">
            <div class="location-address">
              <i class="el-icon-location"></i>
              <p>鹤清酒店位于：江西省南昌市八一公园旁</p>
            </div>
            <div class="location-contact">
              <p><i class="el-icon-phone"></i> 电话：0791-88888888</p>
              <p><i class="el-icon-message"></i> 邮箱：info@heqinghotel.com</p>
            </div>
            <el-button type="primary" class="direction-btn" @click="handleNavigation">获取导航</el-button>
          </div>
        </el-col>
        <el-col :span="12">
          <div id="map-container" class="map-container"></div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';

export default {
  setup() {
    const homeContainer = ref(null);

    // 导航按钮点击事件处理函数
    const handleNavigation = () => {
      // 鹤清酒店的位置坐标（南昌八一公园附近）
      const destination = '115.892151,28.682892';
      // 使用高德地图导航
      window.location.href = `https://uri.amap.com/navigation?to=${destination},鹤清酒店&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=0`;
    };

    onMounted(() => {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.style.opacity = '0';
            entry.target.style.transform = entry.target.dataset.transform || 'translateY(50px)';
            
            requestAnimationFrame(() => {
              entry.target.style.transition = 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)';
              entry.target.style.opacity = '1';
              entry.target.style.transform = 'none';
            });
          }
        });
      }, {
        threshold: 0.1,
        rootMargin: '-50px 0px -100px 0px'
      });

      const sections = document.querySelectorAll('.story-section, .packages-section, .rooms-section, .experience-section, .reviews-section, .location-section');
      sections.forEach((section, index) => {
        const transforms = [
          'translateX(-50px)',
          'translateX(50px)',
          'translateY(50px) scale(0.95)',
          'translateX(-50px) rotate(-2deg)',
          'translateX(50px) rotate(2deg)',
          'translateY(50px) scale(0.98)'
        ];
        section.style.opacity = '0';
        section.dataset.transform = transforms[index % transforms.length];
        observer.observe(section);
      });
    });

    return { homeContainer };
  }
};
</script>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AMapLoader from '@amap/amap-jsapi-loader'

const router = useRouter()

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
    key: 'ad101aa6969036479910b4a3988add24',
    version: '2.0',
    plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.ControlBar', 'AMap.MapType']
  }).then((AMap) => {
    const map = new AMap.Map('map-container', {
      center: [115.892151, 28.682892], // 南昌八一公园附近坐标
      zoom: 15,
      viewMode: '3D',
      pitch: 35,
      mapStyle: 'amap://styles/normal',
      defaultLayer: new AMap.TileLayer(),
      layers: [
        new AMap.TileLayer()
      ]
    })
    const marker = new AMap.Marker({
      position: [115.892151, 28.682892],
      title: '鹤清酒店',
      label: {
        content: '鹤清酒店',
        direction: 'top',
        offset: [0, -36]
      },
      animation: 'AMAP_ANIMATION_BOUNCE'
    })
    map.add(marker)
    map.addControl(new AMap.Scale())
    map.addControl(new AMap.ToolBar())
    map.addControl(new AMap.ControlBar())
  })
})
</script>

<style scoped>
.home {
  background-color: #ffffff;
  color: #333333;
  font-family: "Microsoft YaHei", "SimSun", serif;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
}

.section-title {
  color: #1a1a1a;
  font-family: "Times New Roman", "SimSun", serif;
  font-weight: 600;
  letter-spacing: 2px;
  position: relative;
  margin-bottom: 20px;
  font-size: 2.5em;
}

.title-underline {
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #c59d5f, transparent);
  margin: 0 auto 25px;
}

.section-desc {
  color: #666;
  max-width: 800px;
  margin: 0 auto 30px;
  font-size: 1.1em;
  line-height: 1.6;
}

.hero-section {
  width: 100%;
  height: 100vh;
  margin-bottom: 80px;
  position: relative;
  overflow: hidden;
}

.carousel-content {
  height: 100%;
  width: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: white;
  position: relative;
  transform: scale(1);
  transition: transform 8s ease-in-out;
  overflow: hidden;
}

.carousel-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.6));
  z-index: 1;
}

.el-carousel__item--active .carousel-content {
  transform: scale(1.1);
}

.el-carousel__item--active .carousel-text {
  transform: translateY(0);
  opacity: 1;
}

.el-carousel__item:not(.el-carousel__item--active) .carousel-text {
  transform: translateY(20px);
  opacity: 0;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.carousel-text {
  max-width: 900px;
  padding: 60px;
  position: relative;
  z-index: 2;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  transform: translateY(0);
  transition: transform 0.8s ease-out, opacity 0.8s ease-out;
  opacity: 0.95;
}

.carousel-text h1 {
  font-size: 56px;
  margin-bottom: 20px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 3px;
  color: #ffffff;
  font-family: "Times New Roman", "SimSun", serif;
}

.carousel-text p {
  font-size: 22px;
  line-height: 1.8;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  color: #f0f0f0;
  max-width: 700px;
  margin: 0 auto 30px;
}

.hero-btn {
  padding: 12px 30px;
  font-size: 18px;
  background: #c59d5f;
  border-color: #c59d5f;
  transition: all 0.3s ease;
}

.hero-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.2);
}

.story-section,
.packages-section,
.rooms-section,
.experience-section,
.reviews-section,
.location-section {
  margin: 100px auto;
  max-width: 1200px;
  padding: 0 20px;
  opacity: 1;
  transform: none;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.story-card {
  text-align: center;
  padding: 40px 30px;
  background: #ffffff;
  border-radius: 8px;
  height: 100%;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  transition: all 0.4s ease;
  border: 1px solid #f0f0f0;
}

.story-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
}

.story-icon {
  font-size: 2.5em;
  color: #c59d5f;
  margin-bottom: 20px;
  display: inline-block;
  width: 80px;
  height: 80px;
  line-height: 80px;
  border-radius: 50%;
  background: #f9f5eb;
}

.story-card h3 {
  margin-bottom: 15px;
  color: #333;
  font-size: 1.5em;
  font-family: "Times New Roman", "SimSun", serif;
}

.story-card p {
  line-height: 1.8;
  color: #666;
}

.package-card {
  text-align: center;
  padding: 40px 30px;
  border-radius: 8px;
  transition: all 0.4s ease;
  background: #ffffff;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  position: relative;
  overflow: hidden;
  height: 100%;
  border: 1px solid #f0f0f0;
}

.package-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
}

.package-card.featured {
  background: linear-gradient(135deg, #2d3436 0%, #1a1a1a 100%);
  color: white;
  border: none;
}

.package-ribbon {
  position: absolute;
  top: 20px;
  right: -30px;
  transform: rotate(45deg);
  background: #c59d5f;
  color: white;
  padding: 5px 40px;
  font-size: 0.9em;
  font-weight: bold;
  letter-spacing: 1px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.package-header {
  margin-bottom: 20px;
}

.package-card h3 {
  font-size: 1.8em;
  margin-bottom: 10px;
  font-family: "Times New Roman", "SimSun", serif;
}

.package-price {
  font-size: 2.5em;
  font-weight: 600;
  margin: 15px 0;
  color: #c59d5f;
  font-family: "Times New Roman", serif;
}

.package-price span {
  font-size: 0.4em;
  font-weight: normal;
  vertical-align: middle;
}

.featured .package-price {
  color: #c59d5f;
}

.package-desc {
  margin-bottom: 20px;
  font-size: 1.1em;
}

.package-features {
  list-style: none;
  padding: 0;
  margin: 0 0 30px;
  text-align: left;
}

.package-features li {
  padding: 8px 0;
  position: relative;
  padding-left: 25px;
  border-bottom: 1px dashed #eee;
}

.package-features li:last-child {
  border-bottom: none;
}

.package-features li:before {
  content: "✓";
  position: absolute;
  left: 0;
  color: #c59d5f;
}

.featured .package-features li:before {
  color: #c59d5f;
}

.featured .package-features li {
  border-color: rgba(255,255,255,0.1);
}

.book-btn {
  width: 100%;
  height: 45px;
  border-radius: 4px;
  font-size: 1.1em;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  background: #c59d5f;
  border-color: #c59d5f;
}

.book-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  background: #b58d40;
  border-color: #b58d40;
}

.room-card {
  background: #ffffff;
  border-radius: 8px;
  margin-bottom: 40px;
  transition: all 0.4s ease;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  overflow: hidden;
  height: 100%;
}

.room-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
}

.room-image-container {
  position: relative;
  overflow: hidden;
}

.room-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.room-card:hover .room-image {
  transform: scale(1.05);
}

.room-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.4s ease;
}

.room-card:hover .room-overlay {
  opacity: 1;
}

.view-details-btn {
  background: #c59d5f;
  border-color: #c59d5f;
  padding: 12px 25px;
}

.view-details-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
}

.room-info {
  padding: 25px;
}

.room-info h3 {
  margin-bottom: 15px;
  font-size: 1.5em;
  font-family: "Times New Roman", "SimSun", serif;
}

.room-info p {
  color: #666;
  line-height: 1.6;
}

.review-card {
  background: #ffffff;
  padding: 40px 30px;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  height: 100%;
  transition: all 0.4s ease;
  position: relative;
  border: 1px solid #f0f0f0;
}

.review-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
}

.review-quote {
  position: absolute;
  top: 20px;
  left: 20px;
  font-size: 70px;
  line-height: 1;
  color: #f0f0f0;
  font-family: "Times New Roman", serif;
}

.review-content {
  margin-bottom: 25px;
  font-style: italic;
  line-height: 1.8;
  color: #333333;
  font-size: 1.1em;
  position: relative;
  z-index: 1;
}

.reviewer {
  display: flex;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.reviewer-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 15px;
  object-fit: cover;
  border: 3px solid #f0f0f0;
}

.reviewer-info h4 {
  margin: 0;
  color: #333;
  font-size: 1.2em;
}

.reviewer-info p {
  margin: 5px 0 0;
  color: #999;
  font-size: 0.9em;
}

.location-info {
  padding: 40px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.location-address {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30px;
}

.location-address i {
  font-size: 24px;
  color: #c59d5f;
  margin-right: 15px;
  margin-top: 3px;
}

.location-address p {
  font-size: 1.2em;
  line-height: 1.6;
}

.location-contact {
  margin-bottom: 30px;
}

.location-contact p {
  margin: 15px 0;
  font-size: 1.1em;
}

.location-contact i {
  color: #c59d5f;
  margin-right: 10px;
}

.direction-btn {
  padding: 12px 25px;
  width: 100%;
  background: #c59d5f;
  border-color: #c59d5f;
  transition: all 0.3s ease;
}

.direction-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.map-container {
  height: 100%;
  min-height: 400px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
}
</style>