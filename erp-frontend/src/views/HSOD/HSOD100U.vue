<!--
	=============================================================
	프로그램명	: 주문등록 (HSOD100U) - 최종 수습 및 표준본
	작성일자	: 2025.03.15 (보강)
	설명        : 포커스 튕김 방지, 0원 데이터 저장 필터링, 컬럼 전수 복구
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" @close="restoreFocus" />

  <div class="erp-container d-flex flex-column h-100 bg-white" @keydown="handleGlobalShortcuts">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cart-check-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        주문관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문등록 (HSOD100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" tabindex="-1">저장(S)</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="!form_02.ordno || form_02.ordno === '0000'" tabindex="-1">삭제(D)</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 (Tabindex: 101~102) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup><col style="width: 10%" /><col style="width: 40%" /><col style="width: 10%" /><col style="width: 40%" /></colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light small">주문일자</th>
                <td><DateForm v-model:fromdt="form_01.fromdt" v-model:todt="form_01.todt" :tabindex="101" /></td>
                <th class="text-center bg-light small">주문거래명</th>
                <td><input v-model="form_01.schcustnm" class="form-control form-control-sm" placeholder="거래처명 검색" @keyup.enter="search" tabindex="102" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- ⬅️ 좌측: 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">주문 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 마스터 상세 폼 (Tabindex: 1~11) -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup><col style="width: 110px;" /><col /><col style="width: 110px;" /><col /><col style="width: 110px;" /><col /><col style="width: 110px;" /><col /></colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light small">주문부서</th>
                    <td colspan="3">
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light small">주문번호</th>
                    <td><input :value="displayOrdNo" class="form-control bg-light text-primary fw-bold text-center" readonly tabindex="-1" placeholder="자동생성" /></td>
                    <th class="required bg-light small">주문일자</th>
                    <td><input v-model="form_02.ordymd" type="date" class="form-control" tabindex="3" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">거래처</th>
                    <td colspan="3">
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly tabindex="4" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" tabindex="5"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light small">납품일자</th>
                    <td><input v-model="form_02.outymd" type="date" class="form-control" tabindex="6" /></td>
                    <th class="required bg-light small">주문종류</th>
                    <td>
                      <select v-model="form_02.ordkind" class="form-select" tabindex="7">
                        <option v-for="item in ordkindData" :key="item.code" :value="item.code">{{ item.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">배송처</th>
                    <td colspan="3">
                      <AddressPopupForm
                        v-model:trancd="form_02.trancd"
                        v-model:postno="form_02.postno"
                        v-model:address="form_02.address"
                        v-model:d_address="form_02.d_address"
                        :tabindex="8"
                        @open-address="handleOpenHelp('ADDR')"
                      />
                    </td>
                    <th class="required bg-light small">결제조건</th>
                    <td>
                      <select v-model="form_02.paycndt" class="form-select" tabindex="9">
                        <option v-for="item in paycndtData" :key="item.code" :value="item.code">{{ item.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light small">영업담당</th>
                    <td>
                      <select v-model="form_02.ordemp" class="form-select" tabindex="10">
                        <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light small">특기사항</th>
                    <td colspan="7">
                      <input
                        ref="remarkRef"
                        v-model="form_02.remark"
                        class="form-control"
                        tabindex="11"
                        @keydown.tab="handleRemarkTab"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 품목 그리드 (Tabindex: 12) -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>주문 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1" tabindex="12"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'
import { useManualStore } from '@/stores/manualStore'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
import AddressPopupForm from '@/components/AddressPopupForm.vue'

const authStore = useAuthStore()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp, restoreFocus, lastActiveElement } = useCommonHelp()
const manualStore = useManualStore()

const firstFocusRef = ref<HTMLInputElement | null>(null)
const remarkRef = ref<HTMLInputElement | null>(null)

const form_01 = reactive({ fromdt: firstDay, todt: today, schcustnm: '' })
const form_02 = reactive<any>({
  actkind: 'S0', cmpycd: authStore.cmpycd,
  ordym: today.replace(/-/g, '').substring(0, 6), ordno: '0000',
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ordymd: today, custcd: '', custnm: '', outymd: today,
  ordkind: '100', paycndt: '110', ordemp: authStore.userid,
  remark: '', sts: 'Y', totsum: 0, trancd: '', address: '', postno: '', d_address: ''
})

const displayOrdNo = computed(() => (!form_02.ordno || form_02.ordno === '0000') ? '' : `${form_02.ordym}-${form_02.ordno}`)
watch(() => form_02.balymd, (nv) => { if (nv) form_02.ordym = nv.replace(/-/g, '').substring(0, 6) })

const closingInfo = reactive({ sclsym: '' })
const ordkindData = ref<any[]>([]); const paycndtData = ref<any[]>([]); const userData = ref<any[]>([])
const tableRef1 = ref<HTMLDivElement | null>(null); const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null; let grid2: Tabulator | null = null

const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
    const container = document.createElement("div");
    container.className = "w-100 h-100 d-flex align-items-center";
    container.innerHTML = `<input type="text" class="form-control form-control-sm border-0 bg-transparent px-2" style="font-size:12px;" value="${cell.getValue() || ''}">`;
    const input = container.querySelector("input") as HTMLInputElement;
    onRendered(() => { input.focus(); input.select(); });
    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault(); e.stopPropagation();
            const cellEl = cell.getElement();
            success(input.value);
            cellEl.focus();
            handleOpenHelp('ITEM', cell.getRow());
        }
        else if (e.key === "Tab" && e.shiftKey) { if (cell.getRow() === grid2?.getRows("active")[0]) { e.preventDefault(); cancel(); remarkRef.value?.focus(); } }
    });
    return container;
};

function focusGrid() { if (grid2) { const rows = grid2.getRows("active"); if (rows.length > 0) nextTick(() => rows[0].getCell("itemnm").edit()); } }
function handleRemarkTab(e: KeyboardEvent) { if (e.key === 'Tab' && !e.shiftKey) { e.preventDefault(); focusGrid(); } }
function handleGlobalShortcuts(e: KeyboardEvent) {
    if (e.altKey) {
        const key = e.key.toLowerCase();
        if (key === 'f') { e.preventDefault(); search(); }
        else if (key === 's') { e.preventDefault(); save(); }
        else if (key === 'n') { e.preventDefault(); initialize(); }
        else if (key === 'd') { e.preventDefault(); handleFullDelete(); }
        else if (key === 'h') { e.preventDefault(); manualStore.open('HSOD100U'); }
    }
}

const initGrids = () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "일자", field: "ordymd", hozAlign: "center", width: 90 },
      { title: "거래처명", field: "custnm", hozAlign: "left" },
      { title: "주문번호", field: "ordno_full", hozAlign: "center", width: 120, cssClass: "fw-bold text-primary" }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "품목 없음", selectable: true,
    keybindings: { "navNext": "9" },
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">입력</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "품목코드", field: "itemcd", width: 100, hozAlign: "center" },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor,
        cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()),
        cellClick: (e, cell) => { if(!cell.isEditing()) cell.edit(); }
      },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "수량", field: "ordqty", width: 80, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number",
        cellKeyDown: (e, cell) => {
            if (e.key === "Tab" && !e.shiftKey) {
                e.preventDefault();
                calcRow(cell.getRow());
                const nextRow = cell.getRow().getNextRow();
                if (nextRow) { nextRow.select(); nextRow.getCell("itemnm").edit(); }
                else { addRow(); setTimeout(() => { const rs = grid2?.getRows("active") || []; rs[rs.length-1].getCell("itemnm").edit(); }, 50); }
            }
        },
        formatter: "money", formatterParams: { precision: 0 }, cellEdited: (cell) => calcRow(cell.getRow())
      },
      { title: "금액", field: "ordamt", width: 110, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cellEdited: (cell) => calcRowAmt(cell.getRow()) },
      { title: "부가세", field: "ordvat", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cellEdited: (cell) => calcRowVat(cell.getRow()) },
      { title: "합계", field: "amtsum", width: 110, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cellEdited: (cell) => calcRowTotal(cell.getRow()) },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });
}

const updateRowStatus = (row: any) => { const d = row.getData(); if (d._state === 'EXIST' && d._status !== '삭제') row.update({ _status: '수정' }); }
const calcRow = (row: any) => { const d = row.getData(); const amt = Math.round(Number(d.ordqty || 0) * Number(d.price || 0)); const vat = Math.floor(amt * 0.1); row.update({ ordamt: amt, ordvat: vat, amtsum: amt + vat }); updateRowStatus(row); }
const calcRowAmt = (row: any) => { const d = row.getData(); const qty = Number(d.ordqty || 0); const amt = Number(d.ordamt || 0); const vat = Math.floor(amt * 0.1); const price = qty > 0 ? Math.round(amt / qty) : Number(d.price || 0); row.update({ ordvat: vat, amtsum: amt + vat, price: price }); updateRowStatus(row); }
const calcRowVat = (row: any) => { const d = row.getData(); const amt = Number(d.ordamt || 0); const vat = Number(d.ordvat || 0); row.update({ amtsum: amt + vat }); updateRowStatus(row); }
const calcRowTotal = (row: any) => { const d = row.getData(); const qty = Number(d.ordqty || 0); const total = Number(d.amtsum || 0); const amt = Math.round(total / 1.1); const vat = total - amt; const price = qty > 0 ? Math.round(amt / qty) : Number(d.price || 0); row.update({ ordamt: amt, ordvat: vat, price: price }); updateRowStatus(row); }

const initialize = () => {
    resetForm(form_02);
    Object.assign(form_02, { cmpycd: authStore.cmpycd, ordno: '0000', ordymd: today, ordym: today.replace(/-/g, '').substring(0, 6), outymd: today, deptcd: authStore.deptcd, deptnm: authStore.deptnm, ordemp: authStore.userid, paycndt: '110', ordkind: '100' });
    grid2?.clearData();
    for(let i=0; i<5; i++) grid2?.addRow({ _status: '입력', _state: 'NEW', ordqty: 0, price: 0, ordamt: 0, ordvat: 0, amtsum: 0 }, false);
    nextTick(() => firstFocusRef.value?.focus());
}

async function search() {
    const res = await api.post('/hsod/HSOD_100U_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, fromdt: form_01.fromdt.replace(/-/g, ''), todt: form_01.todt.replace(/-/g, ''), custnm: form_01.schcustnm });
    grid1?.setData(res.data.map((i: any) => ({ ...i, ordno_full: `${i.ordym}-${i.ordno}` })));
    vAlert('조회되었습니다(Alt+F)');
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => d && d.length === 8 ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}` : today;
  Object.assign(form_02, { ...row, ordymd: fYmd(row.ordymd), outymd: fYmd(row.outymd) });
  try {
    const res = await api.post('/hsod/HSOD_101U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, ordym: row.ordym, ordno: row.ordno });
    grid2?.setData(res.data.map((i: any) => {
      const qty = Number(i.ordqty || 0); const amt = Number(i.ordamt || 0); const vat = Number(i.ordvat || 0);
      return { ...i, _state: 'EXIST', _status: '', price: qty > 0 ? Math.round(amt / qty) : 0, amtsum: amt + vat };
    }));
  } catch (e: any) { vAlertError('상세 로드 실패'); }
}

async function save() {
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.');
  const details = grid2?.getData().filter((r: any) => r._status && r.itemcd && Number(r.ordqty || 0) > 0 && Number(r.amtsum || 0) > 0) || [];
  if (!details.length && form_02.ordno === '0000') return vAlertError('저장할 유효한 품목이 없습니다.');

  try {
    const totalAmtSum = details.reduce((acc, cur) => acc + (Number(cur.ordamt) || 0), 0);
    const mst = { ...form_02, actkind: form_02.ordno === '0000' ? 'A0' : 'U0', ordymd: form_02.ordymd.replace(/-/g, ''), outymd: form_02.outymd.replace(/-/g, ''), totsum: totalAmtSum, sts: 'Y', updemp: authStore.userid };
    const dtl = details.map((d: any) => ({ ...d, actkind: d._status === '입력' ? 'A0' : (d._status === '삭제' ? 'D0' : 'U1'), updemp: authStore.userid }));
    await api.post('/hsod/HSOD_100U_SAVE', { mst, dtl });
    vAlert('저장되었습니다(Alt+S)'); search();
  } catch (e) { vAlertError('저장 오류'); }
}

async function handleFullDelete() {
  if (confirm('정말 삭제하시겠습니까?')) { await api.post('/hsod/HSOD_100U_SAVE', { mst: { ...form_02, actkind: 'D0' }, dtl: [] }); vAlert('삭제되었습니다(Alt+D)'); initialize(); search(); }
}

const handleRowAction = (row: any) => { const d = row.getData(); if (d._state === 'NEW') row.delete(); else row.update({ _status: d._status === '삭제' ? '' : '삭제' }); }
const deleteSelectedRows = () => { const sel = grid2?.getSelectedRows(); if (sel?.length) sel.forEach(row => handleRowAction(row)); }
const addRow = () => grid2?.addRow({ _status: '입력', _state: 'NEW', ordqty: 0, price: 0, ordamt: 0, ordvat: 0, amtsum: 0 }, false);

const handleOpenHelp = (type: string, target?: any) => {
    lastActiveElement.value = document.activeElement as HTMLElement;
    if (type === 'DEPT') openHelp('DEPT', (d: any) => { form_02.deptcd = d.deptcd; form_02.deptnm = d.deptnm; });
    else if (type === 'CUST') openHelp('CUST', (d: any) => { form_02.custcd = d.custcd; form_02.custnm = d.custnm });
    else if (type === 'ADDR') openHelp('ADDR', (d: any) => { form_02.trancd = d.trancd; form_02.postno = d.postno; form_02.address = d.address; form_02.d_address = d.d_address || ''; }, { code: form_02.custcd });
    else if (type === 'ITEM') openHelp('ITEM', (d: any) => {
        target.update({ itemcd: d.itemcd, itemnm: d.itemnm, unit: d.unit || 'EA', price: d.outcost || 0, ordqty: 1, ordamt: d.outcost || 0, ordvat: Math.floor((d.outcost || 0) * 0.1), amtsum: Math.round((d.outcost || 0) * 1.1), _status: '입력', _state: 'NEW' });
        calcRow(target);
        setTimeout(() => target.getCell("ordqty").edit(), 150);
    });
}

onMounted(() => {
    nextTick(() => { initGrids(); initialize(); });
    api.post('/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '220' }).then(r => ordkindData.value = r.data);
    api.post('/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '300' }).then(r => paycndtData.value = r.data);
    api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL' } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym; });
    api.post('/ha00/HA00_00P_STR', { gubun: 'SD', cmpycd: authStore.cmpycd, gbncd: '', code: '', remark: '' }).then(r => userData.value = r.data);
});
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
