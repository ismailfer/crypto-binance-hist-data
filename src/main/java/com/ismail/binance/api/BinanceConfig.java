package com.ismail.binance.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("binance")
public class BinanceConfig
{
    private String urlPrefix;

    private String klinesUrl;

    private String klinesUrlQuerySymbol;

    private String klinesUrlQueryInterval;

    private String klinesUrlQueryStartTime;

    private String klinesUrlQueryEndTime;

    private String klinesUrlQueryLimit;
}
