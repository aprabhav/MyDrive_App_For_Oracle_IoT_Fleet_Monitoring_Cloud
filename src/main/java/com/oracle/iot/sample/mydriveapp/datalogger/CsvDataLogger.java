package com.oracle.iot.sample.mydriveapp.datalogger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvDataLogger implements DataLoggerInterface
{
    private CSVPrinter csv;
    private FileWriter fileWriter;
    private boolean headersSet;
    private File file;
    private Context context;

    public CsvDataLogger(Context context, File file)
    {
        this.context = context;
        this.file = file;

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.getProperty("line.separator"));

        try
        {
            fileWriter = new FileWriter(file);
            csv = new CSVPrinter(fileWriter, csvFileFormat);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        headersSet = false;
    }

    public void setHeaders(Iterable<String> headers) throws IllegalStateException
    {
        if(!headersSet)
        {
            try
            {
                csv.printRecord(headers);
                headersSet = true;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            throw new IllegalStateException("Headers already exist!");
        }
    }

    public void addRow(Iterable<String> values) throws IllegalStateException
    {
        if(headersSet)
        {
            try
            {
                csv.printRecord(values);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            throw new IllegalStateException("Headers do not exist!");
        }
    }

    public void writeToFile()
    {
        try {
            fileWriter.flush();
            fileWriter.close();
            csv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }
}
