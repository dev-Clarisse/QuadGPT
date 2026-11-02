import { Box, Button, Dialog, HStack, Portal, Text } from '@chakra-ui/react'
import React, { useCallback, useRef, useState } from 'react'
import { FiAlertTriangle } from 'react-icons/fi'

export type ConfirmOptions = {
  title: string
  message: React.ReactNode
  confirmLabel?: string
  cancelLabel?: string
}

/**
 * Styled replacement for window.confirm.
 *
 *   const { confirm, confirmDialog } = useConfirm()
 *   if (!(await confirm({ title: 'Delete?', message: '...' }))) return
 *   ...
 *   return <>{...}{confirmDialog}</>
 */
export const useConfirm = () => {
  const [isOpen, setIsOpen] = useState(false)
  // Kept after closing so the content does not vanish during the exit animation.
  const [options, setOptions] = useState<ConfirmOptions | null>(null)
  const resolverRef = useRef<((confirmed: boolean) => void) | null>(null)
  const cancelRef = useRef<HTMLButtonElement | null>(null)

  const confirm = useCallback(
    (confirmOptions: ConfirmOptions) =>
      new Promise<boolean>((resolve) => {
        resolverRef.current = resolve
        setOptions(confirmOptions)
        setIsOpen(true)
      }),
    []
  )

  const close = (confirmed: boolean) => {
    resolverRef.current?.(confirmed)
    resolverRef.current = null
    setIsOpen(false)
  }

  const confirmDialog = (
    <Dialog.Root
      open={isOpen}
      onOpenChange={(details) => {
        if (!details.open) close(false)
      }}
      role="alertdialog"
      placement="center"
      initialFocusEl={() => cancelRef.current}
    >
      <Portal>
        <Dialog.Backdrop bg="rgba(0, 0, 0, 0.6)" backdropFilter="blur(2px)" />
        <Dialog.Positioner px="16px">
          <Dialog.Content
            bg="#202123"
            color="#ececf1"
            border="1px solid rgba(255, 255, 255, 0.1)"
            borderRadius="12px"
            boxShadow="0 12px 32px rgba(0, 0, 0, 0.45)"
            maxW="420px"
          >
            <Dialog.Header pb="8px">
              <HStack gap="10px">
                <Box
                  display="flex"
                  alignItems="center"
                  justifyContent="center"
                  boxSize="32px"
                  borderRadius="full"
                  bg="rgba(239, 68, 68, 0.15)"
                  flexShrink={0}
                >
                  <FiAlertTriangle size={16} color="#ef4444" />
                </Box>
                <Dialog.Title fontSize="16px" fontWeight="700" color="#ececf1">
                  {options?.title}
                </Dialog.Title>
              </HStack>
            </Dialog.Header>

            <Dialog.Body pt="0">
              <Dialog.Description asChild>
                <Text as="div" fontSize="13px" lineHeight="1.6" color="#c5c5d2">
                  {options?.message}
                </Text>
              </Dialog.Description>
            </Dialog.Body>

            <Dialog.Footer gap="8px">
              <Button
                ref={cancelRef}
                size="sm"
                variant="ghost"
                color="#ececf1"
                _hover={{ bg: 'rgba(255, 255, 255, 0.1)' }}
                onClick={() => close(false)}
              >
                {options?.cancelLabel ?? 'Cancel'}
              </Button>
              <Button
                size="sm"
                bg="#ef4444"
                color="#ffffff"
                _hover={{ bg: '#dc2626' }}
                onClick={() => close(true)}
              >
                {options?.confirmLabel ?? 'Delete'}
              </Button>
            </Dialog.Footer>
          </Dialog.Content>
        </Dialog.Positioner>
      </Portal>
    </Dialog.Root>
  )

  return { confirm, confirmDialog }
}
