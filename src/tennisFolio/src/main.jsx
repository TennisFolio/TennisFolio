import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import {Provider} from 'react-redux'
import store from './store/store';
import { HelmetProvider } from 'react-helmet-async'


const root = ReactDOM.createRoot(document.getElementById('root')).render(
      <Provider store={store}>
            <HelmetProvider>
                  <App />,
            </HelmetProvider>
      </Provider>
)
