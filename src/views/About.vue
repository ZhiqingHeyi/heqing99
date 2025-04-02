<template>
  <div class="about">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="page-header">
          <h1>关于我们</h1>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-section">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>酒店简介</h2>
            </div>
          </template>
          <p>和清99酒店创立于2024年，位于风景秀丽的城市中心地带。我们致力于为每一位宾客提供温馨舒适的住宿体验，以专业的服务和现代化的设施，让您感受宾至如归的温暖。</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-section">
      <el-col :span="8">
        <el-card class="value-card">
          <template #header>
            <div class="card-header">
              <h3>我们的使命</h3>
            </div>
          </template>
          <p>为宾客创造难忘的住宿体验，提供卓越的服务品质，成为酒店行业的标杆。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="value-card">
          <template #header>
            <div class="card-header">
              <h3>我们的愿景</h3>
            </div>
          </template>
          <p>成为最受欢迎的精品酒店品牌，让每位宾客都能享受到独特而温馨的住宿体验。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="value-card">
          <template #header>
            <div class="card-header">
              <h3>核心价值观</h3>
            </div>
          </template>
          <p>诚信、专业、创新、关怀，以客为尊，追求卓越服务品质。</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-section">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>我们的优势</h2>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <ul class="advantage-list">
                <li>优越的地理位置，便捷的交通</li>
                <li>现代化的设施设备</li>
                <li>专业的服务团队</li>
              </ul>
            </el-col>
            <el-col :span="12">
              <ul class="advantage-list">
                <li>舒适的居住环境</li>
                <li>个性化的服务体验</li>
                <li>完善的配套设施</li>
              </ul>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'About'
}
</script>

<style scoped>
.about {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 2.5em;
  color: #2c3e50;
  margin: 0;
}

.content-section {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2, .card-header h3 {
  margin: 0;
  color: #2c3e50;
}

.value-card {
  height: 100%;
}

.advantage-list {
  list-style: none;
  padding: 0;
}

.advantage-list li {
  margin: 10px 0;
  padding-left: 20px;
  position: relative;
}

.advantage-list li:before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #409EFF;
}
</style>