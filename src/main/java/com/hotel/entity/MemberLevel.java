package com.hotel.entity;

/**
 * 会员等级枚举类
 */
public enum MemberLevel {
    REGULAR("普通用户", 1.0),
    BRONZE("铜牌会员", 0.98),
    SILVER("银牌会员", 0.95),
    GOLD("金牌会员", 0.90),
    DIAMOND("钻石会员", 0.85);

    private final String displayName;
    private final double discount;

    MemberLevel(String displayName, double discount) {
        this.displayName = displayName;
        this.discount = discount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDiscount() {
        return discount;
    }

    /**
     * 根据累计消费金额获取对应的会员等级
     */
    public static MemberLevel getByTotalSpent(int amount) {
        if (amount >= 30000) {
            return DIAMOND;
        } else if (amount >= 10000) {
            return GOLD;
        } else if (amount >= 5000) {
            return SILVER;
        } else if (amount >= 1500) {
            return BRONZE;
        } else {
            return REGULAR;
        }
    }

    /**
     * 根据显示名称获取会员等级
     */
    public static MemberLevel getByDisplayName(String displayName) {
        for (MemberLevel level : MemberLevel.values()) {
            if (level.displayName.equals(displayName)) {
                return level;
            }
        }
        return REGULAR;
    }
} 