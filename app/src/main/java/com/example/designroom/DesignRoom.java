package com.example.designroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DesignRoom extends AppCompatActivity {
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;

    Button removeObj, record, home;
    TransformableNode node;


    ImageView img, img2, img3, img4, img5, img6;
    Uri videoUri;
    VideoRecorder videoRecorder;

    AlertDialog dialog;

    String titleRoom; //global variable for alert box
    //String description; //global variable for description

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.designroom);

        if (ActivityCompat.checkSelfPermission(DesignRoom.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(DesignRoom.this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        removeObj = (Button) findViewById(R.id.remove);
        home = (Button) findViewById(R.id.home);

        setUpPlane();

        img = (ImageView) findViewById(R.id.chair);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setUpModel();
            }
        });

        img2 = (ImageView) findViewById(R.id.table);
        img2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Common.model = "table.sfb";
                setUpTable();

            }
        });

        img3 = (ImageView) findViewById(R.id.lamp);
        img3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Common.model = "table.sfb";
                setUpLamp();
            }
        });

        img4 = (ImageView) findViewById(R.id.clothdryer);
        img4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Common.model = "table.sfb";
                setUpDryer();
            }
        });

        img5 = (ImageView) findViewById(R.id.tv);
        img5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Common.model = "table.sfb";
                setUpTV();
            }
        });

        img6 = (ImageView) findViewById(R.id.shelf);
        img6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Common.model = "table.sfb";
                setUpShelf();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DesignRoom.this, MainActivity.class);
                startActivity(intent);
            }
        });
        removeObj.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                arFragment.getArSceneView().getScene().callOnHierarchy(node -> {
                    node.setRenderable(null);
                    if (node instanceof AnchorNode) {
                        ((AnchorNode) node).getAnchor().detach();
                    }
                });
            }
        });

        record = (Button) findViewById(R.id.record);
        videoRecorder = new VideoRecorder();
        //specify the AR scene view to be recorded.
        videoRecorder.setSceneView(arFragment.getArSceneView());
        //Set video quality and recording orientation to match that of the device.
        int orientation = DesignRoom.this.getResources().getConfiguration().orientation;
        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation);

        record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
               // videoRecorder = new VideoRecorder();
                try {
                  /*  //specify the AR scene view to be recorded.
                    videoRecorder.setSceneView(arFragment.getArSceneView());
                    //Set video quality and recording orientation to match that of the device.
                    int orientation = DesignRoom.this.getResources().getConfiguration().orientation;
                    videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation);
                 */
                    boolean recording = videoRecorder.onToggleRecord();
                    if (recording) {
                        Toast.makeText(DesignRoom.this, "Started Record", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DesignRoom.this, "Stopped Record", Toast.LENGTH_SHORT).show();
                        //alert box --> choose room
                        AlertDialog.Builder builder = new AlertDialog.Builder(DesignRoom.this);
                        builder.setTitle("Enter Description");

                        View view = getLayoutInflater().inflate(R.layout.custom_dialog,null);
                        EditText description = view.findViewById(R.id.description);
                        Button submit = view.findViewById(R.id.submit);
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                titleRoom = description.getText().toString();
                                String videoPath = videoRecorder.getVideoPath().getAbsolutePath();
                                videoUri = Uri.fromFile(new File(videoPath));
                                //time stamp
                                String timeStamp = "" + System.currentTimeMillis();

                                //File Path
                                String filePathandName = "Videos/" + "video_" + timeStamp;

                                //Storage Reference
                                StorageReference storageBase = FirebaseStorage.getInstance().getReference(filePathandName);
                                //Upload Videos
                                storageBase.putFile(videoUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                                while (!uriTask.isSuccessful()) ;
                                                Uri downloadUri = uriTask.getResult();
                                                if (uriTask.isSuccessful()) {
                                                    //url of uploaded video is received

                                                    //add video details to our firebase db
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("id", "" + timeStamp);
                                                    hashMap.put("title", "" + titleRoom);
                                                    hashMap.put("timestamp", "" + timeStamp);
                                                    hashMap.put("videoUrl", "" + downloadUri);

                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
                                                    reference.child(timeStamp)
                                                            .setValue(hashMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(DesignRoom.this, "Video uploaded to Firebase...", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(DesignRoom.this, "Failed here" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(DesignRoom.this, "First Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                dialog.dismiss();
                            }
                        });

                        builder.setView(view);
                        dialog = builder.create();
                        dialog.show();



                        //String videoPath = videoRecorder.getVideoPath().getAbsolutePath();
                        //Toast.makeText(DesignRoom.this, videoPath,Toast.LENGTH_SHORT).show();
                        //Log.d("Locate",videoPath);
                    }
                }
                catch(Exception e ) {
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (ActivityCompat.checkSelfPermission(DesignRoom.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(DesignRoom.this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    private void setUpModel() {
        ModelRenderable.builder()
                .setSource(this, R.raw.chair) //Uri.parse("file:///c:/Users/Evan/AndroidStudioProjects/DesignRoom/app/src/main/res/raw/table.sfb"))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(DesignRoom.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }


    private void setUpTable() {
        ModelRenderable.builder()
                .setSource(this, R.raw.table)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(DesignRoom.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }
    private void setUpLamp() {
        ModelRenderable.builder()
                .setSource(this, R.raw.lamp)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(DesignRoom.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }
    private void setUpDryer() {
        ModelRenderable.builder()
                .setSource(this, R.raw.cloth)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(DesignRoom.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }
    private void setUpTV() {
        ModelRenderable.builder()
                .setSource(this, R.raw.tv)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(DesignRoom.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }
    private void setUpShelf() {
        ModelRenderable.builder()
                .setSource(this, R.raw.model)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(DesignRoom.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }



    private void setUpPlane(){
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                createModel(anchorNode);
            }
        });
    }

    private void createModel(AnchorNode anchorNode){
        node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();

    }


}
