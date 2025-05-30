import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Main from './page/Main'
import Ranking from './page/Ranking'
import Layout from './Layout'
import LiveEventsDetail from './page/LiveEventsDetail'
import {Buffer} from 'buffer';
import process from 'process';
import TestDetail from './page/TestDetail.jsx'
import TestList from './page/TestList.jsx'
import TestResult from './page/TestResult.jsx'
import { Navigate } from 'react-router-dom';

window.global ||= window;
window.Buffer ||= Buffer;
window.process ||= process;

export const base_server_url = "http://localhost:8080";

function App() {
  

  return (
  <BrowserRouter>
    <Layout>
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/ranking" element={<Ranking />} />
        <Route path="/liveEvents/:matchId" element={<LiveEventsDetail />}/>
        <Route path="/test" element={<TestList />} />
        <Route path="/test/:category" element={<TestDetail />} />
        <Route path="/test/:category/result" element={<Navigate to="/test/:category" />} />
        <Route path="/test/:category/result/:query" element={<TestResult />} />
      </Routes>
    </Layout>
   </BrowserRouter>
  )
}

export default App
