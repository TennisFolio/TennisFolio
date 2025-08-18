import React from 'react';
import { useSelector } from 'react-redux';
import './LoadingMask.css';
import loadingAnimation from '../../assets/loading-animation.json';
import Lottie from 'lottie-react';

const LoadingMask = () => {
  const isLoading = useSelector((state) => state.loading.isLoading);

  // 테스트용: 항상 표시
  if (!isLoading) return null;

  return (
    <div className="loading-mask">
      <div className="loading-spinner">
        <Lottie
          animationData={loadingAnimation}
          loop={true}
          autoplay={true}
          style={{
            width: 120,
            height: 120,
            marginBottom: '1rem',
            filter: 'sepia(1) hue-rotate(40deg) saturate(1.8) brightness(1.2)',
          }}
        />
      </div>
    </div>
  );
};

export default LoadingMask;
