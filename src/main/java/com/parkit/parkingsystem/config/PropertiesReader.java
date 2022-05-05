package com.parkit.parkingsystem.config;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    private Properties dbProperties;

    public PropertiesReader(String propertyFileName) throws IOException {
        InputStream readerOfProperty = getClass().getClassLoader()
                .getResourceAsStream(propertyFileName);
        this.dbProperties = new Properties();
        this.dbProperties.load(readerOfProperty);
    }

    public String getProperty(String propertyName) {
        return this.dbProperties.getProperty(propertyName);
    }
}
