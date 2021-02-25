package com.javam4.modules.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author : niceyoo
 * @version : 1.0
 * @title : OrderUtil
 * @description :
 * @copyright : 公共服务与应急管理战略本部 Copyright(c)2020
 * @date : 2021/2/8 13:48
 */
public class OrderUtil {

    /**
     *  根据时间戳生成订单号
     * */
    public static String getOrderNo () {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime localDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        return df.format(localDateTime);
    }

}


