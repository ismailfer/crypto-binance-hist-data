package com.ismail.binance.binancehistdata;

import lombok.Getter;

@Getter
public enum Symbol
{
    BTCUSDT("BTCUSDT"),

    LTCUSDT("LTCUSDT");

    private String code;

    Symbol(String code)
    {
        this.code = code;
    }
}
