package com.ismail.binance.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("binance")
public class BinanceConfig
{
    private int chunkMax = 1000;

    // Binance base URL

    private String urlPrefix;

    // Sever ping
    private String serverPingUrl;

    // Sever time
    private String serverTimeUrl;

    // Book Ticker

    private String bookTickerUrl;

    private String bookTickerQuerySymbol;

    // KLines

    private String klinesUrl;

    private String klinesQuerySymbol;

    private String klinesQueryInterval;

    private String klinesQueryStartTime;

    private String klinesQueryEndTime;

    private String klinesQueryLimit;



}
