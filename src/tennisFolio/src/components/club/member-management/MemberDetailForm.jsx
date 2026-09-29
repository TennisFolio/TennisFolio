function MemberDetailForm({ form, skillTiers, isNew, isSaving, onChange, onSubmit, onBack }) {
  const statusDate = form.memberStatus === 'SUSPENDED' ? form.suspendedAt : form.leftAt;
  const setField = (field, value) => onChange({ ...form, [field]: value });

  return (
    <form className="club-dashboard-card club-member-detail-form" onSubmit={onSubmit}>
      <header className="club-dashboard-section-header"><h2>{isNew ? '회원 추가' : '회원 상세'}</h2><p>기본 정보와 회원 상태를 관리합니다.</p></header>
      <div className="club-member-detail-grid">
        <label className="club-field"><span>이름</span><input required value={form.name} onChange={(event) => setField('name', event.target.value)} /></label>
        <label className="club-field"><span>성별</span><select value={form.gender} onChange={(event) => setField('gender', event.target.value)}><option value="MALE">남성</option><option value="FEMALE">여성</option></select></label>
        <label className="club-field"><span>출생연도</span><input type="number" min="1900" max="2100" value={form.birthYear} onChange={(event) => setField('birthYear', event.target.value)} /></label>
        <label className="club-field"><span>가입일</span><input type="date" value={form.joinedAt} onChange={(event) => setField('joinedAt', event.target.value)} /></label>
        <label className="club-field"><span>티어</span><select value={form.skillTierId ?? ''} onChange={(event) => setField('skillTierId', event.target.value || null)}><option value="">미정</option>{skillTiers.map((tier) => <option key={tier.id} value={tier.id}>{tier.name}</option>)}</select></label>
        <label className="club-field"><span>역할</span><select value={form.role} onChange={(event) => setField('role', event.target.value)}><option value="MEMBER">회원</option><option value="ADMIN">관리자</option></select></label>
        {!isNew && <><label className="club-field"><span>상태</span><select value={form.memberStatus} onChange={(event) => setField('memberStatus', event.target.value)}><option value="ACTIVE">활동</option><option value="SUSPENDED">휴면</option><option value="WITHDRAWN">탈퇴</option></select></label>
          {form.memberStatus !== 'ACTIVE' && <label className="club-field"><span>상태 변경일</span><input type="date" value={statusDate ?? ''} onChange={(event) => setField(form.memberStatus === 'SUSPENDED' ? 'suspendedAt' : 'leftAt', event.target.value)} /></label>}</>}
      </div>
      <label className="club-field"><span>연락처 메모</span><input value={form.contactMemo} onChange={(event) => setField('contactMemo', event.target.value)} /></label>
      <label className="club-field"><span>메모</span><textarea value={form.memo} onChange={(event) => setField('memo', event.target.value)} /></label>
      <div className="club-member-detail-actions"><button className="club-button" type="button" onClick={onBack}>목록으로</button><button className="club-button primary" type="submit" disabled={isSaving}>{isSaving ? '저장 중...' : '저장'}</button></div>
    </form>
  );
}

export default MemberDetailForm;
