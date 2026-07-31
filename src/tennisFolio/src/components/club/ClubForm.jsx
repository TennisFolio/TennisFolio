function ClubForm({
  form,
  submitLabel,
  isSaving,
  onSubmit,
  onChange,
  onBack,
  onDelete,
}) {
  const skillTiers = form.skillTiers ?? [];

  const updateSkillTier = (index, name) => {
    onChange({
      ...form,
      skillTiers: skillTiers.map((skillTier, tierIndex) =>
        tierIndex === index ? { ...skillTier, name } : skillTier,
      ),
    });
  };

  const addSkillTier = () => {
    if (skillTiers.length >= 10) {
      return;
    }
    onChange({ ...form, skillTiers: [...skillTiers, { id: null, name: '' }] });
  };

  const moveSkillTier = (index, direction) => {
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= skillTiers.length) {
      return;
    }
    const nextSkillTiers = [...skillTiers];
    [nextSkillTiers[index], nextSkillTiers[targetIndex]] = [
      nextSkillTiers[targetIndex],
      nextSkillTiers[index],
    ];
    onChange({ ...form, skillTiers: nextSkillTiers });
  };

  const removeSkillTier = (index) => {
    onChange({
      ...form,
      skillTiers: skillTiers.filter((_, tierIndex) => tierIndex !== index),
    });
  };

  return (
    <form className="club-stack" onSubmit={onSubmit}>
      <label className="club-field">
        <span>클럽명</span>
        <input
          value={form.name}
          onChange={(event) => onChange({ ...form, name: event.target.value })}
        />
      </label>
      <section className="club-skill-tier-editor" aria-labelledby="club-skill-tier-title">
        <div className="club-section-title">
          <div>
            <h2 id="club-skill-tier-title">클럽 등급</h2>
            <p className="club-field-help">위에 있을수록 높은 등급입니다.</p>
          </div>
          <button
            className="club-button small accent"
            type="button"
            onClick={addSkillTier}
            disabled={skillTiers.length >= 10}
          >
            추가
          </button>
        </div>
        {skillTiers.length === 0 && (
          <p className="club-field-help">등록된 등급이 없습니다.</p>
        )}
        <div className="club-skill-tier-list">
          {skillTiers.map((skillTier, index) => (
            <div className="club-skill-tier-row" key={skillTier.id ?? `new-${index}`}>
              <input
                aria-label={`등급 ${index + 1} 이름`}
                value={skillTier.name}
                maxLength={10}
                placeholder="등급 이름"
                onChange={(event) => updateSkillTier(index, event.target.value)}
              />
              <div className="club-skill-tier-actions">
                <button
                  className="club-button small"
                  type="button"
                  onClick={() => moveSkillTier(index, -1)}
                  disabled={index === 0}
                  aria-label="등급 위로 이동"
                >
                  ↑
                </button>
                <button
                  className="club-button small"
                  type="button"
                  onClick={() => moveSkillTier(index, 1)}
                  disabled={index === skillTiers.length - 1}
                  aria-label="등급 아래로 이동"
                >
                  ↓
                </button>
                <button
                  className="club-button small danger"
                  type="button"
                  onClick={() => removeSkillTier(index)}
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>
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
