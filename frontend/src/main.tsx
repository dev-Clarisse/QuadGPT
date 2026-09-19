import { ChakraProvider } from '@chakra-ui/react'
import React, { useState } from 'react'
import ReactDOM from 'react-dom/client'
import Connexion from './Components/Connexion/Connexion.tsx'
import App from './App.tsx'
import system from './theme/theme.ts'

function MainApp() {
  const [currentUser, setCurrentUser] = useState<string | null>(() => {
    return localStorage.getItem('currentUser')
  });


  const handleLogin = (username: string) => {
    localStorage.setItem('currentUser', username); 
    setCurrentUser(username);
  };

 
  const handleLogout = () => {
    localStorage.removeItem('currentUser'); 
    localStorage.removeItem('fakeToken');
    setCurrentUser(null);
  };

  if (currentUser) {
    return <App username={currentUser} onLogout={handleLogout} />;
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