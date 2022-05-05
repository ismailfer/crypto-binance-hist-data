package com.ismail.binance.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerPing
{
    private boolean success;

    private long sendTime;

    private long responseTime;

    @Override
    public String toString()
    {
        return "ServerPing{" +
                "success=" + success +
                " time-taken=" + (responseTime - sendTime) + "ms" +
                '}';
    }
}
