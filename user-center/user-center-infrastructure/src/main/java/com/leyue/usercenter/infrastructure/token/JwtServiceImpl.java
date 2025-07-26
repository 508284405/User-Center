package com.leyue.usercenter.infrastructure.token;

import com.leyue.usercenter.domain.token.JtiGenerator;
import com.leyue.usercenter.domain.token.JwtService;
import com.leyue.usercenter.dto.MenuDTO;
import com.leyue.usercenter.dto.TokenDTO;
import com.leyue.usercenter.dto.UserDTO;
import com.leyue.usercenter.infrastructure.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT 服务实现类
 *
 * @author Trae
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    
    /**
     * Redis 中 Token 黑名单的前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    /**
     * Redis 中 Refresh Token 的前缀
     */
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    
    private final JwtConfig jwtConfig;
    private final RedissonClient redissonClient;
    private final JtiGenerator jtiGenerator;
    
    /**
     * 生成签名密钥（私钥）
     *
     * @return RSA私钥
     */
    private PrivateKey getSigningKey() {
        try {
            String privateKeyPEM = jwtConfig.getPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("无法加载RSA私钥", e);
        }
    }
    
    /**
     * 获取验签公钥
     *
     * @return RSA公钥
     */
    private PublicKey getPublicKey() {
        try {
            String publicKeyPEM = jwtConfig.getPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("无法加载RSA公钥", e);
        }
    }
    
    /**
     * 生成 Token（包含 Access Token 和 Refresh Token）
     *
     * @param userDTO 用户信息
     * @return Token 信息
     */
    @Override
    public TokenDTO generateToken(UserDTO userDTO) {
        // 生成 Access Token
        long currentTimeMillis = System.currentTimeMillis();
        Date accessTokenExpirationDate = new Date(currentTimeMillis + jwtConfig.getAccessTokenExpiration() * 60 * 1000);
        
        // 生成唯一的 JTI
        String jti = jtiGenerator.generateJti();
        
        // 构建 Access Token 的 Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDTO.getId());
        claims.put("username", userDTO.getUsername());
        
        // 如果有角色和权限，也添加到 Claims 中
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            claims.put("roles", userDTO.getRoles());
            List<MenuDTO> menuDTOS = userDTO.getRoles().stream().filter(role -> role.getMenus() != null && !role.getMenus().isEmpty())
                    .flatMap(role -> {
                        return role.getMenus().stream();
                    }).collect(Collectors.toList());
            claims.put("menus", menuDTOS);
        }
        
        // 生成 Access Token
        String accessToken = Jwts.builder()
                .claims(claims)
                .subject(userDTO.getId().toString())
                .issuedAt(new Date(currentTimeMillis))
                .expiration(accessTokenExpirationDate)
                .id(jti)
                .issuer(jwtConfig.getIssuer())
                .signWith(getSigningKey())
                .compact();
        
        // 生成 Refresh Token
        Date refreshTokenExpirationDate = new Date(currentTimeMillis + jwtConfig.getRefreshTokenExpiration() * 24 * 60 * 60 * 1000);
        
        // Refresh Token 只包含最基本的信息
        String refreshToken = Jwts.builder()
                .subject(userDTO.getId().toString())
                .issuedAt(new Date(currentTimeMillis))
                .expiration(refreshTokenExpirationDate)
                .id(UUID.randomUUID().toString())
                .issuer(jwtConfig.getIssuer())
                .signWith(getSigningKey())
                .compact();
        
        // 将 Refresh Token 存储到 Redis 中，用于后续验证和撤销
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userDTO.getId();
        RBucket<String> refreshTokenBucket = redissonClient.getBucket(refreshTokenKey);
        refreshTokenBucket.set(refreshToken, jwtConfig.getRefreshTokenExpiration(), TimeUnit.DAYS);
        
        // 构建并返回 TokenDTO
        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpirationDate.getTime())
                .refreshTokenExpiresAt(refreshTokenExpirationDate.getTime())
                .tokenType("Bearer")
                .build();
    }
    
    /**
     * 验证 Access Token
     *
     * @param token Access Token
     * @return 如果 Token 有效，返回 true；否则返回 false
     */
    @Override
    public boolean validateAccessToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        
        try {
            // 解析 Token
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                    .build()
                    .parseSignedClaims(token);
            
            // 获取 JTI
            String jti = claimsJws.getPayload().getId();
            
            // 检查 Token 是否在黑名单中
            return !isInBlacklist(jti);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("无法验证 JWT Token", e);
            // Token 无效
            return false;
        }
    }
    
    /**
     * 验证 Refresh Token
     *
     * @param token Refresh Token
     * @return 如果 Token 有效，返回 true；否则返回 false
     */
    @Override
    public boolean validateRefreshToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        
        try {
            // 解析 Token
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                    .build()
                    .parseSignedClaims(token);
            
            // 获取用户 ID
            String userId = claimsJws.getPayload().getSubject();
            
            // 从 Redis 中获取存储的 Refresh Token
            String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
            RBucket<String> refreshTokenBucket = redissonClient.getBucket(refreshTokenKey);
            String storedToken = refreshTokenBucket.get();
            
            // 检查 Token 是否与存储的一致
            return token.equals(storedToken);
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效
            return false;
        }
    }
    
    /**
     * 从 Access Token 中提取用户 ID
     *
     * @param token Access Token
     * @return 用户 ID
     */
    @Override
    public Long getUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 从 Access Token 中提取 JTI（JWT ID）
     *
     * @param token Access Token
     * @return JTI
     */
    @Override
    public String getJtiFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getId();
    }
    
    /**
     * 将 Access Token 加入黑名单
     *
     * @param token Access Token
     */
    @Override
    public void addToBlacklist(String token) {
        try {
            // 解析 Token
            Claims claims = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 获取 JTI 和过期时间
            String jti = claims.getId();
            Date expiration = claims.getExpiration();
            
            // 计算剩余有效时间（毫秒）
            long ttl = expiration.getTime() - System.currentTimeMillis();
            
            // 如果 Token 已经过期，则不需要加入黑名单
            if (ttl <= 0) {
                return;
            }
            
            // 将 JTI 加入黑名单，过期时间与 Token 一致
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
            RBucket<String> blacklistBucket = redissonClient.getBucket(blacklistKey);
            blacklistBucket.set("1", ttl, TimeUnit.MILLISECONDS);
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效，忽略
        }
    }
    
    /**
     * 检查 Access Token 是否在黑名单中
     *
     * @param jti JWT ID
     * @return 如果在黑名单中，返回 true；否则返回 false
     */
    @Override
    public boolean isInBlacklist(String jti) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + jti;
        RBucket<String> blacklistBucket = redissonClient.getBucket(blacklistKey);
        return blacklistBucket.isExists();
    }
    
    /**
     * 使用 Refresh Token 刷新 Access Token
     *
     * @param refreshToken Refresh Token
     * @return 新的 Token 信息
     */
    @Override
    public TokenDTO refreshToken(String refreshToken) {
        // 验证 Refresh Token
        if (!validateRefreshToken(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }
        
        try {
            // 解析 Refresh Token
            Claims claims = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .setAllowedClockSkewSeconds(jwtConfig.getClockSkew())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            
            // 获取用户 ID
            Long userId = Long.parseLong(claims.getSubject());
            
            // 构建简单的 UserDTO 用于生成新的 Token
            UserDTO userDTO = UserDTO.builder()
                    .id(userId)
                    .build();
            
            // 生成新的 Token
            TokenDTO newToken = generateToken(userDTO);
            
            // 返回新的 Token
            return newToken;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Failed to refresh token", e);
        }
    }
}