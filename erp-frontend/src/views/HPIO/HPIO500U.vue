<!--
	=============================================================
	프로그램명	: 이관출고등록 (HPIO500U)
	작성일자	: 2025.02.25
	설명        : 생산 공정/창고 간 자재 및 반제품 이관 출고 관리 (HSOD100U 표준 적용본)
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
  <!-- 🚀 하단 자재 검색용 (RawItemHelp) -->
  <RawItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-left-right me-2 text-primary" style="font-size: 18px;"></i>
        생산관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        재고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">이관출고등록 (HPIO500U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" :disabled="isClosed" tabindex="-1">저장(S)</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="isClosed" tabindex="-1">삭제(D)</button>
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
                <th class="text-center bg-light small">이동일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="form_01.fromdt"
                    v-model:todt="form_01.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small">출고창고</th>
                <td>
                  <select v-model="form_01.owhcd" class="form-select form-select-sm w-50" tabindex="102" @change="search">
                    <option value="">전체</option>
                    <option v-for="item in owhcdData" :key="item.whcd" :value="item.whcd">{{ item.whnm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 이관 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
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
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light small text-center">출고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.odeptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('ODEPT')" :disabled="isClosed" tabindex="2">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light text-center small border-start">출고번호</th>
                    <td>
                      <input
                        :value="displayIoNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light text-center small border-start">출고일자</th>
                    <td><input v-model="form_02.outymd" type="date" class="form-control" :readonly="isClosed" tabindex="3" /></td>
                    <th class="required bg-light text-center small border-start">입고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.ideptnm" class="form-control" readonly tabindex="4" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('IDEPT')" :disabled="isClosed" tabindex="5">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small text-center">출고창고</th>
                    <td>
                      <select v-model="form_02.owhcd" class="form-select" :disabled="isClosed" tabindex="6">
                        <option v-for="item in owhcdData" :key="item.whcd" :value="item.whcd">{{ item.whnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center small border-start">입고창고</th>
                    <td>
                      <select v-model="form_02.iwhcd" class="form-select" :disabled="isClosed" tabindex="7">
                        <option v-for="item in iwhcdData" :key="item.whcd" :value="item.whcd">{{ item.whnm }}</option>
                      </select>
                    </td>
                    <th class="bg-light text-center small border-start">라인/공정</th>
                    <td colspan="3">
                      <div class="d-flex gap-1">
                        <select v-model="form_02.linecd" class="form-select" :disabled="isClosed" tabindex="8" @change="onLineChange">
                          <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">{{ opt.linenm }}</option>
                        </select>
                        <select v-model="form_02.progcd" class="form-select" :disabled="isClosed" tabindex="9">
                          <option v-for="opt in progOptions" :key="opt.progcd" :value="opt.progcd">{{ opt.prognm }}</option>
                        </select>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">특기사항</th>
                    <td colspan="7">
                      <input
                        ref="remarkRef"
                        v-model="form_02.remark"
                        class="form-control"
                        :readonly="isClosed"
                        tabindex="10"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 하단 그리드: 이관 품목 리스트 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>이관 자재 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="isClosed" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="isClosed" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="11"></div>
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
import RawItemHelp from '@/components/help/RawItemHelp.vue'

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
const popVisible = reactive({
  dept: false,
  item: false,
  isIdept: false // 입고부서 구분용
})
const isSaving = ref(false)

const owhcdData = ref<any[]>([])
const iwhcdData = ref<any[]>([])
const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const form_01 = reactive({
  fromdt: firstDay,
  todt: today,
  owhcd: ''
})

const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd,
  ioym: '',
  iono: '0000',
  outymd: today,
  odeptcd: authStore.deptcd,
  odeptnm: authStore.deptnm,
  owhcd: '100',
  ideptcd: '',
  ideptnm: '',
  iwhcd: '100',
  linecd: '',
  progcd: '',
  prodcd: '200',
  iogbn: '200',
  remark: '',
  sts: 'N'
})

// 4. 연산 및 감시자
const displayIoNo = computed(() => {
  if (!form_02.iono || form_02.iono === '0000') return ''
  return `${form_02.ioym}-${form_02.iono}`
})

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !form_02.outymd) return false
  return form_02.outymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 6. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(form_02)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(form_02, {
    cmpycd: authStore.cmpycd,
    ioym: '',
    iono: '0000',
    outymd: today,
    odeptcd: authStore.deptcd,
    odeptnm: authStore.deptnm,
    owhcd: '100',
    iwhcd: '100',
    prodcd: '200',
    iogbn: '200',
    sts: 'N'
  })

  if (owhcdData.value.length > 0) form_02.owhcd = owhcdData.value[0].whcd
  if (iwhcdData.value.length > 0) form_02.iwhcd = iwhcdData.value[0].whcd

  // 🚀 잔상 물리적 파괴 후 깨끗한 빈 행 5개 생성 (Ghost Data 차단)
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

// 7. 전용 팝업 확정 콜백
const onDeptConfirm = (d: any) => {
  if (popVisible.isIdept) {
    form_02.ideptcd = d.deptcd
    form_02.ideptnm = d.deptnm
  } else {
    form_02.odeptcd = d.deptcd
    form_02.odeptnm = d.deptnm
  }
  popVisible.isIdept = false
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
  setTimeout(() => activeRow.getCell("ioqty").edit(), 150)
}

// 8. 그리드 에디터 및 헬퍼
const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const container = document.createElement("div")
  container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"
  container.innerHTML = `
    <input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex: 1;" value="${cell.getValue() || ''}">
    <i class="bi bi-search text-primary ms-1" style="font-size: 11px;"></i>
  `
  const input = container.querySelector("input") as HTMLInputElement
  onRendered(() => { input.focus(); input.select(); })
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault(); e.stopPropagation()
      success(input.value)
      handleOpenHelp('ITEM', cell.getRow())
    }
  })
  return container
}

const markEdit = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제' && d.itemcd) {
    row.update({ _status: '수정' })
  }
}

// 9. 주요 액션 (조회, 저장, 삭제)
async function search() {
  const res = await api.post('/hpio/HPIO_500U_STR', {
    actkind: 'S0',
    cmpycd: authStore.cmpycd,
    iogbn: '200',
    fromdt: form_01.fromdt.replace(/-/g, ''),
    todt: form_01.todt.replace(/-/g, ''),
    owhcd: form_01.owhcd
  })
  const data = (res.data || []).map((row: any) => ({
    ...row,
    iono_full: row.ioym && row.iono ? `${row.ioym}-${row.iono}` : ''
  }))
  grid1?.setData(data)
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8)
    ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}`
    : today

  Object.assign(form_02, { ...row, outymd: fYmd(row.outymd) })
  if (row.linecd) onLineChange()

  try {
    const res = await api.post('/hpio/HPIO_501U_STR', [{
      actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '200', ioym: row.ioym, ono: row.iono,
      ioqty: 0 // 조회 시 숫자 필드 초기화
    }])
    const data = (res.data || []).map((i: any) => ({ ...i, _state: 'EXIST', _status: '' }))
    // 🚀 [표준] 조회 시에는 실데이터만 출력
    grid2?.setData(data)
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function save() {
  if (isSaving.value) return
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (!form_02.odeptcd || !form_02.owhcd || !form_02.ideptcd || !form_02.iwhcd) {
    return vAlertError('출고/입고 부서와 창고를 확인하세요.')
  }

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const details = (grid2?.getData() || []).filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    ...d,
    actkind: d._status === '입력' ? 'A0' : (d._status === '삭제' ? 'D0' : 'U0'),
    cmpycd: authStore.cmpycd,
    iogbn: '200',
    ioym: form_02.outymd.replace(/-/g, '').substring(0, 6),
    ono: form_02.iono,
    linecd: form_02.linecd,
    progcd: form_02.progcd,
    odeptcd: form_02.odeptcd,
    owhcd: form_02.owhcd,
    ideptcd: form_02.ideptcd,
    iwhcd: form_02.iwhcd,
    ioymd: form_02.outymd.replace(/-/g, ''),
    ioqty: Number(d.ioqty || 0),
    updemp: authStore.userid
  }))

  if (!details.length && (!form_02.iono || form_02.iono === '0000')) {
    return vAlertError('저장할 내역이 없습니다.')
  }

  if (!confirm('이관출고 정보를 저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const targetIoym = form_02.outymd.replace(/-/g, '').substring(0, 6)
    const mst = {
      ...form_02,
      actkind: !form_02.iono || form_02.iono === '0000' ? 'A0' : 'U0',
      ioym: targetIoym,
      outymd: form_02.outymd.replace(/-/g, ''),
      updemp: authStore.userid
    }

    // 마스터 선행 저장 및 채번
    const resMst = await api.post('/hpio/HPIO_500U_STR', mst)
    const mstResult = resMst.data?.[0]
    const iono = mstResult?.iono || form_02.iono

    // 상세 루프 저장 (서버 통합 저장 프로시저 부재 시 표준 대응)
    for (const item of details) {
      item.ono = iono
      await api.post('/hpio/HPIO_501U_STR', [item])
    }

    vAlert('저장되었습니다(Alt+S)'); search()
  } catch (e) { vAlertError('저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'ODEPT') {
    popVisible.isIdept = false
    popVisible.dept = true
  } else if (type === 'IDEPT') {
    popVisible.isIdept = true
    popVisible.dept = true
  } else if (type === 'ITEM') {
    activeRow = target
    popVisible.item = true
  }
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

async function handleFullDelete() {
  if (!form_02.iono || form_02.iono === '0000') return vAlertError('조회 후 처리하세요.')
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (confirm('정말 전체 삭제하시겠습니까?')) {
    try {
      await api.post('/hpio/HPIO_500U_STR', { ...form_02, actkind: 'D0' })
      vAlert('삭제되었습니다.'); initialize(); search()
    } catch (e) { vAlertError('삭제 실패') }
  }
}

const onLineChange = async () => {
  if (!form_02.linecd) { progOptions.value = []; return; }
  try {
    const res = await api.get('/hp00/HP00_000S_STR', { params: { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: form_02.linecd } })
    progOptions.value = res.data
    if (progOptions.value.length > 0 && !form_02.progcd) form_02.progcd = progOptions.value[0].progcd
  } catch (e) {}
}

function handleRemarkTab(e: KeyboardEvent) {
  if (e.key === 'Tab' && !e.shiftKey) {
    e.preventDefault()
    if (grid2) {
      const rows = grid2.getRows()
      if (rows.length > 0) setTimeout(() => rows[0].getCell("itemnm").edit(), 100)
    }
  }
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); search() }
    else if (key === 's') { e.preventDefault(); save() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'd') { e.preventDefault(); handleFullDelete() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HPIO500U') }
  }
}

// 9. 라이프사이클 훅
onMounted(async () => {
  // 라인 데이터 로드
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' } }).then(res => {
    lineOptions.value = res.data
  })
  // 창고 데이터 로드
  api.post('/hs00/HS00_000S_STR', { gubun: 'W0', cmpycd: authStore.cmpycd }).then(res => {
    owhcdData.value = res.data; iwhcdData.value = res.data
  })

  // 그리드1 초기화
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "출고일자", field: "outymd", hozAlign: "center", width: 100, formatter: (c) => formatDateDash(c.getValue()) },
      { title: "출고번호", field: "iono_full", hozAlign: "center", width: 110, cssClass: "fw-bold text-primary" },
      { title: "출고부서", field: "odeptnm", hozAlign: "left" }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

  // 그리드2 초기화
  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", selectable: true,
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
      { title: "자재코드", field: "itemcd", width: 100, hozAlign: "center" },
      { title: "자재명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "수량", field: "ioqty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => markEdit(cell.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())

  api.post('/hp00/HP00_000S_STR', { gubun: 'CL', cmpycd: authStore.cmpycd }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
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
