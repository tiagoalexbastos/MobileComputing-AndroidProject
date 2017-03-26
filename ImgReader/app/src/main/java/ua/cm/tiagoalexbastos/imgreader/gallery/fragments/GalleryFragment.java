package ua.cm.tiagoalexbastos.imgreader.gallery.fragments;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


import ua.cm.tiagoalexbastos.imgreader.R;
import ua.cm.tiagoalexbastos.imgreader.gallery.adapter.GalleryAdapter;
import ua.cm.tiagoalexbastos.imgreader.ImageUtils.Image;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    private static final String TAG = "TAG";
    private ArrayList<Image> images;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private ArrayList<Bitmap> images_bitmaps;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private ArrayList<String> image_paths;
    private GalleryAdapter mAdapter;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference database;


    public GalleryFragment() {
        // Required empty public constructor
    }


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

        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        FirebaseDatabase fire_database = FirebaseDatabase.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.floating_action_button);
        fab.show();

        if(!isNetworkAvailable()){
            File myDir = getActivity().getFilesDir();
            String imgs = "images";
            File imageFolder = new File(myDir, imgs);
            File[] files = imageFolder.listFiles();
            if(!imageFolder.exists())
                Snackbar.make(rootView, "No images in local storage! Connect to internet first", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            else {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        Image _imagem = new Image();
                        _imagem.setMedium(file.getAbsolutePath());
                        _imagem.setLarge(file.getAbsolutePath());
                        Log.d("CAMINHO", file.getAbsolutePath());
                        images.add(_imagem);
                        images_bitmaps.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
                        image_paths.add(file.getAbsolutePath());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

        }



        database.child("imagens").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                cleanImageFolder();

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String img = noteDataSnapshot.getValue(String.class);
//                    Log.d("TAG", img);
                    byte imagem[] = Base64.decode(img, Base64.NO_WRAP | Base64.URL_SAFE);

//                    Log.d("TAGBYTE", String.valueOf(imagem));
                    Bitmap bmp = BitmapFactory.decodeByteArray(imagem, 0, imagem.length);

                    saveBitMap(bmp);
                    mAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()){
                    Snackbar.make(view, "Networn Unavailable! Try later", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    mLayoutManager.removeAllViews();
                    images.clear();
                    images_bitmaps.clear();
                    getImagesFromFirebase();
                    mAdapter.notifyDataSetChanged();
                }
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


        return rootView;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void cleanImageFolder() {
        File myDir = getActivity().getFilesDir();
        String imgs = "images";
        File imageFolder = new File(myDir, imgs);
        if(!imageFolder.exists())
            imageFolder.mkdirs();
        if (imageFolder.listFiles().length > 0)
            for(File f: imageFolder.listFiles())
                f.delete();
    }

    private void getImagesFromFirebase() {
        database.child("imagens").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                cleanImageFolder();

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String img = noteDataSnapshot.getValue(String.class);
//                    Log.d("TAG", img);
                    byte imagem[] = Base64.decode(img, Base64.NO_WRAP | Base64.URL_SAFE);

//                    Log.d("TAGBYTE", String.valueOf(imagem));
                    Bitmap bmp = BitmapFactory.decodeByteArray(imagem, 0, imagem.length);

                    saveBitMap(bmp);
                    mAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveBitMap(Bitmap bitmapImage) {
        // path to /data/data/yourapp/app_data/imageDir

        Long tsLong = System.currentTimeMillis()/1000;
        String filename_ts = tsLong.toString();

//        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myDir = getActivity().getFilesDir();
        String imgs = "images";
        File imageFolder = new File(myDir, imgs);
        if(!imageFolder.exists())
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
        Log.d("CAMINHO", mypath.getAbsolutePath());
        images.add(_imagem);
        images_bitmaps.add(bitmapImage);
        image_paths.add(mypath.getAbsolutePath());
    }



}