package org.wolink.app.appcategory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.wolink.app.appcategory.Category.Items;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

public class CategoryInfo {
    
    public static final long PARENT_ROOT = -1;
    
    public static final int ITEM_CATEGORY = 0;
    public static final int ITEM_APPLICATION = 1;
    public static final int ITEM_SHORTCUT = 2;
    public static final int ITEM_BOOKMARK = 3;
	
	public String title;
	public Bitmap icon;
	private int type;
	public long parent;
	private Intent intent;
	
	public Drawable dr;
	public boolean filtered;
	public ComponentName comName;
	
	private CategoryInfo() {
	}
	
	public static CategoryInfo BuildCategory(String title) {
		CategoryInfo ci = new CategoryInfo();
		
		ci.title = title;
		ci.type = ITEM_CATEGORY;
		ci.parent = PARENT_ROOT; 
		
		return ci;
	}
	
	public static CategoryInfo BuildApplication(ResolveInfo info,  PackageManager manager, long parent){
		CategoryInfo ci = new CategoryInfo();
		
		ci.type = ITEM_APPLICATION;
        ci.title = info.loadLabel(manager).toString();
        ci.intent = new Intent(Intent.ACTION_MAIN);
        ci.intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ci.intent.setComponent(new ComponentName(
              info.activityInfo.applicationInfo.packageName,
              info.activityInfo.name));
        ci.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        ci.dr = info.activityInfo.loadIcon(manager);
        ci.parent = parent;
        
		return ci;
	}
	
	public static CategoryInfo BuildShortcut(ResolveInfo info,  PackageManager manager){
		CategoryInfo ci = new CategoryInfo();
		
		ci.title = info.loadLabel(manager).toString();
        ci.comName = new ComponentName(
              info.activityInfo.applicationInfo.packageName,
              info.activityInfo.name);
        ci.dr = info.activityInfo.loadIcon(manager);
        
		return ci;
	}
	
	public static CategoryInfo BuildShortcut(Context context, Intent data, long parent){
		CategoryInfo ci = new CategoryInfo();
		
		ci.type = ITEM_SHORTCUT;
		ci.intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		ci.title = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = (Bitmap)bitmap;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = CategoryUtils.createIconBitmap(resources.getDrawable(id), context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }		
		
		ci.parent = parent;
		ci.icon = icon; 
        
		return ci;
	}	
	
	public void setIcon(Bitmap bm) {
		icon = bm;
	}
	
    void onAddToDatabase(ContentValues values) { 
        values.put(Items.TITLE, title);
        values.put(Items.PARENT, parent);
        values.put(Items.TYPE, type);
        
        if (icon != null) {
        	byte[] data = flattenBitmap(icon);
        	values.put(Items.ICON, data);
        }
        
        if (intent != null) {
        	values.put(Items.INTENT, intent.toUri(0));
        }
    }	
    
    public static byte[] flattenBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {           
            return null;
        }
    }
    
    public static Bitmap getIconFromCursor(Cursor c, int iconIndex) {
        byte[] data = c.getBlob(iconIndex);
        try {
        	Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            bm.setDensity(240);
            return bm;
        } catch (Exception e) {
            return null;
        }
    }
}
