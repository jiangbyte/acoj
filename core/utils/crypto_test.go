package utils

import (
	"testing"

	"hei-gin/config"
)

func TestSM2Roundtrip(t *testing.T) {
	_ = config.Load("E:/DevProjects/hei/hei-gin/config.yaml")
	if config.C == nil {
		t.Fatal("Config not loaded")
	}

	password := "admin123"
	encrypted, err := SM2Encrypt(password)
	if err != nil {
		t.Fatalf("Encrypt failed: %v", err)
	}
	t.Logf("Encrypted hex (%d chars): %s", len(encrypted), encrypted)

	// Test decrypting with 04 prefix (Go-native format)
	decrypted, err := SM2Decrypt(encrypted)
	if err != nil {
		t.Fatalf("Decrypt (with 04) failed: %v", err)
	}
	if decrypted != password {
		t.Fatalf("Decrypt (with 04) mismatch: got %q, want %q", decrypted, password)
	}

	// Test decrypting WITHOUT 04 prefix (frontend sm-crypto format)
	noPrefix := encrypted[2:] // strip "04"
	decrypted, err = SM2Decrypt(noPrefix)
	if err != nil {
		t.Fatalf("Decrypt (without 04) failed: %v", err)
	}
	if decrypted != password {
		t.Fatalf("Decrypt (without 04) mismatch: got %q, want %q", decrypted, password)
	}
}
