package com.diditech.dd.datacenter.ops.upload.utils;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云 oss 上传工具类
 */
@Slf4j
@Component
public class OssBootUtil{

	private static String endPoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;
    private static String staticDomain;

	@Value(value = "${upload.oss.endpoint}")
	public void setEndPoint(String endPoint) {
		OssBootUtil.endPoint = endPoint;
	}
	@Value(value = "${upload.oss.accessKey}")
	public void setAccessKeyId(String accessKeyId) {
		OssBootUtil.accessKeyId = accessKeyId;
	}
	@Value(value = "${upload.oss.secretKey}")
	public void setAccessKeySecret(String accessKeySecret) {
		OssBootUtil.accessKeySecret = accessKeySecret;
	}
	@Value(value = "${upload.oss.bucketName}")
	public void setBucketName(String bucketName) {
		OssBootUtil.bucketName = bucketName;
	}
//	@Value(value = "${upload.oss.staticDomain}")
	public void setStaticDomain(String staticDomain) {
		OssBootUtil.staticDomain = staticDomain;
	}

	/**
     * oss 工具客户端
     */
    private static OSSClient ossClient = null;

    /**
     * 上传文件至阿里云 OSS
     * 文件上传成功,返回文件完整访问路径
     * 文件上传失败,返回 null
     *
     * @param file    待上传文件
     * @param fileDir 文件保存目录
     * @return oss 中的相对文件路径
     */
    public static String upload(MultipartFile file, String fileDir, String customBucket) {
    	log.info("oss配置信息：enPoint:{},accessKeyId:{},accessKeySecret:{},bucketName:{},staticDomain:{}", endPoint, accessKeyId, accessKeySecret, bucketName, staticDomain);
        String FILE_URL = null;
        initOSS(endPoint, accessKeyId, accessKeySecret);
        StringBuilder fileUrl = new StringBuilder();
        String newBucket = bucketName;
        if(StrUtil.isNotEmpty(customBucket)){
            newBucket = customBucket;
        }
        try {
            //判断桶是否存在,不存在则创建桶
            if(!ossClient.doesBucketExist(newBucket)){
                ossClient.createBucket(newBucket);
            }
            // 获取文件名
            String orgName = file.getOriginalFilename();
            orgName = CommonUtils.getFileName(orgName);
            String fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
            if (!fileDir.endsWith("/")) {
                fileDir = fileDir.concat("/");
            }
            fileUrl = fileUrl.append(fileDir + fileName);

            if (StrUtil.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith("http")) {
                FILE_URL = staticDomain + "/" + fileUrl;
            } else {
                FILE_URL = "https://" + newBucket + "." + endPoint + "/" + fileUrl;
            }
            PutObjectResult result = ossClient.putObject(newBucket, fileUrl.toString(), file.getInputStream());
            // 设置权限(公开读)
//            ossClient.setBucketAcl(newBucket, CannedAccessControlList.PublicRead);
            if (result != null) {
                log.info("------OSS文件上传成功------" + fileUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return FILE_URL;
    }

    /**
     * 文件上传
     * @param file
     * @param fileDir
     * @return
     */
    public static String upload(MultipartFile file, String fileDir) {
        return upload(file, fileDir,null);
    }

    /**
     * 上传文件至阿里云 OSS
     * 文件上传成功,返回文件完整访问路径
     * 文件上传失败,返回 null
     *
     * @param file    待上传文件
     * @param fileDir 文件保存目录
     * @return oss 中的相对文件路径
     */
    public static String upload(FileItemStream file, String fileDir) {
        String FILE_URL = null;
        initOSS(endPoint, accessKeyId, accessKeySecret);
        StringBuilder fileUrl = new StringBuilder();
        try {
            String suffix = file.getName().substring(file.getName().lastIndexOf('.'));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            if (!fileDir.endsWith("/")) {
                fileDir = fileDir.concat("/");
            }
            fileUrl = fileUrl.append(fileDir + fileName);

            if (StrUtil.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith("http")) {
                FILE_URL = staticDomain + "/" + fileUrl;
            } else {
                FILE_URL = "https://" + bucketName + "." + endPoint + "/" + fileUrl;
            }
            PutObjectResult result = ossClient.putObject(bucketName, fileUrl.toString(), file.openStream());
            // 设置权限(公开读)
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            if (result != null) {
                log.info("------OSS文件上传成功------" + fileUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return FILE_URL;
    }

    /**
     * 删除文件
     * @param url
     */
    public static void deleteUrl(String url) {
        deleteUrl(url,null);
    }

    /**
     * 删除文件
     * @param url
     */
    public static void deleteUrl(String url,String bucket) {
        String newBucket = bucketName;
        if(StrUtil.isNotEmpty(bucket)){
            newBucket = bucket;
        }
        String bucketUrl = "";
        if (StrUtil.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith("http")) {
            bucketUrl = staticDomain + "/" ;
        } else {
            bucketUrl = "https://" + newBucket + "." + endPoint + "/";
        }
        url = url.replace(bucketUrl,"");
        ossClient.deleteObject(newBucket, url);
    }

    /**
     * 删除文件
     * @param fileName
     */
    public static void delete(String fileName) {
        ossClient.deleteObject(bucketName, fileName);
    }

    /**
     * 获取文件流
     * @param objectName
     * @param bucket
     * @return
     */
    public static InputStream getOssFile(String objectName,String bucket){
        InputStream inputStream = null;
        try{
            String newBucket = bucketName;
            if(StrUtil.isNotEmpty(bucket)){
                newBucket = bucket;
            }
            initOSS(endPoint, accessKeyId, accessKeySecret);
            OSSObject ossObject = ossClient.getObject(newBucket,objectName);
            inputStream = new BufferedInputStream(ossObject.getObjectContent());
        }catch (Exception e){
            log.info("文件获取失败" + e.getMessage());
        }
        return inputStream;
    }

    /**
     * 获取文件流
     * @param objectName
     * @return
     */
    public static InputStream getOssFile(String objectName){
        return getOssFile(objectName,null);
    }

    /**
     * 获取文件外链
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    public static String getObjectURL(String bucketName, String objectName, Date expires) {
        initOSS(endPoint, accessKeyId, accessKeySecret);
        try{
            if(ossClient.doesObjectExist(bucketName,objectName)){
                URL url = ossClient.generatePresignedUrl(bucketName,objectName,expires);
                return URLDecoder.decode(url.toString(),"UTF-8");
            }
        }catch (Exception e){
            log.info("文件路径获取失败" + e.getMessage());
        }
        return null;
    }

    /**
     * 初始化 oss 客户端
     *
     * @return
     */
    private static OSSClient initOSS(String endpoint, String accessKeyId, String accessKeySecret) {
        if (ossClient == null) {
            ossClient = new OSSClient(endpoint,
                    new DefaultCredentialProvider(accessKeyId, accessKeySecret),
                    new ClientConfiguration());
        }
        return ossClient;
    }


    /**
     * 上传文件到oss
     * @param stream
     * @param relativePath
     * @return
     */
    public static String upload(InputStream stream, String relativePath) {
        String FILE_URL = null;
        String fileUrl = relativePath;
        initOSS(endPoint, accessKeyId, accessKeySecret);
        if (StrUtil.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith("http")) {
            FILE_URL = staticDomain + "/" + relativePath;
        } else {
            FILE_URL = "https://" + bucketName + "." + endPoint + "/" + fileUrl;
        }
        PutObjectResult result = ossClient.putObject(bucketName, fileUrl.toString(),stream);
        // 设置权限(公开读)
        ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        if (result != null) {
            log.info("------OSS文件上传成功------" + fileUrl);
        }
        return FILE_URL;
    }


}