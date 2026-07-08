import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  deleteMeeting,
  getPublicMeeting,
  updateMeeting,
} from '../utils/meetingApi';
import {
  deleteClubMeeting,
  getClubMeeting,
  updateClubMeeting,
} from '../utils/clubApi';
import MeetingBasicInfoStep from '../components/meeting/shared/MeetingBasicInfoStep';
import MeetingSettingsStep from '../components/meeting/shared/MeetingSettingsStep';
import MeetingDangerZone from '../components/meeting/manage/MeetingDangerZone';
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
  const { clubPublicId, publicId } = useParams();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(initialForm);
  const [errorMessage, setErrorMessage] = useState('');
  const [accessDenied, setAccessDenied] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

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

    const loadMeeting = clubPublicId
      ? getClubMeeting(clubPublicId, publicId)
      : getPublicMeeting(publicId);

    loadMeeting
      .then((response) => {
        if (!cancelled) {
          const meeting = response.data.data;
          if (!clubPublicId && meeting?.ownedByCurrentUser !== true) {
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
  }, [clubPublicId, publicId]);

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
      if (clubPublicId) {
        await updateClubMeeting(clubPublicId, publicId, payload);
      } else {
        await updateMeeting(publicId, payload);
      }
      navigate(
        clubPublicId
          ? `/clubs/${clubPublicId}/meetings/${publicId}`
          : `/meetings/${publicId}`,
        {
          state: {
            meetingNotice: {
              type: 'success',
              message: '모임을 수정했습니다.',
            },
          },
        },
      );
    } catch (error) {
      setErrorMessage(error.response?.data?.message || '모임을 수정하지 못했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteMeeting = async () => {
    const confirmed = window.confirm('모임을 삭제할까요?');
    if (!confirmed) {
      return;
    }

    try {
      setIsDeleting(true);
      if (clubPublicId) {
        await deleteClubMeeting(clubPublicId, publicId);
        navigate(`/clubs/${clubPublicId}`);
        return;
      }

      await deleteMeeting(publicId);
      navigate('/meetings');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || '모임을 삭제하지 못했습니다.');
    } finally {
      setIsDeleting(false);
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
        <h1>{clubPublicId ? '클럽 모임 수정' : '모임 수정'}</h1>
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
          onPrevious={() =>
            navigate(
              clubPublicId
                ? `/clubs/${clubPublicId}/meetings/${publicId}`
                : `/meetings/${publicId}`,
            )
          }
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

      <MeetingDangerZone
        disabled={isSubmitting || isDeleting}
        onDeleteMeeting={handleDeleteMeeting}
      />
    </main>
  );
}

export default MeetingUpdate;
