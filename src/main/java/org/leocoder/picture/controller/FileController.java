package org.leocoder.picture.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.annotation.AuthCheck;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.constant.UserConstant;
import org.leocoder.picture.domain.dto.file.UploadPictureResult;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.manager.CosManager;
import org.leocoder.picture.manager.FileManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-12 23:27
 * @description :
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "文件上传")
public class FileController {

    private final CosManager cosManager;

    private final FileManager fileManager;

    /**
     * 文件上传
     *
     * @param multipartFile 上传的文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传", notes = "上传文件到腾讯云对象存储")
    public Result<String> UploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String uploadPathPrefix = String.format("public/%s", "avatar");
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        return ResultUtils.success(uploadPictureResult.getUrl());
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }





}
