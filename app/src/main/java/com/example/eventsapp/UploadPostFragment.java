package com.example.eventsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;


public class UploadPostFragment extends Fragment {

    EditText titleText,locationText,deadlineText,descriptionText;
    ImageView imageView;
    Bitmap choosenImage;
    Button btnUpload,btnCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_upload_post, container, false);

        titleText = view.findViewById(R.id.upload_title);
        locationText = view.findViewById(R.id.upload_location);
        deadlineText = view.findViewById(R.id.upload_deadline);
        descriptionText = view.findViewById(R.id.upload_description);
        imageView = view.findViewById(R.id.upload_imageView);
        btnUpload = view.findViewById(R.id.upload_save);
        btnCancel = view.findViewById(R.id.upload_cancel);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,2);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventListFragment eventListFragment = new EventListFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fts = fragmentManager.beginTransaction();
                fts.replace(R.id.container,eventListFragment);
                fts.addToBackStack(null);
                fts.commit();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleText.getText().toString();
                String location = locationText.getText().toString();
                String description = descriptionText.getText().toString();
                String deadline = deadlineText.getText().toString();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                choosenImage.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();

                ParseFile parseFile = new ParseFile("image.png",bytes);

                ParseObject parseObject = new ParseObject("Posts");
                parseObject.put("image",parseFile);
                parseObject.put("title",title);
                parseObject.put("location",location);
                parseObject.put("description",description);
                parseObject.put("deadline",deadline);
                parseObject.put("username", ParseUser.getCurrentUser().getUsername());
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null){
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getActivity(), "Post Uploaded!", Toast.LENGTH_LONG).show();
                            EventListFragment eventListFragment = new EventListFragment();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction fts = fragmentManager.beginTransaction();
                            fts.replace(R.id.container,eventListFragment);
                            fts.addToBackStack(null);
                            fts.commit();
                        }
                    }
                });
            }
        });


        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults.length >0){
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 & resultCode == getActivity().RESULT_OK & data != null){
            Uri uri = data.getData();

            try{
                if (Build.VERSION.SDK_INT >= 28){
                    ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(),uri);
                    choosenImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(choosenImage);
                }else{
                    choosenImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uri);
                    imageView.setImageBitmap(choosenImage);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
