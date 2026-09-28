<!--	=============================================================
	프로그램명 : 감가상각계산
	작성일자	: 2025.02.24
	작성자    : AI Assistant
	설명        : 해당 월의 변동자산에 대한 감가상각 계산 처리 (표준 UI 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- [헤더] 1. 상단 액션 바 -->
		<div class="erp-header border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="d-flex justify-content-between align-items-center w-100" style="max-width: 60%; min-width: 600px;">
				<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
					<i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
					고정자산 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
					기말처리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
					<span class="text-primary fw-bolder">감가상각계산 (HAFA090U)</span>
				</div>
				<div class="btn-group-erp d-flex gap-1">
					<button class="btn-erp btn-save" @click="save">계산실행</button>
				</div>
			</div>
		</div>

		<!-- [콘텐츠] 2. 메인 콘텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-4 bg-light">
			<div class="d-flex flex-column gap-3" style="max-width: 60%; min-width: 600px;">
				<div class="card border shadow-sm bg-white overflow-hidden w-100">
				<div class="card-header bg-white py-2 px-3 border-bottom text-center">
					<span class="fw-bold small text-dark"><i class="bi bi-gear-fill me-2 text-primary"></i>감가상각 계산 조건 설정</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light text-center">대상연도</th>
								<td class="px-3">
									<select v-model="form.yy" class="form-select form-select-sm" style="width: 150px;">
										<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년도</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="required bg-light text-center">대 상 월</th>
								<td class="px-3">
									<select v-model="form.mm" class="form-select form-select-sm" style="width: 120px;">
										<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="card-body p-4 bg-white">
					<div class="alert alert-warning border-start border-4 border-warning shadow-sm small py-3 px-4 m-0 bg-white">
						<div class="d-flex mb-2">
							<i class="bi bi-exclamation-triangle-fill me-2 fs-5 text-warning"></i>
							<span class="fw-bold text-dark">작업 시 주의사항</span>
						</div>
						<div class="ps-4">
							<div class="d-flex mb-2">
								<span>해당 월의 변동자산에 대해 감가상각을 일괄 계산합니다.</span>
							</div>
							<div class="d-flex">
								<span>12월 작업 시에는 차기년도 감가상각 기초 데이터를 함께 생성합니다.</span>
							</div>
						</div>
					</div>
				</div>
				<div class="card-footer p-3 bg-light text-center border-top">
					<p class="text-muted mb-0" style="font-size: 11px;">※ 계산 작업은 취소할 수 없으므로 년월을 반드시 확인하십시오.</p>
				</div>
			</div>
		</div>
	</div>
</div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')

const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const form = reactive({
	yy: String(currentYear),
    mm: currentMonth
})

async function save() {
	if (!confirm(`${form.yy}년 ${form.mm}월의 감가상각 계산작업을 진행하시겠습니까?`)) return

	try {
		const res = await api.post('/hafa/HAFA_090U_STR', {
			cmpycd: authStore.cmpycd,
			baseym: form.yy + form.mm
		})

		if (res.data && res.data[0]) {
			const result = res.data[0]
			const code = result.col0 || result.res
			const msg = result.col1 || result.msg

			if (code === 'Y' || code === 'OK') {
				vAlert(msg || '감가상각 계산이 완료되었습니다.')
			} else {
				vAlertError(msg || '계산 처리 중 오류가 발생했습니다.')
			}
		} else {
			vAlert('정상적으로 작업이 완료되었습니다.')
		}
	} catch (e) {
		vAlertError('서버 통신 중 오류가 발생했습니다.')
	}
}
</script>

<style scoped>
</style>
