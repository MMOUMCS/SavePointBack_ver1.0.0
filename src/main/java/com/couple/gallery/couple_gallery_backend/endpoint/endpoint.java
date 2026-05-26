package com.couple.gallery.couple_gallery_backend.endpoint;

public class endpoint {
    public static final String API_BASE_URL = "/api";

    public static final String USER_BASE = API_BASE_URL + "/users";
    public static final String USER_REGISTER = "/register"; // 상대 경로
    public static final String USER_LOGIN = "/login";       // 상대 경로

    public static final String COUPLE_BASE = API_BASE_URL + "/couples";
    public static final String COUPLE_CONNECT = "/connect";

    private endpoint() {}
}
