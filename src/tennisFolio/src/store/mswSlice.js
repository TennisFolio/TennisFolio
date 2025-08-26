import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  isActive: false, // MSW 활성화 상태
  isInitialized: false, // MSW 초기화 완료 여부
};

const mswSlice = createSlice({
  name: 'msw',
  initialState,
  reducers: {
    setMSWActive: (state, action) => {
      state.isActive = action.payload;
    },
    setMSWInitialized: (state, action) => {
      state.isInitialized = action.payload;
    },
    toggleMSW: (state) => {
      state.isActive = !state.isActive;
    },
  },
});

export const { setMSWActive, setMSWInitialized, toggleMSW } = mswSlice.actions;

// 선택자
export const selectMSWActive = (state) => state.msw.isActive;
export const selectMSWInitialized = (state) => state.msw.isInitialized;

export default mswSlice.reducer;
