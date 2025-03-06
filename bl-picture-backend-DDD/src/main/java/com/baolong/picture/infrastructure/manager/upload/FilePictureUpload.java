package com.baolong.picture.infrastructure.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.baolong.picture.infrastructure.exception.ErrorCode;
import com.baolong.picture.infrastructure.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传实现
 *
 * @author Baolong 2025年02月15 20:52
 * @version 1.0
 * @since 1.8
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
	/**
	 * 校验输入源（本地文件或 URL）
	 *
	 * @param inputSource 文件输入源
	 */
	@Override
	protected void validPicture(Object inputSource) {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
		// 1. 校验文件大小
		long fileSize = multipartFile.getSize();
		// final long ONE_M = 1024 * 1024L;
		// ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
		// 2. 校验文件后缀
		String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		// 允许上传的文件后缀
		final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp", "gif");
		ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
	}

	/**
	 * 获取输入源的原始文件名
	 *
	 * @param inputSource 文件输入源
	 * @return 原始文件名
	 */
	@Override
	protected String getOriginFilename(Object inputSource) {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		return multipartFile.getOriginalFilename();
	}

	/**
	 * 处理输入源并生成本地临时文件
	 *
	 * @param inputSource 文件输入源
	 * @param file        文件对象
	 * @throws Exception e
	 */
	@Override
	protected void processFile(Object inputSource, File file) throws Exception {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		multipartFile.transferTo(file);
	}
}
