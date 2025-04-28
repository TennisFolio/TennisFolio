import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Main from './page/Main'
import Ranking from './page/Ranking'
import Layout from './Layout'


function App() {
  

  return (
  <BrowserRouter>
    <Layout>
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/ranking" element={<Ranking />} />
      </Routes>
    </Layout>
   </BrowserRouter>
  )
}

export default App
