import ClubForm from './ClubForm';
import ClubMessage from './ClubMessage';

function ClubCreateView({
  form,
  notice,
  error,
  isSaving,
  onSubmit,
  onChange,
  onBack,
}) {
  return (
    <>
      <header className="club-page-head">
        <span className="club-eyebrow">NEW CLUB</span>
        <h1>새 클럽</h1>
        <p>클럽을 만들면 생성자는 자동으로 관리자가 됩니다.</p>
      </header>
      <ClubMessage notice={notice} error={error} />
      <ClubForm
        form={form}
        submitLabel="클럽 만들기"
        isSaving={isSaving}
        onSubmit={onSubmit}
        onChange={onChange}
        onBack={onBack}
      />
    </>
  );
}

export default ClubCreateView;
