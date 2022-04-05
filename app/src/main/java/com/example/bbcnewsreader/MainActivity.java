package com.example.bbcnewsreader;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import org.xmlpull.v1.XmlPullParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ListView listViewNews;
    private NewsListAdapter newsListAdapter;
    private ArrayList<NewsListItems> newsListItems = new ArrayList<NewsListItems>();
    SqlDbHelper dbHelper;
    SQLiteDatabase db;
    FetchFeedTask fetchFeedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //For toolbar:
        Toolbar tBar = findViewById(R.id.toolbar_home);
        setSupportActionBar(tBar);

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout_home);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_home);
        navigationView.bringToFront();
        navigationView.setItemIconTintList(null);

        navigationView.setNavigationItemSelectedListener(this);

        listViewNews = (ListView) findViewById(R.id.listviewNews);

        dbHelper = new SqlDbHelper(this);
        db = dbHelper.getWritableDatabase();

        fetchFeedTask = new FetchFeedTask();
        fetchFeedTask.execute();

        listViewNews.setOnItemClickListener((p, b, pos, id) -> {
            NewsListItems newsListItem = (NewsListItems) newsListAdapter.getItem(pos);

            Intent intent = new Intent(this, NewsDetailsActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString("TITLE_TEXT", newsListItem.getTitle());
            bundle.putString("DESCRIPTION_TEXT", newsListItem.getDescription());
            bundle.putString("LINK_TEXT", newsListItem.getLink());
            bundle.putString("GUID_TEXT", newsListItem.getGuid());
            bundle.putString("PUBDATE_TEXT", newsListItem.getPubdate());

            bundle.putString("FAVOURITESTATE_TEXT", newsListItem.getState());
            intent.putExtras(bundle);
            //startActivity(intent);
            startActivityForResult(intent, 1);
        });

        listViewNews.setOnItemLongClickListener((p, b, pos, id) -> {
            NewsListItems newsListItem = (NewsListItems) newsListAdapter.getItem(pos);
            TextView txtFavouritestate = (TextView) findViewById(R.id.news_favouritestate);
            String favouritestate;
            //favouritestate = txtFavouritestate.getText().toString();
            favouritestate = newsListItem.getState();

            if(favouritestate.equals("0")) {
                // Not a favourite
                //  ==> Set background to green
                //  ==> Set it to favourite and insert it to db
                newsListItem.setState("1");

                txtFavouritestate.setText("1");
                b.setBackgroundColor(Color.GREEN);
                b.getBackground().setAlpha(120);

                ContentValues newValues=new ContentValues();
                newValues.put(SqlDbHelper.COL_TITLE, newsListItem.getTitle());
                newValues.put(SqlDbHelper.COL_DESCRIPTION, newsListItem.getDescription());
                newValues.put(SqlDbHelper.COL_LINK, newsListItem.getLink());
                newValues.put(SqlDbHelper.COL_GUID, newsListItem.getGuid());
                newValues.put(SqlDbHelper.COL_PUBDATE, newsListItem.getPubdate());
                long newId = db.insert(SqlDbHelper.TABLE_NAME, null, newValues);

                Toast.makeText(MainActivity.this,
                        getString(R.string.add_to_favourites),
                        Toast.LENGTH_LONG).show();
            }
            else {
                // Is a favourite
                //  ==> Set background to white
                //  ==> Un-set it from favourite and remove it from db
                newsListItem.setState("0");

                txtFavouritestate.setText("0");
                b.setBackgroundColor(Color.WHITE);

                db.delete(SqlDbHelper.TABLE_NAME,
                        SqlDbHelper.COL_GUID + "=?",
                        new String[] { newsListItem.getGuid() });
                Toast.makeText(MainActivity.this,
                        getString(R.string.delete_from_favourites),
                        Toast.LENGTH_LONG).show();
            }

            newsListAdapter.notifyDataSetChanged();

            return true;
        });
    }


    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {
        private String urlLink;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onProgressUpdate(Void... voids) {
            super.onProgressUpdate(voids);
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            String urlLink = "http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml";

            XmlPullParser parser = Xml.newPullParser();
            InputStream stream = null;
            try {
                stream = new URL(urlLink).openConnection().getInputStream();
                parser.setInput(stream, null);
                int eventType = parser.getEventType();
                boolean done = false;
                NewsListItems newsListItem = new NewsListItems();

                while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                    String name = null;
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            name = parser.getName();
                            if (name.equalsIgnoreCase("item")) {
                                Log.i("new item", "Create new item");
                                newsListItem = new NewsListItems();
                            } else if (newsListItem != null) {
                                if (name.equalsIgnoreCase("link")) {
                                    Log.i("Attribute", "setLink");
                                    newsListItem.setLink(parser.nextText());
                                } else if (name.equalsIgnoreCase("description")) {
                                    Log.i("Attribute", "description");
                                    newsListItem.setDescription(parser.nextText().trim());
                                } else if (name.equalsIgnoreCase("pubDate")) {
                                    Log.i("Attribute", "date");
                                    newsListItem.setPubdate(parser.nextText());
                                } else if (name.equalsIgnoreCase("title")) {
                                    Log.i("Attribute", "title");
                                    newsListItem.setTitle(parser.nextText().trim());
                                } else if (name.equalsIgnoreCase("guid")) {
                                    Log.i("Attribute", "title");
                                    newsListItem.setGuid(parser.nextText().trim());
                                }
                            }

                            break;
                        case XmlPullParser.END_TAG:
                            name = parser.getName();
                            Log.i("End tag", name);
                            if (name.equalsIgnoreCase("item") && newsListItem != null) {
                                Log.i("Added", newsListItem.toString());
                                newsListItem.setState("0");
                                newsListItems.add(newsListItem);
                            } else if (name.equalsIgnoreCase("channel")) {
                                done = true;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return true;
        }

        public InputStream getInputStream(URL url) {
            try{
                return url.openConnection().getInputStream();
            }
            catch (IOException e)
            {
                return null;
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                newsListAdapter = new NewsListAdapter(MainActivity.this, newsListItems);
                listViewNews.setAdapter(newsListAdapter);
            } else {
                // Invalid Rss feed URL
                Toast.makeText(MainActivity.this, getString(R.string.invalid_rss_url), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == 1) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
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
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.toolbar_menu_items_home, menu);

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
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        return true;
        //return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String message = null;
        switch(item.getItemId()) {
            case R.id.item_my_favourites_home:
                Intent intent = new Intent(this, FavouritesActivity.class);
                startActivityForResult(intent, 1);

                break;

            case R.id.item_exit_home:
                finishAffinity(); // Close all activites
                System.exit(0);  // Releasing resources
                break;
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}