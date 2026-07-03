function MeetingAttendanceControlPanel({ status, onChangeStatus }) {
  const isClosed = status === 'CLOSED';

  return (
    <div className={`meeting-operation-status ${isClosed ? 'closed' : ''}`}>
      <div className="meeting-attendance-control-head">
        <div>
          <div className="meeting-attendance-control-title">
            <span
              className={`meeting-attendance-control-dot ${
                isClosed ? 'closed' : ''
              }`}
            />
            참석 체크
          </div>
          <p>
            {status === 'OPEN'
              ? '참가자가 응답할 수 있습니다. 마감하면 새 응답을 잠시 막습니다.'
              : '참석 체크가 마감되어 새 응답은 받지 않습니다. 필요하면 다시 열 수 있습니다.'}
          </p>
        </div>
        <span
          className={`meeting-attendance-status-pill ${
            status === 'OPEN' ? 'ok' : 'danger'
          }`}
        >
          {status === 'OPEN' ? '열림' : '마감'}
        </span>
      </div>
      <div className="meeting-attendance-control-actions">
        {status === 'OPEN' ? (
          <>
            <button
              type="button"
              className="meeting-button primary"
              aria-pressed="true"
            >
              열어두기
            </button>
            <button
              type="button"
              className="meeting-button"
              onClick={() => onChangeStatus('CLOSED')}
            >
              마감하기
            </button>
          </>
        ) : (
          <>
            <button
              type="button"
              className="meeting-button"
              onClick={() => onChangeStatus('OPEN')}
            >
              다시 열기
            </button>
            <button
              type="button"
              className="meeting-button danger"
              aria-pressed="true"
            >
              마감 유지
            </button>
          </>
        )}
      </div>
    </div>
  );
}

export default MeetingAttendanceControlPanel;
