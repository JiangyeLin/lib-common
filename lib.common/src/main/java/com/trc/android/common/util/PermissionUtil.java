package com.trc.android.common.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author HanTuo on 16/8/5.
 * 检查权限，必要的化申请权限
 */
public class PermissionUtil extends AppCompatActivity {
    public static final String INTENT_KEY_PERMISSIONS = "INTENT_KEY_PERMISSIONS";
    public static final String INTENT_KEY_PERMISSION_TIPS = "INTENT_KEY_PERMISSION_TIPS";
    public static final String ACTION_REQUEST_PERMISSION = "TRMALL:ACTION_REQUEST_MERMISSION";//TRC
    public static final String KEY_IS_GRANTED = "KEY_IS_GRANTED";
    public static final String KEY_DENIED_LIST = "KEY_DENIED_LIST";
    private ArrayList<String> permissionList;
    private String tips;
    private AlertDialog alertDialog;

    public interface OnPermissionCallback {
        void onGranted();

        void onDenied();
    }

    public interface OnPermissionsCallback {
        void onGranted();

        void onDenied(List<String> deniedPermissions);
    }

    private static boolean isPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }


    public static void requestPermissions(Context context, String[] permissions, @NonNull String tips, @NonNull final OnPermissionsCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isPermissionsGranted(context, permissions)) {
                callback.onGranted();
            } else {
                BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        context.unregisterReceiver(this);
                        boolean isGranted = intent.getBooleanExtra(KEY_IS_GRANTED, false);
                        if (isGranted) {
                            callback.onGranted();
                        } else {
                            callback.onDenied(Collections.EMPTY_LIST);
                        }
                    }
                };
                IntentFilter intentFilter = new IntentFilter(ACTION_REQUEST_PERMISSION);
                context.registerReceiver(broadcastReceiver, intentFilter);
                boolean isFromActivity = context instanceof Activity;
                Intent intent = new Intent(context, PermissionUtil.class);
                ArrayList<String> permissionList = new ArrayList<>(permissions.length);
                permissionList.addAll(Arrays.asList(permissions));
                intent.putStringArrayListExtra(INTENT_KEY_PERMISSIONS, permissionList);
                intent.putExtra(INTENT_KEY_PERMISSION_TIPS, tips);
                if (!isFromActivity) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                if (isFromActivity) {
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            }
        } else {
            callback.onGranted();
        }

    }


    public static void requestPermission(Context context, String permission, @NonNull String tips, @NonNull final OnPermissionCallback callback) {
        requestPermissions(context, new String[]{permission}, tips, new OnPermissionsCallback() {
            @Override
            public void onGranted() {
                callback.onGranted();
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                callback.onDenied();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionList = getIntent().getStringArrayListExtra(INTENT_KEY_PERMISSIONS);
        tips = getIntent().getStringExtra(INTENT_KEY_PERMISSION_TIPS);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isAllPermissionGranted = true;
        ArrayList<String> deniedList = new ArrayList<>(permissions.length);
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isAllPermissionGranted = false;
                deniedList.add(permissions[i]);
            }
        }
        if (isAllPermissionGranted) {
            Intent intent = new Intent(ACTION_REQUEST_PERMISSION);
            intent.putExtra(KEY_IS_GRANTED, true);
            intent.putStringArrayListExtra(KEY_DENIED_LIST, deniedList);
            sendBroadcast(intent);
            finish();
        } else {
            showExplainationDialog();
        }
    }

    private void showExplainationDialog() {
        final ArrayList deniedList = new ArrayList();
        for (String permission : permissionList) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                deniedList.add(permission);
            }
        }
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar)
                    .setTitle("权限申请")
                    .setMessage(tips)
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ACTION_REQUEST_PERMISSION);
                            intent.putExtra(KEY_IS_GRANTED, false);
                            intent.putExtra(KEY_DENIED_LIST, deniedList);
                            sendBroadcast(intent);
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create();
        }
        if (!alertDialog.isShowing())
            alertDialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private boolean shouldShowExplainationDialog() {
        for (String permission : permissionList) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == alertDialog || !alertDialog.isShowing()) {
            if (shouldShowExplainationDialog()) {
                showExplainationDialog();
            } else {
                String[] permissions = new String[permissionList.size()];
                permissionList.toArray(permissions);
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }
}
