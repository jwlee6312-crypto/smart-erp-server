<!--
	=============================================================
	프로그램명: 카드미지급금기초잔액 (HABA250U)
	작성일자	: 2025.03.14
	작성자    : AI Assistant
	설명        : 카드별 미지급금 기초 잔액 관리 (HSOD100U 균등 배분 레이아웃 및 SaleCustHelp 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<SaleCustHelp v-model:visible="popVisible.cust" @confirm="onCustConfirm" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- [1] 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-credit-card-2-front me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초자료 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">카드미지급금기초잔액 (HABA250U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">신규(N)</button>
				<button class="btn-erp btn-search" @click="search">조회(F)</button>
				<button class="btn-erp btn-save" @click="save">저장(S)</button>
			</div>
		</div>

		<!-- [2] 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- 상단 조회 필터 (균등 배분 4컬럼 그리드) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 120px;" /><col style="width: 38%;" />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light text-center small">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="width: 220px;">
										<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
										<input v-model="searchForm.acctnm" type="text" class="form-control" placeholder="계정명" @keydown.enter="handleOpenHelp('S_ACCT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S_ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light text-center border-start small">발생일</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.symd" type="date" class="form-control form-control-sm" style="width: 140px;" />
										<span class="small text-muted px-1">~</span>
										<input v-model="searchForm.eymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 상세 정보 입력 영역 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-primary"></i>
					<span class="fw-bold small text-dark">카드 미지급 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규등록' : '정보수정' }}]</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 220px;" />
							<col style="width: 110px;" /><col style="width: 280px;" />
							<col style="width: 110px;" /><col style="width: 220px;" />
							<col style="width: 110px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light small text-center">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="max-width: 260px;">
										<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="masterForm.acctnm" type="text" class="form-control" placeholder="계정명" @keydown.enter="handleOpenHelp('M_ACCT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M_ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">카드번호</th>
								<td>
									<div class="input-group input-group-sm" style="max-width: 260px;">
										<input v-model="masterForm.mgtno" type="text" class="form-control" placeholder="카드번호 선택" @keydown.enter="handleOpenHelp('M_MGT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M_MGT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">결제처</th>
								<td colspan="3">
									<div class="input-group input-group-sm" style="max-width: 280px;">
										<input v-model="masterForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="masterForm.custnm" type="text" class="form-control" placeholder="결제처 선택" @keydown.enter="handleOpenHelp('M_CUST')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M_CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th class="required bg-light small text-center border-top">미결잔액</th>
								<td class="border-top">
									<div class="d-flex align-items-center gap-1 px-1">
										<input v-model="masterForm.cramt" type="number" class="form-control form-control-sm text-end fw-bold text-danger" style="max-width: 160px;" step="0" />
										<span class="small text-muted">원</span>
									</div>
								</td>
								<th class="bg-light small text-center border-start border-top">발생일</th>
								<td class="border-top">
									<input v-model="masterForm.pubymd" type="date" class="form-control form-control-sm" style="max-width: 140px;" />
								</td>
								<th class="bg-light small text-center border-start border-top">지불예정일</th>
								<td class="border-top">
									<input v-model="masterForm.reqymd" type="date" class="form-control form-control-sm" style="max-width: 140px;" />
								</td>
								<th class="bg-light small text-center border-start border-top">비고</th>
								<td class="border-top">
									<div class="d-flex align-items-center gap-3">
										<input v-model="masterForm.remark" type="text" class="form-control form-control-sm" />
										<div class="form-check form-check-inline mb-0 flex-shrink-0">
											<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useYnCheck" true-value="Y" false-value="N" />
											<label class="form-check-label small fw-bold" for="useYnCheck">사용</label>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 그리드 영역 -->
			<div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>카드 미지급 잔액 목록</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>

		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import Modal from '@/components/Modal.vue'
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: commonOpenHelp } = useCommonHelp()

const popVisible = reactive({ cust: false })

// 날짜 초기화 (현재 월 1일 ~ 오늘)
const today = new Date().toISOString().split('T')[0]
const firstDay = new Date(new Date().getFullYear(), new Date().getMonth(), 2).toISOString().split('T')[0]

// 검색 조건 데이터
const searchForm = reactive({
	acctcd: '',
	acctnm: '',
	symd: firstDay,
	eymd: today
})

// 마스터 데이터
const masterForm = reactive({
	actkind: 'I1',
	slipymd: '',
	slipno: '',
	srowno: '',
	acctcd: '',
	acctnm: '',
	mgtno: '',
	mgtnm: '',
	custcd: '',
	custnm: '',
	cramt: 0,
	pubymd: firstDay,
	reqymd: '',
	remark: '',
	useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) return vAlert('검색할 계정과목을 선택해 주십시오.')
	if (!searchForm.symd || !searchForm.eymd) return vAlert('발생일을 선택해 주십시오.')

	try {
		const res = await api.post('/haba/HABA_250U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			symd: searchForm.symd.replace(/-/g, ''),
			eymd: searchForm.eymd.replace(/-/g, ''),
			slipymd: '',
			slipno: '',
			srowno: '',
			acctcd: searchForm.acctcd,
			custcd: '',
			mgtno: '',
			pubymd: '',
			cramt: 0,
			reqymd: '',
			remark: '',
			useyn: 'Y',
			userid: ''
		})

		mainGrid?.setData(res.data || [])
		if (res.data?.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.acctcd) return vAlert('계정과목을 선택해 주십시오.')
	if (!masterForm.mgtno) return vAlert('카드번호를 선택해 주십시오.')
	if (!masterForm.custcd) return vAlert('결제처를 선택해 주십시오.')
	if (!masterForm.reqymd) return vAlert('지불예정일을 선택해 주십시오.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			symd: searchForm.symd.replace(/-/g, ''),
			eymd: searchForm.eymd.replace(/-/g, ''),
			slipymd: masterForm.slipymd.replace(/-/g, ''),
			slipno: masterForm.slipno,
			srowno: masterForm.srowno,
			acctcd: masterForm.acctcd,
			custcd: masterForm.custcd,
			mgtno: masterForm.mgtno,
			pubymd: masterForm.pubymd.replace(/-/g, ''),
			cramt: masterForm.cramt || 0,
			reqymd: masterForm.reqymd.replace(/-/g, ''),
			remark: masterForm.remark,
			useyn: masterForm.useyn,
			userid: authStore.userid
		}

		const res = await api.post('/haba/HABA_250U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('정상적으로 저장되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	const prevacctcd = searchForm.acctcd
	const prevacctnm = searchForm.acctnm
	const prevsymd = searchForm.symd
	const preveymd = searchForm.eymd

	resetForm(masterForm)

	masterForm.actkind = 'I1'
	masterForm.acctcd = prevacctcd
	masterForm.acctnm = prevacctnm
	masterForm.pubymd = prevsymd
	masterForm.useyn = 'Y'
	masterForm.cramt = 0
}

// 🚀 [표준] HABA210U 규격 적용 및 SaleCustHelp 사용
function handleOpenHelp(type: string) {
	let searchVal = ''
	if (type === 'S_ACCT') searchVal = searchForm.acctnm
	if (type === 'M_ACCT') searchVal = masterForm.acctnm
	if (type === 'M_MGT') searchVal = masterForm.mgtno
	if (type === 'M_CUST') searchVal = masterForm.custnm

	if (type === 'M_CUST') {
		popVisible.cust = true
		return
	}

	if (type.includes('ACCT')) {
		commonOpenHelp('ACCT', (d: any) => {
			if (type === 'S_ACCT') {
				searchForm.acctcd = d.acctcd
				searchForm.acctnm = d.acctnm
				masterForm.acctcd = d.acctcd
				masterForm.acctnm = d.acctnm
			} else {
				masterForm.acctcd = d.acctcd
				masterForm.acctnm = d.acctnm
			}
		}, { search: searchVal })
	} else if (type === 'M_MGT') {
		if (!masterForm.acctcd) return vAlert("계정과목을 우선 선택해 주십시오.")
		commonOpenHelp('MGT', (d: any) => {
			masterForm.mgtno = d.mgtno
			masterForm.mgtnm = d.mgtnm
		}, { search: searchVal, mgtgbn: '040', acctcd: masterForm.acctcd })
	}
}

function onCustConfirm(d: any) {
	masterForm.custcd = d.custcd
	masterForm.custnm = d.custnm
}

const formatDate = (val: string) => {
	if (!val || val.length !== 8) return ''
	return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "카드번호", field: "col7", width: 150, hozAlign: "center", cssClass: "fw-bold text-primary" },
				{ title: "결제처", field: "col5", width: 100, hozAlign: "center" },
				{ title: "결제처명", field: "col6", minWidth: 150, cssClass: "fw-bold" },
				{ title: "발생일", field: "col0", width: 110, hozAlign: "center", formatter: (cell) => formatDate(cell.getValue()) },
				{ title: "금액", field: "col8", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "지불예정일", field: "col9", width: 110, hozAlign: "center", formatter: (cell) => formatDate(cell.getValue()) },
				{ title: "적요", field: "col10", minWidth: 200, hozAlign: "left" },
				{ title: "사용", field: "col11", width: 80, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.slipymd = formatDate(d.col0)
				masterForm.pubymd = formatDate(d.col0)
				masterForm.slipno = d.col1
				masterForm.srowno = d.col2
				masterForm.acctcd = d.col3
				masterForm.acctnm = d.col4
				masterForm.custcd = d.col5
				masterForm.custnm = d.col6
				masterForm.mgtno = d.col7
				masterForm.cramt = Number(d.col8)
				masterForm.reqymd = formatDate(d.col9)
				masterForm.remark = d.col10
				masterForm.useyn = d.col11
			}
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
</style>
