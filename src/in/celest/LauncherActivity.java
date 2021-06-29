package in.celest;

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
import com.valvesoftware.Games;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.libsdl.app.SDLActivity;
import android.content.pm.PackageManager;
import com.nvidia.valvesoftware.source.R;
import android.widget.LinearLayout.LayoutParams;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import java.security.MessageDigest;
import android.util.Base64;
import android.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import in.celest.DirchActivity;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.DisplayMetrics;
import me.nillerusr.Screen;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;

public class LauncherActivity extends Activity {
	public static String PKG_NAME;
	public static boolean can_write = true;
	static EditText cmdArgs, GamePath, EnvEdit, res_width, res_height;
	public static SharedPreferences mPref;
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
	static CheckBox showtouch, useVolumeButtons, immersiveMode, fixedResolution;
	static Spinner spin;
	public static String found_main_obb = null;
	public static Screen.Resolution scr_res;
	static LinearLayout res_layout;

	final static int REQUEST_PERMISSIONS = 42;

	public void applyPermissions( final String permissions[], final int code ) {
		List<String> requestPermissions = new ArrayList<String>();
		for( int i = 0; i < permissions.length; i++ ) {
			if( checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED )
				requestPermissions.add(permissions[i]);
		}

		if( !requestPermissions.isEmpty() ) {
			String[] requestPermissionsArray = new String[requestPermissions.size()];
			for( int i = 0; i < requestPermissions.size(); i++ )
				requestPermissionsArray[i] = requestPermissions.get(i);
			requestPermissions(requestPermissionsArray, code);
		}
	}

	public void onRequestPermissionsResult( int requestCode,  String[] permissions,  int[] grantResults ) {
		if( requestCode == REQUEST_PERMISSIONS ) {
			if( grantResults[0] == PackageManager.PERMISSION_DENIED ) {
				Toast.makeText( this, R.string.srceng_launcher_error_no_permission, Toast.LENGTH_LONG ).show();
				finish();
			}
		}
	}

	public static String getDefaultDir() {
		File dir = Environment.getExternalStorageDirectory();
		if (dir == null || !dir.exists())
			return "/sdcard/";
		return dir.getPath();
	}

	public static String getADataDir() {
		String path = getDefaultDir() + "/Android/data/" + PKG_NAME + "/files";
		File directory = new File(path);
		if (!directory.exists())
			directory.mkdirs();
		return path;
	}

	public static void changeButtonsStyle( ViewGroup parent )
	{
		if( sdk >= 21 )
			return;

		for( int i = parent.getChildCount() - 1; i >= 0; i-- )
		{
			try
			{
				final View child = parent.getChildAt(i);

				if( child == null )
					continue;

				if( child instanceof ViewGroup )
				{
					changeButtonsStyle((ViewGroup) child);
					// DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
				}
				else if( child instanceof Button )
				{
					final Button b = (Button)child;
					final Drawable bg = b.getBackground();
					if(bg!= null)bg.setAlpha( 96 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
					//b.setText(b.getText().toString().toUpperCase());
					b.setTypeface( b.getTypeface(),Typeface.BOLD );
				}
				else if( child instanceof EditText )
				{
					final EditText b = ( EditText )child;
					b.setBackgroundColor( 0xFF272727 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
				}
			}
			catch( Exception e )
			{
			}
		}
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PKG_NAME = getApplication().getPackageName();
		requestWindowFeature(1);

		if (sdk >= 21)
			super.setTheme(0x01030224);
		else
			super.setTheme(0x01030005);

		scr_res = Screen.getResolution( this );

		mPref = getSharedPreferences("mod", 0);

		setContentView(R.layout.activity_launcher);

		LinearLayout body = (LinearLayout)findViewById(R.id.body);

		cmdArgs = (EditText)findViewById(R.id.edit_cmdline);
		EnvEdit = (EditText)findViewById(R.id.edit_env);
		GamePath = (EditText)findViewById(R.id.edit_gamepath);

		spin = (Spinner)findViewById(R.id.spinner_games);
		ArrayList<String> spinnerArray = new ArrayList<String>();
		for( int j = 0;j < Games.count(); j++)
			spinnerArray.add(Games.at(j).name);

		ArrayAdapter<String> spinnerArrayAdapter;

		if( sdk >= 21 )
			spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
		else
			spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_v8, spinnerArray);

		spin.setAdapter(spinnerArrayAdapter);

		immersiveMode = (CheckBox)findViewById(R.id.checkbox_immersive_mode);
		showtouch = (CheckBox)findViewById(R.id.checkbox_show_touch);
		useVolumeButtons = (CheckBox)findViewById(R.id.checkbox_use_volume);

		useVolumeButtons.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if( isChecked )
					Toast.makeText(LauncherActivity.this, R.string.srceng_launcher_volume_buttons_desc, 5000).show();
			}
		});

		res_layout = (LinearLayout)findViewById(R.id.layout_resolution);

		fixedResolution = (CheckBox)findViewById(R.id.checkbox_fixed_resolution);
		fixedResolution.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if( isChecked )
					LauncherActivity.this.res_layout.setVisibility(View.VISIBLE);
				else
					LauncherActivity.this.res_layout.setVisibility(View.GONE);
			}
		});



		Button button = (Button)findViewById(R.id.button_launch);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				LauncherActivity.this.startSource(v);
			}
		});

		Button aboutButton = (Button) findViewById(R.id.button_about);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Dialog dialog = new Dialog(LauncherActivity.this);
				dialog.setTitle(R.string.srceng_launcher_about);
				ScrollView scroll = new ScrollView(LauncherActivity.this);
				scroll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				scroll.setPadding(5,5,5,5);
				TextView text = new TextView(LauncherActivity.this);
				text.setText(R.string.srceng_launcher_about_text);
				text.setLinksClickable(true);
				Linkify.addLinks(text, Linkify.WEB_URLS|Linkify.EMAIL_ADDRESSES);
//				Linkify.addLinks(text, Pattern.compile("[a-z]+:\\/\\/[^ \\n]*"), "");
				scroll.addView(text);
				dialog.setContentView(scroll);
				dialog.show();
			}
		});

		Button dirButton = findViewById(R.id.button_gamedir);
		dirButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LauncherActivity.this, DirchActivity.class);
				intent.addFlags(268435456);
				startActivity(intent);
			}
		});

		res_width = findViewById(R.id.edit_resolution_width);
		res_height = findViewById(R.id.edit_resolution_height);
		res_width.setText(""+mPref.getInt("resolution_width", scr_res.width));
		res_height.setText(""+mPref.getInt("resolution_height", scr_res.height));

		if (sdk >= 19)
		{
			immersiveMode.setChecked(true);
			//immersiveMode.setVisibility(View.VISIBLE);
			//immersiveMode.setChecked(mPref.getBoolean("immersive_mode", true));
		}

		cmdArgs.setText(mPref.getString("argv", "-console"));
		GamePath.setText(mPref.getString("gamepath", getDefaultDir() + "/srceng"));
		EnvEdit.setText(mPref.getString("env", "LIBGL_USEVBO=0"));
		spin.setSelection(mPref.getInt("game", 0));
		showtouch.setChecked(mPref.getBoolean("show_touch", true));
		useVolumeButtons.setChecked(mPref.getBoolean("use_volume_buttons", false));
		fixedResolution.setChecked(mPref.getBoolean("fixed_resolution", false));

		changeButtonsStyle((ViewGroup)this.getWindow().getDecorView());

		// permissions check
		if( sdk >= 23 )
			applyPermissions( new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSIONS );
	}

	/* access modifiers changed from: package-private */
	public void extractTouchIcons(String path) {
		FileOutputStream os;
		try {
			String[] Files = getApplicationContext().getAssets().list("hl2/materials/vgui/touch");
			File directory = new File(path + "/hl2/materials/vgui/touch");
			if (!directory.exists())
				directory.mkdirs();

			int length = Files.length;
			int i = 0;
			FileOutputStream os2 = null;
			while (i < length) {
				String file = Files[i];
				if (new File(path + "/hl2/materials/vgui/touch/" + file).exists())
					os = os2;
				else {
					InputStream is = getAssets().open("hl2/materials/vgui/touch/" + file);
					os = new FileOutputStream(path + "/hl2/materials/vgui/touch/" + file);
					byte[] buffer = new byte[1024];
					while (true) {
						int length2 = is.read(buffer);
						if (length2 <= 0)
							break;
						os.write(buffer, 0, length2);
					}
					os.close();
					is.close();
				}
				i++;
				os2 = os;
			}
		}
		catch (Exception e) {
			Log.e("SRCAPK", "Failed to extract touch icons:" + e.toString());
		}
	}

	public boolean checkObb( String main, String patch, String extras)
	{
		String gamepath = GamePath.getText().toString();
		File main_obb = new File(gamepath+"/"+main);
		File patch_obb = new File(gamepath+"/"+patch);
		File extras_obb = new File(gamepath+"/"+extras);
		String missing_obb = "";
		boolean bCheckFail = false;
		String missing_obb_desc_a = this.getResources().getString(R.string.srceng_launcher_error_missing_obb_a);
		String missing_obb_desc_b = this.getResources().getString(R.string.srceng_launcher_error_missing_obb_b);

		if( !main_obb.exists() || main_obb.isDirectory() ) {
			File fileName = new File(gamepath);
			if( fileName.exists() ) {
				File[] fileList = fileName.listFiles();

				for (File file: fileList) {
					String fname = file.getName();
					if( !file.isDirectory() && fname.matches("main.\\d\\d.com.nvidia.valvesoftware.(.*).obb") ) {
						found_main_obb = fname;
						break;
					}
				}
			}

			if( found_main_obb == null ) {
				missing_obb += main+"\n";
				bCheckFail = true;
			}
		}

		if( !patch_obb.exists() || patch_obb.isDirectory() ) {
			missing_obb += patch+"\n";
			bCheckFail = true;
		}

		if( extras != null && !extras.trim().isEmpty() && (!extras_obb.exists() || extras_obb.isDirectory()) ) {
			missing_obb += extras+"\n";
			bCheckFail = true;
		}

		if( bCheckFail ) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.srceng_launcher_error)
				.setMessage(missing_obb_desc_a + "\n" + missing_obb + missing_obb_desc_b + "\n" + gamepath)
				.setPositiveButton(R.string.srceng_launcher_ok, (DialogInterface.OnClickListener) null)
				.show();
				return false;
		}
		return true;
	}

	public boolean writeTest(String gamepath)
	{
		try {
			FileWriter myWriter = new FileWriter(gamepath + "/testwrite");
			myWriter.write("TEST!");
			myWriter.close();
		}
		catch (IOException e) { return false; }

		File f = new File(gamepath + "/testwrite");
		if (!f.exists())
			return false;

		f.delete();
		return true;
	}

	public void startSource(View view) {
		String argv = cmdArgs.getText().toString();
		String gamepath = GamePath.getText().toString();
		String env = EnvEdit.getText().toString();
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", argv);
		editor.putString("gamepath", gamepath);
		editor.putString("env", env);
		editor.putInt("game", spin.getSelectedItemPosition());
		editor.putInt("resolution_width", Integer.parseInt(res_width.getText().toString()));
		editor.putInt("resolution_height", Integer.parseInt(res_height.getText().toString()));
		boolean rodir = mPref.getBoolean("rodir", false);

		Games.Game game = Games.at(spin.getSelectedItemPosition() );
		if( !checkObb( game.main_obb, game.patch_obb,  game.extras_obb) )
				return;

		boolean can_write = writeTest(gamepath);

		if (can_write || rodir) {
			if (can_write) {
				editor.putBoolean("rodir", false);
				editor.commit();
				rodir = false;
			}

			if (rodir)
				extractTouchIcons(getADataDir());
			else
				extractTouchIcons(gamepath);

			if (sdk >= 19)
				editor.putBoolean("immersive_mode", immersiveMode.isChecked());
			else
				editor.putBoolean("immersive_mode", false);

			editor.putBoolean("show_touch", showtouch.isChecked());
			editor.putBoolean("use_volume_buttons", useVolumeButtons.isChecked());
			editor.putBoolean("fixed_resolution", fixedResolution.isChecked());
			editor.commit();
			Intent intent = new Intent(this, SDLActivity.class);
			intent.addFlags(268435456);
			startActivity(intent);
			return;
		}

		new AlertDialog.Builder(this).setTitle("Wraning").setMessage(R.string.srceng_launcher_error_test_write + getADataDir()).setPositiveButton(R.string.srceng_launcher_ok, (DialogInterface.OnClickListener) null).show();
		editor.putBoolean("rodir", true);
		editor.commit();
	}
}
