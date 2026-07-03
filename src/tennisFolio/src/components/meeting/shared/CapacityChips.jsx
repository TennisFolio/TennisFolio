import { getCapacityChips } from './meetingAttendanceUtils';

function CapacityChips({ meeting, attendances }) {
  return (
    <div className="meeting-capacity-row" aria-label="정원">
      {getCapacityChips(meeting, attendances).map((label) => (
        <span className="meeting-chip" key={label}>
          {label}
        </span>
      ))}
    </div>
  );
}

export default CapacityChips;
