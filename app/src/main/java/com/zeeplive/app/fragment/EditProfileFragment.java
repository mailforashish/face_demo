package com.zeeplive.app.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.zeeplive.app.R;
import com.zeeplive.app.SpannableGridLayoutManager;
import com.zeeplive.app.activity.EditProfile;
import com.zeeplive.app.activity.ImagePickerActivity;
import com.zeeplive.app.adapter.ProfilePicsAdapter;
import com.zeeplive.app.databinding.ActivityEditProfileBinding;
import com.zeeplive.app.databinding.FragmentEditProfileBinding;
import com.zeeplive.app.dialog.EditProfileBottomSheet;
import com.zeeplive.app.response.ProfileDetailsResponse;
import com.zeeplive.app.response.UserListResponse;
import com.zeeplive.app.retrofit.ApiManager;
import com.zeeplive.app.retrofit.ApiResponseInterface;
import com.zeeplive.app.utils.Constant;
import com.zeeplive.app.utils.DateCallback;
import com.zeeplive.app.utils.DateFormatter;
import com.zeeplive.app.utils.Datepicker;
import com.zeeplive.app.utils.RecyclerTouchListener;

import java.io.File;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;


public class EditProfileFragment extends Fragment implements ApiResponseInterface {

    FragmentEditProfileBinding binding;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 609;
    List<UserListResponse.UserPics> imageList;
    ApiManager apiManager;
    boolean is_album = false;
    ProfilePicsAdapter profilePicsAdapter;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setClickListener(new EventHandler(getContext()));

        SpannableGridLayoutManager gridLayoutManager = new SpannableGridLayoutManager(new SpannableGridLayoutManager.GridSpanLookup() {
            @Override
            public SpannableGridLayoutManager.SpanInfo getSpanInfo(int position) {
                if (position == 0) {
                    return new SpannableGridLayoutManager.SpanInfo(2, 2);
                    //this will count of row and column you want to replace
                } else {
                    return new SpannableGridLayoutManager.SpanInfo(1, 1);
                }
            }
        }, 3, 1f); // 3 is the number of coloumn , how nay to display is 1f
        binding.pictureRecyclerview.setLayoutManager(gridLayoutManager);


        apiManager = new ApiManager(getContext(), this);
        apiManager.getProfileDetails();


        binding.stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                binding.stateTextview.setText(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.pictureRecyclerview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), binding.pictureRecyclerview, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.e("picUrl", imageList.get(position).getImage_name());
                if (position == 0) {
                    is_album = false;
                    pickImage();
                    return;
                }
                if (imageList.get(position).getImage_name().equals("add_pic")) {
                    if (position == 0) {
                        is_album = false;
                    } else {
                        is_album = true;
                    }
                    pickImage();
                    return;
                }
                if (!imageList.get(position).getImage_name().equals("")) {
                    Log.e("I am here","in if condition"+position);
                    new EditProfileBottomSheet(getContext(), imageList, position);
                } else if (position == 0) {
                    Log.e("I am here", "in else if condition" + position);
                    is_album = false;
                    pickImage();
                } else {
                    Log.e("I am here", "in else  condition" + position);
                    is_album = true;
                    pickImage();

                }
            }
            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    public void pickImage() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent intent = new Intent(getContext(), ImagePickerActivity.class);
                        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);
                        // setting aspect ratio
                        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
                        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 3); // 16x9, 1x1, 3:4, 3:2
                        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 4);
                        startActivityForResult(intent, PICK_IMAGE_GALLERY_REQUEST_CODE);


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                // Uri selectedImageUri = data.getData();
                Uri selectedImageUri = data.getParcelableExtra("path");
                Log.e("selectimg", "selectedImageUri==" + selectedImageUri);
                String picturePath = selectedImageUri.getPath();
                //loadProfile(selectedImageUri.toString());
                Log.e("picture", picturePath);

                if (!picturePath.equals("Not found")) {
                    File picture = new File(picturePath);
                    // Compress Image
                    File file = new Compressor(getContext()).compressToFile(picture);
                    Log.e("selectedImageEdit", "selectedImageEdit:" + file);
                    Log.e("is_album", "is_album:" + is_album);
                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    //RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                    MultipartBody.Part picToUpload = MultipartBody.Part.createFormData("profile_pic[]", file.getName(), requestBody);

                    //Create request body with text description and text media type
                    //RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
                    //RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), "image-type");

                    apiManager.updateProfileDetails("", "", "", "", picToUpload, is_album);
                } else {
                    Toast.makeText(getContext(), "File not found", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e("picturewee", e.toString());
                Toast.makeText(getContext(), "Please select another image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    public void refreshData() {
        apiManager.getProfileDetails();
    }


    public class EventHandler {
        Context mContext;

        public EventHandler(Context mContext) {
            this.mContext = mContext;
        }

        public void dob() {

            new Datepicker().selectDateFrom(mContext, binding.birthday, "", new DateCallback() {
                @Override
                public void onDateGot(String date, long timeStamp) {
                    binding.birthday.setText(DateFormatter.getInstance().format(date));
                }
            });
        }

        public void updateData() {
            apiManager.updateProfileDetails(binding.userName.getText().toString(), binding.stateTextview.getText().toString(),
                    binding.birthday.getText().toString(),
                    binding.aboutUser.getText().toString(), null,false);
        }

        public void onBack() {
            FragmentManager fm = getFragmentManager();
            fm.popBackStackImmediate();
           // onBackPressed();
        }
    }


    @Override
    public void isError(String errorCode) {
        Toast.makeText(getContext(), errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void isSuccess(Object response, int ServiceCode) {
        if (ServiceCode == Constant.PROFILE_DETAILS) {

            ProfileDetailsResponse rsp = (ProfileDetailsResponse) response;
            imageList = rsp.getSuccess().getProfile_images();
            Log.e("imgAlbum", new Gson().toJson(rsp.getSuccess().getProfile_images()));
            Log.e("imgAlbumsize", String.valueOf(imageList.size()));

            //Allow add pic if less than 6
            if (imageList.size() <= 5) {
                for (int i = imageList.size(); i < 6; i++) {
                    Log.e("addPlusisData", String.valueOf(imageList.size()));
                    UserListResponse.UserPics pic = new UserListResponse.UserPics();
                    pic.setImage_name("add_pic");
                    imageList.add(pic);
                }
            }
            //imageListPoster = new ArrayList<>();
            //imageListPoster.add(rsp);
            // Log.e("profileImallgesNEW", new Gson().toJson(imageListPoster));
            Log.e("profileImallgesNEW", "ArraySize " + imageList.size());
            Log.e("profileImallgesNEW", "ArraySizeData " + new Gson().toJson(imageList));


            Log.e("profileImallgesNEW", "ArraySizeImage " + imageList.size());
            profilePicsAdapter = new ProfilePicsAdapter(getContext(), imageList);
            binding.pictureRecyclerview.setAdapter(profilePicsAdapter);
            profilePicsAdapter.notifyDataSetChanged();
            //binding.pictureRecyclerview.setAdapter(new ProfilePicsAdapter(this, imageList));

            binding.userName.setText(rsp.getSuccess().getName());
            binding.stateTextview.setText(rsp.getSuccess().getCity());
            binding.birthday.setText(rsp.getSuccess().getDob());
            binding.aboutUser.setText(rsp.getSuccess().getAbout_user());

        }
        if (ServiceCode == Constant.UPDATE_PROFILE) {
            apiManager.getProfileDetails();
        }
    }

}