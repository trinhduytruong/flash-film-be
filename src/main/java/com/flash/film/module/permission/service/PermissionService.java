package com.flash.film.module.permission.service;

import com.flash.film.module.user.entity.User;

public interface PermissionService {
    void checkAccessApi(User user, String httpMethod, String requestUri);
}
