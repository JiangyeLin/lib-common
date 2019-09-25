package com.trc.android.common.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


public class ContactSelectUtil {

    public static void select(final FragmentActivity fragmentActivity, final SelectCallback callback) {
        PermissionUtil.requestPermission(fragmentActivity, Manifest.permission.READ_CONTACTS, "选择联系人需要APP获取联系人读取的权限", new PermissionUtil.OnPermissionCallback() {
            @Override
            public void onGranted() {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(ContactsContract.Contacts.CONTENT_URI);
                LifeCircleCallbackUtil.start(fragmentActivity, intent, new LifeCircleCallbackUtil.Callback() {
                    @Override
                    void onActivityResult(Fragment fragment, int resultCode, Intent data) {
                        try {
                            ContentResolver contentResolver = fragmentActivity.getContentResolver();
                            Cursor cursor = contentResolver.query(data.getData(), null, null, null, null);
                            final String[] contactName = new String[1];
                            if (cursor.moveToFirst()) {
                                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                                Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                                final CharSequence[] phoneNumbers = new CharSequence[phoneCursor.getCount()];
                                if (phoneNumbers.length == 0) return;
                                int index = 0;
                                while (phoneCursor.moveToNext()) {
                                    contactName[0] = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    phoneNumber = phoneNumber.replace(" ", "");
                                    phoneNumber = phoneNumber.replace("-", "");
                                    phoneNumber = phoneNumber.replace("+86", "");
                                    phoneNumbers[index++] = phoneNumber;
                                }
                                phoneCursor.close();
                                if (phoneNumbers.length == 1) {
                                    final String number = phoneNumbers[0].toString();
                                    callback.onSelectSuccess(contactName[0], number);
                                } else {
                                    new AlertDialog.Builder(fragmentActivity).setItems(phoneNumbers, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String number = phoneNumbers[which].toString();
                                            callback.onSelectSuccess(contactName[0], number);
                                        }
                                    }).create().show();
                                }
                            }
                            cursor.close();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onDenied() {
                Toast.makeText(fragmentActivity, "未授予联系人读取权限，无法跳转到系统联系人选择页面", Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface SelectCallback {
        void onSelectSuccess(String name, String phone);
    }

}
