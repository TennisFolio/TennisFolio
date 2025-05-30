import {createSlice} from '@reduxjs/toolkit';

const testSlice = createSlice({
    name: 'test',
    initialState: {
        currentTest: null,
        testResult : null,
    },
    reducers: {
        setCurrentTest: (state, action) => {
            state.currentTest = action.payload;
        },
        clearCurrentTest: (state) => {
            state.currentTest = null;
        },
        setTestResult: (state, action) => {
            state.testResult = action.payload;
        },
        clearTestResult: (state) => {
            state.testResult = null;
        },
    },
});

export const {
    setCurrentTest,
    clearCurrentTest,
    setTestResult,
    clearTestResult,
} = testSlice.actions;

export default testSlice.reducer;