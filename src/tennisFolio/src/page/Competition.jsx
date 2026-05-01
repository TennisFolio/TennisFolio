import { motion } from 'framer-motion';
import CompetitionCreateForm from '../components/competition/CompetitionCreateForm';
import './Competition.css';

const itemTransition = { duration: 0.28, ease: [0.22, 1, 0.36, 1] };
const MotionHeader = motion.header;
const MotionSection = motion.section;

function Competition() {
  return (
    <main className="competition-page">
      <MotionHeader
        className="competition-hero"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={itemTransition}
      >
        <div className="competition-hero-content">
          <span className="competition-hero-label">빠르게 시작해요</span>
          <h1>테니스 경기 만들기</h1>
          <p>참가자와 코트만 입력하면 바로 경기 일정을 만들어 드릴게요.</p>
        </div>
      </MotionHeader>

      <MotionSection
        className="competition-panel"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ ...itemTransition, delay: 0.08 }}
      >
        <CompetitionCreateForm />
      </MotionSection>
    </main>
  );
}

export default Competition;
