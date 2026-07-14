function ClubMessage({ notice, error }) {
  return (
    <>
      {notice && <p className="club-notice">{notice}</p>}
      {error && <p className="club-notice danger">{error}</p>}
    </>
  );
}

export default ClubMessage;
