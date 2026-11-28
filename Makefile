ENV ?= dev
GO ?= go
REGISTRY ?= registry.cn-beijing.aliyuncs.com
OWNER ?= jiangbytecr
TAG ?= latest
# 生产镜像默认空：前端走同源 /api，由 nginx 反代；本地调试可覆盖为 http://127.0.0.1:89
VITE_GATEWAY ?=

.DEFAULT_GOAL := help

.PHONY: help judge similarity check-env check-judge-config check-similarity-config \
	docker-build docker-push

help:
	@printf '%s\n' \
		'ACOJ service commands:' \
		'' \
		'  make judge [ENV=dev|local]       Start service-judge (default: ENV=dev)' \
		'  make similarity [ENV=dev|local]  Start service-similarity (default: ENV=dev)' \
		'  make docker-build [TAG=latest]   Build five app images for Aliyun ACR' \
		'  make docker-push [TAG=latest]    Push five app images to Aliyun ACR' \
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

# ACR 不兼容 BuildKit 默认 attestation（empty.v1+json），构建时关闭
BUILD_OPTS ?= --provenance=false --sbom=false

docker-build:
	@printf 'Building images registry=%s/%s tag=%s...\n' "$(REGISTRY)" "$(OWNER)" "$(TAG)"
	DOCKER_BUILDKIT=1 docker build $(BUILD_OPTS) -f service-system/Dockerfile \
		-t $(REGISTRY)/$(OWNER)/acoj-service-system:$(TAG) .
	DOCKER_BUILDKIT=1 docker build $(BUILD_OPTS) -f service-judge/Dockerfile \
		-t $(REGISTRY)/$(OWNER)/acoj-service-judge:$(TAG) ./service-judge
	DOCKER_BUILDKIT=1 docker build $(BUILD_OPTS) -f service-similarity/Dockerfile \
		-t $(REGISTRY)/$(OWNER)/acoj-service-similarity:$(TAG) ./service-similarity
	DOCKER_BUILDKIT=1 docker build $(BUILD_OPTS) -f app-pc/Dockerfile \
		--build-arg VITE_GATEWAY=$(VITE_GATEWAY) \
		-t $(REGISTRY)/$(OWNER)/acoj-pc:$(TAG) ./app-pc
	DOCKER_BUILDKIT=1 docker build $(BUILD_OPTS) -f app-admin/Dockerfile \
		--build-arg VITE_GATEWAY=$(VITE_GATEWAY) \
		-t $(REGISTRY)/$(OWNER)/acoj-admin:$(TAG) ./app-admin

docker-push:
	docker push $(REGISTRY)/$(OWNER)/acoj-service-system:$(TAG)
	docker push $(REGISTRY)/$(OWNER)/acoj-service-judge:$(TAG)
	docker push $(REGISTRY)/$(OWNER)/acoj-service-similarity:$(TAG)
	docker push $(REGISTRY)/$(OWNER)/acoj-pc:$(TAG)
	docker push $(REGISTRY)/$(OWNER)/acoj-admin:$(TAG)
