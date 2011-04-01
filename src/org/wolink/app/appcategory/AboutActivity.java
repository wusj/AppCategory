package org.wolink.app.appcategory;

import com.mobclick.android.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class AboutActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
        ((TextView)findViewById(R.id.title)).setText(R.string.about);
        Button btnBack = (Button)findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				return;
			}
		});		
        findViewById(R.id.add).setVisibility(View.INVISIBLE);
        findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.openFeedbackActivity(AboutActivity.this);
				//CategoryUtils.genAppMainIcon(AboutActivity.this);
			}
		});
        
        TextView txtv_main_title = (TextView)findViewById(R.id.txtv_main_title);
        try {
        	txtv_main_title.setText(String.format(getString(R.string.about_title), 
        		getString(R.string.app_name),
        		this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        }
        catch (Throwable t) {
        	
        }
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this); 
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this); 
	}
}
