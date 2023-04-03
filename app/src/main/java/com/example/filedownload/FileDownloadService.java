package com.example.filedownload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileDownloadService extends Service {
    public static final String FileDownloadNotificationAction =
            "file_download_notification";
    public static final String FileUrlParamName = "file_url";
    public static final String FileAbsoluteLocalPathParamName = "file_path";

    private static final int CorePoolSize = 1;
    private static final int MaxThreadPoolSize = 3;
    private static final long KeepAliveTime = 1;
    private static final TimeUnit KeepAliveTimeUnit = TimeUnit.MINUTES;

    private ThreadPoolExecutor file_download_executor;

    public FileDownloadService(){
        file_download_executor = new ThreadPoolExecutor(CorePoolSize,
                MaxThreadPoolSize, KeepAliveTime, KeepAliveTimeUnit,
                new LinkedBlockingDeque<>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String file_url = intent.getStringExtra(FileUrlParamName);
        String file_path =
                intent.getStringExtra(FileAbsoluteLocalPathParamName);

        StartFileDownloadAsync(file_url, file_path);
        return START_STICKY;
    }

    private void StartFileDownloadAsync(String file_url,
                                        String file_absolute_path){
        file_download_executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Downloading file async: " + file_absolute_path);
                StartFileDownloadSync(file_url, file_absolute_path);
            }
        });
    }

    private void StartFileDownloadSync(String file_url,
                                       String file_absolute_path){
        System.out.println("Downloading file sync: " + file_absolute_path);
        Boolean download_succeeded = false;
        try{
            URL url = null;
            try {
                url = new URL(file_url);
            } catch (Exception e){
                System.out.println("Invalid file url: " + e.toString());
                throw new Exception();
            }

            File output_file = new File(file_absolute_path);
            if(!output_file.exists()){
                try {
                    output_file.createNewFile();
                }catch (Exception e){
                    System.out.println("Exception in file create: " + e.toString());
                    throw new Exception();
                }
            }

            try{
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input_stream =
                        new BufferedInputStream(url.openStream(), 8192);
                OutputStream output_stream =
                        new FileOutputStream(output_file.getAbsoluteFile());

                byte data[] = new byte[1024];
                int total_bytes = 0;
                int current_bytes = 0;
                while((current_bytes = input_stream.read(data)) != -1){
                    total_bytes += current_bytes;
                    output_stream.write(data, 0, current_bytes);
                }

                output_stream.flush();
                output_stream.close();
                input_stream.close();
                download_succeeded = true;
            }catch (Exception e){
                System.out.println("Exception in file download: " + e.toString());
                throw new Exception();
            }
        }catch (Exception e){
            download_succeeded = false;
        }finally {
            onFileDownload(file_url, download_succeeded);
        }
    }

    protected void onFileDownload(String file_name, Boolean download_status){
        Intent intent = new Intent(FileDownloadNotificationAction);
        intent.putExtra("file", file_name);
        intent.putExtra("download_status", download_status);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}