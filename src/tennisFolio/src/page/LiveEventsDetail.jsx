
import EventCard from '../components/liveEventsDetail/EventCard';
import ChatRoom from '../components/liveEventsDetail/ChatRoom';
import {useParams} from 'react-router-dom';
import { useEffect, useState } from 'react';
import {Client} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { base_server_url } from '../App';
import axios from 'axios';
function LiveEventsDetail() {
    
    const { matchId } = useParams();
    const [liveEvent, setLiveEvent] = useState(null);
    useEffect(() => {
      const fetchData = async () => {
        try {    
          const response = await axios.get(`${base_server_url}/api/liveEvents/${matchId}`);
          if (response.data.code !== '0000') {
            console.error('데이터 조회 실패!', response.data.message);
            return;
          }
          setLiveEvent(response.data.data);
        } catch (error) {
          console.error('Error fetching live events:', error);
        }
      };

      fetchData();
    },[matchId]);

    useEffect(() => {
      const socket = new SockJS(`${base_server_url}/ws`);
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 10000,
        onConnect: () => {
          console.log("디테일 WebSocket 연결됨");

          client.subscribe(`/topic/liveMatch/${matchId}`, (message) => {
            const data = JSON.parse(message.body);
            console.log("디테일 WebSocket 메시지 수신됨", data);
            setLiveEvent(data);
          });
        },
        onStompError: (frame) => {
          console.error('STOMP 오류: ', frame);
        },
      });

      client.activate();
      return () => {
        if(client.active) client.deactivate();
      };
    }, [matchId])
  return (
    <div>
        {liveEvent ? (
            <>
                <EventCard event = {liveEvent}/>
                <ChatRoom matchId = {matchId}/>
            </>
        ) : (
            <div>Loading...</div>
        )}
    
    </div>
  )
}

export default LiveEventsDetail