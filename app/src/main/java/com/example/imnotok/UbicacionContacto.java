package com.example.imnotok;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
//Activity para manejar la ubicación en tiempo real del contacto que compartio su app con el usuario. Mostrando un mapa en tiempo real de la ultima ubicación del contacto con la ubicación del usuario.
public class UbicacionContacto extends FragmentActivity implements OnMapReadyCallback {

    String NumeroContacto;
    String NombreContacto;
    String NumeroUsuario;
    String NombreUsuario;

    GoogleMap map;
    SupportMapFragment mapFragment;
    DatabaseReference mDatabase;

    UsuarioOK ContactoUsuario = new UsuarioOK();
    UsuarioOK Usuario = new UsuarioOK();

    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion_contacto);

        mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        NumeroContacto=getIntent().getStringExtra("NumeroContacto");
        NombreContacto=getIntent().getStringExtra("NombreContacto");
        NumeroUsuario=getIntent().getStringExtra("NumeroUsuario");
        NombreUsuario=getIntent().getStringExtra("NombreUsuario");

        mDatabase= FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;

        mDatabase.child("usuarios").child(NumeroContacto).child("usuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ContactoUsuario=dataSnapshot.getValue(UsuarioOK.class);
                latLng = new LatLng(ContactoUsuario.getLatitude(),ContactoUsuario.getLongitude());

                map.clear();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                map.addMarker(new MarkerOptions().position(latLng).title(NombreContacto));
                Toast.makeText(UbicacionContacto.this,"Ubicacion Contacto Actualizada.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabase.child("usuarios").child(NumeroUsuario).child("usuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario=dataSnapshot.getValue(UsuarioOK.class);
                latLng = new LatLng(Usuario.getLatitude(),Usuario.getLongitude());

                map.clear();
                MarkerOptions MakerUsuario = new MarkerOptions().position(latLng).title(NombreUsuario);
                map.addMarker(MakerUsuario);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
