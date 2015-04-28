package info.sorgedev.breadcrumbtour.ui;

import info.sorgedev.breadcrumbtour.R;
import info.sorgedev.breadcrumbtour.SearchActivity;
import info.sorgedev.breadcrumbtour.R.layout;
import info.sorgedev.breadcrumbtour.log.SearchResultListAdapter;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SearchResultFragment extends ListFragment {

	private SearchResultListAdapter listAdapter;
	private SearchActivity parentActivity;
	
	SelectionListener selectionListener = null;
    private int currentIndex = -1;

    public interface SelectionListener {
        public void onItemSelected(int position);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		View view = inflater.inflate(R.layout.fragment_search_results, container);
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if(parentActivity != null) {
			listAdapter = new SearchResultListAdapter(parentActivity, R.layout.fragment_search_results, parentActivity.getTrails());
			setListAdapter(listAdapter);
		}
	}

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        currentIndex = position;
        getListView().setItemChecked(position, true);
        selectionListener.onItemSelected(position);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (SearchActivity) activity;
        try {
            selectionListener = (SelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                "must implement SelectionListener");
        }
    }

	@Override
	public void onDetach() {
		super.onDetach();
		parentActivity = null;
	}  
    
	
}
