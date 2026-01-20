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
import Schedule from './page/Schedule.jsx';

window.global ||= window;
window.Buffer ||= Buffer;
window.process ||= process;

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Main />} />
          <Route path="/live/:category" element={<LiveEventsList />} />
          <Route path="/ranking/:category" element={<Ranking />} />
          <Route path="/liveEvents/:matchId" element={<LiveEventsDetail />} />
          <Route path="/schedule" element={<Schedule />} />
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
