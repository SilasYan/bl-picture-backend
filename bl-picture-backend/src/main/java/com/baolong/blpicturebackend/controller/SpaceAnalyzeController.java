package com.baolong.blpicturebackend.controller;

import com.baolong.blpicturebackend.comment.BaseResponse;
import com.baolong.blpicturebackend.comment.ResultUtils;
import com.baolong.blpicturebackend.exception.ErrorCode;
import com.baolong.blpicturebackend.exception.ThrowUtils;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceCategoryAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceRankAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceSizeAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceTagAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceUsageAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceUserAnalyzeRequest;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.blpicturebackend.model.entity.User;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceCategoryAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceSizeAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceTagAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceUsageAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceUserAnalyzeResponse;
import com.baolong.blpicturebackend.service.SpaceAnalyzeService;
import com.baolong.blpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间分析控制层
 *
 * @author Baolong 2025年02月27 20:14
 * @version 1.0
 * @since 1.8
 */
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

	@Resource
	private SpaceAnalyzeService spaceAnalyzeService;

	@Resource
	private UserService userService;

	/**
	 * 空间使用分析
	 */
	@PostMapping("/usage")
	public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
			@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
			HttpServletRequest request
	) {
		ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
		return ResultUtils.success(spaceUsageAnalyze);
	}

	/**
	 * 空间分类分析
	 */
	@PostMapping("/category")
	public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		List<SpaceCategoryAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
		return ResultUtils.success(resultList);
	}

	/**
	 * 空间标签分析
	 */
	@PostMapping("/tag")
	public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
		return ResultUtils.success(resultList);
	}

	/**
	 * 空间大小分析
	 */
	@PostMapping("/size")
	public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
		return ResultUtils.success(resultList);
	}

	/**
	 * 空间用户上传分析
	 */
	@PostMapping("/user")
	public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
		return ResultUtils.success(resultList);
	}

	/**
	 * 空间排行分析
	 */
	@PostMapping("/rank")
	public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		List<Space> resultList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
		return ResultUtils.success(resultList);
	}

}
