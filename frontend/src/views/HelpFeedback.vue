<template>
  <div class="help-feedback-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">帮助与反馈</h1>
    </header>

    <!-- 常见问题 -->
    <div class="help-section">
      <h2>常见问题</h2>
      <div class="faq-list">
        <div 
          class="faq-item"
          v-for="(faq, index) in faqs"
          :key="index"
          @click="toggleFaq(index)"
        >
          <div class="faq-question">
            <h3>{{ faq.question }}</h3>
            <div class="faq-toggle">{{ expandedFaq === index ? '−' : '+' }}</div>
          </div>
          <div class="faq-answer" v-if="expandedFaq === index">
            <p>{{ faq.answer }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 联系我们 -->
    <div class="contact-section">
      <h2>联系我们</h2>
      <div class="contact-options">
        <div class="contact-option" @click="contactByEmail">
          <div class="contact-icon">📧</div>
          <div class="contact-info">
            <h3>电子邮件</h3>
            <p>support@petfit.com</p>
          </div>
        </div>
        
        <div class="contact-option" @click="contactByPhone">
          <div class="contact-icon">📞</div>
          <div class="contact-info">
            <h3>客服热线</h3>
            <p>400-123-4567</p>
          </div>
        </div>
        
        <div class="contact-option" @click="contactByWeChat">
          <div class="contact-icon">💬</div>
          <div class="contact-info">
            <h3>微信客服</h3>
            <p>扫描二维码添加</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 意见反馈 -->
    <div class="feedback-section">
      <h2>意见反馈</h2>
      <div class="feedback-form">
        <textarea 
          v-model="feedbackContent"
          placeholder="请描述您遇到的问题或建议..."
          rows="4"
        ></textarea>
        <div class="feedback-actions">
          <button @click="submitFeedback" class="submit-btn">提交反馈</button>
        </div>
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

// 常见问题数据
const faqs = ref([
  {
    question: '如何连接蓝牙设备？',
    answer: '请确保您的设备已开启蓝牙功能，然后在"运动"页面点击"连接设备"按钮，选择您要连接的设备即可。'
  },
  {
    question: '如何查看运动数据？',
    answer: '您可以在"数据"页面查看您的运动统计数据，包括步数、卡路里消耗、运动时长等信息。'
  },
  {
    question: '如何邀请好友加入队伍？',
    answer: '在"社群"页面点击"邀请好友"按钮，选择分享方式将邀请链接发送给好友即可。'
  },
  {
    question: '如何解锁成就？',
    answer: '完成相应的运动目标即可解锁成就，您可以在"数据"页面的"成就"部分查看已解锁的成就。'
  }
])

// 展开的FAQ索引
const expandedFaq = ref<number | null>(null)

// 反馈内容
const feedbackContent = ref('')

// 切换FAQ展开状态
const toggleFaq = (index: number) => {
  expandedFaq.value = expandedFaq.value === index ? null : index
}

// 联系方式
const contactByEmail = () => {
  console.log('通过邮件联系客服')
  // 实际项目中可以打开邮件应用
}

const contactByPhone = () => {
  console.log('通过电话联系客服')
  // 实际项目中可以拨打电话
}

const contactByWeChat = () => {
  console.log('通过微信联系客服')
  // 实际项目中可以显示二维码
}

// 提交反馈
const submitFeedback = () => {
  if (feedbackContent.value.trim() === '') {
    alert('请输入反馈内容')
    return
  }
  
  console.log('提交反馈:', feedbackContent.value)
  // 实际项目中可以发送到服务器
  
  alert('感谢您的反馈！我们会尽快处理。')
  feedbackContent.value = ''
}
</script>

<style scoped lang="scss">
.help-feedback-page {
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

.help-section, .contact-section, .feedback-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  h2 {
    font-size: 18px;
    color: #333;
    margin: 0 0 16px 0;
  }
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  
  .faq-question {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    cursor: pointer;
    
    h3 {
      font-size: 16px;
      color: #333;
      margin: 0;
      flex: 1;
    }
    
    .faq-toggle {
      font-size: 20px;
      color: #999;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .faq-answer {
    padding: 0 16px 16px;
    border-top: 1px solid #f0f0f0;
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0;
      line-height: 1.5;
    }
  }
}

.contact-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-option {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #f9f9f9;
  cursor: pointer;
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
    background: #f0f0f0;
  }
  
  .contact-icon {
    font-size: 24px;
  }
  
  .contact-info {
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
}

.feedback-form {
  textarea {
    width: 100%;
    padding: 16px;
    border: 1px solid #e0e0e0;
    border-radius: 12px;
    font-size: 16px;
    resize: vertical;
    margin-bottom: 16px;
    
    &:focus {
      outline: none;
      border-color: #91B3F0;
    }
  }
  
  .feedback-actions {
    text-align: center;
    
    .submit-btn {
      padding: 12px 32px;
      background: #91B3F0;
      color: white;
      border: none;
      border-radius: 24px;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s;
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
}
</style>