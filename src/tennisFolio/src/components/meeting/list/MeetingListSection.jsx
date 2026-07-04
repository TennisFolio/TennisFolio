import MeetingListCard from './MeetingListCard';

function MeetingListSection({
  meetings,
  hasMoreMeetings,
  onDelete,
  onLoadMore,
  onManage,
  onShare,
}) {
  return (
    <section className="meeting-list" aria-label="모임 관리 목록">
      {meetings.map((meeting) => (
        <MeetingListCard
          key={meeting.publicId}
          meeting={meeting}
          onDelete={onDelete}
          onManage={onManage}
          onShare={onShare}
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
