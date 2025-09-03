package com.leyue.usercenter.domain.benefit;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 权益包聚合根
 * 
 * @author Claude Code
 */
@Getter
public class BenefitPackage {
    
    private final Long id;
    private final Long pkgId;
    private final String name;
    private boolean enabled;
    private List<BenefitPackageItem> items;
    private LocalDateTime updatedAt;
    
    public BenefitPackage(Long id, Long pkgId, String name, boolean enabled,
                         List<BenefitPackageItem> items, LocalDateTime updatedAt) {
        this.id = id;
        this.pkgId = Objects.requireNonNull(pkgId, "Package ID cannot be null");
        this.name = Objects.requireNonNull(name, "Package name cannot be null");
        this.enabled = enabled;
        this.items = items;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
    
    /**
     * 创建权益包
     */
    public static BenefitPackage create(Long pkgId, String name, List<BenefitPackageItem> items) {
        return new BenefitPackage(null, pkgId, name, true, items, null);
    }
    
    /**
     * 启用权益包
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 禁用权益包
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新权益包明细
     */
    public void updateItems(List<BenefitPackageItem> newItems) {
        this.items = newItems;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BenefitPackage that = (BenefitPackage) obj;
        return Objects.equals(pkgId, that.pkgId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pkgId);
    }
}