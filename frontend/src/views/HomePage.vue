<template>
  <div class="home-page" :style="{ backgroundImage: `url(${homeBackground})` }">
    <!-- 头部 -->
    <header class="header">
      <button class="shop-btn" @click="goToShop">
        <img :src="shopIcon" alt="商城" class="shop-icon" />
      </button>
    </header>

    <!-- 宠物信息 -->
    <div class="pet-info">
      <h2 class="pet-name">{{ petName }}</h2>
      <div class="pet-level">Lv.{{ petLevel }}</div>
      <div class="exp-bar">
        <div class="exp-fill" :style="{ width: expPercentage + '%' }"></div>
      </div>
      <div class="exp-text">{{ petExp }} / {{ nextLevelExp }} EXP</div>
    </div>

    <!-- 宠物状态指示器 (顶部并排显示) -->
    <div class="top-status-indicators">
      <div class="status-item small">
        <div class="status-content">
          <div class="status-label">心情值 {{ pet.status.happiness }}%</div>
          <div class="status-bar">
            <div
              class="status-fill happiness"
              :style="{ width: pet.status.happiness + '%' }"
            ></div>
          </div>
        </div>
      </div>

      <div class="status-item small">
        <div class="status-content">
          <div class="status-label">能量值 {{ pet.status.energy }}%</div>
          <div class="status-bar">
            <div
              class="status-fill energy"
              :style="{ width: pet.status.energy + '%' }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 和宠物聊聊天（唯一的主聊天框，窄一点，居中在宠物上方） -->
    <div class="main-chat-panel">
      <h3 class="chat-title">和宠物聊聊天</h3>
      <div class="chat-window">
        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            class="chat-message"
            :class="msg.role === 'user' ? 'from-user' : 'from-pet'"
          >
            <div class="chat-bubble">
              <span class="chat-role" v-if="msg.role === 'user'">我：</span>
              <span class="chat-role" v-else>宠物：</span>
              <span class="chat-text">{{ msg.text }}</span>
            </div>
          </div>
          <div v-if="isChatLoading" class="chat-loading">宠物思考中...</div>
        </div>
        <div class="chat-input-area">
          <textarea
            v-model="chatInput"
            class="chat-input"
            placeholder="跟宠物说点什么吧..."
            rows="2"
            @keydown.enter.exact.prevent="sendChat"
          ></textarea>
          <button
            class="chat-send-btn"
            :disabled="isChatLoading || !chatInput.trim()"
            @click="sendChat"
          >
            发送
          </button>
        </div>
      </div>
    </div>

    <!-- 宠物展示区：Live2D 模型，整体放在页面中间偏下 -->
    <div class="pet-display">
      <div class="pet-avatar" @click="testClick">
        <Live2DWidget
          jsonPath="/live2d/cat-white/model.json"
          :width="300"
          :height="300"
          :vOffset="60"
          @click="testClick"
          @ready="onLive2DReady"
        />
      </div>
    </div>

    <!-- 特效覆盖层 -->
    <transition name="fade">
      <div v-if="currentEffect" class="effect-overlay">
        <div class="effect-content" :class="currentEffect.type">
          <img :src="currentEffect.icon" alt="effect" class="effect-icon" />
          <!-- 泡沫效果 -->
          <div v-if="currentEffect.type === 'clean'" class="foam-container">
            <img
              v-for="i in 8"
              :key="i"
              :src="foamIcon"
              class="foam-bubble"
              :style="{
                left: (i * 20 - 40) + 'px',
                top: (Math.sin(i) * 10 - 20) + 'px',
                animationDelay: (i * 0.1) + 's'
              }"
            />
          </div>
          <!-- 饲料掉落效果 -->
          <div v-if="currentEffect.type === 'feed'" class="food-container">
            <img
              v-for="i in 8"
              :key="i"
              :src="foodIcon"
              class="food-item"
              :style="{
                left: (i * 15 - 60) + 'px',
                animationDelay: (i * 0.1) + 's'
              }"
            />
          </div>
        </div>
      </div>
    </transition>

    <!-- 快速操作按钮 -->
    <div class="action-buttons">
      <button @click="handleFeed" class="action-btn feed">
        <img :src="feedIcon" alt="喂食" class="btn-icon" />
        <span v-if="feedCount > 0" class="badge">{{ feedCount }}</span>
        <span class="btn-label">喂食</span>
      </button>
      <button @click="handlePlay" class="action-btn play">
        <img :src="playIcon" alt="玩耍" class="btn-icon" />
        <span class="btn-label">玩耍</span>
      </button>
      <button @click="handleClean" class="action-btn clean">
        <img :src="cleanIcon" alt="清洁" class="btn-icon" />
        <span class="btn-label">清洁</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, nextTick } from 'vue'
import Live2DWidget from '@/components/Live2DWidget.vue'
import { useRouter } from 'vue-router'
import { usePetStore } from '@/stores/pet'
import { storeToRefs } from 'pinia'
import api from '@/api'
import feedIcon from '@/图标/投喂.svg'
import playIcon from '@/图标/玩耍.svg'
import cleanIcon from '@/图标/洗澡.svg'
import shopIcon from '@/图标/商城.svg'
import foamIcon from '@/图标/泡沫.png'
import foodIcon from '@/图标/饲料.png'
import showerIcon from '@/图标/花洒.png'
import bowlIcon from '@/图标/碗.png'
import ballIcon from '@/图标/皮球.png'
import homeBackground from '@/图标/首页背景.png?url'

const router = useRouter()
const petStore = usePetStore()
const { feed, play, clean, updateMotivationalQuote, addFeedCount } = petStore
const { pet, feedCount } = storeToRefs(petStore)

// 定义动效类型
type EffectType = {
  type: 'feed' | 'play' | 'clean';
  icon: string;
}

const currentEffect = ref<EffectType | null>(null)
let effectTimeout: ReturnType<typeof setTimeout> | null = null

// 后端宠物基础信息
const petName = ref('我的宠物')
const petLevel = ref(1)
const petExp = ref(0)
const petGold = ref(0)

// 简单的等级经验公式
const nextLevelExp = computed(() => petLevel.value * 100)

const testClick = () => {
  fetchNewQuote()
}

const onLive2DReady = () => {
  console.log('Live2D whitecat 模型已就绪')
}

// 计算经验值百分比
const expPercentage = computed(() => {
  if (nextLevelExp.value === 0) return 0
  const percent = (petExp.value / nextLevelExp.value) * 100
  return Math.max(0, Math.min(100, Math.round(percent)))
})

// 从后端加载宠物等级和经验
const loadPetInfo = async () => {
  try {
    const res = await api.get<{
      id: number
      name: string
      level: number
      exp: number
      gold: number
    }>('/pets/me')
    petName.value = res.name
    petLevel.value = res.level
    petExp.value = res.exp
    petGold.value = res.gold
  } catch (e) {
    console.error('获取宠物信息失败', e)
  }
}

onMounted(() => {
  loadPetInfo()
})

const handleFeed = () => {
  if (feedCount.value <= 0) {
    alert('还没有食物可以喂给宠物哦，先去商城购买吧~')
    return
  }
  addFeedCount(-1)
  feed()
  triggerEffect('feed', bowlIcon)
}

const handlePlay = () => {
  play()
  triggerEffect('play', ballIcon)
}

const handleClean = () => {
  clean()
  triggerEffect('clean', showerIcon)
}


const goToShop = () => {
  router.push('/shop')
}

// 触发特效的通用函数
const triggerEffect = (type: 'feed' | 'play' | 'clean', icon: string) => {
  // 如果有正在进行的动画，先清除
  if (effectTimeout) clearTimeout(effectTimeout)
  
  // 设置当前特效
  currentEffect.value = { type, icon }
  
  // 喂食和玩耍特效持续2秒，清洁特效1.5秒后清除
  const duration = type === 'feed' || type === 'play' ? 2000 : 1500
  effectTimeout = setTimeout(() => {
    currentEffect.value = null
  }, duration)
}

// ---------------- 宠物聊天 ----------------

type ChatHistoryPair = [string, string]

interface ChatMessageItem {
  role: 'user' | 'assistant'
  text: string
}

const chatMessages = ref<ChatMessageItem[]>([])
const chatInput = ref('')
const isChatLoading = ref(false)
const chatHistory = ref<ChatHistoryPair[]>([])
const messagesContainer = ref<HTMLDivElement | null>(null)

const scrollChatToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(chatMessages, () => {
  scrollChatToBottom()
})

const sendChat = async () => {
  const content = chatInput.value.trim()
  if (!content || isChatLoading.value) return

  chatMessages.value.push({ role: 'user', text: content })
  chatInput.value = ''
  isChatLoading.value = true

  try {
    const res = await api.post<{
      response: string
      history: ChatHistoryPair[]
    }>('/chat', {
      query: content,
      history: chatHistory.value
    })

    const replyText = res.response ?? ''
    chatMessages.value.push({ role: 'assistant', text: replyText })
    chatHistory.value = res.history ?? chatHistory.value
  } catch (e) {
    console.error('聊天失败', e)
    chatMessages.value.push({
      role: 'assistant',
      text: '宠物今天有点累，稍后再聊好不好呀~'
    })
  } finally {
    isChatLoading.value = false
  }
}


</script>

<style scoped lang="scss">
.home-page {
  position: relative;
  min-height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 20px;
  padding-top: 60px;
  padding-bottom: 80px;

  body.dark-theme & {
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }
}

.header {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;

  .shop-btn {
    width: 40px;
    height: 40px;
    border: none;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      transform: scale(0.95);
    }

    .shop-icon {
      width: 20px;
      height: 20px;
    }
  }
}

.pet-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 23px;
  margin-left: 2px;
}

.pet-info .pet-name {
  color: rgba(0, 0, 0, 0.5);
  font-size: 16px;
  margin: 0;
}

.pet-info .pet-level {
  display: inline-block;
  background: rgba(0, 0, 0, 0.1);
  color: white;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.pet-info .exp-bar {
  width: 80px;
  height: 6px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
  overflow: hidden;
  margin-left: 10px;
}

.pet-info .exp-fill {
  height: 100%;
  background: #fff2cc;
  transition: width 0.3s;
}

/* 顶部状态指示器 */
.top-status-indicators {
  display: flex;
  gap: 12px;
  margin-top: -20px;
  margin-bottom: 12px;
}

.status-item {
  &.small {
    width: 181px;
    height: 57px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    padding: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .status-content {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .status-label {
      font-size: 12px;
      color: #666;
    }

    .status-bar {
      height: 8px;
      background: #e0e0e0;
      border-radius: 4px;
      overflow: hidden;

      .status-fill {
        height: 100%;
        transition: width 0.3s;
        border-radius: 4px;

        &.happiness {
          background: #8faadc;
        }

        &.energy {
          background: #91b3f0;
        }
      }
    }
  }
}

/* 主聊天框：心情值下方、居中且变窄 */
.main-chat-panel {
  margin: 4px auto 4px;
  width: 100%;
  max-width: 320px; /* 保持较窄 */
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 6px 8px 8px; /* 原来 10px 10px 12px，整体减小内边距 */
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 聊天通用样式（只服务 main-chat-panel） */
.chat-title {
  font-size: 13px;           /* 稍微小一点 */
  font-weight: 600;
  margin: 0 0 4px;           /* 缩小标题和内容间距 */
  color: #333;
}

.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-messages {
  flex: 1;
  min-height: 60px;          /* 原来 120px，减半，整体高度会明显变小 */
  max-height: 140px;         /* 原来 240px，限制能撑高的最大值 */
  overflow-y: auto;
  padding: 4px 2px;          /* 减小内部留白 */
}

.chat-message {
  display: flex;
  margin-bottom: 4px;        /* 行间距更紧凑 */
}

.chat-bubble {
  max-width: 80%;
  padding: 4px 6px;          /* 气泡本身更薄 */
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.3;
}

.chat-input-area {
  display: flex;
  align-items: flex-end;
  gap: 4px;                   /* 输入区更紧凑 */
  margin-top: 4px;
}

.chat-input {
  flex: 1;
  resize: none;
  border-radius: 8px;
  border: 1px solid #ddd;
  padding: 4px 6px;           /* 输入框更薄 */
  font-size: 12px;
  outline: none;
}

.chat-send-btn {
  border: none;
  border-radius: 999px;
  padding: 4px 8px;           /* 按钮更小 */
  font-size: 12px;
  background: #91b3f0;
  color: #fff;
  cursor: pointer;
}

/* 宠物展示区域：整体放在页面中间偏下 */
.pet-display {
  position: relative;
  text-align: center;
  margin-top: 10px;
  margin-bottom: 30px;
  z-index: 20;

  .pet-avatar {
    width: 300px;
    height: 300px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
}

.pet-info-and-quote {
  position: absolute;
  top: 260px;
  left: 20px;
  z-index: 5;
}

.quote-card {
  position: relative;
  width: 200px;
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;

  .bubble-icon {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .quote-text {
    position: relative;
    color: #333;
    font-size: 12px;
    line-height: 1.4;
    text-align: center;
    z-index: 1;
    padding: 15px 10px;
    margin: 0;
    width: 68%;
  }
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 300px;

  .action-btn {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border: none;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.9);
    cursor: pointer;
    transition: all 0.3s;
    width: 60px;
    height: 60px;
    justify-content: center;

    .btn-icon {
      width: 24px;
      height: 24px;
    }

    .btn-label {
      display: none;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

.badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #4c9dff; /* 蓝色小圆点 */
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.8);
}

/* ------ 新增：动效样式 ------ */
.effect-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 100; // 确保在最上层
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: none; // 让点击穿透，不影响底层操作
}

.effect-content {
  display: flex;
  justify-content: center;
  align-items: center;
  
  .effect-icon {
    width: 100px;
    height: 100px;
    filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
  }

  // 清洁特效图标大小调整
  &.clean .effect-icon {
    width: 80px;
    height: 80px;
  }

  // 喂食动画：碗从左边移动到宠物脚下
  &.feed {
    position: absolute;
    bottom: 150px; 
    left: -100px; /* 初始位置在画面左边 */
    animation: bowlMoveToPet 2s ease-in-out forwards;
    
    .food-container {
      position: absolute;
      top: -150px;
      left: 50%;
      transform: translateX(-50%);
      width: 100px; /* 调整宽度以匹配碗的宽度 */
      height: 150px;
      
      .food-item {
        position: absolute;
        width: 20px;
        height: 20px;
        animation: foodDrop 1s ease-in-out forwards;
      }
    }
  }

  // 玩耍动画：皮球从上方掉落到宠物脚下并弹跳
  &.play {
    position: absolute;
    bottom: 150px; /* 与碗的位置一致 */
    left: 50%;
    transform: translateX(-50%);
    animation: ballDropAndBounce 2s ease-in-out forwards;
  }

  // 清洁动画：花洒在宠物上方左右移动并产生泡沫
  &.clean {
    animation: showerMove 2s ease-in-out infinite;
    position: relative;
    
    .foam-container {
      position: absolute;
      top: 100%;
      left: 50%;
      transform: translateX(-50%);
      width: 200px;
      height: 100px;
      margin-top: 15px;
      
      .foam-bubble {
        position: absolute;
        width: 20px;
        height: 20px;
        animation: foamBubble 1.5s ease-out infinite;
      }
    }
  }
}

// Vue Transition 的淡入淡出效果
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 关键帧定义
@keyframes dropAndBounce {
  0% { transform: translateY(-200px) scale(0.5); opacity: 0; }
  50% { transform: translateY(0) scale(1.1); opacity: 1; }
  70% { transform: translateY(-20px) scale(1); }
  100% { transform: translateY(0) scale(1); opacity: 0; }
}

@keyframes shake {
  0% { transform: rotate(0deg) scale(1); }
  25% { transform: rotate(-15deg) scale(1.2); }
  50% { transform: rotate(15deg) scale(1.2); }
  75% { transform: rotate(-15deg) scale(1.2); }
  100% { transform: rotate(0deg) scale(1); opacity: 0; }
}

@keyframes spinAndPulsate {
  0% { transform: scale(0.5) rotate(0deg); opacity: 0.5; }
  50% { transform: scale(1.3) rotate(180deg); opacity: 1; }
  100% { transform: scale(1.5) rotate(360deg); opacity: 0; }
}

// 皮球掉落和弹跳动画
@keyframes ballDropAndBounce {
  0% { 
    transform: translateX(-50%) translateY(-300px) scale(0.5); 
    opacity: 0; 
  }
  25% { 
    transform: translateX(-50%) translateY(0) scale(1.3); 
    opacity: 1; 
  }
  35% { 
    transform: translateX(-50%) translateY(-80px) scale(1.1); 
  }
  45% { 
    transform: translateX(-50%) translateY(0) scale(1.2); 
  }
  55% { 
    transform: translateX(-50%) translateY(-60px) scale(1.05); 
  }
  65% { 
    transform: translateX(-50%) translateY(0) scale(1.1); 
  }
  75% { 
    transform: translateX(-50%) translateY(-40px) scale(1); 
  }
  85% { 
    transform: translateX(-50%) translateY(0) scale(1); 
  }
  95% { 
    transform: translateX(-50%) translateY(-20px) scale(1); 
    opacity: 1; 
  }
  100% { 
    transform: translateX(-50%) translateY(0) scale(1); 
    opacity: 0; 
  }
}

// 碗移动到宠物脚下的动画
@keyframes bowlMoveToPet {
  0% { 
    transform: translate(0, 0) scale(0.8); 
    opacity: 0; 
  }
  20% { 
    opacity: 1; 
  }
  50% { 
    transform: translate(300px, 0) scale(1); 
  }
  80% { 
    transform: translate(400px, 0) scale(1.1); 
  }
  90% { 
    transform: translate(450px, 0) scale(1); 
    opacity: 1; 
  }
  100% { 
    transform: translate(450px, 0) scale(1); 
    opacity: 0; 
  }
}

// 饲料掉落动画
@keyframes foodDrop {
  0% { 
    transform: translateY(-150px) scale(0.5); 
    opacity: 0; 
  }
  50% { 
    transform: translateY(180px) scale(1.2); 
    opacity: 1; 
  }
  70% { 
    transform: translateY(170px) scale(1); 
  }
  90% { 
    transform: translateY(180px) scale(1); 
    opacity: 1; 
  }
  100% { 
    transform: translateY(180px) scale(1); 
    opacity: 0; 
  }
}

// 花洒移动动画
@keyframes showerMove {
  0% { 
    transform: translate(-100px, -105px) scale(0.8); 
    opacity: 0; 
  }
  10% { 
    opacity: 1; 
  }
  20% { 
    transform: translate(-50px, -90px) scale(1); 
  }
  30% { 
    transform: translate(0, -90px) scale(1.1); 
  }
  40% { 
    transform: translate(50px, -90px) scale(1); 
  }
  50% { 
    transform: translate(100px, -90px) scale(0.9); 
  }
  60% { 
    transform: translate(50px, -90px) scale(1); 
  }
  70% { 
    transform: translate(0, -90px) scale(1.1); 
  }
  80% { 
    transform: translate(-50px, -90px) scale(1); 
  }
  90% { 
    transform: translate(-100px, -90px) scale(0.8); 
    opacity: 1; 
  }
  100% { 
    transform: translate(-100px, -90px) scale(0.8); 
    opacity: 0; 
  }
}

// 泡沫动画
@keyframes foamBubble {
  0% { 
    transform: scale(0.2); 
    opacity: 0; 
  }
  50% { 
    transform: scale(1.2); 
    opacity: 0.8; 
  }
  100% { 
    transform: scale(1.5); 
    opacity: 0; 
  }
}
/* --------------------------- */
</style>