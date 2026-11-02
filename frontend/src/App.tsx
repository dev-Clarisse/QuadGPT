import { Flex, Text } from '@chakra-ui/react'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import ChatArea from './Components/ChatArea/ChatArea'
import { useConfirm } from './Components/ConfirmDialog/useConfirm'
import DocumentsPage from './Components/Documents/DocumentsPage'
import Sidebar from './Components/Sidebar/Sidebar'

export interface Message {
  role: 'user' | 'assistant'
  content: string
}

export interface Conversation {
  id: number
  title: string
  updatedAt: string
}

interface AppProps {
  username?: string
  accessToken: string
  onLogout?: () => void
}

const getTokenClaims = (token: string): { role?: string; department?: string } => {
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return {}
  }
}

const App: React.FC<AppProps> = ({ username, accessToken, onLogout }) => {
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [currentConversationId, setCurrentConversationId] = useState<number | null>(null)
  const [currentTitle, setCurrentTitle] = useState('')
  const [messages, setMessages] = useState<Message[]>([])
  const [messageContent, setMessageContent] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [view, setView] = useState<'chat' | 'documents'>('chat')
  const claims = useMemo(() => getTokenClaims(accessToken), [accessToken])
  const isAdmin = claims.role === 'ADMIN'
  const { confirm, confirmDialog } = useConfirm()

  // Incremented whenever the displayed chat changes, so a late answer
  // is not appended to a conversation the user has since left.
  const chatSessionRef = useRef(0)

  const authHeaders = useMemo(() => ({ Authorization: `Bearer ${accessToken}` }), [accessToken])

  const loadConversations = useCallback(async () => {
    try {
      const response = await fetch('/api/conversations', { headers: authHeaders })
      if (response.ok) {
        setConversations((await response.json()) as Conversation[])
      }
    } catch {
      // The sidebar simply stays empty if the history cannot be loaded.
    }
  }, [authHeaders])

  useEffect(() => {
    loadConversations()
  }, [loadConversations])

  const createNewChat = () => {
    chatSessionRef.current += 1
    setView('chat')
    setMessageContent('')
    setCurrentConversationId(null)
    setCurrentTitle('')
    setMessages([])
    setIsLoading(false)
  }

  const sendMessage = async () => {
    if (!messageContent.trim() || isLoading) return

    const userText = messageContent
    const session = chatSessionRef.current

    if (!currentTitle) {
      setCurrentTitle(userText)
    }

    setMessages((prev) => [...prev, { role: 'user', content: userText }])
    setMessageContent('')
    setIsLoading(true)

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders,
        },
        body: JSON.stringify({
          message: userText,
          conversationId: currentConversationId,
        }),
      })

      if (!response.ok) {
        throw new Error((await response.text()) || 'The message could not be sent.')
      }

      const data = (await response.json()) as {
        response: string
        conversationId: number
        title: string
      }

      if (session === chatSessionRef.current) {
        setCurrentConversationId(data.conversationId)
        setCurrentTitle(data.title)
        setMessages((prev) => [...prev, { role: 'assistant', content: data.response }])
      }
      loadConversations()
    } catch (requestError) {
      if (session === chatSessionRef.current) {
        setMessages((prev) => [
          ...prev,
          {
            role: 'assistant',
            content: requestError instanceof Error
              ? requestError.message
              : 'Unable to connect to the server.',
          },
        ])
      }
    } finally {
      if (session === chatSessionRef.current) {
        setIsLoading(false)
      }
    }
  }

  const selectExistingChat = async (conversation: Conversation) => {
    chatSessionRef.current += 1
    const session = chatSessionRef.current

    setView('chat')
    setMessageContent('')
    setCurrentConversationId(conversation.id)
    setCurrentTitle(conversation.title)
    setMessages([])
    setIsLoading(true)

    try {
      const response = await fetch(`/api/conversations/${conversation.id}/messages`, {
        headers: authHeaders,
      })
      if (!response.ok) {
        throw new Error((await response.text()) || 'Unable to load this conversation.')
      }
      const history = (await response.json()) as Message[]
      if (session === chatSessionRef.current) {
        setMessages(history.map(({ role, content }) => ({ role, content })))
      }
    } catch (requestError) {
      if (session === chatSessionRef.current) {
        setMessages([
          {
            role: 'assistant',
            content: requestError instanceof Error
              ? requestError.message
              : 'Unable to load this conversation.',
          },
        ])
      }
    } finally {
      if (session === chatSessionRef.current) {
        setIsLoading(false)
      }
    }
  }

  const deleteChat = async (conversation: Conversation) => {
    const confirmed = await confirm({
      title: 'Delete conversation',
      message: (
        <Text>
          Delete <Text as="span" fontWeight="600" color="#ececf1">{conversation.title}</Text>? Its
          messages will be permanently removed.
        </Text>
      ),
    })
    if (!confirmed) return

    const response = await fetch(`/api/conversations/${conversation.id}`, {
      method: 'DELETE',
      headers: authHeaders,
    })
    if (!response.ok) return

    if (conversation.id === currentConversationId) {
      createNewChat()
    }
    setConversations((prev) => prev.filter((item) => item.id !== conversation.id))
  }

  return (
    <Flex width="full" height="full">
      <Sidebar
        conversations={conversations}
        activeConversationId={currentConversationId}
        handleSelectExistingChat={selectExistingChat}
        handleDeleteChat={deleteChat}
        handleCreateNewChat={createNewChat}
        username={username}
        onLogout={onLogout}
      />

      {view === 'documents' ? (
        <DocumentsPage
          accessToken={accessToken}
          isAdmin={isAdmin}
          userDepartment={claims.department ?? null}
          onBack={() => setView('chat')}
        />
      ) : (
        <ChatArea
          previousMessages={messages}
          currentTitle={currentTitle}
          accessToken={accessToken}
          messageContent={messageContent}
          setMessageContent={setMessageContent}
          handleSendMessage={sendMessage}
          isLoading={isLoading}
          canUploadDocuments={isAdmin}
          onOpenDocuments={() => setView('documents')}
        />
      )}

      {confirmDialog}
    </Flex>
  )
}

export default App
