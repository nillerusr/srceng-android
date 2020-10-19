package in.celest;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import org.libsdl.app.*;
import java.io.File;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.widget.Toast;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import com.valvesoftware.GameInfo;
import android.util.Log;

public class LauncherActivity extends Activity {
	static EditText cmdArgs;
	static EditText GamePath;
	static EditText EnvEdit;
	static CheckBox immersiveMode;

	public static SharedPreferences mPref;
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK);
	
	public SpannableString styleButtonString(String str)
	{
		if(sdk < 21)
			str = str.toUpperCase();

		SpannableString spanString = new SpannableString(str.toUpperCase());
		
		if(sdk < 21)
			spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);

		return spanString;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// set theme
		if ( sdk >= 21 )
			super.setTheme( 0x01030224 );
		else 
			super.setTheme( 0x01030005 );

		// Build layout
		LinearLayout launcher = new LinearLayout(this);
		launcher.setOrientation(LinearLayout.VERTICAL);
		launcher.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		launcher.setBackgroundColor(0xFF252525);
		TextView launcherTitle = new TextView(this);
		LayoutParams titleparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		titleparams.setMargins(5,5,5,1);//размеры верхнего layout
		LayoutParams layparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layparams.setMargins(5,12,5,1);

		LayoutParams buttonparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		buttonparams.setMargins(10,20,10,20);//размеры строки для ввода аргументов

		launcherTitle.setLayoutParams(titleparams);
		launcherTitle.setText("Source Engine");
		//launcherTitle.setTextColor(0xFFFF8C00);
		launcherTitle.setTextAppearance(this, android.R.attr.textAppearanceMedium);
		launcherTitle.setTextSize(25);
		launcherTitle.setGravity(1);

		launcherTitle.setBackgroundColor(0xFF555555);
		try
		{
			launcherTitle.setPadding(9,9,6,0);
		}
		catch(Exception e)
		{
			launcherTitle.setPadding(60,6,6,6);
		}

		launcher.addView(launcherTitle);
		LinearLayout launcherBody = new LinearLayout(this);

		launcherBody.setOrientation(LinearLayout.VERTICAL);
		launcherBody.setLayoutParams(layparams);
		launcherBody.setBackgroundColor(0xFF454545);

		ScrollView m_Scroll = new ScrollView(this);
		m_Scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT));
		m_Scroll.addView( launcherBody, new
			LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT) );

		LinearLayout launcherBorder = new LinearLayout(this);
		launcherBorder.setLayoutParams(layparams);
		launcherBorder.setBackgroundColor(0xFF555555);
		launcherBorder.setOrientation(LinearLayout.VERTICAL);

		LinearLayout launcherBorder2 = new LinearLayout(this);
		launcherBorder2.setLayoutParams(layparams);
		launcherBorder2.setOrientation(LinearLayout.VERTICAL);
		launcherBorder2.setBackgroundColor(0xFF353535);
		launcherBorder2.addView(m_Scroll);
		launcherBorder2.setPadding(10,0,10,10);
		launcherBorder.addView(launcherBorder2);
		launcherBorder.setPadding(10,0,10,20);
		launcher.addView(launcherBorder);

		LayoutParams titleviewparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		titleviewparams.setMargins(13,0,0,0);//размеры верхнего layout

		TextView titleView = new TextView(this);
		titleView.setLayoutParams(titleviewparams);
		titleView.setText("Command-line arguments");
		titleView.setTextAppearance(this, android.R.attr.textAppearanceLarge);

		cmdArgs = new EditText(this);
		cmdArgs.setLayoutParams(buttonparams);
		cmdArgs.setSingleLine(true);
		if(sdk < 21)
		{
			cmdArgs.setBackgroundColor(0xFF353535);
			cmdArgs.setTextColor(0xFF333333);
			cmdArgs.setPadding(5,0,5,5);
		}

		TextView titleView_env = new TextView(this);
		titleView_env.setLayoutParams(titleviewparams);
		titleView_env.setText("env's");
		titleView_env.setTextAppearance(this, android.R.attr.textAppearanceLarge);

		EnvEdit = new EditText(this);
		EnvEdit.setLayoutParams(buttonparams);
		EnvEdit.setSingleLine(true);
		if(sdk < 21)
		{
			EnvEdit.setBackgroundColor(0xFF353535);
			EnvEdit.setTextColor(0xFF333333);
			EnvEdit.setPadding(5,0,5,5);
		}

		TextView titleView2 = new TextView(this);
		titleView2.setLayoutParams(titleviewparams);
		titleView2.setText("Path to game resources:");
		titleView2.setTextAppearance(this, android.R.attr.textAppearanceLarge);

		GamePath = new EditText(this);
		GamePath.setLayoutParams(buttonparams);
		GamePath.setSingleLine(true);
		if(sdk < 21)
		{
			GamePath.setBackgroundColor(0xFF353535);
			GamePath.setTextColor(0xFF333333);
			GamePath.setPadding(5,0,5,5);
		}

		immersiveMode = new CheckBox(this);
		immersiveMode.setLayoutParams(buttonparams);
		immersiveMode.setText( "Immersive Mode" );

		RelativeLayout panel = new RelativeLayout(this);

		LayoutParams panelparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		panel.setLayoutParams(panelparams);
		panel.setGravity(Gravity.BOTTOM);

		Button startButton = new Button(this);

		// Set launch button title here
		startButton.setText(styleButtonString("Launch " + "Source" + "!"));
		RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		startButton.setLayoutParams(buttonParams);
		if(sdk < 21)
		{
			startButton.getBackground().setAlpha(96);
			startButton.getBackground().invalidateSelf();
			startButton.setTextColor(0xFFFFFFFF);
			startButton.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			startButton.setTextSize(20);
		}
		startButton.setOnClickListener(new View.OnClickListener() {

		@Override
		public void onClick(View v) { 
			startSource(v);
			}
		});

		launcherBody.addView(titleView);
		launcherBody.addView(cmdArgs);
		launcherBody.addView(titleView_env);
		launcherBody.addView(EnvEdit);
		launcherBody.addView(titleView2);
		launcherBody.addView(GamePath);
		if( sdk >= 19 )
			launcherBody.addView(immersiveMode);

		// Add other options here

		panel.addView(startButton);
		launcher.addView(panel);
		setContentView(launcher);
		mPref = getSharedPreferences("mod", 0);
		cmdArgs.setText(mPref.getString("argv","+developer 1"));
		GamePath.setText(mPref.getString("gamepath","/sdcard/srceng/"));
		EnvEdit.setText(mPref.getString("env", "LIBGL_USEVBO=0"));
		if( sdk >= 19 )
		{
			immersiveMode.setChecked(mPref.getBoolean("immersive_mode", true));
		}
	}

	void extractTouchIcons(String path)
	{
		InputStream is = null;
		FileOutputStream os = null;
		try
		{
			AssetManager myAssetManager = getApplicationContext().getAssets();

			String[] Files;
			Files = myAssetManager.list("hl2/materials/vgui/touch"); // массив имен файлов
			File directory = new File(path+"/hl2/materials/vgui/touch");
			if (!directory.exists())
				directory.mkdirs();

			for( String file: Files )
			{
				File f = new File(path+"/hl2/materials/vgui/touch/"+file);
				if( f.exists() )
					continue;

                                is = this.getAssets().open("hl2/materials/vgui/touch/"+file);
                                os = new FileOutputStream(path+"/hl2/materials/vgui/touch/"+file);
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = is.read(buffer)) > 0)
                                        os.write(buffer, 0, length);

                                os.close();
                                is.close();
                        }
                }
		catch( Exception e )
        {
			Log.e( "SRCAPK", "Failed to extract touch icons:" + e.toString() );
        }
	}


	public void startSource(View view)
	{
		String argv = cmdArgs.getText().toString();
		String gamepath = GamePath.getText().toString();
		String env = EnvEdit.getText().toString();
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", argv);
		editor.putString("gamepath", gamepath);
		editor.putString("env", env);

		File main_obb = new File(gamepath+"/"+GameInfo.main_obb);
		File patch_obb = new File(gamepath+"/"+GameInfo.patch_obb);
		if(!main_obb.exists() || main_obb.isDirectory() || !patch_obb.exists() || patch_obb.isDirectory())
		{
			new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("There are no obb files on the path "+gamepath)
				.setPositiveButton("OK", null)
				//.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			return;
		}

		extractTouchIcons(gamepath);

		if( sdk >= 19 )
			editor.putBoolean("immersive_mode", immersiveMode.isChecked());
		else
			editor.putBoolean("immersive_mode", false); // just in case...
		editor.commit();

		Intent intent = new Intent(this, SDLActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}

