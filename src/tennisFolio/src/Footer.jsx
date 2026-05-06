function Footer() {
  return (
    <footer className="footer">
      <div className="footer-inner">
        <strong>TennisFolio</strong>
        <p>대진표를 만들고 경기 일정을 관리합니다.</p>
        <a
          className="footer-contact-button"
          href="https://mail.google.com/mail/?view=cm&fs=1&to=tennisfolio1029@gmail.com&su=TennisFolio%20%EB%AC%B8%EC%9D%98"
          target="_blank"
          rel="noreferrer"
        >
          문의하기
        </a>
        <p className="footer-contact">문의: tennisfolio1029@gmail.com</p>
        <a href="/privacy">개인정보 처리방침</a>
        <small>© 2025 TennisFolio</small>
      </div>
    </footer>
  );
}

export default Footer;
