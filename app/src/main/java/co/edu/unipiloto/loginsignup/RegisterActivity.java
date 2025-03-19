package co.edu.unipiloto.loginsignup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Geocoder;
import android.location.Address;
import java.util.Locale;
import java.io.IOException;
import androidx.annotation.NonNull;
import android.location.LocationManager;
import android.content.Context;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationCallback;
import android.location.Location;
import android.os.Looper;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etUsuario, etEmail, etDireccion, etPassword, etConfirmPassword;
    private Spinner spinnerRoles;
    private Button btnFechaNacimiento, btnRegistrar,btnUbicacion;
    private RadioGroup radioGroupGenero;
    private int anio, mes, dia;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Verificar si el GPS está activado
        if (!isGPSEnabled()) {
            Toast.makeText(this, "Por favor, activa el GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        // Solicitar permisos de ubicación si aún no han sido concedidos
        solicitarPermisos();

        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicialización de vistas
        etNombre = findViewById(R.id.etNombre);
        etUsuario = findViewById(R.id.etUsuario);
        etEmail = findViewById(R.id.etEmail);
        etDireccion = findViewById(R.id.etDireccion);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRoles = findViewById(R.id.spinnerRoles);
        btnFechaNacimiento = findViewById(R.id.btnFechaNacimiento);
        radioGroupGenero = findViewById(R.id.radioGroupGenero);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnUbicacion = findViewById(R.id.btnUbicacion);

        btnUbicacion.setOnClickListener(v -> {
            solicitarPermisos();
            obtenerUbicacion();
        });

        // Configurar Spinner de Roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoles.setAdapter(adapter);

        // Configurar botones
        btnFechaNacimiento.setOnClickListener(view -> seleccionarFecha());
        btnRegistrar.setOnClickListener(view -> registrarUsuario());
    }

    private void solicitarPermisos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void seleccionarFecha() {
        final Calendar calendario = Calendar.getInstance();
        anio = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH);
        dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    int edad = anio - year;
                    if (edad >= 18) {
                        btnFechaNacimiento.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    } else {
                        Toast.makeText(this, "Debes ser mayor de 18 años", Toast.LENGTH_SHORT).show();
                    }
                }, anio, mes, dia);
        datePickerDialog.show();
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedGeneroId = radioGroupGenero.getCheckedRadioButtonId();
        if (selectedGeneroId == -1) {
            Toast.makeText(this, "Por favor, seleccione un género", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton radioGenero = findViewById(selectedGeneroId);
        String genero = radioGenero.getText().toString();

        String rol = spinnerRoles.getSelectedItem().toString().trim();
        List<String> rolesValidos = Arrays.asList(getResources().getStringArray(R.array.roles_array));

        if (!rolesValidos.contains(rol)) {
            Toast.makeText(this, "Seleccione un rol válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rol.equalsIgnoreCase(genero)) {
            Toast.makeText(this, "El rol no puede ser el mismo que el género", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.isEmpty() || usuario.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        obtenerUbicacionYRegistrar(nombre, usuario, email, password, genero, rol);
    }

    private void obtenerUbicacionYRegistrar(String nombre, String usuario, String email, String password, String genero, String rol) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitud = location.getLatitude();
                double longitud = location.getLongitude();
                obtenerDireccionYRegistrar(nombre, usuario, email, password, genero, rol, latitud, longitud);
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerDireccionYRegistrar(String nombre, String usuario, String email, String password, String genero, String rol, double latitud, double longitud) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(latitud, longitud, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                String direccion = direcciones.get(0).getAddressLine(0);

                boolean insertado = databaseHelper.registrarUsuario(nombre, usuario, email, direccion, password, genero, rol);
                if (insertado) {
                    Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al obtener la dirección: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerUbicacion() {
        if (!isGPSEnabled()) {
            Toast.makeText(this, "El GPS está desactivado. Actívalo para obtener la ubicación.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitud = location.getLatitude();
                double longitud = location.getLongitude();
                Toast.makeText(this, "Ubicación obtenida: " + latitud + ", " + longitud, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ubicación no disponible. Intentando obtener ubicación en tiempo real...", Toast.LENGTH_SHORT).show();
                solicitarUbicacionEnTiempoReal();
            }
        });
    }

    private void solicitarUbicacionEnTiempoReal() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000)
                .setNumUpdates(1);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(RegisterActivity.this, "No se pudo obtener la ubicación en tiempo real", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitud = location.getLatitude();
                    double longitud = location.getLongitude();
                    Toast.makeText(RegisterActivity.this, "Ubicación en tiempo real: " + latitud + ", " + longitud, Toast.LENGTH_LONG).show();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
}
