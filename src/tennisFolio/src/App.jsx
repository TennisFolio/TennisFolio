import { useEffect, useState } from 'react';
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
import MyCompetitions from './page/MyCompetitions.jsx';
import Meetings from './page/Meetings.jsx';
import MeetingCreate from './page/MeetingCreate.jsx';
import MeetingUpdate from './page/MeetingUpdate.jsx';
import MeetingPublic from './page/MeetingPublic.jsx';
import MeetingManage from './page/MeetingManage.jsx';
import Schedule from './page/Schedule.jsx';
import NotFound from './page/NotFound.jsx';
import { trackPageView } from './utils/analytics';
import { getCurrentUser, updateProfile } from './utils/authApi';
import ProfileSetupSheet from './components/auth/ProfileSetupSheet.jsx';
import { shouldShowProfileSetup } from './utils/profileSetup.js';

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
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    let cancelled = false;

    getCurrentUser()
      .then((response) => {
        if (!cancelled) {
          setCurrentUser(response.data.data);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setCurrentUser(null);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const handleProfileSubmit = async (profile) => {
    const response = await updateProfile(profile);
    setCurrentUser(response.data.data);
  };

  return (
    <BrowserRouter>
      <AnalyticsRouteTracker />
      <Layout currentUser={currentUser} onLogout={() => setCurrentUser(null)}>
        <Routes>
          <Route path="/" element={<Competition />} />
          <Route path="/me/competitions" element={<MyCompetitions />} />
          <Route path="/meetings" element={<Meetings />} />
          <Route path="/meetings/new" element={<MeetingCreate />} />
          <Route path="/meetings/:publicId" element={<MeetingPublic />} />
          <Route path="/meetings/:publicId/edit" element={<MeetingUpdate />} />
          <Route path="/meetings/:publicId/manage" element={<MeetingManage />} />
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
        {shouldShowProfileSetup(currentUser) && (
          <ProfileSetupSheet onSubmit={handleProfileSubmit} />
        )}
      </Layout>
    </BrowserRouter>
  );
}

export default App;
