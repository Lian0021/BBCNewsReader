package com.example.bbcnewsreader;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ListView listViewFavourites;
    private EditText eddittext;
    private Button buttonfilter;
    private FavouritesListAdapter favouritesListAdapter;
    private ArrayList<FavouritesListItems> favouritesListItems = new ArrayList<FavouritesListItems>();

    SqlDbHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        eddittext = (EditText) findViewById(R.id.eddittext);
        buttonfilter = (Button) findViewById(R.id.buttonfilter);

        //For toolbar:
        Toolbar tBar = findViewById(R.id.toolbar_favourites);
        setSupportActionBar(tBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name) + " " + getString(R.string.version));
        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>" + getString(R.string.my_favourite) + "</font>"));

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout_favourites);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_favourites);
        navigationView.bringToFront();
        navigationView.setItemIconTintList(null);

        navigationView.setNavigationItemSelectedListener(this);

        listViewFavourites = (ListView) findViewById(R.id.listviewFavourites);
        favouritesListAdapter = new FavouritesListAdapter(
                FavouritesActivity.this, favouritesListItems);
        listViewFavourites.setAdapter(favouritesListAdapter);

        dbHelper = new SqlDbHelper(this);
        db = dbHelper.getWritableDatabase();

        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String s1 = sh.getString("text", "");
        eddittext.setText(s1);

        loadDataFromDB(s1);

        buttonfilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = eddittext.getText().toString();

                if (text.isEmpty()) {
                    listViewFavourites.setAdapter(null);
                    favouritesListAdapter = new FavouritesListAdapter(
                            FavouritesActivity.this, favouritesListItems);
                    listViewFavourites.setAdapter(favouritesListAdapter);

                    loadDataFromDB("");
                }
                else {
                    listViewFavourites.setAdapter(null);
                    favouritesListAdapter = new FavouritesListAdapter(
                            FavouritesActivity.this, favouritesListItems);
                    listViewFavourites.setAdapter(favouritesListAdapter);

                    loadDataFromDB(text);
                }
            }
        });


        listViewFavourites.setOnItemClickListener((p, b, pos, id) -> {
            FavouritesListItems favouritesListItem = (FavouritesListItems) favouritesListAdapter.getItem(pos);

            View frameLayoutView = (View)findViewById(R.id.news_details_container);

            if(frameLayoutView == null) {
                // on phone
                Intent intent = new Intent(this, NewsDetailsActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("TITLE_TEXT", favouritesListItem.getTitle());
                bundle.putString("DESCRIPTION_TEXT", favouritesListItem.getDescription());
                bundle.putString("LINK_TEXT", favouritesListItem.getLink());
                bundle.putString("GUID_TEXT", favouritesListItem.getGuid());
                bundle.putString("PUBDATE_TEXT", favouritesListItem.getPubdate());
                bundle.putString("FAVOURITESTATE_TEXT", "1");
                intent.putExtras(bundle);
                //startActivity(intent);
                startActivityForResult(intent, 1);
            }
            else {
                // on tablet
                Bundle bundle = new Bundle();
                bundle.putString("TITLE_TEXT", favouritesListItem.getTitle());
                bundle.putString("DESCRIPTION_TEXT", favouritesListItem.getDescription());
                bundle.putString("LINK_TEXT", favouritesListItem.getLink());
                bundle.putString("GUID_TEXT", favouritesListItem.getGuid());
                bundle.putString("PUBDATE_TEXT", favouritesListItem.getPubdate());
                bundle.putString("FAVOURITESTATE_TEXT", "1");

                DetailsFragment dFragment = new DetailsFragment();
                dFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.news_details_container, dFragment)
                        .commit();
            }
        });

        listViewFavourites.setOnItemLongClickListener((p, b, pos, id) -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder (this);
            alertDialogBuilder.setTitle(getString(R.string.dia_title));
            alertDialogBuilder.setMessage(getString(R.string.dia_message));
            alertDialogBuilder.setPositiveButton(getString(R.string.yes), (click, arg) -> {
                FavouritesListItems favouritesListItem = (FavouritesListItems) favouritesListAdapter.getItem(pos);

                deleteItem(favouritesListItem);

                favouritesListItems.remove(pos);
                favouritesListAdapter.notifyDataSetChanged();

                TextView txtFavouritesHeader = (TextView) findViewById(R.id.txtFavouritesHeader);
                txtFavouritesHeader.setText( getString(R.string.num_favourites) + favouritesListItems.size());
            });

            alertDialogBuilder.setNegativeButton(getString(R.string.no), (click, arg) -> {});
            alertDialogBuilder.create().show();

            return true;
        });
    }


    private void deleteItem(FavouritesListItems favouritesListItem) {
        db.delete(SqlDbHelper.TABLE_NAME,
                SqlDbHelper.COL_GUID + "=?",
                new String[] {favouritesListItem.getGuid() });
    }


    private void loadDataFromDB(String text) {
        favouritesListItems.clear();

        String[] columns = {SqlDbHelper.COL_ID, SqlDbHelper.COL_TITLE, SqlDbHelper.COL_DESCRIPTION,
                            SqlDbHelper.COL_LINK, SqlDbHelper.COL_GUID, SqlDbHelper.COL_PUBDATE};
        String selection;
        if(text.isEmpty()) {
            selection = null;
        }
        else {
            selection = SqlDbHelper.COL_TITLE + " like '%" + text + "%'";
        }

        Cursor cursor = db.query(SqlDbHelper.TABLE_NAME, columns,
                selection, null, null, null, SqlDbHelper.COL_ID + " desc");

        TextView txtFavouritesHeader = (TextView) findViewById(R.id.txtFavouritesHeader);
        txtFavouritesHeader.setText( getString(R.string.num_favourites) + cursor.getCount());

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int idColumnIndex = cursor.getColumnIndex(SqlDbHelper.COL_ID);
            int titleColumnIndex = cursor.getColumnIndex(SqlDbHelper.COL_TITLE);
            int descriptionColumnIndex = cursor.getColumnIndex(SqlDbHelper.COL_DESCRIPTION);
            int linkColumnIndex = cursor.getColumnIndex(SqlDbHelper.COL_LINK);
            int guidColumnIndex = cursor.getColumnIndex(SqlDbHelper.COL_GUID);
            int pubdateColumnIndex = cursor.getColumnIndex(SqlDbHelper.COL_PUBDATE);

            do {
                String title = cursor.getString(titleColumnIndex);
                String description = cursor.getString(descriptionColumnIndex);
                String link = cursor.getString(linkColumnIndex);
                String guid = cursor.getString(guidColumnIndex);
                String pubdate = cursor.getString(pubdateColumnIndex);

                long id = cursor.getLong(idColumnIndex);
                FavouritesListItems favouritesListItem =
                        new FavouritesListItems(id, title, description, link, guid, pubdate);

                favouritesListItems.add(favouritesListItem);
            } while(cursor.moveToNext());
            cursor.close();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == 1) {
                // Refresh the Activity Favourites
                Intent i = new Intent(FavouritesActivity.this, FavouritesActivity.class);
                finish();
                overridePendingTransition(0, 0);
                startActivity(i);
                overridePendingTransition(0, 0);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_items_favourites, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        String message = "";

        switch (item.getItemId()) {
            case R.id.action_favorite:
                message = getResources().getString(R.string.action_favorite_clicked);
                break;

            case R.id.help_favourites:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder (this);
                alertDialogBuilder.setTitle(getString(R.string.help_dia_title));
                alertDialogBuilder.setMessage(getString(R.string.help_favourites_dia_message1) +
                        getString(R.string.help_favourites_dia_message2) +
                        getString(R.string.help_favourites_dia_message3) +
                        getString(R.string.help_favourites_dia_message4) +
                        getString(R.string.help_favourites_dia_message5));

                alertDialogBuilder.setPositiveButton(getString(R.string.ok), (click, arg) -> {});
                alertDialogBuilder.create().show();

                break;
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        return true;
        //return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String message = null;
        switch(item.getItemId()) {
            case R.id.item_bbc_news_favourites:
                // Navigate to Activity Favourites
                Intent intent = new Intent(this, MainActivity.class);
                startActivityForResult(intent, 1);
                break;

            case R.id.item_exit_favourites:
                // Exit the app
                finishAffinity(); // Close all activites
                System.exit(0);  // Releasing resources
                break;
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_favourites);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // Fetch the stored data in onResume()
    // Because this is what will be called
    // when the app opens again
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String s1 = sh.getString("text", "");
        eddittext.setText(s1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("text", eddittext.getText().toString());
        myEdit.apply();
    }
}