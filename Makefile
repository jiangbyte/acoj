ENV ?= dev
GO ?= go

.DEFAULT_GOAL := help

.PHONY: help judge similarity check-env check-judge-config check-similarity-config

help:
	@printf '%s\n' \
		'ACOJ service commands:' \
		'' \
		'  make judge [ENV=dev|local]       Start service-judge (default: ENV=dev)' \
		'  make similarity [ENV=dev|local]  Start service-similarity (default: ENV=dev)' \
		'  make help                        Show this help'

check-env:
	@case "$(ENV)" in \
		dev|local) ;; \
		*) printf 'Unsupported ENV "%s". Use ENV=dev or ENV=local.\n' "$(ENV)" >&2; exit 2 ;; \
	esac

check-judge-config: check-env
	@if [ ! -f "service-judge/etc/judge-$(ENV).yaml" ]; then \
		printf 'Missing service-judge/etc/judge-%s.yaml.\n' "$(ENV)" >&2; \
		if [ "$(ENV)" = dev ]; then \
			printf 'Create it from service-judge/etc/judge-dev.yaml.example before starting.\n' >&2; \
		fi; \
		exit 1; \
	fi

check-similarity-config: check-env
	@if [ ! -f "service-similarity/etc/similar-$(ENV).yaml" ]; then \
		printf 'Missing service-similarity/etc/similar-%s.yaml.\n' "$(ENV)" >&2; \
		if [ "$(ENV)" = dev ]; then \
			printf 'Create it from service-similarity/etc/similar-dev.yaml.example before starting.\n' >&2; \
		fi; \
		exit 1; \
	fi

judge: check-judge-config
	@printf 'Starting service-judge with env=%s...\n' "$(ENV)"
	@cd service-judge && $(GO) run main.go -f etc/judge-$(ENV).yaml -nacos

similarity: check-similarity-config
	@printf 'Starting service-similarity with env=%s...\n' "$(ENV)"
	@cd service-similarity && $(GO) run main.go -f etc/similar-$(ENV).yaml -nacos
