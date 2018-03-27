package com.photon.photonchain.storage.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @Author:PTN
 * @Description:
 * @Date:19:10 2018/1/30
 * @Modified by:
 */
@Entity
public class Token {
    @Id
    private String name;
    private String symbol;
    private String icon;
    private int decimals;
    public Token() {
    }

    public Token(String symbol, String name, String icon, int decimals) {
        this.symbol = symbol;
        this.name = name;
        this.icon = icon;
        this.decimals = decimals;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (decimals != token.decimals) return false;
        if (symbol != null ? !symbol.equals(token.symbol) : token.symbol != null) return false;
        if (name != null ? !name.equals(token.name) : token.name != null) return false;
        return icon != null ? icon.equals(token.icon) : token.icon == null;
    }

    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + decimals;
        return result;
    }

    @Override
    public String toString() {
        return "Token{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", decimals=" + decimals +
                '}';
    }
}
