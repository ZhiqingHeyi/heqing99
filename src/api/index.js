import axios from 'axios';

// 创建Axios实例，设置基本配置
const apiClient = axios.create({
  baseURL: '/api', // 所有请求都会添加/api前缀，Vite代理会将请求转发到后端
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器 - 添加认证Token等
apiClient.interceptors.request.use(
  config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('userToken');
    // 如果有Token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误和刷新Token等
apiClient.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response.data;
  },
  error => {
    // 处理HTTP错误
    if (error.response) {
      // 处理401未授权错误
      if (error.response.status === 401) {
        // 可以在这里处理Token过期逻辑，例如刷新Token或退出登录
        console.error('身份验证失败，请重新登录');
      }
      
      // 处理500服务器错误
      if (error.response.status === 500) {
        console.error('服务器内部错误:', error.response.data);
      }
      
      // 将错误信息放在error对象上，方便调用者处理
      error.message = error.response.data.message || error.response.data || '请求失败';
      error.statusCode = error.response.status;
    } else if (error.request) {
      // 请求发送了但没有收到响应
      error.message = '服务器无响应，请检查网络连接';
    } else {
      // 请求配置出错
      error.message = '请求配置错误';
    }
    
    console.error('API请求错误:', error.message);
    return Promise.reject(error);
  }
);

// 用户相关API
export const userApi = {
  // 用户注册
  register: (userData) => {
    console.log('调用注册API:', userData)
    return apiClient.post('/users/register', userData)
      .then(response => {
        console.log('注册API响应:', response)
        // 确保处理后端返回的成功/失败信息
        if (response && response.success === false) {
          // 显式的失败响应
          throw new Error(response.message || '注册失败，请稍后重试')
        }
        return response
      })
      .catch(error => {
        console.error('注册API错误:', error)
        // 处理不同类型的错误
        if (error.response) {
          // 服务器返回了错误状态码
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          // 请求发送了但没有收到响应
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          // 请求设置出错
          throw error
        }
      })
  },
  
  // 用户登录
  login: (credentials) => {
    console.log('调用登录API:', credentials)
    return apiClient.post('/api/login', credentials)
      .then(response => {
        console.log('登录API响应:', response)
        // 确保处理后端返回的成功/失败信息
        if (response && response.success === false) {
          // 显式的失败响应
          throw new Error(response.message || '登录失败，请稍后重试')
        }
        return response
      })
      .catch(error => {
        console.error('登录API错误:', error)
        // 处理不同类型的错误
        if (error.response) {
          // 服务器返回了错误状态码
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          // 请求发送了但没有收到响应
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          // 请求设置出错
          throw error
        }
      })
  },
  
  // 获取用户信息
  getUserInfo: (userId) => {
    console.log('调用获取用户信息API:', userId)
    return apiClient.get(`/api/users/${userId}`)
      .then(response => {
        console.log('获取用户信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户信息失败')
        }
        return response.data
      })
      .catch(error => {
        console.error('获取用户信息API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 更新用户信息
  updateUserInfo: (userId, userData) => apiClient.put(`/users/${userId}`, userData),
  
  // 修改密码
  changePassword: (userId, passwordData) => apiClient.put(`/users/${userId}/password`, passwordData),

  // 刷新Token
  refreshToken: (refreshData) => apiClient.post('/users/refresh-token', refreshData),

  // 退出登录
  logout: (token) => apiClient.post('/users/logout', { token })
};

// 会员相关API
export const membershipApi = {
  // 获取会员信息
  getMemberInfo: (userId) => apiClient.get(`/membership/${userId}`),
  
  // 更新会员等级
  updateMemberLevel: (userId) => apiClient.post(`/membership/${userId}/update-level`),
  
  // 添加积分
  addPoints: (userId, points) => apiClient.post(`/membership/${userId}/add-points?points=${points}`),
  
  // 使用积分
  redeemPoints: (userId, points) => apiClient.post(`/membership/${userId}/redeem-points?points=${points}`),
  
  // 添加消费记录
  addSpending: (userId, amount) => apiClient.post(`/membership/${userId}/add-spending?amount=${amount}`),
  
  // 获取折扣信息
  getDiscount: (userId) => apiClient.get(`/membership/${userId}/discount`),
  
  // 获取会员等级列表
  getMembershipLevels: () => apiClient.get('/membership/levels')
};

// 预订相关API
export const reservationApi = {
  // 创建预订
  createReservation: (reservationData) => apiClient.post('/reservations', reservationData),
  
  // 获取用户的所有预订
  getUserReservations: (userId) => apiClient.get(`/reservations/user/${userId}`),
  
  // 获取预订详情
  getReservation: (id) => apiClient.get(`/reservations/${id}`),
  
  // 取消预订
  cancelReservation: (id) => apiClient.post(`/reservations/${id}/cancel`),
  
  // 确认预订（管理员用）
  confirmReservation: (id) => apiClient.post(`/reservations/${id}/confirm`),
  
  // 计算折扣价格
  calculateDiscount: (userId, amount) => apiClient.post(`/reservations/calculate-discount?userId=${userId}&amount=${amount}`)
};

// 房间相关API
export const roomApi = {
  // 获取所有房间类型
  getAllRoomTypes: () => apiClient.get('/rooms/types'),
  
  // 获取房间详情
  getRoomDetails: (id) => apiClient.get(`/rooms/${id}`),
  
  // 检查房间可用性
  checkRoomAvailability: (params) => apiClient.get('/rooms/availability', { params })
};

// 导出API客户端，方便直接使用
export default apiClient;