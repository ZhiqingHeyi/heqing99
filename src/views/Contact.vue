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
    key: 'your-amap-key', // 使用您的高德地图API密钥
    version: '2.0',
    plugins: ['AMap.Scale', 'AMap.ToolBar']
  }).then((AMap) => {
    const map = new AMap.Map('map-container', {
      zoom: 15,
      center: [120.152, 30.287] // 设置酒店的经纬度坐标
    })
    
    const marker = new AMap.Marker({
      position: [120.152, 30.287],
      title: '和庆酒店'
    })
    
    map.add(marker)
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
  height: 400px;
  width: 100%;
  border-radius: 4px;
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