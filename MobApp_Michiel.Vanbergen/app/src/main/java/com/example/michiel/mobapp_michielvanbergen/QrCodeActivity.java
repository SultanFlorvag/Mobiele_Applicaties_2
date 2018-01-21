package com.example.michiel.mobapp_michielvanbergen;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;

public class QrCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences savedValues;

    private String UID;

    private ImageView QrCodeImageView;

    private Button BackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        QrCodeImageView = (ImageView) this.findViewById(R.id.QrCodeImageView);
        BackButton = (Button) this.findViewById(R.id.BackButton);

        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
        UID = savedValues.getString("UID", "");

        Bitmap myQrCode = QRCode.from(UID).withColor(0xFFFFFFFF, 0xFF01579b).bitmap();
        QrCodeImageView.setImageBitmap(myQrCode);

        BackButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
