import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  isLoading: false,
  loadingCount: 0, // 동시 요청 수 관리
};

export const loadingSlice = createSlice({
  name: 'loading',
  initialState,
  reducers: {
    showLoading: (state) => {
      state.loadingCount += 1;
      state.isLoading = true;
    },
    hideLoading: (state) => {
      state.loadingCount = Math.max(0, state.loadingCount - 1);
      state.isLoading = state.loadingCount > 0;
    },
    resetLoading: (state) => {
      state.loadingCount = 0;
      state.isLoading = false;
    },
  },
});

export const { showLoading, hideLoading, resetLoading } = loadingSlice.actions;

export default loadingSlice.reducer;
