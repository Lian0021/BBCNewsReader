package com.example.bbcnewsreader;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ProgressBar progressBar;
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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        //For toolbar:
        Toolbar tBar = findViewById(R.id.toolbar_home);
        setSupportActionBar(tBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name) + " " + getString(R.string.version));
        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>" + getString(R.string.home) + "</font>"));

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

            View frameLayoutView = (View)findViewById(R.id.news_details_container);

            if(frameLayoutView == null) {
                // on phone
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
            }
            else {
                // on tablet
                Bundle bundle = new Bundle();
                bundle.putString("TITLE_TEXT", newsListItem.getTitle());
                bundle.putString("DESCRIPTION_TEXT", newsListItem.getDescription());
                bundle.putString("LINK_TEXT", newsListItem.getLink());
                bundle.putString("GUID_TEXT", newsListItem.getGuid());
                bundle.putString("PUBDATE_TEXT", newsListItem.getPubdate());
                bundle.putString("FAVOURITESTATE_TEXT", newsListItem.getState());

                DetailsFragment dFragment = new DetailsFragment();
                dFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.news_details_container, dFragment)
                        .commit();
            }
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


    private class FetchFeedTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onProgressUpdate(Integer... args) {
            super.onProgressUpdate(args);
            // update progressBar
            progressBar.setProgress(args[0]);

        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            int totalitems = 0;
            int counter = 0;

            String urlLink = "http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml";

            XmlPullParser parser = Xml.newPullParser();
            InputStream stream = null;
            try {

                URL url = new URL(urlLink);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("item");
                totalitems = nodeList.getLength();

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
                                newsListItem = new NewsListItems();
                                counter++;
                                publishProgress((int) ((counter * 100) / totalitems));
                                Thread.sleep(45);

                            } else if (newsListItem != null) {
                                if (name.equalsIgnoreCase("link")) {
                                    newsListItem.setLink(parser.nextText());
                                } else if (name.equalsIgnoreCase("description")) {
                                    newsListItem.setDescription(parser.nextText().trim());
                                } else if (name.equalsIgnoreCase("pubDate")) {
                                    newsListItem.setPubdate(parser.nextText());
                                } else if (name.equalsIgnoreCase("title")) {
                                    newsListItem.setTitle(parser.nextText().trim());
                                } else if (name.equalsIgnoreCase("guid")) {
                                    newsListItem.setGuid(parser.nextText().trim());
                                }
                            }

                            break;
                        case XmlPullParser.END_TAG:
                            name = parser.getName();
                            if (name.equalsIgnoreCase("item") && newsListItem != null) {
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
            progressBar.setVisibility(View.INVISIBLE);
            if (success) {
                newsListAdapter = new NewsListAdapter(MainActivity.this, newsListItems);
                listViewNews.setAdapter(newsListAdapter);
                View parentLayout = findViewById(android.R.id.content);

                Snackbar.make(parentLayout, "Total news:" + newsListItems.size(), Snackbar.LENGTH_INDEFINITE)
                        .setDuration(30000)
                        .setAction("Close", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Nothing to do but close the Snackbar
                            }
                        })
                        .show();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_items_home, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.help_home:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder (this);
                alertDialogBuilder.setTitle(getString(R.string.help_dia_title));
                alertDialogBuilder.setMessage(getString(R.string.help_home_dia_message1) +
                        getString(R.string.help_home_dia_message2) +
                        getString(R.string.help_home_dia_message3) +
                        getString(R.string.help_home_dia_message4));

                alertDialogBuilder.setPositiveButton(getString(R.string.ok), (click, arg) -> {});
                alertDialogBuilder.create().show();

                break;
        }

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
                finishAffinity(); // Close all activities
                System.exit(0);  // Releasing resources
                break;
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}