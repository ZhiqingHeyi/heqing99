<template>
  <div class="home" ref="homeContainer">
    <!-- 酒店介绍区域 -->
    <section class="hero-section">
      <div class="hero-carousel-container">
        <el-carousel height="700px" arrow="always" indicator-position="none" :interval="5000" type="card">
          <el-carousel-item v-for="(image, index) in hotelImages" :key="index">
            <div class="carousel-content" :style="{ backgroundImage: `url(${image})` }">
              <div class="overlay"></div>
              <div class="carousel-text">
                <div class="logo-emblem">
                  <img src="/src/assets/icons/crane-logo.svg" alt="鹤清酒店" class="logo-image" />
                </div>
                <h1>鹤清酒店 · 雅境天成</h1>
                <p>东方美学与奢华体验的完美融合，为您打造一方超然脱俗的仙境居所。聆听山水，感悟东方雅韵。</p>
                <el-button type="primary" class="hero-btn" @click="router.push('/booking')">立即预订</el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="scroll-indicator">
        <span>向下滚动探索更多</span>
        <i class="el-icon-arrow-down scroll-arrow"></i>
      </div>
    </section>

    <!-- 品牌精髓 -->
    <section class="essence-section">
      <div class="essence-background"></div>
      <div class="essence-content">
        <div class="essence-left">
          <h2>匠心传承</h2>
          <div class="title-decorator">
            <span class="line"></span>
            <span class="dot"></span>
            <span class="line"></span>
          </div>
          <p>鹤清酒店融合东方智慧与现代奢华，打造一方静谧的艺术净土。我们秉承"东方美学"理念，每一处设计、每一项服务都蕴含匠心。</p>
          <div class="essence-stats">
            <div class="stat-item">
              <span class="stat-number">13</span>
              <span class="stat-label">年臻心服务</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">99%</span>
              <span class="stat-label">宾客满意度</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">50+</span>
              <span class="stat-label">奢华套房</span>
            </div>
          </div>
        </div>
        <div class="essence-right">
          <div class="essence-image-container">
            <img src="/src/assets/hotel1.jpg" alt="酒店精髓" class="essence-image" />
            <div class="image-decorator"></div>
          </div>
        </div>
      </div>
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
              <div class="story-icon">
                <img src="/src/assets/icons/history.svg" alt="历史" class="story-logo" />
              </div>
              <h3>我们的历史</h3>
              <p>创立于2010年，鹤清酒店秉承"仙居雅境"的理念，以典雅高雅的建筑风格和精致考究的中式装潢闻名。我们将传统文化与现代舒适完美融合，创造出独特的东方居家体验。</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="story-card">
              <div class="story-icon">
                <img src="/src/assets/icons/mission.svg" alt="使命" class="story-logo" />
              </div>
              <h3>我们的使命</h3>
              <p>我们的使命是为每位宾客营造一方净土，让身心在此获得升华。以东方美学为基调，以细致入微的服务为根本，带来超越期待的居停体验。</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="story-card">
              <div class="story-icon">
                <img src="/src/assets/icons/values.svg" alt="价值观" class="story-logo" />
              </div>
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
        <p class="section-desc">探索我们为您精心设计的难忘住宿体验</p>
      </div>
      
      <div class="package-slider">
        <el-carousel :interval="5000" type="card" height="550px">
          <el-carousel-item v-for="(pkg, index) in packages" :key="index">
            <div class="luxury-package-card" :class="{'featured': pkg.featured}">
              <div v-if="pkg.featured" class="package-ribbon">推荐</div>
              <div class="package-image" :style="{backgroundImage: `url(${pkg.image})`}">
                <div class="package-overlay"></div>
                <div class="package-title">
                  <h3>{{pkg.name}}</h3>
                  <div class="package-price">¥{{pkg.price}}<span>/{{pkg.unit}}</span></div>
                </div>
              </div>
              <div class="package-body">
                <div class="package-room-type">
                  <span class="room-type-label">房型:</span>
                  <span class="room-type-value">{{pkg.roomType}}</span>
                  <span class="room-size-label">面积:</span>
                  <span class="room-size-value">{{pkg.roomSize}}</span>
                </div>
                <p class="package-desc">{{pkg.description}}</p>
                <ul class="package-features">
                  <li v-for="(feature, idx) in pkg.features" :key="idx">{{feature}}</li>
                </ul>
                <el-button type="primary" class="book-btn" @click="router.push('/booking')">立即预订</el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- 奢华体验 -->
    <section class="experience-section">
      <div class="section-header">
        <h2 class="section-title">奢华体验</h2>
        <div class="title-underline"></div>
        <p class="section-desc">探索鹤清酒店为您带来的非凡体验</p>
      </div>
      
      <div class="experience-grid">
        <div class="experience-item" v-for="(exp, index) in experiences" :key="index">
          <div class="experience-icon">
            <img :src="exp.logoUrl" alt="体验图标" class="experience-logo" />
          </div>
          <h3>{{exp.title}}</h3>
          <p>{{exp.description}}</p>
        </div>
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
        <p class="section-desc">聆听我们的宾客对鹤清酒店的真实评价</p>
      </div>
      
      <el-carousel :interval="4000" type="card" height="450px" class="reviews-carousel">
        <el-carousel-item v-for="(review, index) in reviews" :key="index">
          <div class="review-card">
            <div class="review-quote">"</div>
            <div class="review-content">
              <p>{{review.content}}</p>
            </div>
            <div class="reviewer">
              <div class="reviewer-avatar">
                <span v-if="!review.avatar">{{review.initial}}</span>
                <img v-else :src="review.avatar" alt="评价者" @error="handleImageError($event, review)" />
              </div>
              <div class="reviewer-info">
                <h4>{{review.name}}</h4>
                <p>{{review.type}}</p>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 位置信息 -->
    <section class="location-section">
      <div class="section-header">
        <h2 class="section-title">位置信息</h2>
        <div class="title-underline"></div>
      </div>
      <div class="location-container">
        <el-row :gutter="40">
          <el-col :xs="24" :sm="24" :md="12" :lg="10">
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
          <el-col :xs="24" :sm="24" :md="12" :lg="14">
            <div id="map-container" class="map-container"></div>
          </el-col>
        </el-row>
      </div>
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

// 奢华套餐数据
const packages = [
  {
    name: '典雅休闲套餐',
    price: '888',
    unit: '晚',
    description: '沉浸在东方式的典雅氛围中，体验高品质的休闲时光',
    image: '/src/assets/room1.jpg',
    features: ['高级客房住宿', '双人早餐', '免费迷你吧', '欢迎水果篮', '延迟退房服务'],
    featured: false,
    roomType: '豪华大床房',
    roomSize: '38平方米'
  },
  {
    name: '豪华度假套餐',
    price: '1688',
    unit: '晚',
    description: '全方位的奢华体验，是休闲度假的不二之选',
    image: '/src/assets/room2.jpg',
    features: ['豪华套房住宿', '24小时管家服务', '双人SPA护理', '免费专车接送', '米其林晚餐体验'],
    featured: true,
    roomType: '行政套房',
    roomSize: '58平方米'
  },
  {
    name: '家庭尊享套餐',
    price: '2388',
    unit: '晚',
    description: '为家庭旅行提供舒适奢华的度假体验',
    image: '/src/assets/room3.jpg',
    features: ['连通家庭套房', '专属儿童活动', '全家SPA体验', '定制家庭餐饮', '私人观光导览'],
    featured: false,
    roomType: '家庭套房',
    roomSize: '75平方米'
  }
]

// 奢华体验数据
const experiences = [
  {
    title: '私人管家',
    logoUrl: '/src/assets/icons/private-concierge.svg',
    description: '全程私人管家服务，满足您的一切需求，尽享无忧入住体验'
  },
  {
    title: '东方SPA',
    logoUrl: '/src/assets/icons/spa.svg',
    description: '融合东方草药与现代技术的SPA体验，让身心彻底放松'
  },
  {
    title: '米其林餐厅',
    logoUrl: '/src/assets/icons/michelin.svg',
    description: '由米其林星级主厨打造的餐饮体验，品味舌尖上的艺术'
  },
  {
    title: '茶艺体验',
    logoUrl: '/src/assets/icons/tea.svg',
    description: '品味东方茶道文化，感受岁月静好的雅致时光'
  },
  {
    title: '专车接送',
    logoUrl: '/src/assets/icons/car.svg',
    description: '豪华专车接送服务，开启您的尊贵旅程'
  },
  {
    title: '定制旅行',
    logoUrl: '/src/assets/icons/travel.svg',
    description: '量身定制专属旅行路线，探索城市秘境'
  }
]

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

// 客户评价数据
const reviews = [
  {
    content: '在鹤清酒店的住宿体验非常愉快！工作人员热情周到，环境优美宜人，是放松身心的完美之选。期待下次再来！',
    avatar: '/src/assets/avatar1.jpg',
    name: '张先生',
    type: '商务旅客',
    initial: 'Z'
  },
  {
    content: '在鹤清度过了美好的时光。房间布置精美，地理位置便利，非常适合探索城市。强烈推荐给寻找宁静度假的旅客！',
    avatar: '/src/assets/avatar2.jpg',
    name: '李女士',
    type: '度假旅客',
    initial: 'L'
  },
  {
    content: '鹤清酒店超出了我的期望！设施一流，服务无可挑剔。整个住宿过程都感受到被精心照顾，真是城市中的一片净土！',
    avatar: '/src/assets/avatar3.jpg',
    name: '王先生',
    type: '家庭出游',
    initial: 'W'
  },
  {
    content: '茶艺体验让人难忘，东方SPA使身心焕然一新。鹤清酒店不只是住宿，更是一场东方美学的盛宴。',
    avatar: '/src/assets/avatar4.jpg',
    name: '陈女士',
    type: '文化体验者',
    initial: 'C'
  },
  {
    content: '米其林餐厅的美食体验堪称完美，私人管家的服务细致入微。鹤清酒店是我在南昌最奢华的选择，值得每一分钱。',
    avatar: '/src/assets/avatar5.jpg',
    name: '刘先生',
    type: '美食爱好者',
    initial: 'L'
  },
  {
    content: '作为常年商旅人士，鹤清酒店是我在南昌的首选。安静典雅的环境让我每次出差都能得到充分休息，宾至如归。',
    avatar: '/src/assets/avatar6.jpg',
    name: '赵女士',
    type: '常客',
    initial: 'Z'
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

// 处理头像加载失败的情况
const handleImageError = (event, review) => {
  event.target.style.display = 'none';
  const parent = event.target.parentNode;
  // 创建一个文本节点显示首字母
  if (!parent.querySelector('span')) {
    const span = document.createElement('span');
    span.textContent = review.initial || review.name.charAt(0);
    parent.appendChild(span);
  }
}
</script>

<style scoped>
.home {
  background-color: #fcfaf7;
  color: #333333;
  font-family: "Microsoft YaHei", "SimSun", serif;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M54.627 0l.83.828-1.415 1.415L51.8 0h2.827zM5.373 0l-.83.828L5.96 2.243 8.2 0H5.374zM48.97 0l3.657 3.657-1.414 1.414L46.143 0h2.828zM11.03 0L7.372 3.657 8.787 5.07 13.857 0H11.03zm32.284 0L49.8 6.485 48.384 7.9l-7.9-7.9h2.83zM16.686 0L10.2 6.485 11.616 7.9l7.9-7.9h-2.83zm20.97 0l9.315 9.314-1.414 1.414L34.828 0h2.83zM22.344 0L13.03 9.314l1.414 1.414L25.172 0h-2.83zM32 0l12.142 12.142-1.414 1.414L30 .828 17.272 13.556l-1.414-1.414L28 0h4zM.284 0l28 28-1.414 1.414L0 2.544V0h.284zM0 5.373l25.456 25.455-1.414 1.415L0 8.2V5.374zm0 5.656l22.627 22.627-1.414 1.414L0 13.86v-2.83zm0 5.656l19.8 19.8-1.415 1.413L0 19.514v-2.83zm0 5.657l16.97 16.97-1.414 1.415L0 25.172v-2.83zM0 28l14.142 14.142-1.414 1.414L0 30.828V28zm0 5.657L11.314 44.97 9.9 46.386l-9.9-9.9v-2.828zm0 5.657L8.485 47.8 7.07 49.212 0 42.143v-2.83zm0 5.657l5.657 5.657-1.414 1.415L0 47.8v-2.83zm0 5.657l2.828 2.83-1.414 1.413L0 53.456v-2.83zM54.627 60L30 35.373 5.373 60H8.2L30 38.2 51.8 60h2.827zm-5.656 0L30 41.03 11.03 60h2.828L30 43.858 46.142 60h2.83zm-5.656 0L30 46.686 16.686 60h2.83L30 49.515 40.485 60h2.83zm-5.657 0L30 52.343 22.344 60h2.83L30 55.172 34.828 60h2.83zM32 60l-2-2-2 2h4zM59.716 0l-28 28 1.414 1.414L60 2.544V0h-.284zM60 5.373L34.544 30.828l1.414 1.415L60 8.2V5.374zm0 5.656L37.373 33.656l1.414 1.414L60 13.86v-2.83zm0 5.656l-19.8 19.8 1.415 1.413L60 19.514v-2.83zm0 5.657l-16.97 16.97 1.414 1.415L60 25.172v-2.83zM60 28L45.858 42.142l1.414 1.414L60 30.828V28zm0 5.657L48.686 44.97l1.415 1.415 9.9-9.9v-2.828zm0 5.657L51.515 47.8l1.414 1.413 7.07-7.07v-2.83zm0 5.657l-5.657 5.657 1.414 1.415L60 47.8v-2.83zm0 5.657l-2.828 2.83 1.414 1.413L60 53.456v-2.83zM39.9 16.385l1.414-1.414L30 3.658 18.686 14.97l1.415 1.415 9.9-9.9 9.9 9.9zm-2.83 2.828l1.415-1.414L30 9.313 21.515 17.8l1.414 1.413L30 11.8l7.07 7.414v-.002z' fill='%23c59d5f' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
  position: relative;
}

.section-header::after {
  content: "";
  position: absolute;
  width: 150px;
  height: 150px;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23c59d5f' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: -1;
  opacity: 0.5;
}

.section-title {
  color: #1a1a1a;
  font-family: "SimSun", serif;
  font-weight: 600;
  letter-spacing: 3px;
  position: relative;
  margin-bottom: 25px;
  font-size: 2.8em;
  display: inline-block;
}

.section-title::before,
.section-title::after {
  content: "";
  position: absolute;
  width: 30px;
  height: 2px;
  background: rgba(197, 157, 95, 0.5);
  top: 50%;
}

.section-title::before {
  left: -45px;
}

.section-title::after {
  right: -45px;
}

.title-underline {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #c59d5f, transparent);
  margin: 0 auto 30px;
}

.section-desc {
  color: #666;
  max-width: 800px;
  margin: 0 auto 30px;
  font-size: 1.2em;
  line-height: 1.8;
  font-family: "Microsoft YaHei", sans-serif;
  text-align: center;
}

.hero-section {
  width: 100%;
  height: 100vh;
  margin-bottom: 120px;
  position: relative;
  overflow: hidden;
}

.hero-carousel-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0;
  position: relative;
  top: 50%;
  transform: translateY(-50%);
}

@media (max-width: 1650px) {
  .hero-carousel-container {
    max-width: 90%;
  }
}

@media (max-width: 768px) {
  .hero-carousel-container {
    max-width: 95%;
  }
  
  .hero-section {
    height: 85vh;
  }
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
  border-radius: 8px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
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
  padding: 80px;
  position: relative;
  z-index: 2;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  transform: translateY(0);
  transition: transform 0.8s ease-out, opacity 0.8s ease-out;
  opacity: 0.95;
}

.logo-emblem {
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
  border-radius: 50%;
  background: radial-gradient(circle, #faf6ed, #f0e6d2);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 5px double rgba(197, 157, 95, 0.6);
  box-shadow: 0 0 30px rgba(197, 157, 95, 0.6);
}

.logo-image {
  width: 80%;
  height: 80%;
  object-fit: contain;
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.story-icon {
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 25px;
  border-radius: 50%;
  background: radial-gradient(circle, #faf6ed, #f0e6d2);
  border: 1px solid rgba(197, 157, 95, 0.3);
  box-shadow: 0 8px 20px rgba(197, 157, 95, 0.15);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.story-icon::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.8), transparent 60%);
  pointer-events: none;
}

.story-logo {
  width: 50px;
  height: 50px;
  object-fit: contain;
  transition: all 0.4s ease;
}

.story-card:hover .story-icon {
  transform: translateY(-5px);
  background: radial-gradient(circle, #f0e6d2, #e8d7ba);
  box-shadow: 0 15px 30px rgba(197, 157, 95, 0.25);
  border-color: rgba(197, 157, 95, 0.6);
}

.story-card:hover .story-logo {
  transform: scale(1.1);
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
  border-radius: 10px;
  margin-bottom: 40px;
  transition: all 0.4s ease;
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
  overflow: hidden;
  height: 100%;
  border: 1px solid #f0f0f0;
}

.room-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border-color: #c59d5f;
}

.room-image-container {
  position: relative;
  overflow: hidden;
}

.room-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.room-card:hover .room-image {
  transform: scale(1.1);
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
  letter-spacing: 1px;
}

.view-details-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
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
  padding: 40px 35px;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  height: 100%;
  transition: all 0.4s ease;
  position: relative;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.review-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border-color: rgba(197, 157, 95, 0.3);
}

.review-quote {
  position: absolute;
  top: 30px;
  left: 30px;
  font-size: 90px;
  line-height: 1;
  color: rgba(197, 157, 95, 0.1);
  font-family: "Times New Roman", serif;
}

.review-content {
  margin-bottom: 30px;
  font-style: italic;
  line-height: 1.8;
  color: #333333;
  font-size: 1.1em;
  position: relative;
  z-index: 1;
  flex: 1;
}

.reviewer {
  display: flex;
  align-items: center;
  padding-top: 25px;
  border-top: 1px solid rgba(197, 157, 95, 0.2);
}

.reviewer-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 20px;
  object-fit: cover;
  border: 3px solid rgba(197, 157, 95, 0.2);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #f0e6d2, #c59d5f);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 22px;
  font-family: "SimSun", serif;
}

.reviewer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
}

.review-card:hover .reviewer-avatar {
  border-color: rgba(197, 157, 95, 0.5);
  transform: scale(1.05);
}

.reviewer-info h4 {
  margin: 0;
  color: #333;
  font-size: 1.3em;
  font-family: "SimSun", serif;
}

.reviewer-info p {
  margin: 5px 0 0;
  color: rgba(197, 157, 95, 0.8);
  font-size: 0.9em;
  letter-spacing: 1px;
}

.location-section {
  margin: 120px auto;
  max-width: 1200px;
  padding: 50px 20px;
  position: relative;
}

.location-container {
  background: linear-gradient(to right, rgba(250, 246, 237, 0.7), rgba(255, 255, 255, 0.9), rgba(250, 246, 237, 0.7));
  border-radius: 10px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
  padding: 40px;
  position: relative;
}

.location-container::before {
  content: "";
  position: absolute;
  top: 15px;
  left: 15px;
  right: 15px;
  bottom: 15px;
  border: 1px solid rgba(197, 157, 95, 0.2);
  border-radius: 8px;
  z-index: 0;
  pointer-events: none;
}

.location-section .el-row {
  align-items: stretch;
  min-height: 400px;
}

.location-info {
  padding: 30px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.05);
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  z-index: 1;
  border: 1px solid rgba(197, 157, 95, 0.1);
  margin-bottom: 20px;
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
  background: linear-gradient(135deg, #c59d5f, #ddbf85);
  border-color: #c59d5f;
  transition: all 0.3s ease;
  font-weight: 500;
}

.direction-btn:hover {
  background: linear-gradient(135deg, #b58d40, #c59d5f);
  border-color: #b58d40;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.map-container {
  height: 100%;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  border: 1px solid rgba(197, 157, 95, 0.1);
}

/* 品牌精髓部分 */
.essence-section {
  position: relative;
  padding: 100px 0;
  margin: 120px auto;
  overflow: hidden;
}

.essence-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f9f5eb;
  clip-path: polygon(0 15%, 100% 0, 100% 85%, 0 100%);
  z-index: -1;
}

.essence-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.essence-left {
  flex: 1;
  padding-right: 60px;
}

.essence-left h2 {
  font-size: 48px;
  color: #333;
  margin-bottom: 20px;
  font-family: "Times New Roman", "SimSun", serif;
}

.title-decorator {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.title-decorator .line {
  height: 2px;
  width: 40px;
  background: #c59d5f;
}

.title-decorator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c59d5f;
  margin: 0 10px;
}

.essence-left p {
  font-size: 18px;
  line-height: 1.8;
  margin-bottom: 40px;
  color: #555;
}

.essence-stats {
  display: flex;
  justify-content: space-between;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 48px;
  font-weight: bold;
  color: #c59d5f;
  font-family: "Times New Roman", serif;
  margin-bottom: 10px;
}

.stat-label {
  color: #555;
  font-size: 16px;
}

.essence-right {
  flex: 1;
}

.essence-image-container {
  position: relative;
  padding: 20px;
}

.essence-image {
  width: 100%;
  height: 500px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  position: relative;
  z-index: 1;
}

.image-decorator {
  position: absolute;
  top: 0;
  left: 0;
  right: 40px;
  bottom: 40px;
  border: 5px solid #c59d5f;
  border-radius: 10px;
  z-index: 0;
}

/* 套餐卡片新样式 */
.package-slider {
  margin: 50px auto;
}

.luxury-package-card {
  height: 100%;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
  position: relative;
  transition: all 0.4s ease;
  display: flex;
  flex-direction: column;
}

.luxury-package-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.package-image {
  height: 250px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.package-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.7));
}

.package-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px;
  color: white;
}

.package-title h3 {
  font-size: 28px;
  margin-bottom: 10px;
  font-family: "Times New Roman", "SimSun", serif;
}

.package-body {
  padding: 30px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.package-room-type {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  border-bottom: 1px dashed #f0f0f0;
  padding-bottom: 15px;
}

.room-type-label, .room-size-label {
  color: #999;
  margin-right: 5px;
  font-size: 14px;
}

.room-type-value, .room-size-value {
  color: #c59d5f;
  font-weight: bold;
  margin-right: 20px;
  font-size: 16px;
}

.package-desc {
  font-size: 16px;
  color: #555;
  margin-bottom: 20px;
  flex: 0 0 auto;
}

.package-features {
  list-style: none;
  padding: 0;
  margin: 0 0 30px;
  flex: 1 0 auto;
}

.package-features li {
  padding: 10px 0;
  position: relative;
  padding-left: 30px;
  border-bottom: 1px dashed #eee;
}

.package-features li:before {
  content: "✓";
  position: absolute;
  left: 0;
  color: #c59d5f;
  font-weight: bold;
}

/* 奢华体验区域 */
.experience-section {
  margin: 120px auto;
  max-width: 1200px;
  padding: 0 20px;
  position: relative;
}

.experience-section::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(197, 157, 95, 0.3), transparent);
  z-index: -1;
}

.experience-section::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(197, 157, 95, 0.3), transparent);
  z-index: -1;
}

.experience-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-top: 60px;
}

@media (max-width: 992px) {
  .experience-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .experience-grid {
    grid-template-columns: 1fr;
  }
}

.experience-item {
  background: white;
  padding: 40px 30px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: all 0.5s ease;
  border: 1px solid #f0f0f0;
  position: relative;
  z-index: 1;
}

.experience-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(145deg, #ffffff, #f5f5f5);
  border-radius: 10px;
  z-index: -1;
  transition: all 0.5s ease;
}

.experience-item:hover {
  transform: translateY(-15px);
  box-shadow: 0 20px 40px rgba(197, 157, 95, 0.15);
  border-color: rgba(197, 157, 95, 0.3);
}

.experience-item:hover::before {
  background: linear-gradient(145deg, #ffffff, #faf6f0);
}

.experience-icon {
  width: 100px;
  height: 100px;
  line-height: 100px;
  background: radial-gradient(circle, #f9f5eb, #f0e6d2);
  border-radius: 50%;
  margin: 0 auto 30px;
  transition: all 0.5s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(197, 157, 95, 0.3);
  box-shadow: 0 5px 15px rgba(197, 157, 95, 0.15);
  position: relative;
  overflow: hidden;
}

.experience-icon::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.8), transparent 60%);
  pointer-events: none;
}

.experience-logo {
  width: 60px;
  height: 60px;
  object-fit: contain;
  transition: all 0.5s ease;
}

.experience-item:hover .experience-icon {
  transform: translateY(-5px) rotateY(180deg);
  box-shadow: 0 15px 25px rgba(197, 157, 95, 0.25);
  border-color: rgba(197, 157, 95, 0.6);
  background: radial-gradient(circle, #f0e6d2, #e8d7ba);
}

.experience-item:hover .experience-logo {
  transform: rotateY(-180deg);
}

.experience-item h3 {
  font-size: 22px;
  margin-bottom: 15px;
  color: #333;
  font-family: "Times New Roman", "SimSun", serif;
  position: relative;
  display: inline-block;
}

.experience-item h3::after {
  content: "";
  position: absolute;
  width: 60%;
  height: 2px;
  background: #c59d5f;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  transition: all 0.3s ease;
  opacity: 0;
}

.experience-item:hover h3::after {
  width: 80%;
  opacity: 1;
}

.experience-item p {
  color: #666;
  line-height: 1.7;
  margin-top: 20px;
}

/* 客户评价优化 */
.reviews-section {
  margin: 120px auto;
  max-width: 1400px;
  padding: 80px 20px;
  position: relative;
  background: linear-gradient(to right, rgba(250, 246, 237, 0.7), rgba(240, 230, 210, 0.7), rgba(250, 246, 237, 0.7));
  border-radius: 10px;
}

.reviews-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/src/assets/hotel2.jpg') center/cover no-repeat fixed;
  opacity: 0.05;
  z-index: -1;
  filter: blur(5px);
  border-radius: 10px;
}

.reviews-section::after {
  content: "";
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  bottom: 20px;
  border: 1px solid rgba(197, 157, 95, 0.2);
  border-radius: 8px;
  z-index: -1;
  pointer-events: none;
}

.reviews-carousel {
  margin-top: 50px;
}

.review-card {
  background: #ffffff;
  padding: 40px 35px;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  height: 100%;
  transition: all 0.4s ease;
  position: relative;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.review-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border-color: rgba(197, 157, 95, 0.3);
}

.review-quote {
  position: absolute;
  top: 30px;
  left: 30px;
  font-size: 90px;
  line-height: 1;
  color: rgba(197, 157, 95, 0.1);
  font-family: "Times New Roman", serif;
}

.review-content {
  margin-bottom: 30px;
  font-style: italic;
  line-height: 1.8;
  color: #333333;
  font-size: 1.1em;
  position: relative;
  z-index: 1;
  flex: 1;
}

.reviewer {
  display: flex;
  align-items: center;
  padding-top: 25px;
  border-top: 1px solid rgba(197, 157, 95, 0.2);
}

.reviewer-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 20px;
  object-fit: cover;
  border: 3px solid rgba(197, 157, 95, 0.2);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #f0e6d2, #c59d5f);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 22px;
  font-family: "SimSun", serif;
}

.reviewer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
}

.review-card:hover .reviewer-avatar {
  border-color: rgba(197, 157, 95, 0.5);
  transform: scale(1.05);
}

.reviewer-info h4 {
  margin: 0;
  color: #333;
  font-size: 1.3em;
  font-family: "SimSun", serif;
}

.reviewer-info p {
  margin: 5px 0 0;
  color: rgba(197, 157, 95, 0.8);
  font-size: 0.9em;
  letter-spacing: 1px;
}

/* 轮播图样式恢复 */
.carousel-text h1 {
  font-size: 60px;
  margin-bottom: 25px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 5px;
  color: #ffffff;
  font-family: "Times New Roman", "SimSun", serif;
  position: relative;
  display: inline-block;
}

.carousel-text h1::after {
  content: "";
  position: absolute;
  width: 180px;
  height: 3px;
  background: #c59d5f;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
}

.carousel-text p {
  font-size: 24px;
  line-height: 1.8;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  color: #f0f0f0;
  max-width: 750px;
  margin: 25px auto 40px;
}

.hero-btn {
  padding: 15px 40px;
  font-size: 20px;
  background: #c59d5f;
  border-color: #c59d5f;
  transition: all 0.4s ease;
  letter-spacing: 2px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.hero-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.7s;
  z-index: -1;
}

.hero-btn:hover::before {
  left: 100%;
}

.hero-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.3);
}

.scroll-indicator {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  text-align: center;
  z-index: 10;
  animation: bounce 2s infinite;
}

.scroll-indicator span {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.scroll-arrow {
  font-size: 24px;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {transform: translateY(0) translateX(-50%);}
  40% {transform: translateY(-20px) translateX(-50%);}
  60% {transform: translateY(-10px) translateX(-50%);}
}

/* 通用区块样式 */
.story-section,
.packages-section,
.rooms-section,
.experience-section,
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

/* 地图容器样式 */
.map-container {
  width: 100%;
  height: 400px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  position: relative;
}

/* 恢复房型卡片样式 */
.room-card {
  background: #ffffff;
  border-radius: 10px;
  margin-bottom: 40px;
  transition: all 0.4s ease;
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
  overflow: hidden;
  height: 100%;
  border: 1px solid #f0f0f0;
}

.room-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border-color: #c59d5f;
}

.room-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.room-card:hover .room-image {
  transform: scale(1.1);
}

.view-details-btn {
  background: #c59d5f;
  border-color: #c59d5f;
  padding: 12px 25px;
  letter-spacing: 1px;
}

.view-details-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}
</style>