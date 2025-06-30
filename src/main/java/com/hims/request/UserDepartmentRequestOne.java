package com.hims.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UserDepartmentRequestOne {
    private long userId;
    private List<Department> departments;

    @Getter
    @Setter
    public static class Department {
        private long departmentId;
        private String status;
    }
}
