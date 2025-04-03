<template>
  <div class="contact-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="contact-info-card">
          <template #header>
            <div class="card-header">
              <h2>联系我们</h2>
            </div>
          </template>
          <div class="contact-details">
            <p><i class="el-icon-location"></i> 地址：浙江省杭州市西湖区文三路XXX号</p>
            <p><i class="el-icon-phone"></i> 电话：0571-XXXXXXXX</p>
            <p><i class="el-icon-message"></i> 邮箱：contact@heqinghotel.com</p>
            <div class="business-hours">
              <h3>营业时间</h3>
              <p>周一至周日：24小时服务</p>
              <p>前台办理入住时间：14:00-22:00</p>
              <p>退房时间：12:00前</p>
            </div>
          </div>
        </el-card>

        <el-card class="contact-form-card">
          <template #header>
            <div class="card-header">
              <h2>在线咨询</h2>
            </div>
          </template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入您的姓名"></el-input>
            </el-form-item>
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="请输入您的联系电话"></el-input>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入您的邮箱地址"></el-input>
            </el-form-item>
            <el-form-item label="留言">
              <el-input
                v-model="form.message"
                type="textarea"
                rows="4"
                placeholder="请输入您的留言内容"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitForm">提交</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="map-card">
          <template #header>
            <div class="card-header">
              <h2>地理位置</h2>
            </div>
          </template>
          <div id="map-container" class="map-container"></div>
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

const submitForm = () => {
  // 这里添加表单提交逻辑
  ElMessage.success('感谢您的留言，我们会尽快与您联系！')
}

onMounted(() => {
  // 初始化高德地图
  AMapLoader.load({
    key: '8325164e247e15eea68b59e89200988b',
    key: 'ad101aa6969036479910b4a3988add24',
    version: '2.0',
    plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.ControlBar', 'AMap.HawkEye', 'AMap.MapType']
  }).then((AMap) => {
    const map = new AMap.Map('map-container', {
      zoom: 15,
      center: [115.892151, 28.682892], // 南昌市八一公园附近的经纬度坐标
      viewMode: '3D',
      pitch: 45,
      defaultLayer: new AMap.TileLayer(),
      layers: [
        new AMap.TileLayer(),
        new AMap.TileLayer.Satellite()
      ],
      features: ['bg', 'building', 'point']
    })
    
    const marker = new AMap.Marker({
      position: [115.892151, 28.682892],
      title: '鹤清酒店',
      label: {
        content: '和庆酒店',
        direction: 'top',
        offset: [0, -36]
      },
      animation: 'AMAP_ANIMATION_BOUNCE'
    })
    
    map.add(marker)
    map.addControl(new AMap.Scale())
    map.addControl(new AMap.ToolBar())
    map.addControl(new AMap.ControlBar())
    map.addControl(new AMap.HawkEye())
    map.addControl(new AMap.MapType())

    // 添加信息窗体
    const infoWindow = new AMap.InfoWindow({
      content: `
        <div style="padding:10px;">
          <h4>鹤清酒店</h4>
          <p>地址：江西省南昌市八一公园旁</p>
          <p>电话：0791-12345678</p>
        </div>
      `,
      offset: new AMap.Pixel(0, -30)
    })

    // 点击标记时打开信息窗体
    marker.on('click', () => {
      infoWindow.open(map, marker.getPosition())
    })
  }).catch(e => {
    console.error('地图加载失败：', e)
  })
})
</script>

<style scoped>
.contact-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.contact-info-card,
.contact-form-card,
.map-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5em;
  color: #303133;
}

.contact-details p {
  margin: 10px 0;
  font-size: 16px;
  color: #606266;
}

.business-hours {
  margin-top: 20px;
}

.business-hours h3 {
  color: #303133;
  margin-bottom: 10px;
}

.map-container {
  height: 600px;
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin: 30px 0;
}

.map-card {
  margin-bottom: 40px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.contact-form-card {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .el-row {
    flex-direction: column;
  }
  
  .el-col {
    width: 100% !important;
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
}
</style>