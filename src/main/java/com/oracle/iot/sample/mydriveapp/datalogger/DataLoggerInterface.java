package com.oracle.iot.sample.mydriveapp.datalogger;

public interface DataLoggerInterface
{
    void setHeaders(Iterable<String> headers) throws IllegalStateException;
    void addRow(Iterable<String> values) throws IllegalStateException;
    void writeToFile();
}
