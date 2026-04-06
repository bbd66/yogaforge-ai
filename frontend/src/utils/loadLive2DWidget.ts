// 动态加载 live2d-widget 脚本（通过 CDN），仅加载一次
export function loadLive2DWidget(cb?: () => void) {
  if (typeof window === 'undefined' || typeof document === 'undefined') return
  const exists = document.querySelector('script[data-live2d-widget]') as HTMLScriptElement | null
  if (exists) {
    // 已经加载过
    cb && cb()
    return
    }
  const script = document.createElement('script')
  script.setAttribute('data-live2d-widget', 'true')
  script.src = 'https://unpkg.com/live2d-widget@3.1.4/lib/L2Dwidget.min.js'
  script.async = true
  script.onload = () => cb && cb()
  script.onerror = () => console.error('Failed to load live2d-widget script')
  document.head.appendChild(script)
}
