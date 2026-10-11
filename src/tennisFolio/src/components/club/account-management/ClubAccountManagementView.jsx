function ClubAccountManagementView({ accounts, invitation, invitationLink, feedback, isIssuing, onIssue, onCopy, onClose, onOpenAccount }) {
  const owners = accounts.filter((account) => account.role === 'OWNER').length;
  const admins = accounts.filter((account) => account.role === 'ADMIN').length;

  return <>
    <header className="club-dashboard-heading"><div><span className="club-dashboard-eyebrow">CLUB ACCOUNTS</span><h1>클럽 계정 관리</h1><p>운영 권한을 가진 계정을 관리합니다. 클럽 명단 회원은 표시되지 않습니다.</p></div><span className="club-account-count">운영 계정 {accounts.length}명</span></header>
    <section className="club-account-section">
      <header className="club-dashboard-section-header"><h2>ADMIN 초대</h2><p>링크는 여러 사람이 사용할 수 있습니다.</p></header>
      {invitation?.active ? <article className="club-dashboard-card club-invitation-card"><div><h3>활성 초대 링크</h3><p>운영진에게 공유하면 로그인한 사용자가 ADMIN으로 클럽에 참여할 수 있습니다.</p><strong>만료: {new Date(invitation.expiresAt).toLocaleString('ko-KR')}</strong></div><div className="club-account-actions"><button className="club-button primary" type="button" onClick={() => onCopy(invitationLink)}>링크 복사</button><button className="club-button" type="button" disabled={isIssuing} onClick={onIssue}>새 링크 발급</button><button className="club-button danger" type="button" onClick={onClose}>초대 종료</button></div></article> : <article className="club-dashboard-card club-invitation-empty"><div><h3>활성 ADMIN 초대 링크가 없습니다.</h3><p>새 링크를 생성해 운영진에게 공유하세요. 링크는 3시간 동안 여러 Account가 사용할 수 있습니다.</p></div><button className="club-button primary" type="button" disabled={isIssuing} onClick={onIssue}>{isIssuing ? '생성 중...' : 'ADMIN 초대 링크 생성'}</button></article>}
      {feedback && <p className="club-notice static">{feedback}</p>}
      <p className="club-account-note">새 링크를 발급하거나 초대를 종료하면 기존 링크는 즉시 사용할 수 없습니다.</p>
    </section>
    <section className="club-account-section"><header className="club-dashboard-section-header"><h2>클럽 계정</h2><p>OWNER {owners}명 · ADMIN {admins}명</p></header><div className="club-dashboard-card club-account-list">{accounts.map((account) => <article key={account.id} className="club-account-row"><div className="club-account-person"><span className="club-account-avatar">{account.displayName?.slice(0, 1) ?? '?'}</span><strong>{account.displayName}</strong></div><span className={`club-account-role ${account.role.toLowerCase()}`}>{account.role}</span><button className="club-button small" type="button" onClick={() => onOpenAccount(account.id)}>수정</button></article>)}</div></section>
  </>;
}

export default ClubAccountManagementView;
