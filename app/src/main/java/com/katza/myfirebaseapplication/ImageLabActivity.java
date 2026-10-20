
package com.katza.myfirebaseapplication;

import android.os.Bundle;
import android.net.Uri;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ImageLabActivity extends AppCompatActivity {

    private Button btnSource;
    private ImageView imagePreview;

    private Uri cameraUri;
    private Uri selectedUri = null;

    private boolean sampleDrawable = false;
    private boolean selectedFromCamera = false;

    private ActivityResultLauncher<String[]> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_lab);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);
                    return insets;
                });

        btnSource = findViewById(R.id.btnSource);
        imagePreview = findViewById(R.id.imagePreview);

        // בחירת תמונה מהגלריה
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        selectedUri = uri;
                        selectedFromCamera = false;
                        sampleDrawable = false;

                        imagePreview.setImageURI(uri);
                        status("התמונה נבחרה מהגלריה");
                    }
                });

        // צילום תמונה במצלמה
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraUri != null) {
                        selectedUri = cameraUri;
                        selectedFromCamera = true;
                        sampleDrawable = false;

                        imagePreview.setImageURI(cameraUri);
                        status("התמונה צולמה בהצלחה");
                    } else {
                        status("הצילום בוטל או נכשל");
                    }
                });

        btnSource.setOnClickListener(v -> showSourceMenu());
    }

    private void showSourceMenu() {
        PopupMenu menu = new PopupMenu(
                this, findViewById(R.id.btnSource));

        menu.getMenu().add(Menu.NONE, 1, 1, "בחר מהגלריה");
        menu.getMenu().add(Menu.NONE, 2, 2, "צלם במצלמה");
        menu.getMenu().add(Menu.NONE, 3, 3, "תמונת Drawable לדוגמה");

        menu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == 1) {
                            galleryLauncher.launch(
                                    new String[]{"image/*"});
                            return true;
                        }

                        else if (item.getItemId() == 2) {
                            try {
                                cameraUri = createGalleryItem(
                                        "camera_" +
                                                System.currentTimeMillis()
                                                + ".jpg",
                                        "image/jpeg");

                                if (cameraUri == null) {
                                    status("לא ניתן ליצור קובץ לתמונה");
                                    return true;
                                }

                                cameraLauncher.launch(cameraUri);

                            } catch (Exception e) {
                                status("לא ניתן לפתוח מצלמה: "
                                        + e.getMessage());
                            }

                            return true;
                        }

                        else if (item.getItemId() == 3) {
                            sampleDrawable = true;
                            selectedUri = null;
                            selectedFromCamera = false;

                            imagePreview.setImageResource(
                                    R.drawable.sample_image);

                            status("נבחרה תמונת Drawable לדוגמה");

                            return true;
                        }

                        return false;
                    }
                });

        menu.show();
    }

    // יצירת יעד לשמירת תמונה בגלריה
    private Uri createGalleryItem(
            String fileName, String mimeType) {

        ContentValues values = new ContentValues();

        values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                fileName);

        values.put(
                MediaStore.Images.Media.MIME_TYPE,
                mimeType);

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.Q) {

            values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/MyFirebaseApplication");
        }

        return getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
    }

    private void status(String message) {
        Toast.makeText(
                ImageLabActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}