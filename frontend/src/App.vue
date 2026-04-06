<template>
 <div style="background: #fffae6; padding: 8px; text-align: center;">
    本机 Vite 开发环境 TEST_999
  </div>
 <div class="app-container">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    
    <!-- 底部导航栏 -->
    <nav v-if="showTabBar" class="tab-bar">
      <router-link 
        v-for="tab in tabs" 
        :key="tab.path" 
        :to="tab.path"
        class="tab-item"
        :class="{ active: currentPath === tab.path }"
      >
        <img :src="tab.icon" :alt="tab.label" class="tab-icon" />
        <span class="tab-label">{{ tab.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
// 导入图标
import homeIcon from './图标/首页.png'
import dataIcon from './图标/数据.png'
import workoutIcon from './图标/连接.png'
import communityIcon from './图标/社群.png'
import profileIcon from './图标/我.png'

const route = useRoute()
const currentPath = computed(() => route.path)

// 应用已保存的主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme') || 'light'
  const body = document.body
  
  // 根据保存的主题添加对应的类
  if (savedTheme === 'dark') {
    body.classList.add('dark-theme')
  } else if (savedTheme === 'light') {
    body.classList.add('light-theme')
  } else {
    // 自动模式 - 根据时间判断
    const hour = new Date().getHours()
    if (hour < 6 || hour >= 18) {
      body.classList.add('dark-theme')
    } else {
      body.classList.add('light-theme')
    }
  }
})

const tabs = [
  { path: '/home', label: '首页', icon: homeIcon },
  { path: '/data', label: '数据', icon: dataIcon },
  { path: '/workout', label: '运动', icon: workoutIcon },
  { path: '/community', label: '社群', icon: communityIcon },
  { path: '/profile', label: '我的', icon: profileIcon }
]

const showTabBar = computed(() => {
  const hiddenPaths = ['/login', '/register']
  return !hiddenPaths.includes(currentPath.value)
})
</script>

<style scoped lang="scss">
.app-container {
  width: 100%;
  min-height: 100vh;
  background: #91B3F0;
  padding-bottom: 60px;
  
  // 深色主题
  body.dark-theme & {
    background: #1a1a1a;
  }
}

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
  
  // 深色主题
  body.dark-theme & {
    background: #444;
  }
  
  @media (min-width: 768px) {
    left: 50%;
    transform: translateX(-50%);
    max-width: 414px;
  }
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #999;
  text-decoration: none;
  transition: all 0.3s;
  
  .tab-icon {
    width: 24px;
    height: 24px;
  }
  
  .tab-label {
    font-size: 12px;
  }
  
  &.active {
    color: #F08713;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>