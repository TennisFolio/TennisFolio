import { http, HttpResponse } from 'msw';
import { generateLiveEvents } from '../utils/mockDataGenerator';

// LiveEvents 페이지 전용 API 핸들러
export const handlers = [
  // 라이브 이벤트 조회 API만 가로채기 (다른 API는 실제 서버로)
  http.get('*/api/:category/liveEvents', ({ params }) => {
    const { category } = params;

    const events = generateLiveEvents(5);

    return HttpResponse.json({
      success: true,
      data: events,
      message: `${category.toUpperCase()} 라이브 이벤트 조회 성공 (목 데이터)`,
    });
  }),

  // 라이브 이벤트 상세 조회 API
  http.get('*/api/liveEvents/:matchId', ({ params }) => {
    const { matchId } = params;

    // 실제 LiveEvents 구조와 동일한 목 데이터
    const eventDetail = {
      rapidId: matchId,
      tournamentName: 'ATP Masters 1000 Miami',
      roundName: 'Quarterfinals',
      status: 'Live',
      homePlayer: {
        playerName: 'Carlos Alcaraz',
        playerRanking: 1,
        playerImage: 'player1.jpg',
        playerCountryAlpha: 'ES',
      },
      awayPlayer: {
        playerName: 'Novak Djokovic',
        playerRanking: 2,
        playerImage: 'player2.jpg',
        playerCountryAlpha: 'RS',
      },
      homeScore: {
        current: 1,
        point: '30',
        periodScore: [6, 4, 0, 0, 0],
      },
      awayScore: {
        current: 1,
        point: '15',
        periodScore: [4, 6, 0, 0, 0],
      },
    };

    return HttpResponse.json({
      code: '0000',
      data: eventDetail,
      message: '라이브 이벤트 상세 조회 성공 (목 데이터)',
    });
  }),

  // 채팅 메시지 조회 API
  http.get('*/api/chat/:matchId', ({ params }) => {
    const { matchId } = params;

    const mockChatMessages = [
      {
        matchId,
        sender: '우성환',
        userId: 'user-1',
        timestamp: '20251003143000',
        message: '임재학선수 오늘 경기력 정말 최고네요!',
        type: 'TALK',
      },
    ];

    return HttpResponse.json({
      code: '0000',
      data: mockChatMessages,
      message: '채팅 메시지 조회 성공 (목 데이터)',
    });
  }),
];
