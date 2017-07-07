package com.zhuoli.earning.DataManager;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zhuoli on 8/24/16.
 */
@Data
@Builder
public class Order {

    /**
     * Stock Symbol
     */
    private String Symbol;

    /**
     * Numbers of shares.
     */
    private int Shares;

    /**
     * Order price.
     */
    private Double Price;

    /**
     * Is this order Buying or Selling.
     */
    private OrderType type;
}
