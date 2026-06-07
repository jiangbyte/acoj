.PHONY: dev build run test lint clean scaffold list-imports

# ── Development ─────────────────────────────────────────────────────
dev:          ## Start with hot-reload (requires air)
	@command -v air >/dev/null 2>&1 || { \
		echo "Installing air..."; go install github.com/air-verse/air@latest; \
	}
	air

build:        ## Build binary
	go build -o bin/hei-gin main.go

run:          ## Build & run
	go run main.go

# ── Code Generation ─────────────────────────────────────────────────
scaffold:     ## Create new plugin: make scaffold name=plugin-xxx
	go run cmd/codegen/main.go scaffold $(name)

list-plugins: ## List all plugins
	go run cmd/codegen/main.go list

gen-imports:  ## Regenerate blank imports in main.go
	go run cmd/codegen/main.go gen-imports

# ── Database ────────────────────────────────────────────────────────
migrate:      ## Apply database migrations
	go run cmd/migrate/main.go

migrate-dry:  ## Preview database migrations (dry-run)
	go run cmd/migrate/main.go -skip-seed

# ── Quality ─────────────────────────────────────────────────────────
test:         ## Run tests
	go test ./...

lint:         ## Run linter
	go vet ./...

clean:        ## Clean build artifacts
	rm -rf .air_tmp bin/

help:         ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
