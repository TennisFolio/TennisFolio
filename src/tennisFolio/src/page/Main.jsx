import React from 'react'
import { useState } from 'react'
import { useEffect } from 'react'
import LiveEvents from '../components/main/LiveEvents';
import axios from 'axios';
import {LIVEEVENTS} from '../data/LIVEEVENTS';

function Main() {
  const [liveEvents, setLiveEvents] = useState([]);

  useEffect(() => {
    // axios.get('http://localhost:8080/api/liveEvents')
    //   .then((response) => {
    //     console.log("response : ", response);
    //     setLiveEvents(response.data);
    //     console.log("responseData : ", response.data);
    //   })
    //   .catch((error) => {
    //     console.error('Error fetching live events:', error);
    //   });

    setLiveEvents(LIVEEVENTS);
  },[])
  return (
    <LiveEvents liveEvents={liveEvents} />
  )
}

export default Main