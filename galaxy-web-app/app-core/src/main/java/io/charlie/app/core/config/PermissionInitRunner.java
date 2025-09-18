package io.charlie.app.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionInitRunner implements CommandLineRunner {
    private final PermissionScanner permissionScanner;

    @Override
    public void run(String... args) throws Exception {
        List<String> permissions = permissionScanner.scanSaCheckPermissions();
        System.out.println("扫描到的权限列表:");
        permissions.forEach(System.out::println);
        
        System.out.println("去重后的权限:");
        permissionScanner.getUniquePermissions().forEach(System.out::println);
    }
}