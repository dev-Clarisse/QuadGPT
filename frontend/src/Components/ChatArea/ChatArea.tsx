import {
  Box,
  Flex,
  Group,
  IconButton,
  Input,
  Text,
} from '@chakra-ui/react'
import React from 'react'
import { FiSend } from 'react-icons/fi'
import { type Message } from '../../App'
import Messages from './Messages'

type ChatAreaProps = {
  currentTitle: string
  messageContent: string
  setMessageContent: (value: string) => void
  handleSendMessage: () => void
  previousMessages: Message[]
  isLoading?: boolean
}

const ChatArea: React.FC<ChatAreaProps> = ({
  currentTitle,
  messageContent,
  setMessageContent,
  handleSendMessage,
  previousMessages,
  isLoading,
}) => {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  return (
    <Flex direction="column" w="full" h="100vh" bg="#343541" p="24px">
      <Messages
        currentTitle={currentTitle}
        previousMessages={previousMessages}
        isLoading={isLoading}
      />

      <Box px="16px">
        <Group attached w="full">
          <Input
            placeholder="Ask anything..."
            bg="#444654"
            color="#ececf1"
            border="1px solid"
            borderColor="rgba(255, 255, 255, 0.15)"
            borderLeftRadius="8px"
            p={3}
            value={messageContent}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
              setMessageContent(e.target.value)
            }
            onKeyDown={handleKeyDown}
            _placeholder={{ color: '#8e8ea0' }}
            _focus={{
              borderColor: '#aa3bff',
              boxShadow: '0 0 0 1px #aa3bff',
            }}
          />
          <IconButton
            aria-label="Send message"
            variant="ghost"
            onClick={handleSendMessage}
            bg="#aa3bff"
            color="#ffffff"
            borderRightRadius="8px"
            _hover={{ bg: '#9322eb' }}
          >
            <FiSend size={16} />
          </IconButton>
        </Group>
      </Box>

      <Box px="16px" pt="16px">
        <Text color="#8e8ea0" fontSize="12px" textAlign="center">
          © 2026 QuadGPT. All rights reserved.
        </Text>
      </Box>
    </Flex>
  )
}

export default ChatArea