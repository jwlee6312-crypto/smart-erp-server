<!--
	=============================================================
	컴포넌트명	: 공통 조회 팝업 (Airtight Focus Trap 적용판)
	작성일자	: 2025.03.15
	설명		: 팝업 이탈 0% 보장, 조회버튼 탭 스킵으로 그리드 진입 속도 최적화
	=============================================================
-->

<template>
	<div class="modal-root-wrapper">
		<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

		<div
			v-if="visible"
			class="modal fade show d-block"
			tabindex="-1"
			@keydown.esc="close"
			style="background: rgba(0, 0, 0, 0.5); z-index: 1060; backdrop-filter: blur(2px);"
		>
			<div :class="props.modalProps.large ? 'modal-dialog modal-lg modal-dialog-centered' : 'modal-dialog modal-dialog-centered'">
				<!-- 🚀 팝업 전체 포커스 제어 컨테이너 -->
				<div class="modal-content border-0 shadow-lg" style="border-radius: 8px; overflow: hidden;" @keydown="handleAirtightTrap">
					<!-- 헤더 -->
					<div class="modal-header py-2 bg-white border-bottom shadow-sm">
						<h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
							<i class="bi bi-search text-primary me-2"></i> {{ props.modalProps.title || '데이터 조회' }}
						</h5>
						<button type="button" class="btn-close shadow-none" style="font-size: 10px;" tabindex="-1" @click="close"></button>
					</div>

					<!-- 본문 -->
					<div class="modal-body p-3 bg-light">
						<!-- 검색 바 -->
						<div class="d-flex align-items-center gap-2 mb-3">
							<div class="input-group input-group-sm shadow-sm" style="width: 300px;">
								<span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-funnel"></i></span>
								<input
									ref="filterInputRef"
									v-model="filterValue"
									type="text"
									class="form-control border-start-0 ps-0 shadow-none"
									tabindex="0"
									:placeholder="`${props.modalProps.title?.replace(' 선택', '')} 검색...`"
									@keyup.enter="search"
								/>
							</div>
							<!-- 💡 조회 버튼은 탭 스킵 처리 (엔터로 대체) -->
							<button class="btn btn-primary btn-sm px-3 fw-bold shadow-sm" tabindex="-1" @click="search" :disabled="loading">조회</button>
						</div>

						<!-- 📊 데이터 그리드 (이동 목표) -->
						<div
							ref="gridWrapperRef"
							class="popup-grid-container border rounded bg-white shadow-sm overflow-hidden"
							style="height: 450px; position: relative; outline: none;"
							tabindex="0"
							@keydown="handleGridKey"
						>
							<div v-if="loading" class="loading-overlay">
								<div class="spinner-border text-primary" role="status"></div>
							</div>
							<div ref="popupRef" style="height: 100%; width: 100%;"></div>
						</div>
						<div class="mt-2 text-muted x-small">※ 탭 이동: 검색어 ➔ 그리드(방향키) ➔ 취소 ➔ 검색어</div>
					</div>

					<!-- 푸터 -->
					<div class="modal-footer py-1 bg-white border-top text-end">
						<button ref="cancelBtnRef" type="button" class="btn btn-outline-secondary btn-sm px-4 fw-bold" tabindex="0" @click="close">취소</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import type { ModalProps } from '@/types/modal'

const props = defineProps<{ visible: boolean, modalProps: ModalProps }>()
const emit = defineEmits(['update:visible', 'close'])
const { showAlert, showError, alertMessage } = useAlerts()

const filterInputRef = ref<HTMLInputElement | null>(null)
const gridWrapperRef = ref<HTMLElement | null>(null)
const cancelBtnRef = ref<HTMLElement | null>(null)
const popupRef = ref<HTMLElement | null>(null)
const popupGrid = ref<Tabulator | null>(null)
const filterValue = ref<string>('')
const loading = ref(false)

function close() {
	emit('update:visible', false);
	emit('close');
}

// ⌨️ 1. 완벽한 포커스 트랩 (외부 이탈 100% 차단)
function handleAirtightTrap(e: KeyboardEvent) {
	if (e.key !== 'Tab') return;

	// 포커스 가능한 요소 자동 추출
	const focusable = [filterInputRef.value, gridWrapperRef.value, cancelBtnRef.value].filter(el => !!el);
	const first = focusable[0] as HTMLElement;
	const last = focusable[focusable.length - 1] as HTMLElement;

	if (e.shiftKey) { // Shift + Tab
		if (document.activeElement === first) { e.preventDefault(); last.focus(); }
	} else { // Tab
		if (document.activeElement === last) { e.preventDefault(); first.focus(); }
	}
}

// ⌨️ 2. 그리드 전용 조작 (방향키, 엔터)
function handleGridKey(e: KeyboardEvent) {
	if (!popupGrid.value) return;
	const allRows = popupGrid.value.getRows("active");
	if (allRows.length === 0) return;

	const selected = popupGrid.value.getSelectedRows()[0];
	const idx = selected ? allRows.indexOf(selected) : -1;

	if (e.key === 'ArrowDown') {
		e.preventDefault();
		const next = allRows[idx + 1];
		if (next) { popupGrid.value.deselectRow(); next.select(); next.getElement().scrollIntoView({block:'nearest'}); }
	} else if (e.key === 'ArrowUp') {
		e.preventDefault();
		const prev = allRows[idx - 1];
		if (prev) { popupGrid.value.deselectRow(); prev.select(); prev.getElement().scrollIntoView({block:'nearest'}); }
	} else if (e.key === 'Enter') {
		e.preventDefault();
		if (selected) {
			props.modalProps.onConfirm?.(selected.getData());
			close();
		}
	}
}

async function search() {
	if (!popupGrid.value) return
	loading.value = true;
	try {
		// 🚀 [보정] 기존에 정의된 code(예: 배송처 조회의 부모코드)가 있는 경우 검색어로 덮어쓰지 않음
		const body = {
			...props.modalProps.data,
			codenm: filterValue.value || '',
		}

		// 💡 호출 시 넘겨준 code가 없을 때만 검색어를 code로 사용 (전사 확산 표준)
		if (!props.modalProps.data.code) {
			body.code = filterValue.value || '';
		}

		const res = await api.post(props.modalProps.path, body)
		await popupGrid.value.setData(res.data || [])
		if (res.data?.length > 0) popupGrid.value.selectRow(popupGrid.value.getRows()[0]);
	} catch (e) {
		console.error('조회 실패', e)
	} finally {
		loading.value = false;
	}
}

watch(() => props.visible, async (isVisible) => {
	if (!isVisible) { if (popupGrid.value) { popupGrid.value.destroy(); popupGrid.value = null; } filterValue.value = ''; return }

	await nextTick()
	setTimeout(() => filterInputRef.value?.focus(), 150)

	setTimeout(() => {
		if (!popupRef.value) return
		popupGrid.value = new Tabulator(popupRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1, headerSort: false,
			columns: props.modalProps.columns || [],
			columnDefaults: { headerHozAlign: 'center', vertAlign: 'middle' },
			placeholder: "데이터 없음"
		})
		search()
		popupGrid.value.on('rowClick', (_e, row) => { props.modalProps.onConfirm?.(row.getData()); close(); })
	}, 100)
})
</script>

<style scoped>
.loading-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.7); z-index: 100; display: flex; align-items: center; justify-content: center; }
.popup-grid-container :deep(.tabulator-row.tabulator-selected) { background-color: #005a9f !important; color: #ffffff !important; font-weight: bold !important; }
.popup-grid-container :deep(.tabulator-row.tabulator-selected .tabulator-cell) { color: #ffffff !important; }
.popup-grid-container:focus { border: 2px solid #005a9f !important; box-shadow: 0 0 0 4px rgba(0, 90, 159, 0.1) !important; }
.x-small { font-size: 11px; }
</style>
