package com.leyue.usercenter.infrastructure.config.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Google OAuth2 配置属性
 */
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class GoogleOAuth2Properties {
    
    private Registration registration = new Registration();
    private Provider provider = new Provider();
    private String redirectUri = "http://localhost:8080/oauth2/callback";
    
    public static class Registration {
        private Google google = new Google();
        
        public Google getGoogle() {
            return google;
        }
        
        public void setGoogle(Google google) {
            this.google = google;
        }
        
        public static class Google {
            private String clientId;
            private List<String> scope;
            
            public String getClientId() {
                return clientId;
            }
            
            public void setClientId(String clientId) {
                this.clientId = clientId;
            }
            
            public List<String> getScope() {
                return scope;
            }
            
            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }
    }
    
    public static class Provider {
        private Google google = new Google();
        
        public Google getGoogle() {
            return google;
        }
        
        public void setGoogle(Google google) {
            this.google = google;
        }
        
        public static class Google {
            private String authorizationUri;
            
            public String getAuthorizationUri() {
                return authorizationUri;
            }
            
            public void setAuthorizationUri(String authorizationUri) {
                this.authorizationUri = authorizationUri;
            }
        }
    }
    
    public Registration getRegistration() {
        return registration;
    }
    
    public void setRegistration(Registration registration) {
        this.registration = registration;
    }
    
    public Provider getProvider() {
        return provider;
    }
    
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
