import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { default_oauth_provider } from '@/constants';
import {
  deleteMyCompetition,
  getMyCompetitions,
  isAuthenticationRequiredError,
  loginWithProvider,
} from '../utils/authApi';
import './MyCompetitions.css';

const INITIAL_VISIBLE_COUNT = 5;
const LOAD_MORE_COUNT = 5;

function formatCreatedAt(value) {
  if (!value) {
    return '';
  }

  return value.replace('T', ' ').slice(0, 16);
}

function formatMode(mode) {
  if (mode === 'CLUB_SESSION') {
    return '진행형 경기';
  }

  return '전체 대진표';
}

function MyCompetitions() {
  const navigate = useNavigate();
  const [competitions, setCompetitions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [deletingPublicId, setDeletingPublicId] = useState('');
  const [visibleCount, setVisibleCount] = useState(INITIAL_VISIBLE_COUNT);
  const [authRequired, setAuthRequired] = useState(false);

  useEffect(() => {
    let cancelled = false;

    getMyCompetitions()
      .then((response) => {
        if (cancelled) {
          return;
        }
        setCompetitions(response.data.data || []);
        setVisibleCount(INITIAL_VISIBLE_COUNT);
        setErrorMessage('');
        setActionMessage('');
        setAuthRequired(false);
      })
      .catch((error) => {
        if (cancelled) {
          return;
        }
        if (isAuthenticationRequiredError(error)) {
          setAuthRequired(true);
          setErrorMessage('');
        } else {
          setAuthRequired(false);
          setErrorMessage('경기 관리 목록을 불러오지 못했습니다.');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!actionMessage) {
      return undefined;
    }

    const timerId = setTimeout(() => {
      setActionMessage('');
    }, 2400);

    return () => {
      clearTimeout(timerId);
    };
  }, [actionMessage]);

  const handleDeleteCompetition = async (competition) => {
    const confirmed = window.confirm(
      '삭제하면 공유 링크와 경기 운영 화면에 더 이상 접근할 수 없습니다. 삭제할까요?'
    );
    if (!confirmed) {
      return;
    }

    try {
      setDeletingPublicId(competition.publicId);
      await deleteMyCompetition(competition.publicId);
      setCompetitions((current) =>
        current.filter((item) => item.publicId !== competition.publicId)
      );
      setActionMessage('경기를 삭제했어요.');
      setErrorMessage('');
    } catch (error) {
      setActionMessage('');
      setErrorMessage(
        error.response?.data?.message || '경기를 삭제하지 못했어요.'
      );
    } finally {
      setDeletingPublicId('');
    }
  };

  const visibleCompetitions = competitions.slice(0, visibleCount);
  const hasMoreCompetitions = visibleCount < competitions.length;

  if (!isLoading && authRequired) {
    return (
      <main className="my-competitions-page">
        <section className="my-competitions-header">
          <div className="my-competitions-title-block">
            <div className="my-competitions-title-row">
              <h1>경기 관리</h1>
            </div>
            <p>로그인하면 만든 경기와 저장한 경기를 한곳에서 다시 열 수 있습니다.</p>
          </div>
          <button
            type="button"
            className="my-competitions-login-button"
            onClick={() => loginWithProvider(default_oauth_provider)}
          >
            로그인하기
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="my-competitions-page">
      <header className="my-competitions-header">
        <div className="my-competitions-title-block">
          <div className="my-competitions-title-row">
            <h1>경기 관리</h1>
            {!isLoading && !errorMessage && !authRequired && (
              <span>{competitions.length}개</span>
            )}
          </div>
          <p>로그인하면 만든 경기와 저장한 경기를 한곳에서 다시 열 수 있습니다.</p>
        </div>
        <button
          type="button"
          className="my-competitions-create-button"
          onClick={() => navigate('/')}
        >
          <span aria-hidden="true">+</span>
          새 경기 만들기
        </button>
      </header>

      {isLoading && (
        <p className="my-competitions-state">불러오는 중입니다.</p>
      )}

      {!isLoading && errorMessage && (
        <p className="my-competitions-state error">{errorMessage}</p>
      )}

      {!isLoading && actionMessage && (
        <p className="my-competitions-state success">{actionMessage}</p>
      )}

      {!isLoading &&
        !authRequired &&
        !errorMessage &&
        competitions.length === 0 && (
        <section className="my-competitions-empty">
          <h2>아직 연결된 경기가 없습니다.</h2>
          <p>로그인한 상태에서 경기를 만들면 여기에 표시됩니다.</p>
        </section>
      )}

      {!isLoading &&
        !authRequired &&
        !errorMessage &&
        competitions.length > 0 && (
        <div className="my-competitions-list">
          {visibleCompetitions.map((competition) => (
            <article
              className="my-competition-item"
              key={competition.publicId}
            >
              <button
                type="button"
                className="my-competition-open-button"
                onClick={() => navigate(`/competitions/${competition.publicId}`)}
              >
                <span className="my-competition-topline">
                  {formatMode(competition.mode)}
                </span>
                <span className="my-competition-name">{competition.name}</span>
                <span className="my-competition-created">
                  {formatCreatedAt(competition.createdAt)}
                </span>
              </button>
              <span className="my-competition-meta-row">
                <span className="my-competition-stats">
                  <span>남성 {competition.maleCount}</span>
                  <span>여성 {competition.femaleCount}</span>
                  <span>코트 {competition.courtCount}</span>
                </span>
                <span className="my-competition-actions">
                  <button
                    type="button"
                    className="my-competition-delete-button"
                    disabled={deletingPublicId === competition.publicId}
                    onClick={() => handleDeleteCompetition(competition)}
                  >
                    {deletingPublicId === competition.publicId ? '삭제 중' : '삭제'}
                  </button>
                </span>
              </span>
            </article>
          ))}
          {hasMoreCompetitions && (
            <button
              type="button"
              className="my-competitions-load-more-button"
              onClick={() =>
                setVisibleCount((current) => current + LOAD_MORE_COUNT)
              }
            >
              더 보기
            </button>
          )}
        </div>
      )}
    </main>
  );
}

export default MyCompetitions;
