package com.ismail.binance.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@SpringBootTest
@Slf4j
public class BinanceServiceTest
{
    @Autowired
    private BinanceService service;

    @Test
    public void getServerPing()
    {
        ServerPing serverPing1 = service.getServerPing();

        ServerPing serverPing2 = service.getServerPing();

        System.out.println("ServerPing1: " + serverPing1.toString());
        System.out.println("ServerPing2: " + serverPing2.toString());
    }

    @Test
    public void getServerTime()
    {
        ServerTime serverTime = service.getServerTime();

        System.out.println("ServerTime: " + serverTime.toString());
    }

    @Test
    public void getBookTicker()
    {
        BookTicker ticker = service.getBookTicker(Symbol.BTCUSDT);

        System.out.println("BookTicker: " + ticker.toStringSimple());
    }

    @Test
    public void getHistDataAndSaveToDB()
    {
        LocalDateTime start = LocalDateTime.of(2022, 04, 1, 0, 0, 0);
        LocalDateTime end = start.plusHours(1).minusNanos(1);

        int numberOfRecords = service.getHistDataAndSaveToDB(Symbol.BTCUSDT, Interval.ONE_MIN, start, end,true);

        System.out.println("getHistDataAndSaveToDB() Records downloaded: " + numberOfRecords);

        Assertions.assertTrue(numberOfRecords > 0);
    }

    @Test
    public void getHistDataSingleChunk()
    {
        LocalDateTime start = LocalDateTime.of(2022, 04, 1, 0, 0, 0, 0);
        //LocalDateTime end = start.plusHours(1).minusNanos(1);
        LocalDateTime end = start.plusHours(1);

        List<Candle> items = service.getHistDataSingleChunk(Symbol.BTCUSDT, Interval.ONE_MIN, start.toEpochSecond(ZoneOffset.UTC) * 1000L, end.toEpochSecond(ZoneOffset.UTC) * 1000L);

        Assertions.assertTrue(items.size() > 0);

        items.forEach(e -> {
            System.out.println(e.simpleToString());
        });
    }
}
