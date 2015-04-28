package info.sorgedev.breadcrumbtour.log;

import info.sorgedev.breadcrumbtour.R;
import info.sorgedev.breadcrumbtour.R.id;
import info.sorgedev.breadcrumbtour.R.layout;
import info.sorgedev.breadcrumbtour.obj.Trail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchResultListAdapter extends ArrayAdapter<Trail> {
	
	private Context context;

	public SearchResultListAdapter(Context context, int resource,
			List<Trail> objects) {
		super(context, resource, objects);
		this.context = context;
	}
	
	private class ViewHolder {
		ImageView ivTrailIconSearch;
		TextView tvTrailNameSearch;
		TextView tvTimeSearch;
		TextView tvRatingSearch;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Trail trail = (Trail) getItem(position);
		View view = null;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if(convertView == null) {
			view = inflater.inflate(R.layout.fragment_search_results, null);
			
			holder = new ViewHolder();
			holder.ivTrailIconSearch = (ImageView) view.findViewById(R.id.ivTrailIconSearch);
			holder.tvTrailNameSearch = (TextView) view.findViewById(R.id.tvTrailNameSearch);
			holder.tvTimeSearch = (TextView) view.findViewById(R.id.tvTimeSearch);
			holder.tvRatingSearch = (TextView) view.findViewById(R.id.tvRatingSearch);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		AssetManager assetManager = context.getAssets();
		InputStream inputImage = null;
		try {
			inputImage = assetManager.open(trail.getImageUri().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap trailIcon = BitmapFactory.decodeStream(inputImage);
		holder.ivTrailIconSearch.setImageBitmap(trailIcon);
		holder.tvTrailNameSearch.setText(trail.getName());
		holder.tvTimeSearch.setText(trail.getMinTime().toString()); //TODO zrobiæ dobry string
		holder.tvRatingSearch.setText(String.valueOf(trail.getRating()));
		
		return view;
	}
	
	

}
