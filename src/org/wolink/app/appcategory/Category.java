package org.wolink.app.appcategory;

import android.net.Uri;
import android.provider.BaseColumns;

public class Category {
    public static final String AUTHORITY = "org.wolink.app.appcategory.settings";
    private Category() {}
    
    public static final class Items implements BaseColumns {
        private Items() {}

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/categorys");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.wolink.category";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.wolink.category";
        
        public static final String TITLE = "title";
        public static final String PARENT = "parentId";
        public static final String TYPE = "type";
        public static final String PREV = "prevId";
        public static final String NEXT = "nextId";
        public static final String ISSYSTEM = "isSystem";      
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
        public static final String LASTUSED_DATE = "lastused";
        public static final String ICON = "icon";
        public static final String INTENT = "intent";
        public static final String URL = "url";
        public static final String USECOUNT = "useCount";
    }
}
