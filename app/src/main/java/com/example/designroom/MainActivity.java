package com.example.designroom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button designRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState   ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Design Room Button
        designRoom = (Button) findViewById(R.id.designRoom);
        designRoom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Start your second activity
                Intent intent = new Intent(MainActivity.this, DesignRoom.class);
                startActivity(intent);
            }
        });

        //Room Gallery Button
        Button roomGallery = (Button) findViewById(R.id.roomGallery);
        roomGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Start your third activty
                Intent thirdintent = new Intent(MainActivity.this, RoomGallery.class);
                startActivity(thirdintent);
            }
        });
    }
}