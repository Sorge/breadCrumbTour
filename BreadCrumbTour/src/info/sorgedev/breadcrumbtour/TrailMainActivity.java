package info.sorgedev.breadcrumbtour;

import info.sorgedev.breadcrumbtour.obj.Trail;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TrailMainActivity extends Activity implements OnClickListener {
	
	private ImageView ivTrailImage;
	private TextView tvTrailName;
	private TextView tvTrailTime;
	private TextView tvHintText;
	private Button bPickCrumb;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trail);
		
		ivTrailImage = (ImageView) findViewById(R.id.ivTrailIcon);
		tvTrailName = (TextView) findViewById(R.id.tvTrailName);
		tvTrailTime = (TextView) findViewById(R.id.tvTime);
		tvHintText = (TextView) findViewById(R.id.tvHintText);
		bPickCrumb = (Button) findViewById(R.id.bPickCrumb);
		
		bPickCrumb.setOnClickListener(this);
		
		Intent intent = getIntent();
		Trail trail = (Trail) intent.getParcelableExtra(SearchActivity.TRAIL);
		
		ivTrailImage.setImageURI(trail.getImageUri());
		tvTrailName.setText(trail.getName());
		tvTrailTime.setText(trail.getMinTime().toString());
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(TrailMainActivity.this, ImageTargetActivity.class);
//		intent.setClassName(getPackageName(), "info.sorgedev.breadcrumbtour.ImageTargetActivity");
		startActivity(intent);
	}

}
