import { useEffect, useMemo, useState } from 'react';
import { motion } from 'framer-motion';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from '../utils/apiClient';
import './CompetitionResult.css';

const itemTransition = { duration: 0.24, ease: [0.22, 1, 0.36, 1] };
const MotionHeader = motion.header;
const MotionSection = motion.section;

const RANKING_TABS = [
  { key: 'overall', label: '전체' },
  { key: 'male', label: '남자' },
  { key: 'female', label: '여자' },
];

function getResponseData(response) {
  return response.data?.data ?? response.data;
}

function formatRate(value) {
  return `${Math.round((value ?? 0) * 100)}%`;
}

function formatPointRate(value) {
  return (value ?? 0).toFixed(2);
}

function formatSignedNumber(value) {
  const numberValue = value ?? 0;
  return numberValue > 0 ? `+${numberValue}` : String(numberValue);
}

function getGenderLabel(gender) {
  return gender === 'MALE' ? '남' : '여';
}

function getGenderClass(gender) {
  return gender === 'MALE' ? 'male' : 'female';
}

function CompetitionResult() {
  const { publicId } = useParams();
  const [result, setResult] = useState(null);
  const [activeTab, setActiveTab] = useState('overall');
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [shareFeedback, setShareFeedback] = useState('');

  useEffect(() => {
    let isActive = true;

    async function fetchResult() {
      try {
        setIsLoading(true);
        setErrorMessage('');
        const response = await apiRequest.get(
          `/api/competitions/${publicId}/result`
        );

        if (isActive) {
          setResult(getResponseData(response));
        }
      } catch (error) {
        if (isActive) {
          setErrorMessage(
            error.response?.data?.message ||
              '경기 결과를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.'
          );
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    fetchResult();

    return () => {
      isActive = false;
    };
  }, [publicId]);

  const rankings = result?.rankings?.[activeTab] ?? [];
  const leader = rankings[0];

  const summaryItems = useMemo(
    () => [
      {
        label: '완료 경기',
        value: `${result?.completedGames ?? 0}`,
        suffix: `/ ${result?.totalGames ?? 0}`,
      },
      {
        label: '현재 1위',
        value: leader?.playerName ?? '-',
        suffix: leader ? `승점률 ${formatPointRate(leader.rankingPointRate)}` : '',
      },
    ],
    [leader, result]
  );

  const copyResultLink = async () => {
    const url = window.location.href;
    const shareText = `경기 결과와 현재 순위를 확인하세요.\n${url}`;
    setShareFeedback('');

    try {
      await navigator.clipboard.writeText(shareText);
      setShareFeedback('결과 링크를 복사했어요.');
    } catch {
      setShareFeedback('링크 복사에 실패했습니다. 잠시 후 다시 시도해 주세요.');
    }
  };

  return (
    <main className="competition-result-page">
      <MotionHeader
        className="competition-result-hero"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={itemTransition}
      >
        <div className="competition-result-hero-content">
          <span className="competition-result-label">경기 결과</span>
          <h1>{result?.name ?? '순위표'}</h1>
            <p>승점률 &gt; 승점 &gt; 패배 수 &gt; 득실차 &gt; 경기 수 &gt; TB 득실차</p>
          <p className="competition-result-rule-text">
            <span>승점 : 승 2점 / 무 1점 / 패 0점</span>
            <span>승점률 : 승점을 경기 수로 나눈 값</span>
            <span>TB 득실 : 타이브레이크 득실차</span>
          </p>
          <Link
            className="competition-result-back-link"
            to={`/competitions/${publicId}?view=score`}
          >
            경기 화면으로 돌아가기
          </Link>
          {result && (
            <div className="competition-result-share-box">
              <button type="button" onClick={copyResultLink}>
                결과 링크 복사
              </button>
              {shareFeedback && <p>{shareFeedback}</p>}
            </div>
          )}
        </div>
      </MotionHeader>

      {isLoading && (
        <section className="competition-result-state">
          결과를 불러오고 있어요.
        </section>
      )}

      {!isLoading && errorMessage && (
        <section className="competition-result-message error">
          {errorMessage}
        </section>
      )}

      {!isLoading && !errorMessage && result && (
        <>
          <MotionSection
            className="competition-result-summary"
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ ...itemTransition, delay: 0.05 }}
          >
            {summaryItems.map((item) => (
              <div key={item.label}>
                <span>{item.label}</span>
                <strong>{item.value}</strong>
                {item.suffix && <small>{item.suffix}</small>}
              </div>
            ))}
          </MotionSection>

          <MotionSection
            className="competition-result-panel"
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ ...itemTransition, delay: 0.1 }}
          >
            <div className="competition-result-tabs" aria-label="순위 필터">
              {RANKING_TABS.map((tab) => (
                <button
                  key={tab.key}
                  type="button"
                  className={activeTab === tab.key ? 'active' : ''}
                  onClick={() => setActiveTab(tab.key)}
                >
                  {tab.label}
                </button>
              ))}
            </div>

            {rankings.length > 0 ? (
              <div className="competition-ranking-list">
                {rankings.map((player) => (
                  <article
                    className={`competition-ranking-card rank-${Math.min(
                      player.rank,
                      3
                    )}`}
                    key={`${activeTab}-${player.competitionEntryId}`}
                  >
                    <div className="ranking-main">
                      <strong className="ranking-number">{player.rank}</strong>
                      <div className="ranking-player">
                        <span className={getGenderClass(player.gender)}>
                          {getGenderLabel(player.gender)}
                        </span>
                        <h2>{player.playerName}</h2>
                      </div>
                      <div className="ranking-record">
                        {player.wins}승 {player.draws ?? 0}무 {player.losses}패
                      </div>
                    </div>

                    <div className="ranking-stats">
                      <div>
                        <span>경기</span>
                        <strong>{player.gamesPlayed}</strong>
                      </div>
                      <div>
                        <span>승률</span>
                        <strong>{formatRate(player.winRate)}</strong>
                      </div>
                      <div>
                        <span>승점</span>
                        <strong>{player.rankingPoints}</strong>
                      </div>
                      <div>
                        <span>승점률</span>
                        <strong>{formatPointRate(player.rankingPointRate)}</strong>
                      </div>
                      <div>
                        <span>득실</span>
                        <strong>{formatSignedNumber(player.pointDiff)}</strong>
                      </div>
                      <div>
                        <span>득점</span>
                        <strong>{player.pointsFor}</strong>
                      </div>
                      <div>
                        <span>실점</span>
                        <strong>{player.pointsAgainst}</strong>
                      </div>
                      <div>
                        <span>TB 득실</span>
                        <strong>
                          {formatSignedNumber(player.tiebreakPointDiff)}
                        </strong>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <div className="competition-result-empty">
                표시할 순위 데이터가 없어요.
              </div>
            )}
          </MotionSection>
        </>
      )}
    </main>
  );
}

export default CompetitionResult;
