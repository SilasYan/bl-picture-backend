package com.baolong.picture.interfaces.controller;

import com.baolong.picture.application.service.MainApplicationService;
import com.baolong.picture.infrastructure.common.BaseResponse;
import com.baolong.picture.infrastructure.common.ResultUtils;
import com.baolong.picture.interfaces.vo.CaptchaVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/")
public class MainController {

	@Resource
	private MainApplicationService mainApplicationService;

	/**
	 * 健康检查
	 */
	@GetMapping("/health")
	public BaseResponse<String> health() {
		return ResultUtils.success("ok");
	}

	/**
	 * 获取图形验证码
	 */
	@GetMapping("/captcha")
	public BaseResponse<CaptchaVO> captcha() {
		return ResultUtils.success(mainApplicationService.getCaptcha());
	}
}
