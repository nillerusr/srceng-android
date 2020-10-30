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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.valvesoftware.GameInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.libsdl.app.SDLActivity;

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

    public static String find_main() {
        return GameInfo.extras_obb;
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
        RelativeLayout relativeLayout = new RelativeLayout(this);
        launcher = new LinearLayout(this);
        launcher.setOrientation(1);
        launcher.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        launcher.setBackgroundColor(-14342875);
        LinearLayout.LayoutParams layparams = new LinearLayout.LayoutParams(-1, -1);
        layparams.setMargins(5, 12, 5, 1);
        LinearLayout.LayoutParams buttonparams = new LinearLayout.LayoutParams(-1, -2);
        buttonparams.setMargins(10, 20, 10, 20);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        TextView launcherTitle = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(5, 5, 5, 1);
        launcherTitle.setLayoutParams(layoutParams);
        launcherTitle.setText("Source Engine");
        launcherTitle.setTextAppearance(this, 16842817);
        launcherTitle.setTextSize(25.0f);
        launcherTitle.setGravity(1);
        linearLayout.setPadding(5, 0, 5, -5);
        launcherTitle.setBackgroundColor(-11184811);
        RelativeLayout ass = new RelativeLayout(this);
        ass.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        launcher.addView(linearLayout);
        linearLayout.addView(launcherTitle);
        LinearLayout launcherBody = new LinearLayout(this);
        launcherBody.setOrientation(1);
        launcherBody.setLayoutParams(layparams);
        launcherBody.setBackgroundColor(-12237499);
        ScrollView m_Scroll = new ScrollView(this);
        m_Scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        m_Scroll.setFillViewport(true);
        LinearLayout launcherBorder = new LinearLayout(this);
        launcherBorder.setLayoutParams(layparams);
        launcherBorder.setBackgroundColor(-11184811);
        launcherBorder.setOrientation(1);
        LinearLayout launcherBorder2 = new LinearLayout(this);
        launcherBorder2.setLayoutParams(layparams);
        launcherBorder2.setOrientation(1);
        launcherBorder2.setBackgroundColor(-13290187);
        launcherBorder2.addView(m_Scroll);
        launcherBorder2.setPadding(10, 10, 10, 10);
        launcherBorder.addView(launcherBorder2);
        launcherBorder.setPadding(10, 0, 10, 10);
        m_Scroll.addView(launcherBody);
        RelativeLayout relativeLayout2 = new RelativeLayout(this);
        RelativeLayout.LayoutParams layp = new RelativeLayout.LayoutParams(-1, -1);
        layp.addRule(2, relativeLayout.getId());
        relativeLayout2.setLayoutParams(layp);
        relativeLayout2.addView(launcherBorder);
        ass.addView(relativeLayout2);
        LinearLayout.LayoutParams titleviewparams = new LinearLayout.LayoutParams(-1, -2);
        titleviewparams.setMargins(13, 0, 0, 0);
        TextView textView = new TextView(this);
        textView.setLayoutParams(titleviewparams);
        textView.setText("Command-line arguments");
        textView.setTextAppearance(this, 16842816);
        cmdArgs = new EditText(this);
        cmdArgs.setLayoutParams(buttonparams);
        cmdArgs.setSingleLine(true);
        if (sdk < 21) {
            cmdArgs.setBackgroundColor(-13290187);
            cmdArgs.setTextColor(-13421773);
            cmdArgs.setPadding(5, 0, 5, 5);
        }
        TextView textView2 = new TextView(this);
        textView2.setLayoutParams(titleviewparams);
        textView2.setText("env's");
        textView2.setTextAppearance(this, 16842816);
        EnvEdit = new EditText(this);
        EnvEdit.setLayoutParams(buttonparams);
        EnvEdit.setSingleLine(true);
        if (sdk < 21) {
            EnvEdit.setBackgroundColor(-13290187);
            EnvEdit.setTextColor(-13421773);
            EnvEdit.setPadding(5, 0, 5, 5);
        }
        TextView textView3 = new TextView(this);
        textView3.setLayoutParams(titleviewparams);
        textView3.setText("Path to game resources:");
        textView3.setTextAppearance(this, 16842816);
        GamePath = new EditText(this);
        GamePath.setLayoutParams(buttonparams);
        GamePath.setSingleLine(true);
        if (sdk < 21) {
            GamePath.setBackgroundColor(-13290187);
            GamePath.setTextColor(-13421773);
            GamePath.setPadding(5, 0, 5, 5);
        }
        immersiveMode = new CheckBox(this);
        immersiveMode.setLayoutParams(buttonparams);
        immersiveMode.setText("Immersive Mode");
        showtouch = new CheckBox(this);
        showtouch.setLayoutParams(buttonparams);
        showtouch.setText("Show Touch");
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-1, -2);
        layoutParams2.height = 50;
        layoutParams2.addRule(12);
        relativeLayout.setLayoutParams(layoutParams2);
        Button button = new Button(this);
        button.setText(styleButtonString("Launch Source!"));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2, 1.0f);
        button.setLayoutParams(buttonParams);
        if (sdk < 21) {
            button.getBackground().setAlpha(96);
            button.getBackground().invalidateSelf();
            button.setTextColor(-1);
            button.setTextAppearance(this, 16842816);
            button.setTextSize(20.0f);
        }
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LauncherActivity.this.startSource(v);
            }
        });
        Button aboutButton = new Button(this);
        aboutButton.setText(styleButtonString("About"));
        aboutButton.setLayoutParams(buttonParams);
        if (sdk < 21) {
            aboutButton.getBackground().setAlpha(96);
            aboutButton.getBackground().invalidateSelf();
            aboutButton.setTextColor(-1);
            aboutButton.setTextAppearance(this, 16842816);
            aboutButton.setTextSize(20.0f);
        }
        aboutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Dialog dialog = new Dialog(LauncherActivity.this);
                dialog.setTitle("About");
                ScrollView scroll = new ScrollView(LauncherActivity.this);
                scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
                TextView text = new TextView(LauncherActivity.this);
                text.setText("\nSpecial thanks to:\nptitSeb for gl4es: https://github.com/ptitSeb/gl4es\nnillerusr for port\nvalve for source engine\n\nNot for commercial use!\n\nDonate: https://www.patreon.com/nillerusr\nhttps://donatepay.ru/don/nillerusr");
                text.setLinksClickable(true);
                Linkify.addLinks(text, Pattern.compile("[a-z]+:\\/\\/[^ \\n]*"), GameInfo.extras_obb);
                scroll.addView(text);
                dialog.setContentView(scroll);
                dialog.show();
            }
        });
        launcherBody.addView(textView);
        launcherBody.addView(cmdArgs);
        launcherBody.addView(textView2);
        launcherBody.addView(EnvEdit);
        launcherBody.addView(textView3);
        launcherBody.addView(GamePath);
        if (sdk >= 19) {
            launcherBody.addView(immersiveMode);
        }
        launcherBody.addView(showtouch);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        relativeLayout.addView(linearLayout2);
        linearLayout2.addView(aboutButton);
        linearLayout2.addView(button);
        ass.addView(relativeLayout);
        launcher.addView(ass);
        setContentView(launcher);
        mPref = getSharedPreferences("mod", 0);
        cmdArgs.setText(mPref.getString("argv", "+developer 1"));
        GamePath.setText(mPref.getString("gamepath", getDefaultDir() + "/srceng"));
        EnvEdit.setText(mPref.getString("env", "LIBGL_USEVBO=0"));
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

    public void startSource(View view) {
        String argv = cmdArgs.getText().toString();
        String gamepath = GamePath.getText().toString();
        String env = EnvEdit.getText().toString();
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("argv", argv);
        editor.putString("gamepath", gamepath);
        editor.putString("env", env);
        boolean rodir = mPref.getBoolean("rodir", false);
        File main_obb = new File(gamepath + "/" + GameInfo.main_obb);
        File patch_obb = new File(gamepath + "/" + GameInfo.patch_obb);
        if (!main_obb.exists() || main_obb.isDirectory() || !patch_obb.exists() || patch_obb.isDirectory()) {
            new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There are no obb files on the path " + gamepath)
                .setPositiveButton("OK", (DialogInterface.OnClickListener) null)
                .show();
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
