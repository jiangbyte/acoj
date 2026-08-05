"""ServiceRegistry — 通用服务注册中心。

注册:  register("interface_name", impl_instance)
消费:  resolve("interface_name") → object（调用方 import Protocol 后 cast）

新增 interface 只需要:
  1. 在 interfaces/ 下定义 Protocol 类
  2. 在模块的 module.py 中声明 ServiceRegistration
"""

_registry: dict[str, object] = {}


def register(interface: str, impl: object) -> None:
    _registry[interface] = impl


def resolve(interface: str) -> object:
    impl = _registry.get(interface)
    if impl is None:
        raise RuntimeError(f"'{interface}' not registered")
    return impl
