/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wolink.app.appcategory;

import com.mobclick.android.MobclickAgent;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class CategoryEditor extends Activity implements View.OnClickListener {

    private EditText mText;
    private Uri mItemUri;

    private static final String[] PROJECTION = new String[] {
        Category.Items._ID,
        Category.Items.TITLE,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.category_editor);
        
        mItemUri = getIntent().getData();
        
        mText = (EditText) this.findViewById(R.id.title_edit);
        TextView tvTitle = (TextView) this.findViewById(R.id.title);
        
        if (mItemUri != null) {
        	Cursor cursor = managedQuery(mItemUri, PROJECTION, null, null, null);  
        	if (cursor != null) {
        		cursor.moveToFirst();
        		int titleIdx = cursor.getColumnIndexOrThrow(Category.Items.TITLE);
        		mText.setText(cursor.getString(titleIdx));
        	}
        	tvTitle.setText(R.string.rename_folder);
        } else {
        	tvTitle.setText(R.string.new_folder);
        }
        
        Button b = (Button) findViewById(R.id.ok);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.cancel);
        b.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				finish();
			}
		});
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

    public void onClick(View v) {
    	if (TextUtils.isEmpty(mText.getText())) {
    		Toast.makeText(this, R.string.toast_title_null, 2000).show();
    		return ;
    	}
    	
    	if (mItemUri == null) {
    		CategoryUtils.addCategory(this, mText.getText().toString());
    	} else {
    		CategoryUtils.renameCategory(this, ContentUris.parseId(mItemUri), mText.getText().toString());  		
    	}
    	
        finish();
    }
}
