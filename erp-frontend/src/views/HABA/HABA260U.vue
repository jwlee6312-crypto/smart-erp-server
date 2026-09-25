<!--
	=============================================================
	프로그램명: 선급비용 기초자료등록 (HABA260U)
	작성일자	: 2025.03.14
	작성자    : AI Assistant
	설명        : 선급비용 기초 자료 관리(계정명, 발생일, 기간, 미상각비용 등 - HSOD100U 균등 배분 레이아웃 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- [1] 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초자료 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">선급비용 기초자료등록 (HABA260U)</span>
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
					<span class="fw-bold small text-dark">선급비용 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규등록' : '정보수정' }}]</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 260px;" />
							<col style="width: 110px;" /><col style="width: 220px;" />
							<col style="width: 110px;" /><col style="width: 180px;" />
							<col style="width: 110px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light small text-center">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="max-width: 240px;">
										<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="masterForm.acctnm" type="text" class="form-control" placeholder="계정명" @keydown.enter="handleOpenHelp('M_ACCT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M_ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">미상각비용</th>
								<td>
									<div class="d-flex align-items-center gap-1 px-1">
										<input v-model="masterForm.UNpayamt" type="number" class="form-control form-control-sm text-end fw-bold text-primary" style="max-width: 160px;" step="0" />
										<span class="small text-muted">원</span>
									</div>
								</td>
								<th class="required bg-light small text-center border-start">발생일</th>
								<td colspan="3">
									<input v-model="masterForm.pubymd" type="date" class="form-control form-control-sm" style="max-width: 140px;" />
								</td>
							</tr>
							<tr>
								<th class="required bg-light small text-center border-top">기 간</th>
								<td class="border-top">
									<div class="d-flex align-items-center gap-1">
										<input v-model="masterForm.fromdt" type="date" class="form-control form-control-sm" style="width: 130px;" />
										<span class="small text-muted">~</span>
										<input v-model="masterForm.todt" type="date" class="form-control form-control-sm" style="width: 130px;" />
									</div>
								</td>
								<th class="bg-light small text-center border-start border-top">비 고</th>
								<td class="border-top" colspan="5">
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
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>선급비용 기초 목록</span>
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: commonOpenHelp } = useCommonHelp()

// 날짜 초기화
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
	UNpayamt: 0,
	pubymd: today,
	fromdt: firstDay,
	todt: today,
	remark: '',
	useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/haba/HABA_260U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			symd: searchForm.symd.replace(/-/g, ''),
			eymd: searchForm.eymd.replace(/-/g, ''),
			slipymd: '',
			slipno: '',
			srowno: '',
			acctcd: searchForm.acctcd || '',
			pubymd: ''
		})

		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.acctcd) return vAlert('계정과목을 선택해 주십시오.')
	if (!masterForm.pubymd) return vAlert('발생일을 선택해 주십시오.')
	if (!masterForm.fromdt) return vAlert('기간(FROM)을 선택해 주십시오.')
	if (!masterForm.todt) return vAlert('기간(TO)을 선택해 주십시오.')

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
			pubymd: masterForm.pubymd.replace(/-/g, ''),
			UNpayamt: masterForm.UNpayamt || 0,
			fromdt: masterForm.fromdt.replace(/-/g, ''),
			todt: masterForm.todt.replace(/-/g, ''),
			remark: masterForm.remark,
			useyn: masterForm.useyn,
			userid: authStore.userid
		}

		const res = await api.post('/haba/HABA_260U_STR', payload)

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
	masterForm.pubymd = today
	masterForm.fromdt = firstDay
	masterForm.todt = today
	masterForm.useyn = 'Y'
}

// 🚀 [표준] HABA210U 규격 적용
function handleOpenHelp(type: string) {
	let searchVal = type === 'S_ACCT' ? searchForm.acctnm : masterForm.acctnm

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
				{ title: "발생일", field: "acctymd", width: 110, hozAlign: "center", formatter: (cell) => formatDate(cell.getValue()) },
				{ title: "비고", field: "remark", minWidth: 250, hozAlign: "left" },
				{ title: "미상각비용", field: "amt", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "기간", field: "stdymd", width: 220, hozAlign: "center", formatter: (cell) => {
					const data = cell.getRow().getData()
					return `${formatDate(data.stdymd)} ~ ${formatDate(data.endymd)}`
				}},
                { title: "사용", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">사용</b>' : '';
                  }
                }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.slipymd = formatDate(d.slipymd)
				masterForm.slipno = d.slipno
				masterForm.srowno = d.srowno
				masterForm.acctcd = d.acctcd
				masterForm.acctnm = d.acctnm || d.remark
				masterForm.pubymd = formatDate(d.acctymd)
				masterForm.UNpayamt = Number(d.amt)
				masterForm.fromdt = formatDate(d.stdymd)
				masterForm.todt = formatDate(d.endymd)
				masterForm.remark = d.remark
				masterForm.useyn = d.useyn
			}
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
