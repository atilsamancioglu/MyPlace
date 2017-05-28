package com.atilsamancioglu.myplace;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> names = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = new MenuInflater(getApplicationContext());
        menuInflater.inflate(R.menu.add_place, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_place) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);

        try {

            MapsActivity.myDatabase = this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
            Cursor cursor = MapsActivity.myDatabase.rawQuery("SELECT * FROM places", null);
            int nameIx = cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx = cursor.getColumnIndex("longitude");
            cursor.moveToFirst();

            while (cursor != null) {

                String nameFromSQL = cursor.getString(nameIx);
                String coordl1 = cursor.getString(latitudeIx);
                String coordl2 = cursor.getString(longitudeIx);

                Double l1 = Double.parseDouble(coordl1);
                Double l2 = Double.parseDouble(coordl2);

                LatLng locationFromSQL = new LatLng(l1,l2);

                names.add(nameFromSQL);
                locations.add(locationFromSQL);

                cursor.moveToNext();
            }

        } catch(Exception e) {
            e.printStackTrace();
        }


        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, names);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("info", "old");
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

    }
}
