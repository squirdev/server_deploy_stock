package com.nq.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.dao.StockIndexMapper;
import com.nq.pojo.StockIndex;
import com.nq.pojo.User;
import com.nq.service.IStockIndexService;
import com.nq.service.IStockOptionService;
import com.nq.service.IUserService;
import com.nq.utils.BigDecimalUtil;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.biying.ByStockApi;
import com.nq.utils.stock.wangji.WjStockApi;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
import com.nq.vo.stock.MarketVO;
import com.nq.vo.stock.StockDTO;
import com.nq.vo.stockindex.StockIndexVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iStockIndexService")
public class StockIndexServiceImpl implements IStockIndexService {
    private static final Logger log = LoggerFactory.getLogger(StockIndexServiceImpl.class);


    @Autowired
    StockIndexMapper stockIndexMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    IStockOptionService iStockOptionService;

    @Autowired
    private ByStockApi stockApi;

    @Autowired
    private WjStockApi wjStockApi;


    public ServerResponse listByAdmin(Integer homeShow, Integer listShow, Integer transState, String indexCode, String indexName, int pageNum, int pageSize, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);

        List<StockIndex> stockIndexList = this.stockIndexMapper.listByAdmin(homeShow, listShow, transState, indexCode, indexName);
        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();
        for (StockIndex stockIndex : stockIndexList) {
            StockIndexVO stockIndexVO = assembleStockIndexVO(stockIndex);
            stockIndexVOS.add(stockIndexVO);
        }
        PageInfo pageInfo = new PageInfo(stockIndexList);
        pageInfo.setList(stockIndexVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private StockIndexVO assembleStockIndexVO(StockIndex stockIndex, WjStockExtInfoDTO extInfoDTO) {
        StockIndexVO stockIndexVO = new StockIndexVO();

        stockIndexVO.setId(stockIndex.getId());
        stockIndexVO.setIndexName(stockIndex.getIndexName());
        stockIndexVO.setIndexCode(stockIndex.getIndexCode());
        stockIndexVO.setIndexGid(stockIndex.getIndexGid());
        stockIndexVO.setHomeShow(stockIndex.getHomeShow());
        stockIndexVO.setListShow(stockIndex.getListShow());
        stockIndexVO.setTransState(stockIndex.getTransState());
        stockIndexVO.setDepositAmt(stockIndex.getDepositAmt());
        stockIndexVO.setTransFee(stockIndex.getTransFee());
        stockIndexVO.setEachPoint(stockIndex.getEachPoint());
        stockIndexVO.setMinNum(stockIndex.getMinNum());
        stockIndexVO.setMaxNum(stockIndex.getMaxNum());
        stockIndexVO.setAddTime(stockIndex.getAddTime());
        stockIndexVO.setTDesc(stockIndex.getTDesc());


        if (extInfoDTO != null) {
            stockIndexVO.setCurrentPoint(BigDecimalUtil.formatWithNoTQ(extInfoDTO.getPrice()));
            stockIndexVO.setFloatPoint(BigDecimalUtil.formatWithNoTQ(extInfoDTO.getChange()));
            stockIndexVO.setFloatRate(BigDecimalUtil.formatWithNoTQ(extInfoDTO.getChangeRate()));
        }

        return stockIndexVO;
    }

    private StockIndexVO assembleStockIndexVO(StockIndex stockIndex) {
        StockIndexVO stockIndexVO = new StockIndexVO();

        stockIndexVO.setId(stockIndex.getId());
        stockIndexVO.setIndexName(stockIndex.getIndexName());
        stockIndexVO.setIndexCode(stockIndex.getIndexCode());
        stockIndexVO.setIndexGid(stockIndex.getIndexGid());
        stockIndexVO.setHomeShow(stockIndex.getHomeShow());
        stockIndexVO.setListShow(stockIndex.getListShow());
        stockIndexVO.setTransState(stockIndex.getTransState());
        stockIndexVO.setDepositAmt(stockIndex.getDepositAmt());
        stockIndexVO.setTransFee(stockIndex.getTransFee());
        stockIndexVO.setEachPoint(stockIndex.getEachPoint());
        stockIndexVO.setMinNum(stockIndex.getMinNum());
        stockIndexVO.setMaxNum(stockIndex.getMaxNum());
        stockIndexVO.setAddTime(stockIndex.getAddTime());
        stockIndexVO.setTDesc(stockIndex.getTDesc());

        StockDTO index = stockApi.getStockIndex(stockIndexVO.getIndexGid());

        if (index != null) {
            stockIndexVO.setCurrentPoint(index.getP().toString());
            stockIndexVO.setFloatPoint(index.getUd().toString());
            stockIndexVO.setFloatRate(index.getPc().toString());
        }

        return stockIndexVO;
    }


    public ServerResponse updateIndex(StockIndex stockIndex) {
        if (stockIndex.getId() == null) {
            return ServerResponse.createByErrorMsg("修改id不能为空");
        }

        StockIndex dbindex = this.stockIndexMapper.selectByPrimaryKey(stockIndex.getId());
        dbindex.setHomeShow(stockIndex.getHomeShow());
        dbindex.setListShow(stockIndex.getListShow());
        dbindex.setTransState(stockIndex.getTransState());
        dbindex.setDepositAmt(stockIndex.getDepositAmt());
        dbindex.setTransFee(stockIndex.getTransFee());
        dbindex.setEachPoint(stockIndex.getEachPoint());
        dbindex.setMinNum(stockIndex.getMinNum());
        dbindex.setMaxNum(stockIndex.getMaxNum());

        int updateCount = this.stockIndexMapper.updateByPrimaryKey(dbindex);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失败");
    }


    public ServerResponse addIndex(StockIndex stockIndex) {
        log.info("name = {} code = {} gid = {}", new Object[]{stockIndex
                .getIndexName(), stockIndex.getIndexCode(), stockIndex.getIndexGid()});
        if (StringUtils.isBlank(stockIndex.getIndexName()) ||
                StringUtils.isBlank(stockIndex.getIndexCode()) ||
                StringUtils.isBlank(stockIndex.getIndexGid())) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        StockIndex nameIndex = this.stockIndexMapper.selectIndexByName(stockIndex.getIndexName());
        if (nameIndex != null) {
            return ServerResponse.createByErrorMsg("指数名称已存在");
        }
        StockIndex codeIndex = this.stockIndexMapper.selectIndexByCode(stockIndex.getIndexCode());
        if (codeIndex != null) {
            return ServerResponse.createByErrorMsg("指数代码已存在");
        }

        stockIndex.setAddTime(new Date());
        int insertCount = this.stockIndexMapper.insert(stockIndex);

        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("添加成功");
        }
        return ServerResponse.createByErrorMsg("添加失败");
    }


    public ServerResponse queryHomeIndex() {
//        List<StockIndex> list = this.stockIndexMapper.queryHomeIndex();
//        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();
//        for (StockIndex stockIndex : list) {
//            StockIndexVO stockIndexVO = assembleStockIndexVO(stockIndex);
//            int random = (int) (Math.random() * 3 + 1);
//            stockIndexVO.setRandom(random);
//            stockIndexVOS.add(stockIndexVO);
//        }
        String result = null;
        com.alibaba.fastjson2.JSONObject json = null;
        String url = PropertiesUtil.getProperty("home.index.recommend.url");
        try {
                result = HttpClientRequest.doGet(url);
                json = com.alibaba.fastjson2.JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("首页推荐出错", e);
        }
        return ServerResponse.createBySuccess(json);
    }


    public ServerResponse queryListIndex(HttpServletRequest request) {
        List<StockIndex> list = this.stockIndexMapper.queryListIndex();
        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();
        User user = iUserService.getCurrentUser(request);
        for (StockIndex stockIndex : list) {
            StockIndexVO stockIndexVO = assembleStockIndexVO(stockIndex);
            //是否添加自選
            if (user == null) {
                stockIndexVO.setIsOption("0");
            } else {
                stockIndexVO.setIsOption(iStockOptionService.isMyOption(user.getId(), stockIndex.getIndexCode()));
            }
            stockIndexVOS.add(stockIndexVO);
        }
        return ServerResponse.createBySuccess(stockIndexVOS);
    }


    public ServerResponse queryTransIndex(Integer indexId) {
        StockIndex stockIndex = this.stockIndexMapper.selectByPrimaryKey(indexId);
        if (1 == stockIndex.getTransState().intValue()) {
            return ServerResponse.createBySuccessMsg("可交易");
        }
        return ServerResponse.createByErrorMsg("不可交易");
    }


    public MarketVO querySingleIndex(String indexCode) {
        MarketVO marketVO = null;

            String market_url = PropertiesUtil.getProperty("sina.single.market.url");

            String result = null;
            try {
                market_url = market_url + indexCode;
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //System.out.print("指数请求开始，时间："+sdf.format(new Date())+"，market_url："+market_url + "\n");

                result = HttpClientRequest.doGet(market_url);
                //System.out.print("指数请求结束，时间："+sdf.format(new Date())+"，result："+result + "\n");
            } catch (Exception e) {
                log.error("获取 大盘指数 出错 e = {}", e);
            }


            try {
                if (StringUtils.isNotBlank(result)) {
                    result = result.substring(result.indexOf("\"") + 1, result.lastIndexOf("\""));

                    marketVO = new MarketVO();
                    if (result.contains(",")) {
                        String[] sh01_arr = result.split(",");
                        marketVO.setName(sh01_arr[0]);
                        marketVO.setNowPrice(sh01_arr[1]);
                        marketVO.setIncrease(sh01_arr[2]);
                        marketVO.setIncreaseRate(sh01_arr[3]);
                    }
                }
            } catch (Exception e) {
                log.error("转换大盘指数出错 str = {} ,  e = {}", result, e);
            }

        return marketVO;
    }


    public StockIndex selectIndexById(Integer indexId) {
        return this.stockIndexMapper.selectByPrimaryKey(indexId);
    }
    //指数新闻
    @Override
    public ServerResponse queryIndexNews() {
        String url = PropertiesUtil.getProperty("hk.index.introduction.url");
        String result = HttpClientRequest.doGet(url);
        com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(result);
        return ServerResponse.createBySuccess(json);
    }

    //美股港股指数定时任务
    @Override
    public void otherIndexTask() {
        String[] keys = {"hk", "us"};
        //当前毫秒时间戳
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String url = null;
            if ("hk".equals(key)) {
                url = PropertiesUtil.getProperty("hk.index.url") + now;
            }else {
                url = PropertiesUtil.getProperty("us.index.url") + now;
            }

            String result = HttpClientRequest.doGet(url);
            JSONObject json = JSONObject.parseObject(result);
            //取出data里的diff数组
            ArrayList diff = (ArrayList) json.getJSONObject("data").get("diff");

            for (int i = 0; i < diff.size(); i++) {
                JSONObject data = (JSONObject) diff.get(i);
                String stockCode = data.getString("f12");
                StockIndex stock = stockIndexMapper.selectIndexByCode(stockCode);
            if (stock == null) {
                stock = new StockIndex();
//                String spell = GetPyByChinese.converterToFirstSpell(data.getString("f14"));
                stock.setIndexCode(data.getString("f12"));
                stock.setIndexName(data.getString("f14"));
                stock.setListShow(1);
                stock.setIndexGid(key+data.getString("f12"));
                stock.setHomeShow(1);
                stock.setTransState(1);
                stock.setAddTime(new Date());
                stock.setDepositAmt(10000);
                stock.setTransFee(200);
                stock.setMaxNum(100);
                stock.setMinNum(1);
                stock.setEachPoint(300);
                stockIndexMapper.insert(stock);
                sb.append("新增股票：").append(stock.getIndexName()).append(stock.getIndexCode()).append("/n");
            }

            }
            log.info(key + "股新增股票：{}", sb.toString());
        }
    }


    @Override
    public StockIndex selectIndexByCode(String indexCode) {
        return this.stockIndexMapper.selectIndexByCode(indexCode);
    }

    @Override
    public ServerResponse queryCustIndex() {
        List<StockIndex> list = this.stockIndexMapper.queryListIndex2();

        List<String> ids = list.stream().map(StockIndex::getIndexGid).collect(Collectors.toList());

        Map<String, WjStockExtInfoDTO> stockMap = wjStockApi.getStockBatch(ids);


        List<StockIndexVO> stockIndexVOS = Lists.newArrayList();
        for (StockIndex stockIndex : list) {
            StockIndexVO stockIndexVO = assembleStockIndexVO(stockIndex, stockMap.get(stockIndex.getIndexGid()));
            stockIndexVOS.add(stockIndexVO);
        }
        return ServerResponse.createBySuccess(stockIndexVOS);
    }
}
