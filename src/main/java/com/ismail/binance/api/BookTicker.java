package com.ismail.binance.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents top of book ticker
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTicker
{
    private Symbol symbol;

    private double bidQty;

    private double bidPrice;

    private double askPrice;

    private double askQty;

    public String toStringSimple()
    {
        return "BookTicker{" +
                "symbol=" + symbol +
                ", bidQty=" + bidQty +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                ", askQty=" + askQty +
                ", spread=" + (askPrice-bidPrice) +
                ", spreadBps=" + (10000.0 * (askPrice-bidPrice)/bidPrice) +
                '}';
    }
}
