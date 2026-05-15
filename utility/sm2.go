package utility

import (
	"crypto/elliptic"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"math/big"

	"github.com/tjfoc/gmsm/sm2"
)

var sm2PrivateKey *sm2.PrivateKey
var sm2PublicKey *sm2.PublicKey

// InitSM2 initializes SM2 with the given hex-encoded private and public keys.
func InitSM2(privateKeyHex, publicKeyHex string) (err error) {
	privateKeyBytes, err := hex.DecodeString(privateKeyHex)
	if err != nil {
		return fmt.Errorf("decode private key: %w", err)
	}
	publicKeyBytes, err := hex.DecodeString(publicKeyHex)
	if err != nil {
		return fmt.Errorf("decode public key: %w", err)
	}

	curve := sm2.P256Sm2()
	priv := new(sm2.PrivateKey)
	priv.Curve = curve
	priv.D = new(big.Int).SetBytes(privateKeyBytes)
	priv.PublicKey.X, priv.PublicKey.Y = curve.ScalarBaseMult(priv.D.Bytes())
	priv.PublicKey.Curve = curve

	x, y := elliptic.Unmarshal(curve, publicKeyBytes)
	if x == nil {
		return errors.New("invalid public key")
	}
	pub := &sm2.PublicKey{Curve: curve, X: x, Y: y}

	sm2PrivateKey = priv
	sm2PublicKey = pub
	return nil
}

// SM2Decrypt decrypts a hex-encoded C1C3C2 format ciphertext.
func SM2Decrypt(ciphertextHex string) (string, error) {
	if sm2PrivateKey == nil {
		return "", errors.New("SM2 not initialized")
	}

	raw, err := hex.DecodeString(ciphertextHex)
	if err != nil {
		return "", fmt.Errorf("hex decode: %w", err)
	}

	if len(raw) > 0 && raw[0] != 0x04 {
		raw = append([]byte{0x04}, raw...)
	}

	plaintext, err := sm2.Decrypt(sm2PrivateKey, raw, sm2.C1C3C2)
	if err != nil {
		return "", errors.New("SM2 decryption failed")
	}
	return string(plaintext), nil
}

// SM2GetPublicKey returns the hex-encoded public key.
func SM2GetPublicKey() string {
	if sm2PublicKey == nil {
		return ""
	}
	pubBytes := elliptic.Marshal(sm2PublicKey.Curve, sm2PublicKey.X, sm2PublicKey.Y)
	return hex.EncodeToString(pubBytes)
}

// SM2Encrypt encrypts plaintext to hex-encoded C1C3C2 format.
func SM2Encrypt(plaintext string) (string, error) {
	if sm2PublicKey == nil {
		return "", errors.New("SM2 not initialized")
	}
	ciphertext, err := sm2.Encrypt(sm2PublicKey, []byte(plaintext), rand.Reader, sm2.C1C3C2)
	if err != nil {
		return "", err
	}
	return hex.EncodeToString(ciphertext), nil
}
