import { useEffect, useState } from 'react';

function SmartImage({ base_url, imageName, fallbackText }) {
  const [step, setStep] = useState(0);
  const [src, setSrc] = useState(null);

  console.log(`${base_url}/${imageName}.avif`);
  console.log(`${base_url}/${imageName}.webp`);
  const sources = [
    `${base_url}/converted/${imageName}.avif`,
    `${base_url}/${imageName}.webp`,
  ];

  useEffect(() => {
    if (step >= sources.length) {
      setSrc(null);
      return;
    }

    const img = new Image();
    img.src = sources[step];

    img.onload = () => setSrc(sources[step]);
    img.onerror = () => setStep(prev => prev + 1);
  }, [step]);

  if (step >= sources.length) {
    return <div className="fallbackText">{fallbackText}</div>;
  }

  if (!src) return null; // 로딩 중

  return (
    <img className="playerImg" src={src} alt={fallbackText} />
  );
}


export default SmartImage