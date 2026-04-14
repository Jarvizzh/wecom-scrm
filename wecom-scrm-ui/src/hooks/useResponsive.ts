import { ref, onMounted, onUnmounted } from 'vue'

export function useResponsive() {
  const isMobile = ref(false)
  const screenWidth = ref(window.innerWidth)

  const checkMobile = () => {
    screenWidth.value = window.innerWidth
    isMobile.value = window.innerWidth < 768
  }

  onMounted(() => {
    checkMobile()
    window.addEventListener('resize', checkMobile)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', checkMobile)
  })

  return {
    isMobile,
    screenWidth
  }
}
