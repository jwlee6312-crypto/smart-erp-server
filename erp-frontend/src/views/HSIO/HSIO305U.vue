<!--
	=============================================================
	프로그램명	: 외상매출건별 입금입력 (HSIO305U)
	작성일자	: 2025.02.24
	설명        : 매출건별 상계 처리 (HSOD100U 무결성 표준 적용본)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 🚀 전용 팝업 라이브러리 -->
  <DeptHelp
    v-model:visible="popVisible.dept"
    @confirm="onDeptConfirm"
    @close="restoreFocus"
  />
  <SaleCustHelp
    v-model:visible="popVisible.cust"
    @confirm="onCustConfirm"
    @close="restoreFocus"
  />
  <!-- 입금유형 선택 (HelpBase 활용) -->
  <HelpBase
    ref="imtypeHelpRef"
    v-model:visible="popVisible.imtype"
    title="입금유형 선택"
    :columns="imtypePopupColumns"
    @search="fetchImTypePopupData"
    @confirm="onImTypeConfirm"
    @close="restoreFocus"
  />
  <!-- 관리번호 선택 (HelpBase 활용) -->
  <HelpBase
    ref="mgtHelpRef"
    v-model:visible="popVisible.mgt"
    title="관리번호 선택"
    :columns="mgtPopupColumns"
    :large="popVisible.isP1"
    @search="fetchMgtPopupData"
    @confirm="onMgtConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cash-coin me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        입금관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외상매출건별 입금입력 (HSIO305U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" :disabled="isClosed" tabindex="-1">저장(S)</button>
        <button class="btn-erp btn-delete" @click="handleDelete" :disabled="isClosed" tabindex="-1">삭제(D)</button>
      </div>
    </div>

    <!-- [2] 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 상단 조회 필터 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col style="width: 160px;" />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col style="width: 220px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="bg-light text-center small">기준일자</th>
                <td><input type="date" v-model="filter.imymd" class="form-control form-control-sm" tabindex="101" /></td>
                <th class="required bg-light text-center small border-start">거래처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="filter.schCustnm" class="form-control" placeholder="거래처 검색" @keyup.enter="handleOpenHelp('CUST_SCH')" tabindex="102" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST_SCH')" tabindex="103"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center small border-start">입금번호</th>
                <td>
                  <div class="d-flex gap-1 align-items-center">
                    <input v-model="filter.schImym" class="form-control form-control-sm text-center" style="width: 90px;" placeholder="YYYYMM" tabindex="104" />
                    <input v-model="filter.schImno" class="form-control form-control-sm text-center" style="width: 60px;" placeholder="0000" tabindex="105" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 상단 그리드: 미수 내역 -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-standard">
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between align-items-center">
          <span><i class="bi bi-list-check me-2 text-primary"></i>매출 건별 미수 내역 (상계대상)</span>
          <div class="d-flex gap-4">
             <span class="text-primary small fw-bold">미수 합계: {{ formatNumber(totals.janSum) }}</span>
             <span class="text-danger small fw-bold">상계 선택 합계: {{ formatNumber(totals.setSum) }}</span>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="grid1Ref" class="tabulator-instance flex-grow-1" tabindex="10"></div>
        </div>
      </div>

      <!-- 중간 마스터 정보 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup><col style="width: 120px;" /><col /><col style="width: 120px;" /><col /><col style="width: 120px;" /><col /></colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center small">입금부서</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input ref="firstFocusRef" v-model="formMst.deptnm" class="form-control" readonly tabindex="1" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center small border-start">입금번호</th>
                <td>
                  <div class="d-flex gap-1">
                    <input v-model="formMst.imym" class="form-control bg-light text-center" readonly style="width: 90px;" tabindex="-1" />
                    <input v-model="formMst.imno" class="form-control bg-light text-center text-primary fw-bold" readonly style="width: 60px;" tabindex="-1" />
                  </div>
                </td>
                <th class="required bg-light text-center small border-start">입금일자</th>
                <td><input type="date" v-model="formMst.imymd" class="form-control form-control-sm" :readonly="isClosed" tabindex="3" /></td>
              </tr>
              <tr>
                <th class="bg-light text-center small">거래처명</th>
                <td><input v-model="formMst.custnm" class="form-control bg-light" readonly tabindex="-1" /></td>
                <th class="bg-light text-center small border-start">여신잔액</th>
                <td><input :value="formatNumber(formMst.janamt)" class="form-control bg-light text-end" readonly tabindex="-1" /></td>
                <th class="bg-light text-center small border-start">여신기한</th>
                <td><input v-model="formMst.rcvdd" class="form-control bg-light text-center" readonly style="width: 80px;" tabindex="-1" /></td>
              </tr>
              <tr>
                <th class="bg-light text-center small">적요</th>
                <td colspan="5">
                  <input ref="remarkRef" v-model="formMst.remark" class="form-control form-control-sm" :readonly="isClosed" tabindex="4" @keydown.tab="handleRemarkTab" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 하단 그리드: 입금 수단 -->
      <div class="card border shadow-sm flex-shrink-0 d-flex flex-column overflow-hidden grid-container-standard" style="height: 220px;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-wallet2 me-2 text-primary"></i>입금 수단 리스트</span>
          <div class="d-flex gap-3 align-items-center">
            <span class="text-primary small fw-bold">수단 합계: {{ formatNumber(totals.paySum) }}</span>
            <div class="btn-group-erp d-flex gap-1">
              <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="isClosed" style="font-size: 11px;">+ 행추가</button>
              <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="isClosed" style="font-size: 11px;">- 행삭제</button>
            </div>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="grid2Ref" class="tabulator-instance flex-grow-1" tabindex="5"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, computed, onUnmounted } from 'vue'
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
import DateForm from '@/components/DateForm.vue'
import DeptHelp from '@/components/help/DeptHelp.vue'
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'
import HelpBase from '@/components/help/HelpBase.vue'

// 1. 공통 상태 및 훅 초기화
const authStore = useAuthStore()
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const manualStore = useManualStore()
const searchStore = useSearchStore()
const route = useRoute()

// 2. 참조 및 상태 관리 변수
const firstFocusRef = ref<HTMLInputElement | null>(null)
const remarkRef = ref<HTMLInputElement | null>(null)
const lastActiveElement = ref<HTMLElement | null>(null)
const imtypeHelpRef = ref<any>(null)
const mgtHelpRef = ref<any>(null)

const popVisible = reactive({
  dept: false,
  cust: false,
  imtype: false,
  mgt: false,
  isP1: false,
  isSch: false
})
const isSaving = ref(false)
const closingInfo = reactive({ sclsym: '' })
const billgbnOptions = { "010": "자수어음", "020": "타수어음" }

const totals = reactive({ janSum: 0, setSum: 0, paySum: 0 })
let activeRow: any = null

// 3. 데이터 모델링
const filter = reactive({
  imymd: today,
  schCustcd: '',
  schCustnm: '',
  schImym: today.substring(0, 7).replace(/-/g, ''),
  schImno: ''
})

const formMst = reactive<any>({
  cmpycd: authStore.cmpycd,
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  imno: '0000',
  imym: '',
  imymd: today,
  custcd: '',
  custnm: '',
  janamt: 0,
  rcvdd: '',
  remark: '',
  actkind: 'A0'
})

const isClosed = computed(() => closingInfo.sclsym && formMst.imymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym)

// 4. 그리드 참조
const grid1Ref = ref<HTMLDivElement | null>(null)
const grid2Ref = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 5. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(formMst)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false
  Object.assign(totals, { janSum: 0, setSum: 0, paySum: 0 })

  Object.assign(formMst, {
    cmpycd: authStore.cmpycd,
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    imno: '0000',
    imym: '',
    imymd: today,
    actkind: 'A0',
    janamt: 0,
    rcvdd: ''
  })

  if (grid1) grid1.setData([])
  if (grid2) {
    grid2.setData([])
    for (let i = 0; i < 5; i++) grid2.addRow({ imamt: 0, billamt: 0, pubymd: '', endymd: '' }, false)
  }
  nextTick(() => firstFocusRef.value?.focus())
}

const restoreFocus = () => { nextTick(() => lastActiveElement.value?.focus()) }

const onDeptConfirm = (d: any) => {
  formMst.deptcd = d.deptcd
  formMst.deptnm = d.deptnm
}

const onCustConfirm = (d: any) => {
  if (popVisible.isSch) {
    filter.schCustcd = d.custcd
    filter.schCustnm = d.custnm
    search()
  } else {
    formMst.custcd = d.custcd
    formMst.custnm = d.custnm
    api.post('/hs00/HS00_150S_STR', { cmpycd: authStore.cmpycd, custnm: d.custnm }).then(r => {
      if(r.data?.length) {
        const n = r.data[0]
        formMst.rcvdd = n.rcvdd || n.RCVDD || ''
        formMst.janamt = n.janamt || n.JANAMT || 0
      }
    })
  }
  popVisible.isSch = false
}

const onImTypeConfirm = (d: any) => {
  const code = String(d.imgbn || d.code || '').trim()
  activeRow.update({
    imtype: code,
    imtypenm: String(d.imgbnnm || d.cdnm || '').trim(),
    dacctcd: d.dacctcd || '',
    cacctcd: d.cacctcd || '',
    mgtno: '',
    _status: '입력',
    _state: 'NEW'
  })
  markEdit(activeRow)
  setTimeout(() => activeRow.getCell("imamt").edit(), 150)
}

const onMgtConfirm = (d: any) => {
  activeRow.update({ mgtno: popVisible.isP1 ? d.slipno : d.mgtno })
  markEdit(activeRow)
  setTimeout(() => activeRow.getCell("billamt").edit(), 150)
}

// 6. 그리드 에디터 엔진 (HSIO300U 참조)
const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const field = cell.getField()
  const container = document.createElement("div")
  container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"
  container.innerHTML = `<input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex:1;"><i class="bi bi-search text-primary ms-1" style="font-size:11px; cursor:pointer;"></i>`
  const input = container.querySelector("input") as HTMLInputElement; const icon = container.querySelector("i") as HTMLElement

  const triggerHelp = () => {
    success(input.value)
    if (field === 'imtypenm') handleOpenHelp('IMTYPE', cell.getRow())
    else if (field === 'mgtno') handleOpenHelp('MGT', cell.getRow())
  }
  onRendered(() => { input.value = cell.getValue() || ''; input.focus(); input.select(); })
  input.addEventListener("keydown", (e) => { if (e.key === "Enter") { e.preventDefault(); triggerHelp(); } })
  icon.addEventListener("click", triggerHelp); return container
}

const markEdit = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제') row.update({ _status: '수정' })
  calculateTotals()
}

const calculateTotals = () => {
  totals.janSum = (grid1?.getData() || []).reduce((acc, r:any) => acc + Number(r.janamt || 0), 0)
  totals.setSum = (grid1?.getSelectedData() || []).reduce((acc, r:any) => acc + Number(r.imamt || 0), 0)

  const payData = grid2?.getData().filter(d => d.imtype && d._status !== '삭제') || []
  totals.paySum = payData.reduce((acc, cur) => acc + (Number(cur.imamt) || 0), 0)
  amtTot.value = totals.paySum
  billTot.value = payData.reduce((acc, cur) => acc + (Number(cur.billamt) || 0), 0)
}

const amtTot = ref(0); const billTot = ref(0) // HSIO300U와 동일한 하단 합계 관리용

// 7. 주요 액션 (조회, 저장, 삭제)
async function search() {
  if (!filter.schCustnm && !filter.schCustcd) return vAlertError('거래처를 선택하세요.')
  try {
    const res = await api.post('/hsio/HSIO_300U_STR', {
      actkind: 'S2', cmpycd: authStore.cmpycd,
      custcd: filter.schCustcd, imymd: filter.imymd.replace(/-/g, '')
    })
    grid1?.setData((res.data || []).map((i: any) => ({
      ...i,
      imamt: Number(i.janamt || 0),
      maeamt: Number(i.ioamt || 0) + Number(i.iovat || 0)
    })))
    formMst.custcd = filter.schCustcd
    formMst.custnm = filter.schCustnm
    // 여신 재조회
    api.post('/hs00/HS00_150S_STR', { cmpycd: authStore.cmpycd, custnm: filter.schCustnm }).then(r => {
      if(r.data?.length) {
        const n = r.data[0]
        formMst.janamt = n.janamt || n.JANAMT || 0
        formMst.rcvdd = n.recdd || n.rcvdd || ''
      }
    })
    vAlert('조회되었습니다(Alt+F)')
    nextTick(() => calculateTotals())
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (isSaving.value || isClosed.value) return
  if (Math.abs(totals.setSum - totals.paySum) > 1) return vAlertError('상계 합계와 수단 합계가 일치해야 합니다.')
  if (totals.setSum <= 0) return vAlertError('상계할 금액을 입력하세요.')

  isSaving.value = true
  try {
    const payload = {
      mst: { ...formMst, imymd: formMst.imymd.replace(/-/g, ''), actkind: formMst.imno === '0000' ? 'A0' : 'U0' },
      dtl: grid2?.getData().filter(r => r.imtype && r._status).map((d:any) => ({
        ...d, actkind: 'A2', cmpycd: authStore.cmpycd,
        pubymd: String(d.pubymd || '').replace(/-/g, ''),
        endymd: String(d.endymd || '').replace(/-/g, '')
      })),
      setdtl: grid1?.getSelectedData().map((d:any) => ({ ...d, actkind: 'A0', cmpycd: authStore.cmpycd }))
    }
    await api.post('/hsio/HSIO_300U_SAVE', payload)
    vAlert('저장되었습니다(Alt+S)')
    initialize()
  } catch (e: any) { vAlertError(e.response?.data?.message || '저장 실패') } finally { isSaving.value = false }
}

const handleDelete = async () => {
  if (!confirm('정말 삭제하시겠습니까?')) return
  try {
    await api.post('/hsio/HSIO_300U_STR', { ...formMst, actkind: 'D0' })
    vAlert('삭제되었습니다.')
    initialize()
  } catch (e) { vAlertError('삭제 실패') }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'CUST_SCH') { popVisible.isSch = true; popVisible.cust = true }
  else if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') { popVisible.isSch = false; popVisible.cust = true }
  else if (type === 'IMTYPE') { activeRow = target; popVisible.imtype = true }
  else if (type === 'MGT') {
    activeRow = target; const im = String(activeRow.getData().imtype || '').trim()
    if (im === '400') return vAlertError('어음번호는 직접 입력하십시오.'); popVisible.isP1 = (im === '500' || im === '510'); popVisible.mgt = true
  }
}

const handleRowAction = (row: any) => {
  const d = row.getData();
  if (!d.imtype) row.delete();
  else if (d._state === 'NEW') row.delete();
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' });
  calculateTotals();
}

const addRow = () => {
  if (isClosed.value) return
  const diff = totals.setSum - totals.paySum
  grid2?.addRow({ imamt: diff > 0 ? diff : 0, billamt: 0, pubymd: '', endymd: '', _status: '입력', _state: 'NEW' }, false)
}

const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

// 팝업 데이터 로드
const imtypePopupColumns = [{ title: '코드', field: 'imgbn', width: 100 }, { title: '유형명', field: 'imgbnnm', widthGrow: 1 }]
const fetchImTypePopupData = async (word: string) => {
  api.post('/hs00/HS00_000S_STR', { gubun: 'I2', cmpycd: authStore.cmpycd, codenm: word }).then(r => imtypeHelpRef.value?.setData(r.data))
}

const mgtPopupColumns = computed(() => popVisible.isP1 ? [
  { title: '발행일', field: 'slipymd', width: 100 },
  { title: '번호', field: 'slipno', width: 100 },
  { title: '순번', field: 'srowno', width: 60 },
  { title: '적요', field: 'remark', widthGrow: 1 },
  { title: '잔액', field: 'janamt', width: 110, hozAlign: 'right', formatter: 'money' }
] : [{ title: '계좌/카드번호', field: 'mgtno', width: 180 }, { title: '명칭', field: 'mgtnm', widthGrow: 1 }])

const fetchMgtPopupData = async (word: string) => {
  const d = activeRow.getData(); const im = String(d.imtype || d.imgbn || '').trim(); let gubun = 'M0'; let gbncd = '010'; let rem = d.dacctcd
  if (['200', '600', '210'].includes(im)) rem = (im === '210') ? '1145' : '1120'
  else if (im === '300') { gbncd = '040'; rem = '1110' }
  else if (im === '500' || im === '510') { if (!formMst.custcd) return vAlertError('거래처를 먼저 선택하십시오.'); gubun = 'P1'; gbncd = (im === '500') ? '2110' : '2125'; rem = formMst.custcd }
  api.post('/ha00/HA00_00P_STR', { gubun, cmpycd: authStore.cmpycd, gbncd, remark: rem, codenm: word }).then(r => mgtHelpRef.value?.setData(r.data))
}

function handleRemarkTab(e: KeyboardEvent) { if (e.key === 'Tab' && !e.shiftKey) { e.preventDefault(); if (grid2) { const rows = grid2.getRows(); if (rows.length > 0) setTimeout(() => rows[0].getCell("imtypenm").edit(), 100) } } }
function handleGlobalShortcuts(e: KeyboardEvent) { if (e.altKey) { const k = e.key.toLowerCase(); if (k === 'f') { e.preventDefault(); search() } else if (k === 's') { e.preventDefault(); save() } else if (k === 'n') { e.preventDefault(); initialize() } } }

// 8. 라이프사이클 훅
onMounted(async () => {
  grid1 = new Tabulator(grid1Ref.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터가 없습니다.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "정산번호", field: "jsanno_full", hozAlign: "center", width: 120, formatter: (c) => { const d = c.getData(); return d.jsanym ? `${d.jsanym}-${d.jsanno}` : ''; } },
      { title: "출고번호", field: "iono_full", hozAlign: "center", width: 120, formatter: (c) => { const d = c.getData(); return d.ioym ? `${d.ioym}-${d.iono}` : ''; } },
      { title: "매출부서", field: "deptnm", hozAlign: "left" },
      { title: "정산일", field: "jsanymd", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
      { title: "매출총액", field: "maeamt", width: 110, hozAlign: "right", formatter: "money" },
      { title: "미수잔액", field: "janamt", width: 110, hozAlign: "right", formatter: "money", cssClass: "text-danger fw-bold" },
      { title: "상계금액", field: "imamt", width: 110, hozAlign: "right", editor: "number", cellEdited: () => calculateTotals(), cssClass: "bg-primary-subtle fw-bold" }
    ],
  })
  grid1.on("rowSelectionChanged", () => calculateTotals())

  grid2 = new Tabulator(grid2Ref.value!, {
    layout: "fitColumns", height: "100%", selectable: true,
    keybindings: { "navNext": "9" },
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === '입력' ? '<span class="badge bg-primary">신규</span>' : (c.getValue() === '수정' ? '<span class="badge bg-warning text-dark">수정</span>' : (c.getValue() === '삭제' ? '<span class="badge bg-danger">삭제</span>' : '')) },
      { title: "입금유형", field: "imtypenm", minWidth: 150, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('IMTYPE', cell.getRow()) },
      { title: "입금액", field: "imamt", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "관리번호", field: "mgtno", width: 140, editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('MGT', cell.getRow()) },
      { title: "어음액면가", field: "billamt", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "어음종류", field: "billgbn", width: 90, editor: "list", editorParams: { values: billgbnOptions }, formatter: (c) => billgbnOptions[c.getValue() as keyof typeof billgbnOptions] || c.getValue(), cellEdited: (c) => markEdit(c.getRow()) },
      { title: "발행일", field: "pubymd", width: 120, editor: "date", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "발행인", field: "pubman", width: 100, editor: "input", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "만기일", field: "endymd", width: 120, editor: "date", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "발행은행", field: "pubbank", width: 110, editor: "input", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
  searchStore.removeTab(route.name as string)
})

const formatNumber = (n: any) => Number(n || 0).toLocaleString()
const formatDate = (d: any) => (d && d.length === 8) ? `${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}` : d
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 11px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
