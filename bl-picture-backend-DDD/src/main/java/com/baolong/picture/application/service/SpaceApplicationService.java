package com.baolong.picture.application.service;

import com.baolong.picture.domain.space.entity.Space;
import com.baolong.picture.domain.user.entity.User;
import com.baolong.picture.infrastructure.comment.DeleteRequest;
import com.baolong.picture.interfaces.dto.space.SpaceAddRequest;
import com.baolong.picture.interfaces.dto.space.SpaceEditRequest;
import com.baolong.picture.interfaces.dto.space.SpaceQueryRequest;
import com.baolong.picture.interfaces.dto.space.SpaceUpdateRequest;
import com.baolong.picture.interfaces.vo.space.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 空间应用服务接口
 */
public interface SpaceApplicationService {

	// region 增删改

	/**
	 * 创建空间
	 *
	 * @param spaceAddRequest 创建空间请求
	 * @param request         HttpServletRequest
	 * @return 空间ID
	 */
	Long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request);

	/**
	 * 删除空间
	 *
	 * @param deleteRequest 删除请求
	 * @param request       HttpServletRequest
	 * @return 是否成功
	 */
	Boolean deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request);

	/**
	 * 编辑空间
	 *
	 * @param spaceEditRequest 编辑空间请求
	 * @param request          HttpServletRequest
	 * @return 是否成功
	 */
	Boolean editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request);

	/**
	 * 更新空间（管理员）
	 *
	 * @param spaceUpdateRequest 更新空间请求
	 * @return 是否成功
	 */
	Boolean updateSpaceAsAdmin(SpaceUpdateRequest spaceUpdateRequest);

	/**
	 * 更新空间（管理员）
	 *
	 * @param updateWrapper 更新条件
	 * @return 是否成功
	 */
	Boolean updateSpaceAsAdmin(LambdaUpdateWrapper<Space> updateWrapper);

	// endregion 增删改

	/**
	 * 获取查询条件
	 *
	 * @param spaceQueryRequest 查询条件
	 * @return 查询条件对象
	 */
	QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

	/**
	 * 获取空间封装类
	 *
	 * @param space   空间
	 * @param request HttpServletRequest
	 * @return 空间封装类
	 */
	SpaceVO getSpaceVO(Space space, HttpServletRequest request);

	/**
	 * 分页获取空间封装
	 *
	 * @param spacePage 空间分页对象
	 * @param request   HttpServletRequest
	 * @return Page<SpaceVO>
	 */
	Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

	/**
	 * 校验空间权限
	 *
	 * @param loginUser 当前登录用户
	 * @param space     空间对象
	 */
	void checkSpaceAuth(Space space, User loginUser);

	/**
	 * 根据空间ID获取空间
	 *
	 * @param spaceId 空间ID
	 * @return 空间
	 */
	Space getSpaceById(Long spaceId);

	/**
	 * 根据空间ID获取空间（脱敏）
	 *
	 * @param spaceId 空间ID
	 * @param request HttpServletRequest
	 * @return 空间脱敏信息
	 */
	SpaceVO getSpaceVOById(Long spaceId, HttpServletRequest request);

	/**
	 * 获取空间列表（分页）
	 *
	 * @param spaceQueryRequest 空间查询请求
	 * @return 空间分页列表
	 */
	Page<Space> getSpaceListOfPage(SpaceQueryRequest spaceQueryRequest);

	/**
	 * 获取空间列表（根据空间ID列表）
	 *
	 * @param spaceIdSet 空间ID列表
	 * @return 空间列表
	 */
	List<Space> getSpaceListByIds(Set<Long> spaceIdSet);

	/**
	 * 获取空间列表（根据查询条件）
	 *
	 * @param queryWrapper 查询条件
	 * @return 空间列表
	 */
	List<Space> getSpaceList(QueryWrapper<Space> queryWrapper);
}
