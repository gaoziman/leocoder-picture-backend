package org.leocoder.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.leocoder.picture.domain.Favorite;
import org.leocoder.picture.domain.Like;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.file.UploadPictureResult;
import org.leocoder.picture.domain.dto.picture.PictureQueryRequest;
import org.leocoder.picture.domain.dto.picture.PictureReviewRequest;
import org.leocoder.picture.domain.dto.picture.PictureUploadByBatchRequest;
import org.leocoder.picture.domain.dto.picture.PictureUploadRequest;
import org.leocoder.picture.domain.vo.picture.PictureVO;
import org.leocoder.picture.domain.vo.user.UserVO;
import org.leocoder.picture.enums.PictureReviewStatusEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.manager.FileManager;
import org.leocoder.picture.manager.image.*;
import org.leocoder.picture.manager.upload.FilePictureUpload;
import org.leocoder.picture.manager.upload.PictureUploadTemplate;
import org.leocoder.picture.manager.upload.UrlPictureUpload;
import org.leocoder.picture.mapper.PictureMapper;
import org.leocoder.picture.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.leocoder.picture.utils.CaffeineClient.LOCAL_CACHE;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-12 23:10
 * @description :
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    private final FileManager fileManager;

    private final UserService userService;

    private final FilePictureUpload filePictureUpload;

    private final UrlPictureUpload urlPictureUpload;

    private final LikeService likeService;

    private final FavoriteService favoriteService;

    private final StringRedisTemplate redisTemplate;

    private final FetchedImagesService fetchedImagesService;

    private final StringRedisTemplate stringRedisTemplate;


    /**
     * 上传图片
     *
     * @param inputSource  图片输入源
     * @param requestParam 图片上传请求
     * @param loginUser    登录用户
     * @return 上传结果
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest requestParam, User loginUser) {
        if (ObjectUtil.isEmpty(inputSource)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (requestParam != null) {
            pictureId = requestParam.getId();
        }

        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 根据 inputSource 类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        // 构造要入库的图片信息
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId, requestParam);
        // 补充审核参数
        fillReviewParams(picture, loginUser);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取图片信息
     *
     * @param loginUser           登录用户
     * @param uploadPictureResult 上传图片结果
     * @param pictureId           图片 id
     * @return 图片信息
     */
    private static Picture getPicture(User loginUser, UploadPictureResult uploadPictureResult, Long pictureId, PictureUploadRequest requestParam) {
        Picture picture = Picture.builder().build();
        picture.setUrl(uploadPictureResult.getUrl());
        String picName = uploadPictureResult.getPicName();
        if (ObjectUtil.isNotEmpty(requestParam) && StrUtil.isNotBlank(requestParam.getPicName())) {
            picName = requestParam.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(LocalDateTime.now());
        }
        return picture;
    }


    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询请求参数
     * @return LambdaQueryWrapper
     */
    @Override
    public LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest requestParam) {
        if (ObjectUtil.isNull(requestParam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = requestParam.getId();
        String name = requestParam.getName();
        String introduction = requestParam.getIntroduction();
        String category = requestParam.getCategory();
        List<String> tags = requestParam.getTags();
        Long picSize = requestParam.getPicSize();
        Integer picWidth = requestParam.getPicWidth();
        Integer picHeight = requestParam.getPicHeight();
        Double picScale = requestParam.getPicScale();
        String picFormat = requestParam.getPicFormat();
        String searchText = requestParam.getSearchText();
        Long userId = requestParam.getUserId();
        String sortField = requestParam.getSortField();
        String sortOrder = requestParam.getSortOrder();
        Long reviewerId = requestParam.getReviewerId();
        String reviewMessage = requestParam.getReviewMessage();
        Integer reviewStatus = requestParam.getReviewStatus();

        LambdaQueryWrapper<Picture> lambdaQueryWrapper = Wrappers.lambdaQuery(Picture.class);

        // 多字段模糊搜索
        if (StrUtil.isNotBlank(searchText)) {
            lambdaQueryWrapper.and(qw ->
                    qw.like(Picture::getName, searchText)
                            .or()
                            .like(Picture::getIntroduction, searchText)
            );
        }

        // 条件查询
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(reviewMessage), Picture::getReviewMessage, reviewMessage);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus);

        // 排序 - 按照创建时间降序
        // 排序处理
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);

            // 根据 sortField 动态排序
            switch (sortField) {
                case "viewCount":
                    lambdaQueryWrapper.orderBy(true, isAsc, Picture::getViewCount);
                    break;
                case "likeCount":
                    lambdaQueryWrapper.orderBy(true, isAsc, Picture::getLikeCount);
                    break;
                case "favoriteCount":
                    lambdaQueryWrapper.orderBy(true, isAsc, Picture::getFavoriteCount);
                    break;
                case "createTime":
                default:
                    lambdaQueryWrapper.orderBy(true, isAsc, Picture::getCreateTime);
                    break;
            }
        } else {
            // 默认按照创建时间降序排序
            lambdaQueryWrapper.orderByDesc(Picture::getCreateTime);
        }
        // JSON 数组查询（标签）
        if (ObjUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                lambdaQueryWrapper.like(Picture::getTags, "\"" + tag + "\"");
            }
        }

        // 排序
        // lambdaQueryWrapper.orderBy(ObjUtil.isNotEmpty(sortField),
        //         "ascend".equals(sortOrder),
        //         sortField != null ? Picture::getName : Picture::getId);

        return lambdaQueryWrapper;
    }


    /**
     * 获取图片信息封装类
     *
     * @param picture 图片信息
     * @param request 请求对象
     * @return 图片信息封装类
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
            // 查询图片的点赞状态
            Like pictureLike = likeService.getOne(Wrappers.lambdaQuery(Like.class)
                    .eq(Like::getPictureId, picture.getId())
                    .eq(Like::getUserId, loginUser.getId())
                    .eq(Like::getLikeType, 0));
            // 获取图片的收藏状态
            Favorite favorite = favoriteService.getOne(Wrappers.lambdaQuery(Favorite.class)
                    .eq(Favorite::getPictureId, picture.getId())
                    .eq(Favorite::getUserId, loginUser.getId()));
            // 填充点赞状态
            if (ObjectUtil.isNotNull(favorite)) {
                pictureVO.setIsFavorited(favorite.getIsFavorited());
            } else {
                pictureVO.setIsFavorited(0);
            }
            // 填充图片点赞状态
            if (ObjectUtil.isNotNull(pictureLike)) {
                pictureVO.setIsLiked(pictureLike.getIsLiked());
            } else {
                pictureVO.setIsLiked(0);
            }
        }
        return pictureVO;
    }

    /**
     * 分页获取图片信息封装类
     *
     * @param picturePage 分页对象
     * @param request     请求对象
     * @return 分页图片信息封装类
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 图片审核
     *
     * @param requestParam 图片审核请求参数
     * @param loginUser    登录用户
     */
    @Override
    public void doPictureReview(PictureReviewRequest requestParam, User loginUser) {
        // 1. 校验参数
        Long id = requestParam.getId();
        Integer reviewStatus = requestParam.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 判断是否存在
        Picture oldPicture = this.getById(id);
        if (ObjectUtil.isNull(oldPicture)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        // 判断是否是改状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "请勿重复审核");
        }

        // 3. 更新图片审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(requestParam, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(LocalDateTime.now());
        boolean result = this.saveOrUpdate(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片审核失败");
    }


    /**
     * 填充审核参数
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(LocalDateTime.now());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }


    /**
     * 通用的抓取图片逻辑
     *
     * @param fetcher    图片抓取器
     * @param searchText 搜索关键词
     * @param count      抓取数量
     * @param loginUser  登录用户
     * @param namePrefix 图片名称前缀
     * @param source     数据源名称
     * @return 成功抓取的图片数量
     */
    public Integer fetchImages(ImageFetcher fetcher, String searchText, Integer count, User loginUser,
                               String namePrefix, String source) {
        int uploadCount = 0;
        int page = 1;
        int pageSize = 10;

        // 获取已抓取的 URL 列表
        Set<String> fetchedUrls = fetchedImagesService.getFetchedUrlsBySource(source);

        while (uploadCount < count) {
            // 获取当前页图片 URL 列表
            List<String> imageUrls = fetcher.fetchImageUrls(searchText, page, pageSize);

            if (imageUrls.isEmpty()) {
                log.warn("未找到更多图片，停止抓取");
                break;
            }

            for (String fileUrl : imageUrls) {
                if (fetchedUrls.contains(fileUrl)) {
                    log.info("图片已抓取过，跳过: {}", fileUrl);
                    continue;
                }

                // 检查文件大小
                try {
                    long fileSize = getFileSize(fileUrl);
                    if (fileSize > 4 * 1024 * 1024) {
                        log.warn("图片文件大小超过4MB，跳过: {}", fileUrl);
                        continue;
                    }
                } catch (IOException e) {
                    log.warn("无法获取文件大小，跳过: {}", fileUrl, e);
                    continue;
                }

                // 上传图片
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));

                try {
                    PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                    log.info("图片上传成功, id = {}", pictureVO.getId());
                    uploadCount++;
                    fetchedImagesService.saveFetchedUrl(fileUrl, source);
                } catch (Exception e) {
                    log.error("图片上传失败", e);
                }

                if (uploadCount >= count) {
                    break;
                }
            }

            page++;
        }

        return uploadCount;
    }


    /**
     * 根据不同源抓取图片
     *
     * @param requestParam 批量抓取请求参数
     * @param loginUser    登录用户
     * @return 上传数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest requestParam, User loginUser) {
        String searchText = requestParam.getSearchText();
        Integer count = requestParam.getCount();
        String source = requestParam.getSource();
        String namePrefix = requestParam.getNamePrefix();


        ImageFetcher fetcher;
        Integer uploadCount = 0;

        if ("haowallpaper".equals(source)) {
            uploadCount = fetchFromHaoWallpaper(searchText, count, loginUser, namePrefix, source);
        }

        switch (source.toLowerCase()) {
            case "google":
                fetcher = new GoogleImageFetcher();
                break;
            case "baidu":
                fetcher = new BaiduImageFetcher();
                break;
            case "bing":
                fetcher = new BingImageFetcher();
                break;
            case "pexels":
                fetcher = new PexelsImageFetcher();
                break;
            case "wallhaven":
                fetcher = new WallhavenImageFetcher();
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的抓取源：" + source);
        }

        return fetchImages(fetcher, searchText, count, loginUser, namePrefix, source);
    }


    /**
     * 抓取 HaoWallpaper 图片
     *
     * @param searchText 关键字
     * @param count      数量
     * @param loginUser  登录用户
     * @param namePrefix 名称前缀
     * @return 上传数量
     */
    public Integer fetchFromHaoWallpaper(String searchText, Integer count, User loginUser, String namePrefix, String source) {
        int uploadCount = 0;
        String baseUrl = "https://haowallpaper.com/homeView";

        try {
            // 从数据库中获取已抓取的 URL 列表
            Set<String> fetchedUrls = fetchedImagesService.getFetchedUrlsBySource(source);

            // 分页抓取
            int page = 1;
            while (uploadCount < count) {
                // 构建分页 URL
                String fetchUrl = String.format("%s?page=%d", baseUrl, page);
                log.info("正在抓取 URL: {}", fetchUrl);

                // 获取 HTML 页面
                Document document = Jsoup.connect(fetchUrl).get();

                // 解析图片的元素
                Elements imgElements = document.select("img");
                if (imgElements.isEmpty()) {
                    log.warn("未找到更多图片，停止抓取");
                    break;
                }

                for (Element imgElement : imgElements) {
                    // 提取图片 URL
                    String fileUrl = imgElement.attr("src");
                    // 获取 `alt` 属性值
                    String altText = imgElement.attr("alt");

                    // 检查 `alt` 属性是否包含关键词
                    if (altText == null || !altText.contains(searchText)) {
                        // 跳过不符合条件的图片
                        continue;
                    }

                    if (!fileUrl.startsWith("http")) {
                        // 补全相对路径
                        fileUrl = "https://haowallpaper.com" + fileUrl;
                    }

                    // 检查是否已抓取过
                    if (fetchedUrls.contains(fileUrl)) {
                        log.info("图片已抓取过，跳过: {}", fileUrl);
                        continue; // 跳过已抓取的图片
                    }

                    if (StrUtil.isBlank(namePrefix)) {
                        namePrefix = searchText;
                    }

                    // **检查文件大小**
                    long fileSize = getFileSize(fileUrl);
                    if (fileSize > 4 * 1024 * 1024) {
                        log.warn("图片文件大小超过4MB，跳过: {}", fileUrl);
                        continue;
                    }

                    // 上传图片
                    PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                    pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));

                    try {
                        PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                        log.info("图片上传成功, id = {}", pictureVO.getId());
                        uploadCount++;

                        // 保存已抓取的 URL 到数据库
                        fetchedImagesService.saveFetchedUrl(fileUrl, source);
                    } catch (Exception e) {
                        log.error("图片上传失败", e);
                        continue;
                    }

                    if (uploadCount >= count) {
                        break;
                    }
                }

                page++; // 下一页
            }
        } catch (Exception e) {
            log.error("批量抓取 HaoWallpaper 图片失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量抓取 HaoWallpaper 图片失败");
        }

        return uploadCount;
    }


    /**
     * 获取图片文件大小
     *
     * @param fileUrl 图片 URL
     * @return 文件大小（字节）
     * @throws IOException IO 异常
     */
    private long getFileSize(String fileUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(fileUrl)
                .head() // 只获取 HTTP 头
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("无法获取文件大小: " + fileUrl);
        }

        String contentLength = response.header("Content-Length");
        return contentLength != null ? Long.parseLong(contentLength) : 0;
    }


    /**
     * 获取图片浏览次数
     *
     * @param pictureId 图片ID
     * @return 当前图片的浏览次数
     */
    @Override
    public Long getViewCount(Long pictureId) {
        String key = "picture:view_count:" + pictureId;
        // 从 Redis 获取浏览次数
        String viewCountStr = redisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(viewCountStr)) {
            return Long.parseLong(viewCountStr);
        }

        // 如果 Redis 中没有，则从数据库获取并同步到 Redis
        Picture picture = this.getById(pictureId);
        if (ObjectUtil.isNotNull(picture)) {
            Long viewCount = picture.getViewCount() != null ? picture.getViewCount() : 0L;

            // 将数据库的值同步到 Redis，确保不会丢失数据
            redisTemplate.opsForValue().set(key, String.valueOf(viewCount));
            return viewCount;
        }

        // 若数据库中也不存在对应图片，返回 0
        return 0L;
    }


    /**
     * 增加图片浏览次数
     *
     * @param pictureId 图片ID
     */
    @Override
    public void incrementViewCount(Long pictureId) {
        Picture picture = this.getById(pictureId);
        if (ObjectUtil.isNotNull(picture)) {
            picture.setViewCount(picture.getViewCount() + 1);
            this.updateById(picture);
        }
    }


    /**
     * 增加图片浏览次数，缓存版本
     *
     * @param pictureId 图片ID
     */
    @Override
    public void incrementViewCountInCache(Long pictureId) {
        String key = "picture:view_count:" + pictureId;
        // 判断Redis中是否存在该键
        Boolean hasKey = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(hasKey)) {
            // 如果存在，直接增加浏览量
            redisTemplate.opsForValue().increment(key, 1);

            // 同时更新数据库
            Picture picture = this.getById(pictureId);
            if (ObjectUtil.isNotNull(picture)) {
                picture.setViewCount(picture.getViewCount() + 1);
                this.updateById(picture);
            }
        } else {
            // 如果不存在，从数据库加载浏览量并写入Redis
            Picture picture = this.getById(pictureId);
            Long currentViewCount = picture != null && picture.getViewCount() != null ? picture.getViewCount() : 0L;
            // 将数据库中的浏览量同步到Redis
            redisTemplate.opsForValue().set(key, String.valueOf(currentViewCount + 1));
        }
    }


    /**
     * 分页查询图片信息，带缓存
     *
     * @param requestParam 分页查询请求参数
     * @param request      请求对象
     * @return 分页图片信息封装类
     */
    @Override
    public Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest requestParam, HttpServletRequest request) {
        // 获取分页数据
        long current = requestParam.getPageNum();
        long size = requestParam.getPageSize();
        // 限制爬虫
        // ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        requestParam.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key
        String queryCondition = JSONUtil.toJsonStr(requestParam);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = "lgpicture:listPictureVOByPage:" + hashKey;

        // 1. 查询本地缓存（Caffeine）
        String cachedValue = LOCAL_CACHE.getIfPresent(redisKey);
        if (ObjectUtil.isNotNull(cachedValue)) {
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return cachedPage;
        }

        // 2. 查询分布式缓存（Redis）
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(redisKey);
        if (ObjectUtil.isNotNull(cachedValue)) {
            // 如果命中 Redis，存入本地缓存并返回
            LOCAL_CACHE.put(redisKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return cachedPage;
        }

        // 3.缓存未命中，查询数据库
        Page<Picture> picturePage = this.page(new Page<>(current, size),
                this.getLambdaQueryWrapper(requestParam));
        // 获得封装类
        Page<PictureVO> pictureVOPage = this.getPictureVOPage(picturePage, request);
        // 4. 更新缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 更新本地缓存
        LOCAL_CACHE.put(redisKey, cacheValue);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        return pictureVOPage;
    }
}
