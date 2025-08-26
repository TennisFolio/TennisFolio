import { http, HttpResponse } from 'msw';
import { generateLiveEvents } from '../utils/mockDataGenerator';

// LiveEvents 페이지 전용 API 핸들러
export const handlers = [
  // 라이브 이벤트 조회 API만 가로채기 (다른 API는 실제 서버로)
  http.get('/api/:category/liveEvents', ({ params }) => {
    const { category } = params;

    const events = generateLiveEvents(5);

    return HttpResponse.json({
      success: true,
      data: events,
      message: `${category.toUpperCase()} 라이브 이벤트 조회 성공 (목 데이터)`,
    });
  }),

  // 라이브 이벤트 상세 조회 API (필요시)
  http.get('/api/liveEventsDetail/:matchId', ({ params }) => {
    const { matchId } = params;

    // 간단한 상세 목 데이터
    const eventDetail = {
      rapidId: matchId,
      tournamentName: 'ATP Masters 1000 Miami',
      roundName: 'Quarterfinals',
      status: 'Live',
      player1: { name: 'Carlos Alcaraz', ranking: 1 },
      player2: { name: 'Novak Djokovic', ranking: 2 },
      score: {
        currentGame: '30 : 15',
        sets: [
          { player1: 6, player2: 4 },
          { player1: 3, player2: 2 },
        ],
      },
    };

    return HttpResponse.json({
      success: true,
      data: eventDetail,
      message: '라이브 이벤트 상세 조회 성공 (목 데이터)',
    });
  }),
];
