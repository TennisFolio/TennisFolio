import { useEffect, useState } from 'react';
import { formatRoundSummary } from './competitionDetailSummaryLabels';

const COMPETITION_MODES = {
  MANAGE: 'manage',
  SCORE: 'score',
};

function formatCreatedAt(value) {
  if (!value) {
    return '-';
  }

  const date = Array.isArray(value)
    ? new Date(
        value[0],
        (value[1] ?? 1) - 1,
        value[2] ?? 1,
        value[3] ?? 0,
        value[4] ?? 0,
        value[5] ?? 0
      )
    : new Date(value);

  if (Number.isNaN(date.getTime())) {
    return '-';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
}

function CompetitionDetailSummary({
  competition,
  balance,
  mode,
  canManage,
  isSavingName,
  nameError,
  nameSuccess,
  onSaveName,
}) {
  const stat = competition.stat;
  const totalPlayers =
    (competition.maleCount ?? 0) + (competition.femaleCount ?? 0);
  const isClubSession = competition.mode === 'CLUB_SESSION';
  const [nameDraft, setNameDraft] = useState(competition.name ?? '');

  useEffect(() => {
    setNameDraft(competition.name ?? '');
  }, [competition.name]);

  const isNameChanged = nameDraft.trim() !== (competition.name ?? '');
  const showNameEditor =
    canManage && (!isClubSession || mode === COMPETITION_MODES.MANAGE);

  const handleNameSubmit = (event) => {
    event.preventDefault();
    if (!isNameChanged || isSavingName) {
      return;
    }
    onSaveName(nameDraft);
  };

  return (
    <>
      {showNameEditor ? (
        <form className="competition-name-editor" onSubmit={handleNameSubmit}>
          <div className="competition-name-row">
            <input
              id="competition-name"
              aria-label="대회 이름"
              type="text"
              value={nameDraft}
              maxLength={50}
              onChange={(event) => setNameDraft(event.target.value)}
            />
            <button type="submit" disabled={!isNameChanged || isSavingName}>
              {isSavingName ? '저장 중' : '저장'}
            </button>
          </div>
          {nameError && <p className="competition-name-error">{nameError}</p>}
          {nameSuccess && (
            <p className="competition-name-success">{nameSuccess}</p>
          )}
          <p className="competition-created-at">
            생성일 {formatCreatedAt(competition.createdAt)}
          </p>
        </form>
      ) : (
        <div className="competition-name-view">
          <h2>{competition.name}</h2>
          <span>생성일 {formatCreatedAt(competition.createdAt)}</span>
        </div>
      )}

      <div className="competition-detail-summary">
        <div>
          <p>참가 인원</p>
          <strong>{totalPlayers}명</strong>
        </div>
        <div>
          <p>코트</p>
          <strong>{competition.courtCount}개</strong>
        </div>
        <div>
          <p>{isClubSession ? '운영 방식' : '라운드'}</p>
          <strong>
            {isClubSession ? (
              '클럽'
            ) : (
              <>
                {formatRoundSummary(competition.rounds)}
              </>
            )}
          </strong>
        </div>
      </div>

      {stat && (
        <>
          <div className="competition-stat-grid">
            <div className="total">
              <span>전체 경기</span>
              <strong>{stat.totalGames}</strong>
            </div>
            <div className="type-male">
              <span>남복</span>
              <strong>{stat.maleCount ?? 0}</strong>
            </div>
            <div className="type-female">
              <span>여복</span>
              <strong>{stat.femaleCount ?? 0}</strong>
            </div>
            <div className="type-mixed">
              <span>혼복</span>
              <strong>{stat.mixedCount ?? 0}</strong>
            </div>
            <div className="type-mixed">
              <span>남2:여2</span>
              <strong>{stat.m2f2SplitCount ?? 0}</strong>
            </div>
            <div className="type-random">
              <span>남3/여1</span>
              <strong>{stat.randomM3F1Count ?? 0}</strong>
            </div>
            <div className="type-random">
              <span>남1/여3</span>
              <strong>{stat.randomM1F3Count ?? 0}</strong>
            </div>
          </div>

          {!isClubSession && balance && (
            <div
              className={`competition-balance-message ${
                balance.difference > 0 ? 'warning' : ''
              }`}
            >
              {balance.message}
            </div>
          )}
        </>
      )}
    </>
  );
}

export { COMPETITION_MODES };
export default CompetitionDetailSummary;
