package com.ismail.binance.binancehistdata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class CandleCollectServiceTest
{
    @Autowired
    CandleCollectService service;

    @Test
    public void mineDataAndSaveToDB()
    {
        LocalDateTime start = LocalDateTime.of(2022, 04, 1, 0, 0);
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        int numberOfRecords = service.mineData(start, end, Symbol.BTCUSDT, Interval.FIFTEEN_MIN);

        Assertions.assertTrue(numberOfRecords > 0);

    }

    @Test
    public void extractCandles()
    {
        LocalDateTime start = LocalDateTime.of(2022, 04, 1, 0, 0);
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<CandleItem> items = service.extractCandles(start, end, Symbol.BTCUSDT, Interval.FIFTEEN_MIN);

        Assertions.assertTrue(items.size() > 0);

        items.forEach(e -> {
            System.out.println(e.simpleToString());
        });
    }
}
