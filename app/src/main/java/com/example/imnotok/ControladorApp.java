package com.example.imnotok;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class ControladorApp extends AppCompatActivity {

    Button OKbtn;
    Button Contactosbtn;
    Button Ubicacionbtn;
    TextView NombreTextView;
    TextView NumeroTextView;
    TextView NumNotificacionTextView;
    TextView TimerTextView;
    ListView ListaContactosListView;
    TextView ContactoSeleccionadoTextView;
    CheckBox DiesMinutosCheckBox;
    CheckBox VeinteMinutosCheckBox;
    CheckBox TreintaMinutosCheckBox;
    Button DetenerButton;
    Button IniciarButton;

    String numeroDeUsuario;
    String NombreCompleto;
    String contraseña;
    String crear;

    DatabaseReference miDatabase;
    Contacto contacto = new Contacto();
    Contacto MiContactoActualisado = new Contacto();
    ArrayList<Contacto> arrayList = new ArrayList<>();
    ArrayList<String> arrayListString = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;


    private final static int NotificacionID = 1;
    private final static String ChannelID = "ImNotOk";

    int MinutosSeleccionados;
    int NumeroDeIntento=0;
    public long ContadorMilisegundos;
    CountDownTimer miCountDownTimer;
    boolean Contadorcorriendo;

    private LocationManager ubicacion;
    UsuarioOK MiUsuario = new UsuarioOK();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlador_app);

        OKbtn = (Button)findViewById(R.id.OKbutton);
        Contactosbtn = (Button)findViewById(R.id.Contactosbutton);
        Ubicacionbtn = (Button)findViewById(R.id.Ubicacionbutton);
        NombreTextView = (TextView)findViewById(R.id.NombretextView);
        NumeroTextView = (TextView)findViewById(R.id.NumerotextView);
        NumNotificacionTextView = (TextView)findViewById(R.id.NumNotificationtextView);
        ListaContactosListView = (ListView)findViewById(R.id.ListaContactosListView);
        ContactoSeleccionadoTextView = (TextView)findViewById(R.id.ContactoSeleccionadotextView);
        DiesMinutosCheckBox = (CheckBox)findViewById(R.id.DiesMinutoscheckBox);
        VeinteMinutosCheckBox = (CheckBox)findViewById(R.id.VeinteMinutoscheckBox);
        TreintaMinutosCheckBox = (CheckBox)findViewById(R.id.TreintaMinutoscheckBox);
        DetenerButton = (Button)findViewById(R.id.Detenerbutton);
        IniciarButton = (Button) findViewById(R.id.Iniciarbutton);
        TimerTextView = (TextView) findViewById(R.id.TimerTextView);
        //Hereda los datos de los textview (Loggin) para poder seguir manejando la cuenta del usuario logeado
        numeroDeUsuario=getIntent().getStringExtra("numeroDeUsuario");
        contraseña=getIntent().getStringExtra("contraseña");
        crear=getIntent().getStringExtra("Crear");
        NombreCompleto=getIntent().getStringExtra("NombreCompleto");
        InsertarDatosLabels();

        DetenerButton.setEnabled(false);
        IniciarButton.setEnabled(false);
        OKbtn.setEnabled(false);
        Ubicacionbtn.setEnabled(false);

        miDatabase= FirebaseDatabase.getInstance().getReference();
        //Array para manipular los contactos registrados previamente, del usuario logeado
        arrayAdapter=new ArrayAdapter<String>(ControladorApp.this,android.R.layout.simple_list_item_1,arrayListString);
        ListaContactosListView.setAdapter(arrayAdapter);
        //Metodo para recivir actualizaciones de intento y localización del contacto.
        ActualizarLocalisasion();
        //Listener para contacto que comparte su app con usuario
        miDatabase.child("usuarios").child(numeroDeUsuario).child("contactos").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Contacto value=dataSnapshot.getValue(Contacto.class);
                String ContactoSTR = value.getNombre()+" - "+value.getNumero();
                arrayListString.add(ContactoSTR);
                arrayList.add(new Contacto(value));
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                arrayAdapter.notifyDataSetChanged();
                Contacto value=dataSnapshot.getValue(Contacto.class);
                CrearNotificaciónIntentosCompletos(value);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Contacto value=dataSnapshot.getValue(Contacto.class);
                String ContactoSTR = value.getNombre()+" - "+value.getNumero();
                arrayListString.add(ContactoSTR);
                arrayList.remove(new Contacto(value));
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //listener para la ListView
        ListaContactosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contacto = arrayList.get(position);
                ContactoSeleccionadoTextView.setText(contacto.getNombre()+" - "+contacto.getNumero() );
                IniciarButton.setEnabled(true);
                DetenerButton.setEnabled(false);
                OKbtn.setEnabled(false);

                if(contacto.getIntento()==-1){
                    Ubicacionbtn.setEnabled(false);
                }
                else {
                    Ubicacionbtn.setEnabled(true);
                }
            }
        });

        //Configuración de los checkbox para el control de tiempos de notificaciones a recivir
        DiesMinutosCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DiesMinutosCheckBox.isChecked()){
                    MinutosSeleccionados = MinutosSeleccionados+100;
                    ContadorMilisegundos =MinutosSeleccionados*6000;
                }
                else {
                    MinutosSeleccionados = MinutosSeleccionados-100;
                    if(MinutosSeleccionados<= 0){MinutosSeleccionados=0;}
                }
            }
        });

        VeinteMinutosCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VeinteMinutosCheckBox.isChecked()){
                    MinutosSeleccionados = MinutosSeleccionados+200;
                    ContadorMilisegundos =MinutosSeleccionados*6000;
                }
                else {
                    MinutosSeleccionados = MinutosSeleccionados-200;
                    if(MinutosSeleccionados<= 0){MinutosSeleccionados=0;}
                }
            }
        });

        TreintaMinutosCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TreintaMinutosCheckBox.isChecked()){
                    MinutosSeleccionados = MinutosSeleccionados+300;
                    ContadorMilisegundos =MinutosSeleccionados*6000;
                }
                else {
                    MinutosSeleccionados = MinutosSeleccionados-300;
                    if(MinutosSeleccionados<= 0){MinutosSeleccionados=0;}
                }
            }
        });


    }
    //Metodo para agregar y verificar en el activity(pantalla) los datos del usuario logeado
    public void InsertarDatosLabels(){
        NombreTextView.setText(NombreCompleto);
        NumeroTextView.setText(numeroDeUsuario);
    }

    //Iniciar activity Contactos
    public void ContactosBtn(View view){
        Intent intent = new Intent(getApplicationContext(),Contactos.class);
        intent.putExtra("numeroDeUsuario",numeroDeUsuario);
        startActivity(intent);
    }
    //Metodo OnClick para Iniciar la transferencia de la ubicación e intentos del usuario al contacto seleccionado previamente
    public void IniciarBtn(View view){
            if(MinutosSeleccionados<=0){
                Toast.makeText(ControladorApp.this,"Selecciona un lapso de tiempo primero...",Toast.LENGTH_SHORT).show();
            }
            else {
                if(Contadorcorriendo){
                    Toast.makeText(ControladorApp.this,"Ya esta Activa La aplicación",Toast.LENGTH_SHORT).show();
                }
                else{
                    ComenzarContador();
                }
            }
    }
    //Controlador del Timer para las notificaciónes que recivira el usuario de la app y los timers para que actualice los intentos del contacto con el que se inicio la app, así el contacto recivira las alertas
    private void ComenzarContador() {

        miCountDownTimer = new CountDownTimer(ContadorMilisegundos,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ContadorMilisegundos = millisUntilFinished;
                ActualizarContadorPantalla();
            }
            @Override
            public void onFinish(){
                CanalNotificacion();
                CrearNotificación();
                NumeroDeIntento=NumeroDeIntento+1;
                NumNotificacionTextView.setText(""+NumeroDeIntento);
                DetenerContador();
                ActualizarContador();

                MiContactoActualisado.setNumero(numeroDeUsuario);
                MiContactoActualisado.setNombre(NombreCompleto);
                MiContactoActualisado.setIntento(NumeroDeIntento);

                miDatabase.child("usuarios").child(contacto.getNumero()).child("contactos").child(numeroDeUsuario).setValue(MiContactoActualisado);
                NumNotificacionTextView.setText(""+NumeroDeIntento);
            }
        }.start();
        Contadorcorriendo=true;
        OKbtn.setEnabled(true);
        DetenerButton.setEnabled(true);
        IniciarButton.setEnabled(false);
    }
    //Regresa a 0 los intentos de notificación
    public void ImOkButton(View view){
            NumeroDeIntento = 0 ;
            MiContactoActualisado.setNumero(numeroDeUsuario);
            MiContactoActualisado.setNombre(NombreCompleto);
            MiContactoActualisado.setIntento(NumeroDeIntento);
            miDatabase.child("usuarios").child(contacto.getNumero()).child("contactos").child(numeroDeUsuario).setValue(MiContactoActualisado);
            Toast.makeText(ControladorApp.this,"Intentos Actualisados",Toast.LENGTH_SHORT).show();
            ActualizarContador();

    }
    //Actualiza el contador al iniciar, parar y cambio del tiempo en recivir notificación (CheckBox)
    public void ActualizarContador(){
        ContadorMilisegundos =MinutosSeleccionados*6000;
        DetenerContador();
        ActualizarContadorPantalla();
        ComenzarContador();
        NumNotificacionTextView.setText(""+NumeroDeIntento);

        Contadorcorriendo=true;
    }

    public void ActualizarContadorPantalla(){
        int minutos = (int) (ContadorMilisegundos/1000) / 60;
        int segundos = (int) (ContadorMilisegundos/1000) % 60;

        String FormatodeTiempo = String.format(Locale.getDefault(),"%02d:%02d", minutos,segundos);
        TimerTextView.setText(FormatodeTiempo);
    }

    public void DetenerBtn(View view){
            DetenerContador();
    }

    public void DetenerContador(){
        miCountDownTimer.cancel();
        Contadorcorriendo=false;
        DetenerButton.setEnabled(false);
        IniciarButton.setEnabled(true);
    }


    //Metodo para reconfigurar la notificación si la verción de android es inferior a la asignada
    public void CanalNotificacion(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence nombre = "ImNotOk";
            NotificationChannel notificationChannel = new NotificationChannel(ChannelID,nombre, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    //Metodo para crear la notificación a recivir el usuario según los Timers
    public void CrearNotificación(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ChannelID);
        builder.setSmallIcon(R.drawable.ic_sentiment_satisfied_black_24dp);
        builder.setContentTitle("Im Not OK");
        builder.setContentText("Dale click en - im OK - ");
        builder.setColor(Color.GREEN);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setVibrate(new long []{1000,1000,1000,1000,1000});
        builder.setDefaults(Notification.DEFAULT_SOUND);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NotificacionID,builder.build());
    }
    //Metodo para crear la notificación a recivir el contacto si el contador llega al tercer intento
    public void CrearNotificaciónIntentosCompletos(Contacto value){

        if (value.getIntento()>=3){

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ChannelID);
            builder.setSmallIcon(R.drawable.ic_sentiment_dissatisfied_black_24dp);
            builder.setContentTitle("Algo No Esta Bien");
            builder.setContentText("Tu Contacto "+ value.getNombre()+ " No ha respondido "+value.getIntento()+" notificaciónes");
            builder.setColor(Color.RED);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVibrate(new long []{1000,1000,1000,1000,1000});
            builder.setDefaults(Notification.DEFAULT_SOUND);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(NotificacionID,builder.build());
        }
        else {
            Toast.makeText(ControladorApp.this,"Tu contacto "+value.getNombre()+"  ha cambiado "+ value.getIntento(),Toast.LENGTH_SHORT).show();
        }

    }
    //Metodo OnClick para abrir la ubicación del contacto seleccionado del ListView(Solo si este ah compartido su app)
    public void UbicacionBtn(View view){

        Intent intent = new Intent(getApplicationContext(),UbicacionContacto.class);
        intent.putExtra("NumeroContacto",contacto.getNumero());
        intent.putExtra("NombreContacto",contacto.getNombre());

        intent.putExtra("NumeroUsuario",numeroDeUsuario);
        intent.putExtra("NombreUsuario",NombreCompleto);

        startActivity(intent);
    }

    //actualiza la localización del app, del usuarío en tiempo real.
    private void ActualizarLocalisasion(){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,locationListener);
    }

    LocationListener locationListener= new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());

            MiUsuario.setLatitude(latLng.latitude);
            MiUsuario.setLongitude(latLng.longitude);
            MiUsuario.setNombreDeUsuario(NombreCompleto);
            MiUsuario.setNumeroDeUsuario(numeroDeUsuario);
            MiUsuario.setContraseña(contraseña);

            //actualiza los datos conforme al nickname obtenido del login
            miDatabase.child("usuarios").child(MiUsuario.getNumeroDeUsuario()).child("usuario").setValue(MiUsuario);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
