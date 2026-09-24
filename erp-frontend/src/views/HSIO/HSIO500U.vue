<!--
	=============================================================
	프로그램명	: 무주문 출고등록 (HSIO500U)
	작성일자	: 2025.02.24
	설명        : 영업 주문 없이 직접 출고 등록 (HSOD100U 표준 강제 적용본)
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
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">무주문 출고등록 (HSIO500U)</span>
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
                <th class="text-center bg-light small">출고일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="form_01.fromdt"
                    v-model:todt="form_01.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small">거래처명</th>
                <td>
                  <input
                    v-model="form_01.custnm"
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
        <!-- 좌측: 출고 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">출고 목록</div>
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
                    <th class="required bg-light small">판매부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small text-center">출고번호</th>
                    <td>
                      <input
                        :value="displayIoNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light small text-center">출고일자</th>
                    <td><input v-model="form_02.ioymd" type="date" class="form-control" tabindex="3" /></td>
                    <th class="required bg-light small text-center">출고창고</th>
                    <td>
                      <select v-model="form_02.whcd" class="form-select" tabindex="4">
                        <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">거래처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly tabindex="5" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" tabindex="6">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small text-center">여신잔액</th>
                    <td><input :value="formatNumber(form_02.janamt)" class="form-control bg-light text-end" readonly tabindex="-1" /></td>
                    <th class="bg-light small text-center">여신기한</th>
                    <td><input v-model="form_02.rcvdd" class="form-control bg-light text-center" readonly tabindex="-1" /></td>
                    <th class="required bg-light small text-center">영업담당</th>
                    <td>
                      <select v-model="form_02.sale_userid" class="form-select" tabindex="7">
                        <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">배송처</th>
                    <td colspan="3">
                      <AddressPopupForm
                        v-model:trancd="form_02.addrcd"
                        v-model:postno="form_02.postno"
                        v-model:address="form_02.address"
                        v-model:d_address="form_02.d_address"
                        :tabindex="8"
                        @open-address="handleOpenHelp('ADDR')"
                      />
                    </td>
                    <th class="bg-light small text-center">배송담당</th>
                    <td>
                      <select v-model="form_02.trnemp" class="form-select" tabindex="11">
                        <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center">현금조건</th>
                    <td>
                      <select v-model="form_02.paycndt" class="form-select" tabindex="12">
                        <option v-for="item in paycndtData" :key="item.code" :value="item.code">{{ item.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">특기사항</th>
                    <td colspan="3">
                      <input
                        ref="remarkRef"
                        v-model="form_02.remark"
                        class="form-control"
                        tabindex="13"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                    <th class="bg-light small text-center">만기일자</th>
                    <td><input v-model="form_02.endymd" type="date" class="form-control" tabindex="14" /></td>
                    <th class="bg-light small text-center">합계금액</th>
                    <td><input :value="formatNumber(form_02.totsum)" class="form-control bg-light text-end fw-bold" readonly tabindex="-1" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 품목 그리드 영역 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>출고 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="15"></div>
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

// 🚀 [표준] 전용 팝업 임포트
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

const whOptions = ref<any[]>([])
const userData = ref<any[]>([])
const paycndtData = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const form_01 = reactive({
  fromdt: firstDay,
  todt: today,
  custnm: ''
})

const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd,
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  iogbn: '200',
  ioym: '',
  iono: '',
  ioymd: today,
  whcd: '',
  custcd: '',
  custnm: '',
  iotype: '100',
  sale_userid: authStore.userid,
  trnemp: authStore.userid,
  paycndt: '100',
  postno: '',
  address: '',
  d_address: '',
  addrcd: '',
  area: '',
  endymd: today,
  remark: '',
  totsum: 0,
  cfmyn: '',
  janamt: 0,
  rcvdd: ''
})

// 4. 연산 및 감시자
const displayIoNo = computed(() => {
  if (!form_02.ioym || !form_02.iono) return ''
  return `${form_02.ioym}-${form_02.iono}`
})

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !form_02.ioymd) return false
  return form_02.ioymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 6. 비즈니스 로직 함수 (HSOD100U 표준 강제 적용)
const initialize = () => {
  resetForm(form_02)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(form_02, {
    cmpycd: authStore.cmpycd,
    ioymd: today,
    endymd: today,
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    sale_userid: authStore.userid,
    trnemp: authStore.userid,
    paycndt: '100',
    iogbn: '200',
    iotype: '100',
    totsum: 0,
    janamt: 0,
    rcvdd: '',
    ioym: '',
    iono: ''
  })

  if (whOptions.value.length > 0) form_02.whcd = whOptions.value[0].whcd

  // 🚀 [표준] 잔상 물리적 파괴 후 깨끗한 빈 행 5개 생성
  if (grid2 && grid2.element) {
    grid2.setData([])
    for (let i = 0; i < 5; i++) {
      grid2.addRow({ ioqty: 0, price: 0, ioamt: 0, iovat: 0, sumamt: 0 }, false)
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
  // 여신 정보 즉시 연동
  api.post('/hs00/HS00_150S_STR', { cmpycd: authStore.cmpycd, custnm: d.custnm }).then(r => {
    if (r.data?.length) {
      const n = r.data[0]
      form_02.rcvdd = n.rcvdd || n.RCVDD || ''
      form_02.janamt = n.janamt || n.JANAMT || 0
    }
  })
}

const onAddrConfirm = (d: any) => {
  form_02.addrcd = d.trancd
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
    ioqty: 1,
    ioamt: d.outcost || 0,
    iovat: Math.floor((d.outcost || 0) * 0.1),
    sumamt: Math.round((d.outcost || 0) * 1.1),
    _status: '신규',
    _state: 'NEW'
  })
  calcRow(activeRow)
  setTimeout(() => activeRow.getCell("ioqty").edit(), 150)
}

// 8. 그리드 에디터 및 계산
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
    } else if (e.key === "Tab" && e.shiftKey) {
      if (cell.getRow() === grid2?.getRows()[0]) {
        e.preventDefault(); cancel(); remarkRef.value?.focus()
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
  const amt = Math.round(Number(d.ioqty || 0) * Number(d.price || 0))
  const vat = Math.round(amt * 0.1)
  row.update({ ioamt: amt, iovat: vat, sumamt: amt + vat })
  updateRowStatus(row)
  updateTotalSum()
}

const updateTotalSum = () => {
  const data = grid2?.getData().filter(r => r.itemcd && r._status !== '삭제') || []
  form_02.totsum = data.reduce((sum, r) => sum + Number(r.sumamt || 0), 0)
}

// 9. 주요 액션 (조회, 저장, 삭제)
async function search() {
  const res = await api.post('/hsio/HSIO_500U_STR', {
    actkind: 'L',
    cmpycd: authStore.cmpycd,
    fromdt: form_01.fromdt.replace(/-/g, ''),
    todt: form_01.todt.replace(/-/g, ''),
    custnm: form_01.custnm,
    iogbn: '200'
  })
  grid1?.setData(res.data || [])
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  Object.assign(form_02, row)
  try {
    // 🚀 [해결] 디테일 API(/hsio/HSIO_501U_STR) 규격에 맞게 파라미터를 배열[{...}] 형태 생성
    const res = await api.post(`/hsio/HSIO_501U_STR`, [{
      actkind: 'S',
      cmpycd: authStore.cmpycd,
      iogbn: '200',
      ioym: row.ioym,
      iono: row.iono,
      iorowno: '',
      deptcd: row.deptcd || '',
      custcd: row.custcd || '',
      whcd: row.whcd || '',
      area: row.area || '',
      saleuserid: row.sale_userid || authStore.userid,
      ioymd: row.ioymd || '',
      iotype: row.iotype || '100',
      itemcd: '', itsize: '', unit: '',
      ioqty: 0, ioamt: 0, iovat: 0, cfmyn: 'Y',
      updemp: authStore.userid
    }])

    const displayData = (res.data || []).map((i: any) => {
      const ioqty = Number(i.ioqty || 0); const ioamt = Number(i.ioamt || 0)
      return { ...i, ioqty, ioamt, price: ioqty !== 0 ? Math.round(ioamt / ioqty) : Number(i.price || 0), iovat: Number(i.iovat || 0), sumamt: ioamt + Number(i.iovat || 0), _state: 'EXIST', _status: '' }
    })

    // 🚀 [표준] 조회 시에는 빈 칸 없이 실데이터만 깔끔하게 출력
    grid2?.setData(displayData)
    updateTotalSum()
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function save() {
  if (isSaving.value) return
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.')

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const details = (grid2?.getData() || []).filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    actkind: d._status === '신규' ? 'A' : (d._status === '삭제' ? 'D' : 'U'),
    cmpycd: authStore.cmpycd,
    iogbn: '200',
    ioym: form_02.ioym,
    iono: form_02.iono,
    iorowno: d.iorowno || '',
    itemcd: d.itemcd,
    ioqty: Number(d.ioqty || 0),
    ioamt: Number(d.ioamt || 0),
    iovat: Number(d.iovat || 0),
    updemp: authStore.userid
  }))

  if (!details.length && !form_02.iono) return vAlertError('저장할 내역이 없습니다.')

  isSaving.value = true
  try {
    const ioymd = form_02.ioymd.replace(/-/g, '')
    const totalAmtSum = details.reduce((acc, cur) => acc + (Number(cur.ioamt) || 0), 0)
    const mst = { ...form_02, actkind: form_02.iono ? 'U' : 'A', ioym: ioymd.substring(0, 6), ioymd, endymd: form_02.endymd.replace(/-/g, ''), totsum: totalAmtSum, saleuserid: form_02.sale_userid, updemp: authStore.userid, iogbn: '200', iotype: '100' }
    await api.post('/hsio/HSIO_500U_SAVE', { mst, dtl: details })
    vAlert('저장되었습니다(Alt+S)'); initialize(); search()
  } catch (e) { vAlertError('저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'ADDR') popVisible.addr = true
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
  updateTotalSum()
}

const addRow = () => grid2?.addRow({ ioqty: 0, price: 0, ioamt: 0, iovat: 0, sumamt: 0, _status: '신규', _state: 'NEW' }, false)
const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

async function handleFullDelete() {
  if (!form_02.iono || form_02.iono === '0000') return vAlertError('조회 후 처리하세요.')
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await api.post('/hsio/HSIO_500U_SAVE', { mst: { ...form_02, actkind: 'D' }, dtl: [] })
      vAlert('삭제되었습니다.'); initialize(); search()
    } catch (e) { vAlertError('삭제 실패') }
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
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSIO500U') }
  }
}

const fetchWhOptions = async () => {
  const res = await api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
  whOptions.value = (res.data || []).map((i: any) => ({ whcd: String(i.whcd || '').trim(), whnm: String(i.whnm || '').trim() }))
  if (whOptions.value.length > 0 && !form_02.whcd) form_02.whcd = whOptions.value[0].whcd
}

const formatNumber = (n: any) => Number(n || 0).toLocaleString()

// 10. 라이프사이클 훅
onMounted(async () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%",
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "일자", field: "ioymd", hozAlign: "center", width: 90 },
      { title: "출고번호", field: "iono", hozAlign: "center", width: 100, cssClass: "fw-bold text-primary",
        formatter: (cell) => { const d = cell.getRow().getData(); return d.ioym && d.iono ? `${d.ioym}-${d.iono}` : (d.iono || ""); }
      },
      { title: "거래처", field: "custnm", hozAlign: "left" }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", selectable: true,
    keybindings: { "navNext": "9" }, columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "", width: 40, hozAlign: "center", headerHozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '신규') return '<span class="badge bg-primary">신규</span>'
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>'
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>'
          return ''
      }},
      { title: "품목코드", field: "itemcd", width: 100 },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()), cellClick: (e, cell) => { cell.edit() } },
      { title: "규격", field: "itsize", width: 120 },
      { title: "단위", field: "unit", width: 70 },
      { title: "수량", field: "ioqty", width: 80, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "ioamt", width: 110, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "부가세", field: "iovat", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "합계", field: "sumamt", width: 110, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())

  await fetchWhOptions()
  api.post('/ha00/HA00_00P_STR', { gubun: 'SD', cmpycd: authStore.cmpycd, gbncd: '', code: '', remark: '' }).then(r => userData.value = r.data)
  api.post('/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '300', code: '' }).then(r => paycndtData.value = r.data)
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => { window.removeEventListener('keydown', handleGlobalShortcuts); searchStore.removeTab(route.name as string) })
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
