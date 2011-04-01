package org.wolink.app.appcategory;

import org.wolink.app.appcategory.Category.Items;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.mobclick.android.MobclickAgent;
import com.mobclick.android.ReportPolicy;

public class CategoryBrowserActivity extends Activity implements OnItemClickListener, OnClickListener {	
	private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
    
	private static final int MENU_ADD_CATEGORY = Menu.FIRST + 1;
	private static final int MENU_ADD_APPLICATION = Menu.FIRST + 2;
	private static final int MENU_ADD_BOOKMARK = Menu.FIRST + 3;
	private static final int MENU_ADD_SHORTCUT = Menu.FIRST + 4;
	private static final int MENU_SETTING = Menu.FIRST + 5;
	private static final int MENU_ABOUT = Menu.FIRST + 6;
	
	
	private static final int MENU_ITEM_DELETE = Menu.FIRST + 10;
	private static final int MENU_ITEM_RENAME = Menu.FIRST + 11;
	private static final int MENU_ITEM_TO_HOME = Menu.FIRST + 12;
	
	private static final int DIALOG_SELECT_ADD_ITEM = 1;
	
	private CategorysListAdapter mAdapter;
    private GridView mGridView;
    private long mCurCategoryId;
    
    private int mTitleIdx;
    private int mIconIdx;
    private int mTypeIdx;
    private int mIntentIdx;
    private int mIsSystemIdx;
    private int mUrlIdx;
    
    private boolean mCreateLiveFolder = false;
    private boolean mCreateShortcut = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        MobclickAgent.setReportPolicy(ReportPolicy.DAILY);
        MobclickAgent.update(this);
        
        setContentView(R.layout.categorybroswer);
        TextView tvTitle = (TextView)findViewById(R.id.title);
        Button btnBack = (Button)findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				return;
			}
		});
        
        Button btnAdd = (Button)findViewById(R.id.add);
        btnAdd.setOnClickListener(this);
        
        Intent intent = this.getIntent();
        String action = intent.getAction();
        if (action != null) {
	        if (action.equals(LiveFolders.ACTION_CREATE_LIVE_FOLDER)) {
	        	mCreateLiveFolder = true;
	        	btnAdd.setVisibility(View.INVISIBLE);
	        } else if (action.equals(Intent.ACTION_CREATE_SHORTCUT)) {
	        	mCreateShortcut = true;
	        	btnAdd.setVisibility(View.INVISIBLE);
	        }        
        }
        
        Uri itemUri = intent.getData();
        if (itemUri != null) {
        	Cursor c = managedQuery(itemUri, null, null, null, null);
        	if (c == null) {
        		finish();
        		return;
        	}
        	c.moveToFirst();
        	int typeIdx = c.getColumnIndexOrThrow(Items.TYPE);
        	int type = c.getInt(typeIdx);
        	if (type != CategoryInfo.ITEM_CATEGORY) {
        		finish();
        		return;
        	}
        	int titleIdx = c.getColumnIndexOrThrow(Items.TITLE);
        	String title = c.getString(titleIdx);
        	tvTitle.setText(title);
        	mCurCategoryId = ContentUris.parseId(itemUri);
        }
        else {
        	mCurCategoryId = CategoryInfo.PARENT_ROOT;
        	tvTitle.setText(R.string.app_title);
        	btnBack.setVisibility(View.INVISIBLE);
        }
        
        mGridView = (GridView)findViewById(R.id.categorys);
        
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
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mCreateLiveFolder || mCreateShortcut) 
			return true;
		
		if (mCurCategoryId == CategoryInfo.PARENT_ROOT) {
			menu.add(Menu.NONE, MENU_ADD_CATEGORY, Menu.NONE, R.string.new_folder)
				.setIcon(android.R.drawable.ic_menu_add);
		} else {
			menu.add(Menu.NONE, MENU_ADD_APPLICATION, Menu.NONE, R.string.add_application)
				.setIcon(android.R.drawable.ic_menu_add);
//			menu.add(Menu.NONE, MENU_ADD_BOOKMARK, Menu.NONE, R.string.add_bookmark)
//				.setIcon(android.R.drawable.ic_menu_add);
			menu.add(Menu.NONE, MENU_ADD_SHORTCUT, Menu.NONE, R.string.add_shortcut)
				.setIcon(android.R.drawable.ic_menu_add);

		}
		
//		menu.add(Menu.NONE, MENU_SETTING, Menu.NONE, R.string.menu_setting)
//			.setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about)
			.setIcon(android.R.drawable.ic_menu_info_details);
		
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case MENU_ADD_CATEGORY:
			addCategory();
			break;
		case MENU_ADD_APPLICATION:
			addApplication();
			break;
		case MENU_ADD_BOOKMARK:
			addBookmark();
			break;
		case MENU_ADD_SHORTCUT:
			addShortcut();
			break;
		case MENU_SETTING:
			break;
		case MENU_ABOUT:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);	
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		if (mCreateLiveFolder || mCreateShortcut) 
			return;
		
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
           
            return;
        }

        Cursor cursor = (Cursor) mAdapter.getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        menu.setHeaderTitle(cursor.getString(mTitleIdx));
        boolean isSystem = cursor.getInt(mIsSystemIdx) == 0 ? false : true;
        int type = cursor.getInt(mTypeIdx);
 
        MenuItem m = menu.add(Menu.NONE, MENU_ITEM_DELETE, Menu.NONE, R.string.menu_delete);
        if (isSystem) m.setEnabled(false);
        
        if (type == CategoryInfo.ITEM_CATEGORY) {
            menu.add(Menu.NONE, MENU_ITEM_RENAME, Menu.NONE, R.string.menu_rename);
            menu.add(Menu.NONE, MENU_ITEM_TO_HOME, Menu.NONE, R.string.menu_to_home);        	
        }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }
        
        Cursor cursor = (Cursor) mAdapter.getItem(info.position);
       
        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: 
             	final long item_id = info.id;
            	if (mCurCategoryId == CategoryInfo.PARENT_ROOT) {
            		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        			builder.setTitle(cursor.getString(mTitleIdx));
        			builder.setMessage(R.string.prompt_del);
        			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialoginterface, int i) {
        						//Log.e("onclick", "delete " + item_id);
        						CategoryUtils.delCategory(CategoryBrowserActivity.this, item_id);
        					}				
        				});
        			builder.setNegativeButton(android.R.string.cancel, null);
        			builder.show();
            	} else {
            		CategoryUtils.delShortcut(this, info.id, mCurCategoryId);
            	}
                return true;
            case MENU_ITEM_RENAME:
                Uri itemUri = ContentUris.withAppendedId(Category.Items.CONTENT_URI, info.id);
                Intent intent = new Intent(this, CategoryEditor.class);
    			intent.setData(itemUri);
    			startActivity(intent);           	
            	return true;
            case MENU_ITEM_TO_HOME:
            	setupShortcut(cursor, info.id, false);
            	return true;
        }
        return false;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Log.e("goooooddd", "onCreateDialog " + id);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch (id) {
		case DIALOG_SELECT_ADD_ITEM:
			builder.setTitle(R.string.add_item);
			builder.setItems(R.array.add_item,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						switch (i) {
						case 0:
							addApplication();
							break;
//						case 1:
//							addBookmark();
//							break;
						case 1:
							addShortcut();
							break;
						}
					}
				});
			break;
		}
		
		return builder.create();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		return onCreateDialog(id, null);
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
        	mIsSystemIdx = c.getColumnIndexOrThrow(Items.ISSYSTEM);
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
        
		if (mCreateLiveFolder) {
			setupLiveFolder(cursor);
			finish();
			return;
		} else if (mCreateShortcut) {
			setupShortcut(cursor, id, true);
			finish();
			return;
		}
        
        int item_type = cursor.getInt(mTypeIdx);
        if (item_type == CategoryInfo.ITEM_CATEGORY) {
			Uri itemUri = ContentUris.withAppendedId(Items.CONTENT_URI, id);
			Intent intent = new Intent(this, CategoryBrowserActivity.class);
			intent.setData(itemUri);
			startActivity(intent);           			
        } else if (item_type == CategoryInfo.ITEM_SHORTCUT ||
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
	
	private void setupLiveFolder(Cursor cursor) {
        // Build the live folder intent.
        final Intent liveFolderIntent = new Intent();

        liveFolderIntent.setData(Items.CONTENT_URI);
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME,
                cursor.getString(mTitleIdx));
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON,
                CategoryInfo.getIconFromCursor(cursor, mIconIdx));
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                LiveFolders.DISPLAY_MODE_GRID);

        // The result of this activity should be a live folder intent.
        setResult(RESULT_OK, liveFolderIntent);
	}
	
	private void setupShortcut(Cursor cursor, long id, boolean passive) {

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, "org.wolink.app.appcategory.CategoryDesktopActivity");
        shortcutIntent.setData(ContentUris.withAppendedId(Items.CONTENT_URI, id));

        // Then, set up the container intent (the response to the caller)
        
        Bitmap bm = CategoryInfo.getIconFromCursor(cursor, mIconIdx);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, cursor.getString(mTitleIdx));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bm);

        if (passive) {
        	setResult(RESULT_OK, intent);
        } else {
            intent.putExtra(EXTRA_SHORTCUT_DUPLICATE, false);    
        	intent.setAction(ACTION_INSTALL_SHORTCUT);
        	sendBroadcast(intent);  	
        }
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add:
			if (mCurCategoryId == CategoryInfo.PARENT_ROOT) {
				addCategory();
			} else {
				showDialog(DIALOG_SELECT_ADD_ITEM);
			}
			break;
		}
	}

	private void addCategory() {
		Intent intent = new Intent(this, CategoryEditor.class);
		startActivity(intent);		
	}
	
	private void addApplication() {
		Intent intent = new Intent(this, AppBrowserActivity.class);
		intent.setData(getIntent().getData());
		startActivity(intent);		
	}

	private void addBookmark(){
		
	}

	private void addShortcut(){
		Intent intent = new Intent(this, ShortcutBrowserActivity.class);
		intent.setData(getIntent().getData());
		startActivity(intent);			
	}
}