import React from 'react'
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function Main() {
    const navigate = useNavigate();
    useEffect(() => {
    console.log('Main component mounted');
    navigate('/live/atp');
    }, []);

  return (
    <div>Main</div>
  )
}

export default Main