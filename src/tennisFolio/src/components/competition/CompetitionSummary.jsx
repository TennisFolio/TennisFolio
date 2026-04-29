import { AnimatePresence, motion } from 'framer-motion';

const MotionDiv = motion.div;
const MotionSection = motion.section;
const MotionSpan = motion.span;
const MotionStrong = motion.strong;

function CompetitionSummary({
  totalPlayers,
  participantGameText,
  canCreateGames,
  placementText,
  unavailableReasonText,
}) {
  return (
    <MotionSection
      className={`competition-summary ${canCreateGames ? '' : 'unavailable'}`}
      initial={{ opacity: 0, y: 14 }}
      animate={{
        opacity: 1,
        y: 0,
      }}
      transition={{ duration: 0.26, ease: 'easeOut' }}
    >
      <div className="summary-content">
        <div className="summary-top">
          <div>
            <p className="summary-label">지금 참가자</p>
            <div className="summary-number">
              <MotionStrong
                key={totalPlayers}
                initial={{ scale: 1 }}
                animate={{ scale: [1, 1.04, 1] }}
                transition={{ duration: 0.2, ease: 'easeOut' }}
              >
                {totalPlayers}
              </MotionStrong>
              <span>명</span>
            </div>
          </div>

        <AnimatePresence mode="wait">
            <MotionSpan
              key={canCreateGames ? 'available' : 'unavailable'}
              className={`summary-badge ${
                canCreateGames ? 'available' : 'unavailable'
              }`}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.22, ease: 'easeOut' }}
            >
              {canCreateGames ? '진행돼요' : '조정이 필요해요'}
            </MotionSpan>
          </AnimatePresence>
        </div>

        <div className="summary-details">
          <div>
            <p>1인당 경기</p>
            <MotionStrong
              key={participantGameText}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.2, ease: 'easeOut' }}
            >
              {participantGameText}
            </MotionStrong>
          </div>

          <div>
            <p>진행 상태</p>
            <AnimatePresence mode="wait">
              <MotionDiv
                key={placementText}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.22, ease: 'easeOut' }}
              >
                <strong
                  className={`status-text ${
                    canCreateGames ? '' : 'unavailable'
                  }`}
                >
                  {placementText}
                </strong>
                {!canCreateGames && (
                  <p className="summary-reason">{unavailableReasonText}</p>
                )}
              </MotionDiv>
            </AnimatePresence>
          </div>
        </div>
      </div>
    </MotionSection>
  );
}

export default CompetitionSummary;
