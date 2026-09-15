<template>
  <div class="mobile-main-view d-flex flex-column h-100">
    <header class="bg-primary text-white p-3 d-flex justify-content-between align-items-center shadow-sm">
      <span class="fw-bold">업무 메인</span>
      <i class="bi bi-person-circle fs-4"></i>
    </header>

    <div class="flex-grow-1 p-3 overflow-auto">
      <div class="welcome-box mb-4">
        <h5 class="fw-bold">{{ demoAuthStore.usernm }} 님</h5>
        <div class="text-muted small">오늘의 주요 업무입니다.</div>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
      </div>

      <div v-else class="menu-grid">
        <div v-for="menu in menus" :key="menu.codecd" class="menu-item" @click="goProgram(menu)">
          <div class="menu-icon bg-light rounded-4 mb-2 d-flex align-items-center justify-content-center">
            <i class="bi bi-app-indicator fs-3 text-primary"></i>
          </div>
          <div class="menu-name small fw-bold text-center">{{ menu.codenm }}</div>
        </div>
      </div>
    </div>

    <footer class="bg-white border-top p-2 d-flex justify-content-around">
      <div class="text-primary text-center"><i class="bi bi-house-door-fill d-block"></i>홈</div>
      <div class="text-muted text-center"><i class="bi bi-search d-block"></i>검색</div>
      <div class="text-muted text-center"><i class="bi bi-gear d-block"></i>설정</div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDemoAuthStore } from '@/stores/demoAuthStore'
import { api } from '@/utils/axios'

const router = useRouter()
const demoAuthStore = useDemoAuthStore()
const menus = ref<any[]>([])
const loading = ref(false)

const fetchMobileMenus = async () => {
  loading.value = true
  try {
    const res = await api.get('/comm/top-menus-mobile')
    menus.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goProgram = (menu: any) => {
  // 데모에서는 재고조회(120/HSST200S 관련)만 구현
  if (menu.codenm.includes('재고') || menu.codenm.includes('창고')) {
    router.push({ name: 'MobileDemoInventory' })
  } else {
    alert('데모 준비 중인 기능입니다.')
  }
}

onMounted(fetchMobileMenus)
</script>

<style scoped>
.menu-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.menu-item { cursor: pointer; }
.menu-icon { aspect-ratio: 1 / 1; transition: transform 0.2s; border: 1px solid #eee; }
.menu-item:active .menu-icon { transform: scale(0.9); background: #e9ecef !important; }
.menu-name { color: #444; font-size: 11px; }
footer i { font-size: 20px; }
</style>
