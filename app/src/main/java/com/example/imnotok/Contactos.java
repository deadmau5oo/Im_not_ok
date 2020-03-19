package com.example.imnotok;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
// Activity donde se maneja el control de los contactos del usuario
public class Contactos extends AppCompatActivity {
    //String que hereda el parametro de la anterior activity(ControladorApp)
    String NumeroUsuarioActivo;

    Button AgregarBtn;
    Button EliminarBtn;

    TextView NumeroDeContacto;
    TextView NombreDeContacto;
    ListView ListaContactos;

    String NumerodeContacto;
    String NombredeContacto;

    DatabaseReference miDatabase;

    Contacto ContactoNuevo = new Contacto();
    //Se almacenara la información para poder visualizar los contactos en una ListView
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);
        AgregarBtn=(Button)findViewById(R.id.Agregarbutton);
        EliminarBtn=(Button)findViewById(R.id.Eliminarbutton);
        NumeroDeContacto=(EditText)findViewById(R.id.NumeroContactoeditText);
        NombreDeContacto=(EditText)findViewById(R.id.NombreContactoeditText);
        NumeroUsuarioActivo=getIntent().getStringExtra("numeroDeUsuario");
        ListaContactos =(ListView)findViewById(R.id.ListaContactosListView);

        miDatabase= FirebaseDatabase.getInstance().getReference();

        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        ListaContactos.setAdapter(arrayAdapter);

        //Listener de la base de datos en el campo contactos de la cuenta del usuario
        miDatabase.child("usuarios").child(NumeroUsuarioActivo).child("contactos").addChildEventListener(new ChildEventListener() {
            @Override //Cuando se agrega un dato nuevo en la base
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Contacto value=dataSnapshot.getValue(Contacto.class);
                String ContactoSTR = value.getNombre()+" - "+value.getNumero()+" Intento N:"+value.getIntento();
                arrayList.add(ContactoSTR);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                arrayAdapter.notifyDataSetChanged();


            }

            @Override //Cuando se elimina un dato en la base
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Contacto value=dataSnapshot.getValue(Contacto.class);
                String ContactoSTR = value.getNombre()+" - "+value.getNumero()+" Intento N:"+value.getIntento();
                arrayList.remove(ContactoSTR);
                arrayAdapter.notifyDataSetChanged();


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        ListaContactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
    //Metodo OnClick para agregar un contacto
    public void AgregarContacto(View view){
        NumerodeContacto=NumeroDeContacto.getText().toString();
        NombredeContacto=NombreDeContacto.getText().toString();

        if (NumerodeContacto.equals(null)||NombredeContacto.equals(null)||NumerodeContacto.equals("")||NombredeContacto.equals("")){
            Toast.makeText(Contactos.this,"Inserta los campos del contacto Primero (Numero y Nombre)",Toast.LENGTH_SHORT).show();
        }
        else{
            //Se envia al void buscar contacto con un entero de parametro para hacer función con el switch
            BuscarContacto(1);
        }
    }
    //metodo para agregar el contacto en la base de datos del usuario
    public void SubiraBD(){

        ContactoNuevo.setNumero(NumerodeContacto);
        ContactoNuevo.setNombre(NombredeContacto);
        ContactoNuevo.setIntento(-1);

        miDatabase.child("usuarios").child(NumeroUsuarioActivo).child("contactos").child(ContactoNuevo.getNumero()).setValue(ContactoNuevo);
        Toast.makeText(Contactos.this,"Se ha registrado",Toast.LENGTH_SHORT).show();
        LimpiarEditText();
    }

    //Metodo OnClick para eliminar un contacto
    public void EliminarContacto(View view){

        NumerodeContacto=NumeroDeContacto.getText().toString();
        NombredeContacto=NombreDeContacto.getText().toString();

        if (NumerodeContacto.equals(null)||NombredeContacto.equals(null)||NumerodeContacto.equals("")||NombredeContacto.equals("")){
            Toast.makeText(Contactos.this,"Inserta los campos del contacto Primero (Numero y Nombre)",Toast.LENGTH_SHORT).show();
        }
        else{
            //Se envia al void buscar contacto con un entero de parametro para hacer función con el switch
            BuscarContacto(2);
        }
    }
    //metodo para eliminar contacto en la base de datos del usuario
    public void EliminarContactoBD(){

        ContactoNuevo.setNumero(NumerodeContacto);
        ContactoNuevo.setNombre(NombredeContacto);

        miDatabase.child("usuarios").child(NumeroUsuarioActivo).child("contactos").child(ContactoNuevo.getNumero()).removeValue();
        Toast.makeText(Contactos.this,"Se ha Eliminado",Toast.LENGTH_SHORT).show();
        LimpiarEditText();
    }

    public void LimpiarEditText(){
        NumeroDeContacto.setText("");
        NumeroDeContacto.setHint("Numero De Contacto");
        NombreDeContacto.setText("");
        NombreDeContacto.setHint("Nombre De Contacto");
    }
    //metodo para buscar los datos del contacto comparandolos con los parametros de los textview
    public void BuscarContacto(final int Opcion){
        miDatabase.child("usuarios").child(NumerodeContacto).child("usuario").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsuarioOK inisiarsesion=dataSnapshot.getValue(UsuarioOK.class);
                if (inisiarsesion==null){
                    Toast.makeText(Contactos.this,"Numero No Registrado",Toast.LENGTH_SHORT).show();
                }
                else if (inisiarsesion.getNumeroDeUsuario().equals(NumerodeContacto)){
                    if(Opcion==1){SubiraBD();}
                    if(Opcion==2){EliminarContactoBD();}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }

}
