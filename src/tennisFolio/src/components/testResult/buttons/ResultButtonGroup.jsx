import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useCopyToClipboard } from '@uidotdev/usehooks';
import './resultButtonGroup.css';
import { LinkOutlined , RedoOutlined, HomeOutlined} from '@ant-design/icons';

function ResultButtonGroup() {
  const navigate = useNavigate();
  const { category } = useParams();
  const [, copyToClipboard] = useCopyToClipboard();

  const handleCopyLink = () => {
    copyToClipboard(window.location.href);
    alert('링크가 복사되었습니다!');
  };

  return (
    <div className="result-button-group">
      <div className="button-row">
        <button className="result-button" onClick={handleCopyLink}>
          <LinkOutlined /> &nbsp; 링크 복사
        </button>
        <button className="result-button" onClick={() => navigate(`/test/${category}`)}>
          <RedoOutlined /> &nbsp; 다시 하기
        </button>
      </div>
      <div className="button-row single">
        <button className="result-button" onClick={() => navigate('/test')}>
          <HomeOutlined /> &nbsp; 다른 테스트 하러 가기
        </button>
      </div>
    </div>
  );
}

export default ResultButtonGroup;