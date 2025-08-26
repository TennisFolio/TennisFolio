import { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import { Provider } from 'react-redux';
import store from './store/store';
import { HelmetProvider } from 'react-helmet-async';
import './reset.css';

// MSW 초기화 (개발 모드에서만)
async function initApp() {
  //import.meta.env.MODE          // 'development' | 'production'
  //import.meta.env.DEV           // true (개발 시) | false (배포 시)
  //import.meta.env.PROD          // false (개발 시) | true (배포 시)
  //import.meta.env.BASE_URL      // 기본 URL
  if (import.meta.env.MODE === 'development') {
    const { enableMocking } = await import('./mocks/browser');
    await enableMocking();
  }

  ReactDOM.createRoot(document.getElementById('root')).render(
    <StrictMode>
      <Provider store={store}>
        <HelmetProvider>
          <App />
        </HelmetProvider>
      </Provider>
    </StrictMode>
  );
}

initApp();
