import { useEffect } from 'react';

function MeetingToast({ notice, onClose }) {
  useEffect(() => {
    if (!notice?.message) {
      return undefined;
    }

    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [notice, onClose]);

  if (!notice?.message) {
    return null;
  }

  return (
    <div
      className={`meeting-toast ${notice.type === 'error' ? 'error' : 'success'}`}
      role="status"
      aria-live="polite"
    >
      <span>{notice.message}</span>
      <button type="button" onClick={onClose} aria-label="알림 닫기">
        x
      </button>
    </div>
  );
}

export default MeetingToast;
