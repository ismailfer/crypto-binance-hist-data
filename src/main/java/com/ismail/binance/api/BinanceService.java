package com.ismail.binance.api;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceService
{
    private final BinanceConfig config;

    private final CandleRepository repository;

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
    public int getHistDataAndSaveToDB(Symbol symbol, Interval interval, LocalDateTime begin, LocalDateTime end, boolean pSaveToDB)
    {
        log.info("Mine data for: {} to: {} for {} with interval: {}", begin, end, symbol, interval);

        int numRecordsMined = 0;

        log.info("open {} epoch millis {}", begin , begin.toInstant(ZoneOffset.UTC).toEpochMilli());

        // delete any existing data for this given period from mongodb
        if (pSaveToDB)
        {
            repository.deleteWithinTime(symbol,
                    interval,
                    begin.toInstant(ZoneOffset.UTC).toEpochMilli(),
                    end.toInstant(ZoneOffset.UTC).toEpochMilli());
        }

        // Based on the max chunk size to request from binance; and the period requested;
        // create time bins to download each

        // always truncate to base timeframe
        LocalDateTime currentBegin = begin.truncatedTo(ChronoUnit.MINUTES);
        //LocalDateTime currentBegin = begin.minusMinutes(0);

        // TODO should convert this to minutes; instead of days
        int minutesPerChunk = interval.getMinutes() * CHUNK_MAX;

        LocalDateTime currentEnd = null;

        while (currentBegin.isBefore(end))
        {
            currentEnd = currentBegin.plusMinutes(minutesPerChunk);

            if (currentEnd.isAfter(end))
                currentEnd = end.plusMinutes(0);

            List<Candle> items = getHistDataSingleChunk(symbol, interval, currentBegin.toEpochSecond(ZoneOffset.UTC) * 1000L , currentEnd.toEpochSecond(ZoneOffset.UTC) * 1000L);

            // print for debugging purposes
             items.forEach(e -> {System.out.println(e.simpleToString()); });


            if (items.size() > 0)
            {
                if (pSaveToDB)
                {
                    repository.saveAll(items);
                }

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

            currentBegin = currentBegin.plusMinutes(minutesPerChunk);
        }


        return numRecordsMined;
    }

    /**
     * Request historical data with one chunk (max is 1000 records)
     * this method should be called from this service only
     *
     * @param begin
     * @param end
     * @param symbol
     * @param interval
     * @return
     */
    public List<Candle> getHistDataSingleChunk(Symbol symbol, Interval interval, long begin, long end)
    {
        log.info("Extract candles: {} to {} symbol {} interval {}", begin, end, symbol, interval);

        RestTemplate rt = new RestTemplate();

        // Build the URI for RestAPI request
        // Follow Postman request

        URI url = UriComponentsBuilder.fromHttpUrl(config.getUrlPrefix() + config.getKlinesUrl())
                .queryParam(config.getKlinesUrlQueryStartTime(), begin)
                .queryParam(config.getKlinesUrlQuerySymbol(), symbol.getCode())
                .queryParam(config.getKlinesUrlQueryInterval(), interval.getCode())
                .queryParam(config.getKlinesUrlQueryLimit(), CHUNK_MAX)
                .queryParam(config.getKlinesUrlQueryEndTime(), end)
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

        List<Candle> collect = response.stream()
                .map(l -> Candle.fromArray(l, symbol, interval))
                .collect(Collectors.toList());

        log.info("items extracted: {}", collect.size());

        return collect;
    }


}
