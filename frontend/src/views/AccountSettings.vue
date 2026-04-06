<template>
  <div class="account-settings-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <button @click="goBack" class="back-btn">
        <img :src="backIcon" alt="返回" class="back-icon" />
      </button>
      <h1 class="page-title">账户设置</h1>
    </header>

    <!-- 用户信息卡片 -->
    <div class="user-info-card">
      <div class="avatar-section">
        <div class="avatar-container">
          <div class="user-avatar">{{ form.avatar || user.avatar }}</div>
          <button @click="changeAvatar" class="change-avatar-btn">✏️</button>
        </div>
        <p class="avatar-tip">点击头像更换</p>
      </div>

      <div class="info-item">
        <label class="info-label">用户名</label>
        <input
          v-model="form.username"
          type="text"
          class="info-input"
          maxlength="64"
          placeholder="请输入用户名"
        />
      </div>

      <div class="info-item">
        <label class="info-label">ID号</label>
        <div class="info-value readonly">{{ user.id }}</div>
      </div>

      <div class="info-item">
        <label class="info-label">邮箱</label>
        <input
          v-model="form.email"
          type="email"
          class="info-input"
          placeholder="请输入邮箱"
        />
      </div>

      <!-- 已去除手机号编辑项 -->

      <div class="info-item">
        <label class="info-label">注册时间</label>
        <div class="info-value readonly">{{ formatDate(user.createdAt) }}</div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions-section">
      <button @click="saveProfile" class="action-btn primary" :disabled="saving">
        {{ saving ? '保存中...' : '保存资料' }}
      </button>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      <p v-if="successMessage" class="success-text">{{ successMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import backIcon from '@/图标/返回.svg'

const router = useRouter()
const userStore = useUserStore()
const { user, setUser } = userStore

const form = reactive({
  username: user.username,
  email: user.email,
  avatar: user.avatar
})

const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const goBack = () => {
  router.back()
}

const changeAvatar = () => {
  console.log('更换头像')
  // 这里可以实现更换头像的逻辑
}

const saveProfile = async () => {
  try {
    saving.value = true
    errorMessage.value = ''
    successMessage.value = ''

    const payload: {
      username?: string
      avatarUrl?: string | null
      email?: string
    } = {}

    if (form.username && form.username !== user.username) {
      payload.username = form.username
    }
    if (form.avatar && form.avatar !== user.avatar) {
      payload.avatarUrl = form.avatar
    }
    if (form.email && form.email !== user.email) {
      payload.email = form.email
    }

    if (!payload.username && !payload.avatarUrl && !payload.email) {
      successMessage.value = '没有需要保存的修改'
      return
    }

    const res = await (await import('@/api')).default.patch<{
      id: number
      username: string
      email: string
      avatarUrl?: string | null
      updatedAt: string
    }>('/users/me', payload)

    // 同步更新到用户 store
    setUser({
      id: res.id,
      username: res.username,
      email: res.email,
      avatar: res.avatarUrl || null
    } as any)

    // 同步本地表单
    form.username = res.username
    form.email = res.email
    form.avatar = res.avatarUrl || ''

    successMessage.value = '资料已更新'
  } catch (e: any) {
    console.error('更新资料失败', e)
    errorMessage.value = e?.response?.data || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
}
</script>

<style scoped lang="scss">
.account-settings-page {
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

.user-info-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .avatar-section {
    text-align: center;
    margin-bottom: 30px;
    
    .avatar-container {
      position: relative;
      display: inline-block;
      margin-bottom: 12px;
      
      .user-avatar {
        width: 80px;
        height: 80px;
        background: #f0f0f0;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 40px;
        margin: 0 auto;
        border: 3px solid #F08713;
      }
      
      .change-avatar-btn {
        position: absolute;
        bottom: 0;
        right: -10px;
        width: 32px;
        height: 32px;
        border: none;
        border-radius: 50%;
        background: white;
        font-size: 16px;
        cursor: pointer;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        &:active {
          transform: scale(0.95);
        }
      }
    }
    
    .avatar-tip {
      font-size: 12px;
      color: #999;
      margin: 0;
    }
  }
  
  .info-item {
    display: flex;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .info-label {
      width: 80px;
      font-size: 14px;
      color: #999;
      margin-right: 16px;
    }
    
    .info-value {
      flex: 1;
      font-size: 16px;
      color: #333;
      &.readonly {
        color: #999;
      }
    }

    .info-input {
      flex: 1;
      font-size: 16px;
      color: #333;
      border: none;
      outline: none;
      background: transparent;
    }
  }
}

.actions-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .action-btn {
    padding: 16px;
    border: none;
    border-radius: 12px;
    background: white;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    &.primary {
      background: #F08713;
      color: white;
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
}

.error-text {
  margin-top: 8px;
  font-size: 14px;
  color: #ff4d4f;
}

.success-text {
  margin-top: 8px;
  font-size: 14px;
  color: #52c41a;
}
</style>