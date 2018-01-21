package com.example.michiel.mobapp_michielvanbergen;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScannerActivity extends AppCompatActivity
        implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        scannerView.setResultHandler(this);

        setContentView(scannerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(ScannerActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scannerView.startCamera();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            scannerView.startCamera();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permission[], int grantResults[]) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        scannerView.startCamera();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(-1);
        finish();
        scannerView.stopCameraPreview();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        String contactKey = result.getText();
        setResult(0, new Intent().putExtra("contactKey", contactKey));
        finish();
        scannerView.stopCameraPreview();
        scannerView.stopCamera();
    }

}
