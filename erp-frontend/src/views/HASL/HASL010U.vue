<!--
	=============================================================
	프로그램명	: 현업전표등록 (HASL010U)
	작성일자	: 2025.03.23 (Applied - Total Consistency Final)
	설명        : "신규" 용어 전수 통일 및 데이터 무결성 완결 버전 (직접 적용)
	=============================================================
-->

<template>
  <AppAlert
    :show="showAlert"
    :error="showError"
    :message="alertMessage"
  />

  <!-- 🚀 도움창 엔진 -->
  <DeptHelp v-model:visible="popVisible.dept" @confirm="onDeptConfirm" @close="restoreFocus" />
  <SaleCustHelp v-model:visible="popVisible.cust" @confirm="onCustConfirm" @close="restoreFocus" />
  <HelpBase ref="acctHelpRef" v-model:visible="popVisible.acct" title="계정과목 선택" :columns="acctPopupColumns" @search="fetchAcctPopupData" @confirm="onAcctConfirm" @close="restoreFocus" />
  <HelpBase ref="prjHelpRef" v-model:visible="popVisible.prj" title="프로젝트 선택" :columns="prjPopupColumns" @search="fetchPrjPopupData" @confirm="onPrjConfirm" @close="restoreFocus" />
  <HelpBase ref="mgtHelpRef" v-model:visible="popVisible.mgt" :title="mgtPopupTitle" :columns="mgtPopupColumns" :loading="isMgtLoading" @search="fetchMgtPopupData" @confirm="onMgtConfirm" @close="restoreFocus" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-plus me-2 text-primary" style="font-size: 18px;"></i>
        전표관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">현업전표등록 (HASL010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="searchSlips" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" :disabled="isSaving" tabindex="-1">저장(S)</button>
        <button class="btn-erp btn-print" @click="printSlip" :disabled="!masterForm.slipno" tabindex="-1">인쇄(P)</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" v-if="masterForm.slipno" tabindex="-1">삭제(D)</button>
      </div>
    </div>

    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup><col style="width: 10%" /><col style="width: 40%" /><col style="width: 10%" /><col style="width: 40%" /></colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light small">발행기간</th>
                <td><DateForm v-model:fromdt="searchParams.fromdt" v-model:todt="searchParams.todt" :tabindex="101" /></td>
                <th class="text-center bg-light small border-start">거래내역</th>
                <td><input v-model="searchParams.business" class="form-control form-control-sm" placeholder="검색어 입력" @keyup.enter="searchSlips" tabindex="102" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 320px; min-width: 320px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">전표 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="sliplistGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-shrink-0">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /></colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light text-center small">발행부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="masterForm.deptnm" class="form-control fw-bold" placeholder="부서 검색" tabindex="1" @keyup.enter="handleOpenHelp('DEPT')" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')" tabindex="2"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center small border-start">전표번호</th>
                    <td>
                      <div class="d-flex gap-1">
                        <input v-model="masterForm.slipymd" type="date" class="form-control" tabindex="3" />
                        <input v-model="masterForm.slipno" class="form-control text-center bg-light fw-bold text-primary" readonly style="width: 65px;" placeholder="0000" />
                      </div>
                    </td>
                    <th class="bg-light text-center small border-start">발행인</th>
                    <td><input v-model="masterForm.empnm" class="form-control bg-light" readonly /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center border-top small">거래내역</th>
                    <td colspan="3" class="border-top">
                      <input ref="businessRef" v-model="masterForm.business" class="form-control fw-bold text-primary" placeholder="전표 거래내역 입력" tabindex="4" @input="syncRemarkToRows" @keydown.tab="handleBusinessTab" />
                    </td>
                    <th class="bg-light text-center border-top small border-start">회계일자</th>
                    <td class="border-top"><input v-model="masterForm.acctymd" class="form-control bg-light" readonly /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="flex-grow-1 d-flex gap-2 overflow-hidden">
            <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
              <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
                <span class="fw-bold small text-dark"><i class="bi bi-list-columns me-2 text-primary"></i>분개 상세</span>
                <div class="btn-group-erp d-flex gap-1">
                   <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow()">+ 행추가</button>
                   <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows">- 행삭제</button>
                </div>
              </div>
              <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="maingridRef" class="tabulator-instance flex-grow-1" tabindex="10"></div>
              </div>
              <div class="card-footer p-0 border-top bg-dark text-white">
                <div class="row g-0 text-end fw-bold small py-1 px-3">
                  <div class="col-4 text-center border-end border-secondary">분개 합계</div>
                  <div class="col-2 text-info border-end border-secondary pe-2">{{ format_money(totalDebit) }}</div>
                  <div class="col-2 text-warning border-end border-secondary pe-2">{{ format_money(totalCredit) }}</div>
                  <div class="col-4" :class="balance === 0 ? 'text-success' : 'text-danger'">차액: {{ format_money(balance) }}</div>
                </div>
              </div>
            </div>

            <div class="card border shadow-sm flex-shrink-0 d-flex flex-column bg-white overflow-hidden side-panel-wrapper" style="width: 440px;" v-if="selectedRowData">
              <div class="card-header py-2 px-3 bg-secondary text-white small fw-bold d-flex justify-content-between align-items-center">
                 <span><i class="bi bi-gear-fill me-2"></i>관리항목 설정 <small class="opacity-75">(ID: {{ selectedRowData._rowid }})</small></span>
                 <span class="badge" :class="String(selectedRowData.dbcr || '').toLowerCase() === 'd' ? 'bg-primary' : 'bg-danger'">{{ String(selectedRowData.dbcr || '').toLowerCase() === 'd' ? '차변' : '대변' }}</span>
              </div>
              <div class="card-body p-0 overflow-auto flex-grow-1 bg-light custom-scrollbar">
                 <div class="p-2 bg-white border-bottom d-flex align-items-center gap-2 small sticky-top">
                    <span class="badge bg-light text-dark border px-2 py-1">{{ selectedRowData.acctcd }}</span>
                    <b class="text-primary">{{ selectedRowData.acctnm }}</b>
                 </div>
                 <table class="erp-table-dense side-detail-table w-100 border-0 bg-white">
                    <colgroup><col style="width: 110px;" /><col /></colgroup>
                    <tbody>
                        <template v-if="String(selectedRowData.typeacct || '').trim() === '100'">
                            <tr><th class="required bg-light text-end pe-3">유형</th><td><select v-model="selectedRowData.docno3" class="form-select form-select-sm border-0" tabindex="1001" @change="updateAutoRemark()"><option v-for="opt in entTypeOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option></select></td></tr>
                            <tr><th class="required bg-light text-end pe-3">접대일자</th><td><input v-model="selectedRowData.docno6" type="date" class="form-control border-0" tabindex="1002" @change="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3">사업번호</th><td><input v-model="selectedRowData.docno2" class="form-control border-0" placeholder="숫자만 입력" tabindex="1003" @input="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3 text-primary">상호(Enter)</th><td><div class="input-group input-group-sm"><input v-model="selectedRowData.docno4" class="form-control border-0" placeholder="직접입력/검색" tabindex="1004" @keydown.enter="handleOpenHelp('VAT_CUST')" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('VAT_CUST')" tabindex="-1"><i class="bi bi-search"></i></button></div></td></tr>
                            <tr><th class="bg-light text-end pe-3">접대상대</th><td><input v-model="selectedRowData.docno5" class="form-control border-0" tabindex="1005" @input="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3">접대목적</th><td><input v-model="selectedRowData.docno7" class="form-control border-0" placeholder="목적 입력" tabindex="1006" @input="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3 fw-bold text-primary">접대금액</th><td><input v-model="selectedRowData.docno8" type="number" class="form-control border-0 text-end fw-bold" tabindex="1007" @input="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3">봉사료</th><td><input v-model="selectedRowData.docno9" type="number" class="form-control border-0 text-end" tabindex="1008" @keydown.tab="handleDetailExit" @input="updateAutoRemark()" /></td></tr>
                        </template>

                        <template v-else-if="String(selectedRowData.typeacct || '').trim() === '090'">
                            <tr><th class="required bg-light text-end pe-3">사업장</th><td><select v-model="selectedRowData.docno1" class="form-select form-select-sm border-0" tabindex="1001" @change="updateAutoRemark()"><option v-for="opt in bizPlaceOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option></select></td></tr>
                            <tr><th class="required bg-light text-end pe-3">증빙유형</th><td><select v-model="selectedRowData.docno3" class="form-select form-select-sm border-0" tabindex="1002" @change="updateAutoRemark()"><option v-for="opt in dynamicVatOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option></select></td></tr>
                            <tr><th class="required bg-light text-end pe-3">공급처(Enter)</th><td><div class="input-group input-group-sm"><input v-model="selectedRowData.docno2nm" class="form-control border-0" placeholder="검색(Enter)" tabindex="1003" @keydown.enter="handleOpenHelp('VAT_CUST')" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('VAT_CUST')" tabindex="-1"><i class="bi bi-search"></i></button></div></td></tr>
                            <tr><th class="required bg-light text-end pe-3">발행일</th><td><input v-model="selectedRowData.docno6" type="date" class="form-control border-0" tabindex="1004" @change="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3 fw-bold text-primary">공급가액</th><td><input v-model="selectedRowData.docno8" type="number" class="form-control border-0 text-end fw-bold" tabindex="1005" @input="calcVat(); updateAutoRemark();" /></td></tr>
                            <tr><th class="bg-light text-end pe-3 text-danger">부가세액</th><td><input v-model="selectedRowData.docno9" type="number" class="form-control border-0 text-end" tabindex="1006" @input="updateAutoRemark()" /></td></tr>
                            <tr v-if="String(selectedRowData.dbcr).toLowerCase() === 'd'"><th class="bg-light text-end pe-3 text-primary">카드번호</th><td><div class="input-group input-group-sm"><input v-model="selectedRowData.docno4" class="form-control border-0" placeholder="검색(Enter)" tabindex="1007" @keydown.enter="handleOpenHelp('MGT')" @keydown.tab="handleDetailExit" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('MGT')" tabindex="-1"><i class="bi bi-credit-card"></i></button></div></td></tr>
                        </template>

                        <template v-else-if="['050', '060', '070'].includes(String(selectedRowData.typeacct || '').trim())">
                            <tr><th class="required bg-light text-end pe-3">어음번호</th><td><div class="input-group input-group-sm"><input v-model="selectedRowData.mgtno" class="form-control border-0" placeholder="번호 입력" tabindex="1001" @keydown.enter="handleOpenHelp('MGT')" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('MGT')" tabindex="-1"><i class="bi bi-search"></i></button></div></td></tr>
                            <tr><th class="bg-light text-end pe-3">발행일</th><td><input v-model="selectedRowData.docno6" type="date" class="form-control border-0" tabindex="1002" @change="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3">만기일</th><td><input v-model="selectedRowData.docno7" type="date" class="form-control border-0" tabindex="1003" @change="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3">발행은행</th><td><input v-model="selectedRowData.docno4" class="form-control border-0" tabindex="1004" @input="updateAutoRemark()" /></td></tr>
                            <tr><th class="bg-light text-end pe-3 text-primary">수취처(Enter)</th><td><div class="input-group input-group-sm"><input v-model="selectedRowData.docno9nm" class="form-control border-0" readonly placeholder="검색(Enter)" tabindex="1005" @keydown.enter="handleOpenHelp('VAT_CUST')" @keydown.tab="handleDetailExit" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('VAT_CUST')" tabindex="-1"><i class="bi bi-search"></i></button></div></td></tr>
                        </template>

                        <template v-else>
                            <tr v-if="String(selectedRowData.typemgt || '').trim() > '000'">
                                <th class="bg-light text-end pe-3 text-primary">{{ selectedRowData.titmgt || '관리번호' }}</th>
                                <td><div class="input-group input-group-sm"><input v-model="selectedRowData.mgtno" class="form-control border-0" placeholder="검색(Enter)" tabindex="1003" @keydown.enter="handleOpenHelp('MGT')" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('MGT')" tabindex="-1"><i class="bi bi-search"></i></button></div></td>
                            </tr>
                            <tr v-if="String(selectedRowData.typesub || '').trim() > '000'">
                                <th class="bg-light text-end pe-3 text-primary">{{ selectedRowData.titsub || '거래처' }}</th>
                                <td><div class="input-group input-group-sm"><input v-model="selectedRowData.subnm" class="form-control border-0" placeholder="검색(Enter)" tabindex="1004" @keydown.enter="handleOpenHelp('SUB')" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('SUB')" tabindex="-1"><i class="bi bi-search"></i></button></div></td>
                            </tr>
                            <tr v-if="String(selectedRowData.acctcd || '').trim() > '40000'">
                                <th class="bg-light text-end pe-3">프로젝트</th>
                                <td><div class="input-group input-group-sm"><input v-model="selectedRowData.prjnm" class="form-control border-0" placeholder="검색(Enter)" tabindex="1005" @keydown.enter="handleOpenHelp('PRJ')" @keydown.tab="handleDetailExit" @input="updateAutoRemark()" /><button class="btn btn-outline-secondary border-0 px-2" @click="handleOpenHelp('PRJ')" tabindex="-1"><i class="bi bi-search"></i></button></div></td>
                            </tr>
                        </template>
                    </tbody>
                 </table>
              </div>
              <div class="card-footer bg-white p-2 border-top">
                 <button ref="combineBtnRef" class="btn btn-sm btn-primary w-100 fw-bold shadow-sm" tabindex="1010" @click="updateAutoRemark(true)">적요 자동 조합 (Enter 시 다음행 추가)</button>
              </div>
            </div>

            <div class="card border shadow-sm flex-shrink-0 d-flex align-items-center justify-content-center bg-white" style="width: 440px;" v-else>
               <div class="text-center opacity-30"><i class="bi bi-cursor-fill mb-2" style="font-size: 2rem;"></i><div class="small fw-bold">분개 행을 선택하거나 적요를 클릭하면<br/>관리항목 입력창이 활성화됩니다.</div></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick, onUnmounted, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'

import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { getDate } from '@/composables/useDate'
import { useSearchStore } from '@/stores/useSearchStore'
import { useRoute } from 'vue-router'

import AppAlert from '@/components/AppAlert.vue'
import DateForm from '@/components/DateForm.vue'
import DeptHelp from '@/components/help/DeptHelp.vue'
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'
import HelpBase from '@/components/help/HelpBase.vue'

const acctPopupColumns = [{ title: '코드', field: 'acctcd', width: 100 }, { title: '계정과목명', field: 'acctnm', widthGrow: 1 }]
const prjPopupColumns = [{ title: '코드', field: 'prjcd', width: 100 }, { title: '프로젝트명', field: 'prjnm', widthGrow: 1 }]
const mgtPopupColumns = [{ title: '번호/코드', field: 'mgtno', width: 150 }, { title: '명칭', field: 'mgtnm', width: 200 }]

const authStore = useAuthStore(); const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const searchStore = useSearchStore(); const route = useRoute()

const firstFocusRef = ref<HTMLInputElement | null>(null)
const businessRef = ref<HTMLInputElement | null>(null)
const combineBtnRef = ref<HTMLElement | null>(null)
const lastActiveElement = ref<HTMLElement | null>(null)
const acctHelpRef = ref<any>(null); const prjHelpRef = ref<any>(null); const mgtHelpRef = ref<any>(null)

const popVisible = reactive({ dept: false, cust: false, acct: false, prj: false, mgt: false, sub: false, vat_cust: false })
const mgtPopupTitle = ref('관리번호 선택'); const isMgtLoading = ref(false)
const isSaving = ref(false); const totalDebit = ref(0); const totalCredit = ref(0); const balance = computed(() => totalDebit.value - totalCredit.value)

const gridData = ref<any[]>([])
const selectedRowData = ref<any>(null); const activeRowId = ref<number | null>(null)
let activeRowComponent: any = null

const purchaseVatOptions = ref<any[]>([]); const salesVatOptions = ref<any[]>([])
const bizPlaceOptions = ref<any[]>([]);
const entTypeOptions = [{code:'01', name:'카드'}, {code:'02', name:'현금'}, {code:'03', name:'세금계산서'}]

const dynamicVatOptions = computed(() => { if (!selectedRowData.value) return []; return String(selectedRowData.value.dbcr).toLowerCase() === 'd' ? purchaseVatOptions.value : salesVatOptions.value })
const masterForm = reactive<any>({ cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, slipymd: today, slipno: '', empnm: authStore.usernm, business: '', acctymd: today, slipgu: '010' })
const searchParams = reactive({ fromdt: firstDay, todt: today, business: '' })

let grid1: Tabulator | null = null; let grid2: Tabulator | null = null
const sliplistGridRef = ref<HTMLElement | null>(null); const maingridRef = ref<HTMLElement | null>(null)

const initialize = () => {
    resetForm(masterForm); selectedRowData.value = null; activeRowId.value = null; activeRowComponent = null;
    totalDebit.value = 0; totalCredit.value = 0; Object.assign(masterForm, { cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, slipymd: today, acctymd: today, empnm: authStore.usernm, slipgu: '010', business: '' });
    if (grid2) {
        grid2.setData([]);
        grid2.addRow({ _rowid: 1, srowno: '', dbcr: 'd', remark: '', amount: 0, _status: '신규', _state: 'NEW' }, false);
    }
    nextTick(() => firstFocusRef.value?.focus())
}

const restoreFocus = () => { nextTick(() => lastActiveElement.value?.focus()) }
const onDeptConfirm = (d: any) => { masterForm.deptcd = d.deptcd; masterForm.deptnm = d.deptnm; setTimeout(() => businessRef.value?.focus(), 150); }

const onCustConfirm = (d: any) => {
    if (!activeRowComponent) return;
    const rowData = activeRowComponent.getData();
    rowData.subcd = d.custcd || ''; rowData.subnm = d.custnm || '';
    const typeAcct = String(rowData.typeacct || '').trim();
    if (typeAcct === '100') { rowData.docno4 = d.custnm || ''; rowData.docno2 = d.busino || d.custcd || ''; }
    else if (typeAcct === '090') { rowData.docno2nm = d.custnm || ''; rowData.docno2 = d.busino || d.custcd || ''; }
    else { rowData.docno9nm = d.custnm || ''; rowData.docno9 = d.custcd || ''; }

    activeRowComponent.update(rowData);
    selectedRowData.value = activeRowComponent.getData();
    updateAutoRemark(); handleDetailAutoNext();
}

const onMgtConfirm = (res: any) => {
    if (!activeRowComponent) return;
    const rowData = activeRowComponent.getData();
    rowData.mgtno = res.mgtno || ''; rowData.mgtnm = res.mgtnm || '';
    activeRowComponent.update(rowData);
    selectedRowData.value = activeRowComponent.getData();
    updateAutoRemark(); handleDetailAutoNext();
}

const needsDetail = (d: any) => { if (!d) return false; const typeAcct = String(d.typeacct || '').trim(); return (String(d.typemgt || '').trim() > '000') || (String(d.typesub || '').trim() > '000') || ['100', '090', '050', '060', '110'].includes(typeAcct) || (Number(d.acctcd) > 40000); }

const onAcctConfirm = (d: any) => {
    if (!activeRowComponent) return;
    activeRowComponent.update({
        acctcd: d.acctcd || '', acctnm: d.acctnm || '', typeacct: d.typeacct || '', typemgt: d.typemgt || '',
        typesub: d.typesub || '', typedc: d.typedc || '', typebugt: d.bugtcd || '', titmgt: d.titmgt || '', titsub: d.titsub || '',
        docno3: String(d.typeacct).trim() === '100' ? '01' : '', _status: '신규', _state: 'NEW'
    });
    selectedRowData.value = activeRowComponent.getData();
    setTimeout(() => { activeRowComponent.getCell("amount").edit(); }, 150)
}

const onPrjConfirm = (d: any) => {
    if (!activeRowComponent) return;
    const rowData = activeRowComponent.getData();
    rowData.prjcd = d.prjcd || ''; rowData.prjnm = d.prjnm || '';
    activeRowComponent.update(rowData);
    selectedRowData.value = activeRowComponent.getData();
    updateAutoRemark(); handleDetailAutoNext();
}

watch(selectedRowData, (newval) => {
    if (newval && activeRowComponent) {
        activeRowComponent.update(newval);
        updateAutoRemark();
    }
}, { deep: true });

const updateAutoRemark = (isFinalStep = false) => {
    if (!activeRowComponent) return;
    const d = activeRowComponent.getData(); const parts = new Set<string>();
    if (d.subnm) parts.add(d.subnm.trim()); if (d.mgtnm) parts.add(d.mgtnm.trim()); if (d.docno2nm && d.docno2nm !== d.subnm) parts.add(d.docno2nm.trim()); if (d.docno4 && d.docno4 !== d.subnm) parts.add(d.docno4.trim()); if (d.prjnm) parts.add(d.prjnm.trim());
    const summary = parts.size > 0 ? Array.from(parts).join(' / ') : (masterForm.business || '');
    activeRowComponent.update({ remark: summary });
    selectedRowData.value = activeRowComponent.getData();
    if (isFinalStep) { if (balance.value === 0) { vAlert('전표 완결 - 입력을 종료합니다.'); return; } setTimeout(() => addRow(), 10); }
}

const amountEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const input = document.createElement("input"); input.type = "text"; input.className = "form-control form-control-sm text-end border-0 bg-transparent p-0 pe-2"; input.style.fontSize = "12px"; input.value = cell.getValue() || 0
  onRendered(() => { input.focus(); input.select(); });
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter" || e.key === "Tab") {
      e.preventDefault(); e.stopImmediatePropagation();
      success(Number(input.value.replace(/,/g, '')) || 0);
      focusDetailPanel();
    } else if (e.key === "Escape") cancel()
  });
  input.addEventListener("blur", () => { success(Number(input.value.replace(/,/g, '')) || 0) }); return input
}

const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
  const field = cell.getField(); const container = document.createElement("div"); container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2"; container.innerHTML = `<input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex:1;"><i class="bi bi-search text-primary ms-1" style="font-size:11px; cursor:pointer;"></i>`
  const input = container.querySelector("input") as HTMLInputElement; const icon = container.querySelector("i") as HTMLElement;
  const triggerHelp = () => { success(input.value); if (field === 'acctnm') handleOpenHelp('acct', cell.getRow()) }
  onRendered(() => { input.value = cell.getValue() || ''; input.focus(); input.select(); });
  input.addEventListener("keydown", (e) => { if (e.key === "Enter") { e.preventDefault(); e.stopPropagation(); triggerHelp(); } }); icon.addEventListener("click", triggerHelp); return container
}

const markEdit = (row: any) => { const d = row.getData(); if (d._state === 'EXIST' && d._status !== '삭제' && d._status !== '신규') row.update({ _status: '수정' }); updateTotals() }
const updateTotals = () => { if(!grid2) return; const data = grid2.getRows().map(r => r.getData()).filter(r => r._status !== '삭제') || []; totalDebit.value = data.reduce((s, r: any) => s + (String(r.dbcr).toLowerCase() === 'd' ? Number(r.amount || 0) : 0), 0); totalCredit.value = data.reduce((s, r: any) => s + (String(r.dbcr).toLowerCase() === 'c' ? Number(r.amount || 0) : 0), 0); }

async function searchSlips() { const res = await api.post('/hasl/HASL_010U_STR', { actkind: 'F', cmpycd: authStore.cmpycd, fromdt: searchParams.fromdt.replace(/-/g, ''), todt: searchParams.todt.replace(/-/g, ''), keyword: searchParams.business }); grid1?.setData(res.data || []); vAlert('조회되었습니다(Alt+F)') }
async function fetchDetail(row: any) {
    Object.assign(masterForm, row); if (masterForm.slipymd?.length === 8) { const d = masterForm.slipymd; masterForm.slipymd = `${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}` }
    try {
      const resd = await api.post('/hasl/HASL_011U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, slipymd: masterForm.slipymd.replace(/-/g,''), slipno: masterForm.slipno });
      // 🚀 [해결] 서버의 dbamt, cramt를 그리드의 amount 필드로 합산 매핑
      const data = (resd.data || []).map((i:any) => ({
        ...i,
        amount: Number(i.dbamt || 0) + Number(i.cramt || 0),
        _rowid: Number(i.srowno),
        _state: 'EXIST', _status: '',
        subnm: i.custnm || '', subcd: i.custcd || '',
        docno9nm: i.docno9nm || i.docno9 || ''
      }));
      grid2?.setData(data); updateTotals()
    } catch (e) { vAlertError('상세 조회 실패') }
}

async function save() {
  if (isSaving.value) return;
  isSaving.value = true;

  try {
    if (balance.value !== 0) { vAlertError('차/대변 합계 불일치'); isSaving.value = false; return; }
    if (!masterForm.business) { vAlertError('거래내역 필수'); isSaving.value = false; return; }

    if(!grid2) return;
    const allData = grid2.getRows().map(r => r.getData()).filter(r => r.acctcd && Number(r.amount) !== 0);

    if (allData.length < 2) { vAlertError('최소 차변/대변 각 1개 이상의 행이 필요합니다.'); isSaving.value = false; return; }

    const cleanCmpycd = (authStore.cmpycd || '').trim();
    const cleanUserid = (authStore.userid || '').trim();

    const payload = {
      actkind: masterForm.slipno ? 'U' : 'A',
      master: {
        ...masterForm,
        cmpycd: cleanCmpycd,
        userid: cleanUserid,
        updemp: cleanUserid,
        slipymd: (masterForm.slipymd || '').replace(/-/g, ''),
        acctymd: '' // 🚀 [업무 규칙] 현업전표 저장 시 회계일자는 공백으로 전달 (확정 프로세스에서 처리)
      },
      details: allData.map(item => ({
        actkind: (item._status === '신규' || !item.srowno) ? 'A' : (item._status === '삭제' ? 'D' : 'U'),
        cmpycd: cleanCmpycd,
        slipymd: (masterForm.slipymd || '').replace(/-/g, ''),
        slipno: (masterForm.slipno || '').trim(),
        srowno: (item._status === '신규' || !item.srowno) ? '' : item.srowno,
        acctymd: (masterForm.acctymd || '').replace(/-/g, ''),
        acctcd: (item.acctcd || '').trim(),
        deptcd: (masterForm.deptcd || '').trim(),
        custcd: (item.subcd || item.custcd || '').trim(),
        bugtcd: (item.bugtcd || '').trim(),
        prjcd: (item.prjcd || '').trim(),
        mgtno: (item.mgtno || '').trim(),
        sslipno: '',
        dbamt: String(item.dbcr).toLowerCase() === 'd' ? Number(item.amount || 0) : 0,
        cramt: String(item.dbcr).toLowerCase() === 'c' ? Number(item.amount || 0) : 0,
        remark: (item.remark || '').trim(),
        paycndt: '', payymd: '',
        docno1: (item.docno1 || '').trim(),
        docno2: (item.docno2 || '').trim(),
        docno3: (item.docno3 || '').trim(),
        docno4: (item.docno4 || '').trim(),
        docno5: (item.docno5 || '').trim(),
        docno6: (item.docno6 || '').replace(/-/g, ''),
        docno7: (item.docno7 || '').replace(/-/g, ''),
        docno8: Number(item.docno8 || 0),
        docno9: Number(item.docno9 || 0),
        updemp: cleanUserid,
        frgnkind: '', frgnrate: 0, frgnamt: 0, ret_yn: 'N'
      }))
    }
    console.log(payload);

    const res = await api.post('/hasl/HASL_010U_SAVE', payload);
    if (res.data?.success === false) throw new Error(res.data.message || '저장 오류');

    vAlert('저장되었습니다(Alt+S)');
    initialize();
    searchSlips();
  } catch (e: any) {
    vAlertError(e.message || '저장 실패');
  } finally {
    isSaving.value = false;
  }
}

const addRow = (noFocus = false) => {
    if(!grid2) return;
    const currentData = grid2.getRows().map(r => r.getData()) || [];
    const maxRowId = currentData.reduce((max, r) => Math.max(max, Number(r._rowid || 0)), 0);
    const newRow = { _rowid: maxRowId + 1, srowno: '', dbcr: (currentData.length && String(currentData[currentData.length-1].dbcr).toLowerCase() === 'd') ? 'c' : 'd', remark: (masterForm.business || ''), amount: Math.abs(balance.value), prjcd: '', mgtno: '', subcd: '', _status: '신규', _state: 'NEW' }
    grid2.addRow(newRow, false).then(row => { if (!noFocus) { nextTick(() => { row.select(); activeRowId.value = row.getData()._rowid; activeRowComponent = row; selectedRowData.value = row.getData(); row.getCell("acctnm").edit(); }) } })
}

const focusDetailPanel = () => { setTimeout(() => { const panel = document.querySelector('.side-panel-wrapper'); if (panel) { const firstInput = panel.querySelector('input:not([readonly]):not([disabled]), select:not([disabled])') as HTMLElement; if (firstInput) { firstInput.focus(); if (firstInput instanceof HTMLInputElement) firstInput.select(); } } }, 120) }
const handleDetailExit = (e: KeyboardEvent) => { if (e.key === 'Tab' && !e.shiftKey) { e.preventDefault(); combineBtnRef.value?.focus(); } }
const handleDetailAutoNext = () => { nextTick(() => { const active = document.activeElement as HTMLElement; const panel = document.querySelector('.side-panel-wrapper'); if (panel) { const inputs = Array.from(panel.querySelectorAll('input:not([readonly]), select')); const idx = inputs.indexOf(active); if (idx > -1 && idx < inputs.length - 1) (inputs[idx + 1] as HTMLElement).focus(); else combineBtnRef.value?.focus(); } }) }

const fetchMgtPopupData = async (word: string) => { const rowData = selectedRowData.value; if (!rowData) return; api.post('/ha00/HA00_00P_STR', { gubun: 'M0', cmpycd: authStore.cmpycd, gbncd: rowData.typemgt, remark: rowData.acctcd, code: word || '' }).then(r => mgtHelpRef.value?.setData(r.data)) }
const handleOpenHelp = (type: string, target?: any) => {
  lastActiveElement.value = document.activeElement as HTMLElement
  if (type === 'DEPT') popVisible.dept = true
  else if (type === 'acct') { activeRowComponent = target; popVisible.acct = true; }
  else if (type === 'PRJ' || type === 'MGT' || type === 'VAT_CUST' || type === 'SUB') {
    if (type === 'MGT') { const rowData = selectedRowData.value; mgtPopupTitle.value = rowData.titmgt || '관리번호 선택'; popVisible.mgt = true; isMgtLoading.value = true; api.post('/ha00/HA00_00P_STR', { gubun: 'M0', cmpycd: authStore.cmpycd, gbncd: rowData.typemgt, remark: rowData.acctcd, code: '' }).then(r => { mgtHelpRef.value?.setData(r.data); }).finally(() => { isMgtLoading.value = false; }) }
    else if (type === 'PRJ') popVisible.prj = true
    else popVisible.cust = true
  }
}

const fetchAcctPopupData = async (word: string) => { api.post('/ha00/HA00_00P_STR', { gubun: 'A0', cmpycd: authStore.cmpycd, gbncd: '', code: word || '' }).then(r => acctHelpRef.value?.setData(r.data)) }
const fetchPrjPopupData = async (word: string) => { api.post('/ha00/HA00_00P_STR', { gubun: 'J0', cmpycd: authStore.cmpycd, gbncd: '', code: word || '' }).then(r => prjHelpRef.value?.setData(r.data)) }
const syncRemarkToRows = () => { if(!grid2) return; grid2.getRows().forEach(r => { const d = r.getData(); if(!d.remark || d.remark === "") r.update({ remark: (masterForm.business || '') }) }) }

function handleBusinessTab(e: KeyboardEvent) {
  if (e.key === 'Tab' && !e.shiftKey) {
    e.preventDefault()
    if (grid2) {
      const rows = grid2.getRows()
      if (rows.length > 0) setTimeout(() => rows[0].getCell("acctnm").edit(), 100)
    }
  }
}

function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const k = e.key.toLowerCase();
    if (k === 'f') { e.preventDefault(); searchSlips() }
    else if (k === 's') { e.preventDefault(); if(!isSaving.value) save() }
    else if (k === 'n') { e.preventDefault(); initialize() }
  }
}

const deleteSelectedRows = () => {
    const rows = grid2?.getSelectedRows();
    if (!rows || rows.length === 0) return;
    rows.forEach(row => { const d = row.getData(); if (d._state === 'NEW') row.delete(); else row.update({ _status: '삭제' }); });
    updateTotals();
}

const handleFullDelete = async () => {
    // 🚀 [업무 규칙] acctymd(회계일자)가 있으면 이미 확정된 전표이므로 삭제 불가
    if (masterForm.acctymd && masterForm.acctymd.trim() !== "") {
        vAlertError('이미 확정된 전표이므로 삭제할 수 없습니다. (확정 취소 후 삭제 가능)');
        return;
    }
    if (!masterForm.slipno || !confirm('현재 전표를 전체 삭제하시겠습니까?')) return;
    try {
        const payload = {
            actkind: 'D',
            master: { ...masterForm, slipymd: masterForm.slipymd.replace(/-/g, ''), acctymd: '' },
            details: []
        };
        const res = await api.post('/hasl/HASL_010U_SAVE', payload);
        if (res.data?.success === false) throw new Error(res.data.message || '삭제 실패');
        vAlert('삭제되었습니다.'); initialize(); searchSlips();
    } catch (e: any) { vAlertError(e.message || '삭제 실패'); }
}

const printSlip = () => { if (masterForm.slipno) vAlert('인쇄 기능을 호출합니다.'); }

onMounted(() => {
  api.post('/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '120' }).then(r => purchaseVatOptions.value = r.data); api.post('/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '130' }).then(r => salesVatOptions.value = r.data); api.post('/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd }).then(r => { bizPlaceOptions.value = (r.data || []).map(n => ({ code: n.taxunit || n.code || '', name: n.unitnm || n.codenm || '' })) })
  grid2 = new Tabulator(maingridRef.value!, {
    reactiveData: false, layout: "fitColumns", height: "100%", selectable: 1, keybindings: { "navNext": "9", "navPrev": "shift + 9" }, columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
        { title: "No", field: "_rowid", width: 40, hozAlign: "center" },
        { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
            const v = c.getValue();
            if (v === '신규') return '<span class="badge bg-primary">신규</span>';
            if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
            if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
            return '';
        }},
        { title: "차/대", field: "dbcr", width: 70, hozAlign: "center", editor: "list", editorParams: { values: { "d": "차변", "c": "대변" } }, formatter: (c) => String(c.getValue()).toLowerCase() === 'd' ? '<b class="text-primary">차변</b>' : '<b class="text-danger">대변</b>' },
        { title: "계정과목", field: "acctnm", width: 160, editor: lookupEditor, cssClass: "cursor-pointer text-primary fw-bold" },
        { title: "적요", field: "remark", minWidth: 200, editor: "input", cellClick: (e, cell) => { const row = cell.getRow(); row.select(); activeRowId.value = row.getData()._rowid; activeRowComponent = row; selectedRowData.value = row.getData(); if (needsDetail(selectedRowData.value)) setTimeout(() => focusDetailPanel(), 50); } },
        { title: "금액", field: "amount", width: 110, hozAlign: "right", editor: amountEditor, formatter: "money" },
        { title: "삭제", width: 40, formatter: () => '<i class="bi bi-trash text-danger"></i>', cellClick: (e, cell) => { const d = cell.getRow().getData(); if(d._state === 'NEW') cell.getRow().delete(); else cell.getRow().update({_status: '삭제'}); updateTotals(); } }
    ]
  });
  grid2.on("tableBuilt", () => initialize());
  grid2.on("rowSelected", (row) => { activeRowId.value = row.getData()._rowid; activeRowComponent = row; selectedRowData.value = row.getData(); });
  grid2.on("cellEdited", (cell) => { markEdit(cell.getRow()); });
  grid1 = new Tabulator(sliplistGridRef.value!, {
    layout: "fitColumns",
    height: "100%",
    selectable: 1,
    columns: [
        { title: "No", formatter: "rownum", width: 40 },
        { title: "발행일", field: "slipymd", width: 95, hozAlign: "center", formatter: (c) => { const v = c.getValue() || ''; return v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v } },
        { title: "거래내역", field: "business", hozAlign: "left", headerSort: false },
        { title: "전표금액", field: "slipamt", width: 100, hozAlign: "right", formatter: "money", headerSort: false } // 🚀 [추가] 목록에 금액 컬럼 반영
    ]
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));
  window.addEventListener('keydown', handleGlobalShortcuts);
})
onUnmounted(() => { window.removeEventListener('keydown', handleGlobalShortcuts); searchStore.removeTab(route.name as string) })
const format_money = (n: any) => Number(n || 0).toLocaleString()
const calcVat = () => { if (selectedRowData.value && String(selectedRowData.value.typeacct || '').trim() === '090') selectedRowData.value.docno9 = Math.floor(Number(selectedRowData.value.docno8 || 0) * 0.1); }
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
.side-detail-table th { padding: 4px 10px !important; font-size: 12px; background-color: #f8f9fa; border-right: 1px solid #eee; border-bottom: 1px solid #eee !important; white-space: nowrap; height: 32px; }
.side-detail-table td { padding: 2px 6px !important; border-bottom: 1px solid #eee !important; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #ccc; border-radius: 3px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
