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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ShortcutBrowserActivity extends ListActivity implements OnItemClickListener  {

	private static ArrayList<CategoryInfo> mShortcuts;
	private ListView mListView;
	
	private long mParentId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mParentId = ContentUris.parseId(getIntent().getData());
		setContentView(R.layout.shortcutbroswer);		
		
		loadShortcuts();
		
		setListAdapter(new ApplicationsAdapter(this, mShortcuts));
		
		mListView = getListView();
		mListView.setItemsCanFocus(false);
		mListView.setOnItemClickListener(this);
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

    private void loadShortcuts() {

        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT, null);
        //mainIntent.addCategory(Intent.CATEGORY_DEFAULT);

        final List<ResolveInfo> shortcuts = manager.queryIntentActivities(mainIntent, 0);
        
        if (shortcuts != null) {
        	Collections.sort(shortcuts, new ResolveInfo.DisplayNameComparator(manager));

            final int count = shortcuts.size();

            if (mShortcuts == null) {
            	mShortcuts = new ArrayList<CategoryInfo>(count);
            }
            mShortcuts.clear();

            for (int i = 0; i < count; i++) {
            	ResolveInfo info = shortcuts.get(i);
            	if (info.activityInfo.packageName.equals(this.getPackageName())) {
            		continue;
            	}
            	CategoryInfo shortcut = CategoryInfo.BuildShortcut(info, manager);
            	mShortcuts.add(shortcut);
            }
        }
    }
    
    private class ApplicationsAdapter extends ArrayAdapter<CategoryInfo> {
    	
        public ApplicationsAdapter(Context context, ArrayList<CategoryInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CategoryInfo info = mShortcuts.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1 , parent, false);
            }

            Drawable icon = info.dr;
            
            if (!info.filtered) {
                icon = info.dr = new BitmapDrawable(getResources(), CategoryUtils.createIconBitmap(icon, ShortcutBrowserActivity.this));
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
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		CategoryInfo info = mShortcuts.get(position);
		
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);		
        intent.setComponent(info.comName);
        
        startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			CategoryInfo ci = CategoryInfo.BuildShortcut(this, data, mParentId);
			if (ci.icon == null) {
				ci.setIcon(((BitmapDrawable)getResources().getDrawable(R.drawable.default_folder)).getBitmap());
			}
			CategoryUtils.addShortcut(this, ci);
			finish();
		}
	}

}
