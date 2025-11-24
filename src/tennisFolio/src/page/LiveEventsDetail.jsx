import EventCard from '../components/liveEventsDetail/EventCard';
import ChatRoom from '../components/liveEventsDetail/ChatRoom';
import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { base_server_url } from '@/constants';
import { apiRequest } from '../utils/apiClient';
import { useNavigate } from 'react-router-dom';
import { selectMSWActive } from '../store/mswSlice';
import { LiveEventCardSkeleton } from './LiveEventsSkeleton';

function LiveEventsDetail() {
  const { matchId } = useParams();
  const [liveEvent, setLiveEvent] = useState(null);
  const navigate = useNavigate();
  const isMSWActive = useSelector(selectMSWActive);
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await apiRequest.get(
          `${base_server_url}/api/liveEvents/${matchId}`
        );
        if (response.data.code !== '0000') {
          console.error('데이터 조회 실패!', response.data.message);
        }
        setLiveEvent(response.data.data);
      } catch (error) {
        console.error('Error fetching live events:', error);
        if (error.response.data.code === '9200') {
          alert('해당 경기는 현재 진행중이지 않습니다!');
          navigate('/');
          return;
        }
      }
    };

    fetchData();
  }, [matchId, navigate]);

  // MSW 상태 변경 또는 matchId 변경 시에만 웹소켓/시뮬레이션 재시작
  useEffect(() => {
    // liveEvent가 로드되지 않았으면 아무것도 하지 않음
    if (!liveEvent) return;

    // MSW가 활성화되어 있으면 웹소켓 대신 목데이터로 실시간 업데이트 시뮬레이션
    if (isMSWActive) {
      const updateInterval = setInterval(() => {
        setLiveEvent((prevEvent) => {
          if (!prevEvent) return prevEvent;
          return {
            ...prevEvent,
            homeScore: {
              ...prevEvent.homeScore,
              point: ['0', '15', '30', '40'][Math.floor(Math.random() * 4)],
            },
            awayScore: {
              ...prevEvent.awayScore,
              point: ['0', '15', '30', '40'][Math.floor(Math.random() * 4)],
            },
          };
        });
      }, 5000); // 5초마다 스코어 업데이트

      return () => {
        clearInterval(updateInterval);
      };
    } else {
      // MSW가 비활성화되어 있으면 실제 웹소켓 연결
      const socket = new SockJS(`${base_server_url}/ws`);
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 10000,
        onConnect: () => {
          client.subscribe(`/topic/liveMatch/${matchId}`, (message) => {
            const data = JSON.parse(message.body);
            setLiveEvent(data);
          });
        },
        onStompError: (frame) => {
          console.error('STOMP 오류: ', frame);
        },
      });

      client.activate();
      return () => {
        if (client.active) client.deactivate();
      };
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [matchId, isMSWActive]);
  return (
    <div>
      {liveEvent ? (
        <>
          <EventCard event={liveEvent} />
          <ChatRoom matchId={matchId} />
        </>
      ) : (
        <>
          <LiveEventCardSkeleton />
          <ChatRoom matchId={matchId} />
        </>
      )}
    </div>
  );
}

export default LiveEventsDetail;
