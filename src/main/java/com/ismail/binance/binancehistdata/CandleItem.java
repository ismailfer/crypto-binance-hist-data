package com.ismail.binance.binancehistdata;

import com.ismail.binance.binancehistdata.indicator.Constants;
import com.ismail.binance.binancehistdata.indicator.Indicator;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Builder
@Data
@Slf4j
public class CandleItem
{
    // MongoDB id
    @Id
    private String _id;

    private Symbol symbol;

    private Interval interval;

    // candle data

    private Long openTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private Long closeTime;

    private BigDecimal quoteAssetVolume;
    private BigInteger numberOfTrades;
    private BigDecimal takerBuyBaseAssetVolume;
    private BigDecimal takerBuyQuoteAssetVolume;
    private BigDecimal ignore;

    private Indicator indicator;
    private boolean indicatorCalculated;


    {
        indicatorCalculated = false;
        indicator = Indicator.builder().build();
    }


    public static CandleItem fromArray(List<Object> fields, Symbol symbol, Interval interval)
    {
        int i = 0;

        CandleItem ci = CandleItem.builder()
                .symbol(symbol)
                .interval(interval)
                .openTime((Long) fields.get(i++))
                .open(new BigDecimal(fields.get(i++).toString()))
                .high(new BigDecimal(fields.get(i++).toString()))
                .low(new BigDecimal(fields.get(i++).toString()))
                .close(new BigDecimal(fields.get(i++).toString()))
                .volume(new BigDecimal(fields.get(i++).toString()))
                .closeTime((Long) fields.get(i++))
                .quoteAssetVolume(new BigDecimal(fields.get(i++).toString()))
                .numberOfTrades(BigInteger.valueOf(Long.parseLong(fields.get(i++).toString())))
                .takerBuyBaseAssetVolume(new BigDecimal(fields.get(i++).toString()))
                .takerBuyQuoteAssetVolume(new BigDecimal(fields.get(i++).toString()))
                .ignore(new BigDecimal(fields.get(i).toString()))
                .indicator(Indicator.builder().build())
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

    public BigDecimal difference()
    {
        return close.subtract(open);
    }

    public BigDecimal differencePercentage()
    {
        return difference().divide(open, 20, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public String simpleToString()
    {
        return String.format("%s - %s close: %s volume: %s diff: %s ",
                openDateTime(),
                closeDateTime(),
                close,
                volume,
                differencePercentage());
    }


}
