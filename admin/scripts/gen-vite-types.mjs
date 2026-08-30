import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'vite'

const configFile = join(dirname(fileURLToPath(import.meta.url)), '..', 'vite.config.ts')

const server = await createServer({
  configFile,
  logLevel: 'error',
})

await server.listen(5198, '127.0.0.1')
await server.close()
