const ANALYTICS_DEBUG = import.meta.env.DEV;
const GA_MEASUREMENT_ID = 'G-BVKP7NB8Z6';
const ANALYTICS_HOSTS = new Set(['tennisfolio.net', 'www.tennisfolio.net']);
let isAnalyticsInitialized = false;

function isAnalyticsEnabled() {
  return (
    import.meta.env.PROD &&
    typeof window !== 'undefined' &&
    ANALYTICS_HOSTS.has(window.location.hostname)
  );
}

export function initializeAnalytics() {
  if (!isAnalyticsEnabled() || isAnalyticsInitialized) {
    return;
  }

  window.dataLayer = window.dataLayer || [];
  window.gtag = function gtag() {
    window.dataLayer.push(arguments);
  };

  window.gtag('js', new Date());
  window.gtag('config', GA_MEASUREMENT_ID, { send_page_view: false });

  const script = document.createElement('script');
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
  document.head.appendChild(script);
  isAnalyticsInitialized = true;
}

function getSearchParam(name) {
  return new URLSearchParams(window.location.search).get(name) || undefined;
}

function getLandingPath() {
  const key = 'tf_landing_path';
  const current = `${window.location.pathname}${window.location.search}`;
  const saved = sessionStorage.getItem(key);

  if (saved) {
    return saved;
  }

  sessionStorage.setItem(key, current);
  return current;
}

function getTrafficParams() {
  return {
    utm_source: getSearchParam('utm_source'),
    utm_medium: getSearchParam('utm_medium'),
    utm_campaign: getSearchParam('utm_campaign'),
    utm_content: getSearchParam('utm_content'),
    utm_term: getSearchParam('utm_term'),
    referrer: document.referrer || undefined,
    landing_path: getLandingPath(),
  };
}

function cleanParams(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null)
  );
}

export function trackEvent(eventName, params = {}) {
  if (!isAnalyticsEnabled()) {
    if (ANALYTICS_DEBUG) {
      console.debug('[analytics:disabled]', eventName, params);
    }
    return;
  }
  initializeAnalytics();

  const eventParams = cleanParams({
    ...getTrafficParams(),
    page_path: `${window.location.pathname}${window.location.search}`,
    ...params,
  });

  if (typeof window.gtag === 'function') {
    window.gtag('event', eventName, eventParams);
  }

  if (ANALYTICS_DEBUG) {
    console.debug('[analytics]', eventName, eventParams);
  }
}

export function trackPageView(path) {
  if (!isAnalyticsEnabled()) {
    return;
  }
  initializeAnalytics();

  if (typeof window.gtag !== 'function') {
    return;
  }

  window.gtag('event', 'page_view', {
    page_path: path,
    page_location: window.location.href,
    ...getTrafficParams(),
  });
}

export function incrementSessionCompetitionCreateCount() {
  const key = 'tf_session_competition_create_count';
  const nextCount = Number(sessionStorage.getItem(key) || 0) + 1;
  sessionStorage.setItem(key, String(nextCount));
  return nextCount;
}

export function markCompetitionRevisit(publicId) {
  if (!publicId) {
    return false;
  }

  const visitKey = `tf_competition_visited:${publicId}`;
  const sessionKey = `tf_competition_revisit_tracked:${publicId}`;
  const hasVisitedBefore = localStorage.getItem(visitKey) === '1';
  const alreadyTrackedInSession = sessionStorage.getItem(sessionKey) === '1';

  localStorage.setItem(visitKey, '1');

  if (!hasVisitedBefore || alreadyTrackedInSession) {
    return false;
  }

  sessionStorage.setItem(sessionKey, '1');
  return true;
}
