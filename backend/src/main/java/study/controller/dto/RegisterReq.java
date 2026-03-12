package study.controller.dto;

public class RegisterReq {
    private String token;

    public RegisterReq() {}

    public RegisterReq(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
