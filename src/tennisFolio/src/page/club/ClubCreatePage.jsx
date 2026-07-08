import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ClubCreateView from '../../components/club/ClubCreateView';
import { createClub } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import { emptyClubForm, errorMessage, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubCreatePage({ currentUser }) {
  const navigate = useNavigate();
  const [clubForm, setClubForm] = useState(emptyClubForm);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  const showNotice = (message) => {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 2200);
  };

  const handleCreateClub = async (event) => {
    event.preventDefault();
    const name = clubForm.name.trim();

    if (!name) {
      showNotice('클럽명을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      const response = await createClub({
        name,
        description: clubForm.description.trim(),
      });
      const nextPublicId = unwrapData(response, {})?.publicId;

      setClubForm(emptyClubForm);
      navigate(nextPublicId ? `/clubs/${nextPublicId}` : '/clubs', {
        state: { activeTab: 'members' },
      });
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽을 만들지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  if (!currentUser) {
    return <ClubAuthRequired />;
  }

  return (
    <main className="club-mobile-page">
      <ClubCreateView
        form={clubForm}
        notice={notice}
        error={error}
        isSaving={isSaving}
        onSubmit={handleCreateClub}
        onChange={setClubForm}
        onBack={() => {
          setClubForm(emptyClubForm);
          navigate('/clubs');
        }}
      />
    </main>
  );
}

export default ClubCreatePage;
