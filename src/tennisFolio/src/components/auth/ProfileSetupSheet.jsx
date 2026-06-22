import { useMemo, useState } from 'react';
import './ProfileSetupSheet.css';
import { PROFILE_SETUP_COPY } from './profileSetupCopy';

function ProfileSetupSheet({ onSubmit }) {
  const [nickName, setNickName] = useState('');
  const [gender, setGender] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const trimmedName = nickName.trim();
  const canSubmit =
    trimmedName.length >= 1 &&
    trimmedName.length <= 10 &&
    Boolean(gender) &&
    !isSaving;

  const counterText = useMemo(() => `${trimmedName.length}/10`, [trimmedName]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }

    setIsSaving(true);
    setErrorMessage('');

    try {
      await onSubmit({ nickName: trimmedName, gender });
    } catch {
      setErrorMessage(PROFILE_SETUP_COPY.errorMessage);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="profile-setup-backdrop">
      <form className="profile-setup-sheet" onSubmit={handleSubmit}>
        <div className="profile-setup-header">
          <h2>{PROFILE_SETUP_COPY.title}</h2>
        </div>

        <label className="profile-field">
          <span>{PROFILE_SETUP_COPY.nickNameLabel}</span>
          <input
            value={nickName}
            maxLength={10}
            autoFocus
            placeholder={PROFILE_SETUP_COPY.nickNamePlaceholder}
            onChange={(event) => setNickName(event.target.value)}
          />
        </label>

        <div className="profile-field">
          <span>{PROFILE_SETUP_COPY.genderLabel}</span>
          <div className="profile-gender-options">
            <button
              type="button"
              className={gender === 'MALE' ? 'selected' : ''}
              onClick={() => setGender('MALE')}
            >
              {PROFILE_SETUP_COPY.maleLabel}
            </button>
            <button
              type="button"
              className={gender === 'FEMALE' ? 'selected' : ''}
              onClick={() => setGender('FEMALE')}
            >
              {PROFILE_SETUP_COPY.femaleLabel}
            </button>
          </div>
        </div>

        <div className="profile-setup-meta">
          <span>{counterText}</span>
          {errorMessage && <strong>{errorMessage}</strong>}
        </div>

        <button
          type="submit"
          className="profile-submit"
          disabled={!canSubmit}
        >
          {isSaving
            ? PROFILE_SETUP_COPY.savingLabel
            : PROFILE_SETUP_COPY.saveLabel}
        </button>
      </form>
    </div>
  );
}

export default ProfileSetupSheet;
