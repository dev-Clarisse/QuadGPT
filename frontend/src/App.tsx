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
  accessToken: string
  onLogout?: () => void
}

const App: React.FC<AppProps> = ({ username, accessToken, onLogout }) => {
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

    try {
      const response = await fetch('/api/test/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
          Authorization: `Bearer ${accessToken}`,
        },
        body: userText,
      })

      if (!response.ok) {
        throw new Error((await response.text()) || 'The message could not be sent.')
      }

      const assistantResponse = await response.text()
      setPreviousMessages((prev) => [
        ...prev,
        { title, role: 'assistant', content: assistantResponse },
      ])
    } catch (requestError) {
      setPreviousMessages((prev) => [
        ...prev,
        {
          title,
          role: 'assistant',
          content: requestError instanceof Error
            ? requestError.message
            : 'Unable to connect to the server.',
        },
      ])
    } finally {
      setIsLoading(false)
    }
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