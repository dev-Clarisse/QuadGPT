import { ChakraProvider } from '@chakra-ui/react'
import React, { useState } from 'react'
import ReactDOM from 'react-dom/client'
import Connexion from './Components/Connexion/Connexion.tsx'
import App from './App.tsx'
import system from './theme/theme.ts'

export function MainApp() {
  const [session, setSession] = useState<{ username: string; token: string } | null>(() => {
    const username = sessionStorage.getItem('currentUser')
    const token = sessionStorage.getItem('authToken')
    return username && token ? { username, token } : null
  });

  const handleLogin = (username: string, token: string) => {
    sessionStorage.setItem('currentUser', username)
    sessionStorage.setItem('authToken', token)
    setSession({ username, token })
  };

  const handleLogout = () => {
    sessionStorage.removeItem('currentUser')
    sessionStorage.removeItem('authToken')
    setSession(null)
  };

  if (session) {
    return <App username={session.username} accessToken={session.token} onLogout={handleLogout} />;
  }

  return <Connexion onLogin={handleLogin} />;
}

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ChakraProvider value={system}>
      <MainApp />
    </ChakraProvider>
  </React.StrictMode>
)