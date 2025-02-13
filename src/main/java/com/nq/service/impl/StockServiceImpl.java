package com.nq.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nq.common.ServerResponse;
import com.nq.config.StockPoll;
import com.nq.dao.RealTimeMapper;
import com.nq.dao.StockFuturesMapper;
import com.nq.dao.StockIndexMapper;
import com.nq.dao.StockMapper;
import com.nq.pojo.*;
import com.nq.service.IStockMarketsDayService;
import com.nq.service.IStockOptionService;
import com.nq.service.IStockService;
import com.nq.service.IUserService;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.CacheUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.biying.ByStockApi;
import com.nq.utils.stock.pinyin.GetPyByChinese;
import com.nq.utils.stock.qq.QqStockApi;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.utils.stock.wangji.WjStockApi;
import com.nq.utils.stock.wangji.vo.QueryParam;
import com.nq.utils.stock.wangji.vo.WjPageData;
import com.nq.utils.stock.wangji.vo.WjPageRet;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
import com.nq.utils.translate.GoogleTranslateUtil;
import com.nq.vo.stock.*;
import com.nq.vo.stock.k.MinDataVO;
import com.nq.vo.stock.k.echarts.EchartsDataVO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static com.nq.utils.DateTimeUtil.*;

@Service("iStockService")
public class StockServiceImpl implements IStockService {
  private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

  @Autowired
  StockMapper stockMapper;

  @Autowired
  RealTimeMapper realTimeMapper;

  @Autowired
  IStockMarketsDayService iStockMarketsDayService;

  @Autowired
  StockPoll stockPoll;

  @Autowired
  StockFuturesMapper stockFuturesMapper;

  @Autowired
  StockIndexMapper stockIndexMapper;

  @Autowired
  IUserService iUserService;

  @Autowired
  IStockOptionService iStockOptionService;

  @Autowired
  ByStockApi byStockApi;

  @Autowired
  WjStockApi wjStockApi;

  public ServerResponse getMarket() {
    String market_url = PropertiesUtil.getProperty("sina.market.url");
    String result = null;
    try {
      result = HttpClientRequest.doGet(market_url);
    } catch (Exception e) {
      log.error("e = {}", e);
    }
    String[] marketArray = result.split(";");
    List<MarketVO> marketVOS = Lists.newArrayList();
    for (int i = 0; i < marketArray.length; i++) {
      String hqstr = marketArray[i];
      try {
        if (StringUtils.isNotBlank(hqstr)) {
          hqstr = hqstr.substring(hqstr.indexOf("\"") + 1, hqstr.lastIndexOf("\""));
          MarketVO marketVO = new MarketVO();
          String[] sh01_arr = hqstr.split(",");
          marketVO.setName(sh01_arr[0]);
          marketVO.setNowPrice(sh01_arr[1]);
          marketVO.setIncrease(sh01_arr[2]);
          marketVO.setIncreaseRate(sh01_arr[3]);
          marketVOS.add(marketVO);
        }
      } catch (Exception e) {
        log.error("str = {} ,  e = {}", hqstr, e);
      }
    }
    MarketVOResult marketVOResult = new MarketVOResult();
    marketVOResult.setMarket(marketVOS);
    return ServerResponse.createBySuccess(marketVOResult);
  }

  public ServerResponse getStock(int pageNum, int pageSize, String keyWords, String stockPlate, String stockType, HttpServletRequest request) {
    PageHelper.startPage(pageNum, pageSize);
    User user = iUserService.getCurrentUser(request);
    List<Stock> stockList = this.stockMapper.findStockListByKeyWords(keyWords, stockPlate, stockType, Integer.valueOf(0));

    List<String> codeList = stockList.stream().map(Stock::getStockGid).collect(Collectors.toList());

    Map<String, WjStockExtInfoDTO> infoMap = wjStockApi.getStockBatch(codeList);

    List<StockListVO> stockListVOS = Lists.newArrayList();
    if (stockList.size() > 0)
      for (Stock stock : stockList) {
        StockListVO stockListVO = WjStockApi.assembleStockListVO(infoMap.get(stock.getStockGid()));
        stockListVO.setName(stock.getStockName());
        stockListVO.setCode(stock.getStockCode());
        stockListVO.setSpell(stock.getStockSpell());
        stockListVO.setGid(stock.getStockGid());
//        BigDecimal day3Rate = (BigDecimal)selectRateByDaysAndStockCode(stock.getStockCode(), 3).getData();
//        stockListVO.setDay3Rate(day3Rate);
        stockListVO.setStock_plate(stock.getStockPlate());
        stockListVO.setStock_type(stock.getStockType());
        //是否添加自选
        if(user == null){
          stockListVO.setIsOption("0");
        } else {
          stockListVO.setIsOption(iStockOptionService.isMyOption(user.getId(), stock.getStockCode()));
        }
        stockListVOS.add(stockListVO);
      }
    PageInfo pageInfo = new PageInfo(stockList);
    pageInfo.setList(stockListVOS);
    return ServerResponse.createBySuccess(pageInfo);
  }
  public void z1() {
    this.stockPoll.z1();
  }
  public void z11() {
    this.stockPoll.z11();
  }
  public void z12() {
    this.stockPoll.z12();
  }

  public void z2() {
    this.stockPoll.z2();
  }
  public void z21() {
    this.stockPoll.z21();
  }
  public void z22() {
    this.stockPoll.z22();
  }

  public void z3() {
    this.stockPoll.z3();
  }
  public void z31() {
    this.stockPoll.z31();
  }
  public void z32() {
    this.stockPoll.z32();
  }

  public void z4() {
    this.stockPoll.z4();
  }
  public void z41() {
    this.stockPoll.z41();
  }
  public void z42() {
    this.stockPoll.z42();
  }

  public void h1() {
    this.stockPoll.h1();
  }
  public void h11() {
    this.stockPoll.h11();
  }
  public void h12() {
    this.stockPoll.h12();
  }

  public void h2() {
    this.stockPoll.h2();
  }
  public void h21() {
    this.stockPoll.h21();
  }
  public void h22() {
    this.stockPoll.h22();
  }

  public void h3() {
    this.stockPoll.h3();
  }
  public void h31() {
    this.stockPoll.h31();
  }
  public void h32() {
    this.stockPoll.h32();
  }


    public void bj1() {
        this.stockPoll.bj1();
    }

  public ServerResponse getDateline(HttpServletResponse response, String code) {
    if (StringUtils.isBlank(code))
      return ServerResponse.createByErrorMsg("");
    Stock stock = this.stockMapper.findStockByCode(code);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    response.setHeader("Access-Control-Allow-Origin", "*");
    Date time = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    String end = sdf.format(time);
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(2, -3);
    Date m = c.getTime();
    String mon = sdf.format(m);
    String methodUrl = "http://q.stock.sohu.com/hisHq?code=cn_" + code + "+&start=" + mon + "&end=" + end + "&stat=1&order=D";
    HttpURLConnection connection = null;
    BufferedReader reader = null;
    String line = null;
    EchartsDataVO echartsDataVO = new EchartsDataVO();
    try {
      URL url = new URL(methodUrl);
      connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();
      if (connection.getResponseCode() == 200) {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "gbk"));
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null)
          result.append(line).append(System.getProperty("line.separator"));
        JSONArray jsonArray = JSONArray.fromObject(result.toString());
        JSONObject json = jsonArray.getJSONObject(0);
        JSONArray jsonArray1 = JSONArray.fromObject(json.get("hq"));
        Collections.reverse((List<?>)jsonArray1);
        double[][] values = (double[][])null;
        Object[][] volumes = (Object[][])null;
        String[] date = null;
        values = new double[jsonArray1.size()][5];
        volumes = new Object[jsonArray1.size()][3];
        date = new String[jsonArray1.size()];
        for (int i = 0; i < jsonArray1.size(); i++) {
          JSONArray js = JSONArray.fromObject(jsonArray1.get(i));
          date[i] = js.get(0).toString();
          values[i][0] = Double.valueOf(js.get(1).toString()).doubleValue();
          values[i][1] = Double.valueOf(js.get(2).toString()).doubleValue();
          values[i][2] = Double.valueOf(js.get(5).toString()).doubleValue();
          values[i][3] = Double.valueOf(js.get(6).toString()).doubleValue();
          values[i][4] = Double.valueOf(js.get(7).toString()).doubleValue();
          volumes[i][0] = Integer.valueOf(i);
          volumes[i][1] = Double.valueOf(js.get(7).toString());
          volumes[i][2] = Integer.valueOf((Double.valueOf(js.get(3).toString()).doubleValue() > 0.0D) ? 1 : -1);
        }
        echartsDataVO.setDate(date);
        echartsDataVO.setValues(values);
        echartsDataVO.setVolumes(volumes);
        echartsDataVO.setStockCode(stock.getStockCode());
        echartsDataVO.setStockName(stock.getStockName());
//        log.info(String.valueOf(echartsDataVO));
        ServerResponse.createBySuccess(echartsDataVO);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      connection.disconnect();
    }
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  public ServerResponse getSingleStock(String code,HttpServletRequest request) {
    if (StringUtils.isBlank(code))
      return ServerResponse.createByErrorMsg("");
    Stock stock = new Stock();
    Integer depositAmt = 0;
    String introduction = null;
    String result = null;
    com.alibaba.fastjson2.JSONObject json = null;
    //期货
    String url = null;
    if (code.contains("hf")) {
      StockFutures futmodel = stockFuturesMapper.selectFuturesByCode(code.replace("hf_", ""));
      stock.setStockGid(futmodel.getFuturesGid());
      stock.setStockCode(futmodel.getFuturesCode());
      stock.setStockName(futmodel.getFuturesName());
      stock.setAddTime(futmodel.getAddTime());
      stock.setId(futmodel.getId());
      stock.setStockSpell("0");
      depositAmt = futmodel.getDepositAmt();
      url = PropertiesUtil.getProperty("sina.single.futures.introduction.url") + futmodel.getFuturesCode() + "&app=source&type=1";
    } else if (code.contains("sh") || code.contains("sz")) { //指数
      StockIndex model = stockIndexMapper.selectIndexByCode(code.replace("sh", "").replace("sz", ""));
      stock.setStockGid(model.getIndexGid());
      stock.setStockCode(model.getIndexCode());
      stock.setStockName(model.getIndexName());
      stock.setAddTime(model.getAddTime());
      stock.setId(model.getId());
      stock.setStockSpell("0");
      depositAmt = model.getDepositAmt();
      introduction = model.getIntroduction();


//
    } else {//股票

      stock = this.stockMapper.findStockByCode(code);
      if (stock == null)
        return ServerResponse.createByErrorMsg("");
          if (request.getHeader("lang") != null && !request.getHeader("lang").equals("zh-CN") && !request.getHeader("lang").equals("tww") && !request.getHeader("lang").equals("")) {
            GoogleTranslateUtil googleTranslateUtil = new GoogleTranslateUtil();
            try {
              introduction = googleTranslateUtil.translate(introduction, request.getHeader("lang"));
            } catch (Exception e) {
              log.error("翻译失败");
            }

        }

    }
    String gid = stock.getStockGid();
    //股票数据
    StockVO stockVO = new StockVO();

        String sinaResult = SinaStockApi.getSinaStock(gid);

        if (code.contains("hf")) {
          stockVO = SinaStockApi.assembleStockFuturesVO(sinaResult);
        } else {
          stockVO = SinaStockApi.assembleStockVO(sinaResult);
        }


//    if (stock.getDataBase()!=0){
//       String date = getCurrentTimeMiaoZero();
//      String result1 = HttpClientRequest.doGet(PropertiesUtil.getProperty("changePrice.url")+"?cat_time="+date+"&stock_gid="+stock.getStockGid()+"&price="+stockVO.getNowPrice());
//      JSONObject jsonObject = JSONObject.fromObject(result1);
//      String nowPrice = jsonObject.getJSONObject("data").getString("new_price");
//      if (nowPrice!=null){
//        stockVO.setNowPrice(nowPrice);
//      }else {
//        stockVO.setNowPrice(stockVO.getNowPrice());
//      }
//    }

      stockVO.setDepositAmt(depositAmt);
      stockVO.setId(stock.getId().intValue());
      stockVO.setCode(stock.getStockCode());
      stockVO.setSpell(stock.getStockSpell());
      stockVO.setGid(stock.getStockGid());
      stockVO.setMinImg(PropertiesUtil.getProperty("sina.single.stock.min.url") + stock.getStockGid() + ".jpg");
      stockVO.setDayImg(PropertiesUtil.getProperty("sina.single.stock.day.url") + stock.getStockGid() + ".jpg");
      stockVO.setWeekImg(PropertiesUtil.getProperty("sina.single.stock.week.url") + stock.getStockGid() + ".jpg");
      stockVO.setMonthImg(PropertiesUtil.getProperty("sina.single.stock.month.url") + stock.getStockGid() + ".jpg");
      Map map = Maps.newHashMap();
      map.put("introduction", introduction);
      map.put("stock", stockVO);
      return ServerResponse.createBySuccess(map);
    }


  public ServerResponse getMinK(String code, Integer time, Integer ma, Integer size) {
    if (StringUtils.isBlank(code) || time == null || ma == null || size == null)
      return ServerResponse.createByErrorMsg("");
    StockIndex stock =this.stockIndexMapper.selectIndexByCode(code);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    return SinaStockApi.getStockMinK(stock, time.intValue(), ma.intValue(), size.intValue());
  }

  public ServerResponse getMinK_Echarts(String code, Integer time, Integer ma, Integer size) {
    if (StringUtils.isBlank(code) || time == null || ma == null || size == null)
      return ServerResponse.createByErrorMsg("");
//    Stock stock = this.stockMapper.findStockByCode(code);
    StockIndex stock = this.stockIndexMapper.selectIndexByCode(code);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    ServerResponse<MinDataVO> serverResponse = SinaStockApi.getStockMinK(stock, time.intValue(), ma.intValue(), size.intValue());
    MinDataVO minDataVO = (MinDataVO)serverResponse.getData();
    EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  /*期货分时-k线*/
  public ServerResponse getFuturesMinK_Echarts(String code, Integer time, Integer size) {
    if (StringUtils.isBlank(code) || time == null)
      return ServerResponse.createByErrorMsg("");
    StockFutures stock = this.stockFuturesMapper.selectFuturesByCode(code.split("_")[1]);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    ServerResponse<MinDataVO> serverResponse = SinaStockApi.getFuturesMinK(stock, time.intValue(), size.intValue());
    MinDataVO minDataVO = (MinDataVO)serverResponse.getData();
    EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  /*指数分时-k线*/
  public ServerResponse getIndexMinK_Echarts(String code, Integer time, Integer size) {
    if (StringUtils.isBlank(code) || time == null)
      return ServerResponse.createByErrorMsg("");
    StockIndex stock = this.stockIndexMapper.selectIndexByCode(code.replace("sh","").replace("sz",""));
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    ServerResponse<MinDataVO> serverResponse = SinaStockApi.getIndexMinK(stock, time.intValue(), size.intValue());
    MinDataVO minDataVO = (MinDataVO)serverResponse.getData();
    EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  /*股票日线-K线*/
  public ServerResponse getDayK_Echarts(String code) {
    if (StringUtils.isBlank(code))
      return ServerResponse.createByErrorMsg("");
    Stock stock = this.stockMapper.findStockByCode(code);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    ServerResponse<MinDataVO> serverResponse = QqStockApi.getGpStockMonthK(stock,"day");
    MinDataVO minDataVO = (MinDataVO)serverResponse.getData();
    EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  /*期货日线-K线*/
  public ServerResponse getFuturesDayK(String code) {
    if (StringUtils.isBlank(code))
      return ServerResponse.createByErrorMsg("");
    StockFutures stock = this.stockFuturesMapper.selectFuturesByCode(code.split("_")[1]);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    ServerResponse<MinDataVO> serverResponse = QqStockApi.getQqStockDayK(stock);
    MinDataVO minDataVO = (MinDataVO)serverResponse.getData();
    EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  /*指数日线-K线*/
  public ServerResponse getIndexDayK(String code) {
    if (StringUtils.isBlank(code))
      return ServerResponse.createByErrorMsg("");
    StockIndex stock = this.stockIndexMapper.selectIndexByCode(code.replace("sh","").replace("sz",""));
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    ServerResponse<MinDataVO> serverResponse = QqStockApi.getQqIndexDayK(stock);
    MinDataVO minDataVO = (MinDataVO)serverResponse.getData();
    EchartsDataVO echartsDataVO = SinaStockApi.assembleEchartsDataVO(minDataVO);
    return ServerResponse.createBySuccess(echartsDataVO);
  }

  public ServerResponse<Stock> findStockByName(String name) {
    return ServerResponse.createBySuccess(this.stockMapper.findStockByName(name));
  }

  public ServerResponse<Stock> findStockByCode(String code) {
    return ServerResponse.createBySuccess(this.stockMapper.findStockByCode(code));
  }

  public ServerResponse<Stock> findStockById(Integer stockId) {
    return ServerResponse.createBySuccess(this.stockMapper.selectByPrimaryKey(stockId));
  }

  public ServerResponse<PageInfo> listByAdmin(Integer showState, Integer lockState, String code, String name, String stockPlate, String stockType, int pageNum, int pageSize, HttpServletRequest request) {
    PageHelper.startPage(pageNum, pageSize);
    List<Stock> stockList = this.stockMapper.listByAdmin(showState, lockState, code, name, stockPlate, stockType);
    List<StockAdminListVO> stockAdminListVOS = Lists.newArrayList();
    for (Stock stock : stockList) {
      StockAdminListVO stockAdminListVO = assembleStockAdminListVO(stock);
      stockAdminListVOS.add(stockAdminListVO);
    }
    PageInfo pageInfo = new PageInfo(stockList);
    pageInfo.setList(stockAdminListVOS);
    return ServerResponse.createBySuccess(pageInfo);
  }

  private StockAdminListVO assembleStockAdminListVO(Stock stock) {
    StockAdminListVO stockAdminListVO = new StockAdminListVO();
    stockAdminListVO.setId(stock.getId());
    stockAdminListVO.setStockName(stock.getStockName());
    stockAdminListVO.setStockCode(stock.getStockCode());
    stockAdminListVO.setStockSpell(stock.getStockSpell());
    stockAdminListVO.setStockType(stock.getStockType());
    stockAdminListVO.setStockGid(stock.getStockGid());
    stockAdminListVO.setStockPlate(stock.getStockPlate());
    stockAdminListVO.setIsLock(stock.getIsLock());
    stockAdminListVO.setIsShow(stock.getIsShow());
    stockAdminListVO.setAddTime(stock.getAddTime());
    stockAdminListVO.setDataBase(stock.getDataBase());
    StockListVO stockListVO;
//      stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stock.getStockGid()));

    stockListVO = byStockApi.getStockListVo(stock);
//      String stockOther = RedisShardedPoolUtils.get(stock.getStockName(), 8);
//      if (stock.getDataBase()!=0){
//
//         String date = getCurrentTimeMiaoZero();
//        String result = HttpClientRequest.doGet(PropertiesUtil.getProperty("changePrice.url")+"?cat_time="+date+"&stock_gid="+stock.getStockGid()+"&price="+stockListVO.getNowPrice());
//        JSONObject jsonObject = JSONObject.fromObject(result);
//        String nowPrice = jsonObject.getJSONObject("data").getString("new_price");
//        if (nowPrice!=null){
//          stockListVO.setNowPrice(nowPrice);
//        }else {
//          stockListVO.setNowPrice(stockListVO.getNowPrice());
//        }
//      }

    stockAdminListVO.setNowPrice(stockListVO.getNowPrice());
    stockAdminListVO.setHcrate(stockListVO.getHcrate());
    stockAdminListVO.setSpreadRate(stock.getSpreadRate());
    ServerResponse serverResponse = selectRateByDaysAndStockCode(stock.getStockCode(), 3);
    BigDecimal day3Rate = new BigDecimal("0");
    if (serverResponse.isSuccess())
      day3Rate = (BigDecimal)serverResponse.getData();
    stockAdminListVO.setDay3Rate(day3Rate);
    return stockAdminListVO;
  }

  public ServerResponse updateLock(Integer stockId) {
    Stock stock = this.stockMapper.selectByPrimaryKey(stockId);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    if (stock.getIsLock().intValue() == 1) {
      stock.setIsLock(Integer.valueOf(0));
    } else {
      stock.setIsLock(Integer.valueOf(1));
    }
    int updateCount = this.stockMapper.updateByPrimaryKeySelective(stock);
    if (updateCount > 0)
      return ServerResponse.createBySuccessMsg("成功");
    return ServerResponse.createByErrorMsg("失败");
  }

  public ServerResponse updateShow(Integer stockId) {
    Stock stock = this.stockMapper.selectByPrimaryKey(stockId);
    if (stock == null)
      return ServerResponse.createByErrorMsg("");
    if (stock.getIsShow().intValue() == 0) {
      stock.setIsShow(Integer.valueOf(1));
    } else {
      stock.setIsShow(Integer.valueOf(0));
    }
    int updateCount = this.stockMapper.updateByPrimaryKeySelective(stock);
    if (updateCount > 0)
      return ServerResponse.createBySuccessMsg("成功");
    return ServerResponse.createByErrorMsg("失败");
  }

  public ServerResponse addStock(String stockName, String stockCode, String stockType, String stockPlate, Integer isLock, Integer isShow) {
    if (StringUtils.isBlank(stockName) || StringUtils.isBlank(stockCode) || StringUtils.isBlank(stockType) || isLock == null || isShow == null)
      return ServerResponse.createByErrorMsg("所填信息有误");
    Stock cstock = (Stock) findStockByCode(stockCode).getData();
    if (cstock != null)
      return ServerResponse.createByErrorMsg("已有该股票代码");
    Stock nstock = (Stock) findStockByName(stockName).getData();
    if (nstock != null)
      return ServerResponse.createByErrorMsg("已有该股票名称");
    String sinaStock = SinaStockApi.getSinaStock(stockType + stockCode);
    String[] arrayOfString = sinaStock.split(",");
//    if (arrayOfString.length < 10)
//      return ServerResponse.createByErrorMsg("数据源无该股票");

    Stock stock = new Stock();
    stock.setStockName(stockName);
    stock.setStockCode(stockCode);
    stock.setStockSpell(GetPyByChinese.converterToFirstSpell(stockName));
    stock.setStockType(stockType);
    stock.setStockGid(stockType + stockCode);
    stock.setIsLock(isLock);
    stock.setIsShow(isShow);
    stock.setAddTime(new Date());
    if (stockPlate != null)
      stock.setStockPlate(stockPlate);

    if(stockPlate != null && StringUtils.isNotEmpty(stockPlate) && stockCode.startsWith("300"))
      stock.setStockPlate("创业");
    else if(stockPlate != null && StringUtils.isNotEmpty(stockPlate) &&stockCode.startsWith("688"))
      stock.setStockPlate("科创");
    else
      stock.setStockPlate(null);

    int insertCount = this.stockMapper.insert(stock);
    if (insertCount > 0)
      return ServerResponse.createBySuccessMsg("添加成功");
    return ServerResponse.createByErrorMsg("添加失败");
  }

  public int CountStockNum() {
    return this.stockMapper.CountStockNum();
  }

  public int CountShowNum(Integer showState) {
    return this.stockMapper.CountShowNum(showState);
  }

  public int CountUnLockNum(Integer lockState) {
    return this.stockMapper.CountUnLockNum(lockState);
  }

  public List findStockList() {
    return this.stockMapper.findStockList();
  }

  public ServerResponse selectRateByDaysAndStockCode(String stockCode, int days) {
    Stock stock = this.stockMapper.findStockByCode(stockCode);
    if (stock == null)
      return ServerResponse.createByErrorMsg("没有该股票");
    BigDecimal daysRate = this.iStockMarketsDayService.selectRateByDaysAndStockCode(stock.getId(), days);
    return ServerResponse.createBySuccess(daysRate);
  }

  public ServerResponse updateStock(Stock model) {
    if (StringUtils.isBlank(model.getId().toString()) || StringUtils.isBlank(model.getStockName()))
      return ServerResponse.createByErrorMsg("修改信息有误");
    Stock stock = this.stockMapper.selectByPrimaryKey(model.getId());
    if (stock == null)
      return ServerResponse.createByErrorMsg("股票不存在");
    stock.setStockName(model.getStockName());
    if (model.getSpreadRate() != null)
      stock.setSpreadRate(model.getSpreadRate());

    int updateCount = this.stockMapper.updateByPrimaryKeySelective(stock);
    if (updateCount > 0)
      return ServerResponse.createBySuccessMsg("修改成功");
    return ServerResponse.createByErrorMsg("修改失败");
  }

  public ServerResponse deleteByPrimaryKey(Integer id) {
    int updateCount = this.stockMapper.deleteByPrimaryKey(id);
    if (updateCount > 0) {
      return ServerResponse.createBySuccessMsg("操作成功");
    }
    return ServerResponse.createByErrorMsg("操作失败");
  }

  public ServerResponse stockDataBase() {
    //mybatisplus查询所有database 不是0的
    List<Stock> stockList = this.stockMapper.selectList(new QueryWrapper<Stock>().ne("data_base", 0));
    return ServerResponse.createBySuccess(stockList);
  }


  /**
   * 龙虎榜
   */
  @Override
  public ServerResponse lhb() {
    String day = getWeekDay();
    String url = PropertiesUtil.getProperty("longhu.url");


    url = url.replace("2022-12-02", day);
//    log.info("龙虎榜url:{}", url);
    com.alibaba.fastjson2.JSONArray data = null;
    try {
      String res = HttpClientRequest.doGet(url);
      com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(res);
      if (json.getJSONObject("result") != null) {
        data = json.getJSONObject("result").getJSONArray("data");
      }else {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(addDay(new Date(),-1));
        url = url.replace(day, date);
        String res1 = HttpClientRequest.doGet(url);
        com.alibaba.fastjson2.JSONObject json1 = com.alibaba.fastjson2.JSONObject.parseObject(res1);
        data = json1.getJSONObject("result").getJSONArray("data");
      }
    } catch (Exception e) {
      log.error("龙虎榜出错", e);
    }
    return ServerResponse.createBySuccess(data);
  }
  /**
   * 十大成交股
   * content
   */
  @Override
  public ServerResponse top(Integer content) {

    String url = PropertiesUtil.getProperty("top10.url")+content+"%22)";
    com.alibaba.fastjson2.JSONArray data = null;
    try {
      String res = HttpClientRequest.doGet(url);
      com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(res);
      data = json.getJSONObject("result").getJSONArray("data");
    } catch (Exception e) {
      log.error("十大成交股出错", e);
    }
    return ServerResponse.createBySuccess(data);
  }
  /**
   * 每日停牌
   */
  @Override
  public ServerResponse stop() {
    String day = getWeekDay();
    String url = PropertiesUtil.getProperty("stop.url");
    url = url.replace("2022-12-02", day);
    com.alibaba.fastjson2.JSONArray data = null;
    try {
      String res = HttpClientRequest.doGet(url);
      com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(res);
      data = json.getJSONObject("result").getJSONArray("data");
    } catch (Exception e) {
      log.error("每日停牌出错", e);
    }
    return ServerResponse.createBySuccess(data);
  }


  /**
   * 获取同花顺涨跌幅榜
   * @return
   */
  @Override
  public ServerResponse getZdfb() {
    Object zdfb = CacheUtil.get("zdfb");
    if(!ObjectUtils.isEmpty(zdfb)){
      ArrayList<Object> zdfbList = (ArrayList<Object>) zdfb;
      log.info("涨跌幅榜命中缓存");
      return ServerResponse.createBySuccess(zdfbList);
    }
    String market_url = PropertiesUtil.getProperty("dfcf.market.zdfb");
    String result = null;
    try {
      result = HttpClientRequest.doGet(market_url);
      com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
      com.alibaba.fastjson.JSONObject data = jsonObject.getJSONObject("data");
      com.alibaba.fastjson.JSONArray diff = data.getJSONArray("diff");
      com.alibaba.fastjson.JSONObject sh = diff.getJSONObject(0);
      com.alibaba.fastjson.JSONObject sz = diff.getJSONObject(1);
      ArrayList<ChartCellVO> objects = new ArrayList<>();
      String f104 = sh.getString("f104");
      String f105 = sh.getString("f105");
      String f106 = sh.getString("f106");
      ChartCellVO v1 = new ChartCellVO(f104,"沪涨");
      ChartCellVO v2 = new ChartCellVO(f106,"沪平");
      ChartCellVO v3 = new ChartCellVO(f105,"沪跌");
      String sf104 = sz.getString("f104");
      String sf105 = sz.getString("f105");
      String sf106 = sz.getString("f106");
      ChartCellVO v4 = new ChartCellVO(sf104,"深涨");
      ChartCellVO v5 = new ChartCellVO(sf106,"深平");
      ChartCellVO v6 = new ChartCellVO(sf105,"深跌");

      objects.add(v1);objects.add(v2);
      objects.add(v3);objects.add(v4);
      objects.add(v5);objects.add(v6);
      CacheUtil.set("zdfb",objects,60000);
      return ServerResponse.createBySuccess(objects);
    } catch (Exception e) {
      log.error("涨跌比异常", e);
    }

    return null;
  }
  /**
   *
   * @param pageNo
   * @param pageSize
   * @param sort  根据什么排序   changepercent 涨跌幅  pricechange 涨跌额  volume 成交量 amount 成交额
   * @param asc   是否升序 0否 1是
   * @param node  排序的主板类型 科创板  创业板 a股  北交所等   hs_bjs 北交所  cyb 创业板  kcb 科创板   hs_a 沪深a股
   * @return
   */
  @Override
  public ServerResponse getStockSort(Integer pageNo, Integer pageSize, String sort, Integer asc, String node) {
    String market_url = PropertiesUtil.getProperty("sina.market.zdf.url");
    String result = null;
    String param="page="+pageNo+"&num="+pageSize+"&sort="+sort+"&asc="+asc+"&node="+node+"&symbol=&_s_r_a=sort";
    String url=market_url+param;
    result = HttpClientRequest.doGet(url);
    com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(result);

    for (int i = 0; i < jsonArray.size(); i++) {

      com.alibaba.fastjson.JSONObject jo = jsonArray.getJSONObject(i);
      Stock stock = stockMapper.findStockByCode(jo.getString("code"));
      jo.put("show", stock != null);

    }

    return ServerResponse.createBySuccess(jsonArray);
  }

  /**
   * 涨停板
   * @return
   */
  @Override
  public ServerResponse ztb() {
    String day = getWeekDay();
    List<StockDTO> list = byStockApi.getDailyZt(day);
    return ServerResponse.createBySuccess(list);
  }

  @Override
  public ServerResponse getVipByCode(String code) {
    String day = getWeekDay();
    String url = PropertiesUtil.getProperty("ztb.url");
    String day1 = day.replace("-", "");
    long time = System.currentTimeMillis();
    url = url.replace("20221202", day1) + time;
//    log.info("涨停板接口：{}",url);
    String res = HttpClientRequest.doGet(url);

    com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(res);
    com.alibaba.fastjson2.JSONArray pool = null;
    if (json.getJSONObject("data") != null) {
      pool = json.getJSONObject("data").getJSONArray("pool");
      if (pool != null) {
        for (int i = 0; i < pool.size(); i++) {
          com.alibaba.fastjson2.JSONObject jsonObject = pool.getJSONObject(i);
          String c = jsonObject.getString("c");
          if (c.equals(code)) {
            pool.removeAll(pool);
            pool.add(jsonObject);
            return ServerResponse.createBySuccess(pool);
          }
        }
      }
    }
    return ServerResponse.createBySuccess(pool);
  }


  @Override
  public ServerResponse getSingleStockNew(String code, HttpServletRequest request) {

    Stock stockByCode = stockMapper.findStockByCode(code);

    StockDTO stock = this.byStockApi.getStock(code);
    stock.setDm(stockByCode.getStockGid());
    stock.setMc(stockByCode.getStockName());
    stock.setId(stockByCode.getId());

    com.alibaba.fastjson.JSONObject apiMmwp = byStockApi.getMmwp(code);

    stock.setMmwp(apiMmwp);

    return ServerResponse.createBySuccess(stock);
  }

  @Override
  public ServerResponse getSingleStockNew2(String code, HttpServletRequest request) {

    Stock stockByCode = stockMapper.findStockByCode(code);

    WjStockExtInfoDTO realData = wjStockApi.getRealData(stockByCode.getStockGid());
    realData.setSymbol(stockByCode.getStockGid());
    realData.setStockId(stockByCode.getId());

    return ServerResponse.createBySuccess(realData);
  }

  @Override
  public void updateStockData() {

    QueryParam queryParam = new QueryParam();

    queryParam.setPage(0);
    queryParam.setLimit(1000);
    queryParam.setSortField("px_change_rate");
    queryParam.setType("1");
    queryParam.setOrderBy("desc");


    boolean isLast = false;

    while (!isLast) {
      queryParam.setPage(queryParam.getPage() + 1);
      WjPageRet ret = wjStockApi.getPageList(queryParam);
      WjPageData data = ret.getData();
      isLast = data.isLast();

      List<com.alibaba.fastjson.JSONObject> dataList = data.getDataList();

      dataList.parallelStream().forEach(dto -> {

        String prodCode = dto.getString("prod_code");
        String code = StringUtils.substring(prodCode, 0, 6);
        String name = dto.getString("prod_name");
        String stockType = StringUtils.substring(prodCode, 7).toLowerCase();


        Stock stock = null;
        try {
          stock = stockMapper.findStockByCode(code);
        } catch (Exception e) {
          log.error(e.getMessage(), e);
          return;
        }

        if (stock == null) {

          stock = new Stock();
          stock.setStockName(name);
          stock.setStockCode(code);
          stock.setStockType(stockType);
          stock.setStockGid(String.format("%s%s", stockType, code));
          stock.setAddTime(new Date());

          stock.setStockPlate(getStockPlate(code));

          stockMapper.insertSelective(stock);
          return;
        }

        Stock updateStock = new Stock();
        updateStock.setId(stock.getId());
        updateStock.setStockName(name);
        stock.setStockPlate(getStockPlate(code));
        stockMapper.updateByPrimaryKeySelective(updateStock);
      });
    }

  }

  private String getStockPlate(String dm) {

    if (StringUtils.startsWith(dm, "300")) {
      return "创业";
    } else if (StringUtils.startsWith(dm, "688")) {
      return "科创";
    }

    return null;
  }

  @Override
  public List<Stock> getHideStockCode() {

    LambdaQueryWrapper<Stock> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Stock::getIsShow, 1);
    return stockMapper.selectList(queryWrapper);
  }
}
