package com.infmme.callme;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ContactListAdapter extends SimpleCursorAdapter {

	private static final int THUMBNAIL_SIZE = 200;

	private int usedItemPosition = -1;
	private View usedItemView;

	public ContactListAdapter(Context context){
		super(context, R.layout.contact_list_item, null, new String[]{}, new int[]{});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent){
		return LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);
	}

	@Override
	public void bindView(@NonNull View view, final Context context, @NonNull Cursor cursor){
		final int position = cursor.getPosition();
		final String contactName = cursor.getString(ContactFragment.NAME_INDEX);
		final String contactNumber = normalize(cursor.getString(ContactFragment.PHONE_NUMBER_INDEX));

		((TextView) view.findViewById(R.id.contact_name)).setText(contactName);
		((TextView) view.findViewById(R.id.contact_phone_number)).setText(genReadableNumber(contactNumber));
		setPhotoThumbnail(context, (ImageView) view.findViewById(R.id.contact_pic), cursor);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				if (usedItemPosition != position || usedItemView == null){
					hideActionView();

					ViewFlipper viewFlipper = (ViewFlipper) v.findViewById(R.id.flipper_image);
					viewFlipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.abc_fade_in));
					viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.abc_fade_out));
					viewFlipper.showNext();
					usedItemView = v;
					usedItemPosition = position;
				} else {
					hideActionView();
				}
			}
		});

		(view.findViewById(R.id.button_send)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				String prefix = PreferenceManager.getDefaultSharedPreferences(context).
						getString(MainActivity.CAREER_PREF_TAG, "*104*");
				context.startActivity(getCallIntent(prefix, contactNumber));
			}
		});
	}

	public static String normalize(String number){
		String result = number.replaceAll(" ", "").replaceAll("-", "");
		if (result.substring(0, 3).equals("+38"))
			result = result.substring(3);
		if (result.charAt(0) == '8')
			result = result.substring(1);
		if (result.length() != 10)
			result = "";
		return result;
	}

	public void hideActionView(){
		if (usedItemView != null && usedItemPosition != -1){
			((ViewFlipper) usedItemView.findViewById(R.id.flipper_image)).showPrevious();
			usedItemView = null;
			usedItemPosition = -1;
		}
	}

	private String genReadableNumber(String number){
		return (number.length() == 10)
				? number.substring(0, 3) + " " +
				  number.substring(3, 6) + " " +
				  number.substring(6)
				: "";
	}

	private String getServiceNumber(String prefix, String number){
		return prefix + number + Uri.encode("#");
	}

	private Intent getCallIntent(String prefix, String contactNumber){
		return (new Intent(Intent.ACTION_CALL)).setData(Uri.parse("tel:" + getServiceNumber(prefix, contactNumber)));
	}

	private void setPhotoThumbnail(Context context, ImageView imageView, Cursor cursor){
		int tColumn = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				? ContactFragment.PHOTO_THUMBNAIL_INDEX
				: ContactFragment.CONTACT_ID_INDEX;
		String photoData = cursor.getString(tColumn);

		if (photoData == null){
			imageView.setImageResource(R.drawable.icon_contacts);
			return;
		}
		AssetFileDescriptor afd = null;
		Bitmap photoBitmap = null;
		try{
			Uri thumbUri;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				thumbUri = Uri.parse(photoData);
			} else {
				final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);
				thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);

			}
			afd = context.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
			FileDescriptor fileDescriptor = afd.getFileDescriptor();

			if (fileDescriptor != null){
				photoBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
			}
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} finally {
			if (afd != null) {
				try {
					afd.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (photoBitmap != null){
			imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(photoBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE));
		} else {
			imageView.setImageResource(R.drawable.icon_contacts);
		}
	}
}
