<template>
  <div class="about-us-page">
    <!-- 头部 -->
    <header class="header">
      <button class="back-btn" @click="goBack">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">关于我们</h1>
    </header>

    <!-- 应用介绍 -->
    <div class="app-intro">
      <div class="logo-section">
        <div class="app-logo">🐾</div>
        <h2>MyFitPet</h2>
        <p class="app-version">v1.0.0</p>
      </div>
      <div class="app-description">
        <p>MyFitPet是一款创新的运动习惯养成应用，通过虚拟宠物陪伴的方式，让运动变得更加有趣和持续。</p>
        <p>我们相信，运动不应该是一件枯燥的事情。通过与可爱的虚拟宠物互动，您将在享受乐趣的同时，不知不觉地养成良好的运动习惯。</p>
      </div>
    </div>

    <!-- 团队介绍 -->
    <div class="team-section">
      <h2>我们的团队</h2>
      <div class="team-members">
        <div class="team-member">
          <div class="member-avatar">👨‍💻</div>
          <div class="member-info">
            <h3>邓博文</h3>
            <p>技术总监/后端</p>
          </div>
        </div>
        
        <div class="team-member">
          <div class="member-avatar">👩‍🎨</div>
          <div class="member-info">
            <h3>梁华思</h3>
            <p>UI/前端/产品</p>
          </div>
        </div>
        
        <div class="team-member">
          <div class="member-avatar">👨‍🔧</div>
          <div class="member-info">
            <h3>葛伊诺</h3>
            <p>后端开发工程师</p>
          </div>
        </div>
        
        <div class="team-member">
          <div class="member-avatar">👩‍🔧</div>
          <div class="member-info">
            <h3>张婷</h3>
            <p>市场调研/用户研究</p>
          </div>
        </div>
        
        <div class="team-member">
          <div class="member-avatar">👨‍🔬</div>
          <div class="member-info">
            <h3>方宇浩</h3>
            <p>前端/后端</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 联系方式 -->
    <div class="contact-section">
      <h2>联系我们</h2>
      <div class="contact-info">
        <div class="contact-item">
          <div class="contact-icon">📧</div>
          <div class="contact-text">contact@MyFitPet.com</div>
        </div>
        
        <div class="contact-item">
          <div class="contact-icon">🌐</div>
          <div class="contact-text">www.MyFitPet.com</div>
        </div>
        
        <div class="contact-item">
          <div class="contact-icon">🏢</div>
          <div class="contact-text">广东省广州市番禺区某某街道某某号</div>
        </div>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="copyright-section">
      <p>© 2025 MyFitPet Team. All rights reserved.</p>
      <p>京ICP备12345678号</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

// 导入图标
import backIcon from '@/图标/返回.svg'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.about-us-page {
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

.app-intro {
  background: white;
  border-radius: 16px;
  padding: 30px 20px;
  margin-bottom: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .logo-section {
    margin-bottom: 20px;
    
    .app-logo {
      font-size: 64px;
      margin-bottom: 16px;
    }
    
    h2 {
      font-size: 24px;
      color: #333;
      margin: 0 0 8px 0;
    }
    
    .app-version {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }
  
  .app-description {
    p {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin: 0 0 16px 0;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.team-section, .contact-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  h2 {
    font-size: 18px;
    color: #333;
    margin: 0 0 16px 0;
    text-align: center;
  }
}

.team-members {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.team-member {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .member-avatar {
    font-size: 32px;
    margin-bottom: 8px;
  }
  
  .member-info {
    text-align: center;
    
    h3 {
      font-size: 14px;
      color: #333;
      margin: 0 0 4px 0;
    }
    
    p {
      font-size: 12px;
      color: #999;
      margin: 0;
    }
  }
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .contact-icon {
    font-size: 20px;
    width: 30px;
    text-align: center;
  }
  
  .contact-text {
    font-size: 14px;
    color: #666;
  }
}

.copyright-section {
  text-align: center;
  padding: 20px;
  
  p {
    font-size: 12px;
    color: #999;
    margin: 0 0 8px 0;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}
</style>