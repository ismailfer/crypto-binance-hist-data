package com.ismail.binance.api;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder
@Data
@Slf4j
public class Candle
{
    // MongoDB id
    @Id
    private String _id;

    private Symbol symbol;

    private Interval interval;

    // candle data

    private long openTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private long closeTime;

    private double quoteAssetVolume;
    private long numberOfTrades;
    private double takerBuyBaseAssetVolume;
    private double takerBuyQuoteAssetVolume;
    private double ignore;


    public static Candle fromArray(List<Object> fields, Symbol symbol, Interval interval)
    {
        int i = 0;

        Candle ci = Candle.builder()
                .symbol(symbol)
                .interval(interval)
                .openTime(Long.parseLong(fields.get(i++).toString()))
                .open(Double.parseDouble(fields.get(i++).toString()))
                .high(Double.parseDouble(fields.get(i++).toString()))
                .low(Double.parseDouble(fields.get(i++).toString()))
                .close(Double.parseDouble(fields.get(i++).toString()))
                .volume(Double.parseDouble(fields.get(i++).toString()))
                .closeTime(Long.parseLong (fields.get(i++).toString()))
                .quoteAssetVolume(Double.parseDouble(fields.get(i++).toString()))
                .numberOfTrades(Long.parseLong (fields.get(i++).toString()))
                .takerBuyBaseAssetVolume(Double.parseDouble(fields.get(i++).toString()))
                .takerBuyQuoteAssetVolume(Double.parseDouble(fields.get(i++).toString()))
                .ignore(Double.parseDouble(fields.get(i++).toString()))
                .build();

        return ci;
    }

    /**
     * @return formatted time
     */
    public LocalDateTime openDateTime()
    {
        return LocalDateTime.ofEpochSecond(openTime/1000, (int)(openTime%1000), ZoneOffset.UTC);
    }

    /**
     * @return formatted time
     */
    public LocalDateTime closeDateTime()
    {
        return LocalDateTime.ofEpochSecond(closeTime/1000, (int)(closeTime%1000), ZoneOffset.UTC);
    }

    public double difference()
    {
        return close - open;
    }

    public double differencePercentage()
    {
        return (difference() / open) * 100.0;
    }

    public String simpleToString()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss");

        return String.format("%s - %s close: %s volume: %s diff: %s ",
                openDateTime().format(formatter),
                closeDateTime().format(formatter),
                close,
                volume,
                differencePercentage());
    }


}
