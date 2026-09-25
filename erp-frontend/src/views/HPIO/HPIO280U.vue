<!--
	=============================================================
	프로그램명	: 외주(임)가공계약등록 (HPIO280U)
	작성일자	: 2025.03.22
	설명        : 외주(임) 가공 계약 마스터/상세/자재 관리 (HSOD100U 표준 적용본)
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

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- [1] 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주(임)가공계약등록 (HPIO280U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="fetchList" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="saveAll" tabindex="-1">저장(S)</button>
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
                <th class="text-center bg-light small">품의기간</th>
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
                    @keyup.enter="fetchList"
                    tabindex="102"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 하단 투-그리드 레이아웃 -->
      <div class="d-flex flex-row flex-grow-1 overflow-hidden gap-2" style="min-height: 0;">

        <!-- 🅰️ 좌측: 계약 목록 -->
        <div class="d-flex flex-column gap-2" style="width: 480px;">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-2 text-primary"></i>계약 목록</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="listTableRef" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>

        <!-- 🅱️ 우측: 상세 정보 및 제품/자재 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 마스터 상세 폼 -->
          <div class="card border shadow-sm flex-shrink-0">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 120px;" /><col />
                  <col style="width: 120px;" /><col />
                  <col style="width: 120px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light text-center small">품의번호</th>
                    <td>
                      <div class="d-flex align-items-center gap-1 px-1">
                        <input v-model="pumym_f" type="month" class="form-control form-control-sm" style="max-width: 120px;" tabindex="1" />
                        <input v-model="form_02.pumno" type="text" class="form-control form-control-sm text-center bg-light fw-bold text-primary" style="width: 80px;" readonly tabindex="-1" />
                      </div>
                    </td>
                    <th class="required bg-light text-center small">품의일자</th>
                    <td><div class="px-1"><input v-model="pumymd_f" type="date" class="form-control form-control-sm" tabindex="2" /></div></td>
                    <th class="required bg-light text-center small">품의부서</th>
                    <td>
                      <div class="input-group input-group-sm px-1">
                        <input v-model="form_02.deptnm" type="text" class="form-control" readonly tabindex="3" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')" tabindex="4"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center small">외주가공처</th>
                    <td>
                      <div class="input-group input-group-sm px-1">
                        <input v-model="form_02.custnm" type="text" class="form-control" readonly tabindex="5" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')" tabindex="6"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center small">계약기간</th>
                    <td colspan="3">
                      <div class="d-flex align-items-center gap-1 px-1">
                        <input v-model="frymd_f" type="date" class="form-control form-control-sm" style="max-width: 130px;" tabindex="7" />
                        <span class="small mx-1">~</span>
                        <input v-model="toymd_f" type="date" class="form-control form-control-sm" style="max-width: 130px;" tabindex="8" />
                        <span class="ms-2 small fw-bold text-muted" style="min-width: 50px;">특기:</span>
                        <input v-model="form_02.remark" class="form-control form-control-sm flex-grow-1" placeholder="계약 메모" tabindex="9" @keydown.tab="handleRemarkTab" />
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 그리드 레이아웃 -->
          <div class="d-flex flex-column gap-2 flex-grow-1 overflow-hidden">
            <!-- 1. 제품 그리드 -->
            <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
              <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
                <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-box-seam me-2 text-primary"></i>계약 대상 제품 리스트</span>
                <div class="btn-group-erp d-flex gap-1">
                   <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('PROD')" style="font-size: 11px;">+ 제품추가</button>
                   <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('PROD')" style="font-size: 11px;">- 삭제</button>
                </div>
              </div>
              <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="prodTableRef" class="tabulator-instance flex-grow-1" tabindex="10"></div>
              </div>
            </div>

            <!-- 2. 자재 그리드 -->
            <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
              <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
                <span class="fw-bold small text-dark d-flex align-items-center">
                  <i class="bi bi-tools me-2 text-success"></i>투입 자재 소요량 상세
                  <span v-if="selectedProduct.itemnm" class="badge bg-success-subtle text-success border border-success-subtle ms-2" style="font-size: 10px;">{{ selectedProduct.itemnm }}</span>
                </span>
                <div class="btn-group-erp d-flex gap-1">
                   <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">+ 자재추가</button>
                   <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">- 삭제</button>
                </div>
              </div>
              <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="matTableRef" class="tabulator-instance flex-grow-1" tabindex="11"></div>
              </div>
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

// 1. 공통 상태 및 훅 초기화
const authStore = useAuthStore()
const { today, firstDay } = getDate()
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
  prod: false,
  item: false
})
const isSaving = ref(false)

const selectedProduct = reactive<any>({ itemcd: '', itemnm: '' })
let activeRow: any = null

// 3. 데이터 모델링
const form_01 = reactive({ fromdt: firstDay, todt: today, custnm: '' })

const form_02 = reactive<any>({
  actkind: 'S0', cmpycd: authStore.cmpycd,
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  pumym: today.replace(/-/g, '').substring(0, 6),
  pumno: '000',
  pumymd: today.replace(/-/g, ''),
  frymd: today.replace(/-/g, ''),
  toymd: today.replace(/-/g, ''),
  custcd: '', custnm: '', remark: '', pumgbn: '200'
})

// 포맷팅 헬퍼
const pumym_f = computed({
  get: () => form_02.pumym ? `${form_02.pumym.substring(0, 4)}-${form_02.pumym.substring(4, 6)}` : '',
  set: (v) => { if (v) form_02.pumym = v.replace(/-/g, '') }
})
const pumymd_f = computed({ get: () => formatDate(form_02.pumymd), set: (v) => { if (v) form_02.pumymd = v.replace(/-/g, '') } })
const frymd_f = computed({ get: () => formatDate(form_02.frymd), set: (v) => { if (v) form_02.frymd = v.replace(/-/g, '') } })
const toymd_f = computed({ get: () => formatDate(form_02.toymd), set: (v) => { if (v) form_02.toymd = v.replace(/-/g, '') } })

// 4. 그리드 참조
const listTableRef = ref<HTMLDivElement | null>(null)
const prodTableRef = ref<HTMLDivElement | null>(null)
const matTableRef = ref<HTMLDivElement | null>(null)

let listGrid: Tabulator | null = null
let prodGrid: Tabulator | null = null
let matGrid: Tabulator | null = null

// 5. 비즈니스 로직 함수 (HSOD100U 표준 적용)
const initialize = () => {
  resetForm(form_02)
  activeRow = null
  lastActiveElement.value = null
  isSaving.value = false

  Object.assign(form_02, {
    actkind: 'S0', cmpycd: authStore.cmpycd,
    deptcd: authStore.deptcd, deptnm: authStore.deptnm,
    pumym: today.replace(/-/g, '').substring(0, 6),
    pumno: '000',
    pumymd: today.replace(/-/g, ''),
    frymd: today.replace(/-/g, ''),
    toymd: today.replace(/-/g, ''),
    pumgbn: '200'
  })

  selectedProduct.itemcd = ''
  selectedProduct.itemnm = ''

  // 🚀 잔상 물리적 파괴 후 초기화
  if (prodGrid) prodGrid.setData([])
  if (matGrid) matGrid.setData([])
  if (listGrid) listGrid.setData([])

  nextTick(() => firstFocusRef.value?.focus())
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
  form_02.deptcd = d.deptcd
  form_02.deptnm = d.deptnm
}

const onCustConfirm = (d: any) => {
  form_02.custcd = d.custcd
  form_02.custnm = d.custnm
}

const onProdConfirm = (d: any) => {
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
  markEdit(activeRow)
}

const onItemConfirm = (d: any) => {
  if (!activeRow) return
  activeRow.update({
    mitemcd: d.itemcd,
    mitemnm: d.itemnm,
    mitsize: d.itsize || '',
    munit: d.unit || 'EA',
    soqty: 1,
    _status: '입력',
    _state: 'NEW'
  })
  markEdit(activeRow)
}

// 7. 그리드 에디터 및 헬퍼
const markEdit = (row: any) => {
  const d = row.getData()
  if (d._state === 'EXIST' && d._status !== '삭제') {
    row.update({ _status: '수정' })
  }
}

const handleRowAction = (row: any, type: string) => {
  const d = row.getData()
  if (d._status === '입력') row.delete()
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' })
}

const addRow = (type: string) => {
  if (type === 'PROD') {
    prodGrid?.addRow({ price: 0, _status: '입력', _state: 'NEW' }, false)
  } else {
    if (!selectedProduct.itemcd) return vAlertError('제품을 먼저 선택하세요.')
    matGrid?.addRow({ soqty: 0, _status: '입력', _state: 'NEW' }, false)
  }
}

const deleteRows = (type: string) => {
  const g = type === 'PROD' ? prodGrid : matGrid
  g?.getSelectedRows().forEach(r => handleRowAction(r, type))
}

// 8. 주요 액션 (조회, 저장)
const convertToLower = (list: any[]) => list.map(i => {
  const obj: any = {}
  Object.keys(i).forEach(k => { obj[k.toLowerCase()] = i[k] })
  return obj
})

async function fetchList() {
  try {
    const res = await api.post('/hpio/HPIO_290U_STR', {
      actkind: 'L0',
      cmpycd: authStore.cmpycd,
      pumgbn: '200',
      fromdt: form_01.fromdt.replace(/-/g, ''),
      todt: form_01.todt.replace(/-/g, '')
    })
    listGrid?.setData(convertToLower(res.data || []))
    vAlert('조회되었습니다(Alt+F)')
  } catch (e) { vAlertError('목록 조회 실패') }
}

async function fetchDetail(row: any) {
  Object.assign(form_02, row)
  try {
    const resP = await api.post('/hpio/HPIO_291U_STR', [{
        actkind: 'S0', cmpycd: authStore.cmpycd, pumym: form_02.pumym, pumno: form_02.pumno, price: 0
     }])
    const data = convertToLower(resP.data || []).map(i => ({ ...i, _state: 'EXIST', _status: '' }))
    await prodGrid?.setData(data)

    if (data.length > 0) {
      const firstItem = data[0]
      fetchMaterials(firstItem, true)
      nextTick(() => {
        const rows = prodGrid?.getRows()
        if (rows && rows.length > 0) rows[0].select()
      })
    } else {
      matGrid?.setData([])
      selectedProduct.itemcd = ''; selectedProduct.itemnm = ''
    }
  } catch (e) {}
}

async function fetchMaterials(prod: any, force = false) {
  if (!prod.itemcd || (!force && prod.itemcd === selectedProduct.itemcd)) return
  selectedProduct.itemcd = prod.itemcd
  selectedProduct.itemnm = prod.itemnm

  if (form_02.pumno === '000' || !form_02.pumno) {
    matGrid?.setData([])
    return
  }

  try {
    const resM = await api.post('/hpio/HPIO_292U_STR', {
        actkind: 'S', cmpycd: authStore.cmpycd, pumym: form_02.pumym, pumno: form_02.pumno,
        itemcd: prod.itemcd, mitemcd: '0', soqty: 0, updemp: authStore.userid
     })
    matGrid?.setData(convertToLower(resM.data || []).map(i => ({ ...i, _state: 'EXIST', _status: '' })))
  } catch (e) {}
}

async function saveAll() {
  if (isSaving.value) return
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.')

  // 🚀 [표준] 무결성 필터: 상태가 명확한 실데이터만 정밀 추출
  const prods = prodGrid?.getData().filter((r: any) => r.itemcd && r._status) || []
  const mats = matGrid?.getData().filter((r: any) => r.mitemcd && r._status) || []

  if (form_02.pumno === '000' && prods.length === 0) return vAlertError('저장할 내역이 없습니다.')

  if (!confirm('저장하시겠습니까?')) return

  isSaving.value = true
  try {
    const lastItemCd = selectedProduct.itemcd
    const isNew = form_02.pumno === '000' || !form_02.pumno

    // 1. 마스터 저장
    const resM = await api.post('/hpio/HPIO_290U_STR', {
      ...form_02,
      fromdt: form_01.fromdt.replace(/-/g, ''),
      todt: form_01.todt.replace(/-/g, ''),
      actkind: isNew ? 'A0' : 'U0',
      userid: authStore.userid
    })

    const resMData = convertToLower(resM.data || [])[0]
    if (resMData?.pumno) {
      form_02.pumno = resMData.pumno
      if (resMData.pumym) form_02.pumym = resMData.pumym
    }

    // 2. 제품 저장
    for (const p of prods) {
      const pAct = p._status === '입력' ? 'A0' : (p._status === '삭제' ? 'D0' : 'U0')
      await api.post('/hpio/HPIO_291U_STR', [{
        ...p, actkind: pAct, cmpycd: authStore.cmpycd, pumym: form_02.pumym, pumno: form_02.pumno, updemp: authStore.userid
      }])
    }

    // 3. 자재 저장
    for (const m of mats) {
      const mAct = m._status === '입력' ? 'A' : (m._status === '삭제' ? 'D' : 'U')
      await api.post('/hpio/HPIO_292U_STR', {
        actkind: mAct, cmpycd: authStore.cmpycd, pumym: form_02.pumym, pumno: form_02.pumno,
        itemcd: lastItemCd, mitemcd: m.mitemcd, itsize: m.mitsize || '', munit: m.munit || '',
        soqty: m.soqty || 0, remark: m.remark || '', updemp: authStore.userid
      })
    }

    vAlert('저장되었습니다(Alt+S)')
    await fetchList()
    await fetchDetail({ ...form_02 })
    if (lastItemCd) fetchMaterials({ itemcd: lastItemCd }, true)

  } catch (e) { vAlertError('저장 중 오류 발생') } finally { isSaving.value = false }
}

const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'CUST') popVisible.cust = true
  else if (type === 'PROD') { activeRow = target; popVisible.prod = true }
  else if (type === 'ITEM' || type === 'MAT') { activeRow = target; popVisible.item = true }
}

function handleRemarkTab(e: KeyboardEvent) {
  if (e.key === 'Tab' && !e.shiftKey) {
    e.preventDefault()
    if (prodGrid) {
      const rows = prodGrid.getRows()
      if (rows.length > 0) setTimeout(() => rows[0].getCell("itemnm").edit(), 100)
    }
  }
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'f') { e.preventDefault(); fetchList() }
    else if (key === 's') { e.preventDefault(); saveAll() }
    else if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HPIO280U') }
  }
}

// 9. 라이프사이클 훅
onMounted(() => {
  listGrid = new Tabulator(listTableRef.value!, {
    layout: "fitColumns", height: "100%", placeholder: "목록 없음", selectable: 1,
    columns: [
      { title: "번호", field: "pumno", width: 80, hozAlign: "center" },
      { title: "거래처명", field: "custnm", minWidth: 150 },
      { title: "품의일", field: "pumymd", width: 110, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) }
    ]
  })
  listGrid.on("rowClick", (e, row) => fetchDetail(row.getData()))

  prodGrid = new Tabulator(prodTableRef.value!, {
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
      { title: "제품명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', cellClick: (e, cell) => handleOpenHelp('PROD', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단가", field: "price", width: 120, hozAlign: "right", editor: "number", cellEdited: (c) => markEdit(c.getRow()) }
    ]
  })
  prodGrid.on("cellClick", (e, cell) => { if (cell.getField() !== "price") fetchMaterials(cell.getRow().getData()) })

  matGrid = new Tabulator(matTableRef.value!, {
    layout: "fitColumns", height: "100%", placeholder: "자재 내역 없음", selectable: true,
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
      { title: "자재명", field: "mitemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-success', cellClick: (e, cell) => handleOpenHelp('MAT', cell.getRow()) },
      { title: "규격", field: "mitsize", width: 150 },
      { title: "소요량", field: "soqty", width: 120, hozAlign: "right", editor: "number", cellEdited: (c) => markEdit(c.getRow()) }
    ]
  })

  prodGrid.on("tableBuilt", () => initialize())
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
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
</style>
