package com.ismail.binance.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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


    public ServerPing getServerPing()
    {
        URI url = UriComponentsBuilder.fromHttpUrl(config.getUrlPrefix() + config.getServerPingUrl())
                .build().toUri();

        ServerPing ping = ServerPing.builder()
                .success(false)
                .sendTime(System.currentTimeMillis())
                .build();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        ping.setResponseTime(System.currentTimeMillis());
        ping.setSuccess(resp.getStatusCode() == HttpStatus.OK);

        return ping;
    }


    public ServerTime getServerTime()
    {
        URI url = UriComponentsBuilder.fromHttpUrl(config.getUrlPrefix() + config.getServerTimeUrl())
                .build().toUri();

        RestTemplate restTemplate = new RestTemplate();

        ServerTime serverTime = restTemplate.getForObject(url, ServerTime.class);

        return serverTime;
    }


    public BookTicker getBookTicker(Symbol symbol)
    {
        log.info("getBookTicker: {}", symbol);


        // Build the URI for RestAPI request
        // Follow Postman request

        URI url = UriComponentsBuilder.fromHttpUrl(config.getUrlPrefix() + config.getBookTickerUrl())
                .queryParam(config.getBookTickerQuerySymbol(), symbol.getCode())
                .build().toUri();

        log.info("getBookTicker Url: {}", url);

        BookTicker bt = null;

        RestTemplate restTemplate = new RestTemplate();

        // ----------------------------------------------------------------------------------------
        // automatically map response to a POJO
        // ----------------------------------------------------------------------------------------
        //bt = restTemplate.getForObject(url, BookTicker.class);

        // ----------------------------------------------------------------------------------------
        // automatically map response to a POJO
        // ResponseEntity has headers that might interest us; like status code; error messages etc
        // ----------------------------------------------------------------------------------------
        ResponseEntity<BookTicker> response = restTemplate.getForEntity(url, BookTicker.class);
        bt = response.getBody();

        List<String> mbxUsedWeightList = response.getHeaders().get("X-MBX-USED-WEIGHT");
        List<String> mbxUsedWeight1mList = response.getHeaders().get("X-MBX-USED-WEIGHT-1m");

        System.out.println("--- X-MBX-USED-WEIGHT: " + mbxUsedWeightList + ", X-MBX-USED-WEIGHT-1m: " + mbxUsedWeight1mList);
        /*
        // ----------------------------------------------------------------------------------------
        // manually parse the response using json reader
        // ----------------------------------------------------------------------------------------
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);


        if (response.getStatusCode() == HttpStatus.OK)
        {
            log.info("Book ticker: {} ", response.getBody());

            try
            {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                bt = BookTicker.builder()
                        .symbol(Symbol.valueOf(root.get("symbol").asText()))
                        .bidPrice(root.get("bidPrice").asDouble())
                        .bidQty(root.get("bidQty").asDouble())
                        .askPrice(root.get("askPrice").asDouble())
                        .askQty(root.get("askQty").asDouble())
                        .build();


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

         */


        return bt;
    }


    /**
     * Downloads the data from binance server
     * divide period requested into smaller periods compatible with binance rules
     * max request records by binance is 1000.
     *
     * @param begin
     * @param end
     * @param symbol
     * @param interval
     * @return number of records mined
     */
    public int getHistDataAndSaveToDB(Symbol symbol, Interval interval, LocalDateTime begin, LocalDateTime end, boolean pSaveToDB)
    {
        log.info("Mine data for: {} to: {} for {} with interval: {}", begin, end, symbol, interval);

        int numRecordsMined = 0;

        log.info("open {} epoch millis {}", begin, begin.toInstant(ZoneOffset.UTC).toEpochMilli());

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
        int minutesPerChunk = interval.getMinutes() * config.getChunkMax();

        LocalDateTime currentEnd = null;

        while (currentBegin.isBefore(end))
        {
            currentEnd = currentBegin.plusMinutes(minutesPerChunk);

            if (currentEnd.isAfter(end))
                currentEnd = end.plusMinutes(0);

            List<Candle> items = getHistDataSingleChunk(symbol, interval, currentBegin.toEpochSecond(ZoneOffset.UTC) * 1000L, currentEnd.toEpochSecond(ZoneOffset.UTC) * 1000L);

            // print for debugging purposes
            items.forEach(e -> {
                System.out.println(e.simpleToString());
            });


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
                .queryParam(config.getKlinesQueryStartTime(), begin)
                .queryParam(config.getKlinesQuerySymbol(), symbol.getCode())
                .queryParam(config.getKlinesQueryInterval(), interval.getCode())
                .queryParam(config.getKlinesQueryLimit(), config.getChunkMax())
                .queryParam(config.getKlinesQueryEndTime(), end)
                .build().toUri();

        log.info("Url: {}", url);

        RequestEntity<Object> requestEntity = new RequestEntity<>(HttpMethod.GET, url);


        ResponseEntity<List<List<Object>>> exchange = rt.exchange(requestEntity,
                new ParameterizedTypeReference<>()
                {
                });

        List<List<Object>> response = exchange.getBody();

        if (response == null)
        {
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
