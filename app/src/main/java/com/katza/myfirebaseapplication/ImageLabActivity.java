
package com.katza.myfirebaseapplication;

import android.os.Bundle;
import android.net.Uri;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.view.Menu;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ImageView;
import android.graphics.Bitmap;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class ImageLabActivity extends AppCompatActivity {

    private Button btnSource;
    private ImageView imagePreview;

    private Uri cameraUri;
    private Uri selectedUri = null;

    private boolean sampleDrawable = false;
    private boolean selectedFromCamera = false;

    private Bitmap selectedBitmap;

    private ActivityResultLauncher<String[]> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private FirestoreImageDAO firestoreImageDAO;

    private String firestoreImageFileName = "myImage";

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

        firestoreImageDAO = new FirestoreImageDAO();

        if (savedInstanceState != null) {
            String savedFileName =
                    savedInstanceState.getString(
                            "firestoreImageFileName");

            if (savedFileName != null) {
                firestoreImageFileName = savedFileName;
            }
        }

        // בחירת תמונה מהגלריה
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {

                    if (uri != null) {

                        selectedUri = uri;
                        selectedFromCamera = false;
                        sampleDrawable = false;

                        try {

                            Bitmap bitmap =
                                    ImageUtils.convertUriToBitmap(
                                            ImageLabActivity.this,
                                            uri);

                            if (bitmap == null) {
                                status("לא ניתן לטעון את התמונה");
                                return;
                            }

                            selectedBitmap = bitmap;

                            imagePreview.setImageBitmap(bitmap);

                            // שמירה אוטומטית ב-Firestore
                            saveBitmapAsBase64ToFirestore(bitmap);

                        } catch (IOException | RuntimeException error) {

                            status("שגיאה: " + error.getMessage());
                        }
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

                        try {

                            Bitmap bitmap =
                                    ImageUtils.convertUriToBitmap(
                                            ImageLabActivity.this,
                                            cameraUri);

                            if (bitmap == null) {
                                status("לא ניתן לטעון את התמונה");
                                return;
                            }

                            selectedBitmap = bitmap;

                            imagePreview.setImageBitmap(bitmap);

                            // שמירה אוטומטית ב-Firestore
                            saveBitmapAsBase64ToFirestore(bitmap);

                        } catch (IOException | RuntimeException error) {

                            status("שגיאה: " + error.getMessage());
                        }

                    } else {
                        status("הצילום בוטל או נכשל");
                    }
                });

        btnSource.setOnClickListener(
                v -> showSourceMenu());
    }

    private void showSourceMenu() {

        PopupMenu menu = new PopupMenu(
                this, findViewById(R.id.btnSource));

        menu.getMenu().add(
                Menu.NONE, 1, 1, "בחר מהגלריה");

        menu.getMenu().add(
                Menu.NONE, 2, 2, "צלם במצלמה");

        // אפשרות לטעינת תמונה קיימת
        menu.getMenu().add(
                Menu.NONE, 3, 3, "טען תמונה מ-Firestore");

        menu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == 1) {

                galleryLauncher.launch(
                        new String[]{"image/*"});

                return true;
            }

            else if (item.getItemId() == 2) {

                try {

                    cameraUri = createGalleryItem(
                            "camera_"
                                    + System.currentTimeMillis()
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

                readFromFirestore();

                return true;
            }

            return false;
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

    // שמירת Bitmap כ-Base64 ב-Firestore
    private void saveBitmapAsBase64ToFirestore(Bitmap bitmap) {

        if (bitmap == null) {

            status("צלמו או בחרו תמונה תחילה");
            return;
        }

        String base64 =
                ImageUtils.convertBitmapToBase64(bitmap);

        if (base64 == null) {

            status("לא ניתן להמיר את התמונה ל-Base64");
            return;
        }

        firestoreImageDAO.saveToFirestore(
                firestoreImageFileName,
                base64,
                new IFirestoreImageCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {

                        status("התמונה נשמרה ב-Firestore");
                    }

                    @Override
                    public void onFailure(Exception error) {

                        status("שמירה נכשלה: "
                                + error.getMessage());
                    }
                });
    }

    // שליפת תמונה מ-Firestore והצגתה
    private void readFromFirestore() {

        firestoreImageDAO.readFromFirestore(
                firestoreImageFileName,
                new IFirestoreImageCallback<String>() {

                    @Override
                    public void onSuccess(String base64) {

                        Bitmap bitmap =
                                ImageUtils.convertBase64ToBitmap(
                                        base64);

                        if (bitmap == null) {

                            status("לא ניתן לפענח את התמונה");
                            return;
                        }

                        selectedBitmap = bitmap;

                        imagePreview.setImageBitmap(bitmap);

                        status("התמונה נטענה מ-Firestore");
                    }

                    @Override
                    public void onFailure(Exception error) {

                        status("קריאה נכשלה: "
                                + error.getMessage());
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(
                "firestoreImageFileName",
                firestoreImageFileName);

        super.onSaveInstanceState(outState);
    }

    private void status(String message) {

        Toast.makeText(
                ImageLabActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
