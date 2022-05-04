package com.ismail.binance.binancehistdata;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("histdata")
public class HistDataConfiguration
{
    private String urlPrefix;

    private String url;

    private String urlQuerySymbol;

    private String urlQueryInterval;

    private String urlQueryStartTime;

    private String urlQueryEndTime;

    private String urlQueryLimit;
}
