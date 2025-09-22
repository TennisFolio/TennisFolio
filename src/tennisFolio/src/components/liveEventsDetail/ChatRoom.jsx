import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import './chatRoom.css';
import { base_server_url } from '@/constants';
import { apiRequest } from '../../utils/apiClient';

const MAX_LENGTH = 200;

function ChatRoom({ matchId = 'default-room' }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [nickname, setNickName] = useState(
    () => `User-${Math.floor(Math.random() * 10000)}`
  );
  const clientRef = useRef(null);
  const bottomRef = useRef(null);

  function getOrCreateUserId() {
    let id = localStorage.getItem('chatUserId');
    if (!id) {
      id = crypto.randomUUID();
      localStorage.setItem('chatUserId', id);
    }
  }

  function formatToMinuteSecond(isoString) {
    const date = new Date(isoString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
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

    // 개발 모드에서는 웹소켓 대신 목데이터로 채팅 시뮬레이션
    if (import.meta.env.DEV) {
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

      const simulateChat = () => {
        const chatInterval = setInterval(() => {
          const randomMessage =
            mockMessages[Math.floor(Math.random() * mockMessages.length)];
          const randomUser =
            mockUsers[Math.floor(Math.random() * mockUsers.length)];
          const newMessage = {
            matchId,
            sender: randomUser,
            userId: `mock-${Math.random()}`,
            timestamp: formatToMinuteSecond(new Date().toISOString()),
            message: randomMessage,
            type: 'TALK',
          };
          setMessages((prev) => [...prev, newMessage]);
        }, 8000); // 8초마다 새 메시지

        return chatInterval;
      };

      const intervalId = simulateChat();
      return () => clearInterval(intervalId);
    } else {
      // 프로덕션 모드에서는 실제 웹소켓 연결
      const socket = new SockJS(`${base_server_url}/ws`);
      const client = Stomp.over(socket);

      client.connect({}, () => {
        client.subscribe(`/topic/match.${matchId}`, (msg) => {
          const received = JSON.parse(msg.body);
          setMessages((prev) => [...prev, received]);
        });
        clientRef.current = client;
      });

      return () => {
        if (clientRef.current) {
          clientRef.current.disconnect();
        }
      };
    }
  }, [matchId]);

  const sendMessage = () => {
    if (!input.trim()) return;

    const userId = localStorage.getItem('chatUserId');
    const message = {
      matchId,
      sender: nickname,
      userId: userId,
      timestamp: formatToMinuteSecond(new Date().toISOString()),
      message: input,
      type: 'TALK',
    };

    if (import.meta.env.DEV) {
      // 개발 모드에서는 로컬 상태에 바로 추가
      setMessages((prev) => [...prev, message]);
    } else {
      // 프로덕션 모드에서는 실제 웹소켓으로 전송
      clientRef.current.send(
        `/app/chat.send/${matchId}`,
        {},
        JSON.stringify(message)
      );
    }
    setInput('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  useEffect(() => {
    bottomRef.current?.scrollIntoView({
      behavior: 'smooth',
      block: 'nearest',
      inline: 'nearest',
    });
  }, [messages]);

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
                {isMine && <div className="chat-time">{msg.timestamp}</div>}

                <div className={`chat-bubble ${isMine ? 'mine' : 'theirs'}`}>
                  <div className="chat-meta">
                    <span className="sender">{msg.sender}</span>
                  </div>
                  <div className="chat-text">{msg.message}</div>
                </div>

                {!isMine && <div className="chat-time">{msg.timestamp}</div>}
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
          {input.length} / {MAX_LENGTH}
        </div>
        <button onClick={sendMessage} className="chat-button">
          전송
        </button>
      </div>
    </div>
  );
}

export default ChatRoom;
