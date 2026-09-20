import { reactive, ref, nextTick } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import type { ModalProps } from '@/types/modal'

export function useCommonHelp() {
  const authStore = useAuthStore()
  const modalVisible = ref(false)
  const lastActiveElement = ref<HTMLElement | null>(null)

  const modalProps = reactive<ModalProps>({
    title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table'
  })

  // 🚀 [복구] 팝업 종료 후 호출 지점의 '다음' 탭 인덱스로 강제 이동
  const restoreFocus = () => {
    nextTick(() => {
      if (lastActiveElement.value) {
        const el = lastActiveElement.value;
        // 1. 일단 원래 자리로 복귀 (브라우저 위치 유실 방지)
        el.focus();

        // 2. 100ms 지연 후 다음 탭 인덱스로 이동
        setTimeout(() => {
          const currentTabIndex = el.tabIndex;
          if (currentTabIndex > 0) {
            const nextEl = document.querySelector(`[tabindex="${currentTabIndex + 1}"]`) as HTMLElement;
            if (nextEl) {
              nextEl.focus();
              if (nextEl instanceof HTMLInputElement) nextEl.select();
            }
          }
        }, 100);
      }
    });
  }

  const openHelp = (type: string, callback: (data: any) => void, extraData: any = {}) => {
    lastActiveElement.value = document.activeElement as HTMLElement;
    const commonPath = '/ha00/HA00_00P_STR'

    if (type === 'DEPT') {
      Object.assign(modalProps, {
        title: '부서 선택', path: commonPath, defaultField: 'deptnm', large: false,
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '부서코드', field: 'deptcd', width: 120, hozAlign: 'center' },
          { title: '부서명', field: 'deptnm', minWidth: 200, widthGrow: 1 }
        ],
        onConfirm: callback
      })
    } else if (type === 'CUST') {
      const gubun = (extraData.gubun || 'C4').toUpperCase()
      Object.assign(modalProps, {
        title: '거래처 선택', path: commonPath, defaultField: 'custnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '거래처코드', field: 'custcd', width: 120, hozAlign: 'center' },
          { title: '거래처명', field: 'custnm', minWidth: 250, widthGrow: 1 }
        ],
        onConfirm: callback
      })
    } else if (type === 'ITEM') {
      const gubun = extraData.gubun || 'I1'
      const path = gubun.startsWith('I') ? '/hp00/HP00_000S_STR' : commonPath
      Object.assign(modalProps, {
        title: extraData.title || '품목 선택', path: path, defaultField: 'itemnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || extraData.codegbn || '', code: extraData.search || '', remark: extraData.iogbn || '' },
        columns: [
          { title: '품목코드', field: 'itemcd', width: 120, hozAlign: 'center' },
          { title: '품목명', field: 'itemnm', minWidth: 250, widthGrow: 1 },
          { title: '규격', field: 'itsize', width: 150 },
          { title: '단위', field: 'unit', width: 80, hozAlign: 'center' }
        ],
        onConfirm: callback
      })
    } else if (type === 'ADDR') {
      Object.assign(modalProps, {
        title: '배송처 선택', path: '/hs00/HS00_000S_STR', defaultField: 'trannm', large: true,
        data: { gubun: 'T0', cmpycd: authStore.cmpycd, code: extraData.code || '' },
        columns: [
          { title: '코드', field: 'trancd', width: 80, hozAlign: 'center' },
          { title: '명칭', field: 'custnm', width: 150 },
          { title: '주소', field: 'address', minWidth: 250, widthGrow: 1 }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp, restoreFocus, lastActiveElement }
}
