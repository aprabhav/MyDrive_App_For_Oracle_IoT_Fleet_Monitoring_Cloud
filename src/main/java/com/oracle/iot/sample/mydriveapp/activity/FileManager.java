package com.oracle.iot.sample.mydriveapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class FileManager {
    private final Activity activity;
    //File path = new File("/");
    File sdPath = Environment.getExternalStorageDirectory();
    File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final String DOWNLOAD = "Downloads: ";
    public static final String SDCARD = "SDCard: ";
    AlertDialog fileSelectionDialog;
    // filter on file extension
    private String extension = null;

    public FileManager(Activity activity) {
        this.activity = activity;
        buildDialog();
    }

    private void buildDialog(){
        final String[] fileList = getFileList();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (fileList == null || fileList.length == 0){
            builder  = builder.setMessage("No provisioning assets found. Please insert an SD Card with provisioning assets and try again.");
            builder = builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked Try again button
                    fileSelectionDialog.dismiss();
                    buildDialog();
                    showDialog();
                }
            });
            builder = builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    fileSelectionDialog.dismiss();
                }
            });
        }else{
            builder  = builder.setTitle("Please select a file");
            builder = builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });
            builder = builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            builder = builder.setItems(fileList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    File chosenFile;
                    if(fileList[which].startsWith(DOWNLOAD))
                        chosenFile = new File(downloadPath,fileList[which].substring(fileList[which].lastIndexOf(" ")+1));
                    else
                        chosenFile = new File(sdPath, fileList[which].substring(fileList[which].lastIndexOf(" ")+1));


                    if (mFileListener != null) {
                        mFileListener.fileSelected(chosenFile);
                    }
                    fileSelectionDialog.dismiss();
                }
            });
        }

        fileSelectionDialog = builder.create();

    }

    public void showDialog() {
        fileSelectionDialog.show();
    }

    String[] getFileList(){

        File[] files = sdPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (!file.isDirectory()) {
                    if (!file.canRead()) {
                        return false;
                    } else if (extension == null) {
                        return true;
                    } else {
                        return file.getName().toLowerCase(Locale.ROOT).endsWith(extension);
                    }
                } else {
                    return false;
                }
            }
        });
        File[] files1 = downloadPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (!file.isDirectory()) {
                    if (!file.canRead()) {
                        return false;
                    } else if (extension == null) {
                        return true;
                    } else {
                        return file.getName().toLowerCase(Locale.ROOT).endsWith(extension);
                    }
                } else {
                    return false;
                }
            }
        });
        int totalFilesCount = 0;
        if((files == null || files.length == 0) && (files1 == null || files1.length == 0)){
            // handle error here
            return null;
        }
        if (files != null)
            totalFilesCount += files.length;
        if(files1 != null)
            totalFilesCount += files1.length;
        String[] fileList = new String[totalFilesCount];
        int j=0;
        for(int i=0; files != null && i < files.length; i++,j++){
            fileList[j] = SDCARD+files[i].getName();
        }

        for (int i=0;files1 != null && i < files1.length;i++,j++){
            fileList[j] = DOWNLOAD+files1[i].getName();
        }
        return fileList;
    }

    public static void copyFile(File source, File dest) {
        InputStream input = null;
        OutputStream output = null;
        try {

            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            input.close();
            output.close();
        }catch(Exception e){

        }
    }

    // file selection event handling
    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public FileManager setFileListener(FileSelectedListener fileListener) {
        this.mFileListener = fileListener;
        return this;
    }

    private FileSelectedListener mFileListener;
}
