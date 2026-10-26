
package com.katza.myfirebaseapplication;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class Base64ImageActivity extends AppCompatActivity {

    private ActivityResultLauncher<Void> cameraPreviewLauncher;
    private ActivityResultLauncher<String> galleryImageLauncher;
    private Bitmap selectedBitmap;
    private ImageView ivBase64Image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base64_image);
        setContentView(R.layout.activity_base64_image);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivBase64Image = findViewById(R.id.ivBase64Image);

        cameraPreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                new ActivityResultCallback<Bitmap>() {
                    @Override
                    public void onActivityResult(Bitmap bitmap) {
                        if (bitmap == null) {
                            showToastMessage("הצילום בוטל");
                            return;
                        }
                        selectedBitmap = bitmap;
                        ivBase64Image.setImageBitmap(bitmap);
                    }
                });

        findViewById(R.id.btnTakeBase64Photo)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cameraPreviewLauncher.launch(null);
                    }
                });

        galleryImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri == null) {
                            showToastMessage("לא נבחרה תמונה");
                            return;
                        }
                        try {
                            Bitmap bitmap = ImageUtils.convertUriToBitmap(
                                    Base64ImageActivity.this, uri);
                            if (bitmap == null) {
                                showToastMessage("לא ניתן לטעון את התמונה");
                                return;
                            }
                            selectedBitmap = bitmap;
                            ivBase64Image.setImageBitmap(bitmap);
                        } catch (IOException | RuntimeException error) {
                            showToastMessage("שגיאה: " + error.getMessage());
                        }
                    }
                });
    }

    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
