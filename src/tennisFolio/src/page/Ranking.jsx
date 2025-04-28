import React from 'react'
import { useState } from 'react';
import { useEffect } from 'react';
import axios from 'axios';
import RankingTable from '../components/ranking/rankingTable'; 

function Ranking() {
  const [rankings, setRankings] = useState([]);
   useEffect(() => {
      axios.get('http://localhost:8080/api/ranking')
        .then((response) => {
          console.log("response : ", response);
          setRankings(response.data);
          console.log("responseData : ", response.data);
        })
        .catch((error) => {
          console.error('Error fetching live events:', error);
        });
      
    },[])
  return (
    <RankingTable />
  )
}

export default Ranking