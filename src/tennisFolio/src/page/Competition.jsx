import { motion } from 'framer-motion';
import { useEffect } from 'react';
import CompetitionCreateForm from '../components/competition/CompetitionCreateForm';
import './Competition.css';
import { trackEvent } from '../utils/analytics';

const itemTransition = { duration: 0.28, ease: [0.22, 1, 0.36, 1] };
const MotionHeader = motion.header;
const MotionSection = motion.section;

function Competition() {
  useEffect(() => {
    trackEvent('competition_create_funnel_step', {
      funnel_step: 'create_form_view',
    });
  }, []);

  return (
    <main className="competition-page">
      <MotionHeader
        className="competition-hero"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={itemTransition}
      >
        <div className="competition-hero-content">
          <h1>복식 대진표 만들기</h1>
          <p>참가 인원, 코트 수, 진행 시간을 정하면 대진표를 자동으로 만들어요.</p>
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
