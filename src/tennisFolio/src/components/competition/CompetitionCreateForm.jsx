import { AnimatePresence, motion } from 'framer-motion';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  COMPETITION_FIELDS,
  COMPETITION_CREATE_MODES,
  useCompetitionCreateForm,
} from '../../hooks/useCompetitionCreateForm';
import CompetitionFieldStepper from './CompetitionFieldStepper';
import CompetitionPlayerNameEditor from './CompetitionPlayerNameEditor';
import CompetitionSummary from './CompetitionSummary';
import { saveCompetitionAdminToken } from '../../utils/competitionEditToken';
import { trackEvent } from '../../utils/analytics';

const staggerContainer = {
  hidden: {},
  show: {
    transition: {
      staggerChildren: 0.06,
    },
  },
};

const fadeUp = {
  hidden: { opacity: 0, y: 14 },
  show: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.24, ease: [0.22, 1, 0.36, 1] },
  },
};

const MotionButton = motion.button;
const MotionDiv = motion.div;
const MotionSection = motion.section;

function CompetitionCreateForm() {
  const navigate = useNavigate();
  const [isPlayerNameEditorOpen, setIsPlayerNameEditorOpen] = useState(false);
  const createdRef = useRef(false);
  const latestFormStateRef = useRef({
    totalPlayers: 0,
    canCreateGames: false,
  });
  const {
    competitionForm,
    competitionError,
    competitionResult,
    isCreatingCompetition,
    totalPlayers,
    canCreateGames,
    canSubmitCompetition,
    isClubSession,
    sameGenderDoublesOnlyUnavailable,
    sameGenderDoublesOnlyUnavailableReason,
    placementText,
    participantGameText,
    unavailableReasonText,
    updateCompetitionField,
    stepCompetitionField,
    updateCompetitionPlayerName,
    createCompetition,
  } = useCompetitionCreateForm();

  const visibleFields = isClubSession
    ? COMPETITION_FIELDS.filter((field) => field.name !== 'totalGames')
    : COMPETITION_FIELDS;

  latestFormStateRef.current = {
    totalPlayers,
    canCreateGames,
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const createdCompetition = await createCompetition();
    if (createdCompetition?.publicId) {
      createdRef.current = true;
      saveCompetitionAdminToken(
        createdCompetition.publicId,
        createdCompetition.competitionAdminToken
      );
      navigate(`/competitions/${createdCompetition.publicId}`);
    }
  };

  useEffect(() => {
    return () => {
      if (!createdRef.current) {
        trackEvent('competition_create_funnel_exit', {
          funnel_step: 'create_form',
          total_players: latestFormStateRef.current.totalPlayers,
          can_create_games: latestFormStateRef.current.canCreateGames,
        });
      }
    };
  }, []);

  return (
    <form className="competition-form" onSubmit={handleSubmit}>
      <CompetitionSummary
        totalPlayers={totalPlayers}
        participantGameText={participantGameText}
        canCreateGames={canCreateGames}
        isClubSession={isClubSession}
        placementText={placementText}
        unavailableReasonText={unavailableReasonText}
      />

      <MotionSection
        className="competition-config"
        variants={fadeUp}
        initial="hidden"
        animate="show"
        transition={{ delay: 0.12 }}
      >
        <div className="competition-section-heading">
          <div>
            <h2>경기 조건</h2>
            <p>
              {isClubSession
                ? '경기를 대기열에 쌓아두고 코트별로 운영해요.'
                : '총 경기 수를 정해서 처음부터 전체 경기 일정을 생성해요.'}
            </p>
          </div>
          <span className="competition-rate">
            {isClubSession ? '운영형' : '완성형'}
          </span>
        </div>

        <div className="competition-mode-toggle" aria-label="대진 생성 방식">
          <button
            type="button"
            className={isClubSession ? 'active' : ''}
            aria-pressed={isClubSession}
            onClick={() =>
              updateCompetitionField(
                'mode',
                COMPETITION_CREATE_MODES.CLUB_SESSION
              )
            }
          >
            <strong>진행형 대진</strong>
            <span>늦참·중간 퇴장이 있는 현장 운영</span>
          </button>
          <button
            type="button"
            className={!isClubSession ? 'active' : ''}
            aria-pressed={!isClubSession}
            onClick={() =>
              updateCompetitionField(
                'mode',
                COMPETITION_CREATE_MODES.FIXED_SCHEDULE
              )
            }
          >
            <strong>전체 대진</strong>
            <span>참가자가 확정된 일정 운영</span>
          </button>
        </div>

        <p className="competition-mode-help">
          {isClubSession
            ? '참가자를 중간에 추가하거나 대기 처리하면서, 비는 코트마다 다음 경기를 만듭니다.'
            : '참가자가 확정된 모임에서 입력한 총 경기 수만큼 전체 대진을 한 번에 생성합니다.'}
        </p>

        {!isClubSession && (
          <div
            className={`same-gender-option ${
              competitionForm.sameGenderDoublesOnly
              && sameGenderDoublesOnlyUnavailable
                ? 'disabled'
                : ''
            }`}
          >
            <button
              type="button"
              className={`same-gender-switch ${
                competitionForm.sameGenderDoublesOnly ? 'active' : ''
              }`}
              role="switch"
              aria-checked={competitionForm.sameGenderDoublesOnly}
              onClick={() =>
                updateCompetitionField(
                  'sameGenderDoublesOnly',
                  !competitionForm.sameGenderDoublesOnly
                )
              }
            >
              <span className="same-gender-switch-track">
                <span className="same-gender-switch-thumb" />
              </span>
              <span className="same-gender-switch-copy">
                <strong>혼복 제외</strong>
              </span>
            </button>
            {competitionForm.sameGenderDoublesOnly
              && sameGenderDoublesOnlyUnavailable && (
              <p className="same-gender-option-warning">
                {sameGenderDoublesOnlyUnavailableReason}
              </p>
            )}
          </div>
        )}

        <MotionDiv
          className="competition-fields"
          variants={staggerContainer}
          initial="hidden"
          animate="show"
        >
          {visibleFields.map((field) => (
            <CompetitionFieldStepper
              key={field.name}
              field={field}
              value={competitionForm[field.name]}
              onChange={updateCompetitionField}
              onStep={stepCompetitionField}
            />
          ))}
        </MotionDiv>

        <CompetitionPlayerNameEditor
          isOpen={isPlayerNameEditorOpen}
          malePlayerNames={competitionForm.malePlayerNames}
          femalePlayerNames={competitionForm.femalePlayerNames}
          onToggle={() => setIsPlayerNameEditorOpen((prev) => !prev)}
          onChange={updateCompetitionPlayerName}
        />
      </MotionSection>

      <AnimatePresence mode="wait">
        {competitionError && (
          <MotionDiv
            key={competitionError}
            className="competition-message error"
            initial={{ opacity: 0, y: 8, x: 0 }}
            animate={{
              opacity: 1,
              y: 0,
              x: [0, -5, 5, -4, 4, 0],
            }}
            exit={{ opacity: 0, y: 8 }}
            transition={{ duration: 0.28, ease: 'easeOut' }}
          >
            {competitionError}
          </MotionDiv>
        )}
      </AnimatePresence>

      <AnimatePresence mode="wait">
        {competitionResult && (
          <MotionDiv
            key={competitionResult.publicId}
            className="competition-message success"
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 8 }}
            transition={{ duration: 0.22, ease: 'easeOut' }}
          >
            <span>일정을 만들었어요</span>
            <strong>{competitionResult.publicId}</strong>
          </MotionDiv>
        )}
      </AnimatePresence>

      <MotionButton
        type="submit"
        className={`competition-submit-button ${
          canSubmitCompetition ? '' : 'needs-adjustment'
        }`}
        disabled={isCreatingCompetition || !canSubmitCompetition}
        whileHover={isCreatingCompetition ? undefined : { y: -1 }}
        whileTap={isCreatingCompetition ? undefined : { scale: 0.985 }}
        transition={{ duration: 0.18, ease: 'easeOut' }}
      >
        {isCreatingCompetition
          ? '대진표를 만들고 있어요'
          : isClubSession
            ? '진행형 경기 만들기'
            : '복식 대진표 만들기'}
      </MotionButton>
    </form>
  );
}

export default CompetitionCreateForm;
