package io.charlie.web.oj.modular.context;

public class CacheConstant {
    
    public static final String DATA_SCOPE_CONTEXT = "data:scope:context:";
    public static final String USER_DATA_BUNDLE = "data:user:bundle:";
    public static final String ALL_ROLES = "data:roles:all";
    public static final String PART_ROLES = "data:roles:part";
    public static final String ALL_GROUPS = "data:groups:all";
    
    public static final long EXPIRE_MINUTES = 5;
    public static final long EXPIRE_SECONDS = EXPIRE_MINUTES * 60;
}