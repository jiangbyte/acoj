package utils

import (
	"testing"
)

func TestBcryptWithSeedData(t *testing.T) {
	// Hash from the seed data for admin user
	adminHash := "$2b$12$5t3Ey0kGLXaWgmUMYHh8aeh9hOTwpIcKI4M.txQi26Sd3jz4aeEm2"
	commonHash := "$2b$12$UQFzAxtCkfwFwgrJy0XYm.rO860SX5NIH6zOEm/4SsUdgMA9SkuVC"

	// Test with possible passwords
	passwords := []string{"admin123", "123456", "admin", "password", "hei123456", "Abc123++"}

	for _, pwd := range passwords {
		adminResult := BcryptVerify(pwd, adminHash)
		t.Logf("admin user - password %q: %v", pwd, adminResult)

		commonResult := BcryptVerify(pwd, commonHash)
		t.Logf("common user - password %q: %v", pwd, commonResult)
	}

	// Also verify that empty password doesn't match
	if BcryptVerify("", adminHash) {
		t.Error("Empty password should not match admin hash")
	}
}

func TestBcryptHashAndVerify(t *testing.T) {
	passwords := []string{"admin123", "123456", "", "hello世界", "a"}
	for _, pwd := range passwords {
		hash, err := BcryptHash(pwd)
		if err != nil {
			t.Fatalf("BcryptHash(%q) failed: %v", pwd, err)
		}
		t.Logf("hash for %q: %s", pwd, hash)

		if !BcryptVerify(pwd, hash) {
			t.Fatalf("BcryptVerify failed for password %q with its own hash %s", pwd, hash)
		}
	}
}
