package utils

import (
	"encoding/hex"
	"os"
	"testing"

	"hei-gin/config"
)

func TestSM2RoundTrip(t *testing.T) {
	// Load config
	if err := config.Load("../../config.yaml"); err != nil {
		t.Fatalf("Failed to load config: %v", err)
	}

	original := "admin123"

	// Encrypt
	encrypted, err := SM2Encrypt(original)
	if err != nil {
		t.Fatalf("SM2Encrypt failed: %v", err)
	}
	t.Logf("Encrypted (len=%d): %s", len(encrypted), encrypted[:40]+"...")

	// Decrypt
	decrypted, err := SM2Decrypt(encrypted)
	if err != nil {
		t.Fatalf("SM2Decrypt failed: %v", err)
	}

	if decrypted != original {
		t.Fatalf("Round-trip failed: got %q, want %q", decrypted, original)
	}
	t.Logf("Round-trip OK: %q -> %q", original, decrypted)
}

func TestSM2DecryptWithout04(t *testing.T) {
	// Simulate frontend: sm-crypto outputs without 04 prefix
	os.Setenv("TEST_MODE", "1")
	defer os.Unsetenv("TEST_MODE")

	if err := config.Load("../../config.yaml"); err != nil {
		t.Fatalf("Failed to load config: %v", err)
	}

	original := "TestPass123!"

	// Encrypt first (this will produce output with 04 prefix from SM2Encrypt)
	encrypted, err := SM2Encrypt(original)
	if err != nil {
		t.Fatalf("SM2Encrypt failed: %v", err)
	}

	// Manually strip 04 prefix to simulate frontend output (C1C3C2 without 04)
	cipherBytes, _ := hex.DecodeString(encrypted)
	var strippedHex string
	if len(cipherBytes) > 0 && cipherBytes[0] == 0x04 {
		strippedHex = hex.EncodeToString(cipherBytes[1:])
	} else {
		strippedHex = encrypted
	}
	t.Logf("Stripped hex (len=%d): %s", len(strippedHex), strippedHex[:40]+"...")

	// Now decrypt the stripped version (SM2Decrypt should add back 04)
	decrypted, err := SM2Decrypt(strippedHex)
	if err != nil {
		t.Fatalf("SM2Decrypt failed for stripped input: %v", err)
	}

	if decrypted != original {
		t.Fatalf("Decryption of stripped input failed: got %q, want %q", decrypted, original)
	}
	t.Logf("Stripped 04 round-trip OK: %q -> %q", original, decrypted)
}
