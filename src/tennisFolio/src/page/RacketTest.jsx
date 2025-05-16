import React from 'react'

function RacketTest() {
  const{testParam} = useParams();
  const navigate = useNavigate();
  const[currentTest, setCurrentTest] = useState({});

  useEffect(() => {
      // 1. testParam이 우리 DB에 존재하는가 필터링
      // 1-1. 존재 X -> 안내/Home routing
      // 1-2. 존재 O -> 해당 테스트의 콘텐츠를 렌더링
      const theTest = TESTS?.find((test) => (test?.info?.mainUrl === testParam));
      if(!theTest){
          alert('해당 테스트는 존재하지 않습니다!');
          return  navigate('/');
      }
      console.log(theTest);
      setCurrentTest(theTest);
  }, [testParam, navigate]);

  return( 
  <div>
      <IntroRenderer currentTest = {currentTest}/>
  </div>
  );
}

export default RacketTest