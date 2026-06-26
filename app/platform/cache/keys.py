def login_token_key(token: str) -> str:
    return f"login:token:{token}"


def login_account_tokens_key(account_type: str, account_id: str) -> str:
    return f"login:account:{account_type}:{account_id}"


def permission_registry_modules_key() -> str:
    return "permission:registry:modules"


def permission_registry_resources_key() -> str:
    return "permission:registry:resources"


def permission_registry_permissions_key() -> str:
    return "permission:registry:permissions"


def permission_registry_module_resources_key(module_code: str) -> str:
    return f"permission:registry:module_resources:{module_code}"


def permission_registry_resource_permissions_key(resource_code: str) -> str:
    return f"permission:registry:resource_permissions:{resource_code}"


def banner_interaction_delta_key() -> str:
    return "banner:interaction:deltas"
