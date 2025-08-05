import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LiveEventsList from './page/LiveEventsList.jsx';
import Ranking from './page/Ranking';
import Layout from './Layout';
import LiveEventsDetail from './page/LiveEventsDetail';
import { Buffer } from 'buffer';
import process from 'process';
import TestDetail from './page/TestDetail.jsx';
import TestList from './page/TestList.jsx';
import TestResult from './page/TestResult.jsx';
import { Navigate } from 'react-router-dom';
import Privacy from './components/main/Privacy.jsx';
import Main from './page/Main.jsx';

window.global ||= window;
window.Buffer ||= Buffer;
window.process ||= process;

//export const base_url = "http://localhost:5173";
//export const base_server_url = "http://localhost:8080";
export const base_url = 'https://tennisfolio.net';
export const base_server_url = "https://tennisfolio.net";
//export const base_server_url = 'https://tennisfolio-stg.onrender.com';
export const base_image_url = 'https://tennisfolio.net/img';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Main />} />
          <Route path="/live/:category" element={<LiveEventsList />} />
          <Route path="/ranking" element={<Ranking />} />
          <Route path="/liveEvents/:matchId" element={<LiveEventsDetail />} />
          <Route path="/test" element={<TestList />} />
          <Route path="/test/:category" element={<TestDetail />} />
          <Route
            path="/test/:category/result"
            element={<Navigate to="/test/:category" />}
          />
          <Route
            path="/test/:category/result/:query"
            element={<TestResult />}
          />

          <Route path="/privacy" element={<Privacy />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
