function ClubState({
  eyebrow,
  title,
  description,
  actionLabel,
  onAction,
  children,
}) {
  return (
    <section className="club-state">
      {eyebrow && <span className="club-eyebrow">{eyebrow}</span>}
      <h1>{title}</h1>
      {description && <p>{description}</p>}
      {children}
      {actionLabel && (
        <button className="club-button full" type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </section>
  );
}

export default ClubState;
