import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import {Stomp} from '@stomp/stompjs';
import './ChatRoom.css';
import { base_server_url } from '../../App';
import axios from 'axios';


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
        console.log("aaa" ,response.data.data);
        
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
        console.log("Received message", msg.body);
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
      timestamp: new Date().toISOString(),
      message: input,
      type: 'TALK',
    };
    clientRef.current.send(`/app/chat.send/${matchId}`, {}, JSON.stringify(message));
    setInput('');
  };

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
    
  }, [messages]);

  return (
    <div className="chat-container">
      <div className="chat-header">🗨️ Live Chat - {matchId}</div>
      <div className="chat-box">
        {messages.map((msg, idx) => (
          <div key={idx} className="chat-message">
            <strong>{msg.sender}:</strong> {msg.message}
          </div>
        ))}
        <div ref={bottomRef}></div>
      </div>
      <div className="chat-input-area">
        <input
          className="chat-input"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="메시지를 입력하세요"
          onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
        />
        <button onClick={sendMessage} className="chat-button">전송</button>
      </div>
    </div>
  );
}

export default ChatRoom;