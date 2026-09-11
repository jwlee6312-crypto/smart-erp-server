<!--
	=============================================================
	프로그램명	  : SmartCore 통합 오퍼레이션 콕핏 (최종 완결본)
    프로그램 ID	: DEFAULT_PAGE
	작성일자	    : 25.03.14
	작성자	      : AI Assistant
	설명         : 실시간 지표 + AI 브리핑 + 5대 핵심 팩트 통합 대시보드
	=============================================================
-->

<template>
    <div class="cockpit-wrapper bg-soft-gray h-100 overflow-auto p-3 text-start">
        <!-- [1] 상단 통합 관제 헤더 -->
        <header class="d-flex justify-content-between align-items-center mb-2 border-bottom pb-2">
            <div class="d-flex align-items-center gap-3">
                <h6 class="fw-bold text-dark mb-0">
                    <i class="bi bi-grid-1x2-fill text-primary me-2"></i>OPERATION COCKPIT
                </h6>
                <div class="vr opacity-25" style="height: 15px;"></div>
                <p class="text-muted extra-small mb-0">실시간 전사 운영 지표 통합 모니터링</p>
            </div>
            <!-- 🕒 실시간 디지털 시계 -->
            <div class="digital-clock d-flex align-items-center gap-2 px-3 py-1 bg-white rounded-3 shadow-xs border">
                <i class="bi bi-clock-fill text-primary small"></i>
                <span class="fw-bold text-primary font-monospace" style="font-size: 1rem;">{{ currentTime }}</span>
            </div>
        </header>

        <!-- [2] 리얼타임 피드 & AI 브리핑 -->
        <div class="row g-2 mb-3">
            <div class="col-md-7">
                <div class="live-feed-ribbon p-2 bg-dark text-white rounded-3 border shadow-sm overflow-hidden h-100">
                    <div class="d-flex align-items-center gap-3">
                        <span class="badge bg-danger rounded-pill px-2 py-1 fw-bold extra-small pulse-border">LIVE FEED</span>
                        <div class="flex-grow-1 extra-small fw-bold overflow-hidden" style="position: relative; height: 20px;">
                            <div class="scrolling-text">
                                <span v-for="item in liveFeeds" :key="item.id" class="me-4">
                                    [{{ item.media_type }}] {{ item.content }} ({{ item.time_str }})
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-5">
                <div class="ai-briefing-card p-2 bg-primary bg-opacity-10 rounded-3 border border-primary border-opacity-25 h-100 d-flex align-items-center gap-2">
                    <i class="bi bi-robot text-primary fs-5 ms-2 animate-bounce"></i>
                    <div class="extra-small fw-bold text-dark">
                        <span class="text-primary me-1">[AI 분석]</span> {{ aiBriefing || '데이터 분석 중...' }}
                    </div>
                </div>
            </div>
        </div>

        <!-- [3] 5대 핵심 운영 지표 요약 (팩트 기반) -->
        <div class="row g-2 mb-3">
            <div class="col" v-for="stat in quickStats" :key="stat.label">
                <div class="card border-0 shadow-sm rounded-3 bg-white border-bottom border-3 py-1" :class="'border-' + stat.color">
                    <div class="card-body py-2 px-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-muted extra-small fw-bolder mb-1">{{ stat.label }}</div>
                                <div class="fw-bold text-dark h5 mb-0">{{ stat.value }}<span class="extra-small fw-normal ms-1 opacity-50">건</span></div>
                            </div>
                            <div class="stat-icon-area opacity-25">
                                <i :class="'bi bi-' + stat.icon" class="fs-4"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- [4] 메인 관제 위젯 그리드 -->
        <div class="row g-3">
            <!-- (1) 전사 실시간 업무 통합 관제 -->
            <div class="col-md-7 d-flex flex-column gap-3">
                <div class="card border-0 shadow-sm rounded-4 overflow-hidden bg-white h-100">
                    <div class="card-header bg-success py-2 px-3 d-flex justify-content-between align-items-center border-0">
                        <span class="fw-bold text-white small"><i class="bi bi-shield-check me-2"></i>전사 실시간 업무 통합 관제</span>
                        <button class="btn btn-xs btn-outline-light px-2 py-0 fw-bold">모니터링</button>
                    </div>
                    <div class="card-body p-3">
                        <!-- 이행율 시각화 -->
                        <div class="d-flex flex-column gap-3 mb-4">
                            <div class="goal-item">
                                <div class="d-flex justify-content-between extra-small fw-bold mb-1">
                                    <span>당월 매출 목표 달성도</span>
                                    <span class="text-primary">{{ metrics.outboundRate }}%</span>
                                </div>
                                <div class="progress" style="height: 8px;">
                                    <div class="progress-bar progress-bar-striped progress-bar-animated bg-primary"
                                         role="progressbar" :style="{ width: metrics.outboundRate + '%' }"></div>
                                </div>
                            </div>
                        </div>

                        <!-- 공지사항 리스트 -->
                        <div class="mt-3 card border-0 shadow-none">
                            <div class="card-header bg-light py-1 px-3 d-flex justify-content-between align-items-center border-bottom">
                                <span class="fw-bold text-dark extra-small"><i class="bi bi-megaphone me-2"></i>전사 공지사항</span>
                            </div>
                            <div class="card-body p-0">
                                <div class="list-group list-group-flush">
                                    <div v-for="notice in noticeList" :key="notice.id" class="list-group-item d-flex justify-content-between align-items-center py-2 border-light">
                                        <span class="extra-small text-dark text-truncate px-3" style="max-width: 400px;">{{ notice.title }}</span>
                                        <span class="extra-small text-muted pe-3">{{ notice.date }}</span>
                                    </div>
                                    <div v-if="noticeList.length === 0" class="p-4 text-center text-muted extra-small">등록된 공지가 없습니다.</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- (2) 주요 일정 및 만기 알람 -->
            <div class="col-md-5 d-flex flex-column gap-3">
                <div class="card border-0 shadow-sm rounded-4 border-top border-4 border-primary bg-white">
                    <div class="card-header bg-white py-2 px-3 d-flex justify-content-between align-items-center border-bottom">
                        <span class="fw-bold text-dark small"><i class="bi bi-clock-history me-2 text-primary"></i>주요 일정 및 만기 알람</span>
                    </div>
                    <div class="card-body p-0">
                        <table class="table table-sm extra-small mb-0">
                            <thead class="bg-light text-muted">
                                <tr><th class="ps-3">구분</th><th>만기/일정</th><th class="text-end pe-3">상태</th></tr>
                            </thead>
                            <tbody>
                                <tr v-for="item in expireList" :key="item.id" class="border-light" :class="{'bg-danger-subtle': item.urgency === 'urgent'}">
                                    <td class="ps-3 py-2"><span class="badge bg-soft-blue text-primary extra-small">{{ item.category }}</span></td>
                                    <td class="fw-bold text-dark text-truncate" style="max-width: 180px;">
                                        {{ item.title }}<br/><small class="text-muted fw-normal">{{ item.date }}</small>
                                    </td>
                                    <td class="text-end pe-3">
                                        <span v-if="item.urgency === 'urgent'" class="badge bg-danger rounded-pill">임박</span>
                                        <span v-else class="text-muted">{{ item.amt }}</span>
                                    </td>
                                </tr>
                                <tr v-if="expireList.length === 0">
                                    <td colspan="3" class="text-center py-4 text-muted">등록된 만기 일정이 없습니다.</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { api } from '@/utils/axios'

const currentTime = ref('')
const aiBriefing = ref('')
const liveFeeds = ref<any[]>([])

const updateClock = () => {
    currentTime.value = new Date().toLocaleTimeString('ko-KR', { hour12: false });
}

const metrics = reactive({ outboundRate: 0 })

const quickStats = ref([
    { label: '미입고 발주', value: 0, icon: 'box-seam', color: 'warning', key: 'purchase_pending' },
    { label: '미출고 주문', value: 0, icon: 'cart-check', color: 'success', key: 'sales_pending' },
    { label: '미완료 생산', value: 0, icon: 'tools', color: 'primary', key: 'prod_pending' },
    { label: '미응대 콜백', value: 0, icon: 'headset', color: 'danger', key: 'crm_pending' },
    { label: '진행중 SFA', value: 0, icon: 'lightbulb-fill', color: 'info', key: 'sfa_pending' }
])

const noticeList = ref<any[]>([]);
const expireList = ref<any[]>([]);

const fetchDashboardData = async () => {
    try {
        const { data } = await api.get('/briefing-dashboard/data');
        aiBriefing.value = data.aiBriefing;
        liveFeeds.value = data.feed || [];
        noticeList.value = data.notices || [];
        expireList.value = data.expiry || [];

        if (data.stats) {
            quickStats.value.forEach(stat => {
                stat.value = data.stats[stat.key] || 0;
            });
            metrics.outboundRate = data.stats.sales_goal_rate || 0;
        }
    } catch (e) {
        console.error('브리핑 자료 로드 실패', e);
    }
}

onMounted(() => {
    updateClock();
    setInterval(updateClock, 1000);
    fetchDashboardData();
    setInterval(fetchDashboardData, 300000);
})
</script>

<style scoped>
.cockpit-wrapper { height: calc(100vh - 65px); font-family: 'Inter', sans-serif; }
.bg-soft-gray { background-color: #f1f5f9; }
.extra-small { font-size: 0.72rem; }
.mini-text { font-size: 0.65rem; color: #94a3b8; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.scrolling-text {
    position: absolute; width: 100%; height: 100%; white-space: nowrap;
    animation: scroll-left 35s linear infinite;
}
@keyframes scroll-left { 0% { transform: translateX(100%); } 100% { transform: translateX(-200%); } }
.pulse-border { animation: pulse-soft 2s infinite; }
@keyframes pulse-soft {
    0% { box-shadow: 0 0 0 0 rgba(220, 53, 69, 0.4); }
    70% { box-shadow: 0 0 0 6px rgba(220, 53, 69, 0); }
    100% { box-shadow: 0 0 0 0 rgba(220, 53, 69, 0); }
}
.bg-danger-subtle { background-color: #fff5f5; }
</style>
