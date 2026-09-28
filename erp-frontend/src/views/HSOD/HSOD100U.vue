<!--
	=============================================================
	프로그램명	: 주문등록 (HSOD100U)
	작성일자	: 2025.02.24
	설명        : 영업 주문 마스터/상세 관리 (전용 팝업 적용 및 무결성 수습본)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 🚀 차세대 전용 팝업 라이브러리 (정식 적용) -->
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
  <SaleItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />
  <RegisteredAddrHelp
    v-model:visible="popVisible.addr"
    :custcd="form_02.custcd"
    @confirm="onAddrConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cart-check-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        주문관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문등록 (HSOD100U)</span>
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
                <th class="text-center bg-light small">주문일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="form_01.fromdt"
                    v-model:todt="form_01.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small">주문거래명</th>
                <td>
                  <input
                    v-model="form_01.schcustnm"
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
        <!-- 좌측: 주문 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">주문 목록</div>
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
                    <th class="required bg-light small">주문부서</th>
                    <td colspan="3">
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small">주문번호</th>
                    <td>
                      <input
                        :value="displayOrdNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light small">주문일자</th>
                    <td><input v-model="form_02.ordymd" type="date" class="form-control" tabindex="3" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">거래처</th>
                    <td colspan="3">
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly tabindex="4" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" tabindex="5">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="required bg-light small">납품일자</th>
                    <td><input v-model="form_02.outymd" type="date" class="form-control" tabindex="6" /></td>
                    <th class="required bg-light small">주문종류</th>
                    <td>
                      <select v-model="form_02.ordkind" class="form-select" tabindex="7">
                        <option v-for="item in ordkindData" :key="item.code" :value="item.code">{{ item.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">배송처</th>
                    <td colspan="3">
                      <AddressPopupForm
                        v-model:trancd="form_02.trancd"
                        v-model:postno="form_02.postno"
                        v-model:address="form_02.address"
                        v-model:d_address="form_02.d_address"
                        :tabindex="8"
                        @open-address="handleOpenHelp('ADDR')"
                      />
                    </td>
                    <th class="required bg-light small">결제조건</th>
                    <td>
                      <select v-model="form_02.paycndt" class="form-select" tabindex="11">
                        <option v-for="item in paycndtData" :key="item.code" :value="item.code">{{ item.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small">영업담당</th>
                    <td>
                      <select v-model="form_02.ordemp" class="form-select" tabindex="12">
                        <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small">특기사항</th>
                    <td colspan="7">
                      <input
                        ref="remarkRef"
                        v-model="form_02.remark"
                        class="form-control"
                        tabindex="13"
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
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>주문 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="14"></div>
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
import AddressPopupForm from '@/components/AddressPopupForm.vue'

// 🚀 전용 팝업 임포트
import DeptHelp from '@/components/help/DeptHelp.vue'
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'
import SaleItemHelp from '@/components/help/SaleItemHelp.vue'
import RegisteredAddrHelp from '@/components/help/RegisteredAddrHelp.vue'

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
  item: false,
  addr: false
})
const isSaving = ref(false)

const ordkindData = ref<any[]>([])
const paycndtData = ref<any[]>([])
const userData = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const form_01 = reactive({
  fromdt: firstDay,
  todt: today,
  schcustnm: ''
})

const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd,
  ordym: today.replace(/-/g, '').substring(0, 6),
  ordno: '0000',
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  ordymd: today,
  custcd: '',
  custnm: '',
  outymd: today,
  ordkind: '100',
  paycndt: '110',
  ordemp: authStore.userid,
  remark: '',
  sts: 'Y',
  totsum: 0,
  trancd: '',
  address: '',
  postno: '',
  d_address: ''
})

// 4. 연산 및 감시자
const displayOrdNo = computed(() => {
  if (!form_02.ordno || form_02.ordno === '0000') return ''
  return `${form_02.ordym}-${form_02.ordno}`
})

watch(() => form_02.ordymd, (nv) => {
  if (nv) form_02.ordym = nv.replace(/-/g, '').substring(0, 6)
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 6. 비즈니스 로직 함수 (무결성 최우선)
const initialize = () => {
  resetForm(form_02)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(form_02, {
    cmpycd: authStore.cmpycd,
    ordno: '0000',
    ordymd: today,
    ordym: today.replace(/-/g, '').substring(0, 6),
    outymd: today,
    sts: 'Y',
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    ordemp: authStore.userid,
    ordkind: '100',
    paycndt: '110',
    totsum: 0
  })

  // 🚀 모든 잔상 파괴 후 깨끗한 빈 행 5개 생성
  if (grid2 && grid2.element) {
    grid2.setData([])
    for (let i = 0; i < 5; i++) {
      grid2.addRow({ ordqty: 0, price: 0, ordamt: 0, ordvat: 0, amtsum: 0 }, false)
    }
  }
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

// 7. 전용 팝업 확정 콜백
const onDeptConfirm = (d: any) => {
  form_02.deptcd = d.deptcd
  form_02.deptnm = d.deptnm
}

const onCustConfirm = (d: any) => {
  form_02.custcd = d.custcd
  form_02.custnm = d.custnm
}

const onAddrConfirm = (d: any) => {
  form_02.trancd = d.trancd
  form_02.postno = d.postno
  form_02.address = d.address
  form_02.d_address = d.d_address || ''
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    price: d.outcost || 0,
    ordqty: 1,
    ordamt: d.outcost || 0,
    ordvat: Math.floor((d.outcost || 0) * 0.1),
    amtsum: Math.round((d.outcost || 0) * 1.1),
    _status: '입력',
    _state: 'NEW'
  })
  calcRow(activeRow)
  setTimeout(() => activeRow.getCell("ordqty").edit(), 150)
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
  onRendered(() => {
    input.focus()
    input.select()
  })
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault()
      e.stopPropagation()
      success(input.value)
      handleOpenHelp('ITEM', cell.getRow())
    } else if (e.key === "Tab" && e.shiftKey) {
      if (cell.getRow() === grid2?.getRows()[0]) {
        e.preventDefault()
        cancel()
        remarkRef.value?.focus()
      }
    }
  })
  return container
}

const updateRowStatus = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제' && d.itemcd) {
    row.update({ _status: '수정' })
  }
}

const calcRow = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) return
  const amt = Math.round(Number(d.ordqty || 0) * Number(d.price || 0))
  row.update({ ordamt: amt, ordvat: Math.floor(amt * 0.1), amtsum: Math.round(amt * 1.1) })
  updateRowStatus(row)
}

const calcRowAmt = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) return
  const qty = Number(d.ordqty || 0)
  const amt = Number(d.ordamt || 0)
  const price = qty > 0 ? Math.round(amt / qty) : Number(d.price || 0)
  row.update({ ordvat: Math.floor(amt * 0.1), amtsum: Math.round(amt * 1.1), price: price })
  updateRowStatus(row)
}

const calcRowVat = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) return
  const amt = Number(d.ordamt || 0)
  const vat = Number(d.ordvat || 0)
  row.update({ amtsum: amt + vat })
  updateRowStatus(row)
}

const calcRowTotal = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) return
  const qty = Number(d.ordqty || 0)
  const total = Number(d.amtsum || 0)
  const amt = Math.round(total / 1.1)
  const price = qty > 0 ? Math.round(amt / qty) : Number(d.price || 0)
  row.update({ ordamt: amt, ordvat: total - amt, price: price })
  updateRowStatus(row)
}

// 9. 주요 액션 (조회, 저장, 삭제)
async function search() {
  const res = await api.post('/hsod/HSOD_100U_STR', {
    actkind: 'S1',
    cmpycd: authStore.cmpycd,
    fromdt: form_01.fromdt.replace(/-/g, ''),
    todt: form_01.todt.replace(/-/g, ''),
    custnm: form_01.schcustnm
  })
  grid1?.setData(res.data.map((i: any) => ({
    ...i,
    ordno_full: `${i.ordym}-${i.ordno}`
  })))
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8)
    ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}`
    : today

  Object.assign(form_02, {
    ...row,
    ordymd: fYmd(row.ordymd),
    outymd: fYmd(row.outymd)
  })

  try {
    const res = await api.post('/hsod/HSOD_101U_STR', {
      actkind: 'S0',
      cmpycd: authStore.cmpycd,
      ordym: row.ordym,
      ordno: row.ordno
    })
    grid2?.setData(res.data.map((i: any) => {
      const qty = Number(i.ordqty || 0)
      return {
        ...i,
        _state: 'EXIST',
        _status: '',
        price: qty > 0 ? Math.round(Number(i.ordamt) / qty) : 0,
        amtsum: Number(i.ordamt) + Number(i.ordvat)
      }
    }))
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function save() {
  if (isSaving.value) return
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.')

  // 🚀 무결성 필터: 상태가 있고 품목코드가 있는 '진짜' 데이터만 정밀 추출
  const rawData = grid2?.getData() || []
  const details = rawData.filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    actkind: d._status === '입력' ? 'A0' : (d._status === '삭제' ? 'D0' : 'U0'),
    cmpycd: authStore.cmpycd,
    ordym: form_02.ordym,
    ordno: form_02.ordno,
    itemcd: d.itemcd,
    ordqty: Number(d.ordqty || 0),
    ordamt: Number(d.ordamt || 0),
    ordvat: Number(d.ordvat || 0),
    updemp: authStore.userid
  }))

  if (!details.length && form_02.ordno === '0000') return vAlertError('저장할 유효한 품목 내역이 없습니다.')

  isSaving.value = true
  try {
    const totalAmtSum = details.reduce((acc, cur) => acc + (Number(cur.ordamt) || 0), 0)
    const mst = {
      ...form_02,
      actkind: form_02.ordno === '0000' ? 'A0' : 'U0',
      ordymd: form_02.ordymd.replace(/-/g, ''),
      outymd: form_02.outymd.replace(/-/g, ''),
      totsum: totalAmtSum,
      sts: 'Y',
      updemp: authStore.userid
    }

    await api.post('/hsod/HSOD_100U_SAVE', { mst, dtl: details })
    vAlert('저장되었습니다(Alt+S)')
    initialize()
    search()
  } catch (e) {
    vAlertError('저장 오류')
  } finally {
    isSaving.value = false
  }
}

async function handleFullDelete() {
  if (!form_02.ordno || form_02.ordno === '0000') {
    return vAlertError('조회 후 처리하세요.')
  }
  if (confirm('정말 삭제하시겠습니까?')) {
    await api.post('/hsod/HSOD_100U_SAVE', {
      mst: { ...form_02, actkind: 'D0' },
      dtl: []
    })
    vAlert('삭제되었습니다(Alt+D)')
    initialize()
    search()
  }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))
const addRow = () => grid2?.addRow({ ordqty: 0, price: 0, ordamt: 0, ordvat: 0, amtsum: 0 }, false)

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'ADDR') popVisible.addr = true
  else if (type === 'ITEM') {
    activeRow = target
    popVisible.item = true
  }
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
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSOD100U') }
  }
}

// 10. 라이프사이클 훅
onMounted(() => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns",
    height: "100%",
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "거래처명", field: "custnm", hozAlign: "left" },
      { title: "주문번호", field: "ordno_full", width: 120, cssClass: "fw-bold text-primary", mutator: (v,d) => d.ordym && d.ordno ? `${d.ordym}-${d.ordno}` : v }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns",
    height: "100%",
    selectable: true,
    keybindings: { "navNext": "9" },
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      {
        title: "",
        width: 40,
        hozAlign: "center",
        headerHozAlign: "center",
        formatter: "rowSelection",
        titleFormatter: "rowSelection",
        headerSort: false
      },
      {
        title: "상태",
        field: "_status",
        width: 60,
        hozAlign: "center",
        formatter: (c) => {
          const v = c.getValue()
          if (v === '입력') return '<span class="badge bg-primary">입력</span>'
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>'
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>'
          return ''
      }},
      { title: "품목코드", field: "itemcd", width: 100 },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()), cellClick: (e, cell) => { cell.edit() } },
      { title: "규격", field: "itsize", width: 120 },
      { title: "단위", field: "unit", width: 70 },
      { title: "수량", field: "ordqty", width: 80, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "ordamt", width: 110, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (cell) => calcRowAmt(cell.getRow()) },
      { title: "부가세", field: "ordvat", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (cell) => calcRowVat(cell.getRow()) },
      { title: "합계", field: "amtsum", width: 110, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (cell) => calcRowTotal(cell.getRow()) },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())

  window.addEventListener('keydown', handleGlobalShortcuts)
  api.post('/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '220' }).then(r => ordkindData.value = r.data)
  api.post('/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '300' }).then(r => paycndtData.value = r.data)
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
  api.post('/ha00/HA00_00P_STR', { gubun: 'SD', cmpycd: authStore.cmpycd, gbncd: '', code: '', remark: '' }).then(r => userData.value = r.data)
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
