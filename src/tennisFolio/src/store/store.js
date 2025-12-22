import { configureStore } from '@reduxjs/toolkit';
import testReducer from './testSlice';
import loadingReducer from './loadingSlice';
import mswReducer from './mswSlice';
import playerDetailReducer from './playerDetailSlice';

export const store = configureStore({
  reducer: {
    test: testReducer,
    loading: loadingReducer,
    msw: mswReducer,
    playerDetail: playerDetailReducer,
  },
});

export default store;
