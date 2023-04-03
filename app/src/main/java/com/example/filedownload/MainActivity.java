package com.example.filedownload;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ensure file manage permission first
        EnsureExternalStorageManagerPermission();

        // Initiate view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set receiver for file download events
        registerReceiver(file_download_notification_receiver,
                new IntentFilter(FileDownloadService.FileDownloadNotificationAction));
    }

    public void startDownload(View view){
        Toast.makeText(getApplicationContext(), "Starting downloads.",
                Toast.LENGTH_SHORT).show();

        EditText pdf1EditText, pdf2EditText, pdf3EditText, pdf4EditText, pdf5EditText;
        pdf1EditText = (EditText) findViewById(R.id.pdf1EditText);
        pdf2EditText = (EditText) findViewById(R.id.pdf2EditText);
        pdf3EditText = (EditText) findViewById(R.id.pdf3EditText);
        pdf4EditText = (EditText) findViewById(R.id.pdf4EditText);
        pdf5EditText = (EditText) findViewById(R.id.pdf5EditText);

        // Download file 1
        downloadFile(pdf1EditText.getText().toString());
        // Download file 2
        downloadFile(pdf2EditText.getText().toString());
        // Download file 3
        downloadFile(pdf3EditText.getText().toString());
        // Download file 4
        downloadFile(pdf4EditText.getText().toString());
        // Download file 5
        downloadFile(pdf5EditText.getText().toString());
    }

    private String getFileNameFromUrl(String url){
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private void downloadFile(String url){
        if(url.isEmpty()) return;
        String file_absolute_path =
                getDownloadDir() + "/" + getFileNameFromUrl(url);
        startDownloadService(url, file_absolute_path);
    }

    private void startDownloadService(String file_url, String file_path){
        Intent intent = new Intent(getApplicationContext(),
                FileDownloadService.class);
        intent.putExtra(FileDownloadService.FileUrlParamName, file_url);
        intent.putExtra(FileDownloadService.FileAbsoluteLocalPathParamName,
                file_path);
        startService(intent);
    }

    private static String getDownloadDir(){
        String downloadDirPath = "";
        if(downloadDirPath.isEmpty()){
            downloadDirPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        }
        return downloadDirPath;
    }

    private void EnsureExternalStorageManagerPermission() {
        if(!Environment.isExternalStorageManager()){
            Intent intent =
                    new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private BroadcastReceiver file_download_notification_receiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String file_name = getFileNameFromUrl(bundle.getString("file"));
                Boolean download_status = bundle.getBoolean("download_status");
                String download_status_string = download_status ?
                        "succeeded" : "failed";
                String toast_msg =
                        "Download " + download_status_string + " for " + file_name;
                Toast.makeText(getApplicationContext(), toast_msg,
                        Toast.LENGTH_LONG).show();
            }
        }
    };
}