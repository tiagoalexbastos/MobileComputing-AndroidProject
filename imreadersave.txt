package ua.cm.tiagoalexbastos.imgreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase fire_database = FirebaseDatabase.getInstance();


        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        final List imgs = new ArrayList<String>();


        database.child("imagens").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String img = noteDataSnapshot.getValue(String.class);
                    Log.d("TAG", img);
                    byte imagem[] = Base64.decode( img, Base64.NO_WRAP | Base64.URL_SAFE);

                    Log.d("TAGBYTE", String.valueOf(imagem));
                    Bitmap bmp= BitmapFactory.decodeByteArray(imagem,0,imagem.length);
                    ImageView image= (ImageView) findViewById(R.id.imageView);
                    image.getLayoutParams().width = 920;
                    image.getLayoutParams().height = 920;
                    image.setImageBitmap(bmp);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        Log.d("TAG", (String) imgs.get(0));

//        byte imagem[] = Base64.decode((String) imgs.get(0), Base64.DEFAULT);

    }
}