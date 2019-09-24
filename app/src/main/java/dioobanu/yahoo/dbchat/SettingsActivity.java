package dioobanu.yahoo.dbchat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    //android layout
    private CircleImageView mDisplayImage;
    private TextView mNama;
    private TextView mStatus;
    private Button mStatusBtn;
    private Button mImageBtn;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private static final int GALLERY_PICK = 1;

    //referensi storage untuk foto profile ke profile_images di firebase
    private StorageReference mProfileImagesStorage;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = findViewById(R.id.settings_image);
        mNama = findViewById(R.id.settings_nama);
        mStatus = findViewById(R.id.settings_status);
        mStatusBtn = findViewById(R.id.settings_change_status_button);
        mImageBtn = findViewById(R.id.settings_change_image_button);

        mProfileImagesStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"ByteOrderMark", "ResourceType"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String nama = dataSnapshot.child("nama").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mNama.setText(nama);
                mStatus.setText(status);

                if(!image.equals("default")) {
                    //picasso untuk menampilkan image yg diupload ke dalam display image di profil user
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.defaultavatar).into(mDisplayImage);
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.defaultavatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.defaultavatar).into(mDisplayImage);
                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();

                Intent mStatusBtn = new Intent(SettingsActivity.this, StatusActivity.class);
                mStatusBtn.putExtra("status_value", status_value);
                startActivity(mStatusBtn);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK)
        {

                Uri imageUri = data.getData();
                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500,500)
                        .start(this);

                //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Mengupload Gambar...");
                mProgressDialog.setMessage("Mohon tunggu selagi gambar sedang diupload.");
                mProgressDialog.setCanceledOnTouchOutside(false); //user tidak bisa membatalkan progress dialog
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath()); // membuat url image result uri menjadi bentuk File bernama thumb_filePath

                String current_user_id = mCurrentUser.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this) // masukkan thumb_filePath ke dalam thumb_bitmap yg nantinya akan dikompres
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = mProfileImagesStorage.child("profile_images").child(current_user_id + ".jpg"); //petunjuk tempat penyimpanan/directory file di firebase
                final StorageReference thumb_filepath = mProfileImagesStorage.child("profile_images").child("thumb").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful())
                        {
                            final String[] download_url = {task.getResult().getStorage().getDownloadUrl().toString()};

                            UploadTask uploadtask = thumb_filepath.putBytes(thumb_byte);
                            uploadtask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> Thumb_task) {

                                    final String[] thumb_downloadurl = {Thumb_task.getResult().getStorage().getDownloadUrl().toString()};

                                    if (Thumb_task.isSuccessful())
                                    {

                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                        {
                                            @Override
                                            public void onSuccess(Uri uri)
                                            {
                                                download_url[0] =uri.toString();
                                                mUserDatabase.child("image").setValue(download_url[0]);
                                            }
                                        });

                                        thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                        {
                                            @Override
                                            public void onSuccess(Uri uri)
                                            {
                                                thumb_downloadurl[0] =uri.toString();
                                                mUserDatabase.child("thumb_image").setValue(thumb_downloadurl[0]);
                                            }
                                        });

                            /*mUserDatabase.child("image").setValue(download_url[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {*/

                                        //if (task.isSuccessful())
                                        //{
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Berhasil Upload Gambar", Toast.LENGTH_LONG).show();

                                        //}
                                        // }
                                        //});
                                    }else{

                                        Toast.makeText(SettingsActivity.this, "Error Upload Thumbnail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }
                        else {

                            Toast.makeText(SettingsActivity.this, "Error Upload Gambar", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }
    }