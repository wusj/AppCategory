package org.wolink.app.appcategory;

import org.wolink.app.appcategory.Category.Items;

import com.mobclick.android.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CategoryDesktopActivity extends Activity implements OnItemClickListener {
	
	private CategorysListAdapter mAdapter;
    private GridView mGridView;
    private long mCurCategoryId;
    
    private int mTitleIdx;
    private int mIconIdx;
    private int mTypeIdx;
    private int mIntentIdx;
    private int mUrlIdx;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);       
        setContentView(R.layout.categorydesktop);
        
        Intent intent = this.getIntent();
        
        Uri itemUri = intent.getData();
        if (itemUri != null) {
        	try {
	        	Cursor c = managedQuery(itemUri, null, null, null, null);
	        	c.moveToFirst();
	        	int typeIdx = c.getColumnIndexOrThrow(Items.TYPE);
	        	int type = c.getInt(typeIdx);
	        	if (type != CategoryInfo.ITEM_CATEGORY) {
	        		finish();
	        		return;
	        	}
	        	int titleIdx = c.getColumnIndexOrThrow(Items.TITLE);
	        	String title = c.getString(titleIdx);
	        	((TextView)findViewById(R.id.title)).setText(title);
        	} catch (Exception e){
        		finish();
        		return;
        	}
        	mCurCategoryId = ContentUris.parseId(itemUri);
        }
        else {
        	finish();
        	return;
        }
        
        mGridView = (GridView)findViewById(R.id.categorys);
        
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
	        	return;
			}
		});

        findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setClassName(CategoryDesktopActivity.this, "org.wolink.app.appcategory.CategoryBrowserActivity");
				startActivity(intent);
				finish();
	        	return;
			}
		});
        
        Cursor cursor = getCategoryCursor(mCurCategoryId);
        mAdapter = new CategorysListAdapter(this, R.layout.item, cursor);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnCreateContextMenuListener(this);
        mGridView.setOnItemClickListener(this);
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
	
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        finish();
        return true;
    }
    
	private Cursor getCategoryCursor(long root) {
    	
        //final ContentResolver cr = getContentResolver();
        Cursor c = managedQuery(Category.Items.CONTENT_URI,
            null,
            Category.Items.PARENT + "=?",
            new String[] { String.valueOf(root) }, null);
        
        if (c != null) {
        	mTitleIdx = c.getColumnIndexOrThrow(Items.TITLE);
        	mIconIdx = c.getColumnIndexOrThrow(Items.ICON);
        	mTypeIdx = c.getColumnIndexOrThrow(Items.TYPE);
        	mIntentIdx = c.getColumnIndexOrThrow(Items.INTENT);
        	mUrlIdx = c.getColumnIndexOrThrow(Items.URL);
        }

        return c;
    }
    
    class CategorysListAdapter extends SimpleCursorAdapter {
 
		public CategorysListAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, new String[] {}, new int[] {});
			// TODO Auto-generated constructor stub
			
		}       

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = super.newView(context, cursor, parent);
			ViewHolder vh = new ViewHolder();
			vh.shortcut = (TextView)v.findViewById(R.id.label);
			vh.icon = (ImageView)v.findViewById(R.id.icon);
			v.setTag(vh);
			return v;
		}		
		
        @Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
        	ViewHolder vh = (ViewHolder)view.getTag();
        	vh.shortcut.setText(cursor.getString((mTitleIdx)));
        	vh.icon.setImageBitmap(CategoryInfo.getIconFromCursor(cursor, mIconIdx));
		}

		class ViewHolder {
			TextView shortcut;
			ImageView icon;
        }
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

        Cursor cursor = (Cursor) mAdapter.getItem(position);
        if (cursor == null) {
            return;
        }
        
        int item_type = cursor.getInt(mTypeIdx);
        if (item_type == CategoryInfo.ITEM_SHORTCUT ||
        		item_type == CategoryInfo.ITEM_APPLICATION) {
        	try {
        		String uri = cursor.getString(mIntentIdx);
        		Intent intent = Intent.parseUri(uri, 0);
        		
                int[] pos = new int[2];
                v.getLocationOnScreen(pos);
                intent.setSourceBounds(new Rect(pos[0], pos[1],
                        pos[0] + v.getWidth(), pos[1] + v.getHeight()));       		
        		
        		startActivity(intent);
        	} catch (ActivityNotFoundException e) {
        		// to be fix: install from network
        		final String url = cursor.getString(mUrlIdx);
        		if (TextUtils.isEmpty(url)) {
        			Toast.makeText(this, R.string.toast_intent_not_found, Toast.LENGTH_SHORT).show();
        		} else {
            		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        			builder.setTitle(cursor.getString(mTitleIdx));
        			builder.setMessage(R.string.prompt_install);
        			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialoginterface, int i) {
        					   Uri uri = Uri.parse(url);          
        					   Intent it = new Intent(Intent.ACTION_VIEW, uri);          
        					   startActivity(it);  
        					}				
        				});
        			builder.setNegativeButton(android.R.string.cancel, null);
        			builder.show();        			
        		}
        	} catch (SecurityException e) {
        		Toast.makeText(this, R.string.toast_permission_deny, Toast.LENGTH_SHORT).show();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	
        }
	}
}