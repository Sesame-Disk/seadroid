package com.nihaocloud.sesamedisk.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.collect.Maps;
import com.nihaocloud.sesamedisk.NihaoApplication;
import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.SeafException;
import com.nihaocloud.sesamedisk.SettingsManager;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.account.AccountInfo;
import com.nihaocloud.sesamedisk.account.AccountManager;
import com.nihaocloud.sesamedisk.cameraupload.CameraUploadConfigActivity;
import com.nihaocloud.sesamedisk.cameraupload.CameraUploadManager;
import com.nihaocloud.sesamedisk.cameraupload.GalleryBucketUtils;
import com.nihaocloud.sesamedisk.data.CameraSyncEvent;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.DatabaseHelper;
import com.nihaocloud.sesamedisk.data.ServerInfo;
import com.nihaocloud.sesamedisk.data.StorageManager;
import com.nihaocloud.sesamedisk.gesturelock.LockPatternUtils;
import com.nihaocloud.sesamedisk.ui.activity.BrowserActivity;
import com.nihaocloud.sesamedisk.ui.activity.CreateGesturePasswordActivity;
import com.nihaocloud.sesamedisk.ui.activity.PrivacyPolicyActivity;
import com.nihaocloud.sesamedisk.ui.activity.SeafilePathChooserActivity;
import com.nihaocloud.sesamedisk.ui.activity.SettingsActivity;
import com.nihaocloud.sesamedisk.ui.dialog.ClearCacheTaskDialog;
import com.nihaocloud.sesamedisk.ui.dialog.ClearPasswordTaskDialog;
import com.nihaocloud.sesamedisk.ui.dialog.SwitchStorageTaskDialog;
import com.nihaocloud.sesamedisk.ui.dialog.TaskDialog.TaskDialogListener;
import com.nihaocloud.sesamedisk.util.ConcurrentAsyncTask;
import com.nihaocloud.sesamedisk.util.Utils;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends CustomPreferenceFragment {
    private static final String DEBUG_TAG = "SettingsFragment";

    public static final String CAMERA_UPLOAD_BOTH_PAGES = "com.seafile.seadroid2.camera.upload";
    public static final String CAMERA_UPLOAD_REMOTE_LIBRARY = "com.seafile.seadroid2.camera.upload.library";
    public static final String CAMERA_UPLOAD_LOCAL_DIRECTORIES = "com.seafile.seadroid2.camera.upload.directories";
    public static final String CONTACTS_UPLOAD_REMOTE_LIBRARY = "com.seafile.seadroid2.contacts.upload.library";
    public static final int CHOOSE_CAMERA_UPLOAD_REQUEST = 2;
    //    public static final int CHOOSE_CONTACTS_UPLOAD_REQUEST = 3;
    // Account Info
    private static Map<String, AccountInfo> accountInfoMap = Maps.newHashMap();

    // Camera upload
    private PreferenceCategory cUploadCategory;
    private PreferenceScreen cUploadAdvancedScreen;
    private PreferenceCategory cUploadAdvancedCategory;
    private Preference cUploadRepoPref;
    private CheckBoxPreference cCustomDirectoriesPref;
    private Preference cLocalDirectoriesPref;
    // privacy
    private PreferenceCategory cPrivacyCategory;
    private Preference clientEncPref;

    private SettingsActivity mActivity;
    private String appVersion;
    public SettingsManager settingsMgr;
    private CameraUploadManager cameraManager;
    //    public ContactsUploadManager contactsManager;
    private AccountManager accountMgr;
    private DataManager dataMgr;
    private StorageManager storageManager = StorageManager.getInstance();
    //    private PreferenceCategory cContactsCategory;
//    private Preference cContactsRepoPref;
//    private Preference cContactsRepoTime;
//    private Preference cContactsRepoBackUp;
//    private Preference cContactsRepoRecovery;
    private long mMtime;
    private Preference cUploadRepoState;

    @Override
    public void onAttach(Activity activity) {
        Log.d(DEBUG_TAG, "onAttach");
        super.onAttach(activity);

        // global variables
        mActivity = (SettingsActivity) getActivity();
        settingsMgr = SettingsManager.instance();
        accountMgr = new AccountManager(mActivity);
        cameraManager = new CameraUploadManager(mActivity.getApplicationContext());
//        contactsManager = new ContactsUploadManager(mActivity.getApplicationContext());
        Account act = accountMgr.getCurrentAccount();
        dataMgr = new DataManager(act);
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        settingsMgr.registerSharedPreferencesListener(settingsListener);
        Account account = accountMgr.getCurrentAccount();
        if (!Utils.isNetworkOn()) {
            mActivity.showShortToast(mActivity, R.string.network_down);
            return;
        }

        ConcurrentAsyncTask.execute(new RequestAccountInfoTask(), account);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.d(DEBUG_TAG, "onDestroy()");
        settingsMgr.unregisterSharedPreferencesListener(settingsListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        // User info
        String identifier = getCurrentUserIdentifier();
        findPreference(SettingsManager.SETTINGS_ACCOUNT_INFO_KEY).setSummary(identifier);

        // Space used
        Account currentAccount = accountMgr.getCurrentAccount();
        if (currentAccount != null) {
            String signature = currentAccount.getSignature();
            AccountInfo info = getAccountInfoBySignature(signature);
            if (info != null) {
                String spaceUsed = info.getSpaceUsed();
                findPreference(SettingsManager.SETTINGS_ACCOUNT_SPACE_KEY).setSummary(spaceUsed);
            }
        }

        // Gesture Lock
        findPreference(SettingsManager.GESTURE_LOCK_SWITCH_KEY).setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof Boolean) {
                boolean isChecked = (Boolean) newValue;
                if (isChecked) {
                    // inverse checked status
                    Intent newIntent = new Intent(getActivity(), CreateGesturePasswordActivity.class);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(newIntent, SettingsManager.GESTURE_LOCK_REQUEST);
                } else {
                    LockPatternUtils mLockPatternUtils = new LockPatternUtils(getActivity());
                    mLockPatternUtils.clearLock();
                }
                return true;
            }

            return false;
        });

        // Sign out
        findPreference(SettingsManager.SETTINGS_ACCOUNT_SIGN_OUT_KEY).setOnPreferenceClickListener(preference -> {

            // popup a dialog to confirm sign out request
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(getString(R.string.settings_account_sign_out_title));
            builder.setMessage(getString(R.string.settings_account_sign_out_confirm));
            builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Account account = accountMgr.getCurrentAccount();

                    // sign out operations
                    accountMgr.signOutAccount(account);

                    // password auto clear
                    if (settingsMgr.isPasswordAutoClearEnabled()) {
                        clearPasswordSilently();
                    }

                    // restart BrowserActivity (will go to AccountsActivity)
                    Intent intent = new Intent(mActivity, BrowserActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(intent);
                    mActivity.finish();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // dismiss
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        });

        findPreference(SettingsManager.CLEAR_PASSOWR_SWITCH_KEY).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // clear password
                clearPassword();
                return true;
            }
        });

        // auto clear passwords when logout
        findPreference(SettingsManager.AUTO_CLEAR_PASSOWR_SWITCH_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    boolean isChecked = (Boolean) newValue;
                    // inverse checked status
                    settingsMgr.setupPasswordAutoClear(!isChecked);
                    return true;
                }

                return false;
            }
        });
        if (currentAccount != null) {
            final ServerInfo serverInfo = accountMgr.getServerInfo(currentAccount);

            cPrivacyCategory = (PreferenceCategory) findPreference(SettingsManager.PRIVACY_CATEGORY_KEY);
            // Client side encryption for encrypted Library
            clientEncPref = findPreference(SettingsManager.CLIENT_ENC_SWITCH_KEY);
            clientEncPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue instanceof Boolean) {
                        boolean isChecked = (Boolean) newValue;
                        // inverse checked status
                        settingsMgr.setupEncrypt(!isChecked);
                        return true;
                    }

                    return false;
                }
            });

            if (serverInfo != null && !serverInfo.canLocalDecrypt()) {
                cPrivacyCategory.removePreference(clientEncPref);
            }
        }
        // Camera Upload
        cUploadCategory = (PreferenceCategory) findPreference(SettingsManager.CAMERA_UPLOAD_CATEGORY_KEY);
        cUploadAdvancedScreen = (PreferenceScreen) findPreference(SettingsManager.CAMERA_UPLOAD_ADVANCED_SCREEN_KEY);
        cUploadAdvancedCategory = (PreferenceCategory) findPreference(SettingsManager.CAMERA_UPLOAD_ADVANCED_CATEGORY_KEY);

        findPreference(SettingsManager.CAMERA_UPLOAD_SWITCH_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    boolean isChecked = (Boolean) newValue;
                    if (!isChecked) {
                        cUploadCategory.removePreference(cUploadRepoPref);
                        cUploadCategory.removePreference(cUploadAdvancedScreen);
                        cameraManager.disableCameraUpload();
                    } else {
                        Intent intent = new Intent(mActivity, CameraUploadConfigActivity.class);
                        intent.putExtra(CAMERA_UPLOAD_BOTH_PAGES, true);
                        startActivityForResult(intent, CHOOSE_CAMERA_UPLOAD_REQUEST);
                    }
                    return true;
                }

                return false;
            }
        });

        // Change upload library
        cUploadRepoPref = findPreference(SettingsManager.CAMERA_UPLOAD_REPO_KEY);
        cUploadRepoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // choose remote library
                Intent intent = new Intent(mActivity, CameraUploadConfigActivity.class);
                intent.putExtra(CAMERA_UPLOAD_REMOTE_LIBRARY, true);
                startActivityForResult(intent, CHOOSE_CAMERA_UPLOAD_REQUEST);

                return true;
            }
        });

        cUploadRepoState = findPreference(SettingsManager.CAMERA_UPLOAD_STATE);
        cUploadRepoState.setSummary(Utils.getUploadStateShow(getActivity()));

        // Contacts Upload
//        cContactsCategory = (PreferenceCategory) findPreference(SettingsManager.CONTACTS_UPLOAD_CATEGORY_KEY);
//        findPreference(SettingsManager.CONTACTS_UPLOAD_SWITCH_KEY).setOnPreferenceChangeListener(new Preference
//                .OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if (newValue instanceof Boolean) {
//                    boolean isChecked = (Boolean) newValue;
//                    if (isChecked) {
//                        Intent intent = new Intent(mActivity, ContactsUploadConfigActivity.class);
//                        intent.putExtra(CONTACTS_UPLOAD_REMOTE_LIBRARY, true);
//                        startActivityForResult(intent, CHOOSE_CONTACTS_UPLOAD_REQUEST);
//                    } else {
//                        cContactsCategory.removePreference(cContactsRepoPref);
//                        cContactsCategory.removePreference(cContactsRepoTime);
//                        cContactsCategory.removePreference(cContactsRepoBackUp);
//                        cContactsCategory.removePreference(cContactsRepoRecovery);
//                        contactsManager.disableContactsUpload();
//                    }
//                    return true;
//                }
//
//                return false;
//            }
//        });


        // Change contacts upload library
//        cContactsRepoPref = findPreference(SettingsManager.CONTACTS_UPLOAD_REPO_KEY);
//        cContactsRepoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent = new Intent(mActivity, ContactsUploadConfigActivity.class);
//                intent.putExtra(CONTACTS_UPLOAD_REMOTE_LIBRARY, true);
//                startActivityForResult(intent, CHOOSE_CONTACTS_UPLOAD_REQUEST);
//                return true;
//            }
//        });
//
//        cContactsRepoTime = findPreference(SettingsManager.CONTACTS_UPLOAD_REPO_TIME_KEY);
//
//        cContactsRepoBackUp = findPreference(SettingsManager.CONTACTS_UPLOAD_REPO_BACKUP_KEY);
//        cContactsRepoBackUp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                backupContacts();
//                return true;
//            }
//        });
//        cContactsRepoRecovery = findPreference(SettingsManager.CONTACTS_UPLOAD_REPO_RECOVERY_KEY);
//        cContactsRepoRecovery.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                recoveryContacts();
//                return false;
//            }
//        });
        // change local folder CheckBoxPreference
        cCustomDirectoriesPref = (CheckBoxPreference) findPreference(SettingsManager.CAMERA_UPLOAD_CUSTOM_BUCKETS_KEY);
        cCustomDirectoriesPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    boolean isCustom = (Boolean) newValue;
                    if (!isCustom) {
                        cUploadAdvancedCategory.removePreference(cLocalDirectoriesPref);
                        scanCustomDirs(false);
                    } else {
                        cUploadAdvancedCategory.addPreference(cLocalDirectoriesPref);
                        scanCustomDirs(true);
                    }
                    return true;
                }

                return false;
            }
        });

        // change local folder Preference
        cLocalDirectoriesPref = findPreference(SettingsManager.CAMERA_UPLOAD_BUCKETS_KEY);
        cLocalDirectoriesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // choose media buckets
                scanCustomDirs(true);

                return true;
            }
        });

        refreshCameraUploadView();
//        refreshContactsView();

        // App Version
        try {
            appVersion = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(DEBUG_TAG, "app version name not found exception");
            appVersion = getString(R.string.not_available);
        }
        findPreference(SettingsManager.SETTINGS_ABOUT_VERSION_KEY).setSummary(appVersion);

        // About author
        final Preference aboutPreference = findPreference(SettingsManager.SETTINGS_ABOUT_AUTHOR_KEY);
        aboutPreference.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            // builder.setIcon(R.drawable.icon);
            builder.setMessage(Html.fromHtml(getString(R.string.settings_about_author_info, appVersion)));
            builder.show();
            return true;
        });

        PreferenceCategory aboutCategory = (PreferenceCategory) findPreference("settings_about_key");
        if (aboutCategory != null && aboutPreference != null) {
            aboutCategory.removePreference(aboutPreference);
        }

        final Preference privacyPreference = findPreference(SettingsManager.SETTINGS_PRIVACY_POLICY_KEY);
        privacyPreference.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(mActivity, PrivacyPolicyActivity.class);
            mActivity.startActivity(intent);
            return true;
        });

        if (aboutCategory != null && privacyPreference != null) {
            aboutCategory.removePreference(privacyPreference);
        }

        // Cache size
        calculateCacheSize();

        // Clear cache
        findPreference(SettingsManager.SETTINGS_CLEAR_CACHE_KEY).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearCache();
                return true;
            }
        });

        // Storage selection only works on KitKat or later
        if (storageManager.supportsMultipleStorageLocations()) {
            updateStorageLocationSummary();
            findPreference(SettingsManager.SETTINGS_CACHE_DIR_KEY).setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new SwitchStorageTaskDialog().show(getFragmentManager(), "Select cache location");
                    return true;
                }
            });
        } else {
            PreferenceCategory cCacheCategory = (PreferenceCategory) findPreference(SettingsManager.SETTINGS_CACHE_CATEGORY_KEY);
            cCacheCategory.removePreference(findPreference(SettingsManager.SETTINGS_CACHE_DIR_KEY));
        }

    }

    //contacts  backup
//    private void backupContacts() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (mActivity.checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
//                    PackageManager.PERMISSION_GRANTED) {
//                //if not have read contacts permission to  request
//                mActivity.requestReadContactsPermission();
//            } else {
//                // have read contacts permission  to  show backup dialog
//                showUploadContactsDialog();
//            }
//        } else {
//            showUploadContactsDialog();
//        }
//    }

//    private void recoveryContacts() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (mActivity.checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
//                    PackageManager.PERMISSION_GRANTED) {
//                //if not have read contacts permission to  request
//                mActivity.requestReadContactsPermission();
//            } else {
//                // have read contacts permission  to  show recovery dialog
//                showRecoveryContactsDialog();
//            }
//        } else {
//            showRecoveryContactsDialog();
//        }
//    }
//
//    private void showRecoveryContactsDialog() {
//        ContactsDialog contactsDialog = new ContactsDialog(getActivity(), ContactsDialog.CONTACTS_RECOVERY);
//        contactsDialog.show(getFragmentManager(), "SettingsFragment");
//    }
//

//    public void showUploadContactsDialog() {
//
//        final ContactsDialog contactsDialog = new ContactsDialog(mActivity, ContactsDialog.CONTACTS_BACKUP);
//        contactsDialog.setTaskDialogLisenter(new TaskDialogListener() {
//            @Override
//            public void onTaskSuccess() {
//                long timeMillis = System.currentTimeMillis();
//                String s = Utils.translateCommitTime(timeMillis * 1000);
//                cContactsRepoTime.setSummary(s);
//            }
//        });
//        contactsDialog.show(mActivity.getSupportFragmentManager(), "SettingsFragment");
//    }


//    private void refreshContactsView() {
//        ((CheckBoxPreference) findPreference(SettingsManager.CONTACTS_UPLOAD_SWITCH_KEY))
//                .setChecked(contactsManager.isContactsUploadEnabled());
//
//        if (!contactsManager.isContactsUploadEnabled()) {
//            cContactsCategory.removePreference(cContactsRepoPref);
//            cContactsCategory.removePreference(cContactsRepoTime);
//            cContactsCategory.removePreference(cContactsRepoBackUp);
//            cContactsCategory.removePreference(cContactsRepoRecovery);
//        } else {
//            cContactsCategory.addPreference(cContactsRepoPref);
//            cContactsCategory.addPreference(cContactsRepoTime);
//            cContactsCategory.addPreference(cContactsRepoBackUp);
//            cContactsCategory.addPreference(cContactsRepoRecovery);
//
//            Account camAccount = contactsManager.getContactsAccount();
//            if (camAccount != null && settingsMgr.getContactsUploadRepoName() != null) {
//                cContactsRepoPref.setSummary(camAccount.getSignature()
//                        + "/" + settingsMgr.getContactsUploadRepoName()
//                        + "/" + SettingsActivity.BASE_DIR);
//            }
//
//            //show  backup  time
//            DataManager dataManager = new DataManager(camAccount);
//            String repoId = settingsMgr.getContactsUploadRepoId();
//            if (repoId != null) {
//                List<SeafDirent> dirents = dataManager.getCachedDirents(repoId, "/");
//                if (dirents != null) {
//                    for (int i = 0; i < dirents.size(); i++) {
//                        SeafDirent seafDirent = dirents.get(i);
//                        if (seafDirent.isDir() && seafDirent.getTitle().equals(SettingsActivity.BASE_DIR)) {
//                            String path = Utils.pathJoin("/", seafDirent.name);
//                            List<SeafDirent> childDirents = dataManager.getCachedDirents(repoId, path);
//                            if (childDirents != null) {
//                                for (int j = 0; j < childDirents.size(); j++) {
//                                    SeafDirent childFile = childDirents.get(j);
//                                    if (!childFile.isDir()) {
//                                        String title = childFile.getTitle();
//                                        if (title.indexOf("contacts") != -1) {
//                                            if (seafDirent.mtime > mMtime) {
//                                                mMtime = seafDirent.mtime;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if (mMtime > 0) {
//                        cContactsRepoTime.setSummary(Utils.translateCommitTime(mMtime * 1000));
//                    }
//                }
//            }
//        }
//    }


    private void clearPasswordSilently() {
        ConcurrentAsyncTask.submit(new Runnable() {
            @Override
            public void run() {
                DataManager.clearPassword();

                // clear cached data from database
                DatabaseHelper dbHelper = DatabaseHelper.getDatabaseHelper();
                dbHelper.clearEnckeys();
            }
        });
    }

    private void clearPassword() {
        ClearPasswordTaskDialog dialog = new ClearPasswordTaskDialog();
        dialog.setTaskDialogLisenter(new TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mActivity.showShortToast(mActivity, R.string.clear_password_successful);
            }

            @Override
            public void onTaskFailed(SeafException e) {
                mActivity.showShortToast(mActivity, R.string.clear_password_failed);
            }
        });
        dialog.show(getFragmentManager(), "DialogFragment");
    }

    private void updateStorageLocationSummary() {
        String summary = storageManager.getStorageLocation().description;
        findPreference(SettingsManager.SETTINGS_CACHE_DIR_KEY).setSummary(summary);
    }

    private void refreshCameraUploadView() {
        Account camAccount = cameraManager.getCameraAccount();
        if (camAccount != null && settingsMgr.getCameraUploadRepoName() != null) {
            cUploadRepoPref.setSummary(camAccount.getSignature()
                    + "/" + settingsMgr.getCameraUploadRepoName());
        }

        ((CheckBoxPreference) findPreference(SettingsManager.CAMERA_UPLOAD_SWITCH_KEY)).setChecked(cameraManager.isCameraUploadEnabled());

        if (cameraManager.isCameraUploadEnabled()) {
            cUploadCategory.addPreference(cUploadRepoPref);
            cUploadCategory.addPreference(cUploadAdvancedScreen);
        } else {
            cUploadCategory.removePreference(cUploadRepoPref);
            cUploadCategory.removePreference(cUploadAdvancedScreen);
        }

        // data plan:
        CheckBoxPreference cbDataPlan = ((CheckBoxPreference) findPreference(SettingsManager.CAMERA_UPLOAD_ALLOW_DATA_PLAN_SWITCH_KEY));
        if (cbDataPlan != null)
            cbDataPlan.setChecked(settingsMgr.isDataPlanAllowed());

        // videos
        CheckBoxPreference cbVideoAllowed = ((CheckBoxPreference) findPreference(SettingsManager.CAMERA_UPLOAD_ALLOW_VIDEOS_SWITCH_KEY));
        if (cbVideoAllowed != null)
            cbVideoAllowed.setChecked(settingsMgr.isVideosUploadAllowed());

        List<String> bucketNames = new ArrayList<>();
        List<String> bucketIds = settingsMgr.getCameraUploadBucketList();
        List<GalleryBucketUtils.Bucket> tempBuckets = GalleryBucketUtils.getMediaBuckets(getActivity().getApplicationContext());
        LinkedHashSet<GalleryBucketUtils.Bucket> bucketsSet = new LinkedHashSet<>(tempBuckets.size());
        bucketsSet.addAll(tempBuckets);
        List<GalleryBucketUtils.Bucket> allBuckets = new ArrayList<>(bucketsSet.size());
        Iterator iterator = bucketsSet.iterator();
        while (iterator.hasNext()) {
            GalleryBucketUtils.Bucket bucket = (GalleryBucketUtils.Bucket) iterator.next();
            allBuckets.add(bucket);
        }

        for (GalleryBucketUtils.Bucket bucket : allBuckets) {
            if (bucketIds.contains(bucket.id)) {
                bucketNames.add(bucket.name);
            }
        }

        if (bucketNames.isEmpty()) {
            cUploadAdvancedCategory.removePreference(cLocalDirectoriesPref);
            cCustomDirectoriesPref.setChecked(false);
        } else {
            cCustomDirectoriesPref.setChecked(true);
            cLocalDirectoriesPref.setSummary(TextUtils.join(", ", bucketNames));
            cUploadAdvancedCategory.addPreference(cLocalDirectoriesPref);
        }

    }

    private void clearCache() {
        ClearCacheTaskDialog dialog = new ClearCacheTaskDialog();
        dialog.setTaskDialogLisenter(new TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                // refresh cache size
                calculateCacheSize();
                //clear Glide cache
                Glide.get(NihaoApplication.getAppContext()).clearMemory();
                Toast.makeText(mActivity, getString(R.string.settings_clear_cache_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskFailed(SeafException e) {
                Toast.makeText(mActivity, getString(R.string.settings_clear_cache_failed), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getFragmentManager(), "DialogFragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SettingsManager.GESTURE_LOCK_REQUEST:
                if (resultCode == Activity.RESULT_OK) {


                } else if (resultCode == Activity.RESULT_CANCELED) {
                    ((CheckBoxPreference) findPreference(SettingsManager.GESTURE_LOCK_SWITCH_KEY)).setChecked(false);
                }
                break;

            case CHOOSE_CAMERA_UPLOAD_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        return;
                    }
                    final String repoName = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_NAME);
                    final String repoId = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_ID);
                    final Account account = data.getParcelableExtra(SeafilePathChooserActivity.DATA_ACCOUNT);
                    if (repoName != null && repoId != null) {
                        // Log.d(DEBUG_TAG, "Activating camera upload to " + account + "; " + repoName);
                        cameraManager.setCameraAccount(account);
                        settingsMgr.saveCameraUploadRepoInfo(repoId, repoName);
                    }

                } else if (resultCode == Activity.RESULT_CANCELED) {

                }
                refreshCameraUploadView();
                break;
//            case CHOOSE_CONTACTS_UPLOAD_REQUEST:
//                if (resultCode == Activity.RESULT_OK) {
//                    if (data == null) {
//                        return;
//                    }
//                    final String repoName = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_NAME);
//                    final String repoId = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_ID);
//                    final Account account = data.getParcelableExtra(SeafilePathChooserActivity.DATA_ACCOUNT);
//                    if (repoName != null && repoId != null) {
//                        //                        Log.d(DEBUG_TAG, "Activating contacts upload to " + account + "; " + repoName);
//                        contactsManager.setContactsAccount(account);
//                        settingsMgr.saveContactsUploadRepoInfo(repoId, repoName);
//                    }
//                } else if (resultCode == Activity.RESULT_CANCELED) {
//                }
////                refreshContactsView();
//                break;

            default:
                break;
        }

    }


    private void scanCustomDirs(boolean isCustomScanOn) {
        if (isCustomScanOn) {
            Intent intent = new Intent(mActivity, CameraUploadConfigActivity.class);
            intent.putExtra(CAMERA_UPLOAD_LOCAL_DIRECTORIES, true);
            startActivityForResult(intent, CHOOSE_CAMERA_UPLOAD_REQUEST);
        } else {
            List<String> selectedBuckets = new ArrayList<>();
            settingsMgr.setCameraUploadBucketList(selectedBuckets);
            refreshCameraUploadView();
        }
    }

    /**
     * automatically update Account info, like space usage, total space size, from background.
     */
    class RequestAccountInfoTask extends AsyncTask<Account, Void, AccountInfo> {

        @Override
        protected void onPreExecute() {
            mActivity.setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected AccountInfo doInBackground(Account... params) {
            AccountInfo accountInfo = null;

            if (params == null) return null;

            try {
                // get account info from server
                accountInfo = dataMgr.getAccountInfo();
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "could not get account info!", e);
            }

            return accountInfo;
        }

        @Override
        protected void onPostExecute(AccountInfo accountInfo) {
            mActivity.setSupportProgressBarIndeterminateVisibility(false);

            if (accountInfo == null) return;

            // update Account info settings
            findPreference(SettingsManager.SETTINGS_ACCOUNT_INFO_KEY).setSummary(getCurrentUserIdentifier());
            String spaceUsage = accountInfo.getSpaceUsed();
            findPreference(SettingsManager.SETTINGS_ACCOUNT_SPACE_KEY).setSummary(spaceUsage);
            Account currentAccount = accountMgr.getCurrentAccount();
            if (currentAccount != null)
                saveAccountInfo(currentAccount.getSignature(), accountInfo);
        }
    }

    public String getCurrentUserIdentifier() {
        Account account = accountMgr.getCurrentAccount();

        if (account == null)
            return "";

        return account.getDisplayName();
    }

    public void saveAccountInfo(String signature, AccountInfo accountInfo) {
        accountInfoMap.put(signature, accountInfo);
    }

    public AccountInfo getAccountInfoBySignature(String signature) {
        if (accountInfoMap.containsKey(signature))
            return accountInfoMap.get(signature);
        else
            return null;
    }

    private void calculateCacheSize() {
        ConcurrentAsyncTask.execute(new CalculateCacheTask());
    }

    class CalculateCacheTask extends AsyncTask<String, Void, Long> {

        @Override
        protected Long doInBackground(String... params) {
            return storageManager.getUsedSpace();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            String total = FileUtils.byteCountToDisplaySize(aLong);
            findPreference(SettingsManager.SETTINGS_CACHE_SIZE_KEY).setSummary(total);
        }

    }

    class UpdateStorageSLocationSummaryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void ret) {
            updateStorageLocationSummary();
        }

    }

    private SharedPreferences.OnSharedPreferenceChangeListener settingsListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    switch (key) {
                        case SettingsManager.SHARED_PREF_STORAGE_DIR:
                            ConcurrentAsyncTask.execute(new UpdateStorageSLocationSummaryTask());
                            break;
                    }
                }
            };


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CameraSyncEvent result) {

        cUploadRepoState.setSummary(Utils.getUploadStateShow(getActivity()));

        Log.d(DEBUG_TAG, "==========" + result.getLogInfo());
        Utils.utilsLogInfo(true, "==========" + result.getLogInfo());
    }

}
