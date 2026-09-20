<!--
	=============================================================
	프로그램명	: 무주문 출고등록 (HSIO500U) - 키보드 표준 (이동 보강형)
	작성일자	: 2025.03.15 (최종 수습)
	설명        : 팝업 선택 후 다음 필드로 자동 이동 표준 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" @close="restoreFocus" />

  <div class="erp-container d-flex flex-column h-100 bg-white" @keydown="handleGlobalShortcuts">
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
          <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
            <i class="bi bi-cart-check-fill me-2 text-primary" style="font-size: 18px;"></i> 영업관리
            <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
            <span class="text-primary fw-bolder text-nowrap">무주문 출고등록 (HSIO500U)</span>
        </div>
        <div class="btn-group-erp d-flex gap-1 pe-3">
            <button class="btn-erp btn-init" @click="initialize" tabindex="-1">신규(N)</button>
            <button class="btn-erp btn-search" @click="search" tabindex="-1">조회(F)</button>
            <button class="btn-erp btn-save" @click="save" tabindex="-1">저장(S)</button>
            <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="!form_02.iono" tabindex="-1">삭제(D)</button>
        </div>
    </div>

    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup><col style="width: 10%" /><col style="width: 40%" /><col style="width: 10%" /><col style="width: 40%" /></colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light small">출고일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="form_01.fromdt" v-model:todt="form_01.todt" :tabindex="101" />
                </td>
                <th class="text-center bg-light small">거래처명</th>
                <td><input v-model="form_01.custnm" class="form-control form-control-sm" placeholder="거래처명 검색" @keyup.enter="search" tabindex="102" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">출고 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="tableRef1" class="tabulator-instance h-100"></div>
          </div>
        </div>

        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup><col style="width: 110px;" /><col /><col style="width: 110px;" /><col /><col style="width: 110px;" /><col /><col style="width: 110px;" /><col /></colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light small">판매부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input ref="firstFocusRef" v-model="form_02.deptnm" class="form-control" readonly tabindex="1" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" tabindex="2"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light small">출고번호</th>
                    <td><input :value="form_02.ioym && form_02.iono ? `${form_02.ioym}-${form_02.iono}` : ''" class="form-control bg-light text-primary fw-bold text-center" readonly tabindex="-1" /></td>
                    <th class="required bg-light small">출고일자</th>
                    <td><input v-model="form_02.ioymd" type="date" class="form-control" tabindex="3" /></td>
                    <th class="required bg-light small">출고창고</th>
                    <td><select v-model="form_02.whcd" class="form-select" tabindex="4"><option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option></select></td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">거래처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly tabindex="5" />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" tabindex="6"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light small text-center">여신잔액</th>
                    <td><input :value="formatNumber(form_02.janamt)" class="form-control bg-light text-end" readonly tabindex="-1" /></td>
                    <th class="bg-light small text-center">여신기한</th>
                    <td><input v-model="form_02.rcvdd" class="form-control bg-light text-center" readonly tabindex="-1" /></td>
                    <th class="required bg-light small text-center">영업담당</th>
                    <td><select v-model="form_02.sale_userid" class="form-select" tabindex="7"><option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option></select></td>
                  </tr>
                  <tr>
                    <th class="required bg-light small">배송처</th>
                    <td colspan="3">
                      <AddressPopupForm
                        v-model:trancd="form_02.addrcd"
                        v-model:postno="form_02.postno"
                        v-model:address="form_02.address"
                        v-model:d_address="form_02.d_address"
                        :tabindex="8"
                        @open-address="handleOpenHelp('ADDR')"
                      />
                    </td>
                    <th class="bg-light small text-center">배송담당</th>
                    <td><select v-model="form_02.trnemp" class="form-select" tabindex="9"><option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option></select></td>
                    <th class="required bg-light small text-center">현금조건</th>
                    <td><select v-model="form_02.paycndt" class="form-select" tabindex="10"><option v-for="item in paycndtData" :key="item.code" :value="item.code">{{ item.cdnm }}</option></select></td>
                  </tr>
                  <tr>
                    <th class="bg-light small text-center">특기사항</th>
                    <td colspan="3"><input ref="remarkRef" v-model="form_02.remark" class="form-control" tabindex="11" @keydown.tab="handleRemarkTab" /></td>
                    <th class="bg-light small text-center">만기일자</th>
                    <td><input v-model="form_02.endymd" type="date" class="form-control" tabindex="12" /></td>
                    <th class="bg-light small text-center text-primary fw-bold">합계금액</th>
                    <td><input v-model="form_02.totsum" class="form-control bg-light text-end fw-bold text-primary" readonly tabindex="-1" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>출고 품목 리스트</span>
              <div class="btn-group-erp d-flex gap-1">
                 <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                 <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="tableRef2" class="tabulator-instance h-100" tabindex="13"></div>
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
const { modalVisible, modalProps, openHelp, restoreFocus } = useCommonHelp()
const manualStore = useManualStore()

const firstFocusRef = ref<HTMLInputElement | null>(null)
const remarkRef = ref<HTMLInputElement | null>(null)
const form_01 = reactive({ fromdt: firstDay, todt: today, schcustnm: '' })
const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, iogbn: '200',
  ioym: '', iono: '', ioymd: today, whcd: '', custcd: '', custnm: '', iotype: '100',
  sale_userid: authStore.userid, trnemp: authStore.userid, paycndt: '100', remark: '', totsum: 0, endymd: today
})

const closingInfo = reactive({ sclsym: '' })
const whOptions = ref<any[]>([]); const userData = ref<any[]>([]); const paycndtData = ref<any[]>([])
const tableRef1 = ref<HTMLDivElement | null>(null); const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null; let grid2: Tabulator | null = null

const isClosed = computed(() => closingInfo.sclsym && form_02.ioymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym)

const lookupEditor = (cell: any, onRendered: any, success: any, cancel: any) => {
    const container = document.createElement("div");
    container.className = "w-100 h-100 d-flex align-items-center justify-content-between px-2";
    container.innerHTML = `<input type="text" class="form-control form-control-sm border-0 bg-transparent p-0" style="font-size:12px; flex: 1;" value="${cell.getValue() || ''}"><i class="bi bi-search text-primary ms-1" style="font-size: 11px;"></i>`;
    const input = container.querySelector("input") as HTMLInputElement;
    onRendered(() => { input.focus(); input.select(); });
    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") { e.preventDefault(); e.stopPropagation(); success(input.value); handleOpenHelp('ITEM', cell.getRow()); }
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
        else if (key === 'h') { e.preventDefault(); manualStore.open('HSIO500U'); }
    }
}

const initGrids = () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "일자", field: "ioymd", hozAlign: "center", width: 90, headerSort: false },
      { title: "출고번호", field: "iono_full", hozAlign: "center", width: 100, cssClass: "fw-bold text-primary", headerSort: false, mutator: (v, d) => d.ioym && d.iono ? `${d.ioym}-${d.iono}` : v },
      { title: "거래처", field: "custnm", hozAlign: "left", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "품목 없음", selectable: true,
    columnCalcs: "table", keybindings: { "navNext": "9" },
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 50, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">입력</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', editor: lookupEditor, cellDblClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()), cellClick: (e, cell) => { if(!cell.isEditing()) cell.edit(); } },
      { title: "규격", field: "itsize", width: 130 },
      { title: "단위", field: "unit", width: 60, hozAlign: "center" },
      { title: "수량", field: "ioqty", width: 80, hozAlign: "right", editor: "number", bottomCalc: "sum", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", bottomCalc: "sum",
        cellKeyDown: (e, cell) => {
            if (e.key === "Tab" && !e.shiftKey) {
                e.preventDefault(); calcRow(cell.getRow());
                const nextRow = cell.getRow().getNextRow();
                if (nextRow) { nextRow.select(); nextRow.getCell("itemnm").edit(); }
                else { addRow(); setTimeout(() => { const rs = grid2?.getRows("active") || []; rs[rs.length-1].getCell("itemnm").edit(); }, 50); }
            }
        },
        cellEdited: (cell) => calcRow(cell.getRow())
      },
      { title: "금액", field: "ioamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "부가세", field: "iovat", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "합계", field: "sumamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });
}

const updateRowStatus = (row: any) => { const d = row.getData(); if (d._state === 'EXIST' && d._status !== '삭제') row.update({ _status: '수정' }); }
const calcRow = (row: any) => {
    const d = row.getData(); const amt = Math.round(Number(d.ioqty || 0) * Number(d.price || 0)); const vat = Math.floor(amt * 0.1);
    row.update({ ioamt: amt, iovat: vat, sumamt: amt + vat }); updateRowStatus(row); updateTotalSum();
}
const updateTotalSum = () => { const rs = grid2?.getData().filter(r => r._status !== '삭제') || []; form_02.totsum = rs.reduce((s, r) => s + Number(r.sumamt || 0), 0); }

const fetchWhOptions = async () => {
  try {
    const resWh = await api.get('/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
    whOptions.value = (resWh.data || []).map((i: any) => ({ whcd: String(i.whcd || '').trim(), whnm: String(i.whnm || '').trim() }))
    if (whOptions.value.length > 0 && !form_02.whcd) form_02.whcd = whOptions.value[0].whcd
  } catch (e) { whOptions.value = [] }
}

const initialize = () => {
    resetForm(form_02);
    Object.assign(form_02, { cmpycd: authStore.cmpycd, ioymd: today, endymd: today, deptcd: authStore.deptcd, deptnm: authStore.deptnm, sale_userid: authStore.userid, trnemp: authStore.userid, paycndt: '100', iogbn: '200' });
    grid2?.clearData();
    for(let i=0; i<5; i++) grid2?.addRow({ _status: '입력', _state: 'NEW', ioqty: 0, price: 0, ioamt: 0, iovat: 0, sumamt: 0 }, false);
    nextTick(() => { firstFocusRef.value?.focus(); fetchWhOptions(); });
}

async function search() {
    const res = await api.post('/hsio/HSIO_500U_STR', { ...form_01, actkind: 'L', cmpycd: authStore.cmpycd, fromdt: form_01.fromdt.replace(/-/g, ''), todt: form_01.todt.replace(/-/g, ''), iogbn: '200' });
    grid1?.setData(res.data || []); vAlert('조회되었습니다(Alt+F)');
}

async function fetchDetail(row: any) {
  Object.assign(form_02, row);
  try {
    const res = await api.post('/hsio/HSIO_501U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, iogbn: '200', ioym: row.ioym, iono: row.iono });
    grid2?.setData(res.data.map((i: any) => {
      const qty = Number(i.ioqty || 0); const amt = Number(i.ioamt || 0); const vat = Number(i.iovat || 0);
      return { ...i, _state: 'EXIST', _status: '', price: qty > 0 ? Math.round(amt / qty) : 0, sumamt: amt + vat };
    }));
    updateTotalSum();
  } catch (e) { vAlertError('상세 로드 실패'); }
}

async function save() {
  if (isClosed.value) return vAlertError('마감된 월입니다.');
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.');
  const details = grid2?.getData().filter((r: any) => r._status && r.itemcd && Number(r.ioqty || 0) > 0 && Number(r.sumamt || 0) > 0) || [];
  if (!details.length && !form_02.iono) return vAlertError('저장할 유효한 품목이 없습니다.');
  const ioymd = form_02.ioymd.replace(/-/g, '');
  const mst = { ...form_02, actkind: form_02.iono ? 'U' : 'A', ioym: ioymd.substring(0, 6), ioymd, endymd: form_02.endymd.replace(/-/g, ''), updemp: authStore.userid };
  const dtl = details.map((d: any) => ({ ...d, ioymd, actkind: d._status === '입력' ? 'A' : (d._status === '삭제' ? 'D' : 'U'), updemp: authStore.userid }));
  await api.post('/hsio/HSIO_500U_SAVE', { mst, dtl });
  vAlert('저장되었습니다(Alt+S)'); search();
}

async function handleFullDelete() {
  if (isClosed.value) return vAlertError('마감된 월입니다.');
  if (confirm('정말 삭제하시겠습니까?')) { await api.post('/hsio/HSIO_500U_STR', { ...form_02, actkind: 'D', iogbn: '200', ioymd: form_02.ioymd.replace(/-/g, ''), updemp: authStore.userid }); vAlert('삭제되었습니다(Alt+D)'); initialize(); search(); }
}

const handleRowAction = (row: any) => { const d = row.getData(); if (d._state === 'NEW') row.delete(); else row.update({ _status: d._status === '삭제' ? '' : '삭제' }); updateTotalSum(); }
const deleteSelectedRows = () => { const sel = grid2?.getSelectedRows(); if (sel?.length) sel.forEach(row => handleRowAction(row)); }
const addRow = () => grid2?.addRow({ _status: '입력', _state: 'NEW', ioqty: 0, price: 0, ioamt: 0, iovat: 0, sumamt: 0 }, false);

const handleOpenHelp = (type: string, target?: any) => {
    if (type === 'DEPT') openHelp('DEPT', (d: any) => { form_02.deptcd = d.deptcd; form_02.deptnm = d.deptnm; });
    else if (type === 'CUST') openHelp('CUST', (d: any) => { form_02.custcd = d.custcd; form_02.custnm = d.custnm; api.post('/hs00/HS00_150S_STR', { cmpycd: authStore.cmpycd, custnm: d.custnm }).then(r => { if(r.data?.length) { form_02.rcvdd = r.data[0].rcvdd; form_02.janamt = r.data[0].janamt; } }) });
    else if (type === 'ADDR') openHelp('ADDR', (d: any) => { form_02.addrcd = d.trancd; form_02.postno = d.postno; form_02.address = d.address; form_02.d_address = d.d_address || ''; }, { code: form_02.custcd });
    else if (type === 'ITEM') openHelp('ITEM', (d: any) => {
        target.update({ itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unit: d.unit, price: d.outcost || 0, ioqty: 1, ioamt: d.outcost || 0, iovat: Math.floor((d.outcost || 0) * 0.1), sumamt: Math.round((d.outcost || 0) * 1.1), _status: '입력', _state: 'NEW' });
        calcRow(target);
        setTimeout(() => target.getCell("ioqty").edit(), 150);
    });
}

const formatNumber = (n: any) => Number(n || 0).toLocaleString();

onMounted(async () => {
  await fetchWhOptions();
  nextTick(() => { initGrids(); initialize(); });
  api.post('/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '300' }).then(r => paycndtData.value = r.data);
  api.post('/ha00/HA00_00P_STR', { gubun: 'SD', cmpycd: authStore.cmpycd }).then(r => userData.value = r.data);
  api.get('/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym; });
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus { border-color: #005a9f !important; box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important; outline: none; }
</style>
