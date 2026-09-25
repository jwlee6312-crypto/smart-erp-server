<!--
	=============================================================
	프로그램명	: 외주가공 자재출고 (HPIO340U)
	작성일자	: 2025.03.22
	설명        : 외주가공 자재 출고 관리 (HSOD100U 표준 적용본)
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
  <ProdCustHelp
    v-model:visible="popVisible.cust"
    @confirm="onCustConfirm"
    @close="restoreFocus"
  />
  <ProdItemHelp
    v-model:visible="popVisible.prod"
    @confirm="onProdConfirm"
    @close="restoreFocus"
  />
  <RawItemHelp
    v-model:visible="popVisible.item"
    @confirm="onItemConfirm"
    @close="restoreFocus"
  />
  <!-- 외주가공 품의조회 전용 -->
  <HelpBase
    v-model:visible="popVisible.pum"
    title="외주가공 품의조회"
    :columns="pumPopupColumns"
    :large="true"
    @search="fetchPumPopupData"
    @confirm="onPumConfirm"
    @close="restoreFocus"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주가공 자재출고 (HPIO340U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="fetchList" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="saveAll" tabindex="-1">저장(S)</button>
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
              <col style="width: 25%" />
              <col style="width: 10%" />
              <col style="width: 25%" />
              <col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required small">생산라인</th>
                <td>
                  <select v-model="form_01.linecd" class="form-select form-select-sm" tabindex="101" @change="fetchList">
                    <option value="">라인 선택</option>
                    <option v-for="l in lineData" :key="l.linecd" :value="l.linecd">{{ l.linenm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light required small border-start">출고일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="form_01.fromdt"
                    v-model:todt="form_01.todt"
                    :tabindex="102"
                    @change="fetchList"
                  />
                </td>
                <td class="text-muted small ps-2">※ 생산라인과 기간을 선택하십시오.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 출고 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-2 text-primary"></i>출고 목록</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="listTableRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 마스터 상세 폼 및 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 마스터 정보 폼 -->
          <div class="card border shadow-sm flex-shrink-0">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 85px;" /><col />
                  <col style="width: 85px;" /><col style="width: 15%;" />
                  <col style="width: 85px;" /><col style="width: 15%;" />
                  <col style="width: 85px;" /><col style="width: 15%;" />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light text-center small">품의번호</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.pumym" type="text" class="form-control text-center" style="max-width: 80px;" readonly tabindex="-1" />
                        <input v-model="form_02.pumno" type="text" class="form-control text-center fw-bold" readonly tabindex="-1" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('PUM')" tabindex="1"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center small">생산라인</th>
                    <td>
                      <select v-model="form_02.linecd" class="form-select form-select-sm" tabindex="2">
                        <option v-for="l in lineData" :key="l.linecd" :value="l.linecd">{{ l.linenm }}</option>
                      </select>
                    </td>
                    <th class="bg-light text-center small">출고번호</th>
                    <td>
                      <input
                        :value="displayIoNo"
                        class="form-control bg-light text-primary fw-bold text-center"
                        readonly tabindex="-1"
                        placeholder="자동생성"
                      />
                    </td>
                    <th class="required bg-light text-center small">출고일자</th>
                    <td><input v-model="form_02.outymd" type="date" class="form-control" tabindex="3" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center small">외주가공처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" type="text" class="form-control" readonly tabindex="-1" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')" tabindex="4"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center small">생산공정</th>
                    <td>
                      <select v-model="form_02.progcd" class="form-select form-select-sm" tabindex="5">
                        <option v-for="p in processData" :key="p.progcd" :value="p.progcd">{{ p.prognm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center small">출고창고</th>
                    <td>
                      <select v-model="form_02.whcd" class="form-select form-select-sm" tabindex="6">
                        <option v-for="w in warehouseData" :key="w.whcd" :value="w.whcd">{{ w.whnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center small">입고창고</th>
                    <td>
                      <select v-model="form_02.iwhcd" class="form-select form-select-sm" tabindex="7">
                        <option v-for="w in warehouseData" :key="w.whcd" :value="w.whcd">{{ w.whnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center small">제품명</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.itemnm" type="text" class="form-control" readonly tabindex="-1" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('PROD')" tabindex="8"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center small">가공단가</th>
                    <td><input v-model="form_02.price" type="number" class="form-control text-end" tabindex="9" /></td>
                    <th class="required bg-light text-center small">생 산 량</th>
                    <td><input v-model="form_02.proqty" type="number" class="form-control text-end fw-bold text-primary" tabindex="10" @change="fetchDetailItems" /></td>
                    <th class="required bg-light text-center small">입고일자</th>
                    <td><input v-model="form_02.proymd" type="date" class="form-control" tabindex="11" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 하단 그리드: 투입 자재 상세 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>투입 자재 상세 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="itemTableRef" class="tabulator-instance flex-grow-1" tabindex="12"></div>
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
import ProdCustHelp from '@/components/help/ProdCustHelp.vue'
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
  dept: false,
  cust: false,
  prod: false,
  item: false,
  pum: false
})
const isSaving = ref(false)

const lineData = ref<any[]>([])
const processData = ref<any[]>([])
const warehouseData = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const form_01 = reactive({
  linecd: '888',
  fromdt: firstDay,
  todt: today
})

const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd,
  pumym: '',
  pumno: '',
  linecd: '888',
  outym: '',
  outno: '',
  outymd: today,
  custcd: '',
  custnm: '',
  progcd: '888',
  whcd: '100',
  iwhcd: '900',
  itemcd: '',
  itemnm: '',
  price: 0,
  proqty: 0,
  proymd: today,
  remark: '',
  inym: '',
  inno: ''
})

// 4. 연산 및 감시자
const displayIoNo = computed(() => {
  if (!form_02.outno || form_02.outno === '0000') return ''
  return `${form_02.outym}-${form_02.outno}`
})

watch(() => form_02.linecd, (nv) => {
  if (nv) {
    api.post('/hp00/HP00_000S_STR', { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: nv }).then(res => {
      processData.value = res.data
    })
  }
})

// 5. 그리드 참조
const listTableRef = ref<HTMLDivElement | null>(null)
const itemTableRef = ref<HTMLDivElement | null>(null)
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
    outymd: today,
    proymd: today,
    outym: today.replace(/-/g, '').substring(0, 6),
    outno: '0000',
    linecd: '888',
    progcd: '888',
    whcd: '100',
    iwhcd: '900',
    price: 0,
    proqty: 0
  })

  // 🚀 잔상 물리적 파괴 후 초기화
  if (grid1) grid1.setData([])
  if (grid2) grid2.setData([])

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
  // 필요 시 구현
}

const onCustConfirm = (d: any) => {
  form_02.custcd = d.custcd
  form_02.custnm = d.custnm
}

const onProdConfirm = (d: any) => {
  form_02.itemcd = d.itemcd
  form_02.itemnm = d.itemnm
  form_02.itsize = d.itsize
  form_02.unit = d.unit
  fetchDetailItems() // 제품 선택 시 가용 자재 조회
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize || '',
    unit: d.unit || 'EA',
    ioqty: 1,
    _status: '입력',
    _state: 'NEW'
  })
}

const onPumConfirm = (d: any) => {
  Object.assign(form_02, {
    pumym: d.pumym,
    pumno: d.pumno,
    custcd: d.custcd,
    custnm: d.custnm,
    itemcd: d.itemcd,
    itemnm: d.itemnm,
    itsize: d.itsize,
    unit: d.unit,
    price: d.price,
    proqty: d.qty || 0
  })
  fetchDetailItems()
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
  if (d._state === 'EXIST' && d._status !== '삭제') {
    row.update({ _status: '수정' })
  }
}

// 9. 주요 액션
async function fetchList() {
  if (!form_01.linecd) return vAlertError('생산라인을 선택하세요.')
  try {
    const res = await api.post('/hpio/HPIO_340U_STR', {
      actkind: 'L', cmpycd: authStore.cmpycd, fromdt: form_01.fromdt.replace(/-/g, ''), todt: form_01.todt.replace(/-/g, ''),
      iogbn: '200', outno: '0000', proqty: 0, linecd: form_01.linecd
    })
    grid1?.setData((res.data || []).map((i: any) => ({ ...i, outno_full: `${i.outym}-${i.outno}` })))
    vAlert('조회되었습니다(Alt+F)')
  } catch (e) { vAlertError('목록 조회 실패') }
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8) ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}` : today
  Object.assign(form_02, { ...row, outymd: fYmd(row.outymd), proymd: fYmd(row.proymd) })

  try {
    const res = await api.post('/hpio/HPIO_341U_STR', [{
      actkind: 'S', cmpycd: authStore.cmpycd, iogbn: '200', outym: row.outym, outno: row.outno, ioqty: 0
    }])
    grid2?.setData((res.data || []).map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })))
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function fetchDetailItems() {
  if (!form_02.itemcd) return
  const qty = Number(form_02.proqty || 1)
  try {
    const res = await api.post('/hpio/HPIO_341U_STR', [{
      actkind: 'B', cmpycd: authStore.cmpycd, iogbn: '200', itemcd: form_02.itemcd, ioqty: qty,
      pumym: form_02.pumym, pumno: form_02.pumno, outym: form_02.outym, outno: form_02.outno,
      linecd: form_02.linecd, progcd: form_02.progcd, ioymd: form_02.outymd.replace(/-/g, ''), proymd: form_02.proymd.replace(/-/g, '')
    })
    grid2?.setData((res.data || []).map((i: any) => ({ ...i, _status: '입력', _state: 'NEW' })))
  } catch (e) {}
}

async function saveAll() {
  if (isSaving.value) return
  if (!form_02.custcd) return vAlertError('외주가공처를 선택하세요.')

  const details = (grid2?.getData() || []).filter((r: any) => r.itemcd && r._status)
  if (!details.length && (!form_02.outno || form_02.outno === '0000')) return vAlertError('항목을 추가하세요.')

  if (!confirm('저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const act = (!form_02.outno || form_02.outno === '0000') ? 'A' : 'U'
    // 1. 마스터 저장
    const resM = await api.post('/hpio/HPIO_340U_STR', {
      ...form_02, actkind: act, pumym: form_02.pumym,
      outym: form_02.outymd.replace(/-/g, '').substring(0, 6),
      outymd: form_02.outymd.replace(/-/g, ''),
      proymd: form_02.proymd.replace(/-/g, ''),
      inyn: 'Y', owhcd: form_02.whcd, ocustcd: form_02.custcd, iwhcd: form_02.iwhcd, prodcd: '200', userid: authStore.userid
    })

    const mRes = resM.data?.[0]
    if (mRes && (mRes.outym === '000000' || mRes.col_0 === '000000')) throw new Error(mRes.outno || mRes.col_1 || '마스터 저장 오류')

    const keyYM = mRes.outym; const keyNO = mRes.outno; const keyIN = mRes.inno

    // 2. 상세 저장
    for (const d of details) {
      const dAct = d._status === '입력' ? 'A' : (d._status === '삭제' ? 'D' : 'U')
      await api.post('/hpio/HPIO_341U_STR', [{
        ...d, actkind: dAct, cmpycd: authStore.cmpycd, iogbn: '200',
        ioymd: form_02.outymd.replace(/-/g, ''), outym: keyYM, outno: keyNO, inno: keyIN,
        owhcd: form_02.whcd, custcd: form_02.custcd, linecd: form_02.linecd, progcd: form_02.progcd,
        proymd: form_02.proymd.replace(/-/g, ''), pumym: form_02.pumym, pumno: form_02.pumno, userid: authStore.userid
      })
    }

    vAlert('저장되었습니다(Alt+S)'); fetchList(); fetchDetail({ outym: keyYM, outno: keyNO })
  } catch (e: any) { vAlertError(e.message || '저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'CUST') popVisible.cust = true
  else if (type === 'PROD') popVisible.prod = true
  else if (type === 'ITEM' || type === 'MITEM') { activeRow = target; popVisible.item = true }
  else if (type === 'PUM') popVisible.pum = true
}

const handleRowAction = (row: any) => {
  const d = row.getData()
  if (!d.itemcd) row.delete()
  else if (d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = () => grid2?.addRow({ ioqty: 0, _status: '입력', _state: 'NEW' }, false)
const deleteSelectedRows = () => grid2?.getSelectedRows().forEach(row => handleRowAction(row))

async function handleFullDelete() {
  if (!form_02.outno || form_02.outno === '0000') return vAlertError('조회 후 처리하세요.')
  if (confirm('전체 삭제하시겠습니까?')) {
    try {
      await api.post('/hpio/HPIO_340U_STR', { ...form_02, actkind: 'D', userid: authStore.userid })
      vAlert('삭제되었습니다.'); initialize(); fetchList()
    } catch (e) { vAlertError('삭제 실패') }
  }
}

// 품의 팝업 관련
const pumPopupColumns = [
  { title: '품의일자', field: 'pumymd', width: 100, hozAlign: 'center', formatter: (c:any) => formatDate(c.getValue()) },
  { title: '번호', field: 'pumno', width: 60, hozAlign: 'center' },
  { title: '거래처', field: 'custnm', width: 150 },
  { title: '품목명', field: 'itemnm', width: 180 },
  { title: '규격', field: 'itsize', width: 120 }
]
const fetchPumPopupData = async (word: string) => {
  const res = await api.post('/hpio/HPIO_340U_POPUP', { cmpycd: authStore.cmpycd, fromdt: form_01.fromdt.replace(/-/g, ''), todt: form_01.todt.replace(/-/g, ''), codenm: word })
  return res.data
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); fetchList() }
    else if (key === 's') { e.preventDefault(); saveAll() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'd') { e.preventDefault(); handleFullDelete() }
  }
}

// 10. 라이프사이클 훅
onMounted(async () => {
  grid1 = new Tabulator(listTableRef.value!, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columns: [
      { title: "출고번호", field: "outno_full", hozAlign: "center", width: 120, cssClass: "fw-bold text-primary" },
      { title: "거래처명", field: "custnm", hozAlign: "left" },
      { title: "출고일", field: "outymd", hozAlign: "center", width: 100, formatter: (c) => formatDate(c.getValue()) }
    ]
  })
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()))

  grid2 = new Tabulator(itemTableRef.value!, {
    layout: "fitColumns", height: "100%", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>'
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>'
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>'
          return ''
      }},
      { title: "자재명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-success', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "출고수량", field: "ioqty", width: 100, hozAlign: "right", editor: "number", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "비고", field: "remark", minWidth: 150, editor: "input", cellEdited: (c) => markEdit(c.getRow()) },
      { title: "삭제", width: 40, formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  })

  grid2.on("tableBuilt", () => initialize())

  api.post('/hp00/HP00_000S_STR', { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' }).then(res => lineData.value = res.data)
  api.post('/hs00/HS00_000S_STR', { gubun: 'W0', cmpycd: authStore.cmpycd }).then(res => warehouseData.value = res.data)
  window.addEventListener('keydown', handleGlobalShortcuts)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
  searchStore.removeTab(route.name as string)
})

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
