<!--
	=============================================================
	프로그램명	: 입금입력 (HSIO300U)
	작성일자	: 2025.02.24
	설명        : 영업 입금 관리 (입력 방식 최적화 및 어음 정보 복구본)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 🚀 필수 팝업만 유지 -->
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
  <HelpBase
    ref="imtypeHelpRef"
    v-model:visible="popVisible.imtype"
    title="입금유형 선택"
    :columns="imtypePopupColumns"
    @search="fetchImTypePopupData"
    @confirm="onImTypeConfirm"
    @close="restoreFocus"
  />
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
        <i class="bi bi-cash-stack me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        입금관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">입금입력 (HSIO300U)</span>
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
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 40%" /><col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light small">입금일자</th>
                <td><DateForm v-model:fromdt="form_01.fromdt" v-model:todt="form_01.todt" :tabindex="101" /></td>
                <th class="text-center bg-light small border-start">거래처명</th>
                <td><input v-model="form_01.schcustnm" class="form-control form-control-sm" placeholder="거래처명 검색" @keyup.enter="search" tabindex="102" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 380px; min-width: 380px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex align-items-center justify-content-between">
            <span>입금 목록</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 110px;" /><col /><col style="width: 110px;" /><col /><col style="width: 110px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light text-center small">입금부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" :disabled="isClosed" tabindex="2"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center small border-start">입금번호</th>
                    <td>
                      <div class="d-flex gap-1">
                        <input v-model="form_02.imym" class="form-control bg-light text-center" readonly style="width: 85px;" placeholder="연월" tabindex="-1" />
                        <input v-model="form_02.imno" class="form-control bg-light text-primary fw-bold text-center" readonly style="width: 65px;" placeholder="번호" tabindex="-1" />
                      </div>
                    </td>
                    <th class="required bg-light text-center small border-start">입금일자</th>
                    <td><input v-model="form_02.imymd" type="date" class="form-control" :readonly="isClosed" tabindex="3" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center small">거래처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly tabindex="4" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" :disabled="isClosed" tabindex="5"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center small border-start">여신잔액</th>
                    <td><input :value="formatNumber(form_02.janamt)" class="form-control bg-light text-end" readonly tabindex="-1" /></td>
                    <th class="bg-light text-center small border-start">여신기한</th>
                    <td><input v-model="form_02.rcvdd" class="form-control bg-light text-center" readonly style="width: 80px;" tabindex="-1" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center small">적요</th>
                    <td colspan="5"><input ref="remarkRef" v-model="form_02.remark" class="form-control" :readonly="isClosed" tabindex="6" @keydown.tab="handleRemarkTab" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>입금 상세 내역 (어음 포함)</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="isClosed" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="isClosed" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="7"></div>
            </div>
            <div class="card-footer bg-light p-1 border-top d-flex justify-content-end gap-5 pe-4">
               <div class="small fw-bold">입금액 합계: <span class="text-primary fw-bold">{{ formatNumber(amtTot) }}</span></div>
               <div class="small fw-bold">어음 합계: <span class="text-danger fw-bold">{{ formatNumber(billTot) }}</span></div>
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
import DeptHelp from '@/components/help/DeptHelp.vue'
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'
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
const imtypeHelpRef = ref<any>(null)
const mgtHelpRef = ref<any>(null)

const popVisible = reactive({ dept: false, cust: false, imtype: false, mgt: false, isP1: false })
const isSaving = ref(false)
const closingInfo = reactive({ sclsym: '' })
const amtTot = ref(0); const billTot = ref(0)
let activeRow: any = null

const billgbnOptions = { "010": "자수어음", "020": "타수어음" }

// 3. 데이터 모델링
const form_01 = reactive({ fromdt: firstDay, todt: today, schcustnm: '' })
const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, imno: '0000', imym: '', imymd: today,
  custcd: '', custnm: '', janamt: 0, rcvdd: '', remark: '', actkind: 'A0'
})

const isClosed = computed(() => closingInfo.sclsym && form_02.imymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym)
const displayIoNo = computed(() => (!form_02.imno || form_02.imno === '0000') ? '' : `${form_02.imym}-${form_02.imno}`)

// 4. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 5. 비즈니스 로직 함수
const initialize = () => {
  resetForm(form_02); activeRow = null; lastActiveElement.value = null; isSaving.value = false; amtTot.value = 0; billTot.value = 0;
  Object.assign(form_02, { cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, imno: '0000', imym: '', imymd: today, actkind: 'A0', janamt: 0, rcvdd: '' })
  if (grid2) {
    grid2.setData([])
    for (let i = 0; i < 5; i++) grid2.addRow({ imamt: 0, billamt: 0, pubymd: '', endymd: '' }, false)
  }
  nextTick(() => firstFocusRef.value?.focus())
}

const restoreFocus = () => { nextTick(() => lastActiveElement.value?.focus()) }

const onDeptConfirm = (d: any) => { form_02.deptcd = d.deptcd; form_02.deptnm = d.deptnm }
const onCustConfirm = (d: any) => {
  form_02.custcd = d.custcd; form_02.custnm = d.custnm;
  api.post('/hs00/HS00_150S_STR', { cmpycd: authStore.cmpycd, custnm: d.custnm }).then(r => {
    if(r.data?.length) { const n = r.data[0]; form_02.rcvdd = n.rcvdd || n.RCVDD || ''; form_02.janamt = n.janamt || n.JANAMT || 0; }
  })
}

const onImTypeConfirm = (d: any) => {
  activeRow.update({ imtype: String(d.imgbn || d.code || '').trim(), imtypenm: String(d.imgbnnm || d.cdnm || '').trim(), dacctcd: d.dacctcd || '', cacctcd: d.cacctcd || '', mgtno: '', _status: '입력', _state: 'NEW' })
  markEdit(activeRow); setTimeout(() => activeRow.getCell("imamt").edit(), 150)
}

const onMgtConfirm = (d: any) => {
  activeRow.update({ mgtno: popVisible.isP1 ? d.slipno : d.mgtno })
  markEdit(activeRow); setTimeout(() => activeRow.getCell("billamt").edit(), 150)
}

// 6. 그리드 에디터 엔진 (팝업과 직접 입력 분리)
const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const field = cell.getField()
  const container = document.createElement("div")
  container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"
  container.innerHTML = `<input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex:1;"><i class="bi bi-search text-primary ms-1" style="font-size:11px; cursor:pointer;"></i>`
  const input = container.querySelector("input") as HTMLInputElement; const icon = container.querySelector("i") as HTMLElement
  const triggerHelp = () => {
    success(input.value);
    if (field === 'imtypenm') handleOpenHelp('IMTYPE', cell.getRow())
    else if (field === 'mgtno') handleOpenHelp('MGT', cell.getRow())
  }
  onRendered(() => { input.value = cell.getValue() || ''; input.focus(); input.select(); })
  input.addEventListener("keydown", (e) => { if (e.key === "Enter") { e.preventDefault(); triggerHelp(); } })
  icon.addEventListener("click", triggerHelp); return container
}

const markEdit = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제' && d.imtype) row.update({ _status: '수정' })
  calcTotals()
}

const calcTotals = () => {
  const data = grid2?.getData().filter(d => d.imtype && d._status !== '삭제') || []
  amtTot.value = data.reduce((acc, cur) => acc + (Number(cur.imamt) || 0), 0)
  billTot.value = data.reduce((acc, cur) => acc + (Number(cur.billamt) || 0), 0)
}

// 7. 주요 액션
async function search() {
  const res = await api.post('/hsio/HSIO_300U_STR', { actkind: 'L0', cmpycd: authStore.cmpycd, fromdt: form_01.fromdt.replace(/-/g, ''), todt: form_01.todt.replace(/-/g, ''), custnm: form_01.schcustnm })
  grid1?.setData(res.data || []); vAlert('조회되었습니다(Alt+F)')
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8) ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}` : today
  Object.assign(form_02, { ...row, imymd: fYmd(row.imymd) })
  try {
    const res = await api.post('/hsio/HSIO_300U_STR', { actkind: 'S1', imym: row.imym, imno: row.imno, imamt: 0, billamt: 0 })
    grid2?.setData((res.data || []).map((i: any) => ({ ...i, imtype: (i.imtype || i.imgbn || '').trim(), imtypenm: (i.imtypenm || i.imgbnnm || '').trim(), _state: 'EXIST', _status: '' })))
    nextTick(() => calcTotals())
  } catch (e) { vAlertError('상세 로드 실패') }
}

async function save() {
  if (isSaving.value || isClosed.value) return
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.')
  const details = (grid2?.getData() || []).filter((r: any) => r.imtype && r._status).map((d: any) => ({
    ...d, actkind: d._status === '입력' ? 'A1' : (d._status === '삭제' ? 'D1' : 'U1'),
    cmpycd: authStore.cmpycd, imym: form_02.imym, imno: form_02.imno, deptcd: form_02.deptcd, custcd: form_02.custcd, imymd: form_02.imymd.replace(/-/g, ''),
    imamt: Number(d.imamt || 0), billamt: Number(d.billamt || 0), pubymd: String(d.pubymd || '').replace(/-/g, ''), endymd: String(d.endymd || '').replace(/-/g, ''), updemp: authStore.userid
  }))
  if (!details.length && (!form_02.imno || form_02.imno === '0000')) return vAlertError('저장할 내역이 없습니다.')
  if (!confirm('입금 정보를 저장하시겠습니까?')) return
  isSaving.value = true
  try {
    const mst = {
    ...form_02,
    actkind: (!form_02.imno || form_02.imno === '0000') ? 'A0' : 'U0',
    imymd: form_02.imymd.replace(/-/g, ''),
    updemp: authStore.userid }
    console.log(mst, details)

    await api.post('/hsio/HSIO_300U_SAVE', { mst, dtl: details })
    vAlert('저장되었습니다(Alt+S)'); initialize(); search()
  } catch (e) { vAlertError('저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'IMTYPE') { activeRow = target; popVisible.imtype = true }
  else if (type === 'MGT') {
    activeRow = target; const im = String(activeRow.getData().imtype || '').trim()
    if (im === '400') return vAlertError('어음번호는 직접 입력하십시오.'); popVisible.isP1 = (im === '500' || im === '510'); popVisible.mgt = true
  }
}

const handleRowAction = (row: any) => { const d = row.getData(); if (!d.imtype) row.delete(); else if (d._state === 'NEW') row.delete(); else row.update({ _status: d._status === '삭제' ? '' : '삭제' }); calcTotals(); }
const addRow = () => { if (!isClosed.value) grid2?.addRow({ imamt: 0, billamt: 0, pubymd: '', endymd: '', _status: '입력', _state: 'NEW' }, false) }
const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

const imtypePopupColumns = [{ title: '코드', field: 'imgbn', width: 100 }, { title: '유형명', field: 'imgbnnm', widthGrow: 1 }]
const fetchImTypePopupData = async (word: string) => { api.post('/hs00/HS00_000S_STR', { gubun: 'I2', cmpycd: authStore.cmpycd, codenm: word }).then(r => imtypeHelpRef.value?.setData(r.data)) }
const mgtPopupColumns = computed(() => popVisible.isP1 ? [{ title: '발행일', field: 'slipymd', width: 100 }, { title: '번호', field: 'slipno', width: 100 }, { title: '순번', field: 'srowno', width: 60 }, { title: '적요', field: 'remark', widthGrow: 1 }, { title: '잔액', field: 'janamt', width: 110, hozAlign: 'right', formatter: 'money' }] : [{ title: '계좌/카드번호', field: 'mgtno', width: 180 }, { title: '명칭', field: 'mgtnm', widthGrow: 1 }])
const fetchMgtPopupData = async (word: string) => {
  const d = activeRow.getData(); const im = String(d.imtype || '').trim(); let gubun = 'M0'; let gbncd = '010'; let rem = d.dacctcd
  if (['200', '600', '210'].includes(im)) rem = (im === '210') ? '1145' : '1120'
  else if (im === '300') { gbncd = '040'; rem = '1110' }
  else if (im === '500' || im === '510') { if (!form_02.custcd) return vAlertError('거래처를 먼저 선택하십시오.'); gubun = 'P1'; gbncd = (im === '500') ? '2110' : '2125'; rem = form_02.custcd }
  api.post('/ha00/HA00_00P_STR', { gubun, cmpycd: authStore.cmpycd, gbncd, remark: rem, codenm: word }).then(r => mgtHelpRef.value?.setData(r.data))
}

function handleRemarkTab(e: KeyboardEvent) { if (e.key === 'Tab' && !e.shiftKey) { e.preventDefault(); if (grid2) { const rows = grid2.getRows(); if (rows.length > 0) setTimeout(() => rows[0].getCell("imtypenm").edit(), 100) } } }
function handleGlobalShortcuts(e: KeyboardEvent) { if (e.altKey) { const k = e.key.toLowerCase(); if (k === 'f') { e.preventDefault(); search() } else if (k === 's') { e.preventDefault(); save() } else if (k === 'n') { e.preventDefault(); initialize() } } }

// 8. 라이프사이클
onMounted(async () => {
  grid1 = new Tabulator(tableRef1.value!, { layout: "fitColumns", height: "100%", selectable: 1, columns: [{ title: "No", formatter: "rownum", width: 40 }, { title: "거래처명", field: "custnm", hozAlign: "left", minWidth: 150 }, { title: "입금일자", field: "imymd", hozAlign: "center", width: 100, formatter: (c) => formatDateDash(c.getValue()) }, { title: "입금번호", field: "imno", hozAlign: "center", width: 90, cssClass: "fw-bold text-primary" }] })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))
  grid2 = new Tabulator(tableRef2.value!, { layout: "fitColumns", height: "100%", selectable: true, keybindings: { "navNext": "9" }, columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === '입력' ? '<span class="badge bg-primary">신규</span>' : (c.getValue() === '수정' ? '<span class="badge bg-warning text-dark">수정</span>' : (c.getValue() === '삭제' ? '<span class="badge bg-danger">삭제</span>' : '')) },
      { title: "입금유형", field: "imtypenm", minWidth: 150, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('IMTYPE', cell.getRow()) },
      { title: "입금액", field: "imamt", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "관리번호", field: "mgtno", width: 140, editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('MGT', cell.getRow()) },
      { title: "어음액면가", field: "billamt", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "어음종류", field: "billgbn", width: 100, editor: "list", editorParams: { values: billgbnOptions }, formatter: (c) => billgbnOptions[c.getValue() as keyof typeof billgbnOptions] || c.getValue(), cellEdited: (c) => markEdit(c.getRow()) },
      { title: "발행일", field: "pubymd", width: 120, editor: "date", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "발행인", field: "pubman", width: 100, editor: "input", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "만기일", field: "endymd", width: 120, editor: "date", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "발행은행", field: "pubbank", width: 110, editor: "input", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })
  grid2.on("tableBuilt", () => initialize())
  api.post('/hp00/HP00_000S_STR', { gubun: 'CL', cmpycd: authStore.cmpycd }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym })
  window.addEventListener('keydown', handleGlobalShortcuts)
})
onUnmounted(() => { window.removeEventListener('keydown', handleGlobalShortcuts); searchStore.removeTab(route.name as string) })
const formatDateDash = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v
const formatNumber = (n: any) => Number(n || 0).toLocaleString()
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 11px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
