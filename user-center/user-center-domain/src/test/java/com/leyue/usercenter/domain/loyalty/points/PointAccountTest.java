package com.leyue.usercenter.domain.loyalty.points;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("积分账户领域模型测试")
class PointAccountTest {
    
    private PointAccount pointAccount;
    private static final String USER_ID = "user123";
    
    @BeforeEach
    void setUp() {
        pointAccount = new PointAccount(USER_ID);
    }
    
    @Nested
    @DisplayName("积分赚取测试")
    class EarnPointsTests {
        
        @Test
        @DisplayName("应该能够正常赚取积分")
        void shouldEarnPointsSuccessfully() {
            // Given
            int pointsToEarn = 100;
            String reason = "ORDER_COMPLETED";
            String sourceId = "order123";
            
            // When
            pointAccount.earn(pointsToEarn, reason, sourceId);
            
            // Then
            assertEquals(100, pointAccount.getAvailablePoints());
            assertEquals(100, pointAccount.getTotalPoints());
            assertEquals(0, pointAccount.getFrozenPoints());
            assertEquals(1, pointAccount.getVersion());
        }
        
        @Test
        @DisplayName("赚取0积分应该抛出异常")
        void shouldThrowExceptionWhenEarningZeroPoints() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointAccount.earn(0, "TEST", "test123")
            );
            assertEquals("积分数量必须大于0", exception.getMessage());
        }
        
        @Test
        @DisplayName("赚取负积分应该抛出异常")
        void shouldThrowExceptionWhenEarningNegativePoints() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointAccount.earn(-10, "TEST", "test123")
            );
            assertEquals("积分数量必须大于0", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("积分扣减测试")
    class DeductPointsTests {
        
        @BeforeEach
        void setUpPoints() {
            pointAccount.earn(200, "SETUP", "setup123");
        }
        
        @Test
        @DisplayName("应该能够正常扣减积分")
        void shouldDeductPointsSuccessfully() {
            // Given
            int pointsToDeduct = 50;
            String reason = "REDEMPTION";
            String sourceId = "redemption123";
            
            // When
            pointAccount.deduct(pointsToDeduct, reason, sourceId);
            
            // Then
            assertEquals(150, pointAccount.getAvailablePoints());
            assertEquals(200, pointAccount.getTotalPoints());
            assertEquals(0, pointAccount.getFrozenPoints());
        }
        
        @Test
        @DisplayName("扣减积分超过可用积分应该抛出异常")
        void shouldThrowExceptionWhenDeductingMoreThanAvailable() {
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> pointAccount.deduct(300, "TEST", "test123")
            );
            assertEquals("可用积分不足", exception.getMessage());
        }
        
        @Test
        @DisplayName("扣减0积分应该抛出异常")
        void shouldThrowExceptionWhenDeductingZeroPoints() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointAccount.deduct(0, "TEST", "test123")
            );
            assertEquals("积分数量必须大于0", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("积分冻结测试")
    class FreezePointsTests {
        
        @BeforeEach
        void setUpPoints() {
            pointAccount.earn(200, "SETUP", "setup123");
        }
        
        @Test
        @DisplayName("应该能够正常冻结积分")
        void shouldFreezePointsSuccessfully() {
            // Given
            int pointsToFreeze = 50;
            String reason = "PENDING_ORDER";
            String sourceId = "order123";
            
            // When
            pointAccount.freeze(pointsToFreeze, reason, sourceId);
            
            // Then
            assertEquals(150, pointAccount.getAvailablePoints());
            assertEquals(200, pointAccount.getTotalPoints());
            assertEquals(50, pointAccount.getFrozenPoints());
        }
        
        @Test
        @DisplayName("冻结积分超过可用积分应该抛出异常")
        void shouldThrowExceptionWhenFreezingMoreThanAvailable() {
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> pointAccount.freeze(300, "TEST", "test123")
            );
            assertEquals("可用积分不足，无法冻结", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("积分解冻测试")
    class UnfreezePointsTests {
        
        @BeforeEach
        void setUpFrozenPoints() {
            pointAccount.earn(200, "SETUP", "setup123");
            pointAccount.freeze(100, "TEST_FREEZE", "freeze123");
        }
        
        @Test
        @DisplayName("应该能够正常解冻积分")
        void shouldUnfreezePointsSuccessfully() {
            // Given
            int pointsToUnfreeze = 50;
            String reason = "ORDER_CONFIRMED";
            String sourceId = "order123";
            
            // When
            pointAccount.unfreeze(pointsToUnfreeze, reason, sourceId);
            
            // Then
            assertEquals(150, pointAccount.getAvailablePoints());
            assertEquals(200, pointAccount.getTotalPoints());
            assertEquals(50, pointAccount.getFrozenPoints());
        }
        
        @Test
        @DisplayName("解冻积分超过冻结积分应该抛出异常")
        void shouldThrowExceptionWhenUnfreezingMoreThanFrozen() {
            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> pointAccount.unfreeze(150, "TEST", "test123")
            );
            assertEquals("冻结积分不足，无法解冻", exception.getMessage());
        }
    }
    
    @Test
    @DisplayName("应该正确计算积分余额")
    void shouldCalculateBalanceCorrectly() {
        // Given
        pointAccount.earn(300, "EARN", "earn123");
        pointAccount.deduct(50, "DEDUCT", "deduct123");
        pointAccount.freeze(100, "FREEZE", "freeze123");
        
        // Then
        assertEquals(150, pointAccount.getAvailablePoints()); // 300 - 50 - 100
        assertEquals(300, pointAccount.getTotalPoints()); // 总积分不变
        assertEquals(100, pointAccount.getFrozenPoints());
    }
    
    @Test
    @DisplayName("新创建的积分账户应该有正确的初始状态")
    void shouldHaveCorrectInitialState() {
        // Then
        assertEquals(USER_ID, pointAccount.getUserId());
        assertEquals(0, pointAccount.getAvailablePoints());
        assertEquals(0, pointAccount.getTotalPoints());
        assertEquals(0, pointAccount.getFrozenPoints());
        assertEquals(0, pointAccount.getVersion());
        assertNotNull(pointAccount.getCreatedAt());
        assertNotNull(pointAccount.getUpdatedAt());
    }
}