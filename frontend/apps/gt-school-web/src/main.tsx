import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import './index.css'
import './styles/gt-design-system.css'

import App from './App.tsx'
import { AuthProvider } from './auth/authContext'


createRoot(document.getElementById('root')!).render(

  <StrictMode>

    <BrowserRouter>

      <AuthProvider>

        <App />

      </AuthProvider>

    </BrowserRouter>

  </StrictMode>,

)
