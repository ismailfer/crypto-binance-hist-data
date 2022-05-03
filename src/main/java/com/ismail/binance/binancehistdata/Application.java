package com.ismail.binance.binancehistdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CandleCollectConfiguration.class})
public class Application
{

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

}
