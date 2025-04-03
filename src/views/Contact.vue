<template>
  <div class="contact-page">
    <div class="page-background"></div>
    <div class="contact-header">
      <h1>尊享服务 · 品质沟通</h1>
      <div class="header-divider"></div>
      <p>鹤清酒店竭诚为您提供贴心服务，随时倾听您的需求与建议</p>
    </div>
    
    <el-row :gutter="30" class="luxury-row">
      <el-col :lg="12" :md="24">
        <el-card class="contact-info-card luxury-card">
          <template #header>
            <div class="card-header">
              <h2>联系方式</h2>
            </div>
          </template>
          <div class="contact-details">
            <div class="contact-item">
              <div class="icon-container">
                <i class="el-icon-location"></i>
              </div>
              <div class="contact-text">
                <h4>酒店地址</h4>
                <p>浙江省杭州市西湖区文三路XXX号</p>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="icon-container">
                <i class="el-icon-phone"></i>
              </div>
              <div class="contact-text">
                <h4>预订电话</h4>
                <p>0571-XXXXXXXX</p>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="icon-container">
                <i class="el-icon-message"></i>
              </div>
              <div class="contact-text">
                <h4>电子邮箱</h4>
                <p>contact@heqinghotel.com</p>
              </div>
            </div>
            
            <div class="business-hours">
              <h3>营业时间</h3>
              <div class="hours-grid">
                <div class="hours-item">
                  <span class="hours-label">前台服务</span>
                  <span class="hours-value">24小时</span>
                </div>
                <div class="hours-item">
                  <span class="hours-label">入住办理</span>
                  <span class="hours-value">14:00-22:00</span>
                </div>
                <div class="hours-item">
                  <span class="hours-label">退房时间</span>
                  <span class="hours-value">12:00前</span>
                </div>
              </div>
            </div>
            
            <div class="social-media">
              <h3>关注我们</h3>
              <div class="social-icons">
                <a href="#" class="social-icon wechat">
                  <img src="@/assets/wechat.svg" alt="微信" />
                </a>
                <a href="#" class="social-icon weibo">
                  <img src="@/assets/weibo.svg" alt="微博" />
                </a>
                <a href="#" class="social-icon douyin">
                  <img src="@/assets/douyin.svg" alt="抖音" />
                </a>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="contact-form-card luxury-card">
          <template #header>
            <div class="card-header">
              <h2>在线咨询</h2>
            </div>
          </template>
          <el-form :model="form" label-width="80px" class="luxury-form">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入您的姓名" class="luxury-input"></el-input>
            </el-form-item>
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="请输入您的联系电话" class="luxury-input"></el-input>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入您的邮箱地址" class="luxury-input"></el-input>
            </el-form-item>
            <el-form-item label="留言">
              <el-input
                v-model="form.message"
                type="textarea"
                rows="4"
                placeholder="请输入您的留言内容"
                class="luxury-textarea"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitForm" class="luxury-button">提交留言</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :lg="12" :md="24">
        <el-card class="map-card luxury-card">
          <template #header>
            <div class="card-header">
              <h2>地理位置</h2>
            </div>
          </template>
          <div id="map-container" class="map-container"></div>
          

          
          <div class="transportation-info">
            <h3>交通指南</h3>
            <div class="transport-options">
              <div class="transport-option">
                <div class="transport-icon car"></div>
                <div class="transport-details">
                  <h4>自驾路线</h4>
                  <p>导航至"鹤清酒店"，地下停车场24小时开放，住店客人可享受免费停车</p>
                </div>
              </div>
              <div class="transport-option">
                <div class="transport-icon subway"></div>
                <div class="transport-details">
                  <h4>地铁路线</h4>
                  <p>地铁2号线到达"西湖文化广场站"，从A出口出站步行约800米可达</p>
                </div>
              </div>
              <div class="transport-option">
                <div class="transport-icon airport"></div>
                <div class="transport-details">
                  <h4>机场专线</h4>
                  <p>金牌及以上会员可预约免费接机服务，请提前24小时联系酒店安排</p>
                </div>
              </div>
              <div class="transport-option">
                <div class="transport-icon taxi"></div>
                <div class="transport-details">
                  <h4>出租车服务</h4>
                  <p>酒店可代为预约出租车服务，提供24小时专业接送服务</p>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AMapLoader from '@amap/amap-jsapi-loader'

const form = ref({
  name: '',
  phone: '',
  email: '',
  message: ''
})

const showRoads = ref(true)
const showBuildings = ref(true)
let mapInstance = null

const submitForm = () => {
  // 这里添加表单提交逻辑
  ElMessage({
    message: '感谢您的留言，我们会尽快与您联系！',
    type: 'success',
    customClass: 'luxury-message'
  })
}

const setMapType = (type) => {
  if (!mapInstance) return
  
  if (type === 'standard') {
    mapInstance.setLayers([mapInstance.getLayer('TileLayer')])
  } else if (type === 'satellite') {
    mapInstance.setLayers([mapInstance.getLayer('TileLayer.Satellite')])
  }
}

const toggleMapLayers = () => {
  if (!mapInstance) return
  
  const features = ['bg', 'point']
  if (showRoads.value) features.push('road')
  if (showBuildings.value) features.push('building')
  mapInstance.setFeatures(features)
}

onMounted(() => {
  // 初始化高德地图
  AMapLoader.load({
    key: '8325164e247e15eea68b59e89200988b',
    version: '2.0',
    plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.ControlBar', 'AMap.MapType']
  }).then((AMap) => {
    mapInstance = new AMap.Map('map-container', {
      zoom: 15,
      center: [115.892151, 28.682892], // 南昌市八一公园附近的经纬度坐标
      viewMode: '3D',
      pitch: 45,
      defaultLayer: new AMap.TileLayer(),
      layers: [
        new AMap.TileLayer(),
        new AMap.TileLayer.Satellite()
      ],
      features: ['bg', 'building', 'point', 'road']
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
    
    mapInstance.add(marker)
    mapInstance.addControl(new AMap.Scale())
    mapInstance.addControl(new AMap.ToolBar())
    mapInstance.addControl(new AMap.ControlBar())
    mapInstance.addControl(new AMap.MapType())

    // 添加信息窗体
    const infoWindow = new AMap.InfoWindow({
      content: `
        <div style="padding:15px;">
          <h4 style="margin:0 0 10px;color:#8a6d3b;font-size:16px;">鹤清酒店</h4>
          <p style="margin:5px 0;color:#666;">地址：江西省南昌市八一公园旁</p>
          <p style="margin:5px 0;color:#666;">电话：0791-12345678</p>
          <div style="margin-top:10px;border-top:1px solid #eee;padding-top:10px;">
            <button style="background:#8a6d3b;color:white;border:none;padding:5px 10px;border-radius:3px;cursor:pointer;">
              导航前往
            </button>
          </div>
        </div>
      `,
      offset: new AMap.Pixel(0, -30)
    })

    // 点击标记时打开信息窗体
    marker.on('click', () => {
      infoWindow.open(mapInstance, marker.getPosition())
    })
  }).catch(e => {
    console.error('地图加载失败：', e)
  })
})
</script>

<style scoped>
.contact-page {
  position: relative;
  padding: 60px 0;
  background-color: #f9f7f3;
}

.page-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('@/assets/hotel3.jpg');
  background-size: cover;
  background-position: center;
  opacity: 0.08;
  z-index: 0;
}

.contact-header {
  text-align: center;
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
}

.contact-header h1 {
  font-family: "Playfair Display", "Times New Roman", serif;
  font-size: 2.8em;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
  letter-spacing: 2px;
}

.header-divider {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ab8a62, transparent);
  margin: 0 auto 20px;
}

.contact-header p {
  font-size: 1.2em;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

.luxury-row {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.luxury-card {
  margin-bottom: 30px;
  border: none;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.luxury-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

.luxury-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #d4af37, #e5c17f, #d4af37);
}

.card-header {
  padding: 20px 25px;
  background: linear-gradient(to right, #f3efe7, #f9f7f3);
  border-bottom: 1px solid #f0e6d9;
}

.card-header h2 {
  margin: 0;
  font-family: "Playfair Display", "Times New Roman", serif;
  font-size: 1.8em;
  color: #8a6d3b;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.card-header h2::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 40px;
  height: 2px;
  background-color: #c1aa89;
}

.contact-details {
  padding: 30px;
}

.contact-item {
  display: flex;
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.contact-item:hover {
  transform: translateX(5px);
}

.icon-container {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f9f7f3, #f3efe7);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  color: #8a6d3b;
  font-size: 22px;
  flex-shrink: 0;
}

.contact-text {
  flex: 1;
}

.contact-text h4 {
  margin: 0 0 5px;
  color: #665744;
  font-size: 16px;
  font-weight: 600;
}

.contact-text p {
  margin: 0;
  color: #888;
  font-size: 15px;
}

.business-hours {
  margin-top: 40px;
  padding: 25px;
  background: linear-gradient(to right, #f9f7f3, #f3efe7);
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.03);
}

.business-hours h3 {
  color: #8a6d3b;
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 600;
  font-size: 1.2em;
  position: relative;
  padding-bottom: 10px;
}

.business-hours h3::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background-color: #c1aa89;
}

.hours-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.hours-item {
  display: flex;
  flex-direction: column;
}

.hours-label {
  color: #665744;
  font-size: 14px;
  margin-bottom: 5px;
}

.hours-value {
  color: #8a6d3b;
  font-weight: 600;
  font-size: 16px;
}

.social-media {
  margin-top: 40px;
}

.social-media h3 {
  color: #8a6d3b;
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 600;
  font-size: 1.2em;
  position: relative;
  padding-bottom: 10px;
}

.social-media h3::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background-color: #c1aa89;
}

.social-icons {
  display: flex;
  gap: 15px;
}

.social-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #f3efe7;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.social-icon img {
  width: 24px;
  height: 24px;
  filter: opacity(0.6);
  transition: all 0.3s ease;
}

.social-icon:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.social-icon:hover img {
  filter: opacity(1);
}

.luxury-form {
  padding: 20px;
}

.luxury-input :deep(.el-input__inner),
.luxury-textarea :deep(.el-textarea__inner) {
  border-color: #dccfb8;
  background-color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  padding: 12px 15px;
}

.luxury-input :deep(.el-input__inner:focus),
.luxury-textarea :deep(.el-textarea__inner:focus) {
  border-color: #8a6d3b;
  box-shadow: 0 0 0 2px rgba(138, 109, 59, 0.2);
}

.luxury-button {
  background: linear-gradient(135deg, #ab8a62, #8a6d3b) !important;
  border-color: #8a6d3b !important;
  padding: 12px 30px !important;
  font-size: 16px !important;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.luxury-button::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: rgba(255, 255, 255, 0.1);
  transform: rotate(45deg);
  opacity: 0;
  transition: opacity 0.4s;
}

.luxury-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 14px rgba(138, 109, 59, 0.4);
}

.luxury-button:hover::before {
  opacity: 1;
  animation: shine 1.5s forwards;
}

@keyframes shine {
  0% {
    left: -50%;
    opacity: 0;
  }
  30% {
    opacity: 0.5;
  }
  100% {
    left: 150%;
    opacity: 0;
  }
}

.map-container {
  height: 400px;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.map-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f7f3;
  margin-top: 15px;
  border-radius: 8px;
}

.map-controls label {
  display: inline-flex;
  align-items: center;
  margin-right: 15px;
  cursor: pointer;
}

.map-controls label span {
  margin-left: 5px;
  color: #665744;
}

.map-checkboxes {
  display: flex;
}

.transportation-info {
  margin-top: 30px;
  padding: 25px;
}

.transportation-info h3 {
  color: #8a6d3b;
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: 600;
  font-size: 1.2em;
  position: relative;
  padding-bottom: 10px;
}

.transportation-info h3::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background-color: #c1aa89;
}

.transport-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.transport-option {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  background-color: #f9f7f3;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.transport-option:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
}

.transport-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  margin-right: 15px;
  background-size: 24px;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #f3efe7;
  flex-shrink: 0;
}

.transport-icon.car {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>');
}

.transport-icon.subway {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M12 2c-4.42 0-8 .5-8 4v9.5C4 17.43 5.57 19 7.5 19L6 20.5v.5h12v-.5L16.5 19c1.93 0 3.5-1.57 3.5-3.5V6c0-3.5-3.58-4-8-4zM7.5 17c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 17 7.5 17zm3.5-6H6V6h5v5zm5.5 6c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm1.5-6h-5V6h5v5z"/></svg>');
}

.transport-icon.airport {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M21.5 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L14 19v-5.5l7.5 2.5z"/></svg>');
}

.transport-icon.taxi {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%238a6d3b"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.85 7h10.29l1.04 3H5.81l1.04-3zM19 17H5v-5h14v5z"/></svg>');
}

.transport-details {
  flex: 1;
}

.transport-details h4 {
  margin: 0 0 8px;
  color: #665744;
  font-size: 16px;
  font-weight: 600;
}

.transport-details p {
  margin: 0;
  color: #888;
  font-size: 14px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .contact-page {
    padding: 40px 15px;
  }
  
  .contact-header h1 {
    font-size: 2em;
  }
  
  .hours-grid {
    grid-template-columns: 1fr;
  }
  
  .map-container {
    height: 300px;
  }
}
</style>