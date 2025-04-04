<template>
  <div class="contact-page">
    <div class="page-background"></div>
    <div class="contact-header">
      <h1>尊享服务 · 品质沟通</h1>
      <div class="header-divider"></div>
      <p>鹤清酒店竭诚为您提供贴心服务，随时倾听您的需求与建议</p>
    </div>
    
    <div class="contact-container">
      <el-row :gutter="40">
        <!-- 左侧联系信息与表单 -->
        <el-col :lg="12" :md="24">
          <div class="info-form-container">
            <el-card class="contact-info-card luxury-card">
              <template #header>
                <div class="card-header">
                  <h2>联系方式</h2>
                </div>
              </template>
              <div class="contact-details">
                <div class="contact-item">
                  <div class="icon-container">
                    <i class="elegant-icon location-icon"></i>
                  </div>
                  <div class="contact-text">
                    <h4>酒店地址</h4>
                    <p>浙江省杭州市西湖区文三路XXX号</p>
                  </div>
                </div>
                
                <div class="contact-item">
                  <div class="icon-container">
                    <i class="elegant-icon phone-icon"></i>
                  </div>
                  <div class="contact-text">
                    <h4>预订电话</h4>
                    <p>0571-XXXXXXXX</p>
                  </div>
                </div>
                
                <div class="contact-item">
                  <div class="icon-container">
                    <i class="elegant-icon email-icon"></i>
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
                      <i class="elegant-icon wechat-icon"></i>
                    </a>
                    <a href="#" class="social-icon weibo">
                      <i class="elegant-icon weibo-icon"></i>
                    </a>
                    <a href="#" class="social-icon douyin">
                      <i class="elegant-icon douyin-icon"></i>
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
                <el-form-item class="form-actions">
                  <el-button type="primary" @click="submitForm" class="luxury-button">提交留言</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </el-col>

        <!-- 右侧地图和交通信息 -->
        <el-col :lg="12" :md="24">
          <div class="map-info-container">
            <el-card class="map-card luxury-card">
              <template #header>
                <div class="card-header">
                  <h2>地理位置</h2>
                </div>
              </template>
              <div id="map-container" class="map-container"></div>
              
              <div class="map-controls">
                <div class="map-control-item">
                  <span>地图类型:</span>
                  <el-radio-group v-model="mapType" size="small" @change="setMapType">
                    <el-radio-button label="standard">标准</el-radio-button>
                    <el-radio-button label="satellite">卫星</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="map-control-item">
                  <el-checkbox v-model="showRoads" @change="toggleMapLayers">显示道路</el-checkbox>
                  <el-checkbox v-model="showBuildings" @change="toggleMapLayers">显示建筑</el-checkbox>
                </div>
              </div>
              
              <div class="transportation-info">
                <h3>交通指南</h3>
                <div class="transport-options">
                  <div class="transport-option">
                    <div class="transport-icon car">
                      <i class="elegant-icon car-icon"></i>
                    </div>
                    <div class="transport-details">
                      <h4>自驾路线</h4>
                      <p>导航至"鹤清酒店"，地下停车场24小时开放，住店客人可享受免费停车</p>
                    </div>
                  </div>
                  <div class="transport-option">
                    <div class="transport-icon subway">
                      <i class="elegant-icon subway-icon"></i>
                    </div>
                    <div class="transport-details">
                      <h4>地铁路线</h4>
                      <p>地铁2号线到达"西湖文化广场站"，从A出口出站步行约800米可达</p>
                    </div>
                  </div>
                  <div class="transport-option">
                    <div class="transport-icon airport">
                      <i class="elegant-icon plane-icon"></i>
                    </div>
                    <div class="transport-details">
                      <h4>机场专线</h4>
                      <p>金牌及以上会员可预约免费接机服务，请提前24小时联系酒店安排</p>
                    </div>
                  </div>
                  <div class="transport-option">
                    <div class="transport-icon taxi">
                      <i class="elegant-icon taxi-icon"></i>
                    </div>
                    <div class="transport-details">
                      <h4>出租车服务</h4>
                      <p>酒店可代为预约出租车服务，提供24小时专业接送服务</p>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>
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

const mapType = ref('standard')
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
  
  // 清空表单
  form.value = {
    name: '',
    phone: '',
    email: '',
    message: ''
  }
}

const setMapType = (type) => {
  if (!mapInstance) return
  
  if (type === 'standard') {
    mapInstance.setLayers([new AMap.TileLayer()])
  } else if (type === 'satellite') {
    mapInstance.setLayers([new AMap.TileLayer.Satellite()])
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
      layers: [new AMap.TileLayer()],
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

    // 添加信息窗体
    const infoWindow = new AMap.InfoWindow({
      content: `
        <div style="padding:15px;">
          <h4 style="margin:0 0 10px;color:#8a6d3b;font-size:16px;">鹤清酒店</h4>
          <p style="margin:5px 0;color:#666;">地址：江西省南昌市八一公园旁</p>
          <p style="margin:5px 0;color:#666;">电话：0791-12345678</p>
          <div style="margin-top:10px;border-top:1px solid #eee;padding-top:10px;">
            <a href="https://uri.amap.com/navigation?to=115.892151,28.682892,鹤清酒店&mode=car" target="_blank" style="background:#c59d5f;color:white;border:none;padding:5px 10px;border-radius:3px;cursor:pointer;text-decoration:none;display:inline-block;">
              导航前往
            </a>
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
  padding: 80px 0;
  background-color: #f9f7f3;
  min-height: calc(100vh - 70px);
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
  font-family: "SimSun", serif;
  font-size: 2.8em;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
  letter-spacing: 3px;
}

.header-divider {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #c59d5f, transparent);
  margin: 0 auto 20px;
}

.contact-header p {
  font-size: 1.2em;
  color: #666;
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.6;
}

.contact-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.info-form-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-info-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.luxury-card {
  margin-bottom: 30px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(0,0,0,0.05);
  border: 1px solid rgba(197, 157, 95, 0.1);
  background: #fff;
  transition: all 0.3s ease;
}

.luxury-card:hover {
  box-shadow: 0 20px 40px rgba(0,0,0,0.08);
  transform: translateY(-5px);
}

.card-header {
  background: linear-gradient(to right, rgba(250, 246, 237, 0.8), rgba(255, 255, 255, 0.9), rgba(250, 246, 237, 0.8));
  border-bottom: 1px solid rgba(197, 157, 95, 0.2);
  padding: 15px 20px;
}

.card-header h2 {
  font-family: "SimSun", serif;
  font-size: 1.8em;
  color: #333;
  margin: 0;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.card-header h2::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #c59d5f, transparent);
  width: 60%;
}

.contact-details {
  padding: 30px;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30px;
}

.icon-container {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #f0e6d2, #fffcf5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  box-shadow: 0 5px 15px rgba(197, 157, 95, 0.15);
  border: 1px solid rgba(197, 157, 95, 0.1);
  flex-shrink: 0;
}

.icon-container i {
  font-size: 24px;
  color: #c59d5f;
}

.contact-text h4 {
  margin: 0 0 10px;
  font-size: 1.2em;
  color: #333;
  font-weight: 600;
}

.contact-text p {
  margin: 0;
  font-size: 1.1em;
  color: #666;
  line-height: 1.5;
}

.business-hours {
  margin: 40px 0;
  border-top: 1px dashed rgba(197, 157, 95, 0.2);
  padding-top: 30px;
}

.business-hours h3 {
  font-size: 1.3em;
  margin: 0 0 20px;
  color: #333;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.business-hours h3::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  height: 2px;
  background: #c59d5f;
  width: 40px;
}

.hours-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.hours-item {
  background: rgba(250, 246, 237, 0.5);
  border: 1px solid rgba(197, 157, 95, 0.1);
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  transition: all 0.3s ease;
}

.hours-item:hover {
  background: rgba(250, 246, 237, 0.8);
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  transform: translateY(-3px);
}

.hours-label {
  display: block;
  font-size: 0.9em;
  color: #666;
  margin-bottom: 8px;
}

.hours-value {
  display: block;
  font-size: 1.2em;
  color: #c59d5f;
  font-weight: 600;
}

.social-media {
  margin-top: 40px;
  border-top: 1px dashed rgba(197, 157, 95, 0.2);
  padding-top: 30px;
}

.social-media h3 {
  font-size: 1.3em;
  margin: 0 0 20px;
  color: #333;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.social-media h3::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  height: 2px;
  background: #c59d5f;
  width: 40px;
}

.social-icons {
  display: flex;
  gap: 20px;
}

.social-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0e6d2, #fffcf5);
  box-shadow: 0 5px 15px rgba(197, 157, 95, 0.15);
  border: 1px solid rgba(197, 157, 95, 0.1);
  transition: all 0.3s ease;
}

.social-icon:hover {
  transform: translateY(-5px) rotate(360deg);
  box-shadow: 0 10px 20px rgba(197, 157, 95, 0.2);
}

.social-icon i {
  width: 36px;
  height: 36px;
}

.luxury-form {
  padding: 30px;
}

.luxury-input,
.luxury-textarea {
  border-radius: 8px;
  border: 1px solid rgba(197, 157, 95, 0.2);
  padding: 12px 15px;
  transition: all 0.3s ease;
}

.luxury-input:focus,
.luxury-textarea:focus {
  border-color: #c59d5f;
  box-shadow: 0 0 10px rgba(197, 157, 95, 0.2);
}

.form-actions {
  margin-top: 20px;
  text-align: right;
  margin-bottom: 0;
}

.luxury-button {
  background: linear-gradient(135deg, #c59d5f, #ddbf85);
  border-color: transparent;
  padding: 12px 30px;
  font-size: 16px;
  letter-spacing: 1px;
  box-shadow: 0 5px 15px rgba(197, 157, 95, 0.2);
  transition: all 0.3s ease;
}

.luxury-button:hover {
  background: linear-gradient(135deg, #b58d40, #c59d5f);
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(197, 157, 95, 0.3);
}

.map-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-container {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
}

.map-controls {
  margin-bottom: 20px;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

.map-control-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.map-control-item span {
  margin-right: 10px;
  color: #666;
}

.transportation-info {
  padding: 0 30px 30px;
}

.transportation-info h3 {
  font-size: 1.3em;
  margin: 0 0 20px;
  color: #333;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.transportation-info h3::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  height: 2px;
  background: #c59d5f;
  width: 40px;
}

.transport-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.transport-option {
  background: rgba(250, 246, 237, 0.5);
  border: 1px solid rgba(197, 157, 95, 0.1);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  transition: all 0.3s ease;
}

.transport-option:hover {
  background: rgba(250, 246, 237, 0.8);
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
  transform: translateY(-5px);
}

.transport-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #f0e6d2, #fffcf5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  box-shadow: 0 5px 15px rgba(197, 157, 95, 0.15);
  border: 1px solid rgba(197, 157, 95, 0.1);
  flex-shrink: 0;
}

.transport-icon i {
  width: 30px;
  height: 30px;
}

.transport-details h4 {
  margin: 0 0 10px;
  font-size: 1.1em;
  color: #333;
  font-weight: 600;
}

.transport-details p {
  margin: 0;
  font-size: 0.9em;
  color: #666;
  line-height: 1.5;
}

.luxury-message {
  background: linear-gradient(135deg, #f0e6d2, #fffcf5);
  border: 1px solid rgba(197, 157, 95, 0.2);
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

@media (max-width: 992px) {
  .contact-page {
    padding: 60px 0;
  }
  
  .contact-header {
    margin-bottom: 40px;
  }
  
  .contact-header h1 {
    font-size: 2.2em;
  }
  
  .luxury-card {
    margin-bottom: 20px;
  }
  
  .transport-options {
    grid-template-columns: 1fr;
  }
  
  .map-controls {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .map-control-item {
    margin-bottom: 10px;
  }
}

/* 优雅金色简笔画图标样式 */
.elegant-icon {
  display: inline-block;
  width: 24px;
  height: 24px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
  opacity: 0.95;
  filter: drop-shadow(0 1px 2px rgba(197, 157, 95, 0.3));
}

.location-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z'/%3E%3C/svg%3E");
}

.phone-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z'/%3E%3C/svg%3E");
}

.email-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z'/%3E%3Cpolyline points='22,6 12,13 2,6'/%3E%3C/svg%3E");
}

.wechat-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23c59d5f'%3E%3Cpath d='M9.5,7.5c-1,0-1.8,0.8-1.8,1.8s0.8,1.8,1.8,1.8c1,0,1.8-0.8,1.8-1.8S10.5,7.5,9.5,7.5z M14.5,7.5c-1,0-1.8,0.8-1.8,1.8 s0.8,1.8,1.8,1.8c1,0,1.8-0.8,1.8-1.8S15.5,7.5,14.5,7.5z M18,12c0-3.31-3.13-6-7-6S4,8.69,4,12c0,2.97,2.16,5.44,5,5.92V20l2.71-2.71 c0.42,0.08,0.86,0.13,1.29,0.13C14.87,18,18,15.31,18,12z M10,13.5c-0.83,0-1.5-0.67-1.5-1.5s0.67-1.5,1.5-1.5s1.5,0.67,1.5,1.5 S10.83,13.5,10,13.5z M14,13.5c-0.83,0-1.5-0.67-1.5-1.5s0.67-1.5,1.5-1.5s1.5,0.67,1.5,1.5S14.83,13.5,14,13.5z'/%3E%3C/svg%3E");
}

.weibo-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23c59d5f'%3E%3Cpath d='M9.82,14.76c0,1.19,1.6,2.16,3.57,2.16s3.57-0.97,3.57-2.16c0-1.19-1.6-2.16-3.57-2.16S9.82,13.56,9.82,14.76z M12.36,14.19 c-0.58,0-1.05,0.47-1.05,1.05c0,0.58,0.47,1.05,1.05,1.05c0.58,0,1.05-0.47,1.05-1.05C13.41,14.66,12.94,14.19,12.36,14.19z M13.71,15.05c-0.22,0-0.4-0.18-0.4-0.4s0.18-0.4,0.4-0.4c0.22,0,0.4,0.18,0.4,0.4S13.93,15.05,13.71,15.05z M9.37,10.54 c-0.75-1.77-0.09-2.93,0.65-3.25c1.53-0.66,3.12,1.08,3.12,1.08s0.75-0.24,0.65-0.89c-0.09-0.66-0.56-0.89,0-1.21 c0.56-0.32,2.15,0.57,2.4,1.35c0.25,0.78-0.4,1.21-0.4,1.21s0.09,0.32,0.65,0.24c0.56-0.08,2.43,0.89,1.87,2.51 c-0.56,1.62-1.65,1.62-1.87,1.62s0.09,0.89-0.75,0.89c-0.83,0-2.71-1.21-2.71-1.21s-0.09,0.73-0.94,0.73 c-0.84,0-3.03-1.7-3.03-1.7S10.12,12.31,9.37,10.54z M8.06,14.6c-0.24,0-0.44-0.2-0.44-0.44s0.2-0.44,0.44-0.44 c0.24,0,0.44,0.2,0.44,0.44S8.31,14.6,8.06,14.6z M6.82,15.05c-0.22,0-0.4-0.18-0.4-0.4s0.18-0.4,0.4-0.4c0.22,0,0.4,0.18,0.4,0.4 S7.04,15.05,6.82,15.05z'/%3E%3C/svg%3E");
}

.douyin-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23c59d5f'%3E%3Cpath d='M16.6,5.82c-0.9-0.95-1.23-2.42-1.23-3.82h-3.25v13.4c0,1.8-1.46,3.25-3.25,3.25c-1.8,0-3.25-1.46-3.25-3.25 c0-1.8,1.46-3.25,3.25-3.25c0.32,0,0.63,0.05,0.92,0.14v-3.31c-0.31-0.04-0.61-0.06-0.92-0.06c-3.63,0-6.57,2.94-6.57,6.57 c0,3.63,2.94,6.57,6.57,6.57c3.63,0,6.57-2.94,6.57-6.57V8.17c1.4,1.08,3.16,1.73,5.07,1.73V6.64 C19.11,6.64,17.59,6.35,16.6,5.82z'/%3E%3C/svg%3E");
}

.car-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M14 16H9m10 0h3v-3.15a1 1 0 0 0-.84-.99L16 11l-2.7-3.6a1 1 0 0 0-.8-.4H5.24a2 2 0 0 0-1.8 1.1l-.8 1.63A6 6 0 0 0 2 12.42V16h2'/%3E%3Ccircle cx='6.5' cy='16.5' r='2.5'/%3E%3Ccircle cx='16.5' cy='16.5' r='2.5'/%3E%3C/svg%3E");
}

.subway-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='4' y='3' width='16' height='16' rx='2'/%3E%3Cpath d='M4 11h16'/%3E%3Cpath d='M12 3v16'/%3E%3Ccircle cx='8' cy='16' r='1'/%3E%3Ccircle cx='16' cy='16' r='1'/%3E%3Cpath d='M9 6.5h1.5'/%3E%3Cpath d='M13.5 6.5H15'/%3E%3C/svg%3E");
}

.plane-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z'/%3E%3C/svg%3E");
}

.taxi-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23c59d5f' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M7,18m10,0H3c-.6,0-1-.4-1-1V8c0-.6,.4-1,1-1H7l2-3h6l2,3h4c.6,0,1,.4,1,1v9c0,.6-.4,1-1,1h-4'/%3E%3Cpath d='M5 18v2'/%3E%3Cpath d='M19 18v2'/%3E%3Crect x='5' y='10' width='3' height='2'/%3E%3Crect x='16' y='10' width='3' height='2'/%3E%3Cpath d='M5 14h14'/%3E%3C/svg%3E");
}
</style>