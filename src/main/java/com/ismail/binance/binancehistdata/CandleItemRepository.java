package com.ismail.binance.binancehistdata;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CandleItemRepository extends MongoRepository<CandleItem, String>
{
    void deleteBySymbolAndInterval(Symbol symbol, Interval interval);

    @Query(
            value = "{'symbol': {$eq: ?0}, 'interval': {$eq: ?1}, 'openTime': {$gte: ?2}, 'openTime': {$lt: ?3} }",
            delete = true
    )
    void deleteWithinTime(Symbol symbol, Interval interval, long toEpochMilli, long toEpochMilli1);
}
