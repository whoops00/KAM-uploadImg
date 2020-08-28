package com.diditech.dd.datacenter.ops.upload.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.diditech.dd.datacenter.ops.api.query.Constants;
import com.diditech.dd.datacenter.ops.upload.DeleteQO;
import com.diditech.dd.datacenter.ops.upload.api.ISysBaseAPI;
import com.diditech.dd.datacenter.ops.upload.utils.CommonUtils;
import com.diditech.diditech.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 文件上传
 * @Author hfx
 */
@Slf4j
@RestController
@RequestMapping("/uploadCommon")
public class CommonController {

	@Autowired(required = false)
	private ISysBaseAPI sysBaseAPI;

	@Value(value = "${upload.path.upload}")
	private String uploadpath;

	/**
	 * 本地：local minio：minio 阿里：alioss
	 */
	@Value(value="${upload.uploadType}")
	private String uploadType;

	/**
	 * 文件上传统一方法
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping(value = "/upload")
	public R<?> upload(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("上传路径uploadpath:{}", uploadpath);
		log.info("上传类型uploadType:{}", uploadType);

		R result = new R();
		String savePath = "";
//		String bizPath = request.getParameter("path");
		String bizPath = request.getHeader("path");
		log.info("bizPath:{}", bizPath);

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multipartRequest.getFile("file");// 获取上传文件对象
		if(StrUtil.isEmpty(bizPath)){
			if(Constants.UPLOAD_TYPE_OSS.equals(uploadType)){
				//未指定目录，则用阿里云默认目录 upload ,使用阿里云文件上传时，必须添加目录！
				bizPath = "upload";
			}else{
				bizPath = "didiDefaultFile";
			}
		}
		log.info("文件目录为path:{}", bizPath);
		if(Constants.UPLOAD_TYPE_LOCAL.equals(uploadType)){
			//针对jeditor编辑器如何使 lcaol模式，采用 base64格式存储
			String jeditor = request.getParameter("jeditor");
			if(StrUtil.isNotEmpty(jeditor)){
				result.setMsg(Constants.UPLOAD_TYPE_LOCAL);
				result.setData(Constants.UPLOAD_TYPE_LOCAL);
				result.setCode(0);
				return result;
			}else{
				savePath = this.uploadLocal(file,bizPath);
			}
		}else{
			savePath = sysBaseAPI.upload(file,bizPath,uploadType);
		}
		if(StrUtil.isNotEmpty(savePath)){
			result.setData(savePath);
			result.setCode(0);
		}else {
			result.setMsg("上传失败！");
			result.setCode(1);
		}
		return result;
	}

	/**
	 * 本地文件上传
	 * @param mf 文件
	 * @param bizPath  自定义路径
	 * @return
	 */
	private String uploadLocal(MultipartFile mf, String bizPath){
		try {
			String ctxPath = uploadpath;
			String fileName = null;
			File file = new File(ctxPath + File.separator + bizPath + File.separator );
			if (!file.exists()) {
				file.mkdirs();// 创建文件根目录
			}
			String orgName = mf.getOriginalFilename();// 获取文件名
			orgName = CommonUtils.getFileName(orgName);
			if(orgName.indexOf(".")!=-1){
				fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
			}else{
				fileName = orgName+ "_" + System.currentTimeMillis();
			}
			String savePath = file.getPath() + File.separator + fileName;
			File savefile = new File(savePath);
			FileCopyUtils.copy(mf.getBytes(), savefile);
			String dbpath = null;
			if(StrUtil.isNotEmpty(bizPath)){
				dbpath = bizPath + File.separator + fileName;
			}else{
				dbpath = fileName;
			}
			if (dbpath.contains("\\")) {
				dbpath = dbpath.replace("\\", "/");
			}
			return dbpath;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return "";
	}

	/**
	 * 删除图片
	 * @return
	 */
	@DeleteMapping(value = "/deleteImg")
	public R<?> deleteImg(@RequestBody DeleteQO deleteQO) {
//		// 是否有需要删除的图片
//		if (CollUtil.isEmpty(preDelImg))
//			return R.ok("没有需要删除的图片！");
//		Collection<String> preDelColl = new ArrayList<>(preDelImg);
//		boolean delImg = preDelColl.removeAll(preSaveImg);
		List<Object> preDelImg = deleteQO.getPreDelImg();
		String relativePath = deleteQO.getRelativePath();
		if (CollUtil.isNotEmpty(preDelImg)) {
			preDelImg.stream().forEach(img -> {
				log.info("删除的图片为：img:{}", img);
				img = relativePath + "/" + StrUtil.subAfter((String)img, "/", true);
				sysBaseAPI.delImg(uploadType, (String) img);
			});
			return R.ok("已删除图片:" + preDelImg);
		}
		return R.ok("没有需要删除的新图片！");
	}

}
