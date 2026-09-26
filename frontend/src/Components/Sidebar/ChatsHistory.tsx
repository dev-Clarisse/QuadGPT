import { Icon, List, Text } from '@chakra-ui/react'
import React from 'react'
import { FiMessageSquare } from 'react-icons/fi'

type ChatsHistoryProps = {
  titles: string[]
  handleSelectExistingChat: (title: string) => void
}

const ChatsHistory: React.FC<ChatsHistoryProps> = ({
  titles,
  handleSelectExistingChat,
}) => {
  return (
    <List.Root mt={4} gap={1} listStyleType="none">
      {titles?.map((title) => (
        <List.Item
          key={title}
          display="flex"
          alignItems="center"
          p="10px 12px"
          borderRadius="8px"
          color="#c5c5d2"
          _hover={{ bg: '#2A2B32', color: '#ffffff' }}
          cursor="pointer"
          transition="all 0.15s ease"
          onClick={() => handleSelectExistingChat(title)}
        >
          <Icon as={FiMessageSquare} w={4} h={4} mr={3} color="#aa3bff" />
          <Text
            overflow="hidden"
            textOverflow="ellipsis"
            whiteSpace="nowrap"
            fontSize="14px"
            flexGrow={1}
          >
            {title}
          </Text>
        </List.Item>
      ))}
    </List.Root>
  )
}

export default ChatsHistory