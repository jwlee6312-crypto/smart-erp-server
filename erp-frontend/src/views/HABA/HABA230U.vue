<!--
	=============================================================
	프로그램명: 관리번호 잔액등록 (HABA230U)
	작성일자	: 2025.03.14
	작성자    : AI Assistant
	설명        : 계정과목 및 관리번호별 기초 잔액(이월/본월) 등록 및 관리 (HSOD100U & HSIO300U 규격 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- [1] 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-tag-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초자료 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">관리번호 잔액등록 (HABA230U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">신규(N)</button>
				<button class="btn-erp btn-search" @click="search">조회(F)</button>
				<button class="btn-erp btn-save" @click="save">저장(S)</button>
			</div>
		</div>

		<!-- [2] 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- 상단 조회 필터 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 100px;" /><col style="width: 200px;" />
							<col style="width: 100px;" /><col style="width: 120px;" />
							<col style="width: 100px;" /><col style="width: 220px;" />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light text-center small">회계년월</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yyyy" class="form-select form-select-sm" style="width: 80px;" @change="syncSearchToMaster">
											<option v-for="year in years" :key="year" :value="year">{{ year }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 65px;" @change="syncSearchToMaster">
											<option v-for="month in months" :key="month" :value="month">{{ month }}월</option>
										</select>
									</div>
								</td>
								<th class="required bg-light text-center border-start small">구분</th>
								<td>
									<select v-model="searchForm.mmgbn" class="form-select form-select-sm" style="width: 90px;" @change="syncSearchToMaster">
										<option value="00">이월</option>
										<option value="88">본월</option>
									</select>
								</td>
								<th class="required bg-light text-center border-start small">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="width: 200px;">
										<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.acctnm" type="text" class="form-control" placeholder="계정명" @keydown.enter="handleOpenHelp('S_ACCT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S_ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="bg-light text-center border-start small">관리번호</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<div class="input-group input-group-sm" style="width: 180px;">
											<input v-model="searchForm.mgtno1" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
											<input v-model="searchForm.mgtnm1" type="text" class="form-control" placeholder="시작" @keydown.enter="handleOpenHelp('S_MGT1')" />
											<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S_MGT1')"><i class="bi bi-search"></i></button>
										</div>
										<span class="small text-muted">~</span>
										<div class="input-group input-group-sm" style="width: 180px;">
											<input v-model="searchForm.mgtno2" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
											<input v-model="searchForm.mgtnm2" type="text" class="form-control" placeholder="종료" @keydown.enter="handleOpenHelp('S_MGT2')" />
											<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S_MGT2')"><i class="bi bi-search"></i></button>
										</div>
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
					<span class="fw-bold small text-dark">관리번호 잔액 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규' : '수정' }}]</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 220px;" />
							<col style="width: 110px;" /><col style="width: 120px;" />
							<col style="width: 110px;" /><col style="width: 280px;" />
							<col style="width: 110px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light small text-center">회계년월</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="masterForm.yyyy" class="form-select form-select-sm" style="width: 80px;">
											<option v-for="year in years" :key="year" :value="year">{{ year }}년</option>
										</select>
										<select v-model="masterForm.mm" class="form-select form-select-sm" style="width: 65px;">
											<option v-for="month in months" :key="month" :value="month">{{ month }}월</option>
										</select>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">구분</th>
								<td>
									<select v-model="masterForm.mmgbn" class="form-select form-select-sm" style="max-width: 100px;">
										<option value="88">본월</option>
										<option value="00">이월</option>
									</select>
								</td>
								<th class="required bg-light small text-center border-start">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="max-width: 260px;">
										<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="masterForm.acctnm" type="text" class="form-control" placeholder="계정명" @keydown.enter="handleOpenHelp('M_ACCT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M_ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">관리번호</th>
								<td>
									<div class="input-group input-group-sm" style="max-width: 280px;">
										<input v-model="masterForm.mgtno" type="text" class="form-control text-center bg-light" style="max-width: 110px;" readonly />
										<input v-model="masterForm.mgtnm" type="text" class="form-control" placeholder="관리번호/계좌/카드 선택" @keydown.enter="handleOpenHelp('M_MGT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M_MGT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th class="bg-light small text-center border-top">차변금액</th>
								<td class="border-top">
									<div class="d-flex align-items-center gap-1 px-1">
										<input v-model="masterForm.dbamt" type="number" class="form-control form-control-sm text-end fw-bold text-primary" style="max-width: 160px;" step="0" />
										<span class="small text-muted">원</span>
									</div>
								</td>
								<th class="bg-light small text-center border-start border-top">대변금액</th>
								<td class="border-top" colspan="5">
									<div class="d-flex align-items-center gap-1 px-1">
										<input v-model="masterForm.cramt" type="number" class="form-control form-control-sm text-end fw-bold text-danger" style="max-width: 160px;" step="0" />
										<span class="small text-muted">원</span>
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
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>관리번호별 잔액 목록</span>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: commonOpenHelp } = useCommonHelp()

// 년도/월 옵션
const currentYear = new Date().getFullYear()
const years = computed(() => Array.from({ length: 10 }, (_, i) => String(currentYear - i)))
const months = computed(() => Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')))

// 검색 조건
const searchForm = reactive({
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '00',
	acctcd: '',
	acctnm: '',
	mgtgbn: '',
	mgtno1: '',
	mgtnm1: '',
	mgtno2: '',
	mgtnm2: ''
})

// 마스터 데이터
const masterForm = reactive({
	actkind: 'I1',
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '88',
	acctcd: '',
	acctnm: '',
	mgtno: '',
	mgtnm: '',
	mgtgbn: '',
	dbamt: 0,
	cramt: 0
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) return vAlert('검색할 계정과목을 선택해 주십시오.')

	try {
		const res = await api.post('/haba/HABA_230U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			acctym: searchForm.yyyy + searchForm.mm,
		    mmgbn: searchForm.mmgbn,
			acctcd: searchForm.acctcd,
			mgtno: '',
			mgtno1: searchForm.mgtno1,
			mgtno2: searchForm.mgtno2
		})

		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.acctcd) return vAlert('계정과목을 선택해 주십시오.')
	if (!masterForm.mgtno) return vAlert('관리번호를 선택해 주십시오.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			acctym: masterForm.yyyy + masterForm.mm,
		    mmgbn: masterForm.mmgbn,
			acctcd: masterForm.acctcd,
			mgtno: masterForm.mgtno,
			mgtno1: searchForm.mgtno1,
			mgtno2: searchForm.mgtno2,
			dbamt: masterForm.dbamt || 0,
			cramt: masterForm.cramt || 0,
			mgtnm: masterForm.mgtnm
		}

		const res = await api.post('/haba/HABA_230U_STR', payload)

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
	const prevyyyy = masterForm.yyyy
	const prevmm = masterForm.mm
	const prevmmgbn = masterForm.mmgbn
	const prevacctcd = masterForm.acctcd
	const prevacctnm = masterForm.acctnm
	const prevmgtgbn = masterForm.mgtgbn

	resetForm(masterForm)

	masterForm.actkind = 'I1'
	masterForm.yyyy = prevyyyy
	masterForm.mm = prevmm
	masterForm.mmgbn = prevmmgbn
	masterForm.acctcd = prevacctcd
	masterForm.acctnm = prevacctnm
	masterForm.mgtgbn = prevmgtgbn
	masterForm.dbamt = 0
	masterForm.cramt = 0
}

const syncSearchToMaster = () => {
	masterForm.yyyy = searchForm.yyyy
	masterForm.mm = searchForm.mm
	masterForm.mmgbn = searchForm.mmgbn
}

// 🚀 [표준] HSIO300U 및 HABA210U 규격 적용
function handleOpenHelp(type: string) {
	let searchVal = ''
	if (type === 'S_ACCT') searchVal = searchForm.acctnm
	if (type === 'S_MGT1') searchVal = searchForm.mgtno1
	if (type === 'S_MGT2') searchVal = searchForm.mgtno2
	if (type === 'M_ACCT') searchVal = masterForm.acctnm
	if (type === 'M_MGT') searchVal = masterForm.mgtnm

	if (type.includes('MGT') && (!searchForm.acctcd && !masterForm.acctcd)) {
		return vAlert("계정과목을 우선 선택해 주십시오.")
	}

	if (type.includes('ACCT')) {
		commonOpenHelp('ACCT', (d: any) => {
			const mgtGbnCode = d.typemgt || d.mgtgbn || d.col2 || ''
			if (type === 'S_ACCT') {
				searchForm.acctcd = d.acctcd
				searchForm.acctnm = d.acctnm
				searchForm.mgtgbn = mgtGbnCode
			} else {
				masterForm.acctcd = d.acctcd
				masterForm.acctnm = d.acctnm
				masterForm.mgtgbn = mgtGbnCode
			}
		}, { search: searchVal })
	} else {
		const mgtGbn = type.startsWith('S') ? (searchForm.mgtgbn || '010') : (masterForm.mgtgbn || '010')
		const acctCd = type.startsWith('S') ? searchForm.acctcd : masterForm.acctcd
		commonOpenHelp('MGT', (d: any) => {
			const mNo = d.mgtno || d.col0 || ''
			const mNm = d.mgtnm || d.col1 || ''
			if (type === 'S_MGT1') {
				searchForm.mgtno1 = mNo
				searchForm.mgtnm1 = mNm
			} else if (type === 'S_MGT2') {
				searchForm.mgtno2 = mNo
				searchForm.mgtnm2 = mNm
			} else {
				masterForm.mgtno = mNo
				masterForm.mgtnm = mNm
			}
		}, { search: searchVal, mgtgbn: mgtGbn, acctcd: acctCd })
	}
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", headerHozAlign: "center" },
			columns: [
				{ title: "관리번호", field: "col4", width: 140, hozAlign: "center", cssClass: "fw-bold text-primary" },
				{ title: "관리번호명", field: "col5", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "차변", field: "col7", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "대변", field: "col8", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "잔액", field: "col9", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold text-primary" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.yyyy = d.col0
				masterForm.mm = d.col1
				masterForm.mmgbn = d.col6
				masterForm.acctcd = d.col2
				masterForm.acctnm = d.col3
				masterForm.mgtno = d.col4
				masterForm.mgtnm = d.col5
				masterForm.dbamt = Number(d.col7)
				masterForm.cramt = Number(d.col8)
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
