<!--
	=============================================================
	프로그램명	: 외주가공생산실적 (HPIO350U)
	작성일자	: 2025.03.22
	설명        : 외주 생산 실적 및 투입 자재 관리 (HSOD100U 표준 적용본)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 🚀 전용 팝업 라이브러리 -->
  <ProdCustHelp
    v-model:visible="popVisible.cust"
    @confirm="onCustConfirm"
    @close="restoreFocus"
  />
  <ProdItemHelp
    v-model:visible="popVisible.prod"
    @confirm="onProdConfirm"
    @close="restoreFocus"
  />
  <!-- 🚀 하단 투입자재 전용 팝업 -->
  <RawItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />
  <!-- 이전공정 선택용 -->
  <HelpBase
    v-model:visible="popVisible.prog"
    title="출고공정 선택"
    :columns="progPopupColumns"
    @search="fetchProgPopupData"
    @confirm="onProgConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
        생산관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        실적관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주가공생산실적 (HPIO350U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="fetchOrderList" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="saveAll" tabindex="-1">저장(S)</button>
      </div>
    </div>

    <!-- [2] 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 상단 조회 필터 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" />
              <col style="width: 20%" />
              <col style="width: 10%" />
              <col style="width: 25%" />
              <col style="width: 10%" />
              <col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required small">생산라인</th>
                <td>
                  <select v-model="searchForm.linecd" class="form-select form-select-sm" tabindex="101" @change="onLineChange">
                    <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">[{{ opt.linecd }}] {{ opt.linenm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light required small border-start">지시일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="fromdt_f"
                    v-model:todt="todt_f"
                    :tabindex="102"
                  />
                </td>
                <th class="text-center bg-light required small border-start">외주가공처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchForm.custnm" type="text" class="form-control fw-bold text-primary" readonly tabindex="-1" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')" tabindex="103"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex flex-row flex-grow-1 overflow-hidden gap-2" style="min-height: 0;">
        <!-- 좌측: 지시 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 380px; min-width: 380px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-list-check me-2 text-primary"></i>실적 등록 대상 (지시)</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="orderTableRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 상세 정보 및 실적/자재 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 실적 공통 정보 -->
          <div class="card border shadow-sm flex-shrink-0">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense" width="100%">
                <colgroup>
                    <col style="width: 15%" /><col style="width: 35%" />
                    <col style="width: 15%" /><col style="width: 35%" />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="text-center bg-light required small">입고일자</th>
                    <td>
                      <div class="px-1">
                        <input v-model="proymd_f" type="date" class="form-control form-control-sm" style="max-width: 200px;" tabindex="1" />
                      </div>
                    </td>
                    <th class="text-center bg-light required small border-start">입고공정</th>
                    <td>
                      <div class="px-1">
                        <select v-model="masterInfo.progcd" class="form-select form-select-sm" tabindex="2">
                          <option v-for="opt in progOptions" :key="opt.progcd" :value="opt.progcd">{{ opt.prognm }}</option>
                        </select>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 제품 실적 그리드 -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-box-seam me-2 text-primary"></i>외주 생산 제품 실적</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef1" class="tabulator-instance flex-grow-1" tabindex="3"></div>
            </div>
          </div>

          <!-- 자재 상세 그리드 -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center">
                <i class="bi bi-tools me-2 text-success"></i>투입 자재 상세 (BOM)
                <span v-if="selectedProduct.itemnm" class="badge bg-success-subtle text-success border border-success-subtle ms-2" style="font-size: 10px;">{{ selectedProduct.itemnm }}</span>
              </span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">+ 자재추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">- 삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="4"></div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch, nextTick, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'

import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { getDate } from '@/composables/useDate'
import { useManualStore } from '@/stores/manualStore'
import { useSearchStore } from '@/stores/useSearchStore'
import { useRoute } from 'vue-router'

import AppAlert from '@/components/AppAlert.vue'

// 🚀 전용 팝업 임포트
import ProdCustHelp from '@/components/help/ProdCustHelp.vue'
import ProdItemHelp from '@/components/help/ProdItemHelp.vue'
import RawItemHelp from '@/components/help/RawItemHelp.vue'
import HelpBase from '@/components/help/HelpBase.vue'

// 1. 공통 상태 및 훅 초기화
const authStore = useAuthStore()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const manualStore = useManualStore()
const searchStore = useSearchStore()
const route = useRoute()

// 2. 참조 및 상태 관리 변수
const firstFocusRef = ref<HTMLInputElement | null>(null)
const lastActiveElement = ref<HTMLElement | null>(null)
const popVisible = reactive({
  cust: false,
  prod: false,
  item: false,
  prog: false
})
const isSaving = ref(false)

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const selectedProduct = reactive<any>({ itemcd: '', itemnm: '', prodid: 0 })
const closingInfo = reactive({ clsymd: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchForm = reactive({
  linecd: '888',
  fromdt: firstDay.replace(/-/g, ''),
  todt: today.replace(/-/g, ''),
  custcd: '',
  custnm: ''
})

const masterInfo = reactive({
  proymd: today.replace(/-/g, ''),
  progcd: '888'
})

// 포맷팅 헬퍼
const fromdt_f = computed({ get: () => formatDateDash(searchForm.fromdt), set: (v) => { if (v) searchForm.fromdt = v.replace(/-/g, '') } })
const todt_f = computed({ get: () => formatDateDash(searchForm.todt), set: (v) => { if (v) searchForm.todt = v.replace(/-/g, '') } })
const proymd_f = computed({ get: () => formatDateDash(masterInfo.proymd), set: (v) => { if (v) masterInfo.proymd = v.replace(/-/g, '') } })

// 4. 그리드 참조
const orderTableRef = ref<HTMLDivElement | null>(null)
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)

let grid0: Tabulator | null = null
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 5. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(searchForm)
  resetForm(masterInfo)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(searchForm, {
    linecd: '888',
    fromdt: firstDay.replace(/-/g, ''),
    todt: today.replace(/-/g, '')
  })
  Object.assign(masterInfo, {
    proymd: today.replace(/-/g, ''),
    progcd: '888'
  })

  selectedProduct.itemcd = ''
  selectedProduct.itemnm = ''
  selectedProduct.prodid = 0

  // 🚀 잔상 물리적 파괴 후 초기화
  if (grid0) grid0.setData([])
  if (grid1) grid1.setData([])
  if (grid2) grid2.setData([])

  fetchInitCodes()
}

const restoreFocus = () => {
  nextTick(() => {
    if (lastActiveElement.value) {
      lastActiveElement.value.focus()
    }
  })
}

// 6. 전용 팝업 확정 콜백
const onCustConfirm = (d: any) => {
  searchForm.custcd = d.custcd
  searchForm.custnm = d.custnm
  fetchOrderList()
}

const onProdConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    price: d.outprice || 0,
    _status: '수정',
    _state: 'NEW'
  })
  selectedProduct.itemcd = d.itemcd
  selectedProduct.itemnm = d.itemnm
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    mitemcd: d.itemcd,
    mitemnm: d.itemnm,
    mitsize: d.itsize || '',
    munit: d.unit || 'EA',
    astkind: d.astkind || '',
    inqty: 1,
    _status: '수정',
    _state: 'NEW'
  })
}

const onProgConfirm = (d: any) => {
  activeRow.update({ befprog: d.progcd, bprognm: d.prognm })
  markEdit(activeRow)
}

// 7. 그리드 헬퍼
const markEdit = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제') {
    row.update({ _status: '수정' })
  }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

// 8. 주요 액션
const fetchInitCodes = async () => {
  try {
    const resLine = await api.post('/hp00/HP00_000S_STR', { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' })
    lineOptions.value = resLine.data
    if (lineOptions.value.length > 0) onLineChange()
  } catch (e) {}
}

const onLineChange = async () => {
  try {
    const res = await api.post('/hp00/HP00_000S_STR', { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: searchForm.linecd })
    progOptions.value = res.data
    if (progOptions.value.length > 0) masterInfo.progcd = progOptions.value[0].progcd
  } catch (e) {}
}

const fetchOrderList = async () => {
  try {
    const res = await api.post('/hpio/HPIO_350U_STR', {
      actkind: 'L0', cmpycd: authStore.cmpycd, prodid: 0, linecd: searchForm.linecd, progcd: '888',
      proymd: searchForm.todt, ordymd: searchForm.fromdt, custcd: searchForm.custcd, prodcd: '200'
    })
    grid0?.setData(res.data || [])
    grid1?.setData([]); grid2?.setData([])
    vAlert('목록 조회가 완료되었습니다.')
  } catch (e) { vAlertError('목록 조회 실패') }
}

const fetchPerformanceMaster = async (row: any) => {
  searchForm.custcd = row.custcd; searchForm.custnm = row.custnm
  try {
    const res = await api.post('/hpio/HPIO_350U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, prodid: row.prodid || 0, linecd: searchForm.linecd,
      custcd: row.custcd, ordymd: row.ordymd, proymd: masterInfo.proymd, progcd: '888', prodcd: '200'
    })
    const data = (res.data || []).map((i: any) => {
      const amt = Math.floor(Number(i.prdqty || 0) * Number(i.price || 0))
      return { ...i, outamt: amt, outtot: Math.floor(amt * 1.1), _state: i.prodid ? 'EXIST' : 'NEW', _status: '수정' }
    })
    grid1?.setData(data)
    if (data.length > 0) { grid1?.selectRow(grid1.getRows()[0]); fetchPerformanceDetails(data[0]); }
    else { grid2?.setData([]); selectedProduct.itemcd = ''; selectedProduct.itemnm = '' }
  } catch (e) { vAlertError('상세 조회 실패') }
}

const fetchPerformanceDetails = async (row: any) => {
  selectedProduct.itemcd = row.itemcd; selectedProduct.itemnm = row.itemnm; selectedProduct.prodid = row.prodid
  if (!row.prodid) { grid2?.setData([]); return; }
  try {
    const res = await api.post('/hpio/HPIO_351U_STR', [{
      actkind: 'S0', cmpycd: authStore.cmpycd, prodid: row.prodid, matlid: 0, useyn: 'Y'
    }])
    grid2?.setData((res.data || []).map((i: any) => ({ ...i, _state: 'EXIST', _status: '수정' })))
  } catch (e) {}
}

async function saveAll() {
  if (isSaving.value) return
  if (masterInfo.proymd <= closingInfo.clsymd) return vAlertError('회계 마감된 일자입니다.')

  const prods = grid1?.getData().filter((r: any) => r._status) || []
  const mats = grid2?.getData().filter((r: any) => r._status) || []
  if (!prods.length && !mats.length) return vAlertError('저장할 변경 내용이 없습니다.')

  if (!confirm('변경된 정보를 저장하시겠습니까?')) return

  isSaving.value = true
  try {
    let lastMsg = ''
    for (const p of prods) {
      const resP = await api.post('/hpio/HPIO_350U_STR', {
        ...p, actkind: 'U0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, custcd: searchForm.custcd,
        ordymd: p.ordymd, proymd: masterInfo.proymd, progcd: masterInfo.progcd, whcd: '300', prodcd: '200'
      })
      if (resP.data?.[0]?.ioym === '000000') throw new Error(resP.data?.[0]?.iono || '제품 실적 저장 오류')
      lastMsg = resP.data?.[0]?.msg || '정상 처리되었습니다.'
    }
    for (const m of mats) {
      const actkind = (m._status === '삭제' || m.useyn === 'N') ? 'D0' : 'U0'
      const resM = await api.post('/hpio/HPIO_351U_STR', [{
        ...m, actkind, cmpycd: authStore.cmpycd, prodid: selectedProduct.prodid, matlid: m.matlid || 0,
        whcd: '300', updemp: authStore.userid
      }])
      if (resM.data?.[0]?.ioym === '000000') throw new Error(resM.data?.[0]?.iono || '자재 소모 저장 오류')
    }
    vAlert(lastMsg); const sel = grid0?.getSelectedData()[0]; if(sel) fetchPerformanceMaster(sel)
  } catch (e: any) { vAlertError(e.message || '저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'CUST') popVisible.cust = true
  else if (type === 'ITEM' || type === 'PROD') { activeRow = target; popVisible.prod = true }
  else if (type === 'MAT') { activeRow = target; popVisible.item = true }
  else if (type === 'befprog') { activeRow = target; popVisible.prog = true }
}

const addRow = (type: string) => {
  if (type === 'PROD') grid1?.addRow({ prdqty: 0, price: 0, outtot: 0, _status: '수정', _state: 'NEW', useyn: 'Y' }, false)
  else grid2?.addRow({ inqty: 0, _status: '수정', _state: 'NEW', useyn: 'Y' }, false)
}

const deleteRows = (type: string) => {
  const g = type === 'PROD' ? grid1 : grid2;
  g?.getSelectedRows().forEach(r => handleRowAction(r))
}

const progPopupColumns = [{ title: '코드', field: 'progcd', width: 100 }, { title: '공정명', field: 'prognm', widthGrow: 1 }]
const fetchProgPopupData = async (word: string) => {
  const res = await api.post('/hp00/HP00_000S_STR', { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: searchForm.linecd, codenm: word })
  return res.data
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); fetchOrderList() }
    else if (key === 's') { e.preventDefault(); saveAll() }
    else if (key === 'n') { e.preventDefault(); initialize() }
  }
}

// 9. 라이프사이클 훅
onMounted(() => {
  initGrids()
  fetchInitCodes()
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.clsymd = r.data[0].clsymd })
  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => { window.removeEventListener('keydown', handleGlobalShortcuts); searchStore.removeTab(route.name as string) })

const formatDateDash = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v
const formatNumber = (n: any) => Number(n || 0).toLocaleString()
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
