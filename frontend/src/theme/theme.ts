import { createSystem, defaultConfig, defineConfig } from '@chakra-ui/react'
import '@fontsource/open-sans'
import '@fontsource/open-sans/300.css'
import '@fontsource/open-sans/400.css'
import '@fontsource/open-sans/700.css'
import '@fontsource/open-sans/800.css'

const config = defineConfig({
  theme: {
    tokens: {
      fonts: {
        heading: { value: `'Open Sans', sans-serif` },
        body: { value: `'Open Sans', sans-serif` },
      },
    },
  },
  globalCss: {
    body: {
      bg: '#343541',
      color: '#ececf1',
    },
  },
})

export const system = createSystem(defaultConfig, config)
export default system