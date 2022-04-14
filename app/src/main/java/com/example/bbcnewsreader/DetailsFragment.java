package com.example.bbcnewsreader;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String title_text;
    String description_text;
    String link_text;
    String guid_text;
    String pubdate_text;
    String favouritestate_text;


    public DetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailsFragment newInstance(String param1, String param2) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            title_text = getArguments().getString("TITLE_TEXT");
            description_text = getArguments().getString("DESCRIPTION_TEXT");
            link_text = getArguments().getString("LINK_TEXT");
            guid_text = getArguments().getString("GUID_TEXT");
            pubdate_text = getArguments().getString("PUBDATE_TEXT");
            favouritestate_text = getArguments().getString("FAVOURITESTATE_TEXT");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        TextView textviewTitle_Text = (TextView) view.findViewById(R.id.news_detail_title);
        textviewTitle_Text.setText(title_text);

        TextView textviewDescription_Text = (TextView) view.findViewById(R.id.news_detail_description);
        textviewDescription_Text.setText(description_text);

        TextView textviewPubdate_Text = (TextView) view.findViewById(R.id.news_detail_pubdate);
        textviewPubdate_Text.setText(pubdate_text);

        String link;
        link = getString(R.string.link_desc) +  "<br />" +
                "<a href='" + link_text + "'>" + title_text + "</a>";
        Spanned Text = Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY);
        TextView textviewLink_Text = (TextView) view.findViewById(R.id.news_detail_link);
        textviewLink_Text.setMovementMethod(LinkMovementMethod.getInstance());
        textviewLink_Text.setText(Text);

        return view;
    }
}