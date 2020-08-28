package com.diditech.dd.datacenter.ops.upload.api;
import org.springframework.web.multipart.MultipartFile;

/**
 * 底层共通业务API，提供其他独立模块调用
 * @Author: hfx
 * @Version:V1.0
 */
public interface ISysBaseAPI {
	/**
	 * 文件上传
	 * @param file 文件
	 * @param bizPath 自定义路径
	 * @param uploadType 上传方式
	 * @return
	 */
	public String upload(MultipartFile file, String bizPath, String uploadType);

	/**
	 * 文件上传 自定义桶
	 * @param file
	 * @param bizPath
	 * @param uploadType
	 * @param customBucket
	 * @return
	 */
	public String upload(MultipartFile file, String bizPath, String uploadType, String customBucket);

	/**
	 * 文件删除
	 * @param delType
	 * @param delImgName
	 * @return
	 */
	public void delImg(String delType, String delImgName);

}
