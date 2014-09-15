package com.infmme.callme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private final static String CONTACT_LIST_FRAGMENT_TAG = "97";
	public final static String CAREER_PREF_TAG = "crr172";
	public final static String PREF_NEWCOMER = "nnnnnnnnu?";

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startContactListFragment();
		if (!isAnybodyOutThere(this))
			buildCareerChooserDialog(this);
	}

	private boolean isAnybodyOutThere(Context context){
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_NEWCOMER, false)){
			PreferenceManager.getDefaultSharedPreferences(context)
							 .edit()
							 .putBoolean(PREF_NEWCOMER, true)
							 .apply();
			return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.action_set_prefix){
			buildCareerChooserDialog(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void buildCareerChooserDialog(final Context context){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final View layout = LayoutInflater.from(context).inflate(R.layout.prefix_setter_dialog, null);
		final EditText editTextPrefix = (EditText) layout.findViewById(R.id.prefix_edit_text);
		String oldPrefix = getPrefix(context);
		if (!TextUtils.isEmpty(oldPrefix))
			editTextPrefix.setText(oldPrefix);

		builder.setTitle(R.string.prefix_dialog_title).
				setView(layout).
				setPositiveButton(android.R.string.ok,
								  new DialogInterface.OnClickListener() {
									  @Override
									  public void onClick(DialogInterface dialog, int which){
										  String prefix =
												  editTextPrefix.getText().toString();
										  if (validatePrefix(prefix)){
											  setPrefix(context, prefix);
										  } else {
											  Toast.makeText(context, R.string.prefix_is_invalid, Toast.LENGTH_SHORT).show();
										  }
									  }
								  });
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private static void setPrefix(final Context context, String careerName){
		if (!TextUtils.isEmpty(careerName)){
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(CAREER_PREF_TAG, careerName);
			editor.apply();
		} else {
			throw new IllegalArgumentException("careerName is null or empty");
		}
	}

	public static String getPrefix(final Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getString(CAREER_PREF_TAG, "");
	}

	private static boolean validatePrefix(String prefix){
		return (prefix.length() == 5 &&
				prefix.charAt(0) == '*' &&
				prefix.charAt(4) == '*');
	}

	private void startContactListFragment(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment existing = fragmentManager.findFragmentByTag(CONTACT_LIST_FRAGMENT_TAG);
		if (existing == null){
			fragmentManager.beginTransaction().
					replace(R.id.fragment_container, new ContactFragment(), CONTACT_LIST_FRAGMENT_TAG).
								   addToBackStack(null).
								   setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
								   commit();
		}
	}
}
