package org.wolink.app.appcategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mobclick.android.MobclickAgent;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class AppBrowserActivity extends ListActivity implements OnClickListener {

	private static ArrayList<CategoryInfo> mApplications;
	private ListView mListView;
	
	private long mParentId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mParentId = ContentUris.parseId(getIntent().getData());
		setContentView(R.layout.appbroswer);		
		
		loadApplications();
		
		
		setListAdapter(new ApplicationsAdapter(this, mApplications));
		
		mListView = getListView();
		mListView.setItemsCanFocus(false);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		Button b = (Button)findViewById(R.id.ok);
		b.setOnClickListener(this);
		b = (Button)findViewById(R.id.cancel);
		b.setOnClickListener(this);
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
    private void loadApplications() {

        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        
        if (apps != null) {
        	Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

            final int count = apps.size();

            if (mApplications == null) {
                mApplications = new ArrayList<CategoryInfo>(count);
            }
            mApplications.clear();

            for (int i = 0; i < count; i++) {
            	CategoryInfo application = CategoryInfo.BuildApplication(apps.get(i), manager, mParentId);

                mApplications.add(application);
            }
        }
    }
    
    private class ApplicationsAdapter extends ArrayAdapter<CategoryInfo> {	
        public ApplicationsAdapter(Context context, ArrayList<CategoryInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CategoryInfo info = mApplications.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice , parent, false);
            }

            Drawable icon = info.dr;
            
            if (!info.filtered) {
                icon = info.dr = new BitmapDrawable(getResources(), CategoryUtils.createIconBitmap(icon, AppBrowserActivity.this));
                info.filtered = true;
            }
            
            final TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setCompoundDrawablePadding(10);
            textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            textView.setText(info.title);

            return convertView;
        }
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			SparseBooleanArray sba = mListView.getCheckedItemPositions();
			if (sba != null) {
				List<CategoryInfo> lci = new ArrayList<CategoryInfo>();
				for(int i = 0; i < sba.size(); i++) {
					int pos = sba.keyAt(i);
					if (sba.get(pos)) {
						CategoryInfo app = mApplications.get(pos);
						app.setIcon(((BitmapDrawable)app.dr).getBitmap());
						lci.add(app);
					}
				}
				if (lci.size() > 0) {
					CategoryUtils.addBulkApplication(this, lci);
				}
			}
			finish();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}
}
