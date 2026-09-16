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
}

const ChatArea: React.FC<ChatAreaProps> = ({
  currentTitle,
  messageContent,
  setMessageContent,
  handleSendMessage,
  previousMessages,
}) => {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  return (
    <Flex direction="column" w="full" p="24px">
      <Messages
        currentTitle={currentTitle}
        previousMessages={previousMessages}
      />

      <Box px="16px">
        <Group attached w="full">
          <Input
            placeholder="Ask anything..."
            bg="#40414f"
            border="1px solid"
            borderColor="white"
            borderLeftRadius="8px"      // ← arrondi gauche
                   // 🔑 supprime la barre vertical     


            p={3}
            value={messageContent}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
              setMessageContent(e.target.value)
            }
            onKeyDown={handleKeyDown}
            _placeholder={{ color: "white" }}
          />
          <IconButton
            aria-label="Send message"
            variant="ghost"
            onClick={handleSendMessage}
            color="#8e8ea0"
            _hover={{ bg: 'transparent', color: '#fff' }}
          >
            <FiSend size={16} />
          </IconButton>
        </Group>
      </Box>

      <Box px="16px" pt="24px">
        <Text color="rgba(255,255,255,.5)" fontSize="12px" textAlign="center">
          © 2026 QuadGPT. All rights reserved.
        </Text>
      </Box>
    </Flex>
  )
}

export default ChatArea