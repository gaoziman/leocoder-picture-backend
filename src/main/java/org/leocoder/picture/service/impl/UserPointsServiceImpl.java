package org.leocoder.picture.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.leocoder.picture.mapper.UserPointsMapper;
import org.leocoder.picture.domain.UserPoints;
import org.leocoder.picture.service.UserPointsService;
/**
 * @author : 程序员Leo
 * @date  2024-12-18 21:21
 * @version 1.0
 * @description :
 */

@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService{

}
