package com.ismail.binance.binancehistdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleCollectService
{
    private final CandleCollectConfiguration config;

    private final CandleItemRepository repository;

    //private final IndicatorForExistingCandlesService indicatorForExistingCandlesService;

    private static final int CHUNK_MAX = 1000;


    /**
     * Downloads the data from binance server
     * divide period requested into smaller periods compatible with binance rules
     * max request records by binance is 1000.
     *
     * @param begin
     * @param end
     * @param symbol
     * @param interval
     * @return  number of records mined
     */
    public int mineData(LocalDateTime begin, LocalDateTime end, Symbol symbol, Interval interval)
    {
        log.info("Mine data for: {} to: {} for {} with interval: {}", begin, end, symbol, interval);

        int numRecordsMined = 0;

        // delete any existing data for this given period from mongodb
        repository.deleteWithinTime(symbol,
                interval,
                begin.toInstant(ZoneOffset.UTC).toEpochMilli(),
                end.toInstant(ZoneOffset.UTC).toEpochMilli());

        // Based on the max chunk size to request from binance; and the period requested;
        // create time bins to download each

        LocalDateTime currentBegin = begin.minusDays(0);

        // TODO should convert this to minutes; instead of days
        int daysAtATime = Double.valueOf(Math.floor(CHUNK_MAX / interval.chunksPerDay())).intValue();

        LocalDateTime currentEnd = null;

        while (currentBegin.isBefore(end))
        {
            currentEnd = currentBegin.plusDays(daysAtATime);

            if (currentEnd.isAfter(end))
                currentEnd = end.plusDays(0);

            List<CandleItem> items = extractCandles(currentBegin, currentEnd, symbol, interval);

            if (items.size() > 0)
            {
                repository.saveAll(items);

                numRecordsMined += items.size();
            }

            try
            {
                Thread.sleep(50L);
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }

            currentBegin = currentBegin.plusDays(daysAtATime);
        }


        return numRecordsMined;
    }

    public List<CandleItem> extractCandles(LocalDateTime begin, LocalDateTime end, Symbol symbol, Interval interval)
    {
        log.info("Extract candles: {} to {} symbol {} interval {}", begin, end, symbol, interval);

        RestTemplate rt = new RestTemplate();

        // Build the URI for RestAPI request
        // Follow Postman request

        URI url = UriComponentsBuilder.fromHttpUrl(config.getCandleUrlPrefix() + config.getCandleUrl())
                .queryParam(config.getCandleUrlQueryStartTime(), begin.toEpochSecond(ZoneOffset.UTC) * 1000)
                .queryParam(config.getCandleUrlQuerySymbol(), symbol.getCode())
                .queryParam(config.getCandleUrlQueryInterval(), interval.getCode())
                .queryParam(config.getCandleUrlQueryLimit(), CHUNK_MAX)
                .queryParam(config.getCandleUrlQueryEndTime(), end.toEpochSecond(ZoneOffset.UTC) * 1000)
                .build().toUri();

        log.info("Url: {}", url);

        RequestEntity<Object> requestEntity = new RequestEntity<>(HttpMethod.GET, url);


        ResponseEntity<List<List<Object>>> exchange = rt.exchange(requestEntity,
                new ParameterizedTypeReference<>()
                {
                });

        List<List<Object>> response = exchange.getBody();

        if (response == null) {
            log.warn("No response from service!");
            return new ArrayList<>();
        }

        List<CandleItem> collect = response.stream()
                .map(l -> CandleItem.fromArray(l, symbol, interval))
                .collect(Collectors.toList());

        log.info("items extracted: {}", collect.size());

        return collect;
    }


}
