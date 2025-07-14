package com.example.carcare;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AICamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AICamFragment extends Fragment {

    private ImageView cameraImage;

    private TextInputEditText resultText;

    private String currentPhotoPath;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    private RelativeLayout mainLayout;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public AICamFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AICamFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AICamFragment newInstance(String param1, String param2) {
        AICamFragment fragment = new AICamFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a_i_cam, container, false);
        mainLayout = view.findViewById(R.id.main);
        cameraImage = view.findViewById(R.id.cameraImage);
        Button captureImgBtn = view.findViewById(R.id.capture_button);
        resultText = view.findViewById(R.id.input_text);
        Button copyTextBtn = view.findViewById(R.id.copy_button);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission is granted
                        try {
                            captureImage();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // Permission is denied
                        Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePictureLauncher  = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success ->{
                    if(success){
                        if(currentPhotoPath != null){
                            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                            ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(currentPhotoPath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            int orientation = exif.getAttributeInt(
                                    ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED
                            );
                            Bitmap rotatedBitmap = null;
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;
                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }
                            cameraImage.setImageBitmap(rotatedBitmap);
                            recognizeText(rotatedBitmap);
                        }
                    }
                }
        );

        captureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        copyTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(
                                requireContext().getApplicationContext(),
                                ClipboardManager.class
                        );

                ClipData clip = ClipData.newPlainText("recognized text", resultText.getText());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(
                            requireContext().getApplicationContext(),
                            "Text Copied",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        initNoFocusInputTextWhenNoKeyboard();

        return view;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true
        );
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void captureImage() throws IOException {
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    photoFile
            );
            takePictureLauncher.launch(photoUri);
        }
    }

    private void recognizeText(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap,180);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text ocrText) {
                        resultText.setText(ocrText.getText());
                        resultText.setMovementMethod(new ScrollingMovementMethod());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                requireContext(),
                                "Failed to recognize text",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void initNoFocusInputTextWhenNoKeyboard(){
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeightDiff = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mainLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mainLayout.getRootView().getHeight();

                int heightDiff = screenHeight - r.height();

                // Dacă diferența e mare, înseamnă că tastatura e afișată
                // Dacă diferența scade, tastatura s-a ascuns
                if (previousHeightDiff > heightDiff) {
                    // Tastatura tocmai s-a închis
                    // Scoatem focusul și ascundem tastatura (de precauție)
                    if (resultText.hasFocus()) {
                        resultText.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(resultText.getWindowToken(), 0);
                        }
                    }
                }
                previousHeightDiff = heightDiff;
            }
        });
    }
}