import { AnimatePresence, motion } from 'framer-motion';
import { PLAYER_NAME_MAX_LENGTH } from '../../hooks/competitionCreateFormNames';

const MotionDiv = motion.div;

function CompetitionPlayerNameEditor({
  isOpen,
  malePlayerNames,
  femalePlayerNames,
  onToggle,
  onChange,
}) {
  return (
    <div className="competition-player-names">
      <button
        type="button"
        className="player-name-toggle"
        aria-expanded={isOpen}
        onClick={onToggle}
      >
        <span>
          <strong>참가자 이름 직접 입력</strong>
          <small>기본 이름을 바꾸고 싶은 사람만 수정해요</small>
        </span>
        <b aria-hidden="true">{isOpen ? '닫기' : '열기'}</b>
      </button>

      <AnimatePresence initial={false}>
        {isOpen && (
          <MotionDiv
            className="player-name-panel"
            initial={{ opacity: 0, height: 0, y: -6 }}
            animate={{ opacity: 1, height: 'auto', y: 0 }}
            exit={{ opacity: 0, height: 0, y: -6 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
          >
            <PlayerNameGroup
              title="남자"
              names={malePlayerNames}
              gender="MALE"
              prefix="M"
              onChange={onChange}
            />
            <PlayerNameGroup
              title="여자"
              names={femalePlayerNames}
              gender="FEMALE"
              prefix="F"
              onChange={onChange}
            />
          </MotionDiv>
        )}
      </AnimatePresence>
    </div>
  );
}

function PlayerNameGroup({ title, names, gender, prefix, onChange }) {
  if (names.length === 0) {
    return null;
  }

  return (
    <div className="player-name-group">
      <div className="player-name-group-heading">
        <h3>{title}</h3>
        <span>{names.length}명</span>
      </div>
      <div className="player-name-grid">
        {names.map((playerName, index) => (
          <label
            className={`player-name-field ${
              gender === 'MALE' ? 'male' : 'female'
            }`}
            key={`${prefix}-${index + 1}`}
          >
            <span>{`${prefix}${index + 1}`}</span>
            <input
              type="text"
              maxLength={PLAYER_NAME_MAX_LENGTH}
              value={playerName}
              onChange={(event) => onChange(gender, index, event.target.value)}
            />
          </label>
        ))}
      </div>
    </div>
  );
}

export default CompetitionPlayerNameEditor;
