import axios from 'axios';
import { store } from '../store/store';
import { showLoading, hideLoading } from '../store/loadingSlice';
import { base_server_url } from '@/constants';

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: base_server_url,
  timeout: 10000,
});

// 요청 인터셉터 - 로딩 시작
apiClient.interceptors.request.use(
  (config) => {
    // 로딩 마스크가 필요한 요청인지 확인 (기본값: true)
    if (config.showLoading !== false) {
      store.dispatch(showLoading());
    }
    return config;
  },
  (error) => {
    store.dispatch(hideLoading());
    return Promise.reject(error);
  }
);

// 응답 인터셉터 - 로딩 종료
apiClient.interceptors.response.use(
  (response) => {
    if (response.config.showLoading !== false) {
      store.dispatch(hideLoading());
    }
    return response;
  },
  (error) => {
    if (error.config?.showLoading !== false) {
      store.dispatch(hideLoading());
    }
    return Promise.reject(error);
  }
);

// 공통 API 함수들
export const apiRequest = {
  // GET 요청
  get: async (url, params = {}, options = {}) => {
    try {
      const response = await apiClient.get(url, {
        params,
        ...options,
      });
      return response;
    } catch (error) {
      console.error('GET 요청 실패:', error);
      throw error;
    }
  },

  // POST 요청
  post: async (url, data = {}, options = {}) => {
    try {
      const response = await apiClient.post(url, data, options);
      return response;
    } catch (error) {
      console.error('POST 요청 실패:', error);
      throw error;
    }
  },

  // PUT 요청
  put: async (url, data = {}, options = {}) => {
    try {
      const response = await apiClient.put(url, data, options);
      return response;
    } catch (error) {
      console.error('PUT 요청 실패:', error);
      throw error;
    }
  },

  // PATCH 요청
  patch: async (url, data = {}, options = {}) => {
    try {
      const response = await apiClient.patch(url, data, options);
      return response;
    } catch (error) {
      console.error('PATCH 요청 실패:', error);
      throw error;
    }
  },

  // DELETE 요청
  delete: async (url, options = {}) => {
    try {
      const response = await apiClient.delete(url, options);
      return response;
    } catch (error) {
      console.error('DELETE 요청 실패:', error);
      throw error;
    }
  },
};

// 로딩 마스크 없이 요청하는 함수들 (필요한 경우)
export const apiRequestSilent = {
  get: (url, params = {}, options = {}) =>
    apiRequest.get(url, params, { ...options, showLoading: false }),

  post: (url, data = {}, options = {}) =>
    apiRequest.post(url, data, { ...options, showLoading: false }),

  put: (url, data = {}, options = {}) =>
    apiRequest.put(url, data, { ...options, showLoading: false }),

  patch: (url, data = {}, options = {}) =>
    apiRequest.patch(url, data, { ...options, showLoading: false }),

  delete: (url, options = {}) =>
    apiRequest.delete(url, { ...options, showLoading: false }),
};

export default apiClient;
