import { useNavigate } from 'react-router-dom';
import './NotFound.css';

function NotFound() {
  const navigate = useNavigate();

  return (
    <main className="not-found-page">
      <section className="not-found-panel">
        <p className="not-found-code">404</p>
        <h1>페이지를 찾을 수 없습니다</h1>
        <p className="not-found-description">
          주소가 변경되었거나 존재하지 않는 경로입니다.
        </p>
        <div className="not-found-actions">
          <button type="button" onClick={() => navigate('/')}>
            홈으로 이동
          </button>
          <button type="button" className="secondary" onClick={() => navigate(-1)}>
            이전 페이지
          </button>
        </div>
      </section>
    </main>
  );
}

export default NotFound;
