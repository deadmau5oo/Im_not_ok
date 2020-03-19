package com.example.imnotok;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

//Aplicación android, guiada a la seguridad personal, con envío y recepción de ubicación y notificaciones para seguimiento de la misma.
//Esta basada en un sistema de alarma, para el envión de la notificación. Esta al acumularse 3 veces(notificaciones no respondidas) se le envía una notificación al contacto guardado dentro de la app, así podrá tener el conocimiento, que no se ha podido dar seguimiento de esto y el contacto asignado podrá ver la ultima ubicación del celular (usuario) que compartió el servicio de su app.

public class MainActivity extends AppCompatActivity {

    Button crearCuenta;
    Button iniciarSesion;
    EditText numeroEditText;
    EditText contraseñaEditText;

    String numeroDeUsuario;
    String contraseña;
    String NombreCompleto;
    Boolean crear = false;

    DatabaseReference miDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crearCuenta=(Button)findViewById(R.id.crearCuentabtn);
        iniciarSesion=(Button)findViewById(R.id.iniciarSesionbtn);
        numeroEditText=(EditText) findViewById(R.id.numeroeditText);
        contraseñaEditText=(EditText)findViewById(R.id.contraseñaeditText);

        miDatabase = FirebaseDatabase.getInstance().getReference();



    }

    public void CrearCuentaBtn(View view){
        Intent intent =new Intent(getApplicationContext(),CrearCuenta.class);
        startActivity(intent);
    }

    //consulta en la base de datos si el nickname y la contraseña son iguales
    public void IniciarSesion(View view){

        numeroDeUsuario=numeroEditText.getText().toString();
        contraseña=contraseñaEditText.getText().toString();
        miDatabase.child("usuarios").child(numeroDeUsuario).child("usuario").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsuarioOK inisiarsesion=dataSnapshot.getValue(UsuarioOK.class);
                if (inisiarsesion==null){
                    Toast.makeText(MainActivity.this,"Numero No Registrado",Toast.LENGTH_SHORT).show();
                }
                else if (inisiarsesion.getNumeroDeUsuario().equals(numeroDeUsuario)&&inisiarsesion.getContraseña().equals(contraseña)){
                    crear=true;
                    NombreCompleto=inisiarsesion.getNombreDeUsuario();
                }
                else{
                    Toast.makeText(MainActivity.this,"contraseña Incorrecta",Toast.LENGTH_SHORT).show();
                    crear=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(crear){
            Intent intent = new Intent(getApplicationContext(),ControladorApp.class);
            intent.putExtra("numeroDeUsuario",numeroDeUsuario);
            intent.putExtra("contraseña",contraseña);
            intent.putExtra("NombreCompleto",NombreCompleto);
            intent.putExtra("Crear","no");
            startActivity(intent);
        }


    }
}
