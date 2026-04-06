<template>
  <div class="language-settings-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">语言设置</h1>
    </header>

    <!-- 语言选项 -->
    <div class="language-options">
      <div 
        class="language-option"
        :class="{ active: currentLanguage === 'zh' }"
        @click="selectLanguage('zh')"
      >
        <div class="flag-icon">🇨🇳</div>
        <div class="language-info">
          <h3>简体中文</h3>
          <p>系统默认语言</p>
        </div>
        <div class="language-check" v-if="currentLanguage === 'zh'">✓</div>
      </div>

      <div 
        class="language-option"
        :class="{ active: currentLanguage === 'en' }"
        @click="selectLanguage('en')"
      >
        <div class="flag-icon">🇺🇸</div>
        <div class="language-info">
          <h3>English</h3>
          <p>English (United States)</p>
        </div>
        <div class="language-check" v-if="currentLanguage === 'en'">✓</div>
      </div>

      <div 
        class="language-option"
        :class="{ active: currentLanguage === 'ja' }"
        @click="selectLanguage('ja')"
      >
        <div class="flag-icon">🇯🇵</div>
        <div class="language-info">
          <h3>日本語</h3>
          <p>日本語 (にほんご)</p>
        </div>
        <div class="language-check" v-if="currentLanguage === 'ja'">✓</div>
      </div>

      <div 
        class="language-option"
        :class="{ active: currentLanguage === 'ko' }"
        @click="selectLanguage('ko')"
      >
        <div class="flag-icon">🇰🇷</div>
        <div class="language-info">
          <h3>한국어</h3>
          <p>한국어 (대한민국)</p>
        </div>
        <div class="language-check" v-if="currentLanguage === 'ko'">✓</div>
      </div>
    </div>

    <!-- 应用范围 -->
    <div class="application-scope">
      <h2>应用范围</h2>
      <div class="scope-option">
        <div class="scope-info">
          <h3>系统语言</h3>
          <p>影响整个应用的界面语言</p>
        </div>
        <label class="switch">
          <input type="checkbox" v-model="systemLanguage" />
          <span class="slider"></span>
        </label>
      </div>
      
      <div class="scope-option">
        <div class="scope-info">
          <h3>内容语言</h3>
          <p>影响运动指导和健康建议的语言</p>
        </div>
        <label class="switch">
          <input type="checkbox" v-model="contentLanguage" />
          <span class="slider"></span>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

// 导入图标
import backIcon from '@/图标/返回.svg'

const router = useRouter()

const goBack = () => {
  router.back()
}

// 当前语言设置
const currentLanguage = ref('zh')

// 应用范围设置
const systemLanguage = ref(true)
const contentLanguage = ref(true)

// 选择语言
const selectLanguage = (language: string) => {
  currentLanguage.value = language
  console.log('选择语言:', language)
  
  // 这里可以保存到本地存储或用户设置中
  localStorage.setItem('language', language)
}
</script>

<style scoped lang="scss">
.language-settings-page {
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

.language-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.language-option {
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
  
  .flag-icon {
    font-size: 32px;
  }
  
  .language-info {
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
  
  .language-check {
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

.application-scope {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  h2 {
    font-size: 18px;
    color: #333;
    margin: 0 0 16px 0;
  }
  
  .scope-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .scope-info {
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
    
    .switch {
      position: relative;
      display: inline-block;
      width: 48px;
      height: 28px;
      
      input {
        opacity: 0;
        width: 0;
        height: 0;
        
        &:checked + .slider {
          background-color: #91B3F0;
        }
        
        &:checked + .slider:before {
          transform: translateX(20px);
        }
      }
      
      .slider {
        position: absolute;
        cursor: pointer;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #ccc;
        transition: .4s;
        border-radius: 34px;
        
        &:before {
          position: absolute;
          content: "";
          height: 20px;
          width: 20px;
          left: 4px;
          bottom: 4px;
          background-color: white;
          transition: .4s;
          border-radius: 50%;
        }
      }
    }
  }
}
</style>