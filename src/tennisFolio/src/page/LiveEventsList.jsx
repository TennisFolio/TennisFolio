import React from 'react';
import { useState } from 'react';
import { useEffect, useRef } from 'react';
import LiveEvents from '../components/main/LiveEvents';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { base_server_url } from '@/constants';

import { apiRequest } from '../utils/apiClient';
import MetatagRenderer from '../components/MetatagRenderer';
import { useParams } from 'react-router-dom';
import MSWToggle from '../components/dev/MSWToggle';
import { LiveEventsSkeleton } from './LiveEventsSkeleton';

function Main() {
  const [liveEvents, setLiveEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const clientRef = useRef(null);
  const param = useParams();
  const category = param.category;
  const emptyEventTestArray = [];

  // 개발 모드 확인
  const isDevelopment = import.meta.env.MODE === 'development';

  // API 호출 함수 분리
  const fetchLiveEvents = () => {
    setIsLoading(true);
    apiRequest
      .get(`/api/${category}/liveEvents`)
      .then((res) => {
        setLiveEvents(res.data.data);
      })
      .catch((err) => {
        console.error('라이브 이벤트 조회 실패:', err);
        setLiveEvents([]);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  // 초기 데이터 요청
  useEffect(() => {
    fetchLiveEvents();
  }, [category]);

  // MSW 토글 감지 및 데이터 재요청
  useEffect(() => {
    if (!isDevelopment) return;

    const handleMswToggle = () => {
      setTimeout(() => {
        fetchLiveEvents();
      }, 100);
    };

    window.addEventListener('mswToggled', handleMswToggle);

    return () => {
      window.removeEventListener('mswToggled', handleMswToggle);
    };
  }, [isDevelopment]);

  // 웹소켓 연결 및 구독 (프로덕션에서만)
  useEffect(() => {
    if (!isDevelopment) {
      const socket = new SockJS(`${base_server_url}/ws`);
      const stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 30000,
        onConnect: () => {
          stompClient.subscribe(`/topic/${category}/liveMatches`, (message) => {
            const matchList = JSON.parse(message.body);
            setLiveEvents(matchList);
          });
        },
        onStompError: (frame) => {
          console.error('Broker reported error:', frame.headers['message']);
          console.error('Additional details:', frame.body);
        },
      });

      clientRef.current = stompClient;
      stompClient.activate();

      return () => {
        if (clientRef.current && clientRef.current.active) {
          clientRef.current.deactivate();
        }
      };
    }
  }, [category, isDevelopment]);

  return (
    <>
      <MetatagRenderer />
      {/* LiveEvents 페이지에서만 MSW 토글 표시 */}
      {isDevelopment && <MSWToggle />}
      {isLoading ? (
        <LiveEventsSkeleton />
      ) : (
        <LiveEvents liveEvents={liveEvents} />
      )}
    </>
  );
}

export default Main;
