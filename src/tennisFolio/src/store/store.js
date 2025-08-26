import { configureStore } from '@reduxjs/toolkit';
import testReducer from './testSlice';
import loadingReducer from './loadingSlice';
import mswReducer from './mswSlice';

export const store = configureStore({
  reducer: {
    test: testReducer,
    loading: loadingReducer,
    msw: mswReducer,
  },
});

export default store;
