function ClubInvitationAcceptView({ invitation, isSubmitting, onSubmit }) {
  return <main className="club-invitation-page"><section className="club-dashboard-card club-invitation-accept-card"><div className="club-invitation-mark" aria-hidden="true">TF</div><h1>클럽 ADMIN으로 참여</h1><p>초대 링크가 유효합니다. Account 이름으로 클럽에 표시됩니다.</p><div className="club-invitation-summary"><strong>{invitation.clubName}</strong><span>ADMIN 권한으로 참여합니다.</span></div><form className="club-account-form" onSubmit={onSubmit}><button className="club-button primary full" type="submit" disabled={isSubmitting}>{isSubmitting ? '참여 중...' : 'ADMIN으로 참여하기'}</button></form><p className="club-account-note">참여 후 회원·모임·대진표를 운영할 수 있습니다.</p></section></main>;
}

export default ClubInvitationAcceptView;
