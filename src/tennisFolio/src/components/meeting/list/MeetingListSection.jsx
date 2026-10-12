import MeetingListCard from './MeetingListCard';

function MeetingListSection({
  meetings,
  hasMoreMeetings,
  onLoadMore,
  onOpen,
}) {
  return (
    <section className="meeting-list" aria-label="모임 목록">
      {meetings.map((meeting) => (
        <MeetingListCard
          key={meeting.publicId}
          meeting={meeting}
          onOpen={onOpen}
        />
      ))}
      {hasMoreMeetings && (
        <button
          type="button"
          className="meeting-load-more-button"
          onClick={onLoadMore}
        >
          더 보기
        </button>
      )}
    </section>
  );
}

export default MeetingListSection;
