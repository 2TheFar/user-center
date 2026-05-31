package com.zhiyuan.usercenter.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {

    String AVATAR_URL_PREFIX = "/user/avatar/";

    String saveAvatar(MultipartFile file);

    Resource loadAvatar(String fileName);
}
