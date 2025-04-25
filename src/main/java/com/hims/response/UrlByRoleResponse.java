package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UrlByRoleResponse {
    private String parentName;
    private String parentUrl;
    private List<ChildUrl> children;

    @Getter
    @Setter
    public static class ChildUrl {
        private String chiledName;
        private String chiledUrl;
    }
}
