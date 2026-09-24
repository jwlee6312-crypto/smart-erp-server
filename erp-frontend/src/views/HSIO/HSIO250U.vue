<!--
	=============================================================
	프로그램명	: 덤입고작업 (HSIO250U)
	작성일자	: 2025.02.24
	설명        : 덤입고(무상입고) 마스터/상세 관리 (HSOD100U 표준 적용본)
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
  <PurchItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-gift-fill me-2 text-primary" style="font-size: 18px;"></i>
        구매관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        입고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">덤입고작업 (HSIO250U)</span>
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
                <th class="text-center bg-light small">입고일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="searchForm.fromdt"
                    v-model:todt="searchForm.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small">매입처명</th>
                <td>
                  <input
                    v-model="searchForm.schcustnm"
                    class="form-control form-control-sm"
                    placeholder="매입처 검색"
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
        <!-- 좌측: 입고 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">입고 목록</div>
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
                    <th class="required bg-light small">입고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="formData.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" :disabled="isClosed" tabindex="2">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small text-center">입고번호</th>
                    <td>
                      <input
                        :value="displayIoNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light small text-center">입고일자</th>
                    <td><input v-model="formData.ioymd" type="date" class="form-control" :readonly="isClosed" tabindex="3" /></td>
                    <th class="required bg-light small text-center">입고창고</th>
                    <td>
                      <select v-model="formData.whcd" class="form-select" :disabled="isClosed" tabindex="4">
                        <option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">매&nbsp;&nbsp;입&nbsp;&nbsp;처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.custnm" class="form-control" readonly tabindex="5" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" :disabled="isClosed" tabindex="6">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small text-center">작업자</th>
                    <td>
                      <select v-model="formData.userid" class="form-select" tabindex="7">
                        <option v-for="user in userData" :key="user.userid" :value="user.userid">{{ user.usernm }}</option>
                      </select>
                    </td>
                    <th class="bg-light small text-center">특기사항</th>
                    <td colspan="3">
                      <input
                        ref="remarkRef"
                        v-model="formData.remark"
                        class="form-control"
                        :readonly="isClosed"
                        tabindex="8"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 하단: 덤입고 품목 리스트 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>덤입고 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="isClosed" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="isClosed" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="9"></div>
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
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'
import PurchItemHelp from '@/components/help/PurchItemHelp.vue'

// 1. 공통 상태 및 훅 초기화
const authStore = useAuthStore()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const manualStore = useManualStore()
const searchStore = useSearchStore()
const route = useRoute()

// 2. 참조 및 상태 관리 변수 (완전 독립 구조)
const firstFocusRef = ref<HTMLInputElement | null>(null)
const remarkRef = ref<HTMLInputElement | null>(null)
const lastActiveElement = ref<HTMLElement | null>(null)
const popVisible = reactive({
  dept: false,
  cust: false,
  item: false
})
const isSaving = ref(false)

const whOptions = ref<any[]>([])
const userData = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchForm = reactive({
  fromdt: firstDay,
  todt: today,
  schcustnm: ''
})

const formData = reactive<any>({
  cmpycd: authStore.cmpycd,
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  iono: '',
  ioymd: today,
  ioym: today.substring(0, 7).replace('-', ''),
  custcd: '',
  custnm: '',
  whcd: '100',
  remark: '',
  userid: authStore.userid,
  usernm: authStore.usernm,
  pkunityn: 'N',
  astkind: '2',
  gubun: '1',
  totsum: 0
})

// 4. 연산 및 감시자
const displayIoNo = computed(() => {
  if (!formData.iono || formData.iono === '0000') return ''
  return `${formData.ioym}-${formData.iono}`
})

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !formData.ioymd) return false
  return formData.ioymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 6. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(formData)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(formData, {
    cmpycd: authStore.cmpycd,
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    ioymd: today,
    ioym: today.substring(0, 7).replace('-', ''),
    whcd: '100',
    gubun: '1',
    astkind: '2',
    iono: '',
    pkunityn: 'N',
    userid: authStore.userid,
    totsum: 0
  })

  if (whOptions.value.length > 0) formData.whcd = whOptions.value[0].code

  // 🚀 잔상 물리적 파괴 후 깨끗한 빈 행 5개 생성
  if (grid2 && grid2.element) {
    grid2.setData([])
    for (let i = 0; i < 5; i++) {
      grid2.addRow({ ioqty: 0, price: 0, ioamt: 0 }, false)
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

// 7. 팝업 확정 콜백
const onDeptConfirm = (d: any) => {
  formData.deptcd = d.deptcd
  formData.deptnm = d.deptnm
}

const onCustConfirm = (d: any) => {
  formData.custcd = d.custcd
  formData.custnm = d.custnm
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || d.unitnm || 'EA',
    price: d.incost || d.price || 0,
    ioqty: 1,
    ioamt: d.incost || d.price || 0,
    upkind: 'A',
    _STATE: 'NEW'
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
    }
  })
  return container
}

const calcRow = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) return
  const amt = Math.round(Number(d.ioqty || 0) * Number(d.price || 0))
  row.update({ ioamt: amt })
  if (d._STATE === 'EXIST' && d.upkind !== 'D') {
    row.update({ upkind: 'U' })
  }
}

// 9. 주요 액션 (조회, 저장, 삭제)
async function search() {
  const res = await api.post('/hsio/HSIO_250U_STR', {
    actkind: 'L',
    cmpycd: authStore.cmpycd,
    iogbn: '100',
    fromdt: searchForm.fromdt.replace(/-/g, ''),
    todt: searchForm.todt.replace(/-/g, ''),
    custnm: searchForm.schcustnm
  })
  grid1?.setData(res.data || [])
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8)
    ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}`
    : today

  Object.assign(formData, { ...row, ioymd: fYmd(row.ioymd) })

  try {
    const res = await api.post('/hsio/HSIO_251U_STR', [{
      actkind: 'S', cmpycd: authStore.cmpycd, iogbn: '100', ioym: row.ioym, iono: row.iono,
      ioqty: 0, ioamt: 0, iovat: 0
    }])
    const data = (res.data || []).map((i: any) => {
      const ioqty = Number(i.ioqty || 0); const ioamt = Number(i.ioamt || 0)
      const price = i.price && Number(i.price) !== 0 ? Number(i.price) : (ioqty !== 0 ? Math.floor(ioamt / ioqty) : 0)
      return { ...i, ioqty, ioamt, price, upkind: 'U', _STATE: 'EXIST' }
    })
    // 🚀 [표준] 조회 시에는 실데이터만 출력
    grid2?.setData(data)
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function save() {
  if (isSaving.value) return
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (!formData.custcd) return vAlertError('매입처를 선택하세요.')

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const details = (grid2?.getData() || []).filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r.upkind
  ).map((d: any) => ({
    actkind: d.upkind === 'A' ? 'A0' : (d.upkind === 'D' ? 'D0' : 'U0'),
    cmpycd: authStore.cmpycd,
    ioym: formData.ioymd.replace(/-/g, '').substring(0, 6),
    iono: formData.iono || '',
    ioymd: formData.ioymd.replace(/-/g, ''),
    iogbn: '100', iotype: '120',
    itemcd: d.itemcd,
    ioqty: Number(d.ioqty || 0),
    ioamt: Number(d.ioamt || 0),
    iovat: Number(d.iovat || 0),
    whcd: formData.whcd,
    deptcd: formData.deptcd,
    custcd: formData.custcd,
    iorowno: d.iorowno || '',
    updemp: authStore.userid
  }))

  if (!details.length && (!formData.iono || formData.iono === '0000')) {
    return vAlertError('저장할 내역이 없습니다.')
  }

  if (!confirm('덤입고 작업을 진행하시겠습니까?')) return

  isSaving.value = true
  try {
    const ioymd = formData.ioymd.replace(/-/g, '')
    const mst = {
      ...formData,
      actkind: (!formData.iono || formData.iono === '0000') ? 'A0' : 'U0',
      ioym: ioymd.substring(0, 6),
      ioymd,
      iogbn: '100',
      iotype: '120',
      gubun: '1',
      updemp: authStore.userid
    }
    await api.post('/hsio/HSIO_250U_SAVE', { mst, dtl: details })
    vAlert('저장되었습니다(Alt+S)'); initialize(); search()
  } catch (e) { vAlertError('저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._STATE === 'NEW') row.delete()
  else row.update({ upkind: d.upkind === 'D' ? 'U' : 'D' })
}

const addRow = () => {
  if (isClosed.value) return
  grid2?.addRow({ ioqty: 0, price: 0, ioamt: 0 }, false)
}

const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

async function handleFullDelete() {
  if (!formData.iono || formData.iono === '0000') return vAlertError('조회 후 처리하세요.')
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await api.post('/hsio/HSIO_250U_STR', { ...formData, actkind: 'D0', iogbn: '100', cfmyn: 'Y' })
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
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSIO250U') }
  }
}

const fetchWhOptions = async () => {
  const res = await api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
  whOptions.value = (res.data || []).map((i: any) => ({
    code: i.whcd || '',
    cdnm: i.whnm || ''
  }))
  if (whOptions.value.length > 0 && !formData.whcd) formData.whcd = whOptions.value[0].code
}

const formatNumber = (n: any) => Number(n || 0).toLocaleString()

// 10. 라이프사이클 훅
onMounted(async () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%",
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "입고일자", field: "ioymd", hozAlign: "center", width: 100, formatter: (c) => {
          const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "입고번호", field: "iono", hozAlign: "center", width: 110, cssClass: "fw-bold text-primary",
        formatter: (cell) => { const d = cell.getRow().getData(); return d.ioym && d.iono ? `${d.ioym}-${d.iono}` : d.iono; }
      }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      {
        title: "", width: 40, hozAlign: "center", headerHozAlign: "center",
        formatter: "rowSelection", titleFormatter: "rowSelection"
      },
      { title: "상태", field: "upkind", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === 'A') return '<span class="badge bg-primary">신규</span>'
          if (v === 'U') return '<span class="badge bg-warning text-dark">수정</span>'
          if (v === 'D') return '<span class="badge bg-danger">삭제</span>'
          return ''
      }},
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()), cellClick: (e, cell) => { if(!isClosed.value) cell.edit() } },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 80, hozAlign: "center" },
      { title: "수량", field: "ioqty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "ioamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())

  await fetchWhOptions()
  api.post('/ha00/HA00_00P_STR', { gubun: 'SD', cmpycd: authStore.cmpycd, gbncd: '', code: '', remark: '' }).then(r => userData.value = r.data)
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
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
