package com.zhiyuan.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiyuan.usercenter.common.BusinessException;
import com.zhiyuan.usercenter.common.ErrorCode;
import com.zhiyuan.usercenter.constant.UserConstant;
import com.zhiyuan.usercenter.mapper.UserMapper;
import com.zhiyuan.usercenter.model.domain.User;
import com.zhiyuan.usercenter.service.RegisterCodeService;
import com.zhiyuan.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现类
 *
 * @author zhiyuan
 * &#064;description  针对表【user(用户)】的数据库操作Service实现
 * &#064;createDate  2026-05-16 09:08:04
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    // 盐值，实际项目中可以放到配置文件里
    private static final String SALT = "kawaii";

    private static final int MAX_TEXT_LENGTH = 256;

    private static final int MAX_AVATAR_URL_LENGTH = 1024;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RegisterCodeService registerCodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)//开启事务管理（只要抛出 Exception 或它的子类异常，就回滚事务）
    public long userRegister(String userAccount, String userPassword, String confirmPassword, String registerCode) {
        // 1. 校验
        // 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能为空");
        }
        // 账号长度不小于6位
        if (userAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于6位");
        }
        // 密码长度不小于8位
        if (userPassword.length() < 8 || confirmPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8位");
        }
        /*
         * 账号不能包含特殊字符
         * ^            // 从字符串开头开始匹配
         * [a-zA-Z0-9_] // 只允许英文字母、数字、下划线
         * +            // 至少出现一次
         * $            // 匹配到字符串结尾
         */
        String regex = "^[a-zA-Z0-9_]+$";
        if (!userAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }
        // 密码要和确认密码相同
        if (!userPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码要和确认密码相同");
        }
        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();// 创建查询条件构造器
        queryWrapper.eq("userAccount", userAccount);// 设置查询条件：userAccount 字段等于 userAccount
        long count = this.count(queryWrapper);// 统计满足条件的用户数量
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能重复");
        }
        /*
         * 注册码是否可用
         *
         * useCode已经包含一模一样的校验，就算不提前校验也可以用，但不够标准
         *
         * 推荐流程：
         * 先 checkCode：提前判断，给用户明确错误
         * 再 useCode：真正消费，防止并发重复使用
         *
         * 提前校验的意义在于：
         * 1. 提前失败，少做无意义操作：提前结束流程
         * 2. 错误码更清楚：确保了可以使用，useCode还失败，那么就可能是并发抢占、数据库更新失败、极端情况下状态变化
         */
        if (!registerCodeService.checkCode(registerCode)) {
            throw new BusinessException(ErrorCode.REGISTER_CODE_ERROR, "注册码不可用");
        }

        // 2. 加密
        // MD5 加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        boolean saveResult = this.save(user);
        // 实际类型是Long，返回类型是long，当保存失败时，Long的值为null，拆箱会出错，所以保存失败时不直接返回id
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，请重新再试");
        }

        // 纳尼？这个user竟然和数据库是同步的？还会反过来给user赋值id的哦
        // 使用注册码
        boolean useCodeResult = registerCodeService.useCode(registerCode, user.getId());
        if (!useCodeResult) {
            /*
             * 关键：用户创建成功，但注册码消费失败，要回滚用户创建
             * 不抛异常的情况下，要手动标记当前事务状态 = 只能回滚，不能提交
             * TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
             * useCode失败应该是业务异常，而不是系统异常
             */
            throw new BusinessException(ErrorCode.REGISTER_CODE_ERROR, "注册码使用失败，请重新再试");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        // 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能为空");
        }
        // 账号长度不小于6位
        if (userAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于6位");
        }
        // 密码长度不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8位");
        }
        // 账号不能包含特殊字符
        /*
         * ^            // 从字符串开头开始匹配
         * [a-zA-Z0-9_] // 只允许英文字母、数字、下划线
         * +            // 至少出现一次
         * $            // 匹配到字符串结尾
         */
        String regex = "^[a-zA-Z0-9_]+$";
        if (!userAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 2. 加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 校验密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();// 创建查询条件构造器
        queryWrapper.eq("userAccount", userAccount);// 设置查询条件：userAccount 字段等于 userAccount
        queryWrapper.eq("userPassword", encryptedPassword);// 设置查询条件：userPassword 字段等于 encryptedPassword
        User user = userMapper.selectOne(queryWrapper);// 这里用userMapper的方法了，要注入，select查询
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4. 用户信息脱敏
        User safeUser = getSafeUser(user);
        // 5. 记录用户的登录态
        /*
         * 逻辑上是：
         * 把 safeUser 存到当前用户对应的 Session 里面。
         * 更准确地说，默认情况下它存到：
         * 后端服务器的 Session 区域里，也就是服务器内存中。
         */
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safeUser);

        return safeUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateCurrentUserProfile(User userProfile, HttpServletRequest request) {
        /*
         * request不为空
         */
        if (request == null || userProfile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        /*
         * session不为空
         * getSession(false) 的意思是：
         * 如果当前请求已经带了有效的 JSESSIONID，并且服务端找得到对应 Session，就返回这个 Session
         * 如果没有 Session，不要新建，直接返回 null
         *
         * getSession()默认会在没有 Session 时新建一个空 Session
         * 所以没有false的话session就不会为空，直接看登录态字段也能判断是否登录
         * 但有false在session层面就能知道有没有登录，有没有拿到JSESSIONID了
         */
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        /*
         * 登录态不为空
         * session不为空但是登录态为空是有可能的，
         * 比如说直接调用request.getSession()
         * 比如说刚刚退出
         */
        if (!(userObj instanceof User loginUser) || loginUser.getId() == null || loginUser.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        String username = normalizeText(userProfile.getUsername());
        String avatarUrl = normalizeText(userProfile.getAvatarUrl());
        String phone = normalizeText(userProfile.getPhone());
        String email = normalizeText(userProfile.getEmail());
        Integer gender = userProfile.getGender();

        /*
         * 个人资料的校验
         */
        validateProfile(username, avatarUrl, phone, email, gender);

        /*
         * 构造一条 UPDATE SQL
         * 创建一个更新条件构造器，用来拼更新语句
         * 设置更新条件：WHERE id = 当前登录用户ID
         * 后面的set表示要更新哪些字段
         */
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, loginUser.getId())
                .set(User::getUsername, username)
                .set(User::getAvatarUrl, avatarUrl)
                .set(User::getPhone, phone)
                .set(User::getEmail, email)
                .set(User::getGender, gender);

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在或资料更新失败");
        }

        User latestUser = this.getById(loginUser.getId());
        if (latestUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        User safeUser = getSafeUser(latestUser);
        session.setAttribute(UserConstant.USER_LOGIN_STATE, safeUser);
        return safeUser;
    }

    /**
     * 用户脱敏
     *
     * @param user 原始用户
     * @return 脱敏用户
     */
    @Override
    public @NonNull User getSafeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setGender(user.getGender());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
        return safeUser;
    }

    /**
     * 字符串清洗
     * 1.去掉前后空格
     * 2.如果去掉空格后是空字符串，就变成 null
     *
     * @param value 字符串
     * @return 清洗后的字符串
     */
    private String normalizeText(String value) {
        return StringUtils.trimToNull(value);
    }

    /**
     * 校验个人资料
     *
     * @param username  用户名
     * @param avatarUrl 头像
     * @param phone     电话号码
     * @param email     邮箱
     * @param gender    性别
     */
    private void validateProfile(String username, String avatarUrl, String phone, String email, Integer gender) {
        if (username != null && username.length() > MAX_TEXT_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称过长");
        }
        if (avatarUrl != null) {
            if (avatarUrl.length() > MAX_AVATAR_URL_LENGTH) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像地址过长");
            }
            if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像地址必须以 http:// 或 https:// 开头");
            }
        }
        /*
         * 必须是 11 位数字
         * 第 1 位必须是 1
         * 第 2 位必须是 3 到 9
         * 后面 9 位可以是任意数字 0 到 9
         */
        if (phone != null && (!phone.matches("^1[3-9]\\d{9}$") || phone.length() > MAX_TEXT_LENGTH)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }
        /*
         * ^           从开头开始
         * [^\s@]+     @ 前面至少 1 个字符，不能是空白字符，也不能是 @
         * @           必须有一个 @
         * [^\s@]+     @ 后面至少 1 个字符，不能是空白字符，也不能是 @
         * \.          必须有一个点 .
         * [^\s@]+     点后面至少 1 个字符，不能是空白字符，也不能是 @
         * $           到结尾结束
         *
         * [ ... ]表示一个字符范围。
         * ^如果 ^ 放在 [] 里面最前面，表示“取反”，也就是“不要这些字符”。
         * \s表示空白字符，比如：空格、Tab、换行
         * +表示前面的规则重复 1 次或多次。
         */
        if (email != null && (email.length() > MAX_TEXT_LENGTH || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        if (gender != null && gender != 0 && gender != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "性别参数不正确");
        }
    }
}




