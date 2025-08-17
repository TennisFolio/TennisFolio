import { configureStore } from '@reduxjs/toolkit';
import testReducer from './testSlice';
import loadingReducer from './loadingSlice';

export const store = configureStore({
  reducer: {
    test: testReducer,
    loading: loadingReducer,
  },
});

export default store;
