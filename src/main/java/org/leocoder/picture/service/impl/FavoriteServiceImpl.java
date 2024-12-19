package org.leocoder.picture.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.leocoder.picture.mapper.FavoriteMapper;
import org.leocoder.picture.domain.Favorite;
import org.leocoder.picture.service.FavoriteService;
/**
 * @author : 程序员Leo
 * @date  2024-12-18 21:21
 * @version 1.0
 * @description :
 */

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

}
