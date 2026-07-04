import { statusLabels } from './meetingAttendanceUtils';

function AttendanceStatusOptions({
  selectedStatus,
  onSelect,
  buttonClassName = 'meeting-button status-option',
}) {
  return (
    <div className="meeting-status-options">
      {Object.entries(statusLabels).map(([status, label]) => (
        <button
          type="button"
          className={`${buttonClassName} ${
            selectedStatus === status ? 'primary' : ''
          }`}
          key={status}
          onClick={() => onSelect(status)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}

export default AttendanceStatusOptions;
