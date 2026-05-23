package com.example.app.dto.response;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
    private Long expiresIn;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, UserResponse user, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public static class LoginResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private UserResponse user;
        private Long expiresIn;

        public LoginResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public LoginResponseBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public LoginResponseBuilder user(UserResponse user) { this.user = user; return this; }
        public LoginResponseBuilder expiresIn(Long expiresIn) { this.expiresIn = expiresIn; return this; }

        public LoginResponse build() {
            return new LoginResponse(accessToken, refreshToken, user, expiresIn);
        }
    }
}
