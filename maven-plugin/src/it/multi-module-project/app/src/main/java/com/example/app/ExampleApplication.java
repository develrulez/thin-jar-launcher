package com.example.app;

import com.example.dataaccess.ExampleDataAccess;
import com.example.service.ExampleService;

public class ExampleApplication {

    public static void main(String[] args) {
        System.out.println(new ExampleService(new ExampleDataAccess()).getMesssage());
    }
}
