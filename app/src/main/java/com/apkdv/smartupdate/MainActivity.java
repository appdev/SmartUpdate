package com.apkdv.smartupdate;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.apkdv.smartupdate.util.ApkUtils;
import com.lypeer.fcpermission.FcPermissionsB;
import com.lypeer.fcpermission.impl.OnPermissionsDeniedListener;
import com.lypeer.fcpermission.impl.OnPermissionsGrantedListener;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;
    private FcPermissionsB mFcPermissionsB;
    private static final int RC_STORAGE = 33;
    private static final int PATCH_FAIL = -5;
    public static final String PATH = Environment.getExternalStorageDirectory() + "/smartUpdate/";
    //合成得到的新版apk
    public static final String NEW_APK_PATH = PATH + "smartUpdateNew.apk";
    //从服务器下载来的差分包
    public static final String PATCH_PATH = PATH + "update.patch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("合并中....");
        mProgressDialog.setCancelable(false);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //合并差分包,先请求权限

                requestNetWorkPermission();

            }
        });
        ((WebView) findViewById(R.id.webview)).loadUrl("https://www.apkdv.com/android-smart-app-update/");
    }


    public void patchUpdate() {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog.show();
            }

            @Override
            protected Integer doInBackground(String... params) {
                String oldApkPath = ApkUtils.getSourceApkPath(MainActivity.this, "com.apkdv.smallupdate");
                if (!TextUtils.isEmpty(oldApkPath) && new File(PATCH_PATH).exists()) {
                    return PatchUtil.patchUpdate(oldApkPath, NEW_APK_PATH, PATCH_PATH);
                } else
                    return PATCH_FAIL;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                mProgressDialog.dismiss();
                if (integer == 0) {
                    Toast.makeText(MainActivity.this, "合并成功", Toast.LENGTH_SHORT).show();
                    ApkUtils.installApk(MainActivity.this, NEW_APK_PATH);
                } else if (integer == PATCH_FAIL) {
                    Toast.makeText(MainActivity.this, "合并失败", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    private void requestNetWorkPermission() {
        mFcPermissionsB = new FcPermissionsB.Builder(this)
                .onGrantedListener(new OnPermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(int requestCode, List<String> perms) {
                        patchUpdate();

                    }
                })
                .onDeniedListener(new OnPermissionsDeniedListener() {
                    @Override
                    public void onPermissionsDenied(int requestCode, List<String> perms) {

                    }
                })
                .positiveBtn4ReqPer(android.R.string.ok)
                .negativeBtn4ReqPer(R.string.cancel)
                .positiveBtn4NeverAskAgain(R.string.setting)
                .negativeBtn4NeverAskAgain(R.string.cancel)
                .rationale4ReqPer(getString(R.string.prompt_request_storage))
                .rationale4NeverAskAgain(getString(R.string.prompt_we_need_storage))
                .requestCode(RC_STORAGE)//必需
                .build();
        mFcPermissionsB.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mFcPermissionsB.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


}
