package org.wolink.app.appcategory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.wolink.app.appcategory.Category.Items;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Environment;

public class CategoryUtils {
	private static final Canvas sCanvas = new Canvas();
	private static final Rect sOldBounds = new Rect();
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    
    private static final String[] sLauncher = new String[] {
    	"com.htc.launcher.settings",
    	"com.android.launcher2.settings",
    	"com.android.launcher.settings"
    };
    private static Uri sLauncherUri;
    private static boolean sInitStatics = false;
    
	private static final String LAUNCHER_TABLE_FAVORITES = "favorites";
//    private static final Uri LAUNCHER_CONTENT_URI = Uri.parse("content://" +
//    		LAUNCHER_AUTHORITY + "/" + LAUNCHER_TABLE_FAVORITES);
    
	
	public CategoryUtils() {
		
	}
	
	public static void addCategory(Context context, String title) {
        final ContentResolver cr = context.getContentResolver();
        CategoryInfo ci = CategoryInfo.BuildCategory(title);
        ci.setIcon(((BitmapDrawable)context.getResources().getDrawable(R.drawable.default_folder)).getBitmap());
        ContentValues values = new ContentValues();
        ci.onAddToDatabase(values);
        cr.insert(Items.CONTENT_URI, values);	
	}
	
	public static void delCategory(Context context, long id) {
		final ContentResolver cr = context.getContentResolver();
		Uri itemUri = ContentUris.withAppendedId(Items.CONTENT_URI, id);
		
		cr.delete(Items.CONTENT_URI, Items.PARENT + "=?", new String[] {String.valueOf(id)});
		cr.delete(itemUri, null, null);
		
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(context, "org.wolink.app.appcategory.CategoryDesktopActivity");
        shortcutIntent.setData(itemUri);
        
        launcherDelete(context, "intent=?", new String[]{shortcutIntent.toUri(0)});
	}
	
	public static void renameCategory(Context context, long id, String title) {
		final ContentResolver cr = context.getContentResolver();
		Uri itemUri = ContentUris.withAppendedId(Items.CONTENT_URI, id);
		
        ContentValues values = new ContentValues();
        values.put(Items.TITLE, title);
        cr.update(itemUri, values, null, null); 
        
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(context, "org.wolink.app.appcategory.CategoryDesktopActivity");
        shortcutIntent.setData(ContentUris.withAppendedId(Items.CONTENT_URI, id));
        

	    values = new ContentValues();
	    values.put("title", title);  
	    launcherUpdate(context, values, "intent=?", new String[]{shortcutIntent.toUri(0)});
//	    cr.update(LAUNCHER_CONTENT_URI, values, "intent=?", new String[]{shortcutIntent.toUri(0)});         
	}
	
	public static void delShortcut(Context context, long id, long parent) {
		Uri itemUri = ContentUris.withAppendedId(Items.CONTENT_URI, id);
		context.getContentResolver().delete(itemUri, null, null);
	    CategoryUtils.updateCategoryIcon(context, parent);		
	}
	
	public static void addBulkApplication(Context context, List<CategoryInfo> apps){
		final ContentResolver cr = context.getContentResolver();
		ContentValues[] values = new ContentValues[apps.size()];
		
		for(int i = 0; i < apps.size(); i++) {
			ContentValues value = new ContentValues();
			apps.get(i).onAddToDatabase(value);
			values[i] = value;
		}
		
		cr.bulkInsert(Items.CONTENT_URI, values);
		
		updateCategoryIcon(context, apps.get(0).parent);
	}
	
	public static void addShortcut(Context context, CategoryInfo ci) {
		final ContentResolver cr = context.getContentResolver();
		ContentValues values = new ContentValues();
		
		ci.onAddToDatabase(values);
		
		cr.insert(Items.CONTENT_URI, values);
		
		updateCategoryIcon(context, ci.parent);		
	}

	public static void updateCategoryIcon(Context context, long id) {
		final ContentResolver cr = context.getContentResolver();
		
		Cursor c = cr.query(Items.CONTENT_URI, new String[] {Items._ID, Items.ICON}, 
				Items.PARENT + "=?", new String[] {String.valueOf(id)}, null);
		Bitmap bm;
		
		try { 
			c.moveToFirst();
	        int iconIdx = c.getColumnIndexOrThrow(Items.ICON);
	        
	        bm = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);	  
	        bm.setDensity(240);
	        Canvas canvas = new Canvas(bm);
	        Drawable d1 = context.getResources().getDrawable(R.drawable.icon_back);
        	d1.setBounds(new Rect(0, 0, 72, 72));
        	d1.draw(canvas);
	        
	        int index = 0;
	        do {
	        	int x = 4 + 20 * (index % 3) + 2 * (index % 3);
	        	int y = 4 + 20 * (index / 3) + 2 * (index/3);
	        	
	        	Bitmap b = CategoryInfo.getIconFromCursor(c, iconIdx);
	        	canvas.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), 
	        			new Rect(x, y, x+20, y+20), null);
	        	index++;
	        } while (c.moveToNext() && index < 9);
	        c.close();
		} catch (Exception e) {
			bm = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.default_folder)).getBitmap();
		}	        
		
	    ContentValues values = new ContentValues();
	    byte[] bmBytes = CategoryInfo.flattenBitmap(bm);
	    values.put(Items.ICON, bmBytes);
	    cr.update(ContentUris.withAppendedId(Items.CONTENT_URI, id), values, null, null);
	        
	    updateShortcutIcon(context, id, bm);
	}
	
	private static void updateShortcutIcon(Context context, long id, Bitmap bm) {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(context, "org.wolink.app.appcategory.CategoryDesktopActivity");
        shortcutIntent.setData(ContentUris.withAppendedId(Items.CONTENT_URI, id));
        
//        Cursor c = (Cursor)launcherOperation(context, LAUNCHER_OPERATION.QUERY, null, new String[] { "title" },
//        		"intent=?", new String[]{shortcutIntent.toUri(0)});

//        Cursor c = cr.query(LAUNCHER_CONTENT_URI, new String[] { "title" },
//        		"intent=?", new String[]{shortcutIntent.toUri(0)}, null);
        try {
        	//c.moveToFirst();
            if (sIconWidth == -1) {
                initStatics(context);
            }
            if (bm.getWidth() != sIconWidth) {
        		bm = Bitmap.createScaledBitmap(bm, sIconWidth, sIconHeight, false);
            }
	        ContentValues values = new ContentValues();
	        values.put("icon", CategoryInfo.flattenBitmap(bm));  
//	        cr.update(LAUNCHER_CONTENT_URI, values, "intent=?", new String[]{shortcutIntent.toUri(0)}); 
	        launcherUpdate(context, values, "intent=?", new String[]{shortcutIntent.toUri(0)});
        } finally {
        	//c.close();
        }
	}
	
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;
            Bitmap originBitmap = null;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                originBitmap = bitmapDrawable.getBitmap();
                if (originBitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();

            if (sourceWidth > 0 && sourceWidth > 0) {
                // There are intrinsic sizes.
                if (width != sourceWidth && height != sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                    Bitmap bm= Bitmap.createScaledBitmap(originBitmap, width, height, false);
                    return bm;
                } else {
                	return originBitmap;
                }
            }

            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            sOldBounds.set(icon.getBounds());
            icon.setBounds(0, 0, width, height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);

            return bitmap;
        }
    }
    

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();

        sIconWidth = sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        
        final ContentResolver cr = context.getContentResolver();
               
        for(String launcher : sLauncher) {
	        Uri uri = Uri.parse("content://" + launcher + "/" + LAUNCHER_TABLE_FAVORITES);
	        try {
	        	ContentValues values = new ContentValues();
	        	values.put("test_test_test", 0);
	        	cr.update(uri, values, "_id=?", new String[] { String.valueOf(-1)});
	        	sLauncherUri = uri;
	        	break;
	        } catch (IllegalArgumentException e) {
	        	//e.printStackTrace();
	        	continue;
	        } catch (Exception e) {
	        	sLauncherUri = uri;
	        	break;	        	
	        }
        }
        
        sInitStatics = true;
    }
    
	public static void genAppMainIcon(Context context) {
		Bitmap bm = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);	  
		Canvas canvas = new Canvas(bm);
		Drawable d1 = context.getResources().getDrawable(R.drawable.icon_back);
		d1.setBounds(new Rect(0, 0, 72, 72));
		d1.draw(canvas);
		
		d1 = context.getResources().getDrawable(R.drawable.default_folder);
		d1.setBounds(new Rect(0, 0, 72, 72));
		d1.draw(canvas);
		    	        
	    byte[] bmBytes = CategoryInfo.flattenBitmap(bm);
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        // We can read and write the media
	    	File rootDir = Environment.getExternalStorageDirectory();
	    	File file = new File(rootDir.getPath() + File.separator + "icon.png");
	    	try {
	    		file.createNewFile();
	    		FileOutputStream fos = new FileOutputStream(file);
				fos.write(bmBytes);
				fos.close();
	    	} catch (Exception e) {
	    		
	    	}
	    }
	}    
	
	public static void launcherUpdate(Context context, ContentValues values, 
			String where, String[] selectionArgs) {
		
        if (sInitStatics == false) {
        	initStatics(context);
        }
        
        if (sLauncherUri != null) {
        	final ContentResolver cr = context.getContentResolver();
        	try {
        		cr.update(sLauncherUri, values, where, selectionArgs); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }		
	}

	public static void launcherDelete(Context context, String where, String[] selectionArgs) {
		
        if (sInitStatics == false) {
        	initStatics(context);
        }
        
        if (sLauncherUri != null) {
        	final ContentResolver cr = context.getContentResolver();
        	try {
        		cr.delete(sLauncherUri, where, selectionArgs); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }		
	}
}
