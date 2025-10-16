package io.charlie.web.oj.modular.sys.file.config;

import io.charlie.web.oj.modular.sys.file.config.properties.StorageProperties;
import io.charlie.web.oj.modular.sys.file.service.StorageService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StorageServiceFactory {
    private final StorageProperties storageProperties;

    private final Map<StorageProperties.StorageType, StorageService> storageServices;
    
    public StorageServiceFactory(List<StorageService> services, StorageProperties storageProperties) {
        storageServices = services.stream()
                .collect(Collectors.toMap(StorageService::getType, Function.identity()));
        this.storageProperties = storageProperties;
    }
    
    public StorageService getStorageService() {
        StorageProperties.StorageType type = storageProperties.getType();
        if (type.equals(StorageProperties.StorageType.LOCAL)) {
            return storageServices.get(StorageProperties.StorageType.LOCAL);
        }
        if (type.equals(StorageProperties.StorageType.MINIO)) {
            return storageServices.get(StorageProperties.StorageType.MINIO);
        }

        // 返回当前激活的存储服务
        return storageServices.values().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到可用的存储服务"));
    }
    
    public StorageService getStorageService(StorageProperties.StorageType type) {
        return storageServices.get(type);
    }
}