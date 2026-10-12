function MeetingListHeader({ count, showCount, onCreate }) {
  return (
    <header className="meeting-header">
      <div className="meeting-header-row">
        <div className="meeting-title-block">
          <div className="meeting-title-row">
            <h1>모임</h1>
            {showCount && <span>{count}개</span>}
          </div>
          <p>모든 모임의 일정과 참가 현황을 확인할 수 있습니다.</p>
        </div>
        <button
          type="button"
          className="meeting-button primary"
          onClick={onCreate}
        >
          모임 만들기
        </button>
      </div>
    </header>
  );
}

export default MeetingListHeader;
