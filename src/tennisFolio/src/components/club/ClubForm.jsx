function ClubForm({
  form,
  submitLabel,
  isSaving,
  onSubmit,
  onChange,
  onBack,
  onDelete,
}) {
  return (
    <form className="club-stack" onSubmit={onSubmit}>
      <label className="club-field">
        <span>클럽명</span>
        <input
          value={form.name}
          onChange={(event) => onChange({ ...form, name: event.target.value })}
        />
      </label>
      <label className="club-field">
        <span>소개</span>
        <textarea
          value={form.description}
          onChange={(event) =>
            onChange({ ...form, description: event.target.value })
          }
        />
      </label>
      <button className="club-button primary full" type="submit" disabled={isSaving}>
        {submitLabel}
      </button>
      <button className="club-button full" type="button" onClick={onBack}>
        목록으로
      </button>
      {onDelete && (
        <div className="club-danger-zone">
          <button
            className="club-button danger full"
            type="button"
            onClick={onDelete}
            disabled={isSaving}
          >
            클럽 삭제
          </button>
        </div>
      )}
    </form>
  );
}

export default ClubForm;
