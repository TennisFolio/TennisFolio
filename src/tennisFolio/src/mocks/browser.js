import { setupWorker } from 'msw/browser';
import { handlers } from './handlers';
import { store } from '../store/store';
import { setMSWActive, setMSWInitialized } from '../store/mswSlice';

// MSW 워커 설정
export const worker = setupWorker(...handlers);

// 개발 모드에서만 MSW 시작
export async function enableMocking() {
  if (import.meta.env.MODE !== 'development') {
    return;
  }

  // 브라우저 환경이 아니면 실행하지 않음
  if (typeof window === 'undefined') {
    return;
  }

  try {
    // 기본적으로 MSW 활성화
    await worker.start();

    // Store에 MSW 활성화 상태 저장
    store.dispatch(setMSWActive(true));
    store.dispatch(setMSWInitialized(true));

    // 간단한 전역 MSW 컨트롤러
    window.MSW = {
      worker: worker, // worker 참조 추가
      start: async () => {
        await worker.start();
        store.dispatch(setMSWActive(true));
      },
      stop: async () => {
        await worker.stop();
        store.dispatch(setMSWActive(false));
      },
    };
  } catch (error) {
    console.error(error);
  }
}
