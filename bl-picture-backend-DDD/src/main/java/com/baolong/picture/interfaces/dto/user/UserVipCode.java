package com.baolong.picture.interfaces.dto.user;

import lombok.Data;

/**
 * 用户兑换码
 */
@Data
public class UserVipCode {

    // 兑换码
    private String code;

    // 是否已经使用
    private boolean hasUsed;
}
