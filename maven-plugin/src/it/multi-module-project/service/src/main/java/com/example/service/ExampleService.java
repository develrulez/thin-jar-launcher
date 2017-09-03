package com.example.service;

import com.example.dataaccess.ExampleDataAccess;
import org.apache.commons.lang3.StringUtils;

public class ExampleService {

    private final ExampleDataAccess exampleDataAccess;

    public ExampleService(ExampleDataAccess exampleDataAccess) {
        this.exampleDataAccess = exampleDataAccess;
    }

    public String getMesssage(){
        return StringUtils.reverse(exampleDataAccess.getMessage());
    }
}
