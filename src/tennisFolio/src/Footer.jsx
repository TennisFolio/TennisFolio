import React from 'react';

function Footer() {
  return (
    <div
      style={{
        backgroundColor: '#f8f8f8',
        padding: '20px',
        textAlign: 'center',
        marginTop: '5rem',
      }}
    >
      <h5>
        광고 및 후원 문의
        <br />
        Advertising and Sponsorshop Contact
      </h5>
      <p>tennisfolio1029@gmail.com</p>
      <p>
        TennisFolio
        <br />
        호스팅 서비스 : AWS
        <br />
      </p>

      <p>
        Disclaimer:<br></br> All content is provided for fun and entertainment
        purposes only. <br />※ Some of the data in this service is collected
        through the TennisApi provided by RapidAPI.
      </p>
      <p> TENNISFOLIO net All rights reserved. 2025 </p>
      <div>
        <a href="/privacy">개인정보 처리방침</a>
      </div>
    </div>
  );
}

export default Footer;
