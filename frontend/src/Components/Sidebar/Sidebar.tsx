import { Button, Flex, Icon, Text } from '@chakra-ui/react'
import React from 'react'
import { AiOutlinePlus } from 'react-icons/ai'
import { type Conversation } from '../../App'
import ChatsHistory from './ChatsHistory'
import SidebarFooter from './SidebarFooter'

type SidebarProps = {
  handleCreateNewChat: () => void
  handleSelectExistingChat: (conversation: Conversation) => void
  handleDeleteChat: (conversation: Conversation) => void
  conversations: Conversation[]
  activeConversationId: number | null
  username?: string;
  onLogout?: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({
  conversations,
  activeConversationId,
  handleCreateNewChat,
  handleSelectExistingChat,
  handleDeleteChat,
  username,
  onLogout,
}) => {
  return (
    <Flex
      direction="column"
      shrink={0}
      bg="#202123"
      width="260px"
      h="100vh"
      display={{ base: 'none', md: 'flex' }}
      borderRight="1px solid rgba(255,255,255,0.05)"
    >
      <Flex direction="column" justify="space-between" h="full" p={3} minH={0}>
        <Flex direction="column" minH={0}>
          <Button
            display="flex"
            alignItems="center"
            justifyContent="flex-start"
            bg="transparent"
            border="1px solid"
            borderColor="rgba(255, 255, 255, 0.15)"
            borderRadius={8}
            p={3}
            color="#ececf1"
            fontWeight={600}
            fontSize={14}
            _hover={{ bg: '#2A2B32', borderColor: '#aa3bff' }}
            transition="all 0.2s ease"
            onClick={handleCreateNewChat}
            flexShrink={0}
          >
            <Icon as={AiOutlinePlus} mr={3} color="#aa3bff" />
            <Text fontSize="0.875rem">New chat</Text>
          </Button>

          <ChatsHistory
            conversations={conversations}
            activeConversationId={activeConversationId}
            handleSelectExistingChat={handleSelectExistingChat}
            handleDeleteChat={handleDeleteChat}
          />
        </Flex>

        <SidebarFooter userEmail={username} onLogout={onLogout} />
      </Flex>
    </Flex>
  )
}

export default Sidebar
