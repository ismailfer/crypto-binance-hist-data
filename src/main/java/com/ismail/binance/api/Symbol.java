package com.ismail.binance.api;

import lombok.Getter;

@Getter
public enum Symbol
{
    BTCUSDT("BTCUSDT"),
    BTCBUSD("BTCBUSD"),

    ETHUSDT("ETHUSDT"),
    ETHBUSD("ETHBUSD"),

    LTCUSDT("LTCUSDT"),
    LTCBUSD("LTCBUSD");

    private String code;

    Symbol(String code)
    {
        this.code = code;
    }
}
