import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getManagedMeeting, updateMeeting } from '../utils/meetingApi';
import './Meeting.css';

const quotaModes = [
  { value: 'NONE', label: '제한 없음' },
  { value: 'TOTAL', label: '전체 정원' },
  { value: 'GENDER', label: '성별 정원' },
];

const initialForm = {
  title: '',
  date: '',
  startTime: '',
  endTime: '',
  note: '',
  quotaMode: 'NONE',
  maxParticipants: '',
  maxMaleParticipants: '',
  maxFemaleParticipants: '',
  courtCount: '1',
  totalGames: '1',
};

function toNumberOrNull(value) {
  return value === '' ? null : Number(value);
}

function buildDateTime(date, time) {
  return `${date}T${time}:00`;
}

function toDateInputValue(value) {
  return value ? value.slice(0, 10) : '';
}

function toTimeInputValue(value) {
  return value ? value.slice(11, 16) : '';
}

function getQuotaMode(meeting) {
  if (meeting.maxParticipants != null) {
    return 'TOTAL';
  }
  if (meeting.maxMaleParticipants != null || meeting.maxFemaleParticipants != null) {
    return 'GENDER';
  }
  return 'NONE';
}

function toForm(meeting) {
  return {
    title: meeting.title || '',
    date: toDateInputValue(meeting.startAt),
    startTime: toTimeInputValue(meeting.startAt),
    endTime: toTimeInputValue(meeting.endAt),
    note: meeting.note || '',
    quotaMode: getQuotaMode(meeting),
    maxParticipants: meeting.maxParticipants?.toString() || '',
    maxMaleParticipants: meeting.maxMaleParticipants?.toString() || '',
    maxFemaleParticipants: meeting.maxFemaleParticipants?.toString() || '',
    courtCount: meeting.courtCount?.toString() || '1',
    totalGames: meeting.totalGames?.toString() || '1',
  };
}

function MeetingUpdate() {
  const { publicId } = useParams();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(initialForm);
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const startAt = useMemo(
    () => buildDateTime(form.date, form.startTime),
    [form.date, form.startTime],
  );
  const endAt = useMemo(
    () => buildDateTime(form.date, form.endTime),
    [form.date, form.endTime],
  );

  useEffect(() => {
    let cancelled = false;

    getManagedMeeting(publicId)
      .then((response) => {
        if (!cancelled) {
          setForm(toForm(response.data.data));
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setErrorMessage(
            error.response?.data?.message || '모임 정보를 불러오지 못했습니다.',
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [publicId]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrorMessage('');
  };

  const validateStepOne = () => {
    if (!form.title.trim()) {
      return '모임 이름을 입력해주세요.';
    }
    if (!form.date || !form.startTime || !form.endTime) {
      return '모임 날짜와 시간을 입력해주세요.';
    }
    if (new Date(endAt).getTime() <= new Date(startAt).getTime()) {
      return '종료 시간은 시작 시간 이후여야 합니다.';
    }
    return '';
  };

  const validateStepTwo = () => {
    if (Number(form.courtCount) < 1 || Number(form.totalGames) < 1) {
      return '코트 수와 총 경기 수는 1 이상이어야 합니다.';
    }
    if (form.quotaMode === 'TOTAL' && Number(form.maxParticipants) < 1) {
      return '전체 정원은 1명 이상이어야 합니다.';
    }
    if (
      form.quotaMode === 'GENDER' &&
      (Number(form.maxMaleParticipants) < 1 ||
        Number(form.maxFemaleParticipants) < 1)
    ) {
      return '남성/여성 정원은 각각 1명 이상이어야 합니다.';
    }
    return '';
  };

  const handleNext = () => {
    const message = validateStepOne();
    if (message) {
      setErrorMessage(message);
      return;
    }
    setStep(2);
  };

  const handleSubmit = async () => {
    const message = validateStepOne() || validateStepTwo();
    if (message) {
      setErrorMessage(message);
      return;
    }

    const payload = {
      title: form.title.trim(),
      startAt,
      endAt,
      note: form.note.trim() || null,
      maxParticipants:
        form.quotaMode === 'TOTAL' ? toNumberOrNull(form.maxParticipants) : null,
      maxMaleParticipants:
        form.quotaMode === 'GENDER'
          ? toNumberOrNull(form.maxMaleParticipants)
          : null,
      maxFemaleParticipants:
        form.quotaMode === 'GENDER'
          ? toNumberOrNull(form.maxFemaleParticipants)
          : null,
      courtCount: Number(form.courtCount),
      totalGames: Number(form.totalGames),
    };

    try {
      setIsSubmitting(true);
      await updateMeeting(publicId, payload);
      navigate(`/meetings/${publicId}/manage`);
    } catch (error) {
      setErrorMessage(error.response?.data?.message || '모임을 수정하지 못했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <main className="meeting-page">
        <p className="meeting-state">불러오는 중입니다.</p>
      </main>
    );
  }

  return (
    <main className="meeting-page">
      <header className="meeting-header">
        <h1>모임 수정</h1>
        <p>공유된 참석 체크와 대진표 생성 조건에 쓰이는 모임 정보를 수정합니다.</p>
        <div className="meeting-stepper" aria-hidden="true">
          <span className="meeting-step active" />
          <span className={`meeting-step ${step === 2 ? 'active' : ''}`} />
        </div>
      </header>

      {errorMessage && <p className="meeting-state meeting-error">{errorMessage}</p>}

      {step === 1 ? (
        <section className="meeting-panel" aria-label="모임 기본 정보">
          <h2>1 / 2 기본 정보</h2>
          <label className="meeting-field">
            <span>모임 이름</span>
            <input
              value={form.title}
              onChange={(event) => updateField('title', event.target.value)}
            />
          </label>
          <label className="meeting-field">
            <span>모임 날짜</span>
            <input
              type="date"
              value={form.date}
              onChange={(event) => updateField('date', event.target.value)}
            />
          </label>
          <div className="meeting-grid two">
            <label className="meeting-field">
              <span>시작 시간</span>
              <input
                type="time"
                value={form.startTime}
                onChange={(event) => updateField('startTime', event.target.value)}
              />
            </label>
            <label className="meeting-field">
              <span>종료 시간</span>
              <input
                type="time"
                value={form.endTime}
                onChange={(event) => updateField('endTime', event.target.value)}
              />
            </label>
          </div>
          <label className="meeting-field">
            <span>메모</span>
            <textarea
              value={form.note}
              onChange={(event) => updateField('note', event.target.value)}
            />
          </label>
          <button type="button" className="meeting-button primary full" onClick={handleNext}>
            다음
          </button>
        </section>
      ) : (
        <section className="meeting-panel" aria-label="모임 설정">
          <h2>2 / 2 모임 설정</h2>
          <div>
            <div className="meeting-toggle" role="group" aria-label="정원 방식">
              {quotaModes.map((mode) => (
                <button
                  type="button"
                  key={mode.value}
                  className={`meeting-button ${
                    form.quotaMode === mode.value ? 'primary' : ''
                  }`}
                  onClick={() => updateField('quotaMode', mode.value)}
                >
                  {mode.label}
                </button>
              ))}
            </div>
          </div>
          {form.quotaMode === 'TOTAL' && (
            <label className="meeting-field">
              <span>전체 참석 정원</span>
              <input
                type="number"
                min="1"
                value={form.maxParticipants}
                onChange={(event) =>
                  updateField('maxParticipants', event.target.value)
                }
              />
            </label>
          )}
          {form.quotaMode === 'GENDER' && (
            <div className="meeting-grid two">
              <label className="meeting-field">
                <span>남성 정원</span>
                <input
                  type="number"
                  min="1"
                  value={form.maxMaleParticipants}
                  onChange={(event) =>
                    updateField('maxMaleParticipants', event.target.value)
                  }
                />
              </label>
              <label className="meeting-field">
                <span>여성 정원</span>
                <input
                  type="number"
                  min="1"
                  value={form.maxFemaleParticipants}
                  onChange={(event) =>
                    updateField('maxFemaleParticipants', event.target.value)
                  }
                />
              </label>
            </div>
          )}
          <div className="meeting-grid two">
            <label className="meeting-field">
              <span>코트 수</span>
              <input
                type="number"
                min="1"
                value={form.courtCount}
                onChange={(event) => updateField('courtCount', event.target.value)}
              />
            </label>
            <label className="meeting-field">
              <span>총 경기 수</span>
              <input
                type="number"
                min="1"
                value={form.totalGames}
                onChange={(event) => updateField('totalGames', event.target.value)}
              />
            </label>
          </div>
          <div className="meeting-action-row">
            <button type="button" className="meeting-button" onClick={() => setStep(1)}>
              이전
            </button>
            <button
              type="button"
              className="meeting-button primary"
              disabled={isSubmitting}
              onClick={handleSubmit}
            >
              {isSubmitting ? '수정 중' : '모임 수정'}
            </button>
          </div>
        </section>
      )}
    </main>
  );
}

export default MeetingUpdate;
