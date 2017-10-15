package com.example.maupi.parkking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "parking_meter.db";

    /*********************************************************************************************
     * Creating the client table in the database and handling the following functions            *
     *      - Inserting a new account in the table                                               *
     *      - Checking user information to authenticate login                                    *
     *      - Checking the uniqueness of the user name a user enters while creating a new account*
     *********************************************************************************************/

    private static final String TABLE_NAME = "client";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_UNAME = "uname";
    private static final String COLUMN_PASS = "pass";
    private static final String COLUMN_EMAIL = "email";
    SQLiteDatabase db;


    // Create table command
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_UNAME + " VARCHAR(255) NOT NULL, " +
            COLUMN_PASS + " VARCHAR(255) NOT NULL, " +
            COLUMN_EMAIL + " VARCHAR(255) NOT NULL);" ;

    public DatabaseHelper(Context context){
        super(context , DATABASE_NAME , null , DATABASE_VERSION);
    }

    // Creating the client table in the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_PAYMENT);
        this.db = db;
    }

    // Inserting a new account in the client table
    public void insertContact(client c){

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UNAME , c.getUname());
        values.put(COLUMN_PASS , c.getPass());
        values.put(COLUMN_EMAIL , c.getEmail());

        db.insert(TABLE_NAME , null , values);
        db.close();
    }

    // Checking user information to authenticate login
    public String searchInfo(String uname){

        db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_UNAME + " , " + COLUMN_PASS + " FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query , null);
        String u , p = "not found";
        if(cursor.moveToFirst()){

            do{
                u = cursor.getString(cursor.getColumnIndex(COLUMN_UNAME));

                if(u.equals(uname)) {
                    p = cursor.getString(cursor.getColumnIndex(COLUMN_PASS));
                    break;
                }
            } while(cursor.moveToNext());
        }
        return p;
    }

    // Checking the username entered by the user while creating a new account to make sure it's unique
    // and storing the user's username to use it later when getting the foreign key
    static String userName;
    public boolean uniqueUname(String uname){

        db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_UNAME + " FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query , null);
        String user = "";
        boolean unique = true;

        if(cursor.moveToFirst()){

            do{
                user = cursor.getString(cursor.getColumnIndex(COLUMN_UNAME));

                if(user.equals(uname)){
                    unique = false;
                } else{
                    unique = true;
                }
            }while(cursor.moveToNext());
        }

        if(unique){
            userName = uname;
        }
        return unique;
    }


    /******************************************************************************************************************
     * Creating the payment table in the database and handling the following functions                                *
     *      - Checking the uniqueness of the user's payment by having a unique card number and a unique security code *
     *      - Inserting the new payment into the table                                                                *
     *      - Getting the user's ID to link it with the payment table                                                 *
     ******************************************************************************************************************/

    private static final String TABLE_PAYMENT = "Payment";
    private static final String COLUMN_PAYMENT_ID = "id";
    private static final String COLUMN_CLIENT = "client";
    private static final String COLUMN_CARD_NUMBER = "cNumber";
    private static final String COLUMN_SECURITY_CODE = "SecurityCode";
    private static final String COLUMN_EXP_DATE = "ExpirationDate";
    private static final String COLUMN_COUNTRY = "Country";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_ZIP_CODE = "ZipCode";
    private static final String COLUMN_BILLING_ADDRESS = "BillingAddress";
    private static final String COLUMN_CITY_STATE = "CityState";

    private static final String CREATE_TABLE_PAYMENT = "CREATE TABLE " + TABLE_PAYMENT + "(" +
            COLUMN_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
            COLUMN_CLIENT + " , " +
            COLUMN_NAME + " VARCHAR(255) NOT NULL , " +
            COLUMN_CARD_NUMBER + " VARCHAR(15) , " +
            COLUMN_SECURITY_CODE + " VARCHAR(5) NOT NULL , " +
            COLUMN_EXP_DATE + " VARCHAR(10) NOT NULL , " +
            COLUMN_BILLING_ADDRESS + " VARCHAR(50) NOT NULL , " +
            COLUMN_ZIP_CODE + " VARCHAR(6) NOT NULL , " +
            COLUMN_CITY_STATE + " VARCHAR(20) NOT NULL , " +
            COLUMN_COUNTRY + " VARCHAR(20) NOT NULL , " +
            "FOREIGN KEY " + "(" + COLUMN_CLIENT + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + ") );";


    // Checking the payment entered by the user after creating a new account to make sure it's unique
    public boolean uniquePayment(String cardNumber , String securityCode){

        db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_CARD_NUMBER + " , " + COLUMN_SECURITY_CODE + " FROM " + TABLE_PAYMENT;
        Cursor cursor = db.rawQuery(query , null);
        String cardNo , secCode;
        boolean unique = true;

        if(cursor.moveToFirst()){

            do{
                cardNo = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_NUMBER));
                secCode = cursor.getString(cursor.getColumnIndex(COLUMN_SECURITY_CODE));

                if(cardNo.equals(cardNumber) && secCode.equals(securityCode)){
                    unique = false;
                } else{
                    unique = true;
                }
            }while(cursor.moveToNext());
        }
        return unique;
    }

    // Inserting a new payment in the payment table
    public void insertPayment(PaymentInfo p){

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME , p.getNameOnCard());
        values.put(COLUMN_CARD_NUMBER , p.getCardNum());
        values.put(COLUMN_SECURITY_CODE , p.getSecurityCode());
        values.put(COLUMN_EXP_DATE , p.getExpDate());
        values.put(COLUMN_BILLING_ADDRESS , p.getAddress());
        values.put(COLUMN_ZIP_CODE , p.getZip());
        values.put(COLUMN_CITY_STATE , p.getCityState());
        values.put(COLUMN_COUNTRY , p.getCountry());
        values.put(COLUMN_CLIENT , getForeignInfo());


        db.insert(TABLE_PAYMENT , null , values);
        db.close();
    }


    // Get the id of the user to connect the user with his/her payment
    public String getForeignInfo(){
        db = this.getReadableDatabase();
        String checkExistence = "SELECT " + COLUMN_UNAME + " FROM " + TABLE_NAME + ";";
        String userExist , id = "";
        Cursor c = db.rawQuery(checkExistence , null);
        if(c.moveToFirst()){
            do{
                userExist = c.getString(c.getColumnIndex(COLUMN_UNAME));
                if(userExist.equals(userName)){
                    String query  = "SELECT " + COLUMN_ID + " FROM " + TABLE_NAME +" WHERE " + COLUMN_UNAME + " = '" + userName + "';";
                    Cursor cursor = db.rawQuery(query , null);
                    cursor.moveToFirst();
                    id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                }
            }while(c.moveToNext());
        }
        return id;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String drop_client = "DROP TABLE IF EXISTS " + TABLE_NAME;
        String drop_payment = "DROP TABLE IF EXISTS " + TABLE_PAYMENT;

        db.execSQL(drop_client);
        db.execSQL(drop_payment);
        this.onCreate(db);
    }
}
