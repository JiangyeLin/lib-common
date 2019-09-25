package com.trc.android.common.h5;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.trc.android.common.util.Base64Util;

import java.net.URLDecoder;

/**
 * @author HanTuo on 2017/3/3.
 */

public class ParamsUtil {
    @Nullable
    public static String getBase64EncodedParameter(Uri uri, String key) {
        String queryParameter = uri.getQueryParameter(key);
        if (null == queryParameter) {
            return null;
        }
        return Base64Util.decodeString(queryParameter);
    }

    @Nullable
    public static String getUriEncodedParameter(Uri uri, String key) {
        String queryParameter = uri.getQueryParameter(key);
        if (null == queryParameter) {
            return null;
        }
        return URLDecoder.decode(queryParameter);
    }
}
