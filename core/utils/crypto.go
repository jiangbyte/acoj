package utils

import (
	"encoding/hex"

	"github.com/tjfoc/gmsm/sm2"
	"github.com/tjfoc/gmsm/x509"
	"golang.org/x/crypto/bcrypt"

	"hei-gin/config"
)

// BcryptHash generates a bcrypt hash of the password.
func BcryptHash(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(bytes), err
}

// BcryptVerify checks a bcrypt hash against a password.
func BcryptVerify(password, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}

// SM2Decrypt decrypts ciphertext using the configured SM2 private key.
// The front-end sm-crypto library outputs C1C3C2 format WITHOUT the 04
// uncompressed-point prefix, but tjfoc/gmsm's Decrypt expects the 04 prefix
// (it strips the first byte internally).  We add it back when missing.
func SM2Decrypt(cipherText string) (string, error) {
	privKey, err := x509.ReadPrivateKeyFromHex(config.C.SM2.PrivateKey)
	if err != nil {
		return "", err
	}

	cipherBytes, err := hex.DecodeString(cipherText)
	if err != nil {
		return "", err
	}

	// sm-crypto JS outputs ciphertext without 04 prefix (C1 || C3 || C2).
	// tjfoc/gmsm Decrypt always strips the first byte, so add 04 if absent.
	if len(cipherBytes) > 0 && cipherBytes[0] != 0x04 {
		cipherBytes = append([]byte{0x04}, cipherBytes...)
	}

	plainBytes, err := sm2.Decrypt(privKey, cipherBytes, sm2.C1C3C2)
	if err != nil {
		return "", err
	}

	return string(plainBytes), nil
}

// SM2Encrypt encrypts plaintext using the configured SM2 public key.
func SM2Encrypt(plainText string) (string, error) {
	pubKey, err := x509.ReadPublicKeyFromHex(config.C.SM2.PublicKey)
	if err != nil {
		return "", err
	}

	cipherBytes, err := sm2.Encrypt(pubKey, []byte(plainText), nil, sm2.C1C3C2)
	if err != nil {
		return "", err
	}

	return hex.EncodeToString(cipherBytes), nil
}
