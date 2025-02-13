package com.nq.service;

import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.Stock;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IStockService {
  ServerResponse getMarket();

  ServerResponse getStock(int paramInt1, int paramInt2, String paramString1, String paramString2, String paramString3, HttpServletRequest request);

  void z1();
  void z11();
  void z12();

  void z2();
  void z21();
  void z22();

  void z3();
  void z31();
  void z32();

  void z4();
  void z41();
  void z42();

  void h1();
  void h11();
  void h12();

  void h2();
  void h21();
  void h22();

  void h3();
  void h31();
  void h32();
  void bj1();
  ServerResponse getDateline(HttpServletResponse paramHttpServletResponse, String paramString);

  ServerResponse getSingleStock(String paramString,HttpServletRequest request);

  ServerResponse getMinK(String paramString, Integer paramInteger1, Integer paramInteger2, Integer paramInteger3);

  ServerResponse getMinK_Echarts(String paramString, Integer paramInteger1, Integer paramInteger2, Integer paramInteger3);

  ServerResponse getFuturesMinK_Echarts(String paramString, Integer paramInteger1, Integer paramInteger2);

  ServerResponse getIndexMinK_Echarts(String paramString, Integer paramInteger1, Integer paramInteger2);

  ServerResponse getDayK_Echarts(String paramString);

  ServerResponse getFuturesDayK(String paramString);

  ServerResponse getIndexDayK(String paramString);

  ServerResponse<Stock> findStockByName(String paramString);

  ServerResponse<Stock> findStockByCode(String paramString);

  ServerResponse<Stock> findStockById(Integer paramInteger);

  ServerResponse<PageInfo> listByAdmin(Integer paramInteger1, Integer paramInteger2, String paramString1, String paramString2, String paramString3, String paramString4, int paramInt1, int paramInt2, HttpServletRequest paramHttpServletRequest);

  ServerResponse updateLock(Integer paramInteger);

  ServerResponse updateShow(Integer paramInteger);

  ServerResponse addStock(String paramString1, String paramString2, String paramString3, String paramString4, Integer paramInteger1, Integer paramInteger2);

  int CountStockNum();

  int CountShowNum(Integer paramInteger);

  int CountUnLockNum(Integer paramInteger);

  List findStockList();

  ServerResponse selectRateByDaysAndStockCode(String paramString, int paramInt);

  ServerResponse updateStock(Stock model);

  ServerResponse deleteByPrimaryKey(Integer id);

    ServerResponse stockDataBase();

  ServerResponse lhb();

  ServerResponse top(Integer content);

  ServerResponse stop();


  /**
   * 获取同花顺涨跌幅榜
   * @return
   */
  ServerResponse getZdfb();


  /**
   *
   * @param pageNo
   * @param pageSize
   * @param sort  根据什么排序
   * @param asc   是否升序 0否 1是
   * @param node  排序的主板类型 科创板  创业板 a股  北交所等
   * @return
   */
  ServerResponse getStockSort(Integer pageNo, Integer pageSize, String sort, Integer asc, String node);

  /**
   * 涨停板
   * @return
   */
  ServerResponse ztb();

  ServerResponse getVipByCode(String code);

  ServerResponse getSingleStockNew(String code, HttpServletRequest request);
  ServerResponse getSingleStockNew2(String code, HttpServletRequest request);

  void updateStockData();

  List<Stock> getHideStockCode();

}
