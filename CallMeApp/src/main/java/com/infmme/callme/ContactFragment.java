package com.infmme.callme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class ContactFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	@SuppressLint("InlinedApi")
	private static final String[] PROJECTION =
			{
					Data._ID,
					Contacts._ID,
					Phone._ID,
					Contacts.LOOKUP_KEY,
					Phone.DISPLAY_NAME,
					Phone.NUMBER,
					Contacts.PHOTO_THUMBNAIL_URI
			};

	public static final int DATA_ID_INDEX = 0;
	public static final int CONTACT_ID_INDEX = 1;
	public static final int PHONE_ID_INDEX = 2;
	public static final int CONTACT_KEY_INDEX = 3;
	public static final int NAME_INDEX = 4;
	public static final int PHONE_NUMBER_INDEX = 5;
	public static final int PHOTO_THUMBNAIL_INDEX = 6;

	@SuppressLint("InlinedApi")
	private static final String NAME_SELECTION =
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
					Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
					Contacts.DISPLAY_NAME + " LIKE ?";

	private String searchString;
	private String[] selectionArgs = {searchString};

	private ListView contactList;
	private ContactListAdapter adapter;

	public ContactFragment(){}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_contact_list, container, false);
		contactList = (ListView) layout.findViewById(R.id.contact_list_view);
		contactList.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState){
				adapter.hideActionView();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
			}
		});
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		adapter = new ContactListAdapter(getActivity());
		contactList.setAdapter(adapter);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
		searchString = "";
		selectionArgs[0] = "%" + searchString + "%";

		String sortOrder = Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		return new CursorLoader(
				getActivity(),
				Phone.CONTENT_URI,
				PROJECTION,
				NAME_SELECTION,
				selectionArgs,
				sortOrder
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
		Set<String> numberSet = new HashSet<String>();
		cursor.moveToFirst();

		MatrixCursor matrixCursor = new MatrixCursor(cursor.getColumnNames());
		while(!cursor.isAfterLast()){
			String contactNumber = ContactListAdapter.normalize(cursor.getString(PHONE_NUMBER_INDEX));

			if(!TextUtils.isEmpty(contactNumber) &&
					!numberSet.contains(contactNumber) &&
					TextUtils.isDigitsOnly(contactNumber)){
				numberSet.add(contactNumber);
				matrixCursor.addRow(new Object[]{
						cursor.getString(DATA_ID_INDEX),
						cursor.getString(CONTACT_ID_INDEX),
						cursor.getString(PHONE_ID_INDEX),
						cursor.getString(CONTACT_KEY_INDEX),
						cursor.getString(NAME_INDEX),
						cursor.getString(PHONE_NUMBER_INDEX),
						cursor.getString(PHOTO_THUMBNAIL_INDEX)
				});
			}

			cursor.moveToNext();
		}

		adapter.changeCursor(matrixCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader){
		adapter.changeCursor(null);
	}

	@Override
	public void onStop(){
		getActivity().finish();
		super.onStop();
	}

	private View genListEmptyView(final Context context){
		TextView textView = new TextView(context);
		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
												ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		textView.setLayoutParams(params);
		textView.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
		textView.setText("Le text");
		return textView;
	}
}
