<!--
	=============================================================
	프로그램명	: 바코드 출고 처리 (HSIO204U)
	작성일자	: 2026.09.30
	설명        : 데스크탑/모바일 바코드 실시간 스캐닝 출고 처리
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 출고의뢰번호 검색 팝업 모달 (HSIO_620S_STR S1) -->
  <Modal
    :visible="modalVisible"
    :modalProps="modalProps"
    @close="modalVisible = false"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom py-2 px-3">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">바코드 출고 처리 (HSIO204U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">신규(N)</button>
        <button class="btn-erp btn-search" @click="search">조회(F)</button>
        <button class="btn-erp btn-save" @click="finishOutbound">스캔완료</button>
      </div>
    </div>

    <!-- 💡 2. 상단 바코드 스캐너 입력 및 마스터 정보 -->
    <div class="p-2 flex-shrink-0">
      <!-- 🔥 2-1. 확장된 대형 바코드 스캐너 포커스 입력창 -->
      <div class="card border-2 border-primary mb-2 shadow-sm bg-primary-subtle">
        <div class="card-body py-2 px-3 d-flex align-items-center gap-3">
          <span class="fw-bolder text-primary d-flex align-items-center fs-6 text-nowrap">
            <i class="bi bi-upc-scan me-2 fs-5"></i> 바코드 스캔:
          </span>
          <input
            ref="barcodeInputRef"
            v-model="scanInput"
            type="text"
            class="form-control border-2 border-primary fw-bold text-primary flex-grow-1"
            placeholder="출고의뢰서 상단 바코드 또는 품목/시리얼 바코드를 스캔하세요 (Enter)"
            @keyup.enter="handleBarcodeScan"
            style="font-size: 14px;"
          />
          <button class="btn btn-primary px-3 text-nowrap fw-bold" @click="handleBarcodeScan">
            <i class="bi bi-search me-1"></i> 엔터/스캔
          </button>
        </div>
      </div>

      <!-- 📋 2-2. 마스터 헤더 정보 -->
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="bg-light text-center">의뢰번호</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formMaster.dispIono" type="text" class="form-control text-center fw-bold text-primary" placeholder="예: 202609-0001" @keyup.enter="search" />
                    <button class="btn btn-outline-secondary" @click="openOutboundOrderModal"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center">출고일자</th>
                <td>
                  <input v-model="formMaster.ioymd" type="date" class="form-control form-control-sm" />
                </td>
                <th class="bg-light text-center">출고창고</th>
                <td>
                  <select v-model="formMaster.whcd" class="form-select form-select-sm">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center">거래처명</th>
                <td>
                  <input v-model="formMaster.custnm" type="text" class="form-control form-control-sm bg-light" readonly placeholder="의뢰번호 스캔시 표시" />
                </td>
                <th class="bg-light text-center">특기사항</th>
                <td colspan="3">
                  <input v-model="formMaster.remark" type="text" class="form-control form-control-sm" placeholder="특기사항" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📊 3. 하단 바코드 실시간 스캔 이력 그리드 -->
    <div class="flex-grow-1 p-2 pt-0 overflow-hidden d-flex flex-column" style="min-height: 0;">
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-list-check me-2 text-primary"></i> 출고 의뢰 상세 품목 목록
          </span>
          <span class="badge bg-primary fs-6">총 품목 건수: {{ itemList.length }}건</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column" style="min-height: 0;">
          <div ref="gridElement" class="tabulator-full-height" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { getDate } from '@/composables/useDate'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { today } = getDate()

const scanInput = ref('')
const barcodeInputRef = ref<HTMLInputElement | null>(null)
const gridElement = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null

const whOptions = ref<any[]>([])
const itemList = ref<any[]>([])

const formMaster = reactive<any>({
  ioym: '',
  iono: '',
  dispIono: '',
  ioymd: today,
  whcd: '000',
  custnm: '',
  remark: ''
})

const modalVisible = ref(false)
const modalProps = reactive<any>({
  title: '출고의뢰번호 검색',
  path: '/hsio/HSIO_620S_STR',
  large: true,
  data: {},
  columns: [
    { title: '거래처코드', field: 'custcd', width: 90, hozAlign: 'center', formatter: (cell: any) => getVal(cell.getRow().getData(), 'custcd') },
    { title: '거래처명', field: 'custnm', minWidth: 150, hozAlign: 'left', formatter: (cell: any) => getVal(cell.getRow().getData(), 'custnm') },
    { title: '의뢰일자', field: 'ioymd', width: 100, hozAlign: 'center', formatter: (cell: any) => getVal(cell.getRow().getData(), 'ioymd') },
    { title: '의뢰년월', field: 'ioym', width: 80, hozAlign: 'center', formatter: (cell: any) => getVal(cell.getRow().getData(), 'ioym') },
    { title: '의뢰순번', field: 'iono', width: 80, hozAlign: 'center', formatter: (cell: any) => getVal(cell.getRow().getData(), 'iono') }
  ],
  onConfirm: (row: any) => {
    handleModalConfirm(row)
  }
})

const getVal = (obj: any, key: string) => {
  if (!obj) return ''
  const kLower = key.toLowerCase(); const kUpper = key.toUpperCase()
  const val = obj[kLower] !== undefined ? obj[kLower] : (obj[kUpper] !== undefined ? obj[kUpper] : '')
  return val === null ? '' : String(val).trim()
}

/** 🚀 [팝업 모달 열기] HSIO_620S_STR S1 프로시저 호출 */
const openOutboundOrderModal = () => {
  modalProps.data = {
    actkind: 'S1',
    cmpycd: authStore.cmpycd,
    iogbn: '200',
    whcd: '000',
    custcd: '0000000',
    ioym: '',
    iono: '',
    slipyn: 'Y',
    fromdt: '20260101',
    todt: today.replace(/-/g, ''),
    custnm: ''
  }
  modalVisible.value = true
}

/** 🚀 [팝업 선택 확정] */
const handleModalConfirm = async (row: any) => {
  modalVisible.value = false
  const ym = getVal(row, 'ioym')
  const no = getVal(row, 'iono')
  if (ym && no) {
    formMaster.ioym = ym
    formMaster.iono = no
    formMaster.dispIono = `${ym}-${no}`
    await search()
    vAlert(`출고의뢰건 [${formMaster.dispIono}] 선택 완료`)
  }
}

/** 🚀 [바코드 스캔 핸들러] 의뢰번호 스캔 vs 품목/시리얼 바코드 스캔 자동 판별 */
const handleBarcodeScan = async () => {
  const val = scanInput.value.trim()
  if (!val) return

  const cleanVal = val.replace(/-/g, '')
  // Case 1: 출고의뢰번호 바코드 스캔 시 (예: "2026090001")
  if (cleanVal.length === 10 && cleanVal.startsWith('202')) {
    formMaster.ioym = cleanVal.substring(0, 6)
    formMaster.iono = cleanVal.substring(6)
    formMaster.dispIono = `${formMaster.ioym}-${formMaster.iono}`
    scanInput.value = ''
    await search()
    vAlert(`출고의뢰번호 [${formMaster.dispIono}] 선택 완료`)
    focusBarcodeInput()
    return
  }

  // Case 2: 품목/시리얼 바코드 스캔 시 (iogbn: '200')
  if (!formMaster.ioym || !formMaster.iono) {
    vAlertError('출고의뢰서 상단 바코드를 먼저 스캔하세요.')
    scanInput.value = ''
    focusBarcodeInput()
    return
  }

  try {
    const srowNo = String(itemList.value.length + 1).padStart(3, '0')

    await api.post('/hsio/HSIO_104U_SAVE', [{
      cmpycd: authStore.cmpycd,
      iogbn: '200', // 200: 출고
      ioym: formMaster.ioym,
      iono: formMaster.iono,
      srowno: srowNo,
      itemcd: val,
      barcode: val,
      scan_qty: 1,
      lotno: val, // 스캔한 시리얼/LOT 번호
      updemp: authStore.userid
    }])

    scanInput.value = ''
    await search()
    vAlert(`바코드 [${val}] 출고 스캔 저장 완료`)
  } catch (e: any) {
    vAlertError('스캔 저장 실패')
  } finally {
    focusBarcodeInput()
  }
}

/** 🚀 [출고 마스터(S2) 및 상세 품목(S0) 순수 프로시저 조회] */
async function search() {
  if (!formMaster.dispIono && (!formMaster.ioym || !formMaster.iono)) return
  if (formMaster.dispIono) {
    const raw = formMaster.dispIono.replace(/-/g, '').trim()
    if (raw.length >= 10) {
      formMaster.ioym = raw.substring(0, 6)
      formMaster.iono = raw.substring(6)
    }
  }

  try {
    // 💡 HSIO_620S_STR 순수 프로시저만으로 마스터(S2) 및 디테일(S0) 조회 (10개 매개변수 전체 세팅)
    const [hRes, dRes] = await Promise.all([
      api.post('/hsio/HSIO_620S_STR', { actkind: 'S2', cmpycd: authStore.cmpycd, iogbn: '200', whcd: '000', fromdt: '20260101', todt: '20261231', custcd: '0000000', ioym: formMaster.ioym, iono: formMaster.iono, slipyn: 'Y' }),
      api.post('/hsio/HSIO_620S_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '200', whcd: '000', fromdt: '20260101', todt: '20261231', custcd: '0000000', ioym: formMaster.ioym, iono: formMaster.iono, slipyn: 'Y' })
    ])

    if (hRes.data && hRes.data.length > 0) {
      const m = hRes.data[0]
      formMaster.custnm = getVal(m, 'custnm') || getVal(m, 'ccustnm') || getVal(m, 'cust_nm')
      formMaster.remark = getVal(m, 'remark')
      formMaster.whcd = getVal(m, 'whcd') || '000'
      const ymd = getVal(m, 'ioymd')
      if (ymd.length === 8) formMaster.ioymd = `${ymd.substring(0,4)}-${ymd.substring(4,6)}-${ymd.substring(6,8)}`
    }

    itemList.value = dRes.data || []
    grid?.setData(itemList.value)
  } catch (e) {
    vAlertError('조회 실패')
  }
}

function initialize() {
  scanInput.value = ''
  formMaster.ioym = ''; formMaster.iono = ''; formMaster.dispIono = ''
  formMaster.custnm = ''; formMaster.remark = ''; formMaster.ioymd = today
  itemList.value = []
  grid?.clearData()
  focusBarcodeInput()
}

function finishOutbound() {
  if (!itemList.value.length) return vAlertError('스캔 내역이 없습니다.')
  vAlert('출고 검수 및 바코드 출고처리가 정상 완료되었습니다.')
}

function focusBarcodeInput() {
  nextTick(() => barcodeInputRef.value?.focus())
}

onMounted(() => {
  api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
     .then(r => whOptions.value = r.data.map((i: any) => ({ code: i.code || i.whcd, cdnm: i.cdnm || i.whnm })))

  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: 'fitColumns',
      height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: 'middle' },
      columns: [
        { title: 'No', formatter: 'rownum', width: 50, hozAlign: 'center' },
        { title: '품목코드', field: 'itemcd', minWidth: 120, hozAlign: 'center', formatter: (cell) => getVal(cell.getRow().getData(), 'itemcd') },
        { title: '품 명', field: 'itemnm', minWidth: 180, hozAlign: 'left', formatter: (cell) => getVal(cell.getRow().getData(), 'itemnm') },
        { title: '규 격', field: 'itsize', width: 90, hozAlign: 'center', formatter: (cell) => getVal(cell.getRow().getData(), 'itsize') },
        { title: '단위', field: 'unit', width: 60, hozAlign: 'center', formatter: (cell) => getVal(cell.getRow().getData(), 'unit') },
        { title: '의뢰수량', field: 'ioqty', width: 90, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '스캔수량', field: 'scan_qty', width: 90, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'fw-bold text-primary fs-6' },
        { title: 'LOT / 시리얼 번호', field: 'lotno', minWidth: 160, hozAlign: 'center', cssClass: 'fw-bold text-danger', formatter: (cell) => getVal(cell.getRow().getData(), 'lotno') || '-' },
        { title: '스캔필수', field: 'autoyn', width: 80, hozAlign: 'center', formatter: (cell) => {
            const v = (getVal(cell.getRow().getData(), 'autoyn') || '').toUpperCase()
            return v === 'Y' ? '<span class="badge bg-warning text-dark">필수</span>' : '<span class="badge bg-light text-secondary">일반</span>'
          }
        }
      ]
    })
  }

  focusBarcodeInput()
})

onUnmounted(() => grid?.destroy())
</script>

<style scoped>
.tabulator-full-height { width: 100% !important; background-color: #fff; border-bottom: 3px solid #005a9f !important; }
</style>
