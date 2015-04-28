package info.sorgedev.breadcrumbtour;

import info.sorgedev.breadcrumbtour.obj.Trail;
import info.sorgedev.breadcrumbtour.ui.SearchFragment;
import info.sorgedev.breadcrumbtour.ui.SearchResultFragment;
import info.sorgedev.breadcrumbtour.ui.SearchResultFragment.SelectionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SearchActivity extends Activity implements OnClickListener, SelectionListener {
	
	public static final String TRAIL = "Trail";
	
	private List<Trail> trails;
	
	private final SearchFragment searchFragment = new SearchFragment();;
	private final SearchResultFragment searchResultFragment = new SearchResultFragment();;

	FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);		
		
		fragmentManager = getFragmentManager();
		trails = new ArrayList<Trail>();
		
		if(savedInstanceState == null) {
			//TODO odkomentowaæ po zrobieniu fragmentu
			fragmentManager.beginTransaction().add(R.id.container, searchFragment).commit();
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO zrobiæ metodê siêgaj¹c¹ do bazy
		
		//TODO usun¹æ sztuczne robienie Trail
		Trail tmpTrail = new Trail();
		tmpTrail.setName("Following Krak");
		tmpTrail.setMinTime(new Date(60*60*1000));
		tmpTrail.setRating(4);
		tmpTrail.setImageUri(Uri.parse("imgs/filler2_land.jpg"));
		if(trails.isEmpty())
			trails.add(tmpTrail);
		
		if(!searchResultFragment.isAdded()) {
			fragmentManager.beginTransaction().replace(R.id.container, searchResultFragment).addToBackStack(null).commit();
			fragmentManager.executePendingTransactions();
			
		}
		
	}

	@Override
	public void onItemSelected(int position) {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent(SearchActivity.this, TrailMainActivity.class);
		intent.putExtra(TRAIL, trails.get(position));
		startActivity(intent);
	}
	
	public List<Trail> getTrails() {
		return trails;
	}

}
