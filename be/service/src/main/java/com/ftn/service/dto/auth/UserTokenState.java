package com.ftn.service.dto.auth;

public class UserTokenState {

    private String access_token;
    private Long expires_in;
    private String authority;

    public UserTokenState() {
        this.access_token = null;
        this.expires_in = null;
        this.authority = null;
    }

    public UserTokenState(String access_token, long expires_in) {
        this.access_token = access_token;
        this.expires_in = expires_in;
    }

    public UserTokenState(String access_token, long expires_in, String authority) {
        this.access_token = access_token;
        this.expires_in = expires_in;
        this.authority = authority;
    }

    public String getAccess_token() { return access_token; }
    public void setAccess_token(String access_token) { this.access_token = access_token; }

    public Long getExpires_in() { return expires_in; }
    public void setExpires_in(Long expires_in) { this.expires_in = expires_in; }

    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
}
