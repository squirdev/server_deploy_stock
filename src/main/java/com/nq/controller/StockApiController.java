package com.nq.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.nq.common.ServerResponse;
import com.nq.pojo.Stock;
import com.nq.pojo.StockIndex;
import com.nq.service.ConvertBondService;
import com.nq.service.IStockIndexService;
import com.nq.service.IStockService;
import com.nq.service.StockDzService;
import com.nq.utils.CacheUtil;
import com.nq.utils.stock.biying.ByStockApi;
import com.nq.utils.stock.wangji.WjStockApi;
import com.nq.utils.stock.wangji.vo.HsQueryParam;
import com.nq.utils.stock.wangji.vo.KLineParam;
import com.nq.utils.stock.wangji.vo.QueryParam;
import com.nq.utils.stock.wangji.vo.WjPageRet;
import com.nq.utils.stock.wangji.vo.WjStockDetailDTO;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
import com.nq.utils.stock.wangji.vo.WjStockInfoDTO;
import com.nq.vo.stock.ChartCellVO;
import com.nq.vo.stock.StockDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/api/stock/"})
public class StockApiController {
    private static final Logger log = LoggerFactory.getLogger(StockApiController.class);

    @Autowired
    IStockService iStockService;
    @Autowired
    StockDzService stockDzService;
    @Autowired
    private ConvertBondService converBondService;

    @Autowired
    private ByStockApi byStockApi;

    @Autowired
    private IStockIndexService stockIndexService;

    @Autowired
    private WjStockApi wjStockApi;




    //查询 股票指数、大盘指数信息
    @RequestMapping({"getMarket.do"})
    @ResponseBody
    public ServerResponse getMarket() {
        return this.iStockService.getMarket();
    }

    //查询官网PC端交易 所有股票信息及指定股票信息
    @RequestMapping({"getStock.do"})
    @ResponseBody
    public ServerResponse getStock(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "stockPlate", required = false) String stockPlate, @RequestParam(value = "stockType", required = false) String stockType, @RequestParam(value = "keyWords", required = false) String keyWords, HttpServletRequest request) {
        return this.iStockService.getStock(pageNum, pageSize, keyWords, stockPlate, stockType, request);
    }

    //通过股票代码查询股票信息
    @RequestMapping({"getSingleStock.do"})
    @ResponseBody
    public ServerResponse getSingleStock(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iStockService.getSingleStock(code, request);
    }

    @RequestMapping({"getSingleStockNew.do"})
    @ResponseBody
    public ServerResponse getSingleStockNew(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iStockService.getSingleStockNew(code, request);
    }

    @RequestMapping({"getSingleStockNew2.do"})
    @ResponseBody
    public ServerResponse getSingleStockNew2(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iStockService.getSingleStockNew2(code, request);
    }

    @RequestMapping({"getMinK.do"})
    @ResponseBody
    public ServerResponse getMinK(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("ma") Integer ma, @RequestParam("size") Integer size) {
        return this.iStockService.getMinK(code, time, ma, size);
    }

    /*查询股票日线*/
    @RequestMapping({"getDayK.do"})
    @ResponseBody
    public ServerResponse getDayK(@RequestParam("code") String code) {
        return this.iStockService.getDayK_Echarts(code);
    }

    //查询股票历史数据数据
    @RequestMapping({"getMinK_Echarts.do"})
    @ResponseBody
    public ServerResponse getMinK_Echarts(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("ma") Integer ma, @RequestParam("size") Integer size) {
        return this.iStockService.getMinK_Echarts(code, time, ma, size);
    }

    /*期货分时-k线*/
    @RequestMapping({"getFuturesMinK_Echarts.do"})
    @ResponseBody
    public ServerResponse getFuturesMinK_Echarts(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("size") Integer size) {
        return this.iStockService.getFuturesMinK_Echarts(code, time, size);
    }

    /*指数分时-k线*/
    @RequestMapping({"getIndexMinK_Echarts.do"})
    @ResponseBody
    public ServerResponse getIndexMinK_Echarts(@RequestParam("code") String code, @RequestParam("time") Integer time, @RequestParam("size") Integer size) {
        return this.iStockService.getIndexMinK_Echarts(code, time, size);
    }

    /*查询期货日线*/
    @RequestMapping({"getFuturesDayK.do"})
    @ResponseBody
    public ServerResponse getFuturesDayK(@RequestParam("code") String code) {
        return this.iStockService.getFuturesDayK(code);
    }

    /*指数日线*/
    @RequestMapping({"getIndexDayK.do"})
    @ResponseBody
    public ServerResponse getIndexDayK(@RequestParam("code") String code) {
        return this.iStockService.getIndexDayK(code);
    }
//    //查询股票需要换数据源的股票
//    @RequestMapping({"stockDataBase.do"})
//    @ResponseBody
//    public ServerResponse stockDataBase() {
//        return this.iStockService.stockDataBase();
//    }

    /**
     * 龙虎榜
     */
    @RequestMapping({"getlhb.do"})
    @ResponseBody
    public ServerResponse getlhb() {
        return this.iStockService.lhb();
    }

    /**
     * 十大成交股
     */
    @RequestMapping({"getTop.do"})
    @ResponseBody
    public ServerResponse getTop(@RequestParam(value = "content")Integer content) {
        return this.iStockService.top(content);
    }

    /**
     * 每日停牌
     */
    @RequestMapping({"getStop.do"})
    @ResponseBody
    public ServerResponse getStop() {
        return this.iStockService.stop();
    }

    /**
     * 获取涨跌数据
     * @return
     */
    @PostMapping({"getZdfNumber.do"})
    @ResponseBody
    public ServerResponse getZdfNumber() {

        ServerResponse zdfb = this.iStockService.getZdfb();
        ArrayList<ChartCellVO> data = (ArrayList<ChartCellVO>) zdfb.getData();
        if(CollectionUtils.isEmpty(data)){
            return ServerResponse.createByError();
        }
        ChartCellVO chartCellVO = data.get(0);
        ChartCellVO chartCellVO1 = data.get(3);
        ChartCellVO chartCellVO3 = data.get(2);
        ChartCellVO chartCellVO4 = data.get(5);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("zhang",Integer.parseInt(chartCellVO.getyAxis())+ Integer.parseInt(chartCellVO1.getyAxis()));
        resultMap.put("die",Integer.parseInt(chartCellVO3.getyAxis())+ Integer.parseInt(chartCellVO4.getyAxis()));
        return ServerResponse.createBySuccess(resultMap);
    }

    /**
     * 获取股票根据 涨跌幅 成交额 价格排序
     * @return
     * sort  ("根据什么排序   changepercent 涨跌幅  pricechange 涨跌额  volume 成交量 amount 成交额")
     * asc ("是否升序 0否 1是")
     *  node ("排序的主板类型 hs_bjs 北交所  cyb 创业板  kcb 科创板   hs_a 沪深a股")
     */
    @PostMapping({"getStockSort.do"})
    @ResponseBody
    public ServerResponse getStockMarketZDFB(Integer pageNo,Integer pageSize, String sort,
                                            Integer asc,
                                            String node){
//        String key=pageNo+pageSize+sort+asc+node;
//        String result = (String) CacheUtil.get(key);
//        if(StringUtils.isEmpty(result)){
            ServerResponse stockSort = this.iStockService.getStockSort(pageNo, pageSize, sort, asc, node);
//            CacheUtil.set(key, JSON.toJSONString(stockSort.getData()),5000);
//            return stockSort;
//        }
//        log.info("命中缓存：{}",key);
        return stockSort;
    }
    /**
     * 涨停板
     */

    @PostMapping({"getztb.do"})
    @ResponseBody
    public ServerResponse getztb() {
        return this.iStockService.ztb();
    }

    /**
     *大宗交易 列表
     */
    @PostMapping({"getDzList.do"})
    @ResponseBody
    public ServerResponse getDzList() {
        return this.stockDzService.getDzList();
    }

    /**
     * vip 抢筹列表
     */
    @PostMapping({"getVipList.do"})
    @ResponseBody
    public ServerResponse getVipList() {
        return this.iStockService.ztb();
    }

    /**
     * vip 抢筹根据股票代码查询
     * @param code
     * @return
     */

    @PostMapping({"getVipByCode.do"})
    @ResponseBody
    public ServerResponse getVipByCode(String code) {
        return this.iStockService.getVipByCode(code);
    }



//    /**
//     * 获取可转债数据
//     * @return
//     */
//    @RequestMapping(value = "/bondList", method = RequestMethod.GET)
//    @ResponseBody
//    public ServerResponse bondList(@RequestParam(value = "page", required = false,defaultValue = "1") Integer page,
//                                   @RequestParam(value = "size", required = false,defaultValue = "10") Integer size ) {
//        return this.converBondService.listByPage(page,size);
//    }


    @PostMapping("getKLine.do")
    @ResponseBody
    public ServerResponse getKLine(@RequestBody JSONObject jo) {

        StockIndex stockIndex = stockIndexService.selectIndexByCode(jo.getString("code"));

        KLineParam kLineParam = new KLineParam();
        kLineParam.setSymbol(stockIndex.getIndexGid());
        List<WjStockDetailDTO> list = wjStockApi.getKLine(kLineParam);

//        List<StockDTO> list = byStockApi.getKLine(stockIndex.getIndexGid(), ByStockApi.convertTime(jo.getIntValue("time")));

        return ServerResponse.createBySuccess(list);

    }


    @PostMapping("getNStock.do")
    @ResponseBody
    public ServerResponse getStock(@RequestBody JSONObject jo) {


        String code = jo.getString("code");

        StockDTO stock = byStockApi.getStock(code);

        return ServerResponse.createBySuccess(stock);

    }

    @PostMapping("getQsgc.do")
    @ResponseBody
    public ServerResponse getQsgc() {


         List<StockDTO> list = byStockApi.getQsgc();

         List<Stock> hideList = iStockService.getHideStockCode();

        List<String> codeList = hideList.stream().map(Stock::getStockCode).collect(Collectors.toList());

        list = list.stream().filter(dto -> {

            if (dto.getZf().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            String dm = dto.getDm();
            String substring = dm.substring(2);
            return !codeList.contains(substring);

        }).collect(Collectors.toList());

        return ServerResponse.createBySuccess(list);

    }


    @PostMapping("getHsph.do")
    @ResponseBody
    public ServerResponse getHsph(@RequestBody HsQueryParam param) {


        List<WjStockExtInfoDTO> list = wjStockApi.getHsPageList(param);

        List<Stock> hideList = iStockService.getHideStockCode();

        List<String> codeList = hideList.stream().map(Stock::getStockGid).collect(Collectors.toList());

        list = list.stream().filter(dto -> {

            return !codeList.contains(dto.getSymbol());

        }).collect(Collectors.toList());

        return ServerResponse.createBySuccess(list);

    }


    @PostMapping("getKLineList.do")
    @ResponseBody
    public ServerResponse getKLineList(@RequestBody JSONObject jo) {
        String code = jo.getString("code");
        String type = jo.getString("type");
        List<StockDTO> list = byStockApi.getKLine(code, type);
        return ServerResponse.createBySuccess(list);
    }

    @PostMapping("getRealTimeData.do")
    @ResponseBody
    public ServerResponse getRealTimeData(@RequestBody List<String> codes) {

        HashMap<String, StockDTO> ret = Maps.newHashMap();

        if (org.apache.commons.collections4.CollectionUtils.isEmpty(codes)) {
            return ServerResponse.createBySuccess(ret);
        }

        Map<String, WjStockExtInfoDTO> stockBatch = wjStockApi.getStockBatch(codes);

        return ServerResponse.createBySuccess(stockBatch);

    }


    @GetMapping("getPageList.do")
    @ResponseBody
    public ServerResponse getPageList() {

        QueryParam queryParam = new QueryParam();
        queryParam.setLimit(10);
        queryParam.setSortField("px_change_rate");
        queryParam.setType("1");
        queryParam.setOrderBy("desc");


        queryParam.setPage(1);

        WjPageRet pageList = wjStockApi.getPageList(queryParam);

        return ServerResponse.createBySuccess(pageList);

    }


    @GetMapping("getStockWj.do")
    @ResponseBody
    public ServerResponse getStockInfo(@RequestParam("code") String code, @RequestParam("type") String type) {

        WjStockInfoDTO stock = wjStockApi.getStock(code, type);
        return ServerResponse.createBySuccess(stock);

    }

    @GetMapping("wj/real.do")
    @ResponseBody
    public ServerResponse getRealData(@RequestParam("code") String code) {

        WjStockExtInfoDTO realData = wjStockApi.getRealData(code);
        return ServerResponse.createBySuccess(realData);

    }

    @GetMapping("updateStockData.do")
    @ResponseBody
    public ServerResponse updateStockData() {

        this.iStockService.updateStockData();
        return ServerResponse.createBySuccess();

    }

}