package ua.cm.tiagoalexbastos.imgreader.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ua.cm.tiagoalexbastos.imgreader.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

    private ImageView imageView;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

//        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.floating_action_button);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                takePicture();
//            }
//        });
//
//        imageView  = (ImageView) rootView.findViewById(R.id.imageView_container);

        return inflater.inflate(R.layout.fragment_main, container,
                false);
    }

//    private void takePicture() {
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
//    }
//
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo);
//        }
//    }

}
