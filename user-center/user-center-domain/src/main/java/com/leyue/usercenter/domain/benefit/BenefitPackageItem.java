package com.leyue.usercenter.domain.benefit;

import lombok.Getter;

import java.util.Objects;

/**
 * 权益包明细实体
 * 
 * @author Claude Code
 */
@Getter
public class BenefitPackageItem {
    
    private final Long id;
    private final Long pkgId;
    private final String benefitCode;
    private final Integer quota;
    private final BenefitPeriod period;
    private final Integer priority;
    
    public BenefitPackageItem(Long id, Long pkgId, String benefitCode, Integer quota, 
                             BenefitPeriod period, Integer priority) {
        this.id = id;
        this.pkgId = Objects.requireNonNull(pkgId, "Package ID cannot be null");
        this.benefitCode = Objects.requireNonNull(benefitCode, "Benefit code cannot be null");
        this.quota = quota != null ? quota : 1;
        this.period = period != null ? period : BenefitPeriod.INFINITE;
        this.priority = priority != null ? priority : 0;
        
        if (this.quota <= 0) {
            throw new IllegalArgumentException("Benefit quota must be positive");
        }
    }
    
    /**
     * 创建权益包明细
     */
    public static BenefitPackageItem create(Long pkgId, String benefitCode, Integer quota, 
                                           BenefitPeriod period, Integer priority) {
        return new BenefitPackageItem(null, pkgId, benefitCode, quota, period, priority);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BenefitPackageItem that = (BenefitPackageItem) obj;
        return Objects.equals(pkgId, that.pkgId) && Objects.equals(benefitCode, that.benefitCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pkgId, benefitCode);
    }
}