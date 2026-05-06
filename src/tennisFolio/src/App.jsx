import { useEffect } from 'react';
import {
  BrowserRouter,
  Navigate,
  Routes,
  Route,
  useLocation,
  useParams,
} from 'react-router-dom';
import LiveEventsList from './page/LiveEventsList.jsx';
import Ranking from './page/Ranking';
import Layout from './Layout';
import LiveEventsDetail from './page/LiveEventsDetail';
import { Buffer } from 'buffer';
import process from 'process';
import TestDetail from './page/TestDetail.jsx';
import TestList from './page/TestList.jsx';
import TestResult from './page/TestResult.jsx';
import Privacy from './components/main/Privacy.jsx';
import Competition from './page/Competition.jsx';
import CompetitionDetail from './page/CompetitionDetail.jsx';
import CompetitionResult from './page/CompetitionResult.jsx';
import Schedule from './page/Schedule.jsx';
import NotFound from './page/NotFound.jsx';
import { trackPageView } from './utils/analytics';

window.global ||= window;
window.Buffer ||= Buffer;
window.process ||= process;

function CompetitionManageRedirect() {
  const { publicId } = useParams();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  searchParams.set('view', 'manage');
  const search = searchParams.toString();

  return (
    <Navigate
      to={`/competitions/${publicId}${search ? `?${search}` : ''}`}
      replace
    />
  );
}

function AnalyticsRouteTracker() {
  const location = useLocation();

  useEffect(() => {
    trackPageView(`${location.pathname}${location.search}`);
  }, [location.pathname, location.search]);

  return null;
}

function App() {
  return (
    <BrowserRouter>
      <AnalyticsRouteTracker />
      <Layout>
        <Routes>
          <Route path="/" element={<Competition />} />
          <Route path="/competitions/:publicId" element={<CompetitionDetail />} />
          <Route
            path="/competitions/:publicId/manage"
            element={<CompetitionManageRedirect />}
          />
          <Route path="/competitions/:publicId/result" element={<CompetitionResult />} />
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
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
