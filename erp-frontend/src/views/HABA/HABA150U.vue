<!--
	=============================================================
	프로그램명: 받을어음기초자료등록 (HABA150U)
	작성일자	: 2025.03.14
	작성자    : AI Assistant
	설명        : 받을어음 기초 정보 등록 및 관리 (HSOD100U 균등 배분 레이아웃 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- [1] 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-medical me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초자료 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">받을어음기초자료등록 (HABA150U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">신규(N)</button>
				<button class="btn-erp btn-search" @click="search">조회(F)</button>
				<button class="btn-erp btn-save" @click="save">저장(S)</button>
			</div>
		</div>

		<!-- [2] 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- 상단 조회 필터 (균등 배분 레이아웃) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 380px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light text-center small">어음번호</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.billno" type="text" class="form-control form-control-sm" style="width: 170px;" maxlength="14" placeholder="시작 어음번호" @keydown.enter="search" />
										<span class="small text-muted px-1">~</span>
										<input v-model="searchForm.billno_TO" type="text" class="form-control form-control-sm" style="width: 170px;" maxlength="14" placeholder="종료 어음번호" @keydown.enter="search" />
									</div>
								</td>
								<td class="text-muted small italic pe-3 text-end">
									<i class="bi bi-info-circle me-1"></i> 조회하시려는 어음번호 범위를 입력하세요.
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
					<span class="fw-bold small text-dark">어음 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규' : '수정' }}]</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 220px;" />
							<col style="width: 110px;" /><col style="width: 250px;" />
							<col style="width: 110px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light small text-center">어음번호</th>
								<td>
									<input v-model="masterForm.billno" type="text" class="form-control form-control-sm fw-bold text-primary" maxlength="14" :readonly="masterForm.actkind === 'U1'" />
								</td>
								<th class="bg-light small text-center border-start">발행은행</th>
								<td>
									<input v-model="masterForm.issubank" type="text" class="form-control form-control-sm" maxlength="50" />
								</td>
								<th class="bg-light small text-center border-start">발행인</th>
								<td>
									<input v-model="masterForm.issuman" type="text" class="form-control form-control-sm" style="max-width: 180px;" maxlength="20" />
								</td>
							</tr>
							<tr>
								<th class="required bg-light small text-center border-top">발행일자</th>
								<td class="border-top">
									<input v-model="masterForm.stdymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
								</td>
								<th class="required bg-light small text-center border-start border-top">만기일자</th>
								<td class="border-top">
									<input v-model="masterForm.endymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
								</td>
								<th class="required bg-light small text-center border-start border-top">금액</th>
								<td class="border-top">
									<div class="d-flex align-items-center gap-1 px-1">
										<input v-model="masterForm.billamt" type="number" class="form-control form-control-sm text-end fw-bold text-primary" style="max-width: 160px;" step="0" />
										<span class="small text-muted">원</span>
									</div>
								</td>
							</tr>
							<tr>
								<th class="bg-light small text-center border-top">어음종류</th>
								<td class="border-top">
									<select v-model="masterForm.BILLKIND" class="form-select form-select-sm" style="max-width: 160px;">
										<option v-for="opt in billKindOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
									</select>
								</td>
								<th class="bg-light small text-center border-start border-top">어음유형</th>
								<td class="border-top">
									<select v-model="masterForm.billtype" class="form-select form-select-sm" style="max-width: 160px;">
										<option v-for="opt in billTypeOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
									</select>
								</td>
								<th class="bg-light small text-center border-start border-top">부도여부</th>
								<td class="border-top">
									<select v-model="masterForm.BUDOYN" class="form-select form-select-sm" style="max-width: 100px;">
										<option value="N">미부도</option>
										<option value="Y">부도</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="required bg-light small text-center border-top">받은거래처</th>
								<td class="border-top" colspan="3">
									<div class="input-group input-group-sm" style="max-width: 350px;">
										<input v-model="masterForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="masterForm.custnm" type="text" class="form-control" placeholder="거래처 선택" @keydown.enter="openHelp" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="bg-light small text-center border-start border-top">사용여부</th>
								<td class="border-top">
									<div class="form-check form-switch pt-1 ms-2">
										<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useYnCheck" true-value="Y" false-value="N" />
										<label class="form-check-label small fw-bold" for="useYnCheck">사용</label>
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
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>받을어음 기초 목록</span>
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 🔍 검색 데이터
const searchForm = reactive({
	billno: '',
	billno_TO: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: 'I1',
	billno: '',
	issubank: '',
	issuman: '',
	stdymd: '',
	endymd: '',
	billamt: 0,
	BILLKIND: '',
	billtype: '',
	BUDOYN: 'N',
	custcd: '',
	custnm: '',
	useyn: 'Y'
})

const billKindOptions = ref<{ value: string; text: string }[]>([])
const billTypeOptions = ref<{ value: string; text: string }[]>([])

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		// 어음종류 (150)
		const resKind = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '150', code: '' })
		billKindOptions.value = resKind.data?.map((i: any) => ({
			value: i.code || i.codecd || i.col0,
			text: i.cdnm || i.codenm || i.col1
		})) || []
		if (billKindOptions.value.length > 0) masterForm.BILLKIND = billKindOptions.value[0].value

		// 어음유형 (160)
		const resType = await api.post('/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '160', code: '' })
		billTypeOptions.value = resType.data?.map((i: any) => ({
			value: i.code || i.codecd || i.col0,
			text: i.cdnm || i.codenm || i.col1
		})) || []
		if (billTypeOptions.value.length > 0) masterForm.billtype = billTypeOptions.value[0].value
	} catch (e) { console.error('기초 데이터 로드 실패', e) }
}

const search = async () => {
	try {
		const res = await api.post('/haba/HABA_150U_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd,
			billno: searchForm.billno,
			billno_TO: searchForm.billno_TO,
			billamt: 0
		})
		const list = res.data || []
		mainGrid?.setData(list)
		if (list.length > 0) vAlert('조회되었습니다.')
		else vAlert('조회된 데이터가 없습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.billno) return vAlert('어음번호를 입력하세요.')
	if (!masterForm.issubank) return vAlert('발행은행을 입력하세요.')
	if (!masterForm.issuman) return vAlert('발행인을 입력하세요.')
	if (!masterForm.stdymd) return vAlert('발행일자를 선택하세요.')
	if (!masterForm.endymd) return vAlert('만기일자를 선택하세요.')
	if (Number(masterForm.billamt || 0) <= 0) return vAlert('어음금액을 입력하세요.')
	if (!masterForm.custcd) return vAlert('거래처를 선택하세요.')

    if (!confirm('저장하시겠습니까?')) return
	try {
		const payload = {
			...masterForm,
			cmpycd: authStore.cmpycd,
			userid: authStore.userid,
			stdymd: masterForm.stdymd.replace(/-/g, ''),
			endymd: masterForm.endymd.replace(/-/g, '')
		}

		const res = await api.post('/haba/HABA_150U_STR', payload)
		const resdata = res.data?.[0] || {};
		if (resdata.ret_yn === 'Y' || resdata.result === 'N') {
			vAlertError(resdata.ret_msg || resdata.msg || '저장 실패')
		} else {
			vAlert('정상적으로 저장되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'I1'
	masterForm.BUDOYN = 'N'
	masterForm.useyn = 'Y'
    masterForm.cmpycd = authStore.cmpycd
	if (billKindOptions.value.length > 0) masterForm.BILLKIND = billKindOptions.value[0].value
	if (billTypeOptions.value.length > 0) masterForm.billtype = billTypeOptions.value[0].value
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp() {
	Object.assign(modalProps, {
		title: '거래처 선택',
		path: '/ha00/HA00_03P_STR',
		data: { custgbn: '010', cmpycd: authStore.cmpycd, search: masterForm.custnm },
		columns: [
			{ title: '코드', field: 'col0', width: 100, hozAlign: 'center' },
			{ title: '거래처명', field: 'col1', width: 250 }
		],
		onConfirm: (d: any) => {
			masterForm.custcd = d.col0
			masterForm.custnm = d.col1
		}
	})
	modalVisible.value = true
}

const formatDate = (val: string) => {
	if (!val || val.length !== 8) return ''
	return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
}

onMounted(async () => {
	await fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", headerHozAlign: "center" },
			columns: [
				{ title: "어음번호", field: "col0", width: 130, hozAlign: "center", cssClass: "fw-bold text-primary" },
				{ title: "발행은행", field: "col1", width: 150, hozAlign: "left" },
				{ title: "발행인", field: "col2", width: 120, hozAlign: "center" },
				{ title: "발행일", field: "col3", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
				{ title: "만기일", field: "col4", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
				{ title: "금액", field: "col5", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "어음종류", field: "col7", width: 100, hozAlign: "center" },
				{ title: "어음유형", field: "col9", width: 100, hozAlign: "center" },
				{ title: "거래처명", field: "col12", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
                { title: "사용", field: "col13", width: 70, hozAlign: "center", formatter: "tickCross" }
			]
		})
        mainGrid.on("rowClick", (e, row) => {
            const d = row.getData()
            Object.assign(masterForm, {
                actkind: 'U1',
                billno: d.col0,
                issubank: d.col1,
                issuman: d.col2,
                stdymd: formatDate(d.col3),
                endymd: formatDate(d.col4),
                billamt: Number(d.col5),
                BILLKIND: d.col6,
                billtype: d.col8,
                BUDOYN: d.col10,
                custcd: d.col11,
                custnm: d.col12,
                useyn: d.col13
            })
        })
	}
    search()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
.italic { font-style: italic; }
</style>
