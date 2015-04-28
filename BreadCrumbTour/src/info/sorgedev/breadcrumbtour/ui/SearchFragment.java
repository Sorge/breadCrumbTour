package info.sorgedev.breadcrumbtour.ui;

import info.sorgedev.breadcrumbtour.R;
import info.sorgedev.breadcrumbtour.R.id;
import info.sorgedev.breadcrumbtour.R.layout;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;

public class SearchFragment extends Fragment {
	
	private Spinner sCity;
	private SeekBar sbMinimumTime;
	private RatingBar rbRating;
	private Button bSearch;
	
	private View root;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_search, container, false);
		return root;
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		sCity = (Spinner) getActivity().findViewById(R.id.sCity);
		sbMinimumTime = (SeekBar) getActivity().findViewById(R.id.sbMinimumTime);
		rbRating = (RatingBar) getActivity().findViewById(R.id.rbRating);
		bSearch = (Button) getActivity().findViewById(R.id.bSearchTour);
		
		bSearch.setOnClickListener((OnClickListener) getActivity());
	}
	
	

	
	
}
