package com.example.bbcnewsreader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bbcnewsreader.NewsListItems;
import com.example.bbcnewsreader.R;
import com.example.bbcnewsreader.SqlDbHelper;

import java.util.ArrayList;

public class NewsListAdapter extends BaseAdapter {
    ArrayList<NewsListItems> myList;
    Context context;

    SqlDbHelper dbHelper;
    SQLiteDatabase db;

    public NewsListAdapter(Context context, ArrayList<NewsListItems> list) {
        this.context = context;
        myList = list;

        dbHelper = new SqlDbHelper(context);
        db = dbHelper.getWritableDatabase();
    }


    @Override
    public int getCount() {
        return myList.size();
    }


    @Override
    public Object getItem(int position) {
        return myList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return (long) position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        ViewHolder viewHolder;
        NewsListItems myListItems = myList.get(position);
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rss_item_list_row, null);

            viewHolder.txtLink = (TextView) convertView.findViewById(R.id.news_url);
            viewHolder.txtGuid = (TextView) convertView.findViewById(R.id.news_guid);
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.news_title);
            viewHolder.txtDescription = (TextView) convertView.findViewById(R.id.news_description);
            viewHolder.txtPubdate = (TextView) convertView.findViewById(R.id.news_pubdate);
            viewHolder.txtFavouritestate = (TextView) convertView.findViewById(R.id.news_favouritestate);

            result=convertView;

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtLink.setText(myListItems.getLink());
        viewHolder.txtGuid.setText(myListItems.getGuid());
        viewHolder.txtTitle.setText(myListItems.getTitle());
        viewHolder.txtDescription.setText(myListItems.getDescription());
        viewHolder.txtPubdate.setText(myListItems.getPubdate());

        String favouritestate = "";
        View view = (View) viewHolder.txtTitle.getParent();
        // check if guid in db
        Cursor c = db.rawQuery("SELECT * FROM Favourites WHERE GUID = ?",
                new String[] { myListItems.getGuid()});
        if(c.getCount() > 0) {
            // find in db
            favouritestate = "1";
            view.setBackgroundColor(Color.GREEN);
            view.getBackground().setAlpha(120);
            myListItems.setState("1");
        }
        else {
            // not find in db
            favouritestate = "0";
            view.setBackgroundColor(Color.WHITE);
            myListItems.setState("0");
        }

        viewHolder.txtFavouritestate.setText(favouritestate);

        return convertView;
    }


    private static class ViewHolder {
        TextView txtLink;
        TextView txtGuid;
        TextView txtTitle;
        TextView txtDescription;
        TextView txtPubdate;
        TextView txtFavouritestate;
    }
}
