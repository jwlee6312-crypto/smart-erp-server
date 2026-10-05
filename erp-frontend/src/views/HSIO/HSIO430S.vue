<!--
	=============================================================
	프로그램명	: 입고상세명세서 (HSIO430S)
	작성일자	: 2025.02.24
	설명        : 창고별/품목별 상세 입고 내역 조회 (Tab키 순항 및 Alt 단축키 지원)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        입고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">입고상세명세서 (HSIO430S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" title="Alt+N: 초기화">초기화(N)</button>
        <button class="btn-erp btn-search" @click="fetchList" title="Alt+F: 조회">조회(F)</button>
        <button class="btn-erp btn-excel" @click="exportExcel">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 25%" />
                <col style="width: 10%" /><col style="width: 25%" />
                <col style="width: 10%" /><col style="width: 30%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required">입고창고</th>
                <td>
                  <select ref="firstFocusRef" v-model="searchData.whcd" class="form-select form-select-sm" tabindex="1">
                      <option value="000">전체</option>
                      <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light required">입고일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <input v-model="fromdt" type="date" class="form-control form-control-sm" style="width: 140px;" tabindex="2" />
                  <span class="px-1 opacity-50">~</span>
                  <input v-model="todt" type="date" class="form-control form-control-sm" style="width: 140px;" tabindex="3" />
                </td>
                <th class="text-center bg-light">품 목</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.itemcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" tabindex="4" readonly />
                    <input v-model="searchData.itemnm" type="text" class="form-control" tabindex="5" placeholder="품목 선택" />
                    <button class="btn btn-outline-secondary px-2" tabindex="6" @click="handleOpenHelp('ITEM')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-list-columns me-2 text-primary"></i>품목 입고 상세 내역</span>
          <span v-if="rowCount" class="badge bg-secondary-subtle text-dark border border-secondary-subtle" style="font-size: 10px;">Total: {{ rowCount }}건</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="tableRef" class="tabulator-instance flex-grow-1" tabindex="7"></div>
        </div>
      </div>

    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'
import { useManualStore } from '@/stores/manualStore'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const route = useRoute()
const { today, firstDay } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()
const manualStore = useManualStore()

const firstFocusRef = ref<HTMLElement | null>(null)
const whOptions = ref<any[]>([])

// [1] 데이터 모델링
const searchData = reactive({
  whcd: '000',
  whnm: '전체',
  fromdt: firstDay.replace(/-/g, ''),
  todt: today.replace(/-/g, ''),
  itemcd: '', itemnm: '', itsize: '', unit: ''
})

const rowCount = ref(0)
const fromdt = computed({ get: () => formatDate(searchData.fromdt), set: (v) => { if (v) searchData.fromdt = v.replace(/-/g, '') } })
const todt = computed({ get: () => formatDate(searchData.todt), set: (v) => { if (v) searchData.todt = v.replace(/-/g, '') } })

const tableRef = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  if (!tableRef.value) return
  grid = new Tabulator(tableRef.value, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "입고일자", field: "ioymd", width: 110, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
      { title: "입고번호", field: "iono_disp", width: 120, hozAlign: "center", cssClass: "fw-bold text-primary", mutatorData: (v, d) => d.ioym && d.iono ? `${d.ioym}-${d.iono}` : v },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: "fw-bold", formatter: (c) => `[${c.getData().itemcd || ''}] ${c.getValue() || ''}` },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "입고창고", field: "whnm", width: 150 },
      { title: "입고량", field: "ioqty", width: 120, hozAlign: "right", formatter: "money", cssClass: "text-primary fw-bold" }
    ],
  });
}

// [3] 비즈니스 로직
async function fetchList() {
  try {
    const res = await api.post('/hsio/HSIO_470S_STR', {
      cmpycd: authStore.cmpycd, whcd: searchData.whcd || '000', fromdt: searchData.fromdt, todt: searchData.todt,
      deptcd: '', custcd: '', itemcd: searchData.itemcd || ''
    })
    grid?.setData(res.data || [])
    rowCount.value = res.data?.length || 0
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const handleOpenHelp = (type: string) => {
  if (type === 'ITEM') {
    openHelp('ITEM', (d) => {
      Object.assign(searchData, { itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unit: d.unit });
    }, { codegbn: 'B' });
  }
}

const fetchWhOptions = async () => {
  try {
    const res = await api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
    whOptions.value = res.data.map((i: any) => ({ whcd: i.code || i.whcd, whnm: i.cdnm || i.whnm }));
  } catch (e) {}
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { whcd: '000', whnm: '전체', fromdt: firstDay.replace(/-/g, ''), todt: today.replace(/-/g, '') })
  grid?.clearData(); rowCount.value = 0;
  nextTick(() => firstFocusRef.value?.focus())
}

const exportExcel = () => grid?.download("xlsx", `입고상세명세_${searchData.todt}.xlsx`)
const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;

/** 🚀 [HSOD100U 표준 키보드 단축키 핸들러 연동] */
function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'f') { e.preventDefault(); fetchList() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSIO430S') }
  }
}

onMounted(async () => {
  window.addEventListener('keydown', handleGlobalShortcuts)
  await fetchWhOptions()

  // 쿼리 파라미터로 전달된 경우 수신
  if (route.query.itemcd) {
    searchData.whcd = String(route.query.whcd || '000')
    searchData.itemcd = String(route.query.itemcd || '')
    searchData.itemnm = String(route.query.itemnm || '')
  }

  nextTick(() => { initGrids(); fetchList(); firstFocusRef.value?.focus() })
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
</style>
