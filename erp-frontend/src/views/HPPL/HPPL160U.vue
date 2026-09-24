<!--
	=============================================================
	프로그램명	: 외주생산계획 (HPPL160U)
	작성일자	: 2025.03.14
	설명        : 외주 생산 요청 자료 등록 및 관리 (HSOD100U 무결성 표준 적용본)
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
  <!-- 🚀 생산시스템 전용 거래처(외주가공처) 검색 적용 -->
  <ProdCustHelp
    v-model:visible="popVisible.cust"
    @confirm="onCustConfirm"
    @close="restoreFocus"
  />
  <!-- 🚀 생산시스템 전용 품목 검색 적용 -->
  <ProdItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-truck me-2 text-primary" style="font-size: 18px;"></i>
        생산관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        생산계획 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주생산계획 (HPPL160U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="fetchData" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="saveData" tabindex="-1">저장(S)</button>
      </div>
    </div>

    <!-- [2] 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 상단 조회 조건 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center small">요청일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1 px-2">
                    <input
                        v-model="searchForm.frymd"
                        type="date"
                        class="form-control form-control-sm"
                        style="width: 140px;"
                        tabindex="101"
                    />
                    <span class="text-muted">~</span>
                    <input
                        v-model="searchForm.toymd"
                        type="date"
                        class="form-control form-control-sm"
                        style="width: 140px;"
                        tabindex="102"
                    />
                  </div>
                </td>
                <th class="bg-light text-center border-start small">계획구분</th>
                <td>
                  <select v-model="searchForm.gubun" class="form-select form-select-sm" style="width: 120px;" disabled tabindex="-1">
                    <option value="300">외주계획</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 하단 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>외주 생산 요청 내역</span>
          <div class="btn-group-erp d-flex gap-1">
             <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
             <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1" tabindex="10"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed, onUnmounted } from 'vue'
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
import ProdCustHelp from '@/components/help/ProdCustHelp.vue'
import ProdItemHelp from '@/components/help/ProdItemHelp.vue'

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
  dept: false,
  cust: false,
  item: false
})
const isSaving = ref(false)
let activeRow: any = null

// 3. 데이터 모델링
const searchForm = reactive({
  frymd: firstDay,
  toymd: today,
  gubun: "300"
})

// 4. 그리드 참조
const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

// 5. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(searchForm)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(searchForm, {
    frymd: firstDay,
    toymd: today,
    gubun: "300"
  })

  // 🚀 잔상 물리적 파괴 후 깨끗한 빈 행 5개 생성 (Ghost Data 차단)
  if (mainGrid && mainGrid.element) {
    mainGrid.setData([])
    for (let i = 0; i < 5; i++) {
      mainGrid.addRow({
        ordymd: today,
        ordqty: 0,
        napgiymd: today
      }, false)
    }
  }
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
  // 필요 시 구현
}

const onCustConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    custcd: d.custcd,
    custnm: d.custnm
  })
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    price: d.outprice || 0,
    _status: '입력',
    _state: 'NEW'
  })
  calcRow(activeRow)
  setTimeout(() => activeRow.getCell("ordqty").edit(), 150)
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

const calcRow = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제' && d.itemcd) {
    row.update({ _status: '수정' })
  }
}

// 8. 주요 액션 (조회, 저장, 삭제)
const fetchData = async () => {
  try {
    const res = await api.get('/product/pdplan/request-list', {
      params: {
        frymd: searchForm.frymd.replace(/-/g, ''),
        toymd: searchForm.toymd.replace(/-/g, ''),
        gubun: searchForm.gubun
      }
    })
    const processedData = (res.data || []).map((n: any) => ({
      ...n,
      _status: '',
      _state: 'EXIST'
    }))
    // 🚀 [표준] 조회 시에는 실데이터만 출력
    mainGrid?.setData(processedData)
    vAlert('조회되었습니다(Alt+F)')
  } catch (e) {
    vAlertError('조회 중 오류가 발생했습니다.')
  }
}

const saveData = async () => {
  if (isSaving.value) return

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const allData = mainGrid?.getData() || []
  const details = allData.filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map(i => {
    return {
      ...i,
      ordymd: String(i.ordymd).replace(/-/g, ''),
      napgiymd: String(i.napgiymd).replace(/-/g, ''),
      cmpycd: authStore.cmpycd,
      updemp: authStore.userid,
      gubun: searchForm.gubun
    }
  })

  if (details.length === 0) return vAlertError('변경사항이 없습니다.')

  if (!confirm('외주 요청 자료를 저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const res = await api.post('/product/pdplan/request-save', details)
    const out = Array.isArray(res.data) ? res.data[0] : res.data
    const isSuccess = out?.result === 'OK' || out?.RESULT === 'OK' || out?.RET_YN === 'N' || out?.ret_yn === 'N'

    if (isSuccess) {
      vAlert(out.msg || out.MSG || out.ret_msg || '저장되었습니다(Alt+S)')
      fetchData()
    } else {
      vAlertError(out.msg || out.MSG || out.ret_msg || '저장 처리 중 오류가 발생했습니다.');
    }
  } catch (e) {
    vAlertError('저장 중 오류 발생')
  } finally {
    isSaving.value = false
  }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') { activeRow = target; popVisible.cust = true }
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = () => {
  mainGrid?.addRow({
    _status: '입력',
    _state: 'NEW',
    ordymd: today,
    napgiymd: today,
    ordqty: 0,
    gubun: searchForm.gubun
  }, false)
}

const deleteSelectedRows = () => mainGrid?.getSelectedRows().forEach(row => handleRowAction(row))

const handleGlobalShortcuts = (e: KeyboardEvent) => {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); fetchData() }
    else if (key === 's') { e.preventDefault(); saveData() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HPPL160U') }
  }
}

// 9. 라이프사이클 훅
onMounted(() => {
  nextTick(() => {
    if (!mainGridRef.value) return
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: true,
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
        { title: '요청일자', field: 'ordymd', width: 110, hozAlign: 'center', editor: 'date',
          formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;
          },
          cellEdited: (cell) => calcRow(cell.getRow())
        },
        { title: '외주가공처', field: 'custnm', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold text-primary', cellClick: (e, cell) => handleOpenHelp('CUST', cell.getRow()) },
        { title: '제품명', field: 'itemnm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
        { title: '규격', field: 'itsize', width: 120 },
        { title: '단위', field: 'unit', width: 60, hozAlign: 'center' },
        { title: '요청수량', field: 'ordqty', width: 90, hozAlign: 'right', editor: 'number', formatter: 'money', formatterParams: { precision: 0 }, cellEdited: (cell) => calcRow(cell.getRow()) },
        { title: '완료요청일', field: 'napgiymd', width: 110, hozAlign: 'center', editor: 'date',
          formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;
          },
          cellEdited: (cell) => calcRow(cell.getRow())
        },
        { title: '비고', field: 'bigo', widthGrow: 1, editor: 'input', cellEdited: (cell) => calcRow(cell.getRow()) },
        { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
      ]
    })

    mainGrid.on("tableBuilt", () => initialize())
    fetchData()
  })

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
</style>
