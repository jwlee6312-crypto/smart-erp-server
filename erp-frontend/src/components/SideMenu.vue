<template>
  <div class="side-bar shadow-sm" :class="{ 'is-collapsed': isCollapsed }">
    <!-- 👤 유저 프로필 카드 (사진 90px 및 세션 정보 강조) -->
    <div v-if="!isCollapsed" class="profile-card">
      <div class="user-details text-center">
        <div class="avatar-area mb-2">
          <div class="avatar-placeholder cursor-pointer" @click="goToProfile" title="개인정보 관리로 이동">
            <img v-if="profileImageSrc" :src="profileImageSrc" class="profile-img" alt="Profile" />
            <i v-else class="bi bi-person-fill"></i>
          </div>
        </div>
        <!-- 💡 세션 유지 확인을 위해 정보를 진하게 표시 -->
        <div class="user-info-text">
          <div class="user-name-info fw-bolder text-dark">
            {{ authStore.usernm }}
            <span class="badge bg-primary text-white ms-1" style="font-size: 11px;">내선:{{ authStore.inner_no || '-' }}</span>
          </div>
          <div v-if="authStore.email" class="user-email-text fw-bold text-secondary mt-1">
            {{ authStore.email }}
          </div>
        </div>
      </div>
    </div>

    <!-- 📋 메뉴 리스트 (기존 기능 유지) -->
    <div id="accordionMenu" class="menu-list">
      <div v-for="group in groupedItems" :key="group.grpcd" class="w-100">
        <a
          class="nav-link group-title"
          :class="{ 'collapsed': !isGroupOpen(group.grpcd) }"
          href="javascript:void(0)"
          @click="toggleGroup(group.grpcd)"
        >
          <i class="bi bi-folder2-open me-2 text-primary"></i>
          <span v-if="!isCollapsed">{{ group.grpnm }}</span>
        </a>

        <div class="collapse-content" v-show="isGroupOpen(group.grpcd) && !isCollapsed">
          <nav class="nav d-flex flex-column">
            <a
              v-for="item in group.items"
              :key="item.pgmid"
              class="sb-nav-link"
              :class="{ 'is-active': tabStore.activeTab?.pgmId === item.pgmid }"
              href="javascript:void(0)"
              @click="goPage(item.pgmid, item.pgmnm, item.grpcd)"
            >
              <i class="bi bi-chevron-right sub-icon"></i>
              <span>{{ item.pgmnm }}</span>
            </a>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useMenuStore } from '@/stores/menuStore'
import { useTabStore } from '@/stores/tabStore'
import { useAuthStore } from '@/stores/authStore'
import { addDynamicRoute } from '@/router/dynamicRoute'

const props = defineProps({
  isCollapsed: Boolean
})

const authStore = useAuthStore()
const menuStore = useMenuStore()
const tabStore = useTabStore()
const openGroupId = ref<string | null>(null)

// 🚀 프로필 이미지 경로 계산 (리눅스 대소문자 무결성 보장)
const profileImageSrc = computed(() => {
  if (authStore.photo_path) {
    const path = authStore.photo_path.trim()
    if (path.startsWith('http') || path.startsWith('data:')) return path

    // 💡 [최종 보정] 조회 경로를 /api/storage/ 로 명시적으로 통일하고 대소문자 문제 해결
    const cmpycd = (authStore.cmpycd || 'COIT').toUpperCase()
    return `/api/storage/${cmpycd}/profile/${path}`
  }
  return ''
})

const isGroupOpen = (grpcd: string) => openGroupId.value === grpcd
const toggleGroup = (grpcd: string) => {
  openGroupId.value = openGroupId.value === grpcd ? null : grpcd
}

const groupedItems = computed(() => menuStore.groupedSidebarItems)

function goToProfile() {
  const pgmId = 'HABA910U';
  const pgmNm = '개인정보 관리';
  const grpCd = '900';
  addDynamicRoute(pgmId, pgmNm, grpCd);
  tabStore.addTab({ pgmId: pgmId, pgmNm: pgmNm, path: `/${pgmId}` });
}

function goPage(pgmid: string, pgmnm: string, grpcd: string) {
  addDynamicRoute(pgmid, pgmnm, grpcd)
  tabStore.addTab({ pgmId: pgmid, pgmNm: pgmnm, path: `/${pgmid}` })
}
</script>

<style scoped>
.side-bar { background-color: #fff; border-right: 1px solid #dcdfe6; display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.profile-card { padding: 20px 10px; border-bottom: 1px solid #ebeef5; flex-shrink: 0; background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%); }

/* 💡 사진 및 아이콘 크기 원복 (상담원님 요청 반영) */
.avatar-placeholder {
  width: 70px;
  height: 70px;
  background-color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38px; /* 🚀 아이콘 크기 원복 */
  color: #005a9f;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid #fff;
  overflow: hidden;
}

.profile-img { width: 100%; height: 100%; object-fit: cover; }
.user-name-info { font-weight: 800; color: #1a202c; font-size: 15px; margin-top: 12px; line-height: 1.2; }
.user-email-text { font-size: 11px; color: #718096; margin-top: 4px; word-break: break-all; }

.menu-list { flex: 1; overflow-y: auto; }
.group-title { display: flex; align-items: center; padding: 9px 15px; font-size: 13px; font-weight: 700; color: #2d3748; text-decoration: none; }
.sb-nav-link { padding: 6px 15px 6px 35px; font-size: 12.5px; color: #4a5568; text-decoration: none; display: flex; align-items: center; border-left: 3px solid transparent; }
.sb-nav-link:hover { background-color: #f7fafc; color: #3182ce; }
.sb-nav-link.is-active { background-color: #ebf8ff; color: #2b6cb0; font-weight: bold; border-left-color: #3182ce; }
.sub-icon { font-size: 8px; margin-right: 10px; opacity: 0.5; }
</style>
