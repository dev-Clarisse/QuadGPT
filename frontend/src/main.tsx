import { ChakraProvider } from '@chakra-ui/react'
import React from 'react'
import ReactDOM from 'react-dom/client'
import Connexion from './Components/Connexion/Connexion.tsx'
// import App from './App.tsx'
import system from './theme/theme.ts'

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ChakraProvider value={system}>
      <Connexion />
    </ChakraProvider>
  </React.StrictMode>
)