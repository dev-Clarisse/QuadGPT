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
    <List.Root mt={3} gap={3} listStyleType="none">
      {titles?.map((title) => (
        <List.Item
          key={title}
          display="flex"
          alignItems="center"
          p={3}
          borderRadius="6px"
          _hover={{ bg: '#2A2B32', pr: 4 }}
          cursor="pointer"
          wordBreak="break-all"
          onClick={() => handleSelectExistingChat(title)}
        >
          <Icon as={FiMessageSquare} w={4} h={4} mr={3} />
          <Text
            overflow="hidden"
            textOverflow="ellipsis"
            whiteSpace="nowrap"
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