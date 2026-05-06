import { AnimatePresence, motion } from 'framer-motion';
import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  COMPETITION_FIELDS,
  useCompetitionCreateForm,
} from '../../hooks/useCompetitionCreateForm';
import CompetitionFieldStepper from './CompetitionFieldStepper';
import CompetitionSummary from './CompetitionSummary';
import {
  markCompetitionAdminLinkPrompt,
  saveCompetitionEditToken,
} from '../../utils/competitionEditToken';
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
    placementText,
    participantGameText,
    unavailableReasonText,
    updateCompetitionField,
    stepCompetitionField,
    createCompetition,
  } = useCompetitionCreateForm();

  latestFormStateRef.current = {
    totalPlayers,
    canCreateGames,
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const createdCompetition = await createCompetition();
    if (createdCompetition?.publicId) {
      createdRef.current = true;
      saveCompetitionEditToken(
        createdCompetition.publicId,
        createdCompetition.editToken
      );
      markCompetitionAdminLinkPrompt(createdCompetition.publicId);
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
            <p>코트 수와 진행 시간을 기준으로 경기 수를 계산해요.</p>
          </div>
          <span className="competition-rate">코트당 2경기/h</span>
        </div>

        <MotionDiv
          className="competition-fields"
          variants={staggerContainer}
          initial="hidden"
          animate="show"
        >
          {COMPETITION_FIELDS.map((field) => (
            <CompetitionFieldStepper
              key={field.name}
              field={field}
              value={competitionForm[field.name]}
              onChange={updateCompetitionField}
              onStep={stepCompetitionField}
            />
          ))}
        </MotionDiv>
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
        className="competition-submit-button"
        disabled={isCreatingCompetition}
        whileHover={isCreatingCompetition ? undefined : { scale: 1.03 }}
        whileTap={isCreatingCompetition ? undefined : { scale: 0.97 }}
        transition={{ duration: 0.18, ease: 'easeOut' }}
      >
        {isCreatingCompetition ? '대진표를 만들고 있어요' : '복식 대진표 만들기'}
      </MotionButton>
    </form>
  );
}

export default CompetitionCreateForm;
