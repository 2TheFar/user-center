package com.zhiyuan.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiyuan.usercenter.mapper.RegisterCodeMapper;
import com.zhiyuan.usercenter.model.domain.RegisterCode;
import com.zhiyuan.usercenter.service.RegisterCodeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Service
public class RegisterCodeServiceImpl extends ServiceImpl<RegisterCodeMapper, RegisterCode>
        implements RegisterCodeService {

    /**
     * 注册码长度
     */
    private static final int CODE_LENGTH = 12;

    /**
     * 注册码字符集：大小写字母 + 数字
     */
    private static final String CODE_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 注册码格式校验
     * 创建一个正则表达式规则，用来判断字符串是不是“12 位注册码”
     * ^          表示从字符串开头开始匹配
     * [a-zA-Z0-9] 表示只能是小写字母、大写字母、数字
     * {12}       表示前面的字符必须刚好出现 12 次
     * $          表示匹配到字符串结尾
     * Pattern.compile()表示把一个“正则表达式字符串”编译成一个 Pattern 对象
     */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{12}$");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String generateCode() {
        // 最多尝试 20 次，避免极小概率重复导致死循环
        for (int i = 0; i < 20; i++) {
            String code = randomCode();

            RegisterCode registerCode = new RegisterCode();
            registerCode.setCode(code);
            registerCode.setStatus(0);

            try {
                /*
                 * 继承了ServiceImpl随便用
                 * 等价于
                 * INSERT INTO register_code (code, status) VALUES ('abc123DEF456', 0);
                 * save(registerCode) 里的对象怎么知道存到哪张表？
                 * 因为实体类上写了@TableName("register_code")
                 */
                boolean saveResult = this.save(registerCode);
                if (saveResult) {
                    return code;
                }
            } catch (DuplicateKeyException e) {
                // 如果因为 code 重复了，数据库会拒绝插入
                // 然后 Spring / MyBatis-Plus 会抛出异常
                // DuplicateKeyException
                // catch的作用是
                // 如果随机出来的注册码重复了，不让程序直接崩掉，而是忽略这次失败，进入下一轮循环，重新生成一个新的注册码。
                // code 有唯一索引，极小概率重复，继续生成即可
            }
        }
        return null;
    }

    @Override
    public boolean checkCode(String code) {
        /*
         * 虽然可以直接查数据库，功能上大多数情况下也能返回 false。
         * 前面先做格式校验，是为了更规范、更安全、更省资源。
         * 1. 避免无意义查询
         * 2. 防止垃圾输入直接打到数据库
         * 3. 让业务逻辑更清楚
         * 4. 和后面的 useCode 保持一致
         */
        if (!isValidCodeFormat(code)) {
            return false;
        }
        /*
         * 1. 创建一个查询条件对象，可以理解为准备开始拼 WHERE 条件
         * 2. 添加条件：
         * `code` = 传进来的 code
         * `status` = 0
         * 3. 使用提供的统计（查询）方法：统计满足条件的数据有多少条
         *
         * RegisterCode::getCode 是 Java 里的方法引用写法。
         * 等价于：registerCode -> registerCode.getCode()
         * 意思是：拿 RegisterCode 对象的 getCode 方法
         *
         * 为什么不直接写 "code"？
         * 其实也可以写：queryWrapper.eq("code", code);
         * 但用方法引用更加安全，不会写错
         *
         * 为什么等价？
         * 因为在 MyBatis-Plus 的 LambdaQueryWrapper 里，它会把：
         * RegisterCode::getCode
         * 解析成实体类属性名：
         * code
         *
         * 转换逻辑是：
         *
         * RegisterCode::getCode
         *         ↓
         * 调用的是 getCode()
         *         ↓
         * 去掉 get 前缀
         *         ↓
         * Code 变成 code
         *         ↓
         * 得到实体属性名 code
         *         ↓
         * 再根据实体类映射到数据库字段 code
         *
         * 但是严格说，RegisterCode::getCode 不是 Java 原生等价于 "code"。
         * 它只是被 MyBatis-Plus 解析后，最终效果等价于 "code"。
         * Java 看它：这是一个方法引用
         * MyBatis-Plus 看它：我要从这个方法引用里推断字段名
         * 最终推断出：code
         */
        LambdaQueryWrapper<RegisterCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RegisterCode::getCode, code);
        queryWrapper.eq(RegisterCode::getStatus, 0);

        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean useCode(String code, Long userId) {
        /*
         * 为什么这里不直接用checkCode校验是否可用？
         *
         * 首先因为这里主要是在做基础参数校验
         * 更关键的是，useCode 不需要checkCode，本来就会先判断Code是否可用，Code存在且未使用，然后才会更新。
         * UPDATE register_code
         * SET status = 1, usedBy = ?
         * WHERE code = ?
         * AND status = 0;
         * 并且如果两个用户几乎同时用同一个注册码注册，只有第一个能更新成功，第二个会更新失败。
         *
         * 如果写了checkCode反而会：
         * 第一，多查了一次数据库。
         * 第二，并不能100%确保真的可用，先查再改不是绝对安全。
         * 可能发生这种情况：
         * A 用户 checkCode 通过
         * B 用户 checkCode 也通过
         * A 用户成功使用注册码
         * B 用户再去使用
         * 所以最终还是必须靠 UPDATE ... WHERE status = 0 来兜底
         * 校验结果不一定对，那这个校验就失去意义了，从根本上就不需要checkCode了。
         */
        if (!isValidCodeFormat(code) || userId == null || userId <= 0) {
            return false;
        }
        /*
         * 这一部分表示：我要改什么字段。
         *
         * 为什么不用把实体对象的属性全设置了？
         * 这个对象不是完整实体，而是一个更新用的实体对象。
         * 一般情况下，MyBatis-Plus 更新时会只更新非 null 字段。
         * Java对象的属性默认值可能会导致预期之外的更新，
         * 所以实体类里数据库字段一般推荐用包装类型。
         * 核心思想：
         * MyBatis-Plus 默认不是按“整个对象覆盖数据库”，而是按“实体对象中需要更新的字段”生成 SET 语句。
         */
        RegisterCode updateRegisterCode = new RegisterCode();
        updateRegisterCode.setStatus(1);
        updateRegisterCode.setUsedBy(userId);
        /*
         * 这一部分表示：我要改哪一行数据。
         */
        LambdaUpdateWrapper<RegisterCode> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RegisterCode::getCode, code);
        updateWrapper.eq(RegisterCode::getStatus, 0);

        // 关键点：只有 status = 0 的注册码才能被更新为已使用
        // 这样可以避免并发情况下同一个注册码被重复使用
        return this.update(updateRegisterCode, updateWrapper);
    }

    /**
     * 生成随机注册码
     */
    private String randomCode() {
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            // 从 CODE_CHARS 这个字符串里，随机生成一个合法下标，对应的字符就是随机字符。
            int index = SECURE_RANDOM.nextInt(CODE_CHARS.length());
            codeBuilder.append(CODE_CHARS.charAt(index));
        }

        return codeBuilder.toString();
    }

    /**
     * 校验注册码格式
     */
    private boolean isValidCodeFormat(String code) {
        /*
         * 判断 code 是否有内容
         * 它会排除：
         * null
         * ""
         * "   "
         */
        if (!StringUtils.hasText(code)) {
            return false;
        }
        /*
         * 判断长度是不是 12。
         */
        if (code.length() != CODE_LENGTH) {
            return false;
        }
        /*
         * 判断是否符合正则规则。
         * Pattern = 正则规则本身，CODE_PATTERN 是一个 Pattern 规则对象
         * Matcher = 用这个规则检查某个字符串的匹配器
         * matches() = 判断整个字符串是否完全符合规则
         * 整个流程是：
         * 1. Pattern.compile(...) 创建一条正则规则
         * 2. CODE_PATTERN.matcher(code) 用这条规则检查 code（生成一个 Matcher 对象，可以理解成“匹配器”）
         * 3. matches() 判断 code 是否完全符合规则
         * 4. 符合返回 true，不符合返回 false
         */
        if (!CODE_PATTERN.matcher(code).matches()) {
            return false;
        }

        return true;
        /*
         * 更简化的写法：
         * return StringUtils.hasText(code)
         *         && code.length() == CODE_LENGTH
         *         && CODE_PATTERN.matcher(code).matches();
         */
    }
}
