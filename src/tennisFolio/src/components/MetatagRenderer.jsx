import { Helmet } from 'react-helmet-async';

function MetatagRenderer() {
  return <Helmet>
    <title>TennisFolio | 테니스 복식 대진표와 모임 운영</title>
    <meta name="description" content="TennisFolio에서 테니스 복식 대진표를 만들고 모임과 경기 일정을 관리하세요." />
    <meta name="keywords" content="테니스, 복식, 대진표, 모임, 경기 일정" />
    <meta name="author" content="TennisFolio Team" />
    <meta property="og:title" content="TennisFolio | 테니스 복식 대진표와 모임 운영" />
    <meta property="og:description" content="테니스 복식 대진표를 만들고 모임과 경기 일정을 관리하세요." />
    <meta property="og:image" content="https://tennisfolio.net/img/meta/tennisfolio-preview.png" />
    <meta property="og:url" content="https://tennisfolio.net/" />
    <meta name="twitter:card" content="summary_large_image" />
  </Helmet>;
}

export default MetatagRenderer;
