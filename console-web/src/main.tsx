import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { DebugContextProvider } from './auth/DebugContextProvider';
import App from './App';
import { DemoRoleProvider } from './rbac/DemoRoleProvider';
import './styles/global.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <DebugContextProvider>
      <DemoRoleProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </DemoRoleProvider>
    </DebugContextProvider>
  </StrictMode>,
);
