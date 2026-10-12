import {
  Box,
  Flex,
  IconButton,
  Input,
  NativeSelect,
  Text,
  HStack,
  Spinner,
  Badge,
} from '@chakra-ui/react'
import React, { useRef, useState } from 'react'
import {
  FiFileText,
  FiSend,
  FiPaperclip,
  FiCheckCircle,
  FiAlertCircle,
  FiX,
  FiFile,
} from 'react-icons/fi'
import { type Message } from '../../App'
import Messages from './Messages'

type ChatAreaProps = {
  currentTitle: string
  accessToken: string
  messageContent: string
  setMessageContent: (value: string) => void
  handleSendMessage: () => void
  previousMessages: Message[]
  isLoading?: boolean
}

const DEPARTMENTS = [
  { value: 'RH', label: 'Human Resources (HR)' },
  { value: 'FINANCE', label: 'Finance & Accounting' },
  { value: 'IT', label: 'IT & Engineering' },
  { value: 'MARKETING', label: 'Marketing & Com' },
  { value: 'DIRECTION', label: 'Management' },
  { value: 'SALES', label: 'Sales' },
  { value: 'SUPPORT', label: 'Customer Support' },
]

const ChatArea: React.FC<ChatAreaProps> = ({
  currentTitle,
  accessToken,
  messageContent,
  setMessageContent,
  handleSendMessage,
  previousMessages,
  isLoading,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadStatus, setUploadStatus] = useState<{
    type: 'success' | 'error' | ''
    message: string
    fileName?: string
    dept?: string
  }>({
    type: '',
    message: '',
  })
  const [department, setDepartment] = useState('')

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    if (!department) {
      setUploadStatus({
        type: 'error',
        message: 'Please select a target department before uploading a document.',
      })
      return
    }

    setIsUploading(true)
    setUploadStatus({ type: '', message: '' })

    const formData = new FormData()
    formData.append('file', file)
    formData.append('departments', department)

    try {
      const response = await fetch('/api/rag/ingest-file', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
        body: formData,
      })

      if (!response.ok) {
        throw new Error((await response.text()) || 'The document could not be uploaded.')
      }

      setUploadStatus({
        type: 'success',
        message: 'Uploaded successfully',
        fileName: file.name,
        dept: department,
      })
    } catch (requestError) {
      setUploadStatus({
        type: 'error',
        message:
          requestError instanceof Error
            ? requestError.message
            : 'Unable to upload the document.',
      })
    } finally {
      setIsUploading(false)
      e.target.value = ''
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  return (
    <Flex direction="column" w="full" h="100vh" bg="#343541" p="24px">
      {/* Messages area */}
      <Messages
        currentTitle={currentTitle}
        previousMessages={previousMessages}
        isLoading={isLoading}
      />

      {/* Input container */}
      <Box px="16px" maxW="900px" w="full" mx="auto">
        {/* Document Ingestion Toolbar */}
        <Flex
          bg="#202123"
          p="8px 12px"
          borderRadius="12px 12px 0 0"
          border="1px solid rgba(255, 255, 255, 0.1)"
          borderBottom="none"
          align="center"
          justify="space-between"
          gap="12px"
        >
          <HStack gap="10px" flex="1">
            <FiFileText size={16} color="#aa3bff" />
            <Text fontSize="13px" fontWeight="500" color="#ececf1">
              Ingest Document:
            </Text>

            {/* Department Selection */}
            <NativeSelect.Root size="sm" maxW="230px">
              <NativeSelect.Field
                placeholder="Select Target Department"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                bg="#343541"
                color="#ececf1"
                borderColor={!department ? 'rgba(170, 59, 255, 0.4)' : 'rgba(255, 255, 255, 0.15)'}
                borderRadius="6px"
                fontSize="12px"
                _hover={{ borderColor: '#aa3bff' }}
                _focus={{ borderColor: '#aa3bff', boxShadow: '0 0 0 1px #aa3bff' }}
              >
                {DEPARTMENTS.map((dept) => (
                  <option key={dept.value} value={dept.value} style={{ background: '#343541', color: '#ececf1' }}>
                    {dept.label}
                  </option>
                ))}
              </NativeSelect.Field>
            </NativeSelect.Root>
          </HStack>

          {/* Upload Button */}
          <IconButton
            aria-label="Upload document"
            size="sm"
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading || !department}
            bg={department ? '#aa3bff' : '#444654'}
            color="#ffffff"
            _hover={{ bg: department ? '#9322eb' : '#444654' }}
            px="12px"
          >
            {isUploading ? <Spinner size="xs" /> : <FiPaperclip size={14} />}
          </IconButton>

          <Input
            ref={fileInputRef}
            type="file"
            accept="application/pdf,.pdf"
            display="none"
            onChange={handleFileUpload}
          />
        </Flex>

        {/* Prompt Input Box */}
        <Flex
          bg="#444654"
          borderRadius="0 0 12px 12px"
          border="1px solid rgba(255, 255, 255, 0.1)"
          p="4px"
          align="center"
          boxShadow="0 4px 12px rgba(0,0,0,0.15)"
        >
          <Input
            placeholder="Ask anything..."
            variant="flushed"
            border="none"
            color="#ececf1"
            px="12px"
            py="10px"
            fontSize="14px"
            value={messageContent}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
              setMessageContent(e.target.value)
            }
            onKeyDown={handleKeyDown}
            _placeholder={{ color: '#8e8ea0' }}
            _focus={{ boxShadow: 'none' }}
          />
          <IconButton
            aria-label="Send message"
            onClick={handleSendMessage}
            bg="#aa3bff"
            color="#ffffff"
            borderRadius="8px"
            size="md"
            _hover={{ bg: '#9322eb' }}
            disabled={!messageContent.trim()}
            m="4px"
          >
            <FiSend size={16} />
          </IconButton>
        </Flex>

        {/* Status / Active Document Badge */}
        {uploadStatus.type === 'success' && (
          <Flex
            align="center"
            justify="space-between"
            mt="10px"
            p="6px 12px"
            borderRadius="8px"
            bg="#202123"
            border="1px solid rgba(170, 59, 255, 0.3)"
          >
            <HStack gap="8px">
              <FiCheckCircle size={14} color="#4ade80" />
              <FiFile size={14} color="#aa3bff" />
              <Text fontSize="12px" fontWeight="600" color="#ececf1">
                {uploadStatus.fileName}
              </Text>
              <Badge
                bg="rgba(170, 59, 255, 0.2)"
                color="#aa3bff"
                fontSize="10px"
                borderRadius="4px"
                px="6px"
                py="2px"
              >
                {uploadStatus.dept}
              </Badge>
            </HStack>
            <IconButton
              aria-label="Dismiss message"
              size="xs"
              variant="ghost"
              color="#8e8ea0"
              _hover={{ color: '#ececf1', bg: 'rgba(255,255,255,0.1)' }}
              onClick={() => setUploadStatus({ type: '', message: '' })}
            >
              <FiX size={12} />
            </IconButton>
          </Flex>
        )}

        {/* Error message */}
        {uploadStatus.type === 'error' && (
          <Flex
            align="center"
            gap="8px"
            mt="8px"
            p="8px 12px"
            borderRadius="6px"
            bg="rgba(239, 68, 68, 0.1)"
            border="1px solid rgba(239, 68, 68, 0.3)"
          >
            <FiAlertCircle size={14} color="#ef4444" />
            <Text color="#f87171" fontSize="12px" flex="1">
              {uploadStatus.message}
            </Text>
            <IconButton
              aria-label="Dismiss error"
              size="xs"
              variant="ghost"
              color="#ef4444"
              _hover={{ bg: 'rgba(239, 68, 68, 0.2)' }}
              onClick={() => setUploadStatus({ type: '', message: '' })}
            >
              <FiX size={12} />
            </IconButton>
          </Flex>
        )}
      </Box>

      {/* Footer */}
      <Box px="16px" pt="16px">
        <Text color="#8e8ea0" fontSize="12px" textAlign="center">
          © 2026 QuadGPT. All rights reserved.
        </Text>
      </Box>
    </Flex>
  )
}

export default ChatArea