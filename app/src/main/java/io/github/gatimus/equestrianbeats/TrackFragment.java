package io.github.gatimus.equestrianbeats;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.github.gatimus.equestrianbeats.eqb.EQBeats;
import io.github.gatimus.equestrianbeats.eqb.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TrackFragment extends Fragment implements AbsListView.OnItemClickListener, Callback<List<Track>> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private TrackAdapter mAdapter;
    private List<Track> tracks;

    // TODO: Rename and change types of parameters
    public static TrackFragment newInstance(String param1, String param2) {
        TrackFragment fragment = new TrackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EQBeats.getEQBInterface().getFeaturedTracks(this);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        tracks = new ArrayList<Track>();
        mAdapter = new TrackAdapter(tracks);
        mAdapter.setNotifyOnChange(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.e(getClass().getSimpleName(), activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction("//TODO");
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void success(List<Track> tracks, Response response) {
        this.tracks.addAll(tracks);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e(getClass().getSimpleName(), error.toString());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public class TrackAdapter extends ArrayAdapter<Track> {

        public TrackAdapter(List<Track> objects) {
            super(getActivity().getApplicationContext(), R.layout.track_list_item, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.track_list_item, parent, false);
            TextView title = (TextView) rowView.findViewById(R.id.title);
            TextView artist = (TextView) rowView.findViewById(R.id.artist);
            ImageView art = (ImageView) rowView.findViewById(R.id.art);
            ToggleButton playPause = (ToggleButton) rowView.findViewById(R.id.play_pause);

            title.setText(tracks.get(position).toString());
            artist.setText(tracks.get(position).artist.toString());
            artist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClass(getActivity().getApplicationContext(), UserActivity.class);
                    intent.putExtra(UserActivity.EXTRA_USER_ID, tracks.get(position).artist.id);
                    startActivity(intent);
                }
            });

            if(tracks.get(position).download.containsKey("art")){
                Picasso picasso = Picasso.with(getActivity().getApplicationContext());
                if(BuildConfig.DEBUG) picasso.setIndicatorsEnabled(true);
                picasso.load(tracks.get(position).download.get("art"))
                        .resize(50,50)
                        .into(art);
            }
            playPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // stop music
                    Intent intent = new Intent(MusicService.ACTION_STOP);
                    intent.setClass(getActivity().getApplicationContext(), MusicService.class);
                    getActivity().startService(intent);
                    if(((ToggleButton)v).isChecked()){
                        // play music
                        intent = new Intent(MusicService.ACTION_PLAY);
                        intent.setClass(getActivity().getApplicationContext(), MusicService.class);
                        intent.putExtra(MusicService.KEY_STREAM_URL, tracks.get(position).stream.get("mp3").toString());
                        getActivity().startService(intent);
                    }
                }
            });

            return rowView;
        }

    }

}
