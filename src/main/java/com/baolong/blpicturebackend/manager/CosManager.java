package com.baolong.blpicturebackend.manager;

import com.baolong.blpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * 腾讯云对象存储服务
 * <p>
 * 叫做 XXXManager 主要是做区分, 表示这个类是可以单独抽取出去的
 */
@Component
public class CosManager {

	@Resource
	private CosClientConfig cosClientConfig;

	@Resource
	private COSClient cosClient;

	/**
	 * 上传对象
	 *
	 * @param key  唯一键
	 * @param file 文件
	 */
	public PutObjectResult putObject(String key, File file) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
		return cosClient.putObject(putObjectRequest);
	}

	/**
	 * 下载对象
	 *
	 * @param key 唯一键
	 */
	public COSObject getObject(String key) {
		GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
		return cosClient.getObject(getObjectRequest);
	}

	/**
	 * 上传对象（附带图片信息）
	 * <p>
	 * <a href="https://cloud.tencent.com/document/product/436/55377">这里用到了 腾讯 COS 的数据万象</a>
	 *
	 * @param key  唯一键
	 * @param file 文件
	 */
	public PutObjectResult putPictureObject(String key, File file) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
		// 对图片进行处理（获取基本信息也被视作为一种处理）
		PicOperations picOperations = new PicOperations();
		// 1 表示返回原图信息
		picOperations.setIsPicInfo(1);
		// 构造处理参数
		putObjectRequest.setPicOperations(picOperations);
		return cosClient.putObject(putObjectRequest);
	}

}
