package com.slightech.mynt.sdk.demo.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 *
 * @see <a href="http://developer.android.com/samples/RuntimePermissions/src/
 * com.example.android.system.runtimepermissions/PermissionUtil.html">PermissionUtil.java</a>
 * @see <a href="https://github.com/tbruyelle/RxPermissions">RxPermissions</a>
 */
public class PermissionUtils {

    /**
     * Check that the specific permission has been granted.
     */
    public static boolean checkPermissionGranted(@NonNull Context context,
                                                 @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check that all specific permissions have been granted.
     *
     * @see ContextCompat#checkSelfPermission(Context, String)
     */
    public static boolean checkPermissionsGranted(@NonNull Context context,
                                                  @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that should show request permission rationale.
     */
    public static boolean shouldPermissionShowRationale(@NonNull Activity activity,
                                                        @NonNull String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Check that should show request permission rationale.
     *
     * @see ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)
     */
    public static boolean shouldPermissionsShowRationale(@NonNull Activity activity,
                                                         @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that the given permission has been granted.
     */
    public static boolean verifyPermission(int[] grantResults) {
        return grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
