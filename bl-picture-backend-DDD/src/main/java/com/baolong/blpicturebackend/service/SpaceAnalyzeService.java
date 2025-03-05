package com.baolong.blpicturebackend.service;

import com.baolong.blpicturebackend.model.dto.analyze.SpaceCategoryAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceRankAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceSizeAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceTagAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceUsageAnalyzeRequest;
import com.baolong.blpicturebackend.model.dto.analyze.SpaceUserAnalyzeRequest;
import com.baolong.blpicturebackend.model.entity.Space;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceCategoryAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceSizeAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceTagAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceUsageAnalyzeResponse;
import com.baolong.blpicturebackend.model.vo.analyze.SpaceUserAnalyzeResponse;

import java.util.List;

/**
 * 空间分析服务
 *
 * @author Baolong 2025年02月27 19:17
 * @version 1.0
 * @since 1.8
 */
public interface SpaceAnalyzeService {

	/**
	 * 获取空间使用分析数据
	 *
	 * @param spaceUsageAnalyzeRequest 空间资源分析请求
	 * @param loginUser                当前登录用户
	 * @return SpaceUsageAnalyzeResponse
	 */
	SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

	/**
	 * 获取空间分类分析数据
	 *
	 * @param spaceCategoryAnalyzeRequest 空间分类分析请求
	 * @param loginUser                   当前登录用户
	 * @return List<SpaceCategoryAnalyzeResponse>
	 */
	List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

	/**
	 * 获取空间标签分析数据
	 *
	 * @param spaceTagAnalyzeRequest 空间标签分析请求
	 * @param loginUser              当前登录用户
	 * @return List<SpaceTagAnalyzeResponse>
	 */
	List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

	/**
	 * 获取空间大小分析数据
	 *
	 * @param spaceSizeAnalyzeRequest 空间大小分析请求
	 * @param loginUser               当前登录用户
	 * @return List<SpaceSizeAnalyzeResponse>
	 */
	List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

	/**
	 * 获取空间用户上传分析数据
	 *
	 * @param spaceUserAnalyzeRequest 空间用户上传分析请求
	 * @param loginUser               当前登录用户
	 * @return List<SpaceUserAnalyzeResponse>
	 */
	List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

	/**
	 * 获取空间排行分析数据
	 *
	 * @param spaceRankAnalyzeRequest 空间排名分析请求
	 * @param loginUser               当前登录用户
	 * @return List<Space>
	 */
	List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
