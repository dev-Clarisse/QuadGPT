import { Box, Button, Flex, Text } from '@chakra-ui/react'
import React from 'react'
import { User, LogOut } from 'lucide-react'


interface SidebarFooterProps {
  userEmail?: string;
  onLogout?: () => void
}


const SidebarFooter: React.FC<SidebarFooterProps> = ({ userEmail = 'utilisateur@email.com', onLogout }) => {
  return (
    <Flex direction="row" align="center" justify="space-between" width="full" pt={2} gap={1}>
      
      <Button
        variant="ghost"
        display="flex"
        alignItems="center"
        justifyContent="flex-start"
        p={2}
        borderRadius="8px"
        _hover={{ bg: '#2A2B32' }}
        flex="1"
        minW={0} 
        gap={3}
      >
        <Box
          display="flex"
          alignItems="center"
          justifyContent="center"
          bg="#aa3bff"
          color="#ffffff"
          borderRadius="full"
          boxSize="32px"
          flexShrink={0}
        >
          <User size={18} />
        </Box>
        <Text
          color="#ececf1"
          fontSize="13px"
          fontWeight={500}
          overflow="hidden"
          textOverflow="ellipsis"
          whiteSpace="nowrap"
        >
          {userEmail}
        </Text>
      </Button>

      <Button
        variant="ghost"
        p={2}
        borderRadius="8px"
        _hover={{ bg: '#2A2B32', color: '#ff4d4d' }}
        color="#8e8ea0"
        onClick={onLogout}
        title="Log out"
        flexShrink={0}
      >
        <LogOut size={18} />
      </Button>
    </Flex>
  )
}

export default SidebarFooter