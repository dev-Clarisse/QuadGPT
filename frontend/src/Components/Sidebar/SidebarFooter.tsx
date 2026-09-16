import { Button, Flex, Text, Box } from '@chakra-ui/react'
import React from 'react'
import { User } from 'lucide-react'


const SidebarFooter: React.FC = () => {
  return (
    <Flex direction="column" align="flex-start" width="full">
      {/* <Button
        variant="ghost"
        w="full"
        display="flex"
        alignItems="center"
        justifyContent="space-between"
        p={3}
        borderRadius="6px"
        _hover={{ bg: '#2A2B32' }}
        fontWeight={400}
        fontSize="14px"
      >
        <Flex alignItems="center">
          <Icon as={FiUser} mr={3} />
          <Text>Upgrade to Plus</Text>
        </Flex>

        <Tag.Root
          variant="solid"
          borderRadius="6px"
          bg="#fae69e"
          color="#343541"
          size="sm"
          px="6px"
          py="2px"
        >
          <Tag.Label fontWeight="bold">NEW</Tag.Label>
        </Tag.Root>
      </Button> */}

      <Button
        variant="ghost"
        display="flex"
        alignItems="center"
        justifyContent="flex-start"
        p={3}
        borderRadius="6px"
        _hover={{ bg: '#2A2B32' }}
        fontWeight={400}
        fontSize="14px"
        w="full"
      >
        
        <Box
          display="flex"
          alignItems="center"
          justifyContent="center"
          bg="yellow.400"
          color="black"
          borderRadius="full"
          boxSize="32px"       
        >
          <User size={20} />  
        </Box>
        <Text color="white" fontSize="14px">claire.chabas@gmail.com</Text>
      </Button>
    </Flex>
  )
}

export default SidebarFooter