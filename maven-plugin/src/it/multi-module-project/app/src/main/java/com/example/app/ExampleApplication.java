package com.example.app;

import com.example.service.ExampleService;
import com.example.dataaccess.ExampleDataAccess;

public class ExampleApplication {

    public static void main(String[] args) {
        System.out.println(new ExampleService(new ExampleDataAccess()).getMesssage());
    }
}
