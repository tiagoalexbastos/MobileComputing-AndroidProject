package ua.cm.tiagoalexbastos.imgreader.gallery.fragments;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


import ua.cm.tiagoalexbastos.imgreader.ImageUtils.Imagem;
import ua.cm.tiagoalexbastos.imgreader.MainActivity;
import ua.cm.tiagoalexbastos.imgreader.R;
import ua.cm.tiagoalexbastos.imgreader.gallery.adapter.GalleryAdapter;
import ua.cm.tiagoalexbastos.imgreader.ImageUtils.Image;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "TAG";
    private ArrayList<Image> images;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private ArrayList<byte[]> images_bitmaps;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private ArrayList<String> image_paths;
    private GalleryAdapter mAdapter;
    @SuppressWarnings("unused")
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference database;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public GalleryFragment() {
        // Required empty public constructor
    }


    @SuppressWarnings("UnusedAssignment")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gallery, container,
                false);

        images = new ArrayList<>();
        images_bitmaps = new ArrayList<>();
        image_paths = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mAdapter = new GalleryAdapter(getContext(), images);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        FirebaseDatabase fire_database = FirebaseDatabase.getInstance();
        database = FirebaseDatabase.getInstance().getReference();


        if (!isNetworkAvailable()) {
            File myDir = getActivity().getFilesDir();
            String imgs = "images";
            File imageFolder = new File(myDir, imgs);
            File[] files = imageFolder.listFiles();
            if (!imageFolder.exists())
                Snackbar.make(rootView, "No images in local storage! Connect to internet first", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            else {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        Image _imagem = new Image();
                        _imagem.setMedium(file.getAbsolutePath());
                        _imagem.setLarge(file.getAbsolutePath());
                        images.add(_imagem);
                        images_bitmaps.add(readImageBytes(file.getAbsolutePath()));
                        image_paths.add(file.getAbsolutePath());
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        } else {
            fetchDataFromStorage();
        }




        database.child("imagens").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                cleanImageFolder();

                sendNotification();

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Imagem img = noteDataSnapshot.getValue(Imagem.class);

                    byte imagem[] = Base64.decode(img.getImagem(), Base64.NO_WRAP | Base64.URL_SAFE);
                    Bitmap bmp = BitmapFactory.decodeByteArray(imagem, 0, imagem.length);

                    saveBitMap(bmp, img.getResultados());

                }

                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getContext(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {


                Bundle bundle = new Bundle();
                bundle.putSerializable("images", images);
                bundle.putInt("position", position);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");

            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isNetworkAvailable()) {
                    Snackbar.make(getView(), "Networn Unavailable! Try later", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    mLayoutManager.removeAllViews();
                    cleanImageFolder();
                    images.clear();
                    images_bitmaps.clear();
                    getImagesFromFirebase();
                }
            }
        });


        return rootView;
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.logo_small)
                        .setContentTitle("Spotter")
                        .setContentText(getString(R.string.photo_available));

        Intent notificationIntent = new Intent(getActivity(),getActivity().getClass());

        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        builder.setStyle(new NotificationCompat.InboxStyle());


        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }


    private void fetchDataFromStorage() {
        File myDir = getActivity().getFilesDir();
        String imgs = "images";
        File imageFolder = new File(myDir, imgs);
        File[] files = imageFolder.listFiles();
        if (imageFolder.exists()) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jpg")) {
                    Image _imagem = new Image();
                    _imagem.setMedium(file.getAbsolutePath());
                    _imagem.setLarge(file.getAbsolutePath());
                    images.add(_imagem);
                    images_bitmaps.add(readImageBytes(file.getAbsolutePath()));
                    image_paths.add(file.getAbsolutePath());
                }
            }
            mAdapter.notifyDataSetChanged();

        }

    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void cleanImageFolder() {
        images.clear();
        images_bitmaps.clear();
        image_paths.clear();
        File myDir = getActivity().getFilesDir();
        String imgs = "images";
        File imageFolder = new File(myDir, imgs);
        if (!imageFolder.exists())
            imageFolder.mkdirs();
        if (imageFolder.listFiles().length > 0)
            for (File f : imageFolder.listFiles())
                f.delete();

    }

    private void getImagesFromFirebase() {
        database.child("imagens").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                cleanImageFolder();

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Imagem img = noteDataSnapshot.getValue(Imagem.class);

                    byte imagem[] = Base64.decode(img.getImagem(), Base64.NO_WRAP | Base64.URL_SAFE);
                    Bitmap bmp = BitmapFactory.decodeByteArray(imagem, 0, imagem.length);

                    saveBitMap(bmp, img.getResultados());

                }


                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveBitMap(Bitmap bitmapImage, String results) {
        // path to /data/data/yourapp/app_data/imageDir

        String filename_ts = String.valueOf(System.currentTimeMillis());

        // Create imageDir
        File myDir = getActivity().getFilesDir();
        String imgs = "images";
        File imageFolder = new File(myDir, imgs);
        if (!imageFolder.exists())
            imageFolder.mkdirs(); // this line creates data folder at documents directory

        File mypath = new File(imageFolder, filename_ts + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Image _imagem = new Image();
        _imagem.setMedium(mypath.getAbsolutePath());
        _imagem.setLarge(mypath.getAbsolutePath());
        _imagem.setName(results);
        images.add(_imagem);
        images_bitmaps.add(readImageBytes(mypath.getAbsolutePath()));
        image_paths.add(mypath.getAbsolutePath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private byte[] readImageBytes(String ImagePath){
        File file = new File(ImagePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}