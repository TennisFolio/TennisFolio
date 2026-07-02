import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const pageSource = readFileSync(new URL('./MeetingPublic.jsx', import.meta.url), 'utf8');
const pageCss = readFileSync(new URL('./Meeting.css', import.meta.url), 'utf8');

test('meeting public page follows mockup detail layout', () => {
  assert.match(pageSource, /meeting-card-title-row/);
  assert.match(pageSource, /meeting-card-meta/);
  assert.match(pageSource, /meeting-note-box/);
  assert.match(pageSource, /meeting-state-note/);
  assert.match(pageSource, /handleCopyShareLink/);
});

test('meeting public detail omits generic public check and my info subtitles', () => {
  assert.doesNotMatch(pageSource, /<p className="meeting-muted">공개 참석 체크<\/p>/);
  assert.doesNotMatch(pageSource, /<p className="meeting-muted">내 정보<\/p>/);
  assert.doesNotMatch(pageSource, /aria-label="내 정보"/);
});

test('meeting public detail links to generated competition when available', () => {
  assert.match(pageSource, /useNavigate/);
  assert.match(pageSource, /meeting\.competitionPublicId/);
  assert.match(pageSource, /navigate\(`\/competitions\/\$\{meeting\.competitionPublicId\}`\)/);
  assert.match(pageSource, /경기표 보기/);
});

test('meeting public page shows one auto-dismiss toast for transient feedback', () => {
  assert.match(pageSource, /MeetingToast/);
  assert.match(pageSource, /const \[notice, setNotice\]/);
  assert.match(pageSource, /showNotice\('success', '공유 링크를 복사했습니다\.'\)/);
  assert.match(pageSource, /showNotice\('error'/);
  assert.match(pageSource, /<MeetingToast notice=\{notice\} onClose=\{\(\) => setNotice\(null\)\} \/>/);
  assert.doesNotMatch(pageSource, /meeting-feedback-stack/);
});

test('meeting public page shows entry screen before a participant enters', () => {
  assert.match(pageSource, /hasEntered/);
  assert.match(pageSource, /renderEntryScreen/);
  assert.match(pageSource, /처음 입장/);
  assert.match(pageSource, /이미 응답했다면 이름을 선택해 모임에 입장하세요/);
  assert.doesNotMatch(pageSource, /이미 응답했다면 아래 이름을 눌러 다시 입장하세요/);
  assert.match(pageSource, /참석 여부를 선택하면 참석 현황과 명단을 볼 수 있습니다/);
  assert.doesNotMatch(pageSource, /같은 공개 URL/);
});

test('meeting public entry and detail screens show participant capacity limits', () => {
  assert.match(pageSource, /function getCapacityChips\(meeting, attendances\)/);
  assert.match(pageSource, /meeting\.maxParticipants/);
  assert.match(pageSource, /meeting\.maxMaleParticipants/);
  assert.match(pageSource, /meeting\.maxFemaleParticipants/);
  assert.match(pageSource, /정원 제한 없음/);
  assert.match(pageSource, /정원 \$\{attendingCount\}\/\$\{meeting\.maxParticipants\}/);
  assert.match(pageSource, /남성 \$\{maleCount\}\/\$\{meeting\.maxMaleParticipants\}/);
  assert.match(pageSource, /여성 \$\{femaleCount\}\/\$\{meeting\.maxFemaleParticipants\}/);
  assert.match(pageSource, /function CapacityChips/);

  const entryScreenStart = pageSource.indexOf('const renderEntryScreen');
  const detailScreenStart = pageSource.indexOf('if (!hasEntered)');
  const entryScreenSource = pageSource.slice(entryScreenStart, detailScreenStart);
  const detailScreenSource = pageSource.slice(detailScreenStart);

  assert.match(entryScreenSource, /<CapacityChips meeting=\{meeting\} attendances=\{attendances\} \/>/);
  assert.match(detailScreenSource, /<CapacityChips meeting=\{meeting\} attendances=\{attendances\} \/>/);
  assert.match(pageCss, /\.meeting-capacity-row\s*\{/);
});

test('meeting public page switches to detail after save or existing name selection', () => {
  assert.match(pageSource, /setHasEntered\(true\)/);
  assert.match(pageSource, /selectAttendance/);
  assert.match(pageSource, /saveAttendance/);
});

test('meeting public page keeps current attendance id after creating or entering detail', () => {
  assert.match(pageSource, /const savedAttendance = response\.data\.data/);
  assert.match(pageSource, /attendanceId: savedAttendance\?\.id \?\?/);
  assert.match(pageSource, /participantName: savedAttendance\?\.participantName \?\?/);
  assert.match(pageSource, /attendanceId: nextForm\.attendanceId \|\| null/);
});

test('meeting public page remembers selected attendance per public meeting', () => {
  assert.match(pageSource, /function getRememberedAttendanceKey\(publicId\)/);
  assert.match(pageSource, /`meetingPublic:\$\{publicId\}:attendance`/);
  assert.match(pageSource, /function rememberAttendance\(publicId, attendance\)/);
  assert.match(pageSource, /localStorage\.setItem/);
  assert.match(pageSource, /attendanceId/);
  assert.match(pageSource, /participantName/);
  assert.match(pageSource, /rememberAttendance\(publicId, attendance\)/);
  assert.match(pageSource, /rememberAttendance\(publicId, savedAttendance/);
});

test('meeting public page restores or clears remembered attendance after loading roster', () => {
  assert.match(pageSource, /function findRememberedAttendance\(publicId, attendances\)/);
  assert.match(pageSource, /localStorage\.getItem/);
  assert.match(pageSource, /JSON\.parse/);
  assert.match(pageSource, /attendance\.id === remembered\.attendanceId/);
  assert.match(pageSource, /forgetRememberedAttendance\(publicId\)/);
  assert.match(pageSource, /setHasEntered\(true\)/);
});

test('meeting public page lets visitor enter as a different participant', () => {
  assert.match(pageSource, /handleEnterAsDifferentParticipant/);
  assert.match(pageSource, /forgetRememberedAttendance\(publicId\)/);
  assert.match(pageSource, /setForm\(emptyAttendance\)/);
  assert.match(pageSource, /setHasEntered\(false\)/);
  assert.match(pageSource, /다른 이름으로 입장/);
});

test('meeting public page saves profile separately and status buttons update immediately', () => {
  assert.match(pageSource, /handleSaveProfile/);
  assert.match(pageSource, /handleStatusClick/);
  assert.match(pageSource, /statusLabels/);
  assert.match(pageSource, /onClick=\{\(\) => handleStatusClick\(status\)\}/);
});

test('meeting public page groups roster by attending gender and status', () => {
  assert.match(pageSource, /groupAttendances/);
  assert.match(pageSource, /attendingMale/);
  assert.match(pageSource, /attendingFemale/);
  assert.match(pageSource, /maybe/);
  assert.match(pageSource, /notAttending/);
  assert.match(pageSource, /RosterPanel/);
});

test('meeting public detail roster is read-only for non-owner visitors', () => {
  const rosterPanelStart = pageSource.indexOf('function RosterPanel');
  const meetingPublicStart = pageSource.indexOf('function MeetingPublic');
  const rosterPanelSource = pageSource.slice(rosterPanelStart, meetingPublicStart);

  assert.doesNotMatch(rosterPanelSource, /onSelect/);
  assert.doesNotMatch(rosterPanelSource, /<button/);
  assert.match(rosterPanelSource, /<span/);
});

test('meeting public styles support same compact roster panels as manage screen', () => {
  assert.match(pageCss, /\.meeting-roster-panel\s*\{/);
  assert.match(pageCss, /\.meeting-status-options\s*\{/);
  assert.match(pageCss, /\.meeting-page\s*\{[^}]*width:\s*min\(520px,\s*100%\);/s);
});
