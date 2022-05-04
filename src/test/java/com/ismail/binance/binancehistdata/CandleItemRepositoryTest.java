package com.ismail.binance.binancehistdata;

import com.sun.jdi.IntegerValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
public class CandleItemRepositoryTest
{
    @Autowired
    private CandleItemRepository repository;

    private CandleItem createTestData(Symbol symbol, Interval interval)
    {
        CandleItem ci = CandleItem.builder()
                .symbol(symbol)
                .interval(interval)
                .open(10.0)
                .high(10.0)
                .low(10.0)
                .close(10.0)
                .openTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L)
                .openTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L + interval.getMinutes() * 60000L)
                .numberOfTrades(20L)
                .ignore(10.0)
                .build();

        return ci;
    }

    @Test
    public void testCreateAndDelete()
    {
        repository.deleteAll();

        repository.save(createTestData(Symbol.BTCUSDT, Interval.FIFTEEN_MIN));
        repository.save(createTestData(Symbol.BTCUSDT, Interval.FIVE_MIN));
        repository.save(createTestData(Symbol.BTCUSDT, Interval.ONE_HOUR));
        repository.save(createTestData(Symbol.LTCUSDT, Interval.FIFTEEN_MIN));
        repository.save(createTestData(Symbol.LTCUSDT, Interval.EIGHT_HOUR));

        Assertions.assertEquals(repository.count(), 5);

        repository.deleteBySymbolAndInterval(Symbol.LTCUSDT, Interval.EIGHT_HOUR);

        Assertions.assertEquals(repository.count(), 4);

    }

}
