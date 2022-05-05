package com.ismail.binance.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerTime
{
    public long serverTime;

    @Override
    public String toString()
    {
        return "ServerTime{" +
                "serverTime=" + serverTime +
                " DiffToUs=" + (serverTime - Instant.now().toEpochMilli()) + "ms" +
                '}';
    }
}
