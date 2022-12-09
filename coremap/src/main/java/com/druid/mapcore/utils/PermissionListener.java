package com.druid.mapcore.utils;

public interface PermissionListener {
    void permissionAgree(int code);

    void permissionRefuse(int code);

    void permissionRefuseAndNotNotify(int code);
}
