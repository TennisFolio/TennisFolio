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
import MeetingManage from './page/MeetingManage.jsx';
import MeetingUpdate from './page/MeetingUpdate.jsx';
import MeetingPublic from './page/MeetingPublic.jsx';
import ClubCreatePage from './page/club/ClubCreatePage.jsx';
import ClubDetailPage from './page/club/ClubDetailPage.jsx';
import ClubDashboardPage from './page/club/ClubDashboardPage.jsx';
import ClubEditPage from './page/club/ClubEditPage.jsx';
import ClubListPage from './page/club/ClubListPage.jsx';
import ClubMemberBulkAddPage from './page/club/ClubMemberBulkAddPage.jsx';
import ClubMemberManagementPage from './page/club/ClubMemberManagementPage.jsx';
import ClubMemberDetailPage from './page/club/ClubMemberDetailPage.jsx';
import ClubAccountManagementPage from './page/club/ClubAccountManagementPage.jsx';
import ClubAccountDetailPage from './page/club/ClubAccountDetailPage.jsx';
import ClubAccountPage from './page/club/ClubAccountPage.jsx';
import ClubInvitationAcceptPage from './page/club/ClubInvitationAcceptPage.jsx';
import Schedule from './page/Schedule.jsx';
import NotFound from './page/NotFound.jsx';
import { initializeAnalytics, trackPageView } from './utils/analytics';
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

function ClubMeetingPublicRedirect() {
  const { publicId } = useParams();

  return <Navigate to={`/meetings/${publicId}`} replace />;
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
    initializeAnalytics();
  }, []);

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
          <Route path="/clubs" element={<ClubListPage currentUser={currentUser} />} />
          <Route
            path="/clubs/new"
            element={<ClubCreatePage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:publicId"
            element={<ClubDetailPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard"
            element={<ClubDashboardPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard/members"
            element={<ClubMemberManagementPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard/members/paste"
            element={<ClubMemberBulkAddPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard/members/:memberId"
            element={<ClubMemberDetailPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard/accounts"
            element={<ClubAccountManagementPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard/accounts/:clubAccountId"
            element={<ClubAccountDetailPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/dashboard/account"
            element={<ClubAccountPage currentUser={currentUser} />}
          />
          <Route
            path="/club-account-invitations/:token"
            element={<ClubInvitationAcceptPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:publicId/edit"
            element={<ClubEditPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:publicId/members/bulk"
            element={<ClubMemberBulkAddPage currentUser={currentUser} />}
          />
          <Route
            path="/clubs/:clubPublicId/meetings/new"
            element={<MeetingCreate />}
          />
          <Route
            path="/clubs/:clubPublicId/meetings/:publicId/manage"
            element={<MeetingManage />}
          />
          <Route
            path="/clubs/:clubPublicId/meetings/:publicId/edit"
            element={<MeetingUpdate />}
          />
          <Route
            path="/clubs/:clubPublicId/meetings/:publicId"
            element={<ClubMeetingPublicRedirect />}
          />
          <Route path="/meetings/new" element={<MeetingCreate />} />
          <Route path="/meetings/:publicId/manage" element={<MeetingManage />} />
          <Route path="/meetings/:publicId" element={<MeetingPublic />} />
          <Route path="/meetings/:publicId/edit" element={<MeetingUpdate />} />
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
