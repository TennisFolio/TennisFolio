import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { selectMSWActive } from '../../store/mswSlice';

function SmartImage({ base_url, imageName, fallbackText }) {
  const [step, setStep] = useState(0);
  const [src, setSrc] = useState(null);
  const isMSWActive = useSelector(selectMSWActive);
  const isDevelopment = import.meta.env.MODE === 'development';

  // MSW 상태에 따라 이미지 소스 결정
  const sources = (() => {
    if (isDevelopment && isMSWActive) {
      // MSW 활성화 시: public 폴더의 테스트 이미지 사용
      return [`/images/${imageName}`];
    } else {
      // 실제 API 사용 시: 서버의 여러 형식 이미지 시도
      return [
        `${base_url}/converted/${imageName}.avif`,
        `${base_url}/${imageName}.webp`,
        `${base_url}/${imageName}`,
      ];
    }
  })();

  // MSW 상태가 변경되면 이미지 로딩을 처음부터 시작
  useEffect(() => {
    setStep(0);
    setSrc(null);
  }, [isMSWActive, imageName]);

  useEffect(() => {
    if (step >= sources.length) {
      setSrc(null);
      return;
    }

    const img = new Image();
    img.src = sources[step];

    img.onload = () => setSrc(sources[step]);
    img.onerror = () => setStep((prev) => prev + 1);
  }, [step, sources]);

  if (step >= sources.length) {
    // 모든 이미지 로딩 실패 시 fallback 표시
    return (
      <div
        className="playerFallback"
        style={{
          width: '100px',
          height: '100px',
          borderRadius: '50%',
          backgroundColor: '#f0f0f0',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '12px',
          color: '#666',
          textAlign: 'center',
          border: '1px solid #ddd',
        }}
      >
        {fallbackText
          ?.split(' ')
          .map((n) => n[0])
          .join('') || '?'}
      </div>
    );
  }

  if (!src) return null; // 로딩 중

  return (
    <img
      className="playerImg"
      src={src}
      alt={fallbackText}
      style={{
        width: '100px',
        height: '100px',
        objectFit: 'cover',
        borderRadius: '50%',
        border: '1px solid #ddd',
      }}
    />
  );
}

export default SmartImage;
