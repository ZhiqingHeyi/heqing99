import { ref, computed } from 'vue';
import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', () => {
  // State
  const userToken = ref(localStorage.getItem('userToken') || null);
  const userId = ref(localStorage.getItem('userId') || null);
  const username = ref(localStorage.getItem('username') || null);
  const userRole = ref(localStorage.getItem('userRole') || null);

  // Getters
  const isLoggedIn = computed(() => !!userToken.value);

  // Actions
  function login(loginData) {
    if (!loginData || !loginData.token) {
      console.error('Login action called with invalid data:', loginData);
      return;
    }
    userToken.value = loginData.token;
    userId.value = loginData.userId;
    username.value = loginData.username;
    userRole.value = loginData.role;

    localStorage.setItem('userToken', loginData.token);
    localStorage.setItem('userId', String(loginData.userId)); // Ensure userId is stored as string
    localStorage.setItem('username', loginData.username);
    localStorage.setItem('userRole', loginData.role);
    console.log('Auth state updated after login:', { 
      token: userToken.value, 
      id: userId.value, 
      name: username.value, 
      role: userRole.value 
    });
  }

  function logout() {
    userToken.value = null;
    userId.value = null;
    username.value = null;
    userRole.value = null;

    localStorage.removeItem('userToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userRole');
    console.log('Auth state cleared after logout.');
  }

  // Optional: Initialize/Check on load - currently handled by ref initializers
  // function initializeAuth() {
  //   const token = localStorage.getItem('userToken');
  //   if (token) {
  //     // Potentially add token validation logic here
  //     userToken.value = token;
  //     userId.value = localStorage.getItem('userId');
  //     username.value = localStorage.getItem('username');
  //     userRole.value = localStorage.getItem('userRole');
  //   }
  // }

  return { 
    userToken, 
    userId, 
    username, 
    userRole, 
    isLoggedIn, 
    login, 
    logout 
    // initializeAuth 
  };
}); 