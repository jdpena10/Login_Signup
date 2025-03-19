package co.edu.unipiloto.loginsignup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "usuarios.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_USERS = "usuarios";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_USUARIO = "usuario";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_DIRECCION = "direccion";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_GENERO = "genero";
    private static final String COLUMN_ROL = "rol";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOMBRE + " TEXT,"
                + COLUMN_USUARIO + " TEXT UNIQUE,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_DIRECCION + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_GENERO + " TEXT,"
                + COLUMN_ROL + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Método para registrar un usuario
    public boolean registrarUsuario(String nombre, String usuario, String email, String direccion,
                                    String password, String genero, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Verificar si el usuario ya existe
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USUARIO + " = ?", new String[]{usuario});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            Log.e("DatabaseHelper", "El usuario ya existe en la base de datos.");
            return false; // Usuario ya existe
        }
        cursor.close();

        // Insertar nuevo usuario
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_USUARIO, usuario);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DIRECCION, direccion);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_GENERO, genero);
        values.put(COLUMN_ROL, rol);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        if (result == -1) {
            Log.e("DatabaseHelper", "Error al insertar usuario en la base de datos.");
            return false;
        } else {
            Log.d("DatabaseHelper", "Usuario insertado correctamente con ID: " + result);
            return true;
        }
    }

    // Método para validar credenciales en login
    public boolean validarUsuario(String usuario, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                        COLUMN_USUARIO + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{usuario, password});

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }
}