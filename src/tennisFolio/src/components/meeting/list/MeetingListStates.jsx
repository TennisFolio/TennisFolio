function MeetingListStates({ errorMessage, isEmpty, isLoading }) {
  if (isLoading) {
    return <p className="meeting-state">불러오는 중입니다.</p>;
  }

  if (errorMessage) {
    return <p className="meeting-state meeting-error">{errorMessage}</p>;
  }

  if (isEmpty) {
    return (
      <section className="meeting-state">
        아직 만든 모임이 없습니다. 첫 모임을 만들고 참석 링크를 공유해보세요.
      </section>
    );
  }

  return null;
}

export default MeetingListStates;
