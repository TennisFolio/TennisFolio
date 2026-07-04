function MeetingConfirmModal({
  title,
  description,
  confirmLabel,
  onCancel,
  onConfirm,
}) {
  return (
    <div className="meeting-confirm-backdrop">
      <div className="meeting-confirm-panel" role="alertdialog" aria-modal="true">
        <strong>{title}</strong>
        <p>{description}</p>
        <div className="meeting-confirm-actions">
          <button
            type="button"
            className="meeting-button"
            onClick={onCancel}
          >
            취소
          </button>
          <button
            type="button"
            className="meeting-button danger"
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export default MeetingConfirmModal;
