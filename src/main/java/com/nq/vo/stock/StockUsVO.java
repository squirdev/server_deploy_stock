
package com.nq.vo.stock;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockUsVO {
    private int id;
    private String name;
    private String code;
    private String spell;
    private String gid;
    private String nowPrice;
    private BigDecimal hcrate;
    private String today_max;
    private String today_min;

    private String business_balance;

    private String business_amount;

    private String preclose_px;

    private String open_px;
    private String type;

    private String timeDate;
    public void setId(int id) {
        this.id = id;
    }




    private Integer depositAmt;

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSpell(String spell) {
        this.spell = spell;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public void setNowPrice(String nowPrice) {
        this.nowPrice = nowPrice;
    }

    public void setHcrate(BigDecimal hcrate) {
        this.hcrate = hcrate;
    }

    public void setToday_max(String today_max) {
        this.today_max = today_max;
    }

    public void setToday_min(String today_min) {
        this.today_min = today_min;
    }

    public void setBusiness_balance(String business_balance) {
        this.business_balance = business_balance;
    }

    public void setBusiness_amount(String business_amount) {
        this.business_amount = business_amount;
    }

    public void setPreclose_px(String preclose_px) {
        this.preclose_px = preclose_px;
    }

    public void setOpen_px(String open_px) {
        this.open_px = open_px;
    }



    protected boolean canEqual(Object other) {
        return other instanceof StockUsVO;
    }




    public int getId() {
        return this.id;
    }


    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getSpell() {
        return this.spell;
    }

    public String getGid() {
        return this.gid;
    }


    public String getNowPrice() {
        return this.nowPrice;
    }


    public BigDecimal getHcrate() {
        return this.hcrate;
    }


    public String getToday_max() {
        return this.today_max;
    }


    public String getToday_min() {
        return this.today_min;
    }


    public String getBusiness_balance() {
        return this.business_balance;
    }


    public String getBusiness_amount() {
        return this.business_amount;
    }


    public String getPreclose_px() {
        return this.preclose_px;
    }


    public String getOpen_px() {
        return this.open_px;
    }







    public Integer getDepositAmt() {
        return depositAmt;
    }

    public void setDepositAmt(Integer depositAmt) {
        this.depositAmt = depositAmt;
    }
}

