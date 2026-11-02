import { Box, Button, Flex, HStack, IconButton, Spinner, Text } from '@chakra-ui/react'
import React, { useEffect, useState } from 'react'
import { FiAlertCircle, FiArrowLeft, FiDownload, FiFileText, FiFolder, FiTrash2 } from 'react-icons/fi'
import { DEPARTMENTS, getDepartmentLabel } from '../../departments'
import { useConfirm } from '../ConfirmDialog/useConfirm'

type DocumentItem = {
  id: number
  name: string
  departments: string[]
  createdAt: string
  downloadable: boolean
}

type DocumentsPageProps = {
  accessToken: string
  isAdmin: boolean
  userDepartment: string | null
  onBack: () => void
}

const DocumentsPage: React.FC<DocumentsPageProps> = ({
  accessToken,
  isAdmin,
  userDepartment,
  onBack,
}) => {
  const [documents, setDocuments] = useState<DocumentItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [downloadingId, setDownloadingId] = useState<number | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const { confirm, confirmDialog } = useConfirm()

  useEffect(() => {
    const loadDocuments = async () => {
      setIsLoading(true)
      setError('')
      try {
        const response = await fetch('/api/documents', {
          headers: { Authorization: `Bearer ${accessToken}` },
        })
        if (!response.ok) {
          throw new Error((await response.text()) || 'Unable to load documents.')
        }
        setDocuments((await response.json()) as DocumentItem[])
      } catch (requestError) {
        setError(requestError instanceof Error ? requestError.message : 'Unable to load documents.')
      } finally {
        setIsLoading(false)
      }
    }

    loadDocuments()
  }, [accessToken])

  const handleDownload = async (doc: DocumentItem) => {
    setDownloadingId(doc.id)
    setError('')
    try {
      const response = await fetch(`/api/documents/${doc.id}/file`, {
        headers: { Authorization: `Bearer ${accessToken}` },
      })
      if (!response.ok) {
        throw new Error((await response.text()) || 'The document could not be downloaded.')
      }

      const url = URL.createObjectURL(await response.blob())
      const link = document.createElement('a')
      link.href = url
      link.download = doc.name
      link.click()
      URL.revokeObjectURL(url)
    } catch (requestError) {
      setError(
        requestError instanceof Error ? requestError.message : 'The document could not be downloaded.'
      )
    } finally {
      setDownloadingId(null)
    }
  }

  const handleDelete = async (doc: DocumentItem) => {
    const confirmed = await confirm({
      title: 'Delete document',
      message: (
        <>
          <Text>
            Delete <Text as="span" fontWeight="600" color="#ececf1">{doc.name}</Text>? The chatbot
            will no longer use it.
          </Text>
          {doc.departments.length > 1 && (
            <Text mt="8px">
              It will be removed from every department it belongs to:{' '}
              {doc.departments.map(getDepartmentLabel).join(', ')}.
            </Text>
          )}
        </>
      ),
    })
    if (!confirmed) return

    setDeletingId(doc.id)
    setError('')
    try {
      const response = await fetch(`/api/documents/${doc.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${accessToken}` },
      })
      if (!response.ok) {
        throw new Error((await response.text()) || 'The document could not be deleted.')
      }
      setDocuments((prev) => prev.filter((item) => item.id !== doc.id))
    } catch (requestError) {
      setError(
        requestError instanceof Error ? requestError.message : 'The document could not be deleted.'
      )
    } finally {
      setDeletingId(null)
    }
  }

  // Admin sees one section per department; a user only sees their own.
  const sections = isAdmin
    ? DEPARTMENTS.map((dept) => dept.value)
    : userDepartment
      ? [userDepartment]
      : []

  return (
    <Flex direction="column" w="full" h="100vh" bg="#343541" p="24px" overflowY="auto">
      <Box maxW="900px" w="full" mx="auto">
        {/* Header */}
        <HStack gap="12px" mb="24px">
          <IconButton
            aria-label="Back to chat"
            title="Back to chat"
            size="sm"
            variant="ghost"
            color="#ececf1"
            _hover={{ bg: 'rgba(255,255,255,0.1)' }}
            onClick={onBack}
          >
            <FiArrowLeft size={16} />
          </IconButton>
          <FiFolder size={20} color="#aa3bff" />
          <Text fontSize="20px" fontWeight="700" color="#ececf1">
            {isAdmin ? 'All documents' : 'My department documents'}
          </Text>
        </HStack>

        {error && (
          <Flex
            align="center"
            gap="8px"
            mb="16px"
            p="8px 12px"
            borderRadius="6px"
            bg="rgba(239, 68, 68, 0.1)"
            border="1px solid rgba(239, 68, 68, 0.3)"
          >
            <FiAlertCircle size={14} color="#ef4444" />
            <Text color="#f87171" fontSize="12px">
              {error}
            </Text>
          </Flex>
        )}

        {isLoading ? (
          <Flex justify="center" py="48px">
            <Spinner color="#aa3bff" />
          </Flex>
        ) : (
          sections.map((section) => {
            const sectionDocuments = documents.filter((doc) => doc.departments.includes(section))

            return (
              <Box
                key={section}
                mb="16px"
                bg="#202123"
                border="1px solid rgba(255, 255, 255, 0.1)"
                borderRadius="12px"
                overflow="hidden"
              >
                <Flex
                  align="center"
                  justify="space-between"
                  px="16px"
                  py="10px"
                  borderBottom="1px solid rgba(255, 255, 255, 0.1)"
                >
                  <Text fontSize="14px" fontWeight="600" color="#ececf1">
                    {getDepartmentLabel(section)}
                  </Text>
                  <Text fontSize="12px" color="#8e8ea0">
                    {sectionDocuments.length} document{sectionDocuments.length === 1 ? '' : 's'}
                  </Text>
                </Flex>

                {sectionDocuments.length === 0 ? (
                  <Text px="16px" py="12px" fontSize="12px" color="#8e8ea0">
                    No documents yet.
                  </Text>
                ) : (
                  sectionDocuments.map((doc) => (
                    <Flex
                      key={doc.id}
                      align="center"
                      justify="space-between"
                      gap="12px"
                      px="16px"
                      py="10px"
                      _hover={{ bg: 'rgba(255,255,255,0.03)' }}
                    >
                      <HStack gap="10px" minW={0}>
                        <FiFileText size={14} color="#aa3bff" style={{ flexShrink: 0 }} />
                        <Box minW={0}>
                          <Text
                            fontSize="13px"
                            color="#ececf1"
                            overflow="hidden"
                            textOverflow="ellipsis"
                            whiteSpace="nowrap"
                          >
                            {doc.name}
                          </Text>
                          <Text fontSize="11px" color="#8e8ea0">
                            Added on {new Date(doc.createdAt).toLocaleDateString()}
                          </Text>
                        </Box>
                      </HStack>

                      <HStack gap="6px" flexShrink={0}>
                        <Button
                          size="xs"
                          bg={doc.downloadable ? '#aa3bff' : '#444654'}
                          color="#ffffff"
                          _hover={{ bg: doc.downloadable ? '#9322eb' : '#444654' }}
                          disabled={!doc.downloadable || downloadingId === doc.id}
                          title={doc.downloadable ? 'Download' : 'Original file not stored for this document'}
                          onClick={() => handleDownload(doc)}
                        >
                          {downloadingId === doc.id ? <Spinner size="xs" /> : <FiDownload size={12} />}
                          Download
                        </Button>

                        {isAdmin && (
                          <IconButton
                            aria-label="Delete document"
                            title="Delete document"
                            size="xs"
                            variant="ghost"
                            color="#8e8ea0"
                            _hover={{ color: '#ff4d4d', bg: 'rgba(239, 68, 68, 0.1)' }}
                            disabled={deletingId === doc.id}
                            onClick={() => handleDelete(doc)}
                          >
                            {deletingId === doc.id ? <Spinner size="xs" /> : <FiTrash2 size={13} />}
                          </IconButton>
                        )}
                      </HStack>
                    </Flex>
                  ))
                )}
              </Box>
            )
          })
        )}
      </Box>

      {confirmDialog}
    </Flex>
  )
}

export default DocumentsPage
