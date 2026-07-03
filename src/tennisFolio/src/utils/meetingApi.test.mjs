import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const apiSource = readFileSync(new URL('./meetingApi.js', import.meta.url), 'utf8');
const appSource = readFileSync(new URL('../App.jsx', import.meta.url), 'utf8');

test('meeting API client uses the documented endpoints and methods', () => {
  assert.match(apiSource, /getMyMeetings\s*=\s*\(\)\s*=>\s*apiRequestSilent\.get\('\/api\/me\/meetings'\)/);
  assert.match(apiSource, /createMeeting\s*=\s*\(meeting\)\s*=>\s*apiRequestSilent\.post\('\/api\/meetings',\s*meeting\)/);
  assert.match(apiSource, /getPublicMeeting\s*=\s*\(publicId\)\s*=>\s*apiRequestSilent\.get\(`\/api\/meetings\/\$\{publicId\}`\)/);
  assert.match(apiSource, /getManagedMeeting\s*=\s*\(publicId\)\s*=>\s*apiRequestSilent\.get\(`\/api\/meetings\/\$\{publicId\}\/manage`\)/);
  assert.match(apiSource, /updateMeetingStatus\s*=\s*\(publicId,\s*status\)\s*=>\s*apiRequestSilent\.patch\(`\/api\/meetings\/\$\{publicId\}\/status`,\s*\{\s*status\s*\}\)/);
  assert.match(apiSource, /deleteMeeting\s*=\s*\(publicId\)\s*=>\s*apiRequestSilent\.delete\(`\/api\/meetings\/\$\{publicId\}`\)/);
  assert.match(apiSource, /upsertAttendance\s*=\s*\(publicId,\s*attendance\)\s*=>\s*apiRequestSilent\.post\(`\/api\/meetings\/\$\{publicId\}\/attendances`,\s*attendance\)/);
  assert.match(apiSource, /updateAttendance\s*=\s*\(publicId,\s*attendanceId,\s*attendance\)\s*=>[\s\S]*?apiRequestSilent\.patch\([\s\S]*?`\/api\/meetings\/\$\{publicId\}\/attendances\/\$\{attendanceId\}`,[\s\S]*?attendance,[\s\S]*?\)/);
  assert.match(apiSource, /deleteAttendance\s*=\s*\(publicId,\s*attendanceId\)\s*=>[\s\S]*?apiRequestSilent\.delete\([\s\S]*?`\/api\/meetings\/\$\{publicId\}\/attendances\/\$\{attendanceId\}`[\s\S]*?\)/);
  assert.match(apiSource, /createMeetingCompetition\s*=\s*\(publicId\)\s*=>\s*apiRequestSilent\.post\(`\/api\/meetings\/\$\{publicId\}\/competition`\)/);
  assert.match(apiSource, /deleteMeetingCompetition\s*=\s*\(publicId\)\s*=>\s*apiRequestSilent\.delete\(`\/api\/meetings\/\$\{publicId\}\/competition`\)/);
});

test('app registers meeting UI routes', () => {
  assert.match(appSource, /path="\/meetings"/);
  assert.match(appSource, /path="\/meetings\/new"/);
  assert.match(appSource, /path="\/meetings\/:publicId"/);
  assert.match(appSource, /path="\/meetings\/:publicId\/manage"/);
});
