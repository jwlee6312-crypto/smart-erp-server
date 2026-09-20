<!--
	=============================================================
	컴포넌트명	: 부서 선택 도움창 (개별 정의형 - 키보드 표준 적용)
	작성일자	: 2025.03.15
	설명		: 탭 순환, 방향키 이동, 더블클릭/엔터 선택 즉시 종료 지원
	=============================================================
-->

<template>
  <div v-if="visible" class="erp-modal-overlay" @keydown.esc="$emit('close')">
    <div class="erp-modal-container shadow-2xl border-0 animate-scale-in" @keydown="handleTabCycle">
      <!-- 🚀 헤더 -->
      <div class="erp-modal-header d-flex justify-content-between align-items-center px-2 py-1">
        <div class="d-flex align-items-center">
          <div class="header-icon-box-xs me-2"><i class="bi bi-diagram-3 text-white" style="font-size: 12px;"></i></div>
          <span class="fw-bold text-white" style="font-size: 12px;">부서 선택 도움창</span>
        </div>
        <button type="button" class="btn-close btn-close-white" style="font-size: 9px;" tabindex="-1" @click="$emit('close')"></button>
      </div>

      <!-- 📊 그리드 영역 (탭 포커스 수용) -->
      <div class="p-1 bg-light">
        <div
          ref="gridWrapper"
          class="grid-card-solid border rounded-1 overflow-hidden bg-white shadow-inner"
          tabindex="1"
          @keydown="handleGridKey"
        >
          <div ref="deptGridRef" style="height: 380px; width: 100%;"></div>
        </div>
      </div>

      <!-- 💡 푸터 -->
      <div class="px-3 py-1 bg-white border-top text-end" style="min-height: 32px;">
        <button ref="lastEl" class="btn btn-xs btn-dark px-4 py-0 fw-bold rounded-1" tabindex="2" @click="$emit('close')" style="font-size: 11px; height: 24px;">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'

const props = defineProps<{ visible: boolean, cmpycd: string }>()
const emit = defineEmits(['close', 'confirm'])

const deptGridRef = ref<HTMLElement | null>(null)
const gridWrapper = ref<HTMLElement | null>(null)
const lastEl = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)

function handleTabCycle(e: KeyboardEvent) {
    if (e.key === 'Tab') {
        if (e.shiftKey && document.activeElement === gridWrapper.value) { e.preventDefault(); lastEl.value?.focus(); }
        else if (!e.shiftKey && document.activeElement === lastEl.value) { e.preventDefault(); gridWrapper.value?.focus(); }
    }
}

function handleGridKey(e: KeyboardEvent) {
    if (!grid.value) return;
    const rows = grid.value.getRows("active");
    const selected = grid.value.getSelectedRows()[0];
    const idx = selected ? rows.indexOf(selected) : -1;

    if (e.key === 'ArrowDown') { e.preventDefault(); rows[idx+1]?.select(); rows[idx+1]?.getElement().scrollIntoView({block:'nearest'}); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); rows[idx-1]?.select(); rows[idx-1]?.getElement().scrollIntoView({block:'nearest'}); }
    else if (e.key === 'Enter' && selected) { emit('confirm', selected.getData()); emit('close'); }
}

const fetchDepts = async () => {
  try {
    const res = await api.post('/ha00/ha00_00p_str', { gubun: 'd0', cmpycd: String(props.cmpycd) })
    if (grid.value) {
      await grid.value.setData(res.data || [])
      if (res.data?.length > 0) grid.value.selectRow(grid.value.getRows()[0]);
    }
  } catch (e) { console.error('부서 조회 실패') }
}

watch(() => props.visible, async (val) => {
  if (!val) { if (grid.value) { grid.value.destroy(); grid.value = null; } return }
  await nextTick()
  setTimeout(() => {
    if (!deptGridRef.value) return
    grid.value = new Tabulator(deptGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columns: [
        { title: '부서코드', field: 'deptcd', width: 100, cssClass: 'fw-bold text-primary' },
        { title: '부서명칭', field: 'deptnm', minWidth: 200, widthGrow: 1 }
      ]
    })
    grid.value.on('rowDblClick', (e, row) => { emit('confirm', row.getData()); emit('close'); })
    fetchDepts()
    gridWrapper.value?.focus();
  }, 100)
})
</script>

<style scoped>
.erp-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(1px); z-index: 10000; display: flex; align-items: center; justify-content: center; }
.erp-modal-container { width: 500px; background: #fff; border-radius: 4px; overflow: hidden; }
.erp-modal-header { background: #005a9f; color: #fff; }
.grid-card-solid :deep(.tabulator-row.tabulator-selected) { background-color: #005a9f !important; color: #fff !important; }
.grid-card-solid:focus { border-color: #005a9f !important; box-shadow: 0 0 0 2px rgba(0,90,159,0.2); }
</style>
