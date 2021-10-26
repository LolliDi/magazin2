package com.example.magazin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int LONG_DELAY = 7000;
    TextView TVPrice;
    EditText etNazv, etPrice;
    SQLiteDatabase database;
    ContentValues contentValues;
    DBHelper dbHelper;
    List kol = new ArrayList();

    boolean z = true; // первый проход по записям
    float price = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button BtnZakaz = findViewById(R.id.BtnZakaz);
        BtnZakaz.setOnClickListener(this);

        Button BtnTovar = findViewById(R.id.BtnNewTovar);
        BtnTovar.setOnClickListener(this);

        TVPrice = findViewById(R.id.TVPrice);
        etNazv = findViewById(R.id.etNazv);
        etPrice=findViewById(R.id.etPrice);
        dbHelper = new DBHelper(this);
        database = dbHelper.getReadableDatabase();
        UpdateTable();

    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        String s = b.getText().toString();
        if(s=="В корзину")
        {
            Cursor cursor = database.query(DBHelper.tb_contacts, null, null, null, null, null, null);
            int gid = v.getId();
            cursor.moveToPosition(gid-1);
            int id = cursor.getColumnIndex(DBHelper.sprice);
            try{
            price+=Float.valueOf(cursor.getString(id));
            kol.set(gid-1,((int)kol.get(gid-1)+1));
            TVPrice.setText(price + "₽");
            }
            catch(Exception ee)
            {
                Toast ttt = Toast.makeText(getApplicationContext(), "Ошибка!\n"+ee, Toast.LENGTH_LONG);
                ttt.show();
            }
            cursor.close();
        }
        else {
            switch (v.getId()) {
                case R.id.BtnZakaz:
                    Toast toast = Toast.makeText(getApplicationContext(), "Сумма заказа: " + price + "₽", Toast.LENGTH_LONG);
                    toast.show();
                    price = 0;
                    TVPrice.setText(price + "₽");
                    break;
                case R.id.BtnNewTovar:
                    try {
                        String naz = etNazv.getText().toString();
                        float spr = Float.valueOf(etPrice.getText().toString());
                        contentValues = new ContentValues();
                        contentValues.put(DBHelper.nazv, naz);
                        contentValues.put(DBHelper.sprice, spr);
                        database.insert(DBHelper.tb_contacts, null, contentValues);
                        kol.add(0);
                    } catch (Exception ee) {
                            Toast tt = Toast.makeText(getApplicationContext(), "Ошибка!", Toast.LENGTH_LONG);
                            tt.show();
                    }
                    break;
                default:
                    View outputDBRow = (View) v.getParent();
                    ViewGroup outputDB = (ViewGroup) outputDBRow.getParent();
                    outputDB.removeView(outputDBRow);
                    outputDB.invalidate();
                    int gid = v.getId();

                    Cursor cursor = database.query(DBHelper.tb_contacts, null, null, null, null, null, null);
                    cursor.moveToPosition(gid-1);
                    int id = cursor.getColumnIndex(DBHelper.sprice);
                    try{
                    price-=(Float.valueOf(cursor.getString(id)))*((int)kol.get(gid-1));
                    TVPrice.setText(price + "₽");
                    kol.remove(gid-1);
                    }catch (Exception ee) {
                        Toast tt = Toast.makeText(getApplicationContext(), ""+ee, Toast.LENGTH_LONG);
                        tt.show();
                    }

                    cursor.close();
                    database.delete(DBHelper.tb_contacts, DBHelper.KEY_ID + " = ?", new String[]{String.valueOf(gid)});
                    Cursor cursorUpdater = database.query(DBHelper.tb_contacts, null, null, null, null, null, null);
                    int realID = 1;
                    if (cursorUpdater.getCount() > gid-1) {
                        if (cursorUpdater.moveToFirst()) {
                            int idIndex = cursorUpdater.getColumnIndex(DBHelper.KEY_ID);
                            int nazvIndex = cursorUpdater.getColumnIndex(DBHelper.nazv);
                            int priceIndex = cursorUpdater.getColumnIndex(DBHelper.sprice);

                            do {
                                if (cursorUpdater.getInt(idIndex) != realID) {
                                    contentValues.put(DBHelper.KEY_ID, realID);
                                    contentValues.put(dbHelper.nazv, cursorUpdater.getString(nazvIndex));
                                    contentValues.put(dbHelper.sprice, cursorUpdater.getString(priceIndex));
                                    database.replace(dbHelper.tb_contacts, null, contentValues);
                                }
                                realID++;
                            } while (cursorUpdater.moveToNext());
                            if (cursorUpdater.moveToLast()) {
                                database.delete(dbHelper.tb_contacts, DBHelper.KEY_ID + " = ?", new String[]{cursorUpdater.getString(idIndex)});
                            }
                        }
                        cursorUpdater.close();
                        break;
                    }
            }
        }
        UpdateTable();
    }
    public void UpdateTable()
    {
        Cursor cursor = database.query(DBHelper.tb_contacts, null, null, null, null, null, null);

        TableLayout dbOutput = findViewById(R.id.dbOutput);
        dbOutput.removeAllViews();
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int nazvIndex = cursor.getColumnIndex(DBHelper.nazv);
            int priceIndex = cursor.getColumnIndex(DBHelper.sprice);

            do{
                TableRow dbOutputRow = new TableRow(this);
                dbOutputRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)) ;

                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                TextView outid = new TextView(this);
                params.weight = 1.0f;
                params.width=30;
                outid.setLayoutParams(params);
                outid.setText(cursor.getString(idIndex));
                dbOutputRow.addView(outid);

                TextView outputName = new TextView(this);
                params.weight = 3.0f;
                params.width=300;
                outputName.setLayoutParams(params);
                outputName.setText(cursor.getString(nazvIndex));
                dbOutputRow.addView(outputName);

                TextView outputMail = new TextView(this);
                params.weight = 3.0f;
                params.width=300;
                outputMail.setLayoutParams(params);
                outputMail.setText(cursor.getString(priceIndex));
                dbOutputRow.addView(outputMail);

                Button BtnDelete = new Button(this);
                BtnDelete.setOnClickListener(this);
                params.weight = 1.0f;
                BtnDelete.setWidth(100);
                BtnDelete.setLayoutParams(params);
                BtnDelete.setText("Удалить запись");
                BtnDelete.setId(cursor.getInt(idIndex));
                dbOutputRow.addView(BtnDelete);

                Button BtnKorz = new Button(this);
                BtnKorz.setOnClickListener(this);
                BtnKorz.setWidth(100);
                params.weight = 1.0f;
                BtnKorz.setLayoutParams(params);
                BtnKorz.setText("В корзину");
                BtnKorz.setId(cursor.getInt(idIndex));
                dbOutputRow.addView(BtnKorz);

                dbOutput.addView(dbOutputRow);
                if(z)
                    kol.add(0);

            } while(cursor.moveToNext());
            z=false;
        }
        cursor.close();
    }
}