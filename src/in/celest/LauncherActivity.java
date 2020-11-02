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
import android.view.View;
import android.widget.*;
import com.valvesoftware.GameInfo;
import java.io.File;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.libsdl.app.SDLActivity;
import android.content.pm.PackageManager;
import com.nvidia.valvesoftware.halflife2.R;
import android.widget.LinearLayout.LayoutParams;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import java.security.MessageDigest;
import android.util.Base64;

public class LauncherActivity extends Activity {
    static EditText EnvEdit;
    static EditText GamePath;
    public static String PKG_NAME;
    public static boolean can_write = true;
    static EditText cmdArgs;
    static CheckBox immersiveMode;
    static LinearLayout launcher;
    public static SharedPreferences mPref;
    public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
    static CheckBox showtouch;
    static Spinner spin;

    public static native boolean checkCert(String cert);

    public SpannableString styleButtonString(String str) {
        if (sdk < 21) {
            str = str.toUpperCase();
        }
        SpannableString spanString = new SpannableString(str.toUpperCase());
        if (sdk < 21) {
            spanString.setSpan(new StyleSpan(1), 0, str.length(), 0);
        }
        return spanString;
    }

    public static String getDefaultDir() {
        File dir = Environment.getExternalStorageDirectory();
        if (dir == null || !dir.exists()) {
            return "/sdcard/";
        }
        return dir.getPath();
    }

    public static String getADataDir() {
        String path = getDefaultDir() + "/Android/data/" + PKG_NAME + "/files";
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return path;
    }

    public boolean checkCertificate()
    {
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo( getPackageName(), PackageManager.GET_SIGNATURES );

            for( Signature signature: info.signatures )
            {
                MessageDigest md = MessageDigest.getInstance( "SHA" );
                final byte[] signatureBytes = signature.toByteArray();

                md.update( signatureBytes );

                final String curSIG = Base64.encodeToString( md.digest(), Base64.NO_WRAP );

		// here check
            }
        }
        catch( PackageManager.NameNotFoundException e )
        {
            e.printStackTrace();
            return false;
        }
        catch( Exception e ) 
        {
            e.printStackTrace();
        }

        return true;
    }


    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PKG_NAME = getApplication().getPackageName();
        requestWindowFeature(1);
        if (sdk >= 21) {
            super.setTheme(16974372);
        } else {
            super.setTheme(16973829);
        }

        setContentView(R.layout.activity_launcher);

        LinearLayout.LayoutParams buttonparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        buttonparams.setMargins(10, 20, 10, 20);

        LinearLayout.LayoutParams titleviewparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleviewparams.setMargins(13, 0, 0, 0);


        LinearLayout body = (LinearLayout)findViewById(R.id.body);

        TextView textView = new TextView(this);
        textView.setLayoutParams(titleviewparams);
        textView.setText("Command-line arguments");
        textView.setTextAppearance(this, android.R.attr.textAppearanceMedium);

        cmdArgs = new EditText(this);
        cmdArgs.setLayoutParams(buttonparams);
        cmdArgs.setSingleLine(true);

        TextView textView2 = new TextView(this);
        textView2.setLayoutParams(titleviewparams);
        textView2.setText("env's");
        textView2.setTextAppearance(this, android.R.attr.textAppearanceMedium);

        EnvEdit = new EditText(this);
        EnvEdit.setLayoutParams(buttonparams);
        EnvEdit.setSingleLine(true);

        TextView textView3 = new TextView(this);
        textView3.setLayoutParams(titleviewparams);
        textView3.setText("Path to game resources:");
        textView3.setTextAppearance(this, android.R.attr.textAppearanceMedium);

        GamePath = new EditText(this);
        GamePath.setLayoutParams(buttonparams);
        GamePath.setSingleLine(true);

        TextView textView4 = new TextView(this);
        textView4.setLayoutParams(titleviewparams);
        textView4.setText("Game:");
        textView4.setTextAppearance(this, android.R.attr.textAppearanceMedium);

	spin = new Spinner(this);
	ArrayList<String> spinnerArray = new ArrayList<String>();
	spinnerArray.add("Half-Life 2");
	spinnerArray.add("Half-Life 2 Episode 1");
	spinnerArray.add("Half-Life 2 Episode 2");
	spinnerArray.add("Portal");

	ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
	spin.setAdapter(spinnerArrayAdapter);

        immersiveMode = new CheckBox(this);
        immersiveMode.setLayoutParams(buttonparams);
        immersiveMode.setText("Immersive Mode");

        showtouch = new CheckBox(this);
        showtouch.setLayoutParams(buttonparams);
        showtouch.setText("Show Touch");

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
                dialog.setTitle("About");
                ScrollView scroll = new ScrollView(LauncherActivity.this);
                scroll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                TextView text = new TextView(LauncherActivity.this);
                text.setText("\nSpecial thanks to:\nptitSeb for gl4es: https://github.com/ptitSeb/gl4es\nRusJJ for the particles fix\nnillerusr for port\nvalve for source engine\n\nNot for commercial use!\n\nDonate: https://www.patreon.com/nillerusr\nhttps://donatepay.ru/don/nillerusr");
                text.setLinksClickable(true);
                Linkify.addLinks(text, Pattern.compile("[a-z]+:\\/\\/[^ \\n]*"), GameInfo.hl2.extras_obb);
                scroll.addView(text);
                dialog.setContentView(scroll);
                dialog.show();
            }
        });

        body.addView(textView);
        body.addView(cmdArgs);
        body.addView(textView2);
        body.addView(EnvEdit);
        body.addView(textView3);
        body.addView(GamePath);
        body.addView(textView4);
	body.addView(spin);
        body.addView(showtouch);
        if (sdk >= 19) {
            body.addView(immersiveMode);
        }

        mPref = getSharedPreferences("mod", 0);
        cmdArgs.setText(mPref.getString("argv", "+developer 1"));
        GamePath.setText(mPref.getString("gamepath", getDefaultDir() + "/srceng"));
        EnvEdit.setText(mPref.getString("env", "LIBGL_USEVBO=0"));
	spin.setSelection(mPref.getInt("game", GameInfo.GAME_HL2));
        if (sdk >= 19) {
            immersiveMode.setChecked(mPref.getBoolean("immersive_mode", true));
        }
        showtouch.setChecked(mPref.getBoolean("show_touch", true));
    }

    /* access modifiers changed from: package-private */
    public void extractTouchIcons(String path) {
        FileOutputStream os;
        try {
            String[] Files = getApplicationContext().getAssets().list("hl2/materials/vgui/touch");
            File directory = new File(path + "/hl2/materials/vgui/touch");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            int length = Files.length;
            int i = 0;
            FileOutputStream os2 = null;
            while (i < length) {
                try {
                    String file = Files[i];
                    if (new File(path + "/hl2/materials/vgui/touch/" + file).exists()) {
                        os = os2;
                    } else {
                        InputStream is = getAssets().open("hl2/materials/vgui/touch/" + file);
                        os = new FileOutputStream(path + "/hl2/materials/vgui/touch/" + file);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int length2 = is.read(buffer);
                            if (length2 <= 0) {
                                break;
                            }
                            os.write(buffer, 0, length2);
                        }
                        os.close();
                        is.close();
                    }
                    i++;
                    os2 = os;
                } catch (Exception e) {
                    Log.e("SRCAPK", "Failed to extract touch icons:" + e.toString());
                    return;
                }
            }
        } catch (Exception e2) {
        }
    }

    public boolean checkObb( String main, String patch, String extras)
    {
        String gamepath = GamePath.getText().toString();
        File main_obb = new File(gamepath+"/"+main);
        File patch_obb = new File(gamepath+"/"+patch);
        File extras_obb = new File(gamepath+"/"+extras);
        if (!main_obb.exists() || main_obb.isDirectory() || !patch_obb.exists() || patch_obb.isDirectory() || (extras != null && !extras.trim().isEmpty() && (!extras_obb.exists() || extras_obb.isDirectory())))
        {
            new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There are no obb files on the path " + gamepath)
                .setPositiveButton("OK", (DialogInterface.OnClickListener) null)
                .show();
                return false;
        }
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
        boolean rodir = mPref.getBoolean("rodir", false);

        if( spin.getSelectedItemPosition() == GameInfo.GAME_HL2 )
	{
            if( !checkObb( GameInfo.hl2.main_obb, GameInfo.hl2.patch_obb, null) )
                return;
	}
        else if( spin.getSelectedItemPosition() == GameInfo.GAME_HL2EP1 )
	{
            if( !checkObb( GameInfo.hl2ep1.main_obb, GameInfo.hl2ep1.patch_obb, null) )
                return;
	}
        else if( spin.getSelectedItemPosition() == GameInfo.GAME_HL2EP2 )
	{
            if( !checkObb( GameInfo.hl2ep2.main_obb, GameInfo.hl2ep2.patch_obb, GameInfo.hl2ep2.extras_obb) )
                return;
	}
        else if( spin.getSelectedItemPosition() == GameInfo.GAME_HL2 )
	{
            if( !checkObb( GameInfo.portal.main_obb, GameInfo.portal.patch_obb, null) )
                return;
	}

        try {
            FileWriter myWriter = new FileWriter(gamepath + "/testwrite");
            myWriter.write("TEST!");
            myWriter.close();
        } catch (IOException e) {
            can_write = false;
        }
        File f = new File(gamepath + "/testwrite");
        if (!f.exists()) {
            can_write = false;
        }
        if (can_write || rodir) {
            if (can_write) {
                editor.putBoolean("rodir", false);
                editor.commit();
                rodir = false;
                f.delete();
            }
            if (rodir) {
                extractTouchIcons(getADataDir());
            } else {
                extractTouchIcons(gamepath);
            }
            if (sdk >= 19) {
                editor.putBoolean("immersive_mode", immersiveMode.isChecked());
            } else {
                editor.putBoolean("immersive_mode", false);
            }
            editor.putBoolean("show_touch", showtouch.isChecked());
            editor.commit();
            Intent intent = new Intent(this, SDLActivity.class);
            intent.addFlags(268435456);
            startActivity(intent);
            return;
        }
        new AlertDialog.Builder(this).setTitle("Wraning").setMessage("Test write failed, all files will be stored in " + getADataDir()).setPositiveButton("OK", (DialogInterface.OnClickListener) null).show();
        editor.putBoolean("rodir", true);
        editor.commit();
    }
}
