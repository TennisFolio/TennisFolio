import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import {Stomp} from '@stomp/stompjs';
import './chatRoom.css';
import { base_server_url } from '../../App';
import axios from 'axios';

const MAX_LENGTH = 200;

function ChatRoom({ matchId = 'default-room' }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [nickname, setNickName] = useState(() => `User-${Math.floor(Math.random() * 10000)}`);
  const clientRef = useRef(null);
  const bottomRef = useRef(null);

  function getOrCreateUserId(){
    let id = localStorage.getItem('chatUserId');
    if (!id){
      id = crypto.randomUUID();
      localStorage.setItem('chatUserId', id);
    }
  }

  function formatToMinuteSecond(isoString){
    const date = new Date(isoString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
  useEffect(() => {
    const socket = new SockJS(`${base_server_url}/ws`);
    const client = Stomp.over(socket);

    const fetchData = async () => {
      try {
        const response = await axios.get(`${base_server_url}/api/chat/${matchId}`);
        if (response.data.code !== '0000') {
          console.error('데이터 조회 실패!', response.data.message);
          return;
        }
        
        const myChat = response.data.data.find((item) => item.userId === localStorage.getItem('chatUserId'));
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
  }, [matchId]);

  const sendMessage = () => {
    if (!input.trim()) return;

    const userId = localStorage.getItem('chatUserId');
    const message = {
      matchId,
      sender: nickname,
      userId : userId,
      timestamp: formatToMinuteSecond(new Date().toISOString()),
      message: input,
      type: 'TALK',
    };
    clientRef.current.send(`/app/chat.send/${matchId}`, {}, JSON.stringify(message));
    setInput('');
  };

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
    
  }, [messages]);

  function formatTime(isoString) {
  const date = new Date(isoString);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

  return (
    <div className="chat-container">
      <div className="chat-header">🗨️ Live Chat</div>
      <div className="chat-box">
        {messages.map((msg, idx) => {
          const isMine = msg.userId === localStorage.getItem('chatUserId');
          return(
            <div key={idx} className={`chat-bubble-container ${isMine ? 'mine' : 'theirs'}`}>
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
          )
        }
          
          
        )}
        <div ref={bottomRef}></div>
      </div>
      <div className="chat-input-area">
        <input
          className="chat-input"
          value={input}
          onChange={(e) => {
            if(e.target.value.length <= MAX_LENGTH){
              setInput(e.target.value)
            }
          }}
          placeholder="메시지를 입력하세요"
          onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
        />
        <div className="chat-length-indicator">
          {input.length} / {MAX_LENGTH}
        </div>
        <button onClick={sendMessage} className="chat-button">전송</button>
      </div>
    </div>
  );
}

export default ChatRoom;