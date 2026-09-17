import { Box, Button, Flex, Text } from '@chakra-ui/react'
import React from 'react'
import { User } from 'lucide-react'

const SidebarFooter: React.FC = () => {
  return (
    <Flex direction="column" align="flex-start" width="full" pt={2}>
      <Button
        variant="ghost"
        display="flex"
        alignItems="center"
        justifyContent="flex-start"
        p={2}
        borderRadius="8px"
        _hover={{ bg: '#2A2B32' }}
        w="full"
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
          claire.chabas@gmail.com
        </Text>
      </Button>
    </Flex>
  )
}

export default SidebarFooter