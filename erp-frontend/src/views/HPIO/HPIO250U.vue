<!--
	=============================================================
	프로그램명	: 자재불출요청 (HPIO250U)
	작성일자	: 2025.02.24
	설명        : 작업지시 기반 자재 불출 요청 및 상세 관리 (HSOD100U 표준 적용본)
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
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        생산관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        자재관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">자재불출요청 (HPIO250U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="handleSearch" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="handleSave" tabindex="-1">저장(S)</button>
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
              <col style="width: 100px" />
              <col style="width: 320px" />
              <col style="width: 100px" />
              <col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light small">불출일자</th>
                <td>
                  <DateForm
                    v-model:fromdt="searchForm.fromdt"
                    v-model:todt="searchForm.todt"
                    :tabindex="101"
                  />
                </td>
                <th class="text-center bg-light small border-start">생산라인</th>
                <td>
                  <select v-model="searchForm.linecd" class="form-select form-select-sm" style="max-width: 200px;" tabindex="102" @change="handleSearch">
                    <option value="">전체라인</option>
                    <option v-for="item in lineData" :key="item.linecd" :value="item.linecd">{{ item.linenm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 불출 요청 내역 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 420px; min-width: 420px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between align-items-center">
            <span>불출 요청 목록</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 상세 정보 폼 및 중하단 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 마스터 정보 -->
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
                    <th class="required bg-light small text-center">요청라인</th>
                    <td>
                      <select ref="firstFocusRef" v-model="form_mst.linecd" class="form-select form-select-sm" tabindex="1">
                        <option v-for="item in lineData" :key="item.linecd" :value="item.linecd">{{ item.linenm }}</option>
                      </select>
                    </td>
                    <th class="bg-light small text-center border-start">요청번호</th>
                    <td>
                      <input :value="displayOutNo" class="form-control form-control-sm bg-light text-primary fw-bold text-center" readonly tabindex="-1" placeholder="자동생성" />
                    </td>
                    <th class="required bg-light small text-center border-start">요청일자</th>
                    <td><input v-model="form_mst.outymd" type="date" class="form-control form-control-sm" tabindex="2" /></td>
                    <th class="required bg-light small text-center border-start">희망일자</th>
                    <td><input v-model="form_mst.hope_inymd" type="date" class="form-control form-control-sm border-primary" tabindex="3" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light small text-center">출고창고</th>
                    <td>
                      <select v-model="form_mst.whcd" class="form-select form-select-sm" tabindex="4">
                        <option v-for="wh in whData" :key="wh.whcd" :value="wh.whcd">{{ wh.whnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small text-center border-start">현장창고</th>
                    <td>
                      <select v-model="form_mst.iwhcd" class="form-select form-select-sm" tabindex="5">
                        <option v-for="wh in whData" :key="wh.whcd" :value="wh.whcd">{{ wh.whnm }}</option>
                      </select>
                    </td>
                    <th class="bg-light small text-center border-start">요청자</th>
                    <td>
                      <select v-model="form_mst.userid" class="form-select form-select-sm" tabindex="6">
                          <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                    <th class="bg-light small text-center border-start">요청부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_mst.deptnm" class="form-control bg-light" readonly tabindex="-1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="7"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">비고</th>
                    <td colspan="7">
                      <input ref="remarkRef" v-model="form_mst.bigo" class="form-control form-control-sm" tabindex="8" @keydown.tab="handleRemarkTab" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 중간: 불출 대상 제품 -->
          <div class="card border shadow-sm overflow-hidden d-flex flex-column" style="height: 35%;">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-journal-check me-2 text-primary"></i>불출 대상 제품</span>
              <div class="btn-group-erp d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="openJobOrderPop" style="font-size: 11px;">+ 작업지시 추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('MID')" style="font-size: 11px;">- 삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="9"></div>
            </div>
          </div>

          <!-- 하단: 불출 요청 자재 상세 -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-box-seam me-2 text-success"></i>불출 요청 자재 상세</span>
              <div class="btn-group-erp d-flex gap-1">
                <button class="btn btn-sm btn-outline-success py-0 px-2 fw-bold" @click="fetchBOMExplosion" style="font-size: 11px;">BOM 전개</button>
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('DTL')" style="font-size: 11px;">+ 행추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('DTL')" style="font-size: 11px;">- 삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef3" class="tabulator-instance flex-grow-1" tabindex="10"></div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>

  <!-- 🚀 작업지시 검색 팝업 -->
  <div v-if="jobOrderVisible" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5); z-index: 1060; backdrop-filter: blur(2px);">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content shadow-lg border-0" style="border-radius: 8px; overflow: hidden;">
        <div class="modal-header bg-white py-2 border-bottom shadow-sm">
          <h5 class="modal-title fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-search me-2 text-primary"></i>작업지시 검색
          </h5>
          <button type="button" class="btn-close shadow-none" style="font-size: 10px;" @click="jobOrderVisible = false"></button>
        </div>
        <div class="modal-body p-3 bg-light">
          <div class="d-flex align-items-center gap-2 mb-3 bg-white p-2 border rounded shadow-sm">
            <span class="small fw-bold text-muted ps-1">지시기간</span>
            <input v-model="jobSearch.frymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
            <span class="text-muted">~</span>
            <input v-model="jobSearch.toymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
            <button class="btn btn-primary btn-sm px-3 fw-bold ms-auto shadow-sm" @click="searchJobOrders" :disabled="jobLoading">조회</button>
          </div>
          <div class="border rounded bg-white shadow-sm overflow-hidden" style="height: 400px; position: relative;">
            <div v-if="jobLoading" class="loading-overlay"><div class="spinner-border text-primary" role="status"></div></div>
            <div ref="jobGridRef" class="tabulator-instance h-100"></div>
          </div>
          <div class="mt-2 text-muted" style="font-size: 11px;">※ 불출 대상 작업지시를 선택하여 추가하세요.</div>
        </div>
        <div class="modal-footer py-2 bg-white border-top">
          <button class="btn btn-outline-secondary btn-sm px-4 fw-bold" @click="jobOrderVisible = false">닫기</button>
          <button class="btn btn-primary btn-sm px-4 fw-bold shadow-sm" @click="confirmJobOrders">선택추가</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
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
const remarkRef = ref<HTMLInputElement | null>(null)
const lastActiveElement = ref<HTMLElement | null>(null)
const popVisible = reactive({
  dept: false,
  item: false
})
const isSaving = ref(false)

const lineData = ref<any[]>([])
const userData = ref<any[]>([])
const whData = ref<any[]>([])
const closingInfo = reactive({ sclsym: '' })
let activeRow: any = null

// 3. 데이터 모델링
const searchForm = reactive({ fromdt: firstDay, todt: today, linecd: '' })

const form_mst = reactive<any>({
  cmpycd: authStore.cmpycd,
  outym: today.replace(/-/g, '').substring(0, 6),
  iono: '0000',
  outymd: today,
  hope_inymd: today,
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  whcd: '100',
  iwhcd: '300',
  linecd: '010',
  userid: authStore.userid,
  bigo: ''
})

// 4. 연산 및 감시자
const displayOutNo = computed(() => {
  if (!form_mst.iono || form_mst.iono === '0000') return ''
  return `${form_mst.outym}-${form_mst.iono}`
})

// 5. 그리드 참조
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
const tableRef3 = ref<HTMLDivElement | null>(null)

let grid1: Tabulator | null = null
let grid2: Tabulator | null = null
let grid3: Tabulator | null = null

// 작업지시 팝업 관련
const jobOrderVisible = ref(false)
const jobLoading = ref(false)
const jobSearch = reactive({ frymd: today, toymd: today })
const jobGridRef = ref<HTMLDivElement | null>(null)
let jobGrid: Tabulator | null = null

// 6. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(form_mst)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(form_mst, {
    cmpycd: authStore.cmpycd,
    outno: '0000',
    outym: today.replace(/-/g, '').substring(0, 6),
    outymd: today,
    hope_inymd: today,
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    whcd: '100',
    iwhcd: '300',
    linecd: '010',
    userid: authStore.userid
  })

  // 🚀 잔상 물리적 파괴 후 초기화
  if (grid1) grid1.setData([])
  if (grid2) grid2.setData([])
  if (grid3) grid3.setData([])
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
  form_mst.deptcd = d.deptcd
  form_mst.deptnm = d.deptnm
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    mitemcd: d.itemcd,
    mitemnm: d.itemnm,
    mitsize: d.itsize || '',
    munit: d.unit || 'EA',
    reqqty: 0,
    outqty: 0,
    _status: '입력',
    _state: 'NEW'
  })
  setTimeout(() => activeRow.getCell("reqqty").edit(), 150)
}

// 8. 그리드 에디터 엔진 (HSOD100U 규격)
const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const container = document.createElement("div")
  container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"
  container.innerHTML = `
    <input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex: 1;" value="${cell.getValue() || ''}">
    <i class="bi bi-search text-primary ms-1" style="font-size: 11px; cursor: pointer;"></i>
  `
  const input = container.querySelector("input") as HTMLInputElement
  const icon = container.querySelector("i") as HTMLElement

  const triggerHelp = () => {
    success(input.value)
    handleOpenHelp('ITEM', cell.getRow())
  }

  onRendered(() => { input.focus(); input.select(); })

  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault(); e.stopPropagation()
      triggerHelp()
    }
  })
  icon.addEventListener("click", () => triggerHelp())
  return container
}

const markEdit = (row: any, gridType: string) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제') {
    row.update({ _status: '수정' })
  }
}

// 9. 주요 액션
const handleSearch = async () => {
  try {
    const res = await api.post('/hpio/HPIO_250U_STR', {
      actkind: 'L0', cmpycd: authStore.cmpycd,
      fromdt: searchForm.fromdt.replace(/-/g, ''),
      todt: searchForm.todt.replace(/-/g, ''),
      linecd: searchForm.linecd || '',
      ordqty: 0, outqty: 0
    })
    const data = (res.data || []).map((i: any) => {
        const item = Object.fromEntries(Object.entries(i).map(([k, v]) => [k.toLowerCase(), v]))
        return { ...item, outno_full: `${item.outym}-${item.outno}` }
    })
    grid1?.setData(data)
    vAlert('조회되었습니다(Alt+F)')
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => (d && d.length === 8) ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}` : today
  Object.assign(form_mst, { ...row, outymd: fYmd(row.outymd), hope_inymd: fYmd(row.hope_inymd) })

  try {
    const resMid = await api.post('/hpio/HPIO_250U_STR', {
        actkind: 'S1', cmpycd: authStore.cmpycd, outym: row.outym, outno: row.outno, outymd: row.outymd, ordqty: 0, outqty: 0
     })
    grid2?.setData((resMid.data || []).map((i: any) => ({ ...i, _status: '', _state: 'EXIST' })))

    const resDtl = await api.post('/hpio/HPIO_253U_STR', {
        actkind: 'S0', cmpycd: authStore.cmpycd, outym: row.outym, outno: row.outno, reqqty: 0
     })
    grid3?.setData((resDtl.data || []).map((i: any) => ({ ...i, _status: '', _state: 'EXIST' })))
  } catch (e) { vAlertError('상세 로드 실패') }
}

const handleSave = async () => {
  if (!form_mst.linecd || !form_mst.whcd || !form_mst.iwhcd) return vAlertError('라인 및 창고 정보를 확인하세요.')

  const midData = grid2?.getData().filter(r => r._status) || []
  const dtlData = grid3?.getData().filter(r => r._status) || []

  if (!midData.length && !dtlData.length && (!form_mst.outno || form_mst.outno === '0000')) {
    return vAlertError('저장할 내역이 없습니다.')
  }

  if (!confirm('불출 요청 정보를 저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const mst = {
        ...form_mst,
        actkind: (!form_mst.outno || form_mst.outno === '0000') ? 'A0' : 'U0',
        outymd: form_mst.outymd.replace(/-/g, ''),
        hope_inymd: form_mst.hope_inymd.replace(/-/g, ''),
        ordqty: 0, outqty: 0
    }

    const midlist = grid2?.getData().map(item => ({
        ...item, cmpycd: authStore.cmpycd, outymd: mst.outymd,
        actkind: item._status === '입력' || item._state === 'NEW' ? 'A1' : (item._status === '삭제' ? 'D0' : 'U1')
    }))

    const dtllist = grid3?.getData().map(item => ({
        ...item, cmpycd: authStore.cmpycd,
        actkind: item._status === '입력' || item._state === 'NEW' ? 'A0' : (item._status === '삭제' ? 'D0' : 'U0')
    }))

    const payload = { cmpycd: authStore.cmpycd, updemp: authStore.userid, mst, midlist, dtllist }
    const res = await api.post('/hpio/HPIO_250U_SAVE', payload)

    if (res.data && res.data.res === 'OK') {
        vAlert('저장되었습니다(Alt+S)')
        handleSearch()
    }
  } catch (e) { vAlertError('저장 실패') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'ITEM') { activeRow = target; popVisible.item = true }
}

const handleRowAction = (row: any, type: string) => {
  const d = row.getData()
  if (d._status === '입력' || d._state === 'NEW') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = (type: string) => {
  if (type === 'DTL') {
    grid3?.addRow({ reqqty: 0, outqty: 0, _status: '입력', _state: 'NEW' }, false)
  }
}

const deleteRows = (type: string) => {
  const g = type === 'MID' ? grid2 : grid3
  g?.getSelectedRows().forEach(row => handleRowAction(row, type))
}

const handleFullDelete = async () => {
  if (!form_mst.outno || form_mst.outno === '0000') return
  if (!confirm('불출 요청 전체를 삭제하시겠습니까?')) return
  try {
    await api.post('/hpio/HPIO_250U_STR', {
      actkind: 'D0', cmpycd: authStore.cmpycd,
      outym: form_mst.outym, outno: form_mst.outno,
      ordqty: 0, outqty: 0
    })
    vAlert('삭제되었습니다.')
    initialize(); handleSearch()
  } catch (e) { vAlertError('삭제 실패') }
}

const fetchBOMExplosion = () => {
    if (grid3?.getData().length) {
        if (!confirm('이미 자재 내역이 존재합니다. 지우고 다시 BOM 전개하시겠습니까?')) return
    }
    grid3?.clearData()
    vAlert('저장 시 자동으로 BOM이 전개됩니다. 제품 리스트 확인 후 저장을 눌러주세요.')
}

const openJobOrderPop = () => {
  if (!form_mst.linecd) return vAlertError('라인을 선택하세요.')
  jobOrderVisible.value = true
  nextTick(() => {
    if (jobGrid) { jobGrid.destroy(); jobGrid = null; }
    jobGrid = new Tabulator(jobGridRef.value!, {
      layout: "fitColumns", height: "100%", selectable: true,
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "선택", width: 40, formatter: "rowSelection", titleFormatter: "rowSelection" },
        { title: "지시일자", field: "lotymd", width: 110, hozAlign: "center", formatter: (c) => formatDateDash(c.getValue()) },
        { title: "지시번호", field: "lotno", width: 100, hozAlign: "center" },
        { title: "제품명", field: "itemnm", minWidth: 200, widthGrow: 1, hozAlign: "left" },
        { title: "지시량", field: "ordqty", width: 100, hozAlign: "right", formatter: "money" },
        { title: "기불출량", field: "outqty", width: 100, hozAlign: "right", formatter: "money" }
      ]
    })
    searchJobOrders()
  })
}

const searchJobOrders = async () => {
  jobLoading.value = true
  try {
    const res = await api.post('/hpio/HPIO_250U_POP', {
      cmpycd: authStore.cmpycd,
      linecd: form_mst.linecd,
      frymd: jobSearch.frymd.replace(/-/g, ''),
      toymd: jobSearch.toymd.replace(/-/g, '')
    })
    jobGrid?.setData(res.data)
  } catch (e) { vAlertError('조회 실패') }
  finally { jobLoading.value = false }
}

const confirmJobOrders = () => {
  const selected = jobGrid?.getSelectedData() || []
  selected.forEach((item: any) => {
    if (!grid2?.getData().find(r => r.lotno === item.lotno && r.lotymd === item.lotymd)) {
      grid2?.addRow({
        ...item,
        outqty: (item.ordqty || 0) - (item.outqty || 0),
        _status: '입력',
        _state: 'NEW'
      }, false)
    }
  })
  jobOrderVisible.value = false
}

function handleRemarkTab(e: KeyboardEvent) {
  if (e.key === 'Tab' && !e.shiftKey) {
    e.preventDefault()
    if (grid2) {
      const rows = grid2.getRows()
      if (rows.length > 0) setTimeout(() => rows[0].getCell("outqty").edit(), 100)
    }
  }
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); handleSearch() }
    else if (key === 's') { e.preventDefault(); handleSave() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'd') { e.preventDefault(); handleFullDelete() }
  }
}

// 10. 라이프사이클 훅
onMounted(async () => {
  // 기초 데이터 로드
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' } }).then(res => lineData.value = res.data)
  api.post('/ha00/HA00_00P_STR', { gubun: 'SD', cmpycd: authStore.cmpycd, gbncd: '', code: '', remark: '' }).then(res => userData.value = res.data)
  api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } }).then(res => whData.value = res.data)

  nextTick(initGrids)
  // 🚀 [해결] 마운트 완료 후 초기화 함수를 호출하여 첫 번째 포커스 강제 지정
  nextTick(() => initialize())
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
.grid-container-left { border-bottom: 3px solid #005a9f !important; }
.grid-container-right { border-bottom: 3px solid #198754 !important; }
.loading-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.7); z-index: 100; display: flex; align-items: center; justify-content: center; }
</style>
