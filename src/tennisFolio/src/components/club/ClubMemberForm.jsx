function ClubMemberForm({
  form,
  isEdit,
  isSaving,
  showDelete,
  onSubmit,
  onChange,
  onBack,
  onDelete,
}) {
  return (
    <form className="club-stack club-member-form" onSubmit={onSubmit}>
      <label className="club-field">
        <span>이름</span>
        <input
          value={form.name}
          onChange={(event) => onChange({ ...form, name: event.target.value })}
        />
      </label>
      <div className="club-form-grid">
        <label className="club-field">
          <span>성별</span>
          <select
            value={form.gender}
            onChange={(event) => onChange({ ...form, gender: event.target.value })}
          >
            <option value="MALE">남성</option>
            <option value="FEMALE">여성</option>
          </select>
        </label>
        <label className="club-field">
          <span>역할</span>
          <select
            value={form.role}
            onChange={(event) => onChange({ ...form, role: event.target.value })}
          >
            <option value="MEMBER">클럽원</option>
            <option value="ADMIN">관리자</option>
          </select>
        </label>
      </div>
      <label className="club-field">
        <span>실력 메모</span>
        <input
          value={form.skillNote}
          onChange={(event) => onChange({ ...form, skillNote: event.target.value })}
        />
      </label>
      <label className="club-field">
        <span>연락 메모</span>
        <input
          value={form.contactMemo}
          onChange={(event) =>
            onChange({ ...form, contactMemo: event.target.value })
          }
        />
      </label>
      <label className="club-field">
        <span>비고</span>
        <input
          value={form.memo}
          onChange={(event) => onChange({ ...form, memo: event.target.value })}
        />
      </label>
      <button className="club-button primary full" type="submit" disabled={isSaving}>
        {isEdit ? '클럽원 수정' : '클럽원 추가'}
      </button>
      <button className="club-button full" type="button" onClick={onBack}>
        목록으로
      </button>
      {showDelete && (
        <div className="club-danger-zone">
          <button
            className="club-button danger full"
            type="button"
            onClick={onDelete}
            disabled={isSaving}
          >
            클럽원 삭제
          </button>
        </div>
      )}
    </form>
  );
}

export default ClubMemberForm;
