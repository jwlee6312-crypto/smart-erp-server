<!--
	=============================================================
	프로그램명	: 타계정출고 (HSIO570U)
	작성일자	: 2025.02.24
	설명        : 타계정 출고 마스터/상세 관리 (HSOD100U 표준 적용본)
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
  <SaleItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />
  <!-- 타계정출고 전용 도움말 (출고유형) -->
  <HelpBase
    v-model:visible="popVisible.iotype"
    title="출고유형 선택"
    :columns="iotypeColumns"
    @search="fetchIoTypeData"
    @confirm="onIoTypeConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        구매관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">타계정출고 (HSIO570U)</span>
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
                    v-model:fromdt="searchParam.fromdt"
                    v-model:todt="searchParam.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small">출고부서</th>
                <td>
                  <div class="input-group input-group-sm w-50">
                    <input v-model="searchParam.deptnm" class="form-control" readonly tabindex="102" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT_SCH')" tabindex="103">
                      <i class="bi bi-search"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 출고 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 400px; min-width: 400px;">
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
                    <th class="required bg-light small">출고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="masterData.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2">
                          <i class="bi bi-search"></i>
                        </button>
                      </div>
                    </td>
                    <th class="bg-light small">출고번호</th>
                    <td>
                      <input
                        :value="displayIoNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light small">출고일자</th>
                    <td><input v-model="masterData.ioymd" type="date" class="form-control" tabindex="3" /></td>
                    <th class="required bg-light small">출고창고</th>
                    <td>
                      <select v-model="masterData.whcd" class="form-select" tabindex="4">
                        <option value="">-- 선택 --</option>
                        <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small">특기사항</th>
                    <td colspan="7">
                      <input
                        ref="remarkRef"
                        v-model="masterData.remark"
                        class="form-control"
                        tabindex="5"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 하단: 출고 품목 리스트 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>출고 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="6"></div>
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
import HelpBase from '@/components/help/HelpBase.vue'
import RegisteredAddrHelp from '@/components/help/RegisteredAddrHelp.vue'

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
  item: false,
  iotype: false,
  idept: false,
  addr: false
})
const isSaving = ref(false)

const whOptions = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchParam = reactive({
  fromdt: firstDay,
  todt: today,
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm
})

const masterData = reactive<any>({
  cmpycd: authStore.cmpycd,
  ioym: today.replace(/-/g, '').substring(0, 6),
  iono: '0000',
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  ioymd: today,
  whcd: '',
  remark: '',
  sts: 'N'
})

// 4. 연산 및 감시자
const displayIoNo = computed(() => {
  if (!masterData.iono || masterData.iono === '0000') return ''
  return `${masterData.ioym}-${masterData.iono}`
})

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !masterData.ioymd) return false
  return masterData.ioymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 6. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(masterData)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(masterData, {
    cmpycd: authStore.cmpycd,
    iono: '0000',
    ioymd: today,
    ioym: today.replace(/-/g, '').substring(0, 6),
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    sts: 'N'
  })

  if (whOptions.value.length > 0) masterData.whcd = whOptions.value[0].whcd

  // 🚀 [표준] 잔상 물리적 파괴 후 깨끗한 빈 행 5개 생성
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
  if (popVisible.idept) { // 그리드 내 사용부서
    activeRow.update({ usedeptcd: d.deptcd, usedeptnm: d.deptnm })
    popVisible.idept = false
  } else { // 마스터 또는 조회 부서
    masterData.deptcd = d.deptcd
    masterData.deptnm = d.deptnm
  }
}

const onCustConfirm = (d: any) => {
  // 타계정출고는 일반적으로 거래처를 쓰지 않으나 규격 유지
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    price: d.incost || 0,
    ioqty: 1,
    ioamt: d.incost || 0,
    _status: '입력',
    _state: 'NEW'
  })
  calcRow(activeRow)
  setTimeout(() => activeRow.getCell("ioqty").edit(), 150)
}

const onIoTypeConfirm = (d: any) => {
  activeRow.update({ iotype: d.code, iotypenm: d.cdnm })
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
  const amt = Math.floor(Number(d.ioqty || 0) * Number(d.price || 0))
  row.update({ ioamt: amt })
  if (d._state === 'EXIST' && d._status !== '삭제' && d.itemcd) {
    row.update({ _status: '수정' })
  }
}

// 9. 주요 액션 (조회, 저장, 삭제)
async function search() {
  const res = await api.post('/hsio/HSIO_570U_STR', {
    actkind: 'L',
    iogbn: '200',
    deptcd: searchParam.deptcd,
    deptnm: searchParam.deptnm,
    fromdt: searchParam.fromdt.replace(/-/g, ''),
    todt: searchParam.todt.replace(/-/g, '')
  })
  const data = (res.data || []).map((item: any) => ({
    ...item,
    iono_full: `${item.ioym}-${item.iono}`
  }))
  grid1?.setData(data)
  vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8)
    ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}`
    : today

  Object.assign(masterData, { ...row, ioymd: fYmd(row.ioymd) })

  try {
    const res = await api.post('/hsio/HSIO_571U_STR', [{
      actkind: 'S', iogbn: '200', ioym: row.ioym, iono: row.iono,
      ioqty: 0, ioamt: 0, iovat: 0
    }])
    const data = (res.data || []).map((item: any) => ({
      ...item,
      usedeptcd: item.usedept || '',
      _state: 'EXIST',
      _status: ''
    }))
    // 🚀 [표준] 조회 시에는 실데이터만 출력
    grid2?.setData(data)
  } catch (e) {
    vAlertError('상세 로드 실패')
  }
}

async function save() {
  if (isSaving.value) return
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (!masterData.deptcd || !masterData.whcd) return vAlertError('부서와 창고를 선택하세요.')

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const details = (grid2?.getData() || []).filter((r: any) =>
    r.itemcd && String(r.itemcd).trim() !== '' && r._status
  ).map((d: any) => ({
    actkind: d._status === '입력' ? 'A' : (d._status === '삭제' ? 'D' : 'U'),
    cmpycd: authStore.cmpycd,
    iogbn: '200',
    ioym: masterData.ioymd.replace(/-/g, '').substring(0, 6),
    deptcd: masterData.deptcd,
    whcd: masterData.whcd,
    ioymd: masterData.ioymd.replace(/-/g, ''),
    itemcd: d.itemcd,
    ioqty: Number(d.ioqty || 0),
    ioamt: Number(d.ioamt || 0),
    iovat: 0,
    cfmyn: 'Y',
    userid: authStore.userid,
    usedept: d.usedeptcd,
    iotype: d.iotype,
    updemp: authStore.userid
  }))

  if (!details.length && masterData.iono === '0000') return vAlertError('저장할 내역이 없습니다.')

  isSaving.value = true
  try {
    const targetIoym = masterData.ioymd.replace(/-/g, '').substring(0, 6)
    const mst = {
      ...masterData,
      actkind: masterData.iono === '0000' ? 'A' : 'U',
      ioym: targetIoym,
      ioymd: masterData.ioymd.replace(/-/g, ''),
      iogbn: '200', iotype: '300', cfmyn: 'Y',
      userid: authStore.userid, updemp: authStore.userid
    }
    await api.post('/hsio/HSIO_570U_SAVE', { mst, dtl: details })
    vAlert('저장되었습니다(Alt+S)'); initialize(); search()
  } catch (e) { vAlertError('저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT_SCH' || type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
  else if (type === 'iotype') { activeRow = target; popVisible.iotype = true }
  else if (type === 'IDEPT') { activeRow = target; popVisible.dept = true; popVisible.idept = true }
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = () => {
  if (isClosed.value) return
  const addRow = () => grid2?.addRow({ usedeptcd: '', usedeptnm: '', ioqty: 0, price: 0, ioamt: 0 }, false)
}

const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

async function handleFullDelete() {
  if (!masterData.iono || masterData.iono === '0000') return vAlertError('조회 후 처리하세요.')
  if (isClosed.value) return vAlertError('마감된 월입니다.')
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await api.post('/hsio/HSIO_570U_STR', { ...masterData, actkind: 'D', iogbn: '200', cfmyn: 'Y' })
      vAlert('삭제되었습니다.'); initialize(); search()
    } catch (e) { vAlertError('삭제 실패') }
  }
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

const iotypeColumns = [{ title: '코드', field: 'code', width: 100 }, { title: '유형명', field: 'cdnm', width: 200 }]
const fetchIoTypeData = async (word: string) => {
  const res = await api.post('/hs00/HS00_000S_STR', { gubun: 'E0', gbncd: '130', cmpycd: authStore.cmpycd, codenm: word })
  return res.data
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); search() }
    else if (key === 's') { e.preventDefault(); save() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'd') { e.preventDefault(); handleFullDelete() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSIO570U') }
  }
}

// 10. 라이프사이클 훅
onMounted(async () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%",
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      { title: "출고일자", field: "ioymd", hozAlign: "center", width: 100, formatter: (c) => {
          const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "출고번호", field: "iono_full", hozAlign: "center", width: 100, cssClass: "fw-bold text-primary" },
      { title: "출고부서", field: "deptnm", hozAlign: "center", width: 150, cssClass: "fw-bold text-primary" }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

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
      { title: "유형", field: "iotypenm", width: 120, cellClick: (e, cell) => handleOpenHelp('iotype', cell.getRow()) },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 120 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "수량", field: "ioqty", width: 80, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "ioamt", width: 110, hozAlign: "right", formatter: "money" },
      { title: "사용부서", field: "usedeptnm", width: 120, cellClick: (e, cell) => handleOpenHelp('IDEPT', cell.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())

  api.post('/hs00/HS00_000S_STR', { gubun: 'W0', cmpycd: authStore.cmpycd }).then(r => { whOptions.value = r.data })
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => { window.removeEventListener('keydown', handleGlobalShortcuts); searchStore.removeTab(route.name as string) })
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
