package com.nq.utils.task.stock;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nq.dao.StockMapper;
import com.nq.pojo.Stock;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.stock.pinyin.GetPyByChinese;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.ws.Action;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Date;

/**
 * @ClassName : StockListTask  //类名 启动后执行run
 * @Description :   //描述
 * @Author :  tf //作者
 * @Date: 2022/10/27  16:14
 */
@Component
@Slf4j
public class StockListTask implements ApplicationRunner {
    @Autowired
    StockMapper stockMapper;
    @Autowired
    StockOthersTask stockOthersTask;

//    @Override
    public void run3(ApplicationArguments args) throws Exception {
        String[] keys = {"hs", "sz", "us", "hk", "bj"};

//            for (String key : keys) {

           try {
               int num = 0;
//               Thread.sleep(1000L);
            for (int y = 1; y < 50; y++) {
                Thread.sleep(1000L);
                //总数量

                String hs = HttpClientRequest.doGet("http://192.168.10.5/sz_list/?page=" + y);
                log.info("hs" + hs + "第" + y + "页,长度" + hs.length());
                if (hs.length() == 2) {
                    log.info("數據為空");
                    break;
                }
                //Sting 转josnarry
                JSONArray hsArray = JSONObject.parseArray(hs);
                num = num + hsArray.size();
                log.info("当前页数量" + hsArray.size());
                for (int i = 0; i < hsArray.size(); i++) {
                    JSONObject hsjson = JSONObject.parseObject(hsArray.get(i).toString());
                    Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("stock_code", hsjson.getString("stock_code")));
                    if (stock == null) {
                        stock = new Stock();
                        stock.setStockCode(hsjson.getString("stock_code"));
                        stock.setStockName(hsjson.getString("stock_name"));
                        stock.setStockType(hsjson.getString("stock_type"));
                        stock.setStockGid(hsjson.getString("stock_gid"));
                        stock.setStockSpell(hsjson.getString("stock_spell"));
                        stock.setIsLock(0);
                        stock.setIsShow(0);
                        stock.setAddTime(new Date());
                        stockMapper.insert(stock);
                    }else {
                        stock.setStockCode(hsjson.getString("stock_code"));
                        stock.setStockName(hsjson.getString("stock_name"));
                        stock.setStockType(hsjson.getString("stock_type"));
                        stock.setStockGid(hsjson.getString("stock_gid"));
                        stock.setStockSpell(hsjson.getString("stock_spell"));
                        stock.setIsLock(0);
                        stock.setIsShow(0);
                        stock.setAddTime(new Date());
                        stockMapper.updateById(stock);
                    }
                }
            }
               log.info("数据量" + num);
            }catch (Exception e) {
               throw new RuntimeException(e);
        }
        }
        /**
        * @Description:   股票数据
        * @Param:
        * @return:
        * @Author: tf
        * @Date: 2022/11/2
        */
//    @Override
    public void run2(ApplicationArguments args) throws Exception {
        int num = 0;
//        String[] keys = {"cyb", "kcb"};
//        for (String key : keys) {
            for (int j = 1; j < 34; j++) {

//            String bj = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page="+j+"&num=80&sort=symbol&asc=1&node=hs_bjs&symbol=&_s_r_a=page");
                String sz = null;
                try {
                    sz = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHKStockData?page=" + j + "&num=80&sort=symbol&asc=1&node=qbgg_hk&_s_r_a=init");

                } catch (Exception e) {
                    log.error("请求失败" + e);
                    Thread.sleep(30000L);
                    sz = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=" + j + "&num=80&sort=symbol&asc=1&node=qbgg_hk&_s_r_a=init");
                }
                if (sz == null) {
                    log.info("获取完毕，总条数" + num);
                    return;
                }
                JSONArray bjArray = JSONObject.parseArray(sz);
                num = num + bjArray.size();
//            log.info("当前页数量" + bjArray.size());
                for (Object o : bjArray) {
                    JSONObject hsjson = JSONObject.parseObject(o.toString());
//                log.info("第" + i+"条数据:"+hsjson);
                    if (hsjson == null) {
                        continue;
                    }
                    Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("stock_code", hsjson.getString("code")));
                    if (stock == null) {
                        stock = new Stock();
                        String type = hsjson.getString("symbol").substring(0, 2);
                        String spell = GetPyByChinese.converterToFirstSpell(hsjson.getString("name"));
                        stock.setStockCode(hsjson.getString("symbol"));
                        stock.setStockName(hsjson.getString("name"));
                        stock.setStockType("hk");
                        stock.setStockGid("hk"+hsjson.getString("symbol"));
                        stock.setStockSpell(spell);
                        stock.setIsLock(0);
                        stock.setIsShow(0);
                        stock.setAddTime(new Date());
                        stockMapper.insert1(stock);
                    }
//                    else {
//                        if (key.equals("cyb")) {
//                            stock.setStockPlate("创业");
//                            stockMapper.updateById(stock);
//                        }else {
//                            stock.setStockPlate("科创");
//                            stockMapper.updateById(stock);
//                        }
                    }
                }
                log.info("数据量" + num);
            }


    public void run(ApplicationArguments args) throws Exception {

//            stockOthersTask.HkStockTask();
//            stockOthersTask.UsStockTask();
//            stockOthersTask.HkIndexTask();
//
//        log.info("=======================数据加载完毕============================");
    }


        }



