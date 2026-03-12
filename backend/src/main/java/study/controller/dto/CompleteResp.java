package study.controller.dto;

public class CompleteResp {
    private String redirect;

    public CompleteResp() {}

    public CompleteResp(String redirect) {
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
