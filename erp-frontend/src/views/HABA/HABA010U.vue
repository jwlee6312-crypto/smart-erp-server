<!--
	=============================================================
	프로그램명	: 계정과목관리 (HABA010U)
	작성일자	: 2026.09.25
	설명        : 계정과목 및 관련 재무제표 구분, 보조부/예산 연동 관리 (원본 ASP 복원 및 표준 UI 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

  <!-- 상위계정 팝업 -->
  <HelpBase
    v-model:visible="popVisible.upacct"
    title="상위계정 검색"
    :columns="upacctColumns"
    @search="searchUpAcct"
    @confirm="onUpAcctConfirm"
  />

  <!-- 예산코드 팝업 -->
  <HelpBase
    v-model:visible="popVisible.bugtcd"
    title="예산코드 검색"
    :columns="bugtColumns"
    @search="searchBugt"
    @confirm="onBugtConfirm"
  />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        회계기준 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">계정과목관리 (HABA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">신규(N)</button>
        <button class="btn-erp btn-search" @click="search">조회(F)</button>
        <button class="btn-erp btn-save" @click="save">저장(S)</button>
        <button class="btn-erp btn-delete" @click="deletedata" :disabled="formdata.actkind !== 'U1'">삭제(D)</button>
        <button class="btn-erp btn-excel" @click="excel">엑셀저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col style="width: 250px;" />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center small">재무제표</th>
                <td>
                  <select v-model="searchform.gubun" class="form-select form-select-sm" @change="search">
                    <option v-for="opt in gubunOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start small">계 정 명</th>
                <td>
                  <input
                    v-model="searchform.acctnm"
                    type="text"
                    class="form-control form-control-sm"
                    placeholder="계정명 입력 후 Enter"
                    @keyup.enter="search"
                  />
                </td>
                <td class="text-end pe-2">
                  <button class="btn btn-sm btn-primary py-0 px-3 fw-bold" @click="search">
                    <i class="bi bi-search me-1"></i> 조회
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중간] 상세 입력/수정 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <i class="bi bi-pencil-square me-2 text-primary"></i>
          <span class="fw-bold small text-dark">계정과목 세부 설정</span>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 90px;" /><col style="width: 200px;" />
              <col style="width: 90px;" /><col style="width: 220px;" />
              <col style="width: 90px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center small">계정과목</th>
                <td>
                  <input
                    v-model="formdata.acctcd"
                    type="text"
                    class="form-control form-control-sm fw-bold text-primary text-center"
                    maxlength="7"
                    :readonly="formdata.actkind === 'U1'"
                    placeholder="계정코드"
                  />
                </td>
                <th class="required bg-light text-center border-start small">계 정 명</th>
                <td>
                  <input v-model="formdata.acctnm" type="text" class="form-control form-control-sm fw-bold" maxlength="30" placeholder="계정명" />
                </td>
                <th class="bg-light text-center border-start small">상위계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formdata.upacct" type="text" class="form-control text-center" style="max-width: 80px;" readonly placeholder="코드" />
                    <input v-model="formdata.upacct_t" type="text" class="form-control" placeholder="상위계정명" @keyup.enter="openHelp('UPACCT')" />
                    <button class="btn btn-outline-secondary" type="button" @click="openHelp('UPACCT')">
                      <i class="bi bi-search"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required bg-light text-center border-top small">차대구분</th>
                <td class="border-top">
                  <select v-model="formdata.typedc" class="form-select form-select-sm">
                    <option value="D">차변 (Debit)</option>
                    <option value="C">대변 (Credit)</option>
                  </select>
                </td>
                <th class="required bg-light text-center border-start border-top small">전표발생</th>
                <td class="border-top">
                  <select v-model="formdata.slipyn" class="form-select form-select-sm">
                    <option value="Y">발행</option>
                    <option value="N">미발행</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top small">보 조 부</th>
                <td class="border-top">
                  <select v-model="formdata.typesub" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in subOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center border-top small">관리번호</th>
                <td class="border-top">
                  <select v-model="formdata.typemgt" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in mgtOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top small">상계유형</th>
                <td class="border-top">
                  <select v-model="formdata.typecrs" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in crsOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top small">기타관리</th>
                <td class="border-top">
                  <select v-model="formdata.typeect" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in ectOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center border-top small">계정유형</th>
                <td class="border-top">
                  <select v-model="formdata.typeacct" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in acctTypeOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top small">증빙유무</th>
                <td class="border-top">
                  <select v-model="formdata.profyn" class="form-select form-select-sm">
                    <option value="N">무</option>
                    <option value="Y">유</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top small">예산코드</th>
                <td class="border-top">
                  <div class="input-group input-group-sm">
                    <input v-model="formdata.bugtcd" type="text" class="form-control text-center" style="max-width: 80px;" readonly placeholder="코드" />
                    <input v-model="formdata.bugtcd_t" type="text" class="form-control" placeholder="예산코드명" @keyup.enter="openHelp('BUGT')" />
                    <button class="btn btn-outline-secondary" type="button" @click="openHelp('BUGT')">
                      <i class="bi bi-search"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center border-top small">외화관리</th>
                <td class="border-top">
                  <div class="form-check form-switch m-0 ms-2">
                    <input id="frgnynCheck" v-model="formdata.frgnyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N">
                    <label class="form-check-label small fw-bold ms-1" for="frgnynCheck">관리함</label>
                  </div>
                </td>
                <th class="bg-light text-center border-start border-top small">검 색 어</th>
                <td class="border-top">
                  <input v-model="formdata.keyword" type="text" class="form-control form-control-sm" maxlength="50" placeholder="검색 키워드" />
                </td>
                <th class="bg-light text-center border-start border-top small">사용여부</th>
                <td class="border-top">
                  <div class="form-check form-switch m-0 ms-2">
                    <input id="useynCheck" v-model="formdata.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N">
                    <label class="form-check-label small fw-bold ms-1" for="useynCheck">사용</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 계정과목 목록 그리드 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>계정과목 목록</span>
          <span class="text-muted small" style="font-size: 11px;">행을 클릭하면 상단에서 수정 가능합니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden d-flex flex-column">
          <div ref="gridelement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import HelpBase from '@/components/help/HelpBase.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

// 1. 검색 및 입력 폼 상태
const searchform = reactive({
  gubun: '010',
  acctnm: ''
})

const formdata = reactive<any>({
  actkind: 'I1',
  gubun: '010',
  acctcd: '',
  acctnm: '',
  upacct: '',
  upacct_t: '',
  typedc: 'D',
  slipyn: 'Y',
  typesub: '000',
  typemgt: '000',
  typecrs: '000',
  typeect: '000',
  typeacct: '000',
  profyn: 'N',
  bugtcd: '',
  bugtcd_t: '',
  frgnyn: 'N',
  keyword: '',
  useyn: 'Y',
  updyn: 'Y'
})

// 2. 공통 코드 드롭다운 옵션 목록
const gubunOptions = ref<any[]>([])
const subOptions = ref<any[]>([])
const mgtOptions = ref<any[]>([])
const crsOptions = ref<any[]>([])
const ectOptions = ref<any[]>([])
const acctTypeOptions = ref<any[]>([])

// 3. 그리드 및 팝업 제어
const gridelement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

const popVisible = reactive({
  upacct: false,
  bugtcd: false
})

const upacctHelpRef = ref<any>(null)
const bugtHelpRef = ref<any>(null)

const upacctColumns = [
  { title: '계정코드', field: 'acctcd', width: 100, hozAlign: 'center', cssClass: 'fw-bold text-primary' },
  { title: '계정명', field: 'acctnm', widthGrow: 1, hozAlign: 'left' }
]

const bugtColumns = [
  { title: '예산코드', field: 'bugtcd', width: 100, hozAlign: 'center', cssClass: 'fw-bold text-primary' },
  { title: '예산명', field: 'bugtnm', widthGrow: 1, hozAlign: 'left' }
]

// 4. 초기화 및 드롭다운 로드
async function fetchDropdowns() {
  try {
    // 재무제표 구분 (070)
    const rGubun = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '070', code: '' })
    gubunOptions.value = (rGubun.data || []).filter((i: any) => (i.code || i.codecd || '') <= '030')
      .map((i: any) => ({ codecd: i.code || i.codecd, codenm: i.cdnm || i.codenm }))

    // 보조부 (010)
    const rSub = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '010', code: '' })
    subOptions.value = (rSub.data || []).map((i: any) => ({ codecd: i.code || i.codecd, codenm: i.cdnm || i.codenm }))

    // 관리번호 (020)
    const rMgt = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '020', code: '' })
    mgtOptions.value = (rMgt.data || []).map((i: any) => ({ codecd: i.code || i.codecd, codenm: i.cdnm || i.codenm }))

    // 상계유형 (030)
    const rCrs = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '030', code: '' })
    crsOptions.value = (rCrs.data || []).map((i: any) => ({ codecd: i.code || i.codecd, codenm: i.cdnm || i.codenm }))

    // 기타관리 (050)
    const rEct = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '050', code: '' })
    ectOptions.value = (rEct.data || []).map((i: any) => ({ codecd: i.code || i.codecd, codenm: i.cdnm || i.codenm }))

    // 계정유형 (040)
    const rType = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '040', code: '' })
    acctTypeOptions.value = (rType.data || []).map((i: any) => ({ codecd: i.code || i.codecd, codenm: i.cdnm || i.codenm }))
  } catch (e) {
    logError('드롭다운 옵션 로드 실패')
  }
}

// 5. 그리드 생성
function initGrid() {
  if (!gridelement.value) return
  grid = new Tabulator(gridelement.value, {
    layout: 'fitColumns',
    height: '100%',
    placeholder: '등록된 계정과목 데이터가 없습니다.',
    columnDefaults: { headerHozAlign: 'center', vertAlign: 'middle' },
    columns: [
      { title: '계정과목', field: 'acctcd', width: 90, hozAlign: 'center', cssClass: 'fw-bold text-primary' },
      { title: '계정명', field: 'acctnm', minWidth: 180, widthGrow: 1, hozAlign: 'left', cssClass: 'fw-bold' },
      { title: '차대', field: 'typedc', width: 70, hozAlign: 'center', formatter: (c) => c.getValue() === 'D' ? '차변' : '대변' },
      { title: '전표', field: 'slipyn', width: 70, hozAlign: 'center', formatter: (c) => c.getValue() === 'Y' ? '발행' : '미발행' },
      { title: '보조부', field: 'typesub_nm', minWidth: 110, hozAlign: 'left', formatter: (c) => c.getValue() || c.getRow().getData().typesub || '없음' },
      { title: '관리번호', field: 'typemgt_nm', minWidth: 110, hozAlign: 'left', formatter: (c) => c.getValue() || c.getRow().getData().typemgt || '없음' },
      { title: '상계', field: 'typecrs_nm', minWidth: 110, hozAlign: 'left', formatter: (c) => c.getValue() || c.getRow().getData().typecrs || '없음' },
      { title: '기타', field: 'typeect_nm', minWidth: 110, hozAlign: 'left', formatter: (c) => c.getValue() || c.getRow().getData().typeect || '없음' },
      { title: '유형', field: 'typeacct_nm', width: 90, hozAlign: 'left', formatter: (c) => c.getValue() || c.getRow().getData().typeacct || '없음' },
      { title: '증빙', field: 'profyn', width: 60, hozAlign: 'center', formatter: (c) => c.getValue() === 'Y' ? '유' : '무' },
      { title: '예산', field: 'bugtcd_nm', width: 100, hozAlign: 'left', formatter: (c) => c.getValue() || c.getRow().getData().bugtcd || '없음' },
      { title: '사용', field: 'useyn', width: 60, hozAlign: 'center', formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
    ]
  })

  grid.on('rowClick', (_e, row) => {
    const data = row.getData()
    Object.assign(formdata, data)
    formdata.actkind = 'U1'
    formdata.updyn = data.updyn || 'Y'
  })
}

// 6. 주요 액션 (조회, 저장, 삭제, 초기화)
async function search() {
  try {
    formdata.gubun = searchform.gubun
    const res = await api.post('/haba/HABA_010U_STR', {
      actkind: 'S2',
      cmpycd: authstore.cmpycd,
      gubun: searchform.gubun,
      acctcd: '',
      acctnm: searchform.acctnm,
      keyword: ''
    })
    grid?.setData(res.data || [])
    valert('조회되었습니다.')
  } catch (e) {
    valerterror('조회 실패')
  }
}

async function save() {
  if (formdata.updyn !== 'Y' && formdata.useyn !== 'Y') {
    return valerterror('미사용으로 수정할 수 없습니다.')
  }
  if (!formdata.acctcd) {
    return valerterror('계정과목을 기재해 주십시요.')
  }
  if (!formdata.typedc || formdata.typedc === '0') {
    return valerterror('차변/대변 구분을 선택해 주십시요.')
  }
  if (!formdata.slipyn || formdata.slipyn === '0') {
    return valerterror('전표발행 여부를 선택해 주십시요.')
  }

  try {
    const payload = {
      ...formdata,
      actkind: formdata.actkind || 'I1',
      cmpycd: authstore.cmpycd,
      gubun: searchform.gubun,
      userid: authstore.userid
    }
    await api.post('/haba/HABA_010U_STR', payload)
    valert('저장되었습니다.')
    search()
    initialize()
  } catch (e) {
    valerterror('저장 실패')
  }
}

async function deletedata() {
  if (!formdata.acctcd) return valerterror('삭제할 계정과목을 선택하세요.')
  if (!confirm('선택한 계정과목 정보를 삭제하시겠습니까?')) return

  try {
    await api.post('/haba/HABA_010U_STR', {
      ...formdata,
      actkind: 'D1',
      cmpycd: authstore.cmpycd,
      gubun: searchform.gubun,
      userid: authstore.userid
    })
    valert('삭제되었습니다.')
    search()
    initialize()
  } catch (e) {
    valerterror('삭제 실패')
  }
}

function initialize() {
  const currentGubun = searchform.gubun
  resetform(formdata)
  Object.assign(formdata, {
    actkind: 'I1',
    gubun: currentGubun,
    acctcd: '',
    acctnm: '',
    upacct: '',
    upacct_t: '',
    typedc: 'D',
    slipyn: 'Y',
    typesub: '000',
    typemgt: '000',
    typecrs: '000',
    typeect: '000',
    typeacct: '000',
    profyn: 'N',
    bugtcd: '',
    bugtcd_t: '',
    frgnyn: 'N',
    keyword: '',
    useyn: 'Y',
    updyn: 'Y'
  })
}

function excel() {
  grid?.download('xlsx', `계정과목관리_${searchform.gubun}_${new Date().toISOString().substring(0, 10)}.xlsx`)
}

// 7. 팝업 연동
function openHelp(type: string) {
  if (type === 'UPACCT') popVisible.upacct = true
  else if (type === 'BUGT') popVisible.bugtcd = true
}

async function searchUpAcct(keyword: string) {
  try {
    const res = await api.post('/haba/HABA_010U_STR', {
      actkind: 'S2',
      cmpycd: authstore.cmpycd,
      gubun: searchform.gubun,
      acctcd: '',
      acctnm: keyword || formdata.upacct_t || ''
    })
    upacctHelpRef.value?.setData(res.data || [])
  } catch (e) {}
}

function onUpAcctConfirm(row: any) {
  formdata.upacct = row.acctcd
  formdata.upacct_t = row.acctnm
}

async function searchBugt(keyword: string) {
  try {
    const res = await api.post('/ha00/HA00_00P_STR', {
      gubun: 'E0',
      cmpycd: authstore.cmpycd,
      gbncd: 'BUGT',
      code: keyword || formdata.bugtcd_t || ''
    })
    bugtHelpRef.value?.setData(res.data || [])
  } catch (e) {}
}

function onBugtConfirm(row: any) {
  formdata.bugtcd = row.code || row.bugtcd
  formdata.bugtcd_t = row.cdnm || row.bugtnm
}

function logError(msg: string) {
  console.error(msg)
}

// 8. 라이프사이클
onMounted(async () => {
  await fetchDropdowns()
  nextTick(() => {
    initGrid()
    search()
  })
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
