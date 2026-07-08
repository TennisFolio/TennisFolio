import { roleLabel } from './clubUtils';

function ClubListView({ clubs, isLoading, onCreateClub, onOpenClub }) {
  return (
    <>
      <header className="club-page-head club-list-head">
        <h1>내 클럽</h1>
        <p>소속 클럽을 보고, 운영 중인 클럽에서 모임과 클럽원을 관리합니다.</p>
        <button
          className="club-button primary full"
          type="button"
          onClick={onCreateClub}
        >
          클럽 만들기
        </button>
      </header>

      <section className="club-section">
        <div className="club-section-title">
          <h2>클럽 목록</h2>
        </div>

        {isLoading && <p className="club-notice static">클럽 목록을 불러오는 중입니다.</p>}
        {!isLoading && clubs.length === 0 && (
          <article className="club-card">
            <div className="club-card-title">아직 소속된 클럽이 없습니다.</div>
            <p className="club-card-copy">첫 클럽을 만들고 명단을 관리해 보세요.</p>
          </article>
        )}
        {clubs.map((club) => (
          <article className="club-card" key={club.publicId}>
            <div className="club-card-head">
              <div>
                <div className="club-card-title">{club.name}</div>
                <p className="club-card-copy club-description">{club.description}</p>
              </div>
              <span className={`club-chip ${club.role === 'ADMIN' ? 'admin' : ''}`}>
                {roleLabel(club.role)}
              </span>
            </div>
            <div className="club-chips">
              <span className="club-chip">{club.memberCount}명</span>
            </div>
            <button
              className={`club-button full ${club.role === 'ADMIN' ? 'accent' : ''}`}
              type="button"
              onClick={() => onOpenClub(club)}
            >
              {club.role === 'ADMIN' ? '관리' : '열기'}
            </button>
          </article>
        ))}
      </section>
    </>
  );
}

export default ClubListView;
