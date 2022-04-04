package com.example.bbcnewsreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bbcnewsreader.R;
import com.example.bbcnewsreader.SqlDbHelper;

public class NewsDetailsActivity extends AppCompatActivity {
    String state;
    Toolbar tBar;
    SqlDbHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        dbHelper = new SqlDbHelper(this);
        db = dbHelper.getWritableDatabase();

        //For toolbar:
        tBar = findViewById(R.id.toolbar_newsdetails);
        setSupportActionBar(tBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String title_text = bundle.getString("TITLE_TEXT");
        TextView textviewTitle_Text = (TextView) findViewById(R.id.news_detail_title);
        textviewTitle_Text.setText(title_text);

        String description_text = bundle.getString("DESCRIPTION_TEXT");
        TextView textviewDescription_Text = (TextView) findViewById(R.id.news_detail_description);
        textviewDescription_Text.setText(description_text);

        String pubdate_text = bundle.getString("PUBDATE_TEXT");
        TextView textviewPubdate_Text = (TextView) findViewById(R.id.news_detail_pubdate);
        textviewPubdate_Text.setText(pubdate_text);

        // todo: link format
        String link_text = bundle.getString("LINK_TEXT");
        String link;
        // todo
        link = "To view the news, click the link: <br /><a href='" + link_text + "'>" + title_text + "</a>";
        Spanned Text = Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY);
        TextView textviewLink_Text = (TextView) findViewById(R.id.news_detail_link);
        textviewLink_Text.setMovementMethod(LinkMovementMethod.getInstance());
        textviewLink_Text.setText(Text);

        String guid_text = bundle.getString("GUID_TEXT");
        TextView textviewGuid_Text = (TextView) findViewById(R.id.news_detail_guid);
        textviewGuid_Text.setText(guid_text);

        String state_text = bundle.getString("FAVOURITESTATE_TEXT");
        TextView textviewState_Text = (TextView) findViewById(R.id.newsdetails_favouritestate);
        textviewState_Text.setText(state_text);

        if(state_text.equals("1")) {
            View view = (View) textviewTitle_Text.getParent();
            view.setBackgroundColor(Color.GREEN);
            view.getBackground().setAlpha(120);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_items_favouritesdetails, menu);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String state_text = bundle.getString("FAVOURITESTATE_TEXT");
        if(state_text.equals("1")) {
            MenuItem menuItem = menu.findItem(R.id.action_favorite);
            menuItem.setIcon(R.drawable.star_un_favourite);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                // Back to Activity Home
                setResult(1);
                finish();
                break;

            case R.id.action_favorite:
                String message = "";

                TextView txtFavouritestate = (TextView) findViewById(R.id.newsdetails_favouritestate);
                state = txtFavouritestate.getText().toString();
                TextView textviewTitle_Text = (TextView) findViewById(R.id.news_detail_title);
                TextView textviewDescription_Text = (TextView) findViewById(R.id.news_detail_description);
                TextView textviewPubdate_Text = (TextView) findViewById(R.id.news_detail_pubdate);
                TextView textviewLink_Text = (TextView) findViewById(R.id.news_detail_link);
                TextView textviewGuid_Text = (TextView) findViewById(R.id.news_detail_guid);

                View view = (View) textviewTitle_Text.getParent();
                Menu menu = tBar.getMenu();;
                MenuItem menuItem = menu.findItem(R.id.action_favorite);

                if(state.equals("1")) {
                    message = getString(R.string.delete_from_favourites);

                    // delete from db
                    db.delete(SqlDbHelper.TABLE_NAME,
                            SqlDbHelper.COL_GUID + "=?",
                            new String[] { textviewGuid_Text.getText().toString() });

                    txtFavouritestate.setText("0");
                    view.setBackgroundColor(Color.WHITE);
                    menuItem.setIcon(R.drawable.star_favourite);
                }
                else {
                    message = getString(R.string.add_to_favourites);

                    // insert into db
                    ContentValues newValues=new ContentValues();
                    newValues.put(SqlDbHelper.COL_TITLE, textviewTitle_Text.getText().toString());
                    newValues.put(SqlDbHelper.COL_DESCRIPTION, textviewDescription_Text.getText().toString());
                    newValues.put(SqlDbHelper.COL_LINK, textviewLink_Text.getText().toString());
                    newValues.put(SqlDbHelper.COL_GUID, textviewGuid_Text.getText().toString());
                    newValues.put(SqlDbHelper.COL_PUBDATE, textviewPubdate_Text.getText().toString());
                    long newId = db.insert(SqlDbHelper.TABLE_NAME, null, newValues);

                    txtFavouritestate.setText("1");
                    view.setBackgroundColor(Color.GREEN);
                    view.getBackground().setAlpha(120);
                    menuItem.setIcon(R.drawable.star_un_favourite);
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                break;
        }

        return true;
        //return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            // Click system back button
            setResult(1);
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}