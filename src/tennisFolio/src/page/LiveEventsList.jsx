import React from 'react'
import { useState } from 'react'
import { useEffect, useRef} from 'react'
import LiveEvents from '../components/main/LiveEvents';
import {Client} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { base_server_url } from '../App';
import axios from 'axios';
import MetatagRenderer from '../components/MetatagRenderer';
import { useParams } from 'react-router-dom';
function Main() {
  const [liveEvents, setLiveEvents] = useState([]);
  
  const clientRef = useRef(null);
  const param = useParams();
  const category = param.category;
  // 초기 데이터 요청
  useEffect(() => {
    
    console.log(category);
    axios.get(`${base_server_url}/api/${category}/liveEvents`)
    .then((res => {
      console.log(res.data.data);
      setLiveEvents(res.data.data)
    }))
    .catch((err) => console.log(err));
    
    
  }, [category]);

  // 웹소켓 연결 및 구독
  useEffect(() => {
    const socket = new SockJS(`${base_server_url}/ws`);
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay : 30000,
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
    //setLiveEvents(LIVEEVENTS);
  },[])
  return (
    <>
    <MetatagRenderer/>
    <LiveEvents liveEvents={liveEvents} />
    </>
  )
}

export default Main