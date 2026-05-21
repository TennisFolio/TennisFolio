import { motion } from 'framer-motion';
import { COMPETITION_FIELD_LIMITS } from '../../hooks/useCompetitionCreateForm';

const MotionButton = motion.button;
const MotionDiv = motion.div;

function CompetitionFieldStepper({ field, value, onChange, onStep }) {
  const limit = COMPETITION_FIELD_LIMITS[field.name];
  const isMin = value <= limit.min;
  const isMax = value >= limit.max;

  const handleStep = (amount) => {
    onStep(field.name, amount);
  };

  return (
    <MotionDiv
      className="competition-field"
      variants={{
        hidden: { opacity: 0, y: 12 },
        show: {
          opacity: 1,
          y: 0,
          transition: { duration: 0.22, ease: [0.22, 1, 0.36, 1] },
        },
      }}
    >
      <div className="field-top">
        <div>
          <label htmlFor={field.name}>{field.label}</label>
          {field.unit && <span>{field.unit} 단위로 입력해요</span>}
        </div>
      </div>

      <div className="stepper-control">
        <MotionButton
          type="button"
          onClick={() => handleStep(-1)}
          disabled={isMin}
          aria-label={`${field.label} 줄이기`}
          whileTap={isMin ? undefined : { scale: 0.92 }}
          transition={{ duration: 0.16 }}
        >
          <span aria-hidden="true">-</span>
        </MotionButton>
        <MotionDiv
          className="stepper-value"
          key={value}
          initial={{ scale: 1, backgroundColor: '#ffffff' }}
          animate={{
            scale: [1, 1.1, 1],
            backgroundColor: ['#ffffff', '#ecfdf5', '#ffffff'],
          }}
          transition={{ duration: 0.24, ease: 'easeOut' }}
        >
          <input
            id={field.name}
            name={field.name}
            type="number"
            min={limit.min}
            max={limit.max}
            value={value}
            onChange={(event) => onChange(field.name, event.target.value)}
          />
        </MotionDiv>
        <MotionButton
          type="button"
          className="plus"
          onClick={() => handleStep(1)}
          disabled={isMax}
          aria-label={`${field.label} 늘리기`}
          whileTap={isMax ? undefined : { scale: 0.92 }}
          transition={{ duration: 0.16 }}
        >
          <span aria-hidden="true">+</span>
        </MotionButton>
      </div>
    </MotionDiv>
  );
}

export default CompetitionFieldStepper;
