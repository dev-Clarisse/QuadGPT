import { Box, Flex, Heading, Text, Spinner } from '@chakra-ui/react'
import React, { useEffect, useRef } from 'react'
import { type Message } from '../../App'

type MessagesProps = {
  currentTitle: string
  previousMessages: Message[]
  isLoading?: boolean 
}

const Messages: React.FC<MessagesProps> = ({
  currentTitle,
  previousMessages,
  isLoading = false,
}) => {
  const currentChat = previousMessages.filter(
    (prev) => prev.title === currentTitle
  )

  // 1. Déclarer la référence pour l'élément tout en bas
  const messagesEndRef = useRef<HTMLDivElement | null>(null)

  // 2. Déclencher le défilement automatique
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [currentChat, isLoading])

  return (
    <Flex direction="column" h="full" w="full" overflowY="auto" mb={4} gap={4}>
      
      {!currentTitle && (
        <Heading
          fontSize="36px"
          fontWeight={800}
          textAlign="center"
          letterSpacing="tighter"
          color="#ececf1"
          w="full"
          mt="20%"
        >
          QuadGPT
        </Heading>
      )}

      {currentTitle && (
        <Flex direction="column" gap={3} px={4}>
          {currentChat?.map((message, index) => {
            const isUser = message.role === 'user'
            return (
              <Box
                key={index}
                alignSelf={isUser ? 'flex-end' : 'flex-start'}
                maxW="80%"
                bg={isUser ? '#aa3bff' : '#444654'}
                color="#ffffff"
                p="12px 16px"
                borderRadius="12px"
                borderBottomRightRadius={isUser ? '2px' : '12px'}
                borderBottomLeftRadius={!isUser ? '2px' : '12px'}
                boxShadow="0 2px 8px rgba(0, 0, 0, 0.15)"
              >
                <Text fontSize="11px" fontWeight={700} opacity={0.7} mb={1}>
                  {isUser ? 'YOU' : 'QUADGPT'}
                </Text>
                <Text fontSize="14px" lineHeight="1.5">
                  {message.content}
                </Text>
              </Box>
            )
          })}

          {isLoading && (
            <Box
              alignSelf="flex-start"
              bg="#444654"
              color="#ffffff"
              p="12px 16px"
              borderRadius="12px"
              borderBottomLeftRadius="2px"
              display="flex"
              alignItems="center"
              gap={2}
            >
              <Text fontSize="11px" fontWeight={700} opacity={0.7}>
                QUADGPT
              </Text>
              <Spinner size="xs" color="#aa3bff" />
            </Box>
          )}

          {/* 3. Ancre invisible placée tout en bas des messages */}
          <div ref={messagesEndRef} />
        </Flex>
      )}
    </Flex>
  )
}

export default Messages