<template>
  <div class="workout-page">
    <!-- 头部 -->
    <header class="header">
      <h1 class="subtle-header">运动连接</h1>
      <div @click="scanDevices" class="scan-btn">
        <img :src="scanDeviceIcon" alt="扫描设备" class="scan-icon" />
      </div>
    </header>

    <!-- 连接状态 -->
    <div v-if="connectedDevice" class="connected-status">
      <div class="status-icon">✅</div>
      <div class="status-info">
        <div class="device-name">{{ connectedDevice.name }}</div>
        <div class="device-type">{{ connectedDevice.type }}</div>
      </div>
      <div class="battery-info">
        <span>🔋 {{ connectedDevice.battery }}%</span>
      </div>
    </div>

    <!-- 蓝牙设备列表 -->
    <div class="devices-section">
      <div class="section-header">
        <h2>可用设备</h2>
        <span class="device-count">{{ devices.length }} 台设备</span>
      </div>
      <div class="device-list">
        <div 
          v-for="device in devices" 
          :key="device.id"
          class="device-item"
          :class="{ connected: device.connected }"
        >
          <div class="device-icon">
            <img :src="device.icon" :alt="device.type" class="device-image" />
          </div>
          <div class="device-info">
            <div class="device-name">{{ device.name }}</div>
            <div class="device-type">{{ device.type }}</div>
          </div>
          <button 
            @click="toggleConnection(device)"
            class="connect-btn"
            :class="{ connected: device.connected }"
          >
            {{ device.connected ? '断开' : '连接' }}
          </button>
        </div>
        <div v-if="devices.length === 0" class="empty-state">
          <div class="empty-icon">📱</div>
          <p>未发现设备</p>
          <p class="empty-hint">请确保蓝牙已开启并点击扫描</p>
        </div>
      </div>
    </div>
    
    <!-- 摄像头权限确认弹窗 -->
    <div v-if="showCameraConfirm" class="camera-confirm-modal">
      <div class="modal-content">
        <h3>摄像头权限请求</h3>
        <p>为了使用瑜伽垫的视觉识别功能，需要打开摄像头。是否允许访问摄像头？</p>
        <div class="modal-actions">
          <button @click="cancelCameraAccess" class="cancel-btn">取消</button>
          <button @click="confirmCameraAccess" class="confirm-btn">确认</button>
        </div>
      </div>
    </div>
    
    <!-- 摄像头预览 -->
    <div v-if="showCameraPreview" class="camera-preview-modal">
      <div class="preview-content">
        <h3>瑜伽识别预览</h3>
        <div class="camera-controls">
          <button @click="switchCamera" class="switch-camera-btn">
            切换摄像头 ({{ currentCamera === 'user' ? '前置' : '后置' }})
          </button>
        </div>

        <!-- 实时摄像头画面 + 叠加骨架层 -->
        <div class="camera-layer-wrapper">
          <video 
            ref="videoElement" 
            autoplay 
            playsinline 
            class="camera-video"
          ></video>
          <canvas ref="overlayCanvas" class="overlay-canvas"></canvas>
        </div>

        <!-- 隐藏画布用于截取视频帧 -->
        <canvas ref="captureCanvas" class="hidden-canvas"></canvas>

        <div class="preview-actions">
          <button @click="closeCameraPreview" class="close-btn">结束训练</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, nextTick, computed } from 'vue'
import api from '@/api'
import type { BluetoothDevice } from '@/types'
import jumpRopeIcon from '@/图标/跳绳.png?url'
import yogaMatIcon from '@/图标/瑜伽垫.png?url'
import scanDeviceIcon from '@/图标/扫描设备.png?url'

const scanning = ref(false)
const connectedDevice = ref<BluetoothDevice | null>(null)
const showCameraConfirm = ref(false)
const showCameraPreview = ref(false)
const videoElement = ref<HTMLVideoElement | null>(null)
const captureCanvas = ref<HTMLCanvasElement | null>(null)
const overlayCanvas = ref<HTMLCanvasElement | null>(null)
const stream = ref<MediaStream | null>(null)
const isMobile = ref(false)
const currentCamera = ref<'user' | 'environment'>('environment')
const annotatedImage = ref<string | null>(null)
let isSendingFrame = false

const isWorkingOut = ref(false)
const startTimestamp = ref<number | null>(null)
const elapsedSeconds = ref(0)
let timerId: number | null = null
let renderTimerId: number | null = null

const calories = computed(() => Math.floor((elapsedSeconds.value / 60) * 5))

const devices = ref<BluetoothDevice[]>([
  { id: '1', name: '智能跳绳', type: '跳绳', connected: false, battery: 85, icon: jumpRopeIcon },
  { id: '2', name: '动作视觉识别瑜伽垫', type: '智能瑜伽垫', connected: false, battery: 92, icon: yogaMatIcon }
])

const startWorkout = () => {
  if (isWorkingOut.value) return
  isWorkingOut.value = true
  startTimestamp.value = Date.now()
  elapsedSeconds.value = 0

  timerId = window.setInterval(() => {
    if (startTimestamp.value) {
      elapsedSeconds.value = Math.floor((Date.now() - startTimestamp.value) / 1000)
    }
  }, 1000)
}

const stopWorkout = async () => {
  if (!isWorkingOut.value || !startTimestamp.value) return
  isWorkingOut.value = false
  if (timerId !== null) {
    clearInterval(timerId)
    timerId = null
  }

  const duration = elapsedSeconds.value
  const cal = calories.value

  try {
    await api.post('/yoga/session/metrics-manual', {
      durationSeconds: duration,
      calories: cal
    })
    alert(`本次运动 ${Math.floor(duration / 60)} 分 ${duration % 60} 秒，消耗约 ${cal} 大卡，已记录`)
  } catch (e) {
    console.error('上报运动数据失败', e)
    alert('上报运动数据失败，请稍后重试')
  }
}

const scanDevices = async () => {
  scanning.value = true
  setTimeout(() => {
    scanning.value = false
  }, 2000)
}

const toggleConnection = (device: BluetoothDevice) => {
  device.connected = !device.connected
  if (device.connected) {
    connectedDevice.value = device
    if (device.type === '智能瑜伽垫') {
      showCameraConfirm.value = true
    }
    startWorkout()
  } else if (connectedDevice.value?.id === device.id) {
    connectedDevice.value = null
    stopWorkout()
  }
}

const cancelCameraAccess = () => {
  showCameraConfirm.value = false
  if (connectedDevice.value?.type === '智能瑜伽垫') {
    const yogaMat = devices.value.find(d => d.type === '智能瑜伽垫')
    if (yogaMat) {
      yogaMat.connected = false
      connectedDevice.value = null
    }
  }
}

const confirmCameraAccess = async () => {
  showCameraConfirm.value = false
  try {
    isMobile.value = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
    stream.value = await navigator.mediaDevices.getUserMedia({ 
      video: { facingMode: currentCamera.value } 
    })
    showCameraPreview.value = true
    
    await nextTick()
    if (videoElement.value) {
      videoElement.value.srcObject = stream.value
    }

    if (renderTimerId !== null) {
      clearInterval(renderTimerId)
    }
    renderTimerId = window.setInterval(() => {
      sendFrameToRender()
    }, 350)
  } catch (error) {
    console.error('无法访问摄像头:', error)
    alert('无法访问摄像头，请检查权限设置')
    const yogaMat = devices.value.find(d => d.type === '智能瑜伽垫')
    if (yogaMat) {
      yogaMat.connected = false
      connectedDevice.value = null
    }
  }
}

const switchCamera = async () => {
  if (!stream.value) return
  
  try {
    stream.value.getTracks().forEach(track => track.stop())
    currentCamera.value = currentCamera.value === 'user' ? 'environment' : 'user'
    stream.value = await navigator.mediaDevices.getUserMedia({ 
      video: { facingMode: currentCamera.value } 
    })
    if (videoElement.value) {
      videoElement.value.srcObject = stream.value
    }
  } catch (error) {
    console.error('切换摄像头失败:', error)
    alert('切换摄像头失败')
    currentCamera.value = currentCamera.value === 'user' ? 'environment' : 'user'
  }
}

const closeCameraPreview = () => {
  showCameraPreview.value = false
  if (renderTimerId !== null) {
    clearInterval(renderTimerId)
    renderTimerId = null
  }
  annotatedImage.value = null
  if (stream.value) {
    stream.value.getTracks().forEach(track => track.stop())
    stream.value = null
  }
  if (videoElement.value) {
    videoElement.value.srcObject = null
  }
  if (overlayCanvas.value) {
    const ctx = overlayCanvas.value.getContext('2d')
    if (ctx) {
      ctx.clearRect(0, 0, overlayCanvas.value.width, overlayCanvas.value.height)
    }
  }
}

type PoseOverlayInfo = {
  templatePose?: string | null
  qualityScore?: number | null
  suggestions?: string[] | null
  recognized?: boolean
}

// 在覆盖层画骨架 + 文字
const drawSkeletonOnOverlay = (
  keypoints: number[][],
  width: number,
  height: number,
  info?: PoseOverlayInfo
) => {
  if (!overlayCanvas.value) return
  const canvas = overlayCanvas.value
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.lineWidth = 2
  ctx.strokeStyle = '#ff4d4f'
  ctx.fillStyle = '#52c41a'

  const edges: [number, number][] = [
    [0, 1], [0, 2], [1, 3], [2, 4], [3, 5], [4, 6],
    [5, 7], [6, 8], [7, 9], [8, 10], [5, 11], [6, 12],
    [11, 12], [11, 13], [12, 14], [13, 15], [14, 16]
  ]

  const threshold = 0.3

  for (const pt of keypoints as any) {
    const [x, y, score] = pt
    if (score < threshold) continue
    ctx.beginPath()
    ctx.arc(x, y, 4, 0, Math.PI * 2)
    ctx.fill()
  }

  edges.forEach(([i, j]) => {
    const p1 = keypoints[i]
    const p2 = keypoints[j]
    if (!p1 || !p2) return
    const [, , s1] = p1
    const [, , s2] = p2
    if (s1 < threshold || s2 < threshold) return
    ctx.beginPath()
    ctx.moveTo(p1[0], p1[1])
    ctx.lineTo(p2[0], p2[1])
    ctx.stroke()
  })

  if (info) {
    const { templatePose, qualityScore, suggestions, recognized } = info
    ctx.font = '16px sans-serif'
    ctx.textBaseline = 'top'
    ctx.fillStyle = '#ffffff'
    const baseX = 10
    let baseY = 10

    const title = recognized && templatePose
      ? `体式: ${templatePose}`
      : '体式: 未识别'
    ctx.fillText(title, baseX, baseY)
    baseY += 22

    if (typeof qualityScore === 'number' && recognized) {
      ctx.fillText(`质量: ${Math.round(qualityScore)}/100`, baseX, baseY)
      baseY += 22
    }

    if (Array.isArray(suggestions) && suggestions.length > 0) {
      suggestions.slice(0, 3).forEach(s => {
        ctx.fillText(s, baseX, baseY)
        baseY += 20
      })
    } else {
      ctx.fillText('提示: 请保持人像完整且姿势明显', baseX, baseY)
    }
  }
}

// 将当前视频帧发送给 Python /pose/evaluate
const sendFrameToRender = async () => {
  if (!videoElement.value || !captureCanvas.value) return
  if (isSendingFrame) return
  isSendingFrame = true

  const video = videoElement.value
  const canvas = captureCanvas.value
  let width = video.videoWidth
  let height = video.videoHeight

  if (!width || !height) {
    isSendingFrame = false
    return
  }

  const maxWidth = 480
  if (width > maxWidth) {
    const scale = maxWidth / width
    width = maxWidth
    height = Math.round(height * scale)
  }

  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    isSendingFrame = false
    return
  }

  ctx.drawImage(video, 0, 0, width, height)
  const dataUrl = canvas.toDataURL('image/jpeg', 0.4)
  const base64 = dataUrl.split(',')[1] || dataUrl

  try {
    const resp = await fetch('http://localhost:5000/pose/evaluate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ image: base64, needAngles: false })
    })

    if (!resp.ok) {
      console.error('pose/evaluate error', resp.status)
      return
    }

    const json = await resp.json()
    if (json && Array.isArray(json.keypoints) && json.keypoints.length > 0) {
      drawSkeletonOnOverlay(
        json.keypoints,
        width,
        height,
        {
          templatePose: json.templatePose,
          qualityScore: json.qualityScore,
          suggestions: json.suggestions,
          recognized: json.recognized
        }
      )
    } else if (overlayCanvas.value) {
      const octx = overlayCanvas.value.getContext('2d')
      if (octx) {
        octx.clearRect(0, 0, overlayCanvas.value.width, overlayCanvas.value.height)
      }
    }
  } catch (err) {
    console.error('调用 pose/evaluate 失败', err)
  } finally {
    isSendingFrame = false
  }
}

onBeforeUnmount(() => {
  if (stream.value) {
    stream.value.getTracks().forEach(track => stopTrackSafe(track))
  }
  if (timerId !== null) {
    clearInterval(timerId)
  }
  if (renderTimerId !== null) {
    clearInterval(renderTimerId)
  }
})

const stopTrackSafe = (track: MediaStreamTrack) => {
  try {
    track.stop()
  } catch {
    // ignore
  }
}
</script>

<style scoped lang="scss">
.workout-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
  
  body.dark-theme & {
    background: #1a1a1a;
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h1 {
    color: #333;
    font-size: 24px;
    font-weight: bold;
    
    body.dark-theme & {
      color: #f0f0f0;
    }
  }
  
  .subtle-header {
    color: #999;
    font-size: 14px;
    font-weight: normal;
    text-align: left;
    flex: 1;
    margin: 0;
    padding: 0;
    
    body.dark-theme & {
      color: #bbb;
    }
  }
  
  .scan-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    
    .scan-icon {
      width: 100px;
      height: 30px;
      object-fit: contain;
    }
    
    &:active {
      transform: scale(0.95);
    }
  }
}

.connected-status {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #91B3F0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  color: white;
  
  body.dark-theme & {
    background: #333;
  }
  
  .status-icon {
    font-size: 32px;
  }
  
  .status-info {
    flex: 1;
    
    .device-name {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 4px;
      
      body.dark-theme & {
        color: #f0f0f0;
      }
    }
    
    .device-type {
      font-size: 12px;
      opacity: 0.9;
      
      body.dark-theme & {
        color: #bbb;
      }
    }
  }
  
  .battery-info {
    font-size: 14px;
    
    body.dark-theme & {
      color: #f0f0f0;
    }
  }
}

.devices-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  body.dark-theme & {
    background: #444;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h2 {
    font-size: 18px;
    color: #333;
    font-weight: bold;
    
    body.dark-theme & {
      color: #f0f0f0;
    }
  }
  
  .device-count {
    font-size: 14px;
    color: #999;
    
    body.dark-theme & {
      color: #bbb;
    }
  }
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .device-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #f9f9f9;
    border-radius: 8px;
    transition: all 0.3s;
    
    body.dark-theme & {
      background: #333;
    }
    
    &.connected {
      background-image: url('@/图标/队伍卡片背景.png');
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;
      border: 1px solid #8FAADC;
      
      body.dark-theme & {
        background-image: url('@/图标/队伍卡片背景.png');
        background-size: cover;
        background-position: center;
        background-repeat: no-repeat;
      }
    }
    
    .device-icon {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .device-image {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
      }
    }
    
    .device-info {
      flex: 1;
      
      .device-name {
        font-size: 16px;
        color: #333;
        font-weight: 500;
        margin-bottom: 4px;
        
        body.dark-theme & {
          color: #f0f0f0;
        }
      }
      
      .device-type {
        font-size: 12px;
        color: #999;
        
        body.dark-theme & {
          color: #bbb;
        }
      }
    }
    
    .connect-btn {
      padding: 6px 16px;
      border: 1px solid #8FAADC;
      border-radius: 16px;
      background: white;
      color: #8FAADC;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;
      
      body.dark-theme & {
        background: #333;
        border-color: #F08713;
        color: #F08713;
      }
      
      &.connected {
        background: #8FAADC;
        color: white;
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    
    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
      
      body.dark-theme & {
        color: #f0f0f0;
      }
    }
    
    p {
      color: #999;
      font-size: 14px;
      margin-bottom: 8px;
      
      body.dark-theme & {
        color: #bbb;
      }
      
      &.empty-hint {
        font-size: 12px;
      }
    }
  }
}

.camera-confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  
  .modal-content {
    background: white;
    border-radius: 12px;
    padding: 24px;
    width: 90%;
    max-width: 400px;
    text-align: center;
    
    body.dark-theme & {
      background: #333;
    }
    
    h3 {
      color: #333;
      margin-bottom: 16px;
      
      body.dark-theme & {
        color: #f0f0f0;
      }
    }
    
    p {
      color: #666;
      margin-bottom: 24px;
      line-height: 1.5;
      
      body.dark-theme & {
        color: #ccc;
      }
    }
    
    .modal-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      
      button {
        padding: 10px 24px;
        border-radius: 8px;
        font-size: 16px;
        cursor: pointer;
        border: none;
        transition: all 0.3s;
      }
      
      .cancel-btn {
        background: #f0f0f0;
        color: #666;
        
        body.dark-theme & {
          background: #444;
          color: #ccc;
        }
        
        &:hover {
          background: #e0e0e0;
          
          body.dark-theme & {
            background: #555;
          }
        }
      }
      
      .confirm-btn {
        background: #8FAADC;
        color: white;
        
        body.dark-theme & {
          background: #F08713;
        }
        
        &:hover {
          background: #7d9bc9;
          
          body.dark-theme & {
            background: #e07a0f;
          }
        }
      }
    }
  }
}

.camera-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  
  .preview-content {
    width: 90%;
    max-width: 600px;
    text-align: center;
    
    h3 {
      color: white;
      margin-bottom: 16px;
    }
    
    .camera-controls {
      margin-bottom: 10px;
      
      .switch-camera-btn {
        padding: 8px 16px;
        background: #8FAADC;
        color: white;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        
        &:hover {
          background: #7d9bc9;
        }
      }
    }
    
    .camera-video {
      width: 100%;
      max-height: 70vh;
      background: black;
      border-radius: 8px;
      margin-bottom: 20px;
      
      &.full-screen {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        max-height: none;
        border-radius: 0;
        object-fit: cover;
        z-index: 1001;
      }
    }
    
    .preview-actions {
      .close-btn {
        padding: 12px 32px;
        background: #ff4757;
        color: white;
        border: none;
        border-radius: 8px;
        font-size: 16px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #ff2e43;
        }
      }
    }
  }
}

.hidden-canvas {
  display: none;
}

.hidden-video {
  display: none;
}

.camera-layer-wrapper {
  position: relative;
  width: 100%;
  max-height: 70vh;
}

.overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
</style>