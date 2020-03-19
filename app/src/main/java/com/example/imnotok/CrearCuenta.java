package com.example.imnotok;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
//Activity para crear una cuenta nueva en la base de datos de la aplicación. Manejandolo  - SIN - los beneficios de la API firebase para crear nuevos usuario con correo y telefono.
public class CrearCuenta extends AppCompatActivity {

    Button crearCuentaBtn;
    EditText NumeroEditText;
    EditText NombreEditText;
    EditText ContraseñaEditText;

    String NumeroTelefonico;
    String NombreCompleto;
    String Contraseña;
    UsuarioOK UsuarioActivo = new UsuarioOK();

    Boolean Comprobador = false;

    LocationManager Ubicacion;
    DatabaseReference miDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);
        crearCuentaBtn=(Button)findViewById(R.id.crearCuentabtn);
        NumeroEditText=(EditText)findViewById(R.id.NumeroTeleditText);
        NombreEditText=(EditText)findViewById(R.id.NombreeditText);
        ContraseñaEditText=(EditText)findViewById(R.id.ContraseñaeditText);


        miDatabase= FirebaseDatabase.getInstance().getReference();

    }

    public void ComprobarCuenta(View view){
        NumeroTelefonico=NumeroEditText.getText().toString();

        miDatabase.child("usuarios").child(NumeroTelefonico).child("usuario").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsuarioOK NuevaCuenta=dataSnapshot.getValue(UsuarioOK.class);
                if (NuevaCuenta==null){
                    Comprobador = true;
                }
                else {
                    Toast.makeText(CrearCuenta.this,"El Numero Ya esta en Uso. Intetna otro numero o codigo para registrarte",Toast.LENGTH_SHORT).show();
                    Comprobador=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(Comprobador){

            NumeroTelefonico=NumeroEditText.getText().toString();
            NombreCompleto=NombreEditText.getText().toString();
            Contraseña=ContraseñaEditText.getText().toString();

            SubirUsuarioDB();
        }
    }

    public void SubirUsuarioDB(){

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }
        Ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LatLng latLng=new LatLng(Ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(),Ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());

        UsuarioActivo.setLatitude(latLng.latitude);
        UsuarioActivo.setLongitude(latLng.longitude);
        UsuarioActivo.setNumeroDeUsuario(NumeroTelefonico);
        UsuarioActivo.setNombreDeUsuario(NombreCompleto);
        UsuarioActivo.setContraseña(Contraseña);

        //Crea un nuevo registro en la base de datos conforme a los datos ingresados del login y la ubicación actual
        miDatabase.child("usuarios").child(UsuarioActivo.getNumeroDeUsuario()).child("usuario").setValue(UsuarioActivo);

        Toast.makeText(CrearCuenta.this,"Se ah registrado tu cuenta! ¡Gracias!",Toast.LENGTH_SHORT).show();

        IniciarControlador();
    }

    public void IniciarControlador(){

        Intent intent = new Intent(getApplicationContext(),ControladorApp.class);
        intent.putExtra("numeroDeUsuario",NumeroTelefonico);
        intent.putExtra("contraseña",Contraseña);
        intent.putExtra("NombreCompleto",NombreCompleto);
        intent.putExtra("Crear","no");
        startActivity(intent);
        finish();

    }

}