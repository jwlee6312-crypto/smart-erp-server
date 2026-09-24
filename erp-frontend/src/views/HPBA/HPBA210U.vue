<!--
	=============================================================
	프로그램명	: 표준 BOM (HPBA210U)
	작성일자	: 2025.03.22
	설명        : 제품별/공정별 투입 원자재 및 표준 소요량(BOM) 관리 (HSOD100U 무결성 표준 적용본)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 🚀 전용 팝업 라이브러리 -->
  <!-- 상단 제품 검색용 -->
  <ProdItemHelp
    v-model:visible="popVisible.prod"
    @confirm="onProdConfirm"
    @close="restoreFocus"
  />
  <!-- 그리드 투입자재 검색용 -->
  <RawItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />
  <!-- 이전공정 선택용 -->
  <HelpBase
    v-model:visible="popVisible.prog"
    title="이전공정 선택"
    :columns="progPopupColumns"
    @search="fetchProgPopupData"
    @confirm="onProgConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
        생산관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        기준정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">표준 BOM (HPBA210U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" :disabled="!selectedProg.progcd" tabindex="-1">저장(S)</button>
      </div>
    </div>

    <!-- [2] 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 상단 조회 필터 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 100px;" /><col style="width: 200px;" />
              <col style="width: 100px;" /><col style="width: 150px;" />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light small text-center">생산라인</th>
                <td>
                  <select v-model="searchData.linecd" class="form-select form-select-sm" tabindex="101" @change="onLineChange">
                    <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">
                      [{{ opt.linecd }}] {{ opt.linenm }}
                    </option>
                  </select>
                </td>
                <th class="required bg-light small text-center border-start">재고자산</th>
                <td>
                  <select v-model="searchData.astkind" class="form-select form-select-sm" tabindex="102" @change="onLineChange">
                    <option value="200">제품</option>
                    <option value="210">반제품</option>
                  </select>
                </td>
                <th class="required bg-light small text-center border-start">제품명</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.itemcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 100px;" readonly tabindex="-1" />
                    <input ref="firstFocusRef" v-model="searchData.itemnm" type="text" class="form-control" placeholder="제품 선택" tabindex="103" @keyup.enter="handleOpenHelp('PROD')" />
                    <input v-model="searchData.itsize" type="text" class="form-control bg-light" style="max-width: 150px;" readonly placeholder="규격" tabindex="-1" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('PROD')" tabindex="104"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 생산 공정 리스트 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="width: 300px; min-width: 300px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-gear-wide-connected me-2 text-primary"></i> 생산 공정 리스트
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <ul class="list-group list-group-flush">
              <li v-if="processList.length === 0" class="list-group-item text-center py-5 text-muted small">제품을 먼저 조회하세요.</li>
              <li
                v-for="prog in processList"
                :key="prog.progcd"
                class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-2 px-3 cursor-pointer border-bottom"
                :class="{ 'bg-primary-subtle text-primary fw-bold': selectedProg.progcd === prog.progcd }"
                @click="onProcessSelect(prog)"
              >
                <div style="font-size: 12px;">
                    <span class="badge bg-secondary me-2" style="font-size: 10px;">{{ prog.progcd }}</span>
                    <span>{{ prog.prognm }}</span>
                </div>
                <i class="bi bi-chevron-right small opacity-50"></i>
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측: 자재 상세 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center">
                <i class="bi bi-box-seam me-2 text-primary"></i> 투입 자재 명세 (BOM)
                <span v-if="selectedProg.prognm" class="badge bg-info ms-2 px-3 text-white" style="font-size: 10px;">{{ selectedProg.prognm }}</span>
              </span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="!selectedProg.progcd" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="!selectedProg.progcd" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="gridElement" class="tabulator-instance flex-grow-1" tabindex="10"></div>
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
import DeptHelp from '@/components/help/DeptHelp.vue'
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
  prod: false,
  item: false,
  prog: false
})
const isSaving = ref(false)

const lineOptions = ref<any[]>([])
const processList = ref<any[]>([])
const selectedProg = reactive({ progcd: '', prognm: '' })
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchData = reactive({
  linecd: '010',
  astkind: '200',
  itemcd: '',
  itemnm: '',
  itsize: '',
  unit: ''
})

// 4. 그리드 참조
const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

// 5. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(searchData)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(searchData, {
    linecd: lineOptions.value.length > 0 ? lineOptions.value[0].linecd : '010',
    astkind: '200'
  })

  processList.value = []
  selectedProg.progcd = ''
  selectedProg.prognm = ''

  // 🚀 잔상 물리적 파괴 후 초기화
  if (grid && grid.element) {
    grid.setData([])
  }
  nextTick(() => firstFocusRef.value?.focus())
}

const restoreFocus = () => {
  nextTick(() => {
    if (lastActiveElement.value) {
      const el = lastActiveElement.value
      el.focus()
    }
  })
}

// 6. 전용 팝업 확정 콜백
const onProdConfirm = (d: any) => {
  searchData.itemcd = d.itemcd
  searchData.itemnm = d.itemnm
  searchData.itsize = d.itsize
  searchData.unit = d.unit
  search() // 제품 선택 시 자동 공정 조회
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    mitemcd: d.itemcd,
    mitemnm: d.itemnm,
    mitsize: d.itsize || '',
    munit: d.unit || 'EA',
    mastkind: d.astkind || '',
    inqty: 1,
    losrate: 0,
    useyn: 'Y',
    _status: '입력',
    _state: 'NEW'
  })
  setTimeout(() => activeRow.getCell("inqty").edit(), 150)
}

const onProgConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({ befprog: d.progcd, bprognm: d.prognm })
  updateRowStatus(activeRow)
}

// 7. 그리드 에디터 및 계산 로직
const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const container = document.createElement("div")
  container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"
  container.innerHTML = `
    <input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex: 1;" value="${cell.getValue() || ''}">
    <i class="bi bi-search text-primary ms-1" style="font-size: 11px;"></i>
  `
  const input = container.querySelector("input") as HTMLInputElement
  onRendered(() => {
    input.focus()
    input.select()
  })
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault(); e.stopPropagation()
      success(input.value)
      handleOpenHelp('ITEM', cell.getRow())
    }
  })
  return container
}

const updateRowStatus = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제' && d.mitemcd) {
    row.update({ _status: '수정' })
  }
}

// 8. 주요 액션 (조회, 저장, 삭제)
async function search() {
  if (!searchData.itemcd) return vAlertError('제품을 먼저 선택하세요.')
  try {
    const res = await api.get('/hp00/HP00_000S_STR', {
      params: {
        gubun: 'G1',
        cmpycd: authStore.cmpycd,
        gbncd: searchData.linecd,
        code: searchData.itemcd
      }
    })
    processList.value = res.data
    selectedProg.progcd = ''
    selectedProg.prognm = ''
    grid?.setData([])
    vAlert('공정 조회가 완료되었습니다.')
  } catch (e) {
    vAlertError('공정 조회 실패')
  }
}

const onProcessSelect = (prog: any) => {
  selectedProg.progcd = prog.progcd
  selectedProg.prognm = prog.prognm
  fetchBomDetails()
}

async function fetchBomDetails() {
  try {
    const res = await api.post('/hpba/HPBA_210U_STR', {
      actkind: 'S0',
      cmpycd: authStore.cmpycd,
      itemcd: searchData.itemcd,
      linecd: searchData.linecd,
      progcd: selectedProg.progcd,
      inqty: 0,
      losrate: 0
    })

    const cleanData = (res.data || []).map((i: any) => ({
      ...i,
      bprognm: i.bprognm || i.befprognm || i.prognm || '',
      _state: 'EXIST',
      _status: ''
    })).filter((i: any) => i.mitemcd)

    // 🚀 [표준] 조회 시에는 실데이터만 출력
    grid?.setData(cleanData)
  } catch (e) {
    vAlertError('BOM 상세 조회 실패')
  }
}

async function save() {
  if (isSaving.value) return
  if (!selectedProg.progcd) return vAlertError('공정을 선택하세요.')

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const allData = grid?.getData() || []
  const details = allData.filter((r: any) =>
    r.mitemcd && String(r.mitemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    actkind: d._status === '입력' ? 'A0' : (d._status === '삭제' ? 'D0' : 'U0'),
    cmpycd: authStore.cmpycd,
    userid: authStore.userid,
    itemcd: searchData.itemcd,
    linecd: searchData.linecd,
    progcd: selectedProg.progcd,
    mitemcd: d.mitemcd,
    inqty: Number(d.inqty || 0),
    losrate: Number(d.losrate || 0),
    befprog: d.befprog || '',
    useyn: (d.useyn === 'Y' || d.useyn === true) ? 'Y' : 'N'
  }))

  if (details.length === 0) return vAlertError('저장할 변경 내역이 없습니다.')

  if (!confirm('현재 공정의 BOM 설정을 저장하시겠습니까?')) return

  isSaving.value = true
  try {
    // BOM 저장은 건별 처리 로직 유지 (기존 API 규격)
    for (const payload of details) {
      await api.post('/hpba/HPBA_210U_STR', payload)
    }
    vAlert('정상적으로 저장되었습니다(Alt+S)')
    fetchBomDetails()
  } catch (e) {
    vAlertError('저장 중 오류 발생')
  } finally {
    isSaving.value = false
  }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'PROD') popVisible.prod = true
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
  else if (type === 'PROG') { activeRow = target; popVisible.prog = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.mitemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = () => {
  if (!selectedProg.progcd) return vAlertError('공정을 먼저 선택하세요.')
  // 🚀 [해결] 아래에 행 추가 (탭 순서 최적화)
  grid?.addRow({
    _status: '입력',
    _state: 'NEW',
    mitemcd: '',
    mitemnm: '',
    inqty: 1,
    losrate: 0,
    useyn: 'Y'
  }, false)
}

const deleteSelectedRows = () => grid?.getSelectedRows().forEach(row => handleRowAction(row))

const onLineChange = () => {
  processList.value = []
  selectedProg.progcd = ''
  selectedProg.prognm = ''
  grid?.setData([])
}

// 이전공정 도움말 데이터 로드
const progPopupColumns = [
  { title: '코드', field: 'progcd', width: 100, hozAlign: 'center' },
  { title: '공정명', field: 'prognm', widthGrow: 1 }
]
const fetchProgPopupData = async (word: string) => {
  const res = await api.post('/hp00/HP00_000S_STR', {
    gubun: 'G0',
    gbncd: searchData.linecd,
    cmpycd: authStore.cmpycd,
    codenm: word
  })
  return res.data
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); search() }
    else if (key === 's') { e.preventDefault(); save() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HPBA210U') }
  }
}

// 9. 라이프사이클 훅
onMounted(async () => {
  // 라인 옵션 로드
  try {
    const res = await api.get('/hp00/HP00_000S_STR', {
        params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' }
    })
    lineOptions.value = res.data.map((i: any) => ({ linecd: i.linecd, linenm: i.linenm }))
    if (lineOptions.value.length > 0) searchData.linecd = lineOptions.value[0].linecd
  } catch (e) {}

  // 그리드 초기화
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "공정을 선택하세요.", selectable: true,
      columnDefaults: {
        headerHozAlign: 'center', headerSort: false, vertAlign: "middle"
      },
      columns: [
        {
          title: "", width: 40, hozAlign: "center", headerHozAlign: "center",
          formatter: "rowSelection", titleFormatter: "rowSelection"
        },
        { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
            const v = c.getValue()
            if (v === '입력') return '<span class="badge bg-primary">입력</span>'
            if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>'
            if (v === '삭제') return '<span class="badge bg-danger">삭제</span>'
            return ''
        }},
        { title: "코드", field: "mitemcd", width: 100, hozAlign: "center", cssClass: "text-primary fw-bold" },
        { title: "투입자재명", field: "mitemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
        { title: "규격", field: "mitsize", width: 120 },
        { title: "단위", field: "munit", width: 60, hozAlign: "center" },
        { title: "소요량", field: "inqty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => updateRowStatus(cell.getRow()) },
        { title: "LOSS(%)", field: "losrate", width: 80, hozAlign: "right", editor: "number", cellEdited: (cell) => updateRowStatus(cell.getRow()) },
        { title: "이전공정", field: "bprognm", width: 150, cellClick: (e, cell) => {
            const rowData = cell.getRow().getData()
            const checkKind = String(rowData.mastkind || rowData.astkind || '').trim()
            if (checkKind !== '200' && checkKind !== '210') {
                return vAlertError('반제품/제품만 이전공정 선택이 가능합니다.')
            }
            handleOpenHelp('PROG', cell.getRow())
          }
        },
        { title: "사용", field: "useyn", width: 70, hozAlign: "center", editor: "list", editorParams: { values: { "Y": "사용", "N": "미사용" } },
          formatter: (c) => String(c.getValue() || '').trim() === 'Y' ? '<b class="text-primary">Y</b>' : 'N',
          cellEdited: (cell) => updateRowStatus(cell.getRow())
        },
        { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
      ]
    })

    // 🚀 [표준] 초기화 시점 보정
    grid.on("tableBuilt", () => {
        if (searchData.itemcd) fetchData()
    })
  }

  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
  searchStore.removeTab(route.name as string)
})

const formatNumber = (n: any) => Number(n || 0).toLocaleString()
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
.list-group-item-action:hover { background-color: #f8f9fa !important; }
</style>
