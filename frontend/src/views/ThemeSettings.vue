<template>
  <div class="theme-settings-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">深色模式</h1>
    </header>

    <!-- 主题选项 -->
    <div class="theme-options">
      <div 
        class="theme-option"
        :class="{ active: currentTheme === 'light' }"
        @click="selectTheme('light')"
      >
        <div class="theme-preview light-preview">
          <div class="preview-header"></div>
          <div class="preview-content">
            <div class="preview-card"></div>
            <div class="preview-card"></div>
          </div>
        </div>
        <div class="theme-info">
          <h3>浅色模式</h3>
          <p>明亮清新的界面风格</p>
        </div>
        <div class="theme-check" v-if="currentTheme === 'light'">✓</div>
      </div>

      <div 
        class="theme-option"
        :class="{ active: currentTheme === 'dark' }"
        @click="selectTheme('dark')"
      >
        <div class="theme-preview dark-preview">
          <div class="preview-header"></div>
          <div class="preview-content">
            <div class="preview-card"></div>
            <div class="preview-card"></div>
          </div>
        </div>
        <div class="theme-info">
          <h3>深色模式</h3>
          <p>护眼舒适的夜间体验</p>
        </div>
        <div class="theme-check" v-if="currentTheme === 'dark'">✓</div>
      </div>

      <div 
        class="theme-option"
        :class="{ active: currentTheme === 'auto' }"
        @click="selectTheme('auto')"
      >
        <div class="theme-preview auto-preview">
          <div class="preview-header"></div>
          <div class="preview-content">
            <div class="preview-card"></div>
            <div class="preview-card"></div>
          </div>
        </div>
        <div class="theme-info">
          <h3>自动切换</h3>
          <p>根据时间自动切换主题</p>
        </div>
        <div class="theme-check" v-if="currentTheme === 'auto'">✓</div>
      </div>
    </div>

    <!-- 自动切换设置 -->
    <div class="auto-switch-settings" v-if="currentTheme === 'auto'">
      <h2>自动切换时间</h2>
      <div class="time-setting">
        <div class="time-item">
          <label>日出时间</label>
          <input type="time" v-model="sunriseTime" />
        </div>
        <div class="time-item">
          <label>日落时间</label>
          <input type="time" v-model="sunsetTime" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 导入图标
import backIcon from '@/图标/返回.svg'

const router = useRouter()

const goBack = () => {
  router.back()
}

// 当前主题设置
const currentTheme = ref('light')

// 日出日落时间
const sunriseTime = ref('06:00')
const sunsetTime = ref('18:00')

// 选择主题
const selectTheme = (theme: string) => {
  currentTheme.value = theme
  console.log('选择主题:', theme)
  
  // 这里可以保存到本地存储或用户设置中
  localStorage.setItem('theme', theme)
  
  // 应用主题到整个应用
  applyTheme(theme)
}

// 应用主题到整个应用
const applyTheme = (theme: string) => {
  const body = document.body
  
  // 移除所有主题类
  body.classList.remove('light-theme', 'dark-theme')
  
  // 根据选择的主题添加对应的类
  if (theme === 'dark') {
    body.classList.add('dark-theme')
  } else if (theme === 'light') {
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
}

// 页面加载时应用已保存的主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme') || 'light'
  currentTheme.value = savedTheme as 'light' | 'dark' | 'auto'
  applyTheme(savedTheme)
})
</script>

<style scoped lang="scss">
.theme-settings-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
  padding-top: 60px;
  padding-bottom: 80px;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
  
  .back-btn {
    width: 40px;
    height: 40px;
    border: none;
    background: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .back-icon {
      width: 24px;
      height: 24px;
    }
  }
  
  .page-title {
    flex: 1;
    text-align: center;
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
  }
}

.theme-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.active {
    border: 2px solid #91B3F0;
  }
  
  .theme-preview {
    width: 80px;
    height: 100px;
    border-radius: 8px;
    overflow: hidden;
    
    .preview-header {
      height: 20px;
    }
    
    .preview-content {
      padding: 8px;
      
      .preview-card {
        height: 16px;
        border-radius: 4px;
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
  
  .light-preview {
    background: #f0f0f0;
    
    .preview-header {
      background: #e0e0e0;
    }
    
    .preview-card {
      background: #ddd;
    }
  }
  
  .dark-preview {
    background: #333;
    
    .preview-header {
      background: #444;
    }
    
    .preview-card {
      background: #555;
    }
  }
  
  .auto-preview {
    background: linear-gradient(to bottom, #f0f0f0 0%, #f0f0f0 50%, #333 50%, #333 100%);
    
    .preview-header {
      background: #e0e0e0;
    }
    
    .preview-card {
      background: #ddd;
    }
    
    .preview-content .preview-card:last-child {
      background: #555;
    }
  }
  
  .theme-info {
    flex: 1;
    
    h3 {
      font-size: 16px;
      color: #333;
      margin: 0 0 4px 0;
    }
    
    p {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }
  
  .theme-check {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #91B3F0;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
  }
}

.auto-switch-settings {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  h2 {
    font-size: 18px;
    color: #333;
    margin: 0 0 16px 0;
  }
  
  .time-setting {
    display: flex;
    gap: 16px;
  }
  
  .time-item {
    flex: 1;
    
    label {
      display: block;
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }
    
    input {
      width: 100%;
      padding: 12px;
      border: 1px solid #e0e0e0;
      border-radius: 12px;
      font-size: 16px;
      
      &:focus {
        outline: none;
        border-color: #91B3F0;
      }
    }
  }
}
</style>