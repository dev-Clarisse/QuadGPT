import { ChakraProvider } from '@chakra-ui/react'
import React, { useState } from 'react'
import ReactDOM from 'react-dom/client'
import Connexion from './Components/Connexion/Connexion.tsx'
import App from './App.tsx'
import system from './theme/theme.ts'

export function MainApp() {
  const [session, setSession] = useState<{ username: string; token: string } | null>(() => {
    const username = localStorage.getItem('currentUser')
    const token = localStorage.getItem('authToken')
    return username && token ? { username, token } : null
  });

  const handleLogin = (username: string, token: string) => {
    localStorage.setItem('currentUser', username)
    localStorage.setItem('authToken', token)
    setSession({ username, token })
  };

  const handleLogout = () => {
    localStorage.removeItem('currentUser')
    localStorage.removeItem('authToken')
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