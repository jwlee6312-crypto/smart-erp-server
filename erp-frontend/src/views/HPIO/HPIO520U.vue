<!--
	=============================================================
	프로그램명	: 재공품 타계정출고 (HPIO520U)
	작성일자	: 2025.02.25
	설명        : 생산 공정 내 재공품의 타계정 출고 관리 (HSOD100U 표준 적용 완결본)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 🚀 전용 팝업 라이브러리 (HSOD100U 규격) -->
  <DeptHelp
    v-model:visible="popVisible.dept"
    @confirm="onDeptConfirm"
    @close="restoreFocus"
  />
  <!-- 🚀 하단 제품 검색용 (ProdItemHelp) -->
  <ProdItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />
  <!-- 출고유형 선택용 (HelpBase 활용) -->
  <HelpBase
    ref="iotypeHelpRef"
    v-model:visible="popVisible.iotype"
    title="출고유형 선택"
    :columns="iotypePopupColumns"
    @search="fetchIoTypePopupData"
    @confirm="onIoTypeConfirm"
    @close="restoreFocus"
  />
  <!-- 거래처 선택용 (HelpBase 활용) -->
  <HelpBase
    ref="scustHelpRef"
    v-model:visible="popVisible.scust"
    title="거래처 선택"
    :columns="scustPopupColumns"
    @search="fetchScustPopupData"
    @confirm="onScustConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        생산관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        재고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">재공품 타계정출고 (HPIO520U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="saveData" :disabled="isClosed" tabindex="-1">저장(S)</button>
        <button class="btn-erp btn-delete" @click="deleteData" :disabled="isClosed" tabindex="-1">삭제(D)</button>
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
              <col style="width: 40%" />
              <col style="width: 10%" />
              <col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light small">출고일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="searchParam.fromdt"
                    v-model:todt="searchParam.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small border-start">생산라인</th>
                <td>
                  <select v-model="searchParam.linecd" class="form-select form-select-sm w-50" tabindex="102" @change="search">
                    <option value="">전체</option>
                    <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">{{ opt.linenm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 출고 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 400px; min-width: 400px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">이관 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 마스터 상세 폼 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light small text-center">생산라인</th>
                    <td>
                      <select ref="firstFocusRef" v-model="masterData.linecd" class="form-select" tabindex="1" @change="onLineChange">
                        <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">{{ opt.linenm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center border-start">공정명</th>
                    <td>
                      <select v-model="masterData.progcd" class="form-select" tabindex="2">
                        <option v-for="opt in progOptions" :key="opt.progcd" :value="opt.progcd">{{ opt.prognm }}</option>
                      </select>
                    </td>
                    <th class="bg-light small text-center border-start">출고번호</th>
                    <td>
                      <input
                        :value="displayIoNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light small text-center border-start">출고일자</th>
                    <td><input v-model="ioymd_f" type="date" class="form-control" :readonly="isClosed" tabindex="3" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">특기사항</th>
                    <td colspan="7">
                      <input
                        ref="remarkRef"
                        v-model="masterData.remark"
                        class="form-control"
                        :readonly="isClosed"
                        tabindex="4"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 하단: 재공품 출고 품목 리스트 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>재공품 출고 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="isClosed" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="isClosed" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="5"></div>
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
import DateForm from '@/components/DateForm.vue'

// 🚀 전용 팝업 임포트
import DeptHelp from '@/components/help/DeptHelp.vue'
import ProdItemHelp from '@/components/help/ProdItemHelp.vue'
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
const remarkRef = ref<HTMLInputElement | null>(null)
const lastActiveElement = ref<HTMLElement | null>(null)
const iotypeHelpRef = ref<any>(null)
const scustHelpRef = ref<any>(null)

const popVisible = reactive({
  dept: false,
  item: false,
  iotype: false,
  scust: false,
  isIdept: false
})
const isSaving = ref(false)

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const closingInfo = reactive({ clsymd: '', sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchParam = reactive({
  fromdt: firstDay,
  todt: today,
  linecd: '010'
})

const masterData = reactive<any>({
  cmpycd: authStore.cmpycd,
  ioym: today.replace(/-/g, '').substring(0, 6),
  iono: '0000',
  linecd: '010',
  progcd: '',
  ioymd: today.replace(/-/g, ''),
  remark: '',
  slipno: ''
})

// 포맷팅 헬퍼
const ioymd_f = computed({
  get: () => formatDateDash(masterData.ioymd),
  set: (v) => { if (v) masterData.ioymd = v.replace(/-/g, '') }
})

const displayIoNo = computed(() => {
  if (!masterData.iono || masterData.iono === '0000') return ''
  return `${masterData.ioym}-${masterData.iono}`
})

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !masterData.ioymd) return false
  return masterData.ioymd.substring(0, 6) <= closingInfo.sclsym
})

// 4. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 5. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(masterData)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(masterData, {
    cmpycd: authStore.cmpycd,
    linecd: lineOptions.value.length > 0 ? lineOptions.value[0].linecd : '010',
    ioym: today.replace(/-/g, '').substring(0, 6),
    iono: '0000',
    ioymd: today.replace(/-/g, ''),
    progcd: ''
  })

  onLineChange()

  // 🚀 [해결] 물리적 파괴 후 초기 빈 행 생성 (Ghost Data 차단)
  if (grid2 && grid2.element) {
    grid2.setData([])
    for (let i = 0; i < 5; i++) {
      grid2.addRow({ ioqty: 0 }, false)
    }
  }
  nextTick(() => firstFocusRef.value?.focus())
}

const restoreFocus = () => {
  nextTick(() => {
    if (lastActiveElement.value) {
      lastActiveElement.value.focus()
    }
  })
}

// 6. 전용 팝업 확정 콜백
const onDeptConfirm = (d: any) => {
  activeRow.update({ ideptcd: d.deptcd, ideptnm: d.deptnm })
  markEdit(activeRow)
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    ioqty: 0,
    _status: '입력',
    _state: 'NEW'
  })
  // 🚀 [해결] 선택 후 즉시 다음 칸(수량)으로 포커스 전이
  setTimeout(() => activeRow.getCell("ioqty").edit(), 150)
}

const onIoTypeConfirm = (d: any) => {
  activeRow.update({ iotype: d.code, iotypenm: d.cdnm })
  markEdit(activeRow)
  // 🚀 [해결] 선택 후 즉시 다음 칸(제품명)으로 포커스 전이
  setTimeout(() => activeRow.getCell("itemnm").edit(), 150)
}

const onScustConfirm = (d: any) => {
  activeRow.update({ scustcd: d.custcd, scustnm: d.custnm })
  markEdit(activeRow)
  // 🚀 [해결] 행 간 연속 입력 보장 (다음 행 유형으로 이동)
  const nextRow = activeRow.getNextRow()
  if (nextRow) {
    setTimeout(() => nextRow.getCell("iotypenm").edit(), 150)
  }
}

// 7. 그리드 에디터 엔진 (HSOD100U 규격)
const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const field = cell.getField()
  const container = document.createElement("div")
  container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"
  container.innerHTML = `
    <input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex: 1;" value="${cell.getValue() || ''}">
    <i class="bi bi-search text-primary ms-1" style="font-size: 11px; cursor: pointer;"></i>
  `
  const input = container.querySelector("input") as HTMLInputElement
  const icon = container.querySelector("i") as HTMLElement

  const triggerHelp = () => {
    success(input.value)
    if (field === 'iotypenm') handleOpenHelp('iotype', cell.getRow())
    else if (field === 'itemnm') handleOpenHelp('ITEM', cell.getRow())
    else if (field === 'ideptnm') handleOpenHelp('IDEPT', cell.getRow())
    else if (field === 'scustnm') handleOpenHelp('SCUST', cell.getRow())
  }

  onRendered(() => { input.focus(); input.select(); })

  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault(); e.stopPropagation()
      triggerHelp()
    }
  })
  icon.addEventListener("click", () => triggerHelp())
  return container
}

const markEdit = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제' && d.itemcd) {
    row.update({ _status: '수정' })
  }
}

// 8. 주요 액션
async function search() {
  const res = await api.post('/hpio/HPIO_520U_STR', {
    actkind: 'L', cmpycd: authStore.cmpycd,
    linecd: searchParam.linecd,
    fromdt: searchParam.fromdt.replace(/-/g, ''),
    todt: searchParam.todt.replace(/-/g, '')
  })
  const data = (res.data || []).map((i: any) => ({ ...i, iono_full: `${i.ioym}-${i.iono}` }))
  grid1?.setData(data)
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8) ? d : today.replace(/-/g, '')
  Object.assign(masterData, { ...row, ioymd: fYmd(row.ioymd) })
  onLineChange()

  try {
    const res = await api.post('/hpio/HPIO_521U_STR', [{
      actkind: 'S', cmpycd: authStore.cmpycd, ioym: row.ioym, iono: row.iono, linecd: row.linecd,
      ioqty: 0
    }])
    const data = (res.data || []).map((i: any) => ({ ...i, _state: 'EXIST', _status: '' }))
    grid2?.setData(data)
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function saveData() {
  if (isSaving.value) return
  if (masterData.ioymd <= closingInfo.clsymd) return vAlertError('회계 마감된 일자입니다.')
  if (masterData.slipno > '000') return vAlertError('이미 전표가 발행되어 수정할 수 없습니다.')

  const details = (grid2?.getData() || []).filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    ...d,
    actkind: d._status === '입력' ? 'A' : (d._status === '삭제' ? 'D' : 'U'),
    cmpycd: authStore.cmpycd,
    ioym: masterData.ioymd.substring(0, 6),
    linecd: masterData.linecd, progcd: masterData.progcd, ioymd: masterData.ioymd,
    ioqty: Number(d.ioqty || 0),
    userid: authStore.userid
  }))

  if (!details.length && masterData.iono === '0000') return vAlertError('저장할 내역이 없습니다.')

  if (!confirm('재공품 타계정출고 정보를 저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const actkind = masterData.iono === '0000' ? 'A' : 'U'
    const resM = await api.post('/hpio/HPIO_520U_STR', {
      ...masterData, actkind, cmpycd: authStore.cmpycd, userid: authStore.userid
    })
    const mstData = resM.data?.[0]
    const rowValues = mstData?.returnkeyvalue || Object.values(mstData || {})
    const keyYM = (mstData?.ioym || rowValues[0] || '').toString().trim()
    const keyNO = (mstData?.iono || rowValues[1] || '').toString().trim()

    if (keyYM === '000000') throw new Error(keyNO || '마스터 저장 오류')

    for (const item of details) {
      item.ioym = keyYM; item.iono = keyNO
      await api.post('/hpio/HPIO_521U_STR', [item])
    }

    vAlert('저장되었습니다(Alt+S)'); search()
  } catch (e: any) { vAlertError(e.message || '저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'ITEM') { activeRow = target; popVisible.item = true }
  else if (type === 'iotype') { activeRow = target; popVisible.iotype = true }
  else if (type === 'IDEPT') { activeRow = target; popVisible.isIdept = true; popVisible.dept = true }
  else if (type === 'SCUST') { activeRow = target; popVisible.scust = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = () => {
  if (isClosed.value) return
  grid2?.addRow({ ioqty: 0, _status: '입력', _state: 'NEW' }, false)
}

const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

async function deleteData() {
  if (!masterData.iono || masterData.iono === '0000') return vAlertError('조회 후 처리하세요.')
  if (confirm('전체 삭제하시겠습니까?')) {
    try {
      await api.post('/hpio/HPIO_520U_STR', { ...masterData, actkind: 'D', cmpycd: authStore.cmpycd })
      vAlert('삭제되었습니다.'); initialize(); search()
    } catch (e) { vAlertError('삭제 실패') }
  }
}

const onLineChange = async () => {
  if (!masterData.linecd) { progOptions.value = []; return; }
  try {
    const res = await api.get('/hp00/HP00_000S_STR', { params: { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: masterData.linecd } })
    progOptions.value = res.data
    if (progOptions.value.length > 0 && !masterData.progcd) masterData.progcd = progOptions.value[0].progcd
  } catch (e) {}
}

const iotypePopupColumns = [{ title: '코드', field: 'code', width: 100 }, { title: '유형명', field: 'cdnm', widthGrow: 1 }]
const fetchIoTypePopupData = async (word: string) => {
  const res = await api.post('/hs00/HS00_000S_STR', {
    gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '130', code: '1', codenm: word, etcval: ''
  })
  if (iotypeHelpRef.value) iotypeHelpRef.value.setData(res.data || [])
}

const scustPopupColumns = [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', widthGrow: 1 }]
const fetchScustPopupData = async (word: string) => {
  const res = await api.post('/ha00/HA00_00P_STR', { gubun: 'C4', cmpycd: authStore.cmpycd, codenm: word })
  if (scustHelpRef.value) scustHelpRef.value.setData(res.data || [])
}

function handleRemarkTab(e: KeyboardEvent) {
  if (e.key === 'Tab' && !e.shiftKey) {
    e.preventDefault()
    if (grid2) {
      const rows = grid2.getRows()
      if (rows.length > 0) setTimeout(() => rows[0].getCell("iotypenm").edit(), 100)
    }
  }
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); search() }
    else if (key === 's') { e.preventDefault(); saveData() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'd') { e.preventDefault(); deleteData() }
  }
}

// 9. 라이프사이클 훅
onMounted(async () => {
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' } }).then(res => {
    lineOptions.value = res.data
    if (lineOptions.value.length > 0 && !masterData.linecd) masterData.linecd = lineOptions.value[0].linecd
    onLineChange()
  })
  api.post('/hp00/HP00_000S_STR', { gubun: 'CL', cmpycd: authStore.cmpycd }).then(r => {
    if(r.data?.length) { closingInfo.clsymd = r.data[0].clsymd; closingInfo.sclsym = r.data[0].sclsym }
  })

  // 그리드1 초기화
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "출고일자", field: "ioymd", hozAlign: "center", width: 100, formatter: (c) => formatDateDash(c.getValue()) },
      { title: "출고번호", field: "iono_full", hozAlign: "center", width: 110, cssClass: "fw-bold text-primary" },
      { title: "생산라인", field: "linenm", hozAlign: "left" }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

  // 그리드2 초기화 (탭 순서 및 고성능 에디터 전수 적용)
  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", selectable: true,
    keybindings: { "navNext": "9" },
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "", width: 40, hozAlign: "center", headerHozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>'
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>'
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>'
          return ''
      }},
      { title: "유형", field: "iotypenm", width: 120, editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('iotype', cell.getRow()) },
      { title: "제품명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "수량", field: "ioqty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => markEdit(cell.getRow()) },
      { title: "사용부서", field: "ideptnm", width: 150, editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('IDEPT', cell.getRow()) },
      { title: "거래처", field: "scustnm", width: 150, editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('SCUST', cell.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())
  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
  searchStore.removeTab(route.name as string)
})

const formatDateDash = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
</style>
