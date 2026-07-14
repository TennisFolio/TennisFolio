import { default_oauth_provider } from '@/constants';
import ClubState from '../../components/club/ClubState';
import { loginWithProvider } from '../../utils/authApi';
import '../Club.css';

function ClubAuthRequired() {
  return (
    <main className="club-mobile-page">
      <ClubState
        eyebrow="CLUB"
        title="로그인이 필요합니다"
        description="클럽 목록과 클럽원 관리는 로그인한 사용자만 사용할 수 있습니다."
      >
        <button
          type="button"
          className="club-button primary full"
          onClick={() => loginWithProvider(default_oauth_provider)}
        >
          로그인하기
        </button>
      </ClubState>
    </main>
  );
}

export default ClubAuthRequired;
