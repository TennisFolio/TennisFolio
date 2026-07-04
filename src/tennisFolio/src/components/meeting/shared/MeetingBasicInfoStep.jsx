function MeetingBasicInfoStep({ form, onFieldChange, onPrevious, onNext }) {
  return (
    <section className="meeting-panel" aria-label="모임 기본 정보">
      <h2>1 / 2 기본 정보</h2>
      <label className="meeting-field">
        <span>모임 이름</span>
        <input
          value={form.title}
          onChange={(event) => onFieldChange('title', event.target.value)}
        />
      </label>
      <label className="meeting-field">
        <span>모임 날짜</span>
        <input
          type="date"
          value={form.date}
          onChange={(event) => onFieldChange('date', event.target.value)}
        />
      </label>
      <div className="meeting-grid two">
        <label className="meeting-field">
          <span>시작 시간</span>
          <input
            type="time"
            value={form.startTime}
            onChange={(event) => onFieldChange('startTime', event.target.value)}
          />
        </label>
        <label className="meeting-field">
          <span>종료 시간</span>
          <input
            type="time"
            value={form.endTime}
            onChange={(event) => onFieldChange('endTime', event.target.value)}
          />
        </label>
      </div>
      <label className="meeting-field">
        <span>안내사항</span>
        <textarea
          value={form.note}
          placeholder="장소, 준비물, 진행 방식 등 참가자에게 알려줄 내용을 적어주세요."
          onChange={(event) => onFieldChange('note', event.target.value)}
        />
      </label>
      <div className="meeting-action-row meeting-form-action-row">
        <button type="button" className="meeting-button" onClick={onPrevious}>
          이전
        </button>
        <button type="button" className="meeting-button primary" onClick={onNext}>
          다음
        </button>
      </div>
    </section>
  );
}

export default MeetingBasicInfoStep;
