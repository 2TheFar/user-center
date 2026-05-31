package com.zhiyuan.usercenter.service.impl;

import com.zhiyuan.usercenter.common.BusinessException;
import com.zhiyuan.usercenter.common.ErrorCode;
import com.zhiyuan.usercenter.service.AvatarStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class AvatarStorageServiceImpl implements AvatarStorageService {

    /*
     * 最大头像大小
     */
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    /*
     * 允许上传的图片类型
     */
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    /*
     * 头像存储目录
     * System.getProperty("user.dir") 获取当前项目运行目录。 user-center
     * System.getProperty("user.dir") 返回的是 当前 Java 进程的工作目录的绝对路径，但它不是固定等于项目根目录，而是取决于你从哪里启动应用。
     * Paths.get(System.getProperty("user.dir"), "uploads", "avatars") 拼接路径：项目根目录/uploads/avatars
     * .toAbsolutePath() 转成绝对路径。
     * .normalize() 规范化路径，去掉一些多余或危险的路径写法
     */
    private final Path avatarStorageRoot = Paths.get(System.getProperty("user.dir"), "uploads", "avatars")
            .toAbsolutePath()
            .normalize();

    /**
     * 保存头像
     *
     * @param file 前端上传过来的文件
     * @return 保存成功后返回头像访问地址
     */
    @Override
    public String saveAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择头像图片");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像图片不能超过 5MB");
        }

        /*
         * 判断图片类型
         * extension 是获取上传文件的 Content-Type 并转换为对应的后缀
         * file.getContentType()可能返回
         * image/jpeg
         * image/png
         * image/webp
         * image/gif
         */
        String extension = ALLOWED_IMAGE_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像只支持 JPG、PNG、WEBP 或 GIF 图片");
        }

        /*
         * 开始保存文件
         */
        try {
            // 创建头像保存目录。不存在就创建，存在也不报错。
            Files.createDirectories(avatarStorageRoot);
            // 生成一个随机文件名。用 UUID 可以避免文件名冲突，也更安全。防止SQL注入。
            String fileName = UUID.randomUUID() + "." + extension;
            // 生成最终保存路径。resolve表示在头像目录下面拼接文件名。
            // normalize()表示规范化路径，防止路径中出现奇怪的 ../。
            Path targetPath = avatarStorageRoot.resolve(fileName).normalize();
            /*
             * 防止路径穿越
             *
             * 判断最终文件路径是不是还在头像目录里面。
             * 这一步是为了防止路径穿越攻击。
             * 比如有人传入类似：../../xxx可能试图把文件保存到头像目录之外。
             */
            if (!targetPath.startsWith(avatarStorageRoot)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像文件名不合法");
            }
            /*
             * 复制上传文件到本地
             *
             * file.getInputStream()获取上传文件的输入流。可以理解为：从上传文件里读取数据。
             * inputStream 文件输入流，也就是上传文件的内容。
             * 这里用了 try-with-resources 写法，方法结束后会自动关闭 inputStream。
             *
             * 然后把上传文件复制到目标路径。
             * StandardCopyOption.REPLACE_EXISTING表示如果目标文件已经存在，就覆盖。
             *
             * 保存成功后，返回头像访问路径。不是相对路径，是浏览器可访问的接口路径。
             * 并且只把这个站内 URL 存进数据库。
             * 一句话：磁盘路径负责存，URL 路径负责访问；数据库保存 URL，不保存服务器磁盘绝对路径。
             */
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return AVATAR_URL_PREFIX + fileName;
        } catch (IOException e) {
            /*
             * 开始捕获可能出现的 IO 异常。
             * 因为创建文件夹、复制文件这些操作都有可能失败。
             */
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像保存失败");
        }
    }

    @Override
    public Resource loadAvatar(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像文件名不能为空");
        }

        /*
         * 1.把传入内容当路径解析，只提取最后的文件名
         * 先把fileName解析成Path，只取这个路径的最后一段文件名，最后把 Path 类型转回字符串。
         * 比如传入../../abc.jpg，提取后只会得到：abc.jpg
         * 但它的目的不是“修正非法路径后继续用”，而是用于接下来判断用户传进来的是不是单纯文件名
         * 2.再和原始值对比，用来判断用户有没有偷偷传路径。
         * 单纯文件名和原始文件名比较，如果用户传进来的不是单纯文件名，而是带路径的东西，就拒绝。
         */
        String safeFileName = Paths.get(fileName).getFileName().toString();
        if (!safeFileName.equals(fileName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像文件名不合法");
        }

        /*
         * 根据头像目录和文件名，拼出完整本地存储路径。
         * 1.第一个判断表示头像路径不在头像根目录下。
         * 单纯文件名的前提下基本不可能，也许相当于！=null的作用吧
         * 2.第二个判断表示这个路径不是一个正常文件。
         * 可能：
         * 文件不存在
         * 路径是文件夹
         * 路径不可访问
         */
        Path avatarPath = avatarStorageRoot.resolve(safeFileName).normalize();
        if (!avatarPath.startsWith(avatarStorageRoot) || !Files.isRegularFile(avatarPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "头像不存在");
        }

        /*
         * 返回头像资源
         * 把本地文件路径转换成 Spring 可以返回的资源对象。
         *
         * 比如本地文件路径会先转成 URI
         * 然后包装成 UrlResource
         * 后续 Controller 就可以把这个资源返回给前端。
         */
        try {
            return new UrlResource(avatarPath.toUri());
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像读取失败");
        }
    }
}
