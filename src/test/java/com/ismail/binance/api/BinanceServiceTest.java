package com.ismail.binance.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class BinanceServiceTest
{
    @Autowired
    private BinanceService service;

    @Test
    public void getHistDataAndSaveToDB()
    {
        LocalDateTime start = LocalDateTime.of(2022, 04, 1, 0, 0);
        LocalDateTime end = start.plusHours(1).minusNanos(1);

        int numberOfRecords = service.getHistDataAndSaveToDB(start, end, Symbol.BTCUSDT, Interval.ONE_MIN, true);

        Assertions.assertTrue(numberOfRecords > 0);
    }

    @Test
    public void getHistData()
    {
        LocalDateTime start = LocalDateTime.of(2022, 04, 1, 0, 0, 0, 0);
        //LocalDateTime end = start.plusHours(1).minusNanos(1);
        LocalDateTime end = start.plusHours(1);

        List<Candle> items = service.getHistData(start, end, Symbol.BTCUSDT, Interval.ONE_MIN);

        Assertions.assertTrue(items.size() > 0);

        items.forEach(e -> {
            System.out.println(e.simpleToString());
        });
    }
}
