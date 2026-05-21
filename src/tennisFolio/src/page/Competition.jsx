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
          <p>
            참가 인원과 코트 수를 입력하면 첫 경기부터 바로 만들고 운영할 수
            있어요.
          </p>
          <div className="competition-hero-points" aria-label="대진표 생성 특징">
            <span>복식 전용</span>
            <span>코트별 운영</span>
            <span>링크 공유</span>
          </div>
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
