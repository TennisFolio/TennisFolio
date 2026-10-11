function ClubAccountSelfForm({ account, isLeaving, onLeave }) {
  return <section className="club-dashboard-card club-account-detail-card"><header><span className={`club-account-role ${account.role.toLowerCase()}`}>{account.role}</span><h1>클럽 계정</h1><p>Account 이름으로 클럽에 표시되며, 이름은 프로필에서 변경할 수 있습니다.</p></header><div className="club-account-profile"><span className="club-account-avatar">{account.displayName?.slice(0, 1) ?? '?'}</span><strong>{account.displayName}</strong></div><div className="club-account-form"><label className="club-field"><span>계정 이름</span><div className="club-account-readonly">{account.displayName}</div></label><label className="club-field"><span>역할</span><div className="club-account-readonly">{account.role}</div></label></div><section className="club-account-danger-zone"><button className="club-button danger" type="button" disabled={isLeaving} onClick={onLeave}>{isLeaving ? '처리 중...' : '클럽 탈퇴'}</button></section></section>;
}

export default ClubAccountSelfForm;
