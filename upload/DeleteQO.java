package com.diditech.dd.datacenter.ops.upload;

import lombok.Data;

import java.util.List;

/**
 * 删除图片请求类
 */
@Data
public class DeleteQO {

	/**
	 * 需要删除的图片
	 */
	private List<Object> preDelImg;
	/**
	 * 删除的图片目录
	 */
	private String relativePath;

//	private List<String>preSaveImg;

}
