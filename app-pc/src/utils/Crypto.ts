import CryptoJS from 'crypto-js'

const SECRET_KEY = import.meta.env.VITE_SECRET_KEY

/**
 * AES 加密工具类
 */
export class AesCrypto {
  /**
   * 加密字符串
   * @param plainText 要加密的明文
   * @returns 加密后的密文
   */
  static encrypt(plainText: string): string {
    if (!plainText)
      return ''
    return CryptoJS.AES.encrypt(plainText, SECRET_KEY).toString()
  }

  /**
   * 解密字符串
   * @param cipherText 要解密的密文
   * @returns 解密后的明文
   */
  static decrypt(cipherText: string): string {
    if (!cipherText)
      return ''
    const bytes = CryptoJS.AES.decrypt(cipherText, SECRET_KEY)
    return bytes.toString(CryptoJS.enc.Utf8)
  }
}
