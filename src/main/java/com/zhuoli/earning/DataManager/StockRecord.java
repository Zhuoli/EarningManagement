package com.zhuoli.earning.DataManager;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Created by zhuol on 2017/7/5.
 */

@Data
@Builder
public class StockRecord {
    private String symbol;
    private String companyname;
    private Date reportDate;
    private double currentPrice;
    private int shares;
    private double sharedAverageCost;
    private double targetPrice;
    private Date timestamp;
    private Date CurrentPriceLatestUpdateTime;
}
