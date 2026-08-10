function ClubMemberBulkAddView({
  rows,
  skillTiers,
  validationRows,
  isSaving,
  onChangeRow,
  onAddRow,
  onRemoveRow,
  onPaste,
  onSubmit,
  onBack,
}) {
  const hasErrors = validationRows.some((row) => row.errors.length > 0);
  const memberCount = validationRows.filter((row) => row.name).length;

  return (
    <>
      <header className="club-page-head club-bulk-page-head">
        <span className="club-eyebrow">CLUB MEMBER</span>
        <h1>클럽원 일괄 추가</h1>
        <p>엑셀 데이터를 표에 붙여넣고 등록 전에 오류를 확인합니다.</p>
      </header>

      <section className="club-bulk-card">
        <h2>클럽원 정보 입력</h2>
        <div className="club-bulk-guide">
          <strong>입력 안내</strong>
          <ul>
            <li>이름과 성별은 필수입니다.</li>
            <li>성별은 남/여, 역할은 관리자일 때만 관리자로 등록됩니다.</li>
            <li>표를 선택한 뒤 엑셀 데이터를 붙여넣을 수 있습니다.</li>
          </ul>
        </div>

        <div className="club-bulk-grid-wrap" onPaste={onPaste}>
          <table className="club-bulk-grid">
            <thead>
              <tr>
                <th>번호</th><th>이름 *</th><th>성별(남/여) *</th><th>역할(관리자/클럽원)</th>
                <th>실력 등급</th><th>비고</th><th>연락 메모</th><th aria-label="행 삭제" />
              </tr>
            </thead>
            <tbody>
              {rows.map((row, index) => (
                <tr key={row.rowId}>
                  <td className="club-bulk-number">{index + 1}</td>
                  <td><input value={row.name} placeholder="이름" onChange={(event) => onChangeRow(row.rowId, { name: event.target.value })} /></td>
                  <td>
                    <select value={row.gender} onChange={(event) => onChangeRow(row.rowId, { gender: event.target.value, invalidGender: false })}>
                      <option value="MALE">남</option><option value="FEMALE">여</option>
                    </select>
                  </td>
                  <td>
                    <select value={row.role} onChange={(event) => onChangeRow(row.rowId, { role: event.target.value })}>
                      <option value="MEMBER">클럽원</option><option value="ADMIN">관리자</option>
                    </select>
                  </td>
                  <td>
                    <select value={row.skillTierId ?? ''} onChange={(event) => onChangeRow(row.rowId, { skillTierId: event.target.value || null, invalidSkillTier: false })}>
                      <option value="">미정</option>
                      {skillTiers.map((tier) => <option key={tier.id} value={tier.id}>{tier.name}</option>)}
                    </select>
                  </td>
                  <td><input value={row.memo} onChange={(event) => onChangeRow(row.rowId, { memo: event.target.value })} /></td>
                  <td><input value={row.contactMemo} onChange={(event) => onChangeRow(row.rowId, { contactMemo: event.target.value })} /></td>
                  <td><button className="club-bulk-remove" type="button" onClick={() => onRemoveRow(row.rowId)}>삭제</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="club-bulk-actions">
          <div><button className="club-button" type="button" onClick={onAddRow}>+ 행 추가</button></div>
        </div>
        <p className="club-field-help">붙여넣기 순서: 이름, 성별(남/여), 역할(관리자/클럽원), 등급명, 비고, 연락 메모</p>
      </section>

      <section className="club-bulk-card">
        <div className="club-bulk-preview-head">
          <h2>내용 확인 결과 <span>{memberCount}명</span></h2>
          <p>{hasErrors ? '오류를 수정하면 전체 등록할 수 있습니다.' : '등록할 클럽원 정보를 확인하세요.'}</p>
        </div>
        <div className="club-bulk-grid-wrap">
          <table className="club-bulk-preview">
            <thead><tr><th>번호</th><th>이름</th><th>성별</th><th>역할</th><th>등급</th><th>확인</th></tr></thead>
            <tbody>
              {validationRows.filter((row) => row.name || row.errors.length).map((row, index) => (
                <tr className={row.errors.length ? 'club-bulk-error-row' : ''} key={row.rowId}>
                  <td>{index + 1}</td><td>{row.name || '-'}</td><td>{row.gender === 'FEMALE' ? '여' : '남'}</td>
                  <td>{row.role === 'ADMIN' ? '관리자' : '클럽원'}</td>
                  <td>{skillTiers.find((tier) => String(tier.id) === String(row.skillTierId))?.name ?? '미정'}</td>
                  <td>{row.errors.length ? row.errors.join(', ') : '등록 가능'}</td>
                </tr>
              ))}
              {!memberCount && <tr><td className="club-bulk-empty" colSpan="6">입력한 클럽원이 없습니다.</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="club-bulk-footer">
          <button className="club-button" type="button" onClick={onBack}>취소</button>
          <button className="club-button primary" type="button" disabled={!memberCount || hasErrors || isSaving} onClick={onSubmit}>
            {isSaving ? '등록 중...' : `${memberCount}명 클럽원 등록`}
          </button>
        </div>
      </section>
    </>
  );
}

export default ClubMemberBulkAddView;
