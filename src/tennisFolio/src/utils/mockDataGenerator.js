const random = {
  arrayElement: (arr) => arr[Math.floor(Math.random() * arr.length)],
  number: (min, max) => Math.floor(Math.random() * (max - min + 1)) + min,
  boolean: () => Math.random() < 0.5,
};

const TOURNAMENTS = [
  'ATP Masters 1000 Miami',
  'French Open',
  'Wimbledon',
  'US Open',
  'Australian Open',
  'ATP 500 Dubai',
  'ATP Masters 1000 Indian Wells',
];

const PLAYERS = [
  { name: 'Novak Djokovic', image: 'player1.jpg' },
  { name: 'Carlos Alcaraz', image: 'player2.jpg' },
  { name: 'Daniil Medvedev', image: 'player3.jpg' },
  { name: 'Jannik Sinner', image: 'player4.jpg' },
  { name: 'Andrey Rublev', image: 'player5.jpg' },
  { name: 'Stefanos Tsitsipas', image: 'player6.jpg' },
  { name: 'Alexander Zverev', image: 'player7.jpg' },
  { name: 'Holger Rune', image: 'player8.jpg' },
  { name: 'Taylor Fritz', image: 'player9.jpg' },
  { name: 'Tommy Paul', image: 'player10.jpg' },
];

const ROUNDS = [
  'Final',
  'Semifinals',
  'Quarterfinals',
  'Fourth Round',
  'Third Round',
];
const STATUSES = ['Live', 'In Progress', '1st Set', '2nd Set', '3rd Set'];

const COUNTRIES = [
  { name: 'Serbia', alpha: 'RS' },
  { name: 'Spain', alpha: 'ES' },
  { name: 'Russia', alpha: 'RU' },
  { name: 'Italy', alpha: 'IT' },
  { name: 'Greece', alpha: 'GR' },
  { name: 'Germany', alpha: 'DE' },
  { name: 'Denmark', alpha: 'DK' },
  { name: 'USA', alpha: 'US' },
];

export function generateLiveEvent() {
  const player1 = random.arrayElement(PLAYERS);
  const player2 = random.arrayElement(
    PLAYERS.filter((p) => p.name !== player1.name)
  );
  const player1Country = random.arrayElement(COUNTRIES);
  const player2Country = random.arrayElement(
    COUNTRIES.filter((c) => c !== player1Country)
  );

  // 세트 스코어 생성 (항상 5세트 구조, 진행되지 않은 세트는 빈 값)
  const completedSets = random.number(1, 4); // 완료된 세트 수 (1-4개)
  const currentSetInProgress = random.boolean(); // 현재 세트 진행 중인지

  const homeScores = [];
  const awayScores = [];

  // 완료된 세트들
  for (let i = 0; i < completedSets; i++) {
    homeScores.push(random.number(0, 7));
    awayScores.push(random.number(0, 7));
  }

  // 현재 진행 중인 세트 (있다면)
  if (currentSetInProgress && completedSets < 5) {
    homeScores.push(random.number(0, 6)); // 진행 중이므로 6 이하
    awayScores.push(random.number(0, 6));
  }

  // 나머지 세트는 빈 값으로 채우기 (항상 총 5개가 되도록)
  while (homeScores.length < 5) {
    homeScores.push(''); // 빈 문자열
    awayScores.push('');
  }

  // 세트 스코어 문자열 생성 (완료된 세트들만)
  const completedSetScores = [];
  for (let i = 0; i < completedSets; i++) {
    completedSetScores.push(`${homeScores[i]}-${awayScores[i]}`);
  }
  let setScoreString = completedSetScores.join(', ');

  // 현재 진행 중인 세트가 있으면 추가
  if (currentSetInProgress && completedSets < 5) {
    if (setScoreString) setScoreString += ', ';
    setScoreString += `${homeScores[completedSets]}-${awayScores[completedSets]}`;
  }

  // 포인트 스코어 생성
  const homePoint = random.arrayElement(['0', '15', '30', '40', 'ADV']);
  const awayPoint = random.arrayElement(['0', '15', '30', '40', 'ADV']);

  return {
    rapidId: random.number(10000000, 99999999),
    tournamentName: random.arrayElement(TOURNAMENTS),
    roundName: random.arrayElement(ROUNDS),
    status: random.arrayElement(STATUSES),

    // Home Player (Player 1)
    homePlayer: {
      playerName: player1.name,
      playerRanking: random.number(1, 100),
      playerImage: player1.image,
      playerCountryAlpha: player1Country.alpha,
      name: player1.name, // fallback
    },

    // Away Player (Player 2)
    awayPlayer: {
      playerName: player2.name,
      playerRanking: random.number(1, 100),
      playerImage: player2.image,
      playerCountryAlpha: player2Country.alpha,
      name: player2.name, // fallback
    },

    // Home Score
    homeScore: {
      current:
        currentSetInProgress && completedSets < 5
          ? homeScores[completedSets]
          : 0, // 현재 세트의 게임 수
      periodScore: homeScores,
      point: homePoint, // pointScore 영역에 표시될 포인트 스코어
    },

    // Away Score
    awayScore: {
      current:
        currentSetInProgress && completedSets < 5
          ? awayScores[completedSets]
          : 0, // 현재 세트의 게임 수
      periodScore: awayScores,
      point: awayPoint, // pointScore 영역에 표시될 포인트 스코어
    },
  };
}

export function generateLiveEvents(count = 5) {
  return Array.from({ length: count }, () => generateLiveEvent());
}
