package com.beiwei.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class testProperties {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            InputStream resourceAsStream = testProperties.class.getClassLoader().getResourceAsStream("applicationContext.properties");
            properties.load(resourceAsStream);
            System.out.println(properties.getProperty("scanPackage"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
