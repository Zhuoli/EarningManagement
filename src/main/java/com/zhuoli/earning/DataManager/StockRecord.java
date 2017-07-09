package com.zhuoli.earning.DataManager;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * Created by zhuol on 2017/7/5.
 */

@Data
@Builder
public class StockRecord {
    @SerializedName("_id")
    private String symbol;
    private String companyname;
    private LocalDate reportDate;
    private double currentPrice;
    private int shares;
    private double sharedAverageCost;
    private double targetPrice;
    private Date timestamp;
    private Date CurrentPriceLatestUpdateTime;
    private boolean hasUpdate = false;
}
