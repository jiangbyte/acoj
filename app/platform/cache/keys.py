def login_token_key(token: str) -> str:
    return f"login:token:{token}"


def login_account_tokens_key(account_type: str, account_id: str) -> str:
    return f"login:account:{account_type}:{account_id}"


def cache_key(name: str) -> str:
    return f"Cache:{name}"


def permission_resource_cache_key() -> str:
    return cache_key("permission-resource")


def permission_resource_method_cache_key() -> str:
    return cache_key("permission-resource-method")


def banner_interaction_delta_key() -> str:
    return "banner:interaction:deltas"
