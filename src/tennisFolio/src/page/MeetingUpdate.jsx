import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getPublicMeeting, updateMeeting } from '../utils/meetingApi';
import MeetingBasicInfoStep from '../components/meeting/shared/MeetingBasicInfoStep';
import MeetingSettingsStep from '../components/meeting/shared/MeetingSettingsStep';
import {
  buildDateTime,
  toMeetingForm,
  toNumberOrNull,
} from '../components/meeting/shared/meetingFormUtils';
import './Meeting.css';

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

function MeetingUpdate() {
  const { publicId } = useParams();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(initialForm);
  const [errorMessage, setErrorMessage] = useState('');
  const [accessDenied, setAccessDenied] = useState(false);
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

    getPublicMeeting(publicId)
      .then((response) => {
        if (!cancelled) {
          const meeting = response.data.data;
          if (meeting?.ownedByCurrentUser !== true) {
            setAccessDenied(true);
            setErrorMessage('모임을 수정할 권한이 없습니다.');
            return;
          }
          setForm(toMeetingForm(meeting));
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setErrorMessage(
            error.response?.data?.message || '모임 정보를 불러오지 못했습니다.',
          );
          setAccessDenied(true);
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
      navigate(`/meetings/${publicId}`, {
        state: {
          meetingNotice: {
            type: 'success',
            message: '모임을 수정했습니다.',
          },
        },
      });
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

  if (accessDenied) {
    return (
      <main className="meeting-page">
        <p className="meeting-state meeting-error">{errorMessage}</p>
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
        <MeetingBasicInfoStep
          form={form}
          onFieldChange={updateField}
          onPrevious={() => navigate(`/meetings/${publicId}`)}
          onNext={handleNext}
        />
      ) : (
        <MeetingSettingsStep
          form={form}
          isSubmitting={isSubmitting}
          submitLabel="모임 수정"
          submittingLabel="수정 중"
          onFieldChange={updateField}
          onPrevious={() => setStep(1)}
          onSubmit={handleSubmit}
        />
      )}
    </main>
  );
}

export default MeetingUpdate;
