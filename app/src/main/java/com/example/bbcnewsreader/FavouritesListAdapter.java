package com.example.bbcnewsreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class FavouritesListAdapter extends BaseAdapter {
    ArrayList<FavouritesListItems> myList;
    Context context;

    SqlDbHelper dbHelper;
    SQLiteDatabase db;

    public FavouritesListAdapter(Context context, ArrayList<FavouritesListItems> list) {
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
        FavouritesListItems myListItems = myList.get(position);
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.favourite_item_list_row, null);

            viewHolder.txtLink = (TextView) convertView.findViewById(R.id.favourites_url);
            viewHolder.txtGuid = (TextView) convertView.findViewById(R.id.favourites_guid);
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.favourites_title);
            viewHolder.txtDescription = (TextView) convertView.findViewById(R.id.favourites_description);
            viewHolder.txtPubdate = (TextView) convertView.findViewById(R.id.favourites_pubdate);
            viewHolder.txtFavouritestate = (TextView) convertView.findViewById(R.id.favourites_favouritesstate);

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
        viewHolder.txtFavouritestate.setText("1");

        View view = (View) viewHolder.txtTitle.getParent();
        view.setBackgroundColor(Color.GREEN);
        view.getBackground().setAlpha(120);

        return convertView;
    }


    // view holder
    private static class ViewHolder {
        TextView txtLink;
        TextView txtGuid;
        TextView txtTitle;
        TextView txtDescription;
        TextView txtPubdate;
        TextView txtFavouritestate;
    }
}
