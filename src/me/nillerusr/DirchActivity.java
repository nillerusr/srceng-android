package me.nillerusr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.libsdl.app.SDLActivity;
import android.content.pm.PackageManager;
import com.valvesoftware.source.R;
import android.widget.LinearLayout.LayoutParams;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import java.security.MessageDigest;
import android.util.Base64;
import android.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.nillerusr.LauncherActivity;
import android.view.LayoutInflater;
import android.graphics.Bitmap;
import java.util.Arrays;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

public class DirchActivity extends Activity implements OnTouchListener{
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
	public static String cur_dir;
	static LinearLayout body;
	public SharedPreferences mPref;

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			TextView btn = (TextView)v.findViewById(R.id.dirname);
			if( cur_dir == null )
				ListDirectory(""+btn.getText());
			else
				ListDirectory(cur_dir+"/"+btn.getText());
		}
		return false;
	}

	public void ListDirectory( String path )
	{
		TextView header = (TextView)findViewById(R.id.header_txt);
		File myDirectory = new File(path);

		File[] directories = myDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if (directories != null && directories.length > 1) {
			Arrays.sort(directories, new Comparator<File>() {
				@Override
				public int compare(File object1, File object2) {
					return object1.getName().toUpperCase().compareTo(object2.getName().toUpperCase());
				}
			});
		}

		LayoutInflater ltInflater = getLayoutInflater();
		if( directories == null )
			return;

		try {
			cur_dir = myDirectory.getCanonicalPath();
			header.setText(cur_dir);
		} catch( IOException e ) { }

		body.removeAllViews();
		View view = ltInflater.inflate(R.layout.directory, body, false);
		TextView txt = (TextView)view.findViewById(R.id.dirname);
		txt.setText("..");
		body.addView(view);
		view.setOnTouchListener(this);

		for ( File dir : directories ) {
			view = ltInflater.inflate(R.layout.directory, body, false);
			txt = (TextView)view.findViewById(R.id.dirname);
			txt.setText(dir.getName());
			body.addView(view);
			view.setOnTouchListener(this);
		}
	}

	public List<String> getExtStoragePaths() {
		List<String> list = new ArrayList<String>();
		File fileList[] = new File("/storage/").listFiles();
		if( fileList == null )
			return list;

		for (File file : fileList) {
			if(!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
           		list.add(file.getAbsolutePath());
		}
		return list;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPref = getSharedPreferences("mod", 0);

		requestWindowFeature(1);
		if (sdk >= 21)
			super.setTheme(16974372);
		else
			super.setTheme(16973829);

		setContentView(R.layout.activity_directory_choice);
		cur_dir = null;
		body = (LinearLayout)findViewById(R.id.bodych);
		TextView header = (TextView)findViewById(R.id.header_txt);
		header.setText("");

		Button button = (Button)findViewById(R.id.button_choice);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if( cur_dir != null ) {
					if( LauncherActivity.GamePath != null )
						LauncherActivity.GamePath.setText(cur_dir+"/");
					SharedPreferences.Editor editor = mPref.edit();
					editor.putString("gamepath", cur_dir+"/");
					editor.commit();
					finish();
				}
			}
		});

		LauncherActivity.changeButtonsStyle((ViewGroup)this.getWindow().getDecorView());

		List<String> l = getExtStoragePaths();
		if( l == null || l.isEmpty() ) {
			ListDirectory(LauncherActivity.getDefaultDir());
			return;
		}

		LayoutInflater ltInflater = getLayoutInflater();
		View view = ltInflater.inflate(R.layout.directory, body, false);
		TextView txt = (TextView)view.findViewById(R.id.dirname);
		txt.setText(LauncherActivity.getDefaultDir());
		body.addView(view);
		view.setOnTouchListener(this);

		for( String dir : l) {
			view = ltInflater.inflate(R.layout.directory, body, false);
			txt = (TextView)view.findViewById(R.id.dirname);
			txt.setText(dir);
			body.addView(view);
			view.setOnTouchListener(this);
		}
	}
}
