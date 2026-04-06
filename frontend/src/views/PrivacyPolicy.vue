<template>
  <div class="privacy-policy-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">隐私政策</h1>
    </header>

    <!-- 政策内容 -->
    <div class="policy-content">
      <div class="policy-section">
        <h2>引言</h2>
        <p>PetFit（以下简称"我们"）非常重视您的隐私保护。本隐私政策旨在向您说明我们如何收集、使用、存储和保护您的个人信息，以及您享有的相关权利。请您在使用我们的服务前，仔细阅读并充分理解本政策。</p>
      </div>

      <div class="policy-section">
        <h2>信息收集</h2>
        <p>为了向您提供更好的服务，我们会收集以下信息：</p>
        <ul>
          <li><strong>账户信息</strong>：您注册时提供的用户名、邮箱、手机号等</li>
          <li><strong>设备信息</strong>：您授权连接的运动设备数据，包括步数、心率、卡路里消耗等</li>
          <li><strong>位置信息</strong>：在您授权的情况下，我们会收集您的位置信息以提供更精准的运动分析</li>
          <li><strong>使用信息</strong>：您使用我们服务时的操作记录，如页面访问、功能使用等</li>
        </ul>
      </div>

      <div class="policy-section">
        <h2>信息使用</h2>
        <p>我们收集的信息将用于以下目的：</p>
        <ul>
          <li>为您提供个性化的运动计划和健康建议</li>
          <li>优化产品功能，提升用户体验</li>
          <li>进行数据分析，改进服务质量</li>
          <li>在法律要求或允许的情况下，用于其他合法目的</li>
        </ul>
      </div>

      <div class="policy-section">
        <h2>信息保护</h2>
        <p>我们采取严格的技术和管理措施保护您的信息安全：</p>
        <ul>
          <li>使用加密技术保护数据传输和存储安全</li>
          <li>建立完善的访问控制机制，防止未授权访问</li>
          <li>定期进行安全评估和漏洞修复</li>
          <li>对员工进行隐私保护培训，严格限制数据访问权限</li>
        </ul>
      </div>

      <div class="policy-section">
        <h2>信息共享</h2>
        <p>我们不会向任何无关第三方出售、出租或泄露您的个人信息，以下情况除外：</p>
        <ul>
          <li>获得您的明确授权</li>
          <li>根据法律法规要求</li>
          <li>为维护公共利益或国家安全所需</li>
          <li>与合作伙伴共同提供服务时，在必要范围内共享信息</li>
        </ul>
      </div>

      <div class="policy-section">
        <h2>您的权利</h2>
        <p>您享有以下权利：</p>
        <ul>
          <li><strong>访问权</strong>：您可以随时查看您的个人信息</li>
          <li><strong>更正权</strong>：您可以修改不准确的个人信息</li>
          <li><strong>删除权</strong>：您可以要求删除您的个人信息</li>
          <li><strong>撤回权</strong>：您可以撤回已授予的同意</li>
        </ul>
      </div>

      <div class="policy-section">
        <h2>联系我们</h2>
        <p>如果您对本隐私政策有任何疑问或建议，请通过以下方式联系我们：</p>
        <div class="contact-info">
          <p>电子邮箱：privacy@petfit.com</p>
          <p>客服热线：400-123-4567</p>
          <p>邮寄地址：北京市朝阳区某某街道某某号</p>
        </div>
      </div>

      <div class="policy-section">
        <h2>政策更新</h2>
        <p>我们可能会适时更新本隐私政策。更新后的政策会在应用内公布，并标注更新日期。建议您定期查看以了解最新内容。</p>
        <div class="policy-date">
          <p>最后更新日期：2025年1月15日</p>
        </div>
      </div>
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
.privacy-policy-page {
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

.policy-content {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.policy-section {
  margin-bottom: 24px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  h2 {
    font-size: 18px;
    color: #333;
    margin: 0 0 12px 0;
  }
  
  p {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin: 0 0 12px 0;
  }
  
  ul {
    padding-left: 20px;
    margin: 0 0 12px 0;
    
    li {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      strong {
        font-weight: 500;
        color: #333;
      }
    }
  }
  
  .contact-info {
    background: #f9f9f9;
    padding: 16px;
    border-radius: 12px;
    
    p {
      margin: 0 0 8px 0;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
  
  .policy-date {
    text-align: center;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    
    p {
      font-size: 12px;
      color: #999;
      margin: 0;
    }
  }
}
</style>