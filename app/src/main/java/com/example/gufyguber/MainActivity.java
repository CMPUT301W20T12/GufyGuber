package com.example.gufyguber;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainActivity extends AppCompatActivity {
    Button generate;
    BitMatrix matrix;
    Bitmap map;
    String codeMessage;
    ImageView qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        generate = findViewById(R.id.generate);
        qrCode = findViewById(R.id.qrCode);

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeMessage = "Trial!!!";
                MultiFormatWriter multi = new MultiFormatWriter();
                try {
                    matrix = multi.encode(codeMessage, BarcodeFormat.QR_CODE, 200, 200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    map = barcodeEncoder.createBitmap(matrix);
                    qrCode.setImageBitmap(map);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
