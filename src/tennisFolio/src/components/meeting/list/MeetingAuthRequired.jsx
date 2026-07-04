function MeetingAuthRequired({ onLogin }) {
  return (
    <main className="meeting-page">
      <section className="meeting-header">
        <div className="meeting-title-block">
          <div className="meeting-title-row">
            <h1>모임 관리</h1>
          </div>
          <p>
            로그인하면 만든 모임을 확인하고 참석 링크와 경기표를 관리할 수
            있습니다.
          </p>
        </div>
        <button
          type="button"
          className="meeting-button primary"
          onClick={onLogin}
        >
          로그인하기
        </button>
      </section>
    </main>
  );
}

export default MeetingAuthRequired;
