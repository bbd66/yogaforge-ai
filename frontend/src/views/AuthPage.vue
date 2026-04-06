<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-b from-sky-100 to-white px-4">
    <div class="w-full max-w-md bg-white/90 backdrop-blur rounded-3xl shadow-xl p-6 sm:p-8">
      <h1 class="text-2xl sm:text-3xl font-bold text-center text-sky-700 mb-2">
        MyFitPet
      </h1>
      <p class="text-center text-gray-500 mb-6">
        {{ isLogin ? '欢迎回来，先登录再陪宠物运动吧～' : '注册一个账号，开启你的宠物健康之旅' }}
      </p>

      <div class="flex mb-6 bg-sky-50 rounded-full p-1">
        <button
          class="flex-1 py-2 text-sm sm:text-base rounded-full transition-all"
          :class="isLogin ? 'bg-white text-sky-700 shadow' : 'text-gray-500'"
          @click="isLogin = true"
        >
          登录
        </button>
        <button
          class="flex-1 py-2 text-sm sm:text-base rounded-full transition-all"
          :class="!isLogin ? 'bg-white text-sky-700 shadow' : 'text-gray-500'"
          @click="isLogin = false"
        >
          注册
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
          <input
            v-model="form.email"
            type="email"
            required
            autocomplete="email"
            class="w-full px-3 py-2 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:border-sky-400 text-sm sm:text-base"
            placeholder="请输入邮箱"
          />
        </div>

        <div v-if="!isLogin">
          <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
          <input
            v-model="form.username"
            type="text"
            required
            minlength="2"
            maxlength="64"
            autocomplete="username"
            class="w-full px-3 py-2 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:border-sky-400 text-sm sm:text-base"
            placeholder="用于展示的昵称"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
          <input
            v-model="form.password"
            type="password"
            required
            minlength="6"
            maxlength="128"
            autocomplete="current-password"
            class="w-full px-3 py-2 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:border-sky-400 text-sm sm:text-base"
            placeholder="请输入密码（至少 6 位）"
          />
        </div>

        <div v-if="!isLogin">
          <label class="block text-sm font-medium text-gray-700 mb-1">头像链接（可选）</label>
          <input
            v-model="form.avatarUrl"
            type="url"
            maxlength="512"
            class="w-full px-3 py-2 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:border-sky-400 text-sm sm:text-base"
            placeholder="可以先留空，后面在个人中心再设置"
          />
        </div>

        <p v-if="errorMessage" class="text-sm text-red-500 mt-1">
          {{ errorMessage }}
        </p>

        <button
          type="submit"
          class="w-full py-2.5 sm:py-3 mt-2 rounded-2xl bg-sky-500 hover:bg-sky-600 text-white font-semibold text-sm sm:text-base flex items-center justify-center disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
          :disabled="submitting"
        >
          <span v-if="!submitting">{{ isLogin ? '登录' : '注册并登录' }}</span>
          <span v-else>处理中...</span>
        </button>
      </form>

      <p class="mt-4 text-xs text-gray-400 text-center">
        登录 / 注册即表示你同意我们的隐私政策
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

interface AuthForm {
  email: string
  username: string
  password: string
  avatarUrl: string
}

interface AuthResponse {
  id: number
  email: string
  username: string
  avatarUrl: string | null
  token: string
}

const router = useRouter()
const isLogin = ref(true)
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive<AuthForm>({
  email: '',
  username: '',
  password: '',
  avatarUrl: ''
})

const handleSubmit = async () => {
  console.log('AUTH_PAGE_SUBMIT_DEBUG', window.location.href)
  errorMessage.value = ''
  submitting.value = true
  try {
    let res: AuthResponse

    if (isLogin.value) {
      res = await api.post<AuthResponse>('/auth/login', {
        email: form.email,
        password: form.password
      })
    } else {
      res = await api.post<AuthResponse>('/auth/register', {
        email: form.email,
        username: form.username,
        password: form.password,
        avatarUrl: form.avatarUrl || null
      })
    }

    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res))

    router.replace('/home')
  } catch (err: any) {
    const status = err?.response?.status
    const data = err?.response?.data

    const code =
      typeof data === 'string'
        ? data
        : typeof data?.code === 'string'
          ? data.code
          : undefined

    if (status === 401 && (code === 'INVALID_CREDENTIALS' || data === 'INVALID_CREDENTIALS')) {
      errorMessage.value = '邮箱或密码错误，请重试'
    } else if (status === 409 && (code === 'EMAIL_EXISTS' || data === 'EMAIL_EXISTS')) {
      errorMessage.value = '该邮箱已被注册'
    } else if (status === 409 && (code === 'USERNAME_EXISTS' || data === 'USERNAME_EXISTS')) {
      errorMessage.value = '该用户名已被占用'
    } else if (status === 409 && (code === 'EMAIL_OR_USERNAME_EXISTS' || data === 'EMAIL_OR_USERNAME_EXISTS')) {
      errorMessage.value = '邮箱或用户名已存在'
    } else {
      errorMessage.value = '请求失败，请稍后再试'
      console.error('AUTH_ERROR_DEBUG', err)
    }
  } finally {
    submitting.value = false
  }
}
</script>
