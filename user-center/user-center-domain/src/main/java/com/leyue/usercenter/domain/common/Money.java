package com.leyue.usercenter.domain.common;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象
 * 
 * @author Claude Code
 */
@Value
public class Money {
    
    private static final int DEFAULT_SCALE = 2;
    
    BigDecimal amount;
    
    private Money(BigDecimal amount) {
        this.amount = amount.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }
    
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return new Money(amount);
    }
    
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }
    
    public static Money of(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount string cannot be null or empty");
        }
        return new Money(new BigDecimal(amount));
    }
    
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier));
    }
    
    public Money divide(BigDecimal divisor) {
        return new Money(this.amount.divide(divisor, DEFAULT_SCALE, RoundingMode.HALF_UP));
    }
    
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean greaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean greaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public boolean lessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean lessThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Money money = (Money) obj;
        return Objects.equals(amount, money.amount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return amount.toString();
    }
}