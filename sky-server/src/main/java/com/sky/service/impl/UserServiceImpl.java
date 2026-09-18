package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    //微信配置属性
    @Autowired
    WeChatProperties weChatProperties;
    @Autowired
    UserMapper userMapper;



    //微信登录
    public User wxlogin(UserLoginDTO userLoginDTO) {
        //调用微信接口服务,获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        //如果用户不存在，说明是新用户
        if (user == null) {
            //注册
            user = new User().builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            //将用户保存到数据库
            userMapper.insert(user);
        }

        //返回这个用户对象
        return user;
    }

    //调用微信接口服务,获取openid
    private String getOpenid(String code) {
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        //向微信服务接口发送GET请求,得到json字符串
        String json = HttpClientUtil.doGet(WX_LOGIN_URL,map);
        log.info("微信接口返回: {}", json);
        //解析json字符串
        JSONObject jsonObject = JSONObject.parseObject(json);
        //检查微信返回的错误码
        Integer errcode = jsonObject.getInteger("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = jsonObject.getString("errmsg");
            log.error("微信登录失败，errcode: {}, errmsg: {}", errcode, errmsg);
            return null;
        }
        //解析json字符串,得到openid
        String openid = jsonObject.getString("openid");
        if (openid == null) {
            log.error("微信登录失败，未获取到openid，返回: {}", json);
        }
        return openid;
    }
}