import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import './chatRoom.css';
import { base_server_url } from '@/constants';
import { apiRequest } from '../../utils/apiClient';
import { selectMSWActive } from '../../store/mswSlice';

const MAX_LENGTH = 200;

function ChatRoom({ matchId = 'default-room' }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [nickname, setNickName] = useState(
    () => `User-${Math.floor(Math.random() * 10000)}`
  );
  const clientRef = useRef(null);
  const lastSentTime = useRef(0);
  const bottomRef = useRef(null);
  const isMSWActive = useSelector(selectMSWActive);

  function getOrCreateUserId() {
    let id = localStorage.getItem('chatUserId');
    if (!id) {
      id = crypto.randomUUID();
      localStorage.setItem('chatUserId', id);
    }
  }

  // YYYYMMDDhhmmss 형식의 문자열을 "hh:mm" 포맷으로 변환
  function formatToMinuteSecond(yyyymmddhhmmss) {
    if (!yyyymmddhhmmss || yyyymmddhhmmss.length !== 14) return '';
    const year = yyyymmddhhmmss.slice(0, 4);
    const month = yyyymmddhhmmss.slice(4, 6);
    const day = yyyymmddhhmmss.slice(6, 8);
    const hour = yyyymmddhhmmss.slice(8, 10);
    const minute = yyyymmddhhmmss.slice(10, 12);
    // const second = yyyymmddhhmmss.slice(12, 14); // 필요시 사용

    // Date 객체로 변환 (로컬 타임존 기준)
    const date = new Date(`${year}-${month}-${day}T${hour}:${minute}:00`);

    // "hh:mm" 형식으로 반환
    return date.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  }
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await apiRequest.get(
          `${base_server_url}/api/chat/${matchId}`
        );
        if (response.data.code !== '0000') {
          console.error('데이터 조회 실패!', response.data.message);
          return;
        }

        const myChat = response.data.data.find(
          (item) => item.userId === localStorage.getItem('chatUserId')
        );
        if (myChat) {
          setNickName(myChat.sender);
        }

        setMessages(response.data.data);
      } catch (error) {
        console.error('Error fetching live events:', error);
      }
    };

    fetchData();
    getOrCreateUserId();

    // MSW가 활성화되어 있으면 웹소켓 대신 목데이터로 채팅 시뮬레이션
    if (isMSWActive) {
      const mockMessages = [
        '와 이 경기 진짜 박진감 넘치네요!',
        '알카라즈 폼이 정말 좋아요 🎾',
        '조코비치도 만만치 않네',
        '이번 세트는 누가 가져갈까요?',
        '테니스 최고!',
        '실시간으로 보니까 더 재밌어요',
        '다음 포인트가 중요할 것 같아요',
      ];

      const mockUsers = ['임재학', '박태환', '윤선아', '이서영', '김현우'];

      const chatInterval = setInterval(() => {
        const randomMessage =
          mockMessages[Math.floor(Math.random() * mockMessages.length)];
        const randomUser =
          mockUsers[Math.floor(Math.random() * mockUsers.length)];
        const now = new Date();
        const timestamp =
          now.getFullYear().toString() +
          (now.getMonth() + 1).toString().padStart(2, '0') +
          now.getDate().toString().padStart(2, '0') +
          now.getHours().toString().padStart(2, '0') +
          now.getMinutes().toString().padStart(2, '0') +
          now.getSeconds().toString().padStart(2, '0');

        const newMessage = {
          matchId,
          sender: randomUser,
          userId: `mock-${Math.random()}`,
          timestamp: timestamp,
          message: randomMessage,
          type: 'TALK',
        };
        setMessages((prev) => [...prev, newMessage]);
      }, 8000); // 8초마다 새 메시지

      return () => {
        clearInterval(chatInterval);
      };
    } else {
      // MSW가 비활성화되어 있으면 실제 웹소켓 연결
      const socket = new SockJS(`${base_server_url}/ws`);
      const client = Stomp.over(socket);

      client.connect({}, () => {
        client.subscribe(`/topic/match.${matchId}`, (msg) => {
          const received = JSON.parse(msg.body);

          // 중복 메시지 필터링 (같은 userId + timestamp + message 조합)
          setMessages((prev) => {
            const isDuplicate = prev.some(
              (existingMsg) =>
                existingMsg.userId === received.userId &&
                existingMsg.timestamp === received.timestamp &&
                existingMsg.message === received.message
            );

            if (isDuplicate) {
              console.log('⚠️ 중복 메시지 무시:', received);
              return prev;
            }

            return [...prev, received];
          });
        });
        clientRef.current = client;
      });

      return () => {
        if (clientRef.current) {
          clientRef.current.disconnect();
        }
      };
    }
  }, [matchId, isMSWActive]);

  const sendMessage = () => {
    if (!input.trim()) return;

    // 디바운스: 500ms 내 중복 전송 방지
    const nowTime = Date.now();
    if (nowTime - lastSentTime.current < 500) {
      setInput(''); // 중복 전송이어도 입력창은 비우기
      return;
    }
    lastSentTime.current = nowTime;

    const userId = localStorage.getItem('chatUserId');
    const now = new Date();
    const timestamp =
      now.getFullYear().toString() +
      (now.getMonth() + 1).toString().padStart(2, '0') +
      now.getDate().toString().padStart(2, '0') +
      now.getHours().toString().padStart(2, '0') +
      now.getMinutes().toString().padStart(2, '0') +
      now.getSeconds().toString().padStart(2, '0');

    const message = {
      matchId,
      sender: nickname,
      userId: userId,
      timestamp: timestamp,
      message: input,
      type: 'TALK',
    };

    if (isMSWActive) {
      // MSW 모드에서는 로컬 상태에 바로 추가
      setMessages((prev) => [...prev, message]);
    } else {
      // MSW 비활성화 시 실제 웹소켓으로 전송 (서버에서 브로드캐스트로 다시 받음)
      if (clientRef.current) {
        clientRef.current.send(
          `/app/chat.send/${matchId}`,
          {},
          JSON.stringify(message)
        );
      }
      // 웹소켓 모드에서는 서버에서 브로드캐스트로 받을 예정이므로 로컬에 추가하지 않음
    }
    setInput('');

    // 사용자가 메시지를 보낼 때만 스크롤을 맨 아래로 이동
    setTimeout(() => {
      bottomRef.current?.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest',
        inline: 'nearest',
      });
    }, 100);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">🗨️ Live Chat</div>
      <div className="chat-box">
        {messages.map((msg, idx) => {
          const isMine = msg.userId === localStorage.getItem('chatUserId');
          return (
            <div
              key={idx}
              className={`chat-bubble-container ${isMine ? 'mine' : 'theirs'}`}
            >
              <div className={`bubble-row ${isMine ? 'mine' : 'theirs'}`}>
                <div className={`chat-bubble ${isMine ? 'mine' : 'theirs'}`}>
                  <div className="chat-meta">
                    <span className="sender">{msg.sender}</span>
                  </div>
                  <div className="chat-text">{msg.message}</div>
                </div>

                {
                  <div className="chat-time">
                    {formatToMinuteSecond(msg.timestamp)}
                  </div>
                }
              </div>
            </div>
          );
        })}
        <div ref={bottomRef}></div>
      </div>
      <div className="chat-input-area">
        <input
          className="chat-input"
          value={input}
          onChange={(e) => {
            if (e.target.value.length <= MAX_LENGTH) {
              setInput(e.target.value);
            }
          }}
          placeholder="메시지를 입력하세요"
          onKeyDown={handleKeyDown}
        />
        <div className="chat-length-indicator">
          <div>
            {input.length} / {MAX_LENGTH}
          </div>
        </div>
        <button onClick={sendMessage} className="chat-button">
          전송
        </button>
      </div>
    </div>
  );
}

export default ChatRoom;
