package com.nq.service;

import com.nq.common.ServerResponse;
import com.nq.pojo.SiteProduct;

public interface ISiteProductService {
  ServerResponse update(SiteProduct paramSiteProduct);

  SiteProduct getProductSetting();
}
