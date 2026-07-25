/**
 * RSA-OAEP/SHA-256 密码加密。
 *
 * 后端要求：
 *   1. 从 /password-key 获取 base64 DER 格式的 RSA 公钥
 *   2. 用 RSA-OAEP/SHA-256 加密密码明文
 *   3. 将密文 base64 编码后发送
 */

export interface PasswordKey {
  key_id: string
  public_key: string
}

export interface EncryptedPassword {
  encrypted: string
  password_key_id: string
}

function base64ToBytes(base64: string): Uint8Array {
  const binary = atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = ''
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}

async function importPublicKey(base64Der: string): Promise<CryptoKey> {
  const derBytes = base64ToBytes(base64Der)
  return crypto.subtle.importKey('spki', derBytes, { name: 'RSA-OAEP', hash: 'SHA-256' }, false, [
    'encrypt',
  ])
}

async function encryptWithPublicKey(publicKey: CryptoKey, plaintext: string): Promise<string> {
  const encoded = new TextEncoder().encode(plaintext)
  const ciphertext = await crypto.subtle.encrypt({ name: 'RSA-OAEP' }, publicKey, encoded)
  return bytesToBase64(new Uint8Array(ciphertext))
}

export async function encryptPassword(
  plainPassword: string,
  passwordKey: PasswordKey,
): Promise<EncryptedPassword> {
  const publicKey = await importPublicKey(passwordKey.public_key)
  const encrypted = await encryptWithPublicKey(publicKey, plainPassword)
  return {
    encrypted,
    password_key_id: passwordKey.key_id,
  }
}
