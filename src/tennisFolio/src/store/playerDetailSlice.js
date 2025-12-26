import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  isOpen: false,
  playerId: null,
  playerData: null,
  isLoading: false,
};

const playerDetailSlice = createSlice({
  name: 'playerDetail',
  initialState,
  reducers: {
    openPlayerDetail: (state, action) => {
      state.isOpen = true;
      state.playerId = action.payload;
      state.playerData = null; // 새로운 선수 선택 시 데이터 초기화
      state.isLoading = false;
    },
    closePlayerDetail: (state) => {
      state.isOpen = false;
      state.playerId = null;
      state.playerData = null;
      state.isLoading = false;
    },
    setPlayerData: (state, action) => {
      state.playerData = action.payload;
      state.isLoading = false;
    },
    setLoading: (state, action) => {
      state.isLoading = action.payload;
    },
  },
});

export const {
  openPlayerDetail,
  closePlayerDetail,
  setPlayerData,
  setLoading,
} = playerDetailSlice.actions;

export const selectPlayerDetail = (state) => state.playerDetail;

export default playerDetailSlice.reducer;



