function DashboardKpiCards({ dashboard }) {
  const participation = dashboard.memberParticipation ?? {};
  const ratio = dashboard.memberGuestRatio ?? {};

  return (
    <section className="club-dashboard-kpis" aria-label="선택한 달 핵심 지표">
      <article className="club-dashboard-card">
        <span className="club-dashboard-label">회원 참여율</span>
        <strong className="club-dashboard-number">{participation.rate ?? 0}%</strong>
        <p>참여 회원 {participation.participantCount ?? 0}명 / 활성 회원 {dashboard.activeMemberCount ?? 0}명</p>
      </article>
      <article className="club-dashboard-card">
        <span className="club-dashboard-label">이달 모임 수</span>
        <strong className="club-dashboard-number">{dashboard.meetingCount}회</strong>
        <p>취소된 모임은 제외</p>
      </article>
      <article className="club-dashboard-card">
        <span className="club-dashboard-label">회원 / 게스트 비율</span>
        <strong className="club-dashboard-number">{ratio.memberRate ?? 0}% / {ratio.guestRate ?? 0}%</strong>
        <div className="club-dashboard-ratio" aria-label={`회원 ${ratio.memberRate ?? 0}%, 게스트 ${ratio.guestRate ?? 0}%`}>
          <span className="club-dashboard-ratio-member" style={{ width: `${ratio.memberRate ?? 0}%` }} />
          <span className="club-dashboard-ratio-guest" style={{ width: `${ratio.guestRate ?? 0}%` }} />
        </div>
        <p>실제 참석 인원 기준</p>
      </article>
    </section>
  );
}

export default DashboardKpiCards;
