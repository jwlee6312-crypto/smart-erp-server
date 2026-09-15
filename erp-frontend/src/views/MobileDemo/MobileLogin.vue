<template>
  <div class="mobile-login-view p-4 d-flex flex-column align-items-center justify-content-center h-100">
    <div class="mb-5 text-center">
      <h2 class="fw-bold text-primary">SmartCore</h2>
      <div class="text-muted small">모바일 데모 시스템</div>
    </div>

    <div class="w-100 d-flex flex-column gap-3">
      <div class="form-floating">
        <input v-model="form.cmpycd" type="text" class="form-control" id="cmpycd" placeholder="회사코드">
        <label for="cmpycd">회사코드</label>
      </div>
      <div class="form-floating">
        <input v-model="form.userid" type="text" class="form-control" id="userid" placeholder="아이디">
        <label for="userid">아이디</label>
      </div>
      <div class="form-floating">
        <input v-model="form.passwd" type="password" class="form-control" id="passwd" placeholder="비밀번호" @keyup.enter="handleLogin">
        <label for="passwd">비밀번호</label>
      </div>

      <button class="btn btn-primary py-3 rounded-pill fw-bold shadow-sm mt-2" @click="handleLogin">
        로그인
      </button>

      <div class="text-center mt-3 text-muted small">
        비밀번호 분실 시 관리자에게 문의하세요.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDemoAuthStore } from '@/stores/demoAuthStore'

const router = useRouter()
const demoAuthStore = useDemoAuthStore()

const form = reactive({
  cmpycd: 'coit',
  userid: '',
  passwd: ''
})

const handleLogin = async () => {
  if (!form.userid || !form.passwd) return alert('아이디와 비밀번호를 입력하세요.')

  try {
    const success = await demoAuthStore.login(form.cmpycd, form.userid, form.passwd)
    if (success) {
      router.push({ name: 'MobileDemoMain' })
    }
  } catch (e: any) {
    alert(e.message || '로그인 실패')
  }
}
</script>

<style scoped>
.mobile-login-view { background: #fff; }
.form-floating > .form-control { border-radius: 12px; }
</style>
