<!--
	=============================================================
	프로그램명	: 수입발주작업 (HSIP_100U)
	작성일자	: 2025.02.24
	설명        : 수입 발주 마스터/상세 관리 (HSOD100U 표준 적용본)
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
  <!-- 🚀 수입 전용 거래처 검색 적용 -->
  <ImportCustHelp
    v-model:visible="popVisible.cust"
    @confirm="onCustConfirm"
    @close="restoreFocus"
  />
  <!-- 🚀 구매시스템 전용 품목 검색 적용 -->
  <PurchItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-globe-americas me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입발주작업 (HSIP_100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" tabindex="-1">저장(S)</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" tabindex="-1">삭제(D)</button>
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
                <th class="text-center bg-light small">발주일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="searchForm.fromdt"
                    v-model:todt="searchForm.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small">거래처명</th>
                <td>
                  <input
                    v-model="searchForm.custnm"
                    class="form-control form-control-sm"
                    placeholder="거래처명 검색"
                    @keyup.enter="search"
                    tabindex="102"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 발주 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">발주 목록</div>
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
                    <th class="required bg-light small">발주부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="formData.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small text-center">PO No</th>
                    <td>
                      <input
                        v-model="formData.fileno"
                        class="form-control bg-white text-primary fw-bold text-center"
                        placeholder="PO 번호 입력"
                        tabindex="3"
                      />
                    </td>
                    <th class="required bg-light small text-center">발주일자</th>
                    <td><input v-model="formData.issymd" type="date" class="form-control" tabindex="4" /></td>
                    <th class="required bg-light small text-center">수입구분</th>
                    <td>
                      <select v-model="formData.imptgbn" class="form-select" tabindex="5">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.imptgbn" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small text-center">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.custnm" class="form-control" readonly tabindex="6" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" tabindex="7">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="required bg-light small text-center">원&nbsp;&nbsp;산&nbsp;&nbsp;지</th>
                    <td>
                      <select v-model="formData.nacd" class="form-select" tabindex="8">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.nacd" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center">선&nbsp;&nbsp;적&nbsp;&nbsp;항</th>
                    <td>
                      <select v-model="formData.shipport" class="form-select" tabindex="9">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.shipport" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center">도&nbsp;&nbsp;착&nbsp;&nbsp;항</th>
                    <td>
                      <select v-model="formData.arvport" class="form-select" tabindex="10">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.arvport" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small text-center">결제조건</th>
                    <td>
                      <select v-model="formData.paycond" class="form-select" tabindex="11">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.paycond" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center">가격조건</th>
                    <td>
                      <select v-model="formData.pricond" class="form-select" tabindex="12">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.pricond" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center">통화/환율</th>
                    <td colspan="3">
                      <div class="d-flex gap-1">
                        <select v-model="formData.currcd" class="form-select" style="width: 100px;" tabindex="13">
                          <option value="">선택</option>
                          <option v-for="opt in comboData.currcd" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                        </select>
                        <input v-model="formData.frgnrate" type="number" class="form-control text-end" step="0.01" @input="updateTotals" tabindex="14" />
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">소요비용</th>
                    <td>
                      <div class="d-flex gap-1 align-items-center">
                        <span class="small text-nowrap">L/C:</span>
                        <input v-model="formData.lcamt" type="number" class="form-control text-end" @input="updateTotals" tabindex="15" />
                        <span class="small text-nowrap">통관:</span>
                        <input v-model="formData.xtamt" type="number" class="form-control text-end" @input="updateTotals" tabindex="16" />
                      </div>
                    </td>
                    <th class="bg-light small text-center">B/L 합계</th>
                    <td colspan="3">
                      <div class="d-flex gap-1 align-items-center">
                        <span class="small text-nowrap">외화:</span>
                        <input :value="formatNumber(formData.frgnamt)" class="form-control text-end bg-light" readonly tabindex="-1" />
                        <span class="small text-nowrap">원화:</span>
                        <input :value="formatNumber(formData.wonamt)" class="form-control text-end bg-light" readonly tabindex="-1" />
                      </div>
                    </td>
                    <th class="bg-light small text-center">총합계</th>
                    <td><input :value="formatNumber(formData.costsum)" class="form-control text-end bg-light fw-bold text-primary" readonly tabindex="-1" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">특기사항</th>
                    <td colspan="7">
                      <input
                        ref="remarkRef"
                        v-model="formData.bigo"
                        class="form-control"
                        placeholder="비고 입력"
                        tabindex="17"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 품목 그리드 영역 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>수입 발주 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="mainGridRef" class="tabulator-instance flex-grow-1" tabindex="18"></div>
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
import ImportCustHelp from '@/components/help/ImportCustHelp.vue'
import PurchItemHelp from '@/components/help/PurchItemHelp.vue'

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
  cust: false,
  item: false
})
const isSaving = ref(false)

const comboData = reactive<any>({
  imptgbn: [], nacd: [], shipport: [], arvport: [], currcd: [], paycond: [], pricond: []
})
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchForm = reactive({
  fromdt: firstDay,
  todt: today,
  custnm: ''
})

const formData = reactive<any>({
  _isNew: true,
  cmpycd: authStore.cmpycd,
  fileno: '',
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  issymd: today,
  custcd: '',
  custnm: '',
  nacd: '',
  currcd: '',
  frgnrate: 1350,
  pricond: '',
  paycond: '',
  imptgbn: '',
  shipport: '',
  arvport: '',
  bigo: '',
  wonamt: 0,
  frgnamt: 0,
  lcamt: 0,
  xtamt: 0,
  costsum: 0
})

// 4. 연산 및 감시자
const displayIoNo = computed(() => {
  return formData.fileno || ''
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const mainGridRef = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let mainGrid: Tabulator | null = null

// 6. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(formData)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(formData, {
    _isNew: true,
    cmpycd: authStore.cmpycd,
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    issymd: today,
    frgnrate: 1350,
    wonamt: 0,
    frgnamt: 0,
    lcamt: 0,
    xtamt: 0,
    costsum: 0,
    fileno: ''
  })

  // 🚀 잔상 물리적 파괴 후 깨끗한 빈 행 5개 생성
  if (mainGrid && mainGrid.element) {
    mainGrid.setData([])
    for (let i = 0; i < 5; i++) {
      mainGrid.addRow({ qty: 0, price: 0, amt: 0 }, false)
    }
  }
  updateTotals()
  nextTick(() => firstFocusRef.value?.focus())
}

const restoreFocus = () => {
  nextTick(() => {
    if (lastActiveElement.value) {
      const el = lastActiveElement.value
      el.focus()
      setTimeout(() => {
        const currentIdx = el.tabIndex
        if (currentIdx > 0) {
          const nextEl = document.querySelector(`[tabindex="${currentIdx + 1}"]`) as HTMLElement
          if (nextEl) {
            nextEl.focus()
            if (nextEl instanceof HTMLInputElement) nextEl.select()
          }
        }
      }, 100)
    }
  })
}

// 7. 팝업 확정 콜백
const onDeptConfirm = (d: any) => {
  formData.deptcd = d.deptcd
  formData.deptnm = d.deptnm
}

const onCustConfirm = (d: any) => {
  formData.custcd = d.custcd
  formData.custnm = d.custnm
  formData.nacd = d.nacd || ''
  formData.shipport = d.shipport || ''
  formData.arvport = d.arvport || ''
  formData.currcd = d.currcd || ''
  formData.paycond = d.paycond || ''
  formData.pricond = d.pricond || ''
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    price: d.incost || 0,
    qty: 1,
    amt: d.incost || 0,
    _status: '입력',
    _state: 'NEW'
  })
  calcRow(activeRow)
  setTimeout(() => activeRow.getCell("qty").edit(), 150)
}

// 8. 그리드 에디터 및 계산 로직
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

const calcRow = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) return
  const amt = Math.floor(Number(d.qty || 0) * Number(d.price || 0))
  row.update({ amt: amt })
  if (d._state === 'EXIST' && d._status !== '삭제') {
    row.update({ _status: '수정' })
  }
  updateTotals()
}

const updateTotals = () => {
  const data = mainGrid?.getData().filter(i => i.itemcd && i._status !== '삭제') || []
  const frgnAmt = data.reduce((acc, cur: any) => acc + (Number(cur.amt) || 0), 0)
  formData.frgnamt = frgnAmt
  formData.wonamt = Math.floor(frgnAmt * (Number(formData.frgnrate) || 0))
  formData.costsum = (Number(formData.lcamt) || 0) + (Number(formData.xtamt) || 0) + (Number(formData.wonamt) || 0)
}

// 9. 주요 액션 (조회, 저장, 삭제)
async function search() {
  const res = await api.post('/hsip/HSIP_100U_STR', {
    actkind: 'L0',
    cmpycd: authStore.cmpycd,
    fromdt: searchForm.fromdt.replace(/-/g, ''),
    todt: searchForm.todt.replace(/-/g, ''),
    custnm: searchForm.custnm,
    frgnrate: 0, lcamt: 0, wonamt: 0, frgnamt: 0, xtamt: 0
  })
  const list = res.data?.data || res.data || []
  grid1?.setData(list)
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(fileNo: string) {
  try {
    const res = await api.post('/hsip/HSIP_100U_STR', {
      fileno: fileNo, actkind: 'S0', cmpycd: authStore.cmpycd,
      frgnrate: 0, lcamt: 0, wonamt: 0, frgnamt: 0, xtamt: 0
    })

    if (res.data?.length) {
      const master = res.data[0]
      Object.assign(formData, {
        ...master,
        issymd: master.issymd && master.issymd.length === 8
          ? `${master.issymd.substring(0, 4)}-${master.issymd.substring(4, 6)}-${master.issymd.substring(6, 8)}`
          : master.issymd,
        _isNew: false
      })

      const itemRes = await api.post('/hsip/HSIP_101U_STR', {
        fileno: fileNo, actkind: 'S0', qty: 0, amt: 0
      })
      const data = (itemRes.data || []).map((i: any) => ({ ...i, _state: 'EXIST', _status: '' }))
      // 🚀 [표준] 조회 시에는 실데이터만 출력
      mainGrid?.setData(data)
      updateTotals()
    }
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function save() {
  if (!formData.custcd) return vAlertError('거래처를 선택하세요.')
  if (!formData.fileno) return vAlertError('PO번호를 입력하세요.')

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const details = (mainGrid?.getData() || []).filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    actkind: d._status === '입력' ? 'A0' : (d._status === '삭제' ? 'D0' : 'U0'),
    cmpycd: authStore.cmpycd || '',
    fileno: formData.fileno || '',
    prowno: d.prowno || '',
    itemcd: d.itemcd || '',
    itsize: d.itsize || '',
    unit: d.unit || '',
    qty: Number(d.qty) || 0,
    amt: Number(d.amt) || 0,
    updemp: authStore.userid || ''
  }))

  if (!details.length && formData._isNew === true) return vAlertError('품목을 추가하세요.')

  if (!confirm('저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const mstData = {
        ...formData,
        actkind: formData._isNew ? 'A0' : 'U0',
        issymd: (formData.issymd || '').replace(/-/g, ''),
        frgnrate: Number(formData.frgnrate) || 0,
        lcamt: Number(formData.lcamt) || 0,
        wonamt: Number(formData.wonamt) || 0,
        frgnamt: Number(formData.frgnamt) || 0,
        xtamt: Number(formData.xtamt) || 0,
        updemp: authStore.userid || ''
    }

    const res = await api.post('/hsip/HSIP_100U_SAVE', { mst: mstData, dtl: details })
    if (res.data) {
        vAlert('저장되었습니다(Alt+S)')
        const newFileNo = res.data.fileno || mstData.fileno
        search()
        fetchDetail(newFileNo)
    }
  } catch (e) {
    vAlertError('저장 실패')
  } finally {
    isSaving.value = false
  }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
  updateTotals()
}

const addRow = () => mainGrid?.addRow({ qty: 0, price: 0, amt: 0 }, false)
const deleteSelectedRows = () => mainGrid?.getSelectedRows().forEach(row => handleRowAction(row))

async function handleFullDelete() {
  if (!formData.fileno || formData.fileno === '0000') return vAlertError('조회 후 처리하세요.')
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await api.post('/hsip/HSIP_100U_STR', {
        fileno: formData.fileno, actkind: 'D0', cmpycd: authStore.cmpycd,
        frgnrate: 0, lcamt: 0, wonamt: 0, frgnamt: 0, xtamt: 0, updemp: authStore.userid
      })
      vAlert('삭제되었습니다.'); initialize(); search()
    } catch (e) { vAlertError('삭제 실패') }
  }
}

function handleRemarkTab(e: KeyboardEvent) {
  if (e.key === 'Tab' && !e.shiftKey) {
    e.preventDefault()
    if (mainGrid) {
      const rows = mainGrid.getRows()
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
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSIP_100U') }
  }
}

const formatNumber = (n: any) => Number(n || 0).toLocaleString()

// 10. 라이프사이클 훅
onMounted(async () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%",
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "발주일자", field: "issymd", hozAlign: "center", width: 90, formatter: (c) => {
          const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "PO No", field: "fileno", hozAlign: "center", width: 100, cssClass: "fw-bold text-primary" },
      { title: "수입처", field: "custnm", hozAlign: "left", minWidth: 100, cssClass: "fw-bold text-primary" }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData().fileno))

  mainGrid = new Tabulator(mainGridRef.value!, {
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
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 80, hozAlign: "center" },
      { title: "수량", field: "qty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "amt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  mainGrid.on("tableBuilt", () => initialize())

  // 공통 콤보 로드
  const loadCombo = async (gbncd: string, target: string) => {
    const res = await api.post('/hs00/HS00_000S_STR', { gubun: 'E2', cmpycd: authStore.cmpycd, gbncd: gbncd })
    comboData[target] = res.data.map((i: any) => ({ code: i.code || '', cdnm: i.codenm || i.cdnm || '' }))
  }

  await Promise.all([
    loadCombo('311', 'imptgbn'), loadCombo('305', 'nacd'), loadCombo('308', 'shipport'),
    loadCombo('309', 'arvport'), loadCombo('310', 'currcd'), loadCombo('312', 'paycond'), loadCombo('314', 'pricond')
  ])

  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
  searchStore.removeTab(route.name as string)
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
</style>
