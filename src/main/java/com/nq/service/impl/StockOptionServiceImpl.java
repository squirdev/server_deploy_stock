package com.nq.service.impl;

 import com.github.pagehelper.PageHelper;

 import com.github.pagehelper.PageInfo;

 import com.google.common.collect.Lists;

 import com.nq.common.ServerResponse;

 import com.nq.dao.StockMapper;

 import com.nq.dao.StockOptionMapper;

 import com.nq.pojo.Stock;

 import com.nq.pojo.StockOption;

 import com.nq.pojo.User;

 import com.nq.service.IStockOptionService;

 import com.nq.service.IUserService;

 import com.nq.utils.BigDecimalUtil;
 import com.nq.utils.redis.RedisShardedPoolUtils;
 import com.nq.utils.stock.biying.ByStockApi;
 import com.nq.utils.stock.sina.SinaStockApi;

 import com.nq.utils.stock.wangji.WjStockApi;
 import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
 import com.nq.vo.stock.StockDTO;
 import com.nq.vo.stock.StockOptionListVO;

 import com.nq.vo.stock.StockVO;

 import java.util.List;
 import java.util.Map;
 import java.util.stream.Collectors;

 import javax.servlet.http.HttpServletRequest;

 import org.slf4j.Logger;

 import org.slf4j.LoggerFactory;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.stereotype.Service;


 @Service("iStockOptionService")

 public class StockOptionServiceImpl implements IStockOptionService {

   private static final Logger log = LoggerFactory.getLogger(StockOptionServiceImpl.class);

   @Autowired
   StockOptionMapper stockOptionMapper;

   @Autowired
   IUserService iUserService;

   @Autowired
   StockMapper stockMapper;

   @Autowired
     ByStockApi byStockApi;

   @Autowired
   private WjStockApi wjStockApi;

   public ServerResponse<PageInfo> findMyStockOptions(String keyWords, HttpServletRequest request, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);
     User user = this.iUserService.getCurrentUser(request);

       if (user == null ){
           return ServerResponse.createBySuccessMsg("請先登錄");
       }
     List<StockOption> stockOptions = this.stockOptionMapper.findMyOptionByKeywords(user.getId(), keyWords);


       List<String> codeList = stockOptions.stream().map(s -> s.getStockGid()).collect(Collectors.toList());


       Map<String, WjStockExtInfoDTO> stockMap = wjStockApi.getStockBatch(codeList);

       List<StockOptionListVO> stockOptionListVOS = Lists.newArrayList();
     for (StockOption option : stockOptions) {
       StockOptionListVO stockOptionListVO = assembleStockOptionListVO(option, stockMap.get(option.getStockGid()));
       stockOptionListVO.setIsOption("1");
       stockOptionListVOS.add(stockOptionListVO);
     }
     PageInfo pageInfo = new PageInfo(stockOptions);

     pageInfo.setList(stockOptionListVOS);

     return ServerResponse.createBySuccess(pageInfo);

   }

   public ServerResponse isOption(Integer uid, String code) {

     StockOption stockOption = this.stockOptionMapper.isOption(uid, code);

     if (stockOption == null) {

       return ServerResponse.createBySuccessMsg("未添加");

     }

     return ServerResponse.createByErrorMsg("已添加");

   }

     public String isMyOption(Integer uid, String code) {
         StockOption stockOption = this.stockOptionMapper.isOption(uid, code);
         if (stockOption == null) {
             return "0";
         }
         return "1";

     }

     private StockOptionListVO assembleStockOptionListVO(StockOption option) {

         StockOptionListVO stockOptionListVO = new StockOptionListVO();



         stockOptionListVO.setId(option.getId().intValue());

         stockOptionListVO.setStockName(option.getStockName());

         stockOptionListVO.setStockCode(option.getStockCode());

         stockOptionListVO.setStockGid(option.getStockGid());


         StockDTO dto = byStockApi.getStock(option.getStockCode());


         stockOptionListVO.setNowPrice(dto.getP().toString());

         stockOptionListVO.setHcrate(dto.getPc().toString());

//         stockOptionListVO.setPreclose_px(stockVO.getPreclose_px());
//
//         stockOptionListVO.setOpen_px(stockVO.getOpen_px());

//         stockOptionListVO.setType(stockVO.getType());

         Stock stock = this.stockMapper.selectByPrimaryKey(option.getStockId());

         stockOptionListVO.setStock_plate(stock.getStockPlate()==null?"":stock.getStockPlate());

         stockOptionListVO.setStock_type(stock.getStockType());


         return stockOptionListVO;

     }

   private StockOptionListVO assembleStockOptionListVO(StockOption option, WjStockExtInfoDTO dto) {

         StockOptionListVO stockOptionListVO = new StockOptionListVO();



         stockOptionListVO.setId(option.getId().intValue());

         stockOptionListVO.setStockName(option.getStockName());

         stockOptionListVO.setStockCode(option.getStockCode());

         stockOptionListVO.setStockGid(option.getStockGid());



       stockOptionListVO.setNowPrice(BigDecimalUtil.formatWithNoTQ(dto.getPrice()));

         stockOptionListVO.setHcrate(dto.getChangeRateStr());

//         stockOptionListVO.setPreclose_px(stockVO.getPreclose_px());
//
//         stockOptionListVO.setOpen_px(stockVO.getOpen_px());

//         stockOptionListVO.setType(stockVO.getType());

         Stock stock = this.stockMapper.selectByPrimaryKey(option.getStockId());

       stockOptionListVO.setStock_plate(stock.getStockPlate()==null?"":stock.getStockPlate());

         stockOptionListVO.setStock_type(stock.getStockType());


         return stockOptionListVO;

     }
 }
