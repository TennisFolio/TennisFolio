function formatMeetingDate(startAt) {
  if (!startAt) return '-';

  const [date] = startAt.split('T');
  const [, month, day] = date.split('-');
  return month && day ? `${month}.${day}` : '-';
}

function statusLabel(status) {
  return { OPEN: '열림', CLOSED: '마감', CANCELLED: '취소' }[status] ?? status;
}

function MonthlyMeetingsPanel({ meetings, year, month }) {
  return (
    <article className="club-dashboard-card club-dashboard-meeting-panel">
      <header className="club-dashboard-section-header">
        <div>
          <h2>이달 모임</h2>
          <p>{year}년 {month}월에 등록된 모임입니다.</p>
        </div>
      </header>
      {meetings.length === 0 ? (
        <p className="club-dashboard-empty">이달에 등록된 모임이 없습니다.</p>
      ) : (
        <div className="club-dashboard-table-wrap">
          <table className="club-dashboard-table club-dashboard-meeting-table">
            <thead>
              <tr><th>날짜</th><th>모임</th><th>참석</th><th>상태</th></tr>
            </thead>
            <tbody>
              {meetings.map((meeting) => (
                <tr key={meeting.publicId}>
                  <td>{formatMeetingDate(meeting.startAt)}</td>
                  <td>{meeting.title}</td>
                  <td>회원 {meeting.memberAttendanceCount} · 게스트 {meeting.guestAttendanceCount}</td>
                  <td><span className={`club-dashboard-status ${meeting.status === 'CANCELLED' ? 'cancelled' : ''}`}>{statusLabel(meeting.status)}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </article>
  );
}

export default MonthlyMeetingsPanel;
