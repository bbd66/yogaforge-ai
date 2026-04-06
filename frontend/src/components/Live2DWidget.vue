<template>
  <!-- 容器：用于定位和占位，canvas 由 widget 注入到 body，我们把 canvas 移入此容器并居中 -->
  <div ref="holder" class="live2d-holder" @click="handleClick"></div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, toRefs, ref as vueRef } from 'vue'
import { loadLive2DWidget } from '@/utils/loadLive2DWidget'

interface Props {
  jsonPath: string
  width?: number
  height?: number
  // 交互是否启用（移动端建议保持启用，可点击）
  interactive?: boolean
  // 水平/垂直偏移（像素），正值向右/向下
  hOffset?: number
  vOffset?: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'click'): void
  (e: 'ready'): void
  (e: 'error', err: unknown): void
}>()

const { jsonPath, width, height, interactive, hOffset, vOffset } = toRefs(props)
const holder = vueRef<HTMLElement | null>(null)

function initWidget() {
  try {
    if (!window.L2Dwidget) return
    // 清理旧的画布，避免重复
    cleanupCanvas()
    window.L2Dwidget.init({
      model: { jsonPath: jsonPath!.value },
      display: {
        position: 'right', // 位置由 CSS 覆盖，这里无关紧要
        width: (width?.value ?? 320),
        height: (height?.value ?? 320),
        hOffset: 0,
        vOffset: 0
      },
      mobile: { show: true, scale: 1 },
      react: { opacityDefault: 1, opacityOnHover: 1 }
    })
  emit('ready')
  // 将默认注入到 body 的 #live2dcanvas 移入 holder，并在 holder 内居中显示
  moveCanvasIntoHolder()
  } catch (e) {
    console.error('live2d init error', e)
    emit('error', e)
  }
}

function moveCanvasIntoHolder() {
  const el = typeof window !== 'undefined' ? document.getElementById('live2dcanvas') as HTMLCanvasElement | null : null
  if (!holder.value) return

  // 如果没有找到 id 为 live2dcanvas 的元素，尝试更宽泛地查找最近注入的 canvas
  let canvas = el
  if (!canvas) {
    // 优先查找包含 live2d 关键字的 canvas
    canvas = document.querySelector('canvas[id*=live2d], canvas[class*=live2d]') as HTMLCanvasElement | null
  }
  if (!canvas) {
    // 兜底：取页面中第一个 canvas（谨慎）
    canvas = document.querySelector('canvas') as HTMLCanvasElement | null
  }
  if (!canvas) return

  // 若 canvas 有父容器且父容器带有定位（通常 widget 使用 fixed 使其右下），清理父容器的定位样式
  const parent = canvas.parentElement
  if (parent) {
    // 移除 widget 施加的固定定位样式（尽量覆盖）
    parent.style.position = 'static'
    parent.style.right = 'auto'
    parent.style.bottom = 'auto'
    parent.style.left = 'auto'
    parent.style.top = 'auto'
    parent.style.transform = 'none'
    parent.style.width = 'auto'
    parent.style.height = 'auto'
  }

  // 如果 canvas 在其他父元素下，先从中移除
  if (canvas.parentElement) canvas.parentElement.removeChild(canvas)

  // 把 canvas 插入到 holder 中，并设置样式让其在 holder 中居中且铺满
  holder.value.appendChild(canvas)
  canvas.style.position = 'absolute'
  const h = (hOffset?.value ?? 0)
  const v = (vOffset?.value ?? 0)
  canvas.style.left = `calc(50% + ${h}px)`
  canvas.style.top = `calc(50% + ${v}px)`
  canvas.style.transform = 'translate(-50%, -50%)'
  canvas.style.width = '100%'
  canvas.style.height = '100%'
  canvas.style.pointerEvents = interactive?.value === false ? 'none' : 'auto'
  canvas.style.zIndex = '20'
}

// MutationObserver：当 widget 异步注入 DOM 时，确保能捕获并迁移画布
let mo: MutationObserver | null = null
function observeForCanvas(timeout = 3000) {
  if (typeof window === 'undefined') return
  const start = Date.now()
  if (mo) mo.disconnect()
  mo = new MutationObserver(() => {
    moveCanvasIntoHolder()
    // 如果已找到并移动，则可以断开观察（通过检查 holder 下是否有 canvas）
    if (holder.value && holder.value.querySelector('canvas')) {
      mo && mo.disconnect()
      mo = null
    }
    if (Date.now() - start > timeout) {
      mo && mo.disconnect()
      mo = null
    }
  })
  mo.observe(document.body, { childList: true, subtree: true })
}

function cleanupCanvas() {
  const el = typeof window !== 'undefined' ? document.getElementById('live2dcanvas') as HTMLElement | null : null
  if (!el) return
  // 如果 canvas 在 holder 内，直接移除；否则尝试从其父节点移除
  if (el.parentElement) {
    el.parentElement.removeChild(el)
  } else {
    el.remove()
  }
}

function handleClick() {
  emit('click')
}

onMounted(() => {
  // 先开始观察 body，以便捕获 live2d widget 注入的 DOM（异步）
  observeForCanvas()
  loadLive2DWidget(() => {
    initWidget()
    // 尝试再次迁移（防止 widget 在 init 后立即重新定位）
    moveCanvasIntoHolder()
  })
})

onBeforeUnmount(() => {
  cleanupCanvas()
})

watch([jsonPath, width, height], () => {
  // 如果 props 发生变化，重新初始化
  if (window.L2Dwidget) initWidget()
})
</script>

<style scoped>
.live2d-holder {
  position: relative;
  width: 320px;
  height: 320px;
  margin: 80px auto 0; /* 与原先宠物图片位置相近 */
}
</style>
