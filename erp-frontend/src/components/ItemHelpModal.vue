<!--
	=============================================================
	컴포넌트명	: 품목 선택 도움창 (개별 정의형 - 키보드 표준 적용)
	작성일자	: 2025.03.15
	설명		: 탭 순환(검색-그리드-닫기), 상하키 이동, 즉시 선택 종료 지원
	=============================================================
-->

<template>
  <div v-if="visible" class="erp-modal-overlay" @keydown.esc="$emit('close')">
    <div class="erp-modal-container shadow-2xl border-0 animate-pop-in" @keydown="handleTabCycle">
      <!-- 🚀 헤더 -->
      <div class="erp-modal-header d-flex justify-content-between align-items-center px-3 py-1">
        <div class="d-flex align-items-center">
          <div class="header-icon-box-sm me-2"><i class="bi bi-box-seam text-white" style="font-size: 13px;"></i></div>
          <span class="fw-bold text-white" style="font-size: 12.5px;">품목 선택 도움창</span>
        </div>
        <button type="button" class="btn-close btn-close-white" style="font-size: 9px;" tabindex="-1" @click="$emit('close')"></button>
      </div>

      <!-- 🔍 검색 영역 -->
      <div class="p-2 bg-white border-bottom shadow-sm">
        <div class="input-group input-group-sm" style="width: 400px;">
          <input
            ref="firstEl"
            v-model="searchWord"
            type="text"
            class="form-control"
            tabindex="1"
            placeholder="품목명 또는 코드를 입력..."
            @keyup.enter="fetchItems"
          />
          <button class="btn btn-primary fw-bold px-3" tabindex="-1" @click="fetchItems">조회</button>
        </div>
      </div>

      <!-- 📊 그리드 영역 -->
      <div class="p-1 bg-light">
        <div
          ref="gridWrapper"
          class="grid-card-border overflow-hidden bg-white shadow-inner"
          tabindex="2"
          @keydown="handleGridKey"
        >
          <div ref="itemGridRef" style="height: 420px; width: 100%;"></div>
        </div>
      </div>

      <!-- 💡 푸터 -->
      <div class="px-3 py-1 bg-white border-top text-end d-flex justify-content-between align-items-center">
        <span class="text-muted smaller-text">※ 탭/방향키로 조작하고 엔터로 선택하세요.</span>
        <button ref="lastEl" class="btn btn-xs btn-dark px-4 py-1 fw-bold rounded-1" tabindex="3" @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'

const props = defineProps<{ visible: boolean, cmpycd: string, astKind?: string }>()
const emit = defineEmits(['close', 'confirm'])

const searchWord = ref('')
const firstEl = ref<HTMLElement | null>(null)
const gridWrapper = ref<HTMLElement | null>(null)
const lastEl = ref<HTMLElement | null>(null)
const itemGridRef = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)

function handleTabCycle(e: KeyboardEvent) {
    if (e.key === 'Tab') {
        const focusable = [firstEl.value, gridWrapper.value, lastEl.value];
        if (e.shiftKey && document.activeElement === focusable[0]) { e.preventDefault(); focusable[2]?.focus(); }
        else if (!e.shiftKey && document.activeElement === focusable[2]) { e.preventDefault(); focusable[0]?.focus(); }
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

const fetchItems = async () => {
  try {
    const res = await api.post('/hs00/HS00_000S_STR', { gubun: 'I1', cmpycd: String(props.cmpycd), gbncd: String(props.astKind || '2'), codenm: String(searchWord.value).trim() })
    if (grid.value) {
      await grid.value.setData(res.data || [])
      if (res.data?.length > 0) grid.value.selectRow(grid.value.getRows()[0]);
    }
  } catch (e) { console.error('조회 실패') }
}

watch(() => props.visible, async (val) => {
  if (!val) { if (grid.value) { grid.value.destroy(); grid.value = null; } searchWord.value = ''; return }
  await nextTick()
  setTimeout(() => {
    if (!itemGridRef.value) return
    grid.value = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columns: [
        { title: '코드', field: 'itemcd', width: 100, cssClass: 'fw-bold text-primary' },
        { title: '품목명칭', field: 'itemnm', minWidth: 200, widthGrow: 1 },
        { title: '규격', field: 'itsize', width: 130 },
        { title: '재고', field: 'qty', width: 80, hozAlign: 'right', formatter: (c:any) => Number(c.getValue()).toLocaleString() }
      ]
    })
    grid.value.on('rowDblClick', (e, row) => { emit('confirm', row.getData()); emit('close'); })
    fetchItems();
    firstEl.value?.focus();
  }, 100)
})
</script>

<style scoped>
.erp-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(1px); z-index: 10000; display: flex; align-items: center; justify-content: center; }
.erp-modal-container { width: 800px; background: #fff; border-radius: 4px; overflow: hidden; }
.erp-modal-header { background: #1e293b; color: #fff; }
.grid-card-border :deep(.tabulator-row.tabulator-selected) { background-color: #005a9f !important; color: #fff !important; }
.grid-card-border:focus { border-color: #005a9f !important; box-shadow: 0 0 0 2px rgba(0,90,159,0.1); }
</style>
