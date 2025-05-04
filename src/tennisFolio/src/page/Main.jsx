import React from 'react'
import { useState } from 'react'
import { useEffect } from 'react'
import LiveEvents from '../components/main/LiveEvents';
import axios from 'axios';
import {LIVEEVENTS} from '../data/LIVEEVENTS';
import { base_server_url } from '../App';
function Main() {
  const [liveEvents, setLiveEvents] = useState([]);

  useEffect(() => {
    axios.get(`${base_server_url}/api/liveEvents`)
      .then((response) => {
        if(response.data.code !== '0000'){
          console.error('데이터 조회 실패!', response.data.message);
          return;
        }
        setLiveEvents(response.data.data);
        
      })
      .catch((error) => {
        console.error('Error fetching live events:', error);
      });

    //setLiveEvents(LIVEEVENTS);
  },[])
  return (
    <LiveEvents liveEvents={liveEvents} />
  )
}

export default Main