import React from 'react'
import { Helmet } from 'react-helmet-async';
function MetatagRenderer() {
  return <Helmet>
    <title>TennisFolio - 테니스 대회 정보</title>
    <meta name="description" content="TennisFolio는 ATP 실시간 대회 정보를 제공합니다." />
    <meta name="keywords" content="테니스, ATP, 랭킹, 뉴스, 통계" />
    <meta name="author" content="TennisFolio Team" />
    <meta property="og:title" content="TennisFolio - 테니스 랭킹, 뉴스, 통계" />
    <meta property="og:description" content="최신 ATP 테니스 랭킹과 경기를 확인하세요." />
    <meta property="og:image" content="/path/to/image.jpg" />
    <meta property="og:url" content="https://tennisfolio.net" />
    <meta name="twitter:card" content="summary_large_image" />
  </Helmet>;
}

export default MetatagRenderer