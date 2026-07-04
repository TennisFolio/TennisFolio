function MeetingListHeader({ count, showCount, onCreate }) {
  return (
    <header className="meeting-header">
      <div className="meeting-header-row">
        <div className="meeting-title-block">
          <div className="meeting-title-row">
            <h1>모임 관리</h1>
            {showCount && <span>{count}개</span>}
          </div>
          <p>참석 체크 링크를 만들고 경기표 생성까지 이어갑니다.</p>
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
