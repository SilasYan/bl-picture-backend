package com.baolong.pictures;

import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ReUtil;

/**
 * 测试一下
 *
 * @author Baolong 2025年03月07 00:12
 * @version 1.0
 * @since 1.8
 */
public class TestMain {
	public static void main(String[] args) {
		boolean match = ReUtil.isMatch(RegexPool.EMAIL, "510132075@qq.com");
		System.out.println(match);
	}
}
