package com.diditech.dd.datacenter.ops.upload.api.impl;

import com.diditech.dd.datacenter.ops.api.query.Constants;
import com.diditech.dd.datacenter.ops.upload.api.ISysBaseAPI;
import com.diditech.dd.datacenter.ops.upload.utils.MinioUtil;
import com.diditech.dd.datacenter.ops.upload.utils.OssBootUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
/**
 * @Description: 底层共通业务API，提供其他独立模块调用
 * @Author: hfx
 * @Version:V1.0
 */
@Slf4j
@Service
@Component
public class SysBaseApiImpl implements ISysBaseAPI {
	/** 当前系统数据库类型 */
	private static String DB_TYPE = "";

	/**
	 * minio桶名称
	 */
	private static String miniBucketName;
	@Value(value = "${upload.minio.bucketName}")
	public void setMiniBucketName(String miniBucketName) {
		SysBaseApiImpl.miniBucketName = miniBucketName;
	}
	private static String ossBucketName;
	@Value(value = "${upload.oss.bucketName}")
	public void setOssBucketName(String ossBucketName) {
		SysBaseApiImpl.ossBucketName = ossBucketName;
	}

	@Override
	public String upload(MultipartFile file, String bizPath, String uploadType) {
		String url = "";
		if(Constants.UPLOAD_TYPE_MINIO.equals(uploadType)){
			url = MinioUtil.upload(file,bizPath);
		}else{
			url = OssBootUtil.upload(file,bizPath);
		}
		return url;
	}

	@Override
	public String upload(MultipartFile file, String bizPath, String uploadType, String customBucket) {
		String url = "";
		if(Constants.UPLOAD_TYPE_MINIO.equals(uploadType)){
			url = MinioUtil.upload(file,bizPath,customBucket);
		}else{
			url = OssBootUtil.upload(file,bizPath,customBucket);
		}
		return url;
	}

	@Override
	public void delImg(String delType, String delImgName) {
		try {
			if (Constants.UPLOAD_TYPE_MINIO.equals(delType)){
				MinioUtil.removeObject(miniBucketName, delImgName);
			}else {
				OssBootUtil.delete(delImgName);
			}
		}catch (Exception e){
			log.info("文件删除失败" + e.getMessage());
		}
	}

}