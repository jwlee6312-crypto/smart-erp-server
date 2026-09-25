<!--
	=============================================================
	프로그램명: 계정별기초자료등록 (HABA210U)
	작성일자	: 2025.03.14
	작성자    : AI Assistant
	설명        : 계정별 기초 자료(이월/본월) 등록 및 관리 (HSOD100U 균등 배분 레이아웃 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- [1] 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초자료 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">계정별기초자료등록 (HABA210U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">신규(N)</button>
				<button class="btn-erp btn-search" @click="search">조회(F)</button>
				<button class="btn-erp btn-save" @click="save">저장(S)</button>
			</div>
		</div>

		<!-- [2] 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- 상단 조회 필터 (균등 배분 6컬럼 그리드) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 100px;" /><col style="width: 25%;" />
							<col style="width: 100px;" /><col style="width: 20%;" />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light text-center small">회계년월</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yyyy" class="form-select form-select-sm" style="width: 90px;" @change="syncSearchToMaster">
											<option v-for="year in years" :key="year" :value="year">{{ year }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 70px;" @change="syncSearchToMaster">
											<option v-for="month in months" :key="month" :value="month">{{ month }}월</option>
										</select>
									</div>
								</td>
								<th class="required bg-light text-center border-start small">구분</th>
								<td>
									<select v-model="searchForm.mmgbn" class="form-select form-select-sm" style="width: 100px;" @change="syncSearchToMaster">
										<option value="00">이월</option>
										<option value="88">본월</option>
									</select>
								</td>
								<th class="bg-light text-center border-start small">계정과목</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<div class="input-group input-group-sm" style="max-width: 200px;">
											<input v-model="searchForm.acctcd1" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
											<input v-model="searchForm.acctcd1_T" type="text" class="form-control" placeholder="시작계정" @keydown.enter="handleOpenHelp('S1')" />
											<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S1')"><i class="bi bi-search"></i></button>
										</div>
										<span class="small text-muted px-1">~</span>
										<div class="input-group input-group-sm" style="max-width: 200px;">
											<input v-model="searchForm.acctcd2" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
											<input v-model="searchForm.acctcd2_T" type="text" class="form-control" placeholder="종료계정" @keydown.enter="handleOpenHelp('S2')" />
											<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S2')"><i class="bi bi-search"></i></button>
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
					<span class="fw-bold small text-dark">계정 기초 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규등록' : '정보수정' }}]</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 220px;" />
							<col style="width: 110px;" /><col style="width: 320px;" />
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
								<th class="required bg-light small text-center border-start">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="max-width: 280px;">
										<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="masterForm.acctcd_t" type="text" class="form-control" placeholder="계정명 선택" @keydown.enter="handleOpenHelp('M1')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M1')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">구분</th>
								<td>
									<select v-model="masterForm.mmgbn" class="form-select form-select-sm" style="max-width: 100px;">
										<option value="88">본월</option>
										<option value="00">이월</option>
									</select>
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
								<td class="border-top" colspan="3">
									<div class="d-flex align-items-center gap-1 px-1">
										<input v-model="masterForm.cramt" type="number" class="form-control form-control-sm text-end fw-bold text-danger" style="max-width: 160px;" step="0" />
										<span class="small text-muted">원</span>
										<span class="ms-3 text-muted" style="font-size: 11px;">※ 이월 자료는 전기이월 잔액을, 본월 자료는 당월 기초 발생 금액을 의미합니다.</span>
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
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>계정별 기초 목록</span>
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
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// 년도/월 옵션
const currentYear = new Date().getFullYear()
const years = computed(() => Array.from({ length: 10 }, (_, i) => String(currentYear - i)))
const months = computed(() => Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')))

// 검색 조건
const searchForm = reactive({
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '00',
	acctcd1: '',
	acctcd1_T: '',
	acctcd2: '',
	acctcd2_T: ''
})

// 마스터 데이터
const masterForm = reactive({
	actkind: 'I1',
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
	acctcd: '',
	acctcd_t: '',
    mmgbn: '88',
	dbamt: 0,
	cramt: 0
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/haba/HABA_210U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			acctcd1: searchForm.acctcd1,
			acctcd2: searchForm.acctcd2,
			acctym: searchForm.yyyy + searchForm.mm,
		    mmgbn: searchForm.mmgbn,
            acctcd: '',
            dbamt: 0,
            cramt: 0
		})

		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.yyyy || !masterForm.mm) return vAlert('회계년월을 선택해 주십시오.')
	if (!masterForm.acctcd) return vAlert('계정과목을 선택해 주십시오.')
	if (!masterForm.mmgbn) return vAlert('구분을 선택해 주십시오.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			acctcd1: searchForm.acctcd1,
			acctcd2: searchForm.acctcd2,
			acctym: masterForm.yyyy + masterForm.mm,
		    mmgbn: masterForm.mmgbn,
			acctcd: masterForm.acctcd,
			dbamt: masterForm.dbamt || 0,
			cramt: masterForm.cramt || 0,
			acctnm: masterForm.acctcd_t
		}

		const res = await api.post('/haba/HABA_210U_STR', payload)

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
	const prevYyyy = masterForm.yyyy
	const prevMm = masterForm.mm
	const prevMmgbn = masterForm.mmgbn

	resetForm(masterForm)

	masterForm.actkind = 'I1'
	masterForm.yyyy = prevYyyy
	masterForm.mm = prevMm
	masterForm.mmgbn = prevMmgbn
	masterForm.dbamt = 0
	masterForm.cramt = 0
}

const syncSearchToMaster = () => {
	masterForm.yyyy = searchForm.yyyy
	masterForm.mm = searchForm.mm
	masterForm.mmgbn = searchForm.mmgbn
}

// 🚀 [표준 계정과목 팝업 연동]
function handleOpenHelp(type: string) {
	let searchVal = ''
	if (type === 'S1') searchVal = searchForm.acctcd1_T
	else if (type === 'S2') searchVal = searchForm.acctcd2_T
	else if (type === 'M1') searchVal = masterForm.acctcd_t

	openHelp('ACCT', (d: any) => {
		if (type === 'S1') {
			searchForm.acctcd1 = d.acctcd
			searchForm.acctcd1_T = d.acctnm
		} else if (type === 'S2') {
			searchForm.acctcd2 = d.acctcd
			searchForm.acctcd2_T = d.acctnm
		} else if (type === 'M1') {
			masterForm.acctcd = d.acctcd
			masterForm.acctcd_t = d.acctnm
		}
	}, { search: searchVal })
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", headerHozAlign: "center" },
			columns: [
				{ title: "계정과목", field: "col2", width: 120, hozAlign: "center", cssClass: "fw-bold text-primary" },
				{ title: "계정과목명", field: "col3", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "차변", field: "col5", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "대변", field: "col6", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "잔액", field: "col7", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
			],
		})
        mainGrid.on("rowClick", (e, row) => {
            const d = row.getData()
            masterForm.actkind = 'U1'
            masterForm.yyyy = d.col0
            masterForm.mm = d.col1
            masterForm.acctcd = d.col2
            masterForm.acctcd_t = d.col3
            masterForm.mmgbn = d.col4
            masterForm.dbamt = Number(d.col5)
            masterForm.cramt = Number(d.col6)
        })
		search()
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
