<!--
	=============================================================
	프로그램명	: 거래명세표 (HSIO620S)
	작성일자	: 2025.02.24
	설명        : 영업 출고 내역 조회 및 화면 데이터 기반 출력/메일 전송 (도움창 복구 및 표준화)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 (버튼 크기 표준화) -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-text-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">거래명세표 (HSIO620S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="searchMaster">조회</button>
        <button class="btn-erp btn-primary" @click="printSpecification" :disabled="!selectedMasterInfo">거래명세서 출력</button>
        <button class="btn-erp btn-success" @click="printOutboundSheet" :disabled="!selectedMasterInfo">출고증 출력</button>
        <button class="btn-erp btn-info" @click="sendMail" :disabled="!selectedMasterInfo" style="color: #000 !important; font-weight: bold;">메일 전송</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup><col style="width: 10%" /><col style="width: 40%" /><col style="width: 10%" /><col style="width: 40%" /></colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">출고창고</th>
                <td>
                  <select v-model="searchData.whcd" class="form-select form-select-sm">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light">출고일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchData.fromdt" v-model:todt="searchData.todt" />
                </td>
              </tr>
              <tr>
                <th class="text-center bg-light">거래처명</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.custnm" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="searchMaster" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">확정여부</th>
                <td><select v-model="searchData.slipyn" class="form-select form-select-sm" style="max-width: 100px;"><option value="Y">확정</option></select></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">출고 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column"><div ref="masterGridElement" class="tabulator-instance flex-grow-1"></div></div>
        </div>
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 상세 품목 내역</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column"><div ref="detailGridElement" class="tabulator-instance flex-grow-1"></div></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { getDate } from '@/composables/useDate'
import { numberToHanja } from '@/utils/hanja'
import { usePrintReport } from '@/composables/usePrintReport'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { firstDay, today } = getDate()

const searchData = reactive({
    whcd: '000',
    fromdt: firstDay,
    todt: today,
    slipyn: 'Y',
    custcd: '',
    custnm: ''
})

const whOptions = ref<any[]>([]); const selectedMasterInfo = ref<any>(null);
const companyConfig = ref<any>(null);

const masterGridElement = ref<HTMLElement | null>(null); const detailGridElement = ref<HTMLElement | null>(null)
let masterGrid: Tabulator | null = null; let detailGrid: Tabulator | null = null

    const initGrids = () => {
      masterGrid = new Tabulator(masterGridElement.value!, {
      layout: "fitColumns", height: "100%", columns: [
        { title: "No", formatter: "rownum", width: 40 },
        { title: "거래처", field: "custnm", minWidth: 150, cssClass: "fw-bold text-primary cursor-pointer" }, { title: "출고번호", field: "iono_full", width: 120, hozAlign: "center", mutatorData: (v,d)=>`${d.ioym}-${d.iono}` }]
      });
      masterGrid.on("rowClick", (e, row) => {
          selectedMasterInfo.value = row.getData();
          fetchDetails(row.getData());
      });
      detailGrid = new Tabulator(detailGridElement.value!, {
      layout: 'fitColumns', height: '100%', columns: [
          { title: "품목명", field: "itemnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
          { title: "규격", field: "itsize", width: 150 },
          { title: "단위", field: "unit", width: 60, hozAlign: "center" },
          { title: "수량", field: "ioqty", width: 100, hozAlign: "right", formatter: "money" },
          { title: "금액", field: "jsanamt", width: 120, hozAlign: "right", formatter: "money" },
          { title: "부가세", field: "jsanvat", width: 110, hozAlign: "right", formatter: "money" }
      ]
      });
    }

async function searchMaster() {
    const res = await api.post('/hsio/HSIO_620S_STR', {
        actkind: 'S1',
        cmpycd: authStore.cmpycd,
        iogbn: '200',
        whcd: searchData.whcd,
        fromdt: searchData.fromdt.replace(/-/g, ''),
        todt: searchData.todt.replace(/-/g, ''),
        custcd: searchData.custcd,
        ioym: '',
        iono: '',
        slipyn: searchData.slipyn
    });

    masterGrid?.setData(res.data);
    detailGrid?.clearData();
    selectedMasterInfo.value = null;
}
async function fetchDetails(row: any) {
    const res = await api.post('/hsio/HSIO_620S_STR', {
        actkind: 'S0',
        cmpycd: authStore.cmpycd,
        iogbn: '200',
        whcd: searchData.whcd,
        fromdt: searchData.fromdt.replace(/-/g, ''),
        todt: searchData.todt.replace(/-/g, ''),
        custcd: row.custcd,
        ioym: row.ioym,
        iono: row.iono,
        slipyn: searchData.slipyn
    });
    detailGrid?.setData(res.data);
}

const { printReportSheet, printSpecification: printSpec } = usePrintReport()

const printSpecification = async () => {
    if (!selectedMasterInfo.value) return vAlertError('출고 내역을 먼저 선택하세요.')
    const m = selectedMasterInfo.value
    try {
        await printSpec(m.ioym, m.iono, detailGrid?.getData() || [], m)
    } catch(e) {
        vAlertError('거래명세서 출력 실패')
    }
}

/** 🚀 [출고의뢰서/출고증 출력] 입고증 서식과 완벽 통일 (헤더 출고번호 + 품목별 CODE128 바코드 포함) */
const printOutboundSheet = async () => {
    if (!selectedMasterInfo.value) return vAlertError('출고 내역을 먼저 선택하세요.')
    const m = selectedMasterInfo.value
    try {
        await printReportSheet('REQ_OUT', m.ioym, m.iono, detailGrid?.getData() || [], m)
    } catch(e) {
        vAlertError('출고의뢰서 출력 실패')
    }
}


const sendMail = async () => { const m = selectedMasterInfo.value; const targetEmail = String(m.email || '').trim(); if (!targetEmail.includes('@')) return vAlertError('거래처 메일 주소 없음'); if (!confirm(`${targetEmail}로 전송하시겠습니까?`)) return; try { const html = await generateSpecHtml(m); await api.post('/mail/send-statement', [{ htmlcontent: html, fromnm: authStore.cmpynm, email: targetEmail, custnm: m.custnm, custcd: m.custcd, docgb: 'TRANS', no: `${m.ioym}-${m.iono}` }]); vAlert('메일 전송 완료'); } catch (e) { vAlertError('메일 전송 실패') } }

function initialize() { resetForm(searchData); searchData.whcd = '000'; searchData.slipyn = 'Y'; masterGrid?.clearData(); detailGrid?.clearData(); selectedMasterInfo.value = null; }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })

/** 🚀 [긴급복구] 거래처 도움창 컬럼 정의 복구 */
function openHelp(type: string) {
  if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택', path: '/ha00/HA00_00P_STR',
      data: { gubun: 'C4', cmpycd: authStore.cmpycd, code: searchData.custnm },
      columns: [
        { title: '코드', field: 'custcd', width: 100, hozAlign: 'center' },
        { title: '거래처명', field: 'custnm', width: 250 }
      ],
      onConfirm: (d: any) => { searchData.custcd = d.custcd; searchData.custnm = d.custnm }
    })
    modalVisible.value = true;
  }
}

onUnmounted(() => { if (masterGrid) masterGrid.destroy(); if (detailGrid) detailGrid.destroy(); });
onMounted(async () => {
  api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } }).then(r => whOptions.value = r.data);
  api.post('/haba/HABA_100U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd }).then(r => { companyConfig.value = r.data?.[0]; });
  nextTick(() => { initGrids(); searchMaster(); });
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; border-bottom: 3px solid #005a9f !important; }
</style>
