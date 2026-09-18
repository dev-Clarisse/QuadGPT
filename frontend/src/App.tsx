import { Flex } from '@chakra-ui/react'
import React, { useState } from 'react'
import ChatArea from './Components/ChatArea/ChatArea'
import Sidebar from './Components/Sidebar/Sidebar'

export interface Message {
  title: string
  role: 'user' | 'assistant'
  content: string
}

interface AppProps {
  username?: string
  onLogout?: () => void
}

const App: React.FC<AppProps> = ({ username, onLogout }) => {
  const [currentTitle, setCurrentTitle] = useState('')
  const [previousMessages, setPreviousMessages] = useState<Message[]>([])
  const [messageContent, setMessageContent] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const createNewChat = () => {
    setMessageContent('')
    setCurrentTitle('')
  }

  const sendMessage = async () => {
    if (!messageContent.trim()) return

    const userText = messageContent
    const title = currentTitle || userText

    if (!currentTitle) {
      setCurrentTitle(userText)
    }

    setPreviousMessages((prev) => [
      ...prev,
      { title: title, role: 'user', content: userText },
    ])

    setMessageContent('')
    setIsLoading(true)

    setTimeout(() => {
      const mockResponses = [
        `Bonjour ! C'est une réponse de démonstration pour : "${userText}".`,
        `Je suis QuadGPT en mode simulation ! Votre backend prendra bientôt le relais.`,
        `Merci pour votre message ! Une fois le backend connecté, je générerai de vraies réponses IA.`,
      ]
      const randomResponse =
        mockResponses[Math.floor(Math.random() * mockResponses.length)]

      setPreviousMessages((prev) => [
        ...prev,
        { title: title, role: 'assistant', content: randomResponse },
      ])
      setIsLoading(false)
    }, 1000)
  }

  const selectExistingChat = (title: string) => {
    setCurrentTitle(title)
    setMessageContent('')
  }

  return (
    <Flex width="full" height="full">
      <Sidebar
        handleSelectExistingChat={selectExistingChat}
        previousMessages={previousMessages}
        handleCreateNewChat={createNewChat}
        username={username}
        onLogout={onLogout}
      />

      <ChatArea
        previousMessages={previousMessages}
        currentTitle={currentTitle}
        messageContent={messageContent}
        setMessageContent={setMessageContent}
        handleSendMessage={sendMessage}
        isLoading={isLoading} 
      />
    </Flex>
  )
}

export default App