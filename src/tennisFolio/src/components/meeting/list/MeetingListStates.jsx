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
        아직 등록된 모임이 없습니다.
      </section>
    );
  }

  return null;
}

export default MeetingListStates;
