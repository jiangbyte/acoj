import CryptoJS from 'crypto-js'

const SECRET_KEY = import.meta.env.VITE_SECRET_KEY

/**
  * Implementation details.
 */
export class AesCrypto {
  /**
    * Implementation details.
    * Implementation details.
    * Implementation details.
   */
  static encrypt(plainText: string): string {
    if (!plainText)
      return ''
    return CryptoJS.AES.encrypt(plainText, SECRET_KEY).toString()
  }

  /**
    * Implementation details.
    * Implementation details.
    * Implementation details.
   */
  static decrypt(cipherText: string): string {
    if (!cipherText)
      return ''
    const bytes = CryptoJS.AES.decrypt(cipherText, SECRET_KEY)
    return bytes.toString(CryptoJS.enc.Utf8)
  }
}
