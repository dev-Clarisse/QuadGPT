import { Icon, IconButton, List, Text } from '@chakra-ui/react'
import React from 'react'
import { FiMessageSquare, FiTrash2 } from 'react-icons/fi'
import { type Conversation } from '../../App'

type ChatsHistoryProps = {
  conversations: Conversation[]
  activeConversationId: number | null
  handleSelectExistingChat: (conversation: Conversation) => void
  handleDeleteChat: (conversation: Conversation) => void
}

const ChatsHistory: React.FC<ChatsHistoryProps> = ({
  conversations,
  activeConversationId,
  handleSelectExistingChat,
  handleDeleteChat,
}) => {
  return (
    <List.Root mt={4} gap={1} listStyleType="none" overflowY="auto" minH={0}>
      {conversations.map((conversation) => {
        const isActive = conversation.id === activeConversationId
        return (
          <List.Item
            key={conversation.id}
            className="group"
            display="flex"
            alignItems="center"
            p="10px 12px"
            borderRadius="8px"
            bg={isActive ? '#343541' : 'transparent'}
            color={isActive ? '#ffffff' : '#c5c5d2'}
            _hover={{ bg: '#2A2B32', color: '#ffffff' }}
            cursor="pointer"
            transition="all 0.15s ease"
            onClick={() => handleSelectExistingChat(conversation)}
          >
            <Icon as={FiMessageSquare} w={4} h={4} mr={3} color="#aa3bff" flexShrink={0} />
            <Text
              overflow="hidden"
              textOverflow="ellipsis"
              whiteSpace="nowrap"
              fontSize="14px"
              flexGrow={1}
            >
              {conversation.title}
            </Text>
            <IconButton
              aria-label="Delete conversation"
              title="Delete conversation"
              size="2xs"
              variant="ghost"
              color="#8e8ea0"
              opacity={isActive ? 1 : 0}
              _groupHover={{ opacity: 1 }}
              _hover={{ color: '#ff4d4d', bg: 'transparent' }}
              onClick={(e) => {
                e.stopPropagation()
                handleDeleteChat(conversation)
              }}
              flexShrink={0}
            >
              <FiTrash2 size={13} />
            </IconButton>
          </List.Item>
        )
      })}
    </List.Root>
  )
}

export default ChatsHistory
