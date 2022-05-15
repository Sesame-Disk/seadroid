package com.nihaocloud.sesamedisk.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.SeafException;
import com.nihaocloud.sesamedisk.account.Account;
import com.nihaocloud.sesamedisk.data.CreateRepo;
import com.nihaocloud.sesamedisk.data.DataManager;
import com.nihaocloud.sesamedisk.data.UploadFolder;

class NewUploadFolderRepoTask extends TaskDialog.Task {
    private final DocumentFile documentFile;
    private final String mPassword;
    private final DataManager mDataManager;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final UploadFolderRepoDialog.UploadDialogListener listener;

    public NewUploadFolderRepoTask(Context context, DocumentFile documentFile,
                                   String password, DataManager dataManager,
                                   UploadFolderRepoDialog.UploadDialogListener listener) {
        this.context = context.getApplicationContext();
        this.documentFile = documentFile;
        mPassword = password;
        mDataManager = dataManager;
        this.listener = listener;
    }

    @Override
    protected void runTask() {
        try {
            final DocumentFile[] files = documentFile.listFiles();
            if (files == null || files.length == 0) {
                throw new SeafException(1011, context.getString(R.string.empty_folder));
            }
            final UploadFolder[] uploadCashFiles = UploadFolder.getUploadCashFiles(context, files);
            if (uploadCashFiles.length == 0) {
                throw new SeafException(1011, context.getString(R.string.empty_folder));
            }
            final CreateRepo repo = mDataManager.createNewRepoWithResponse(documentFile.getName(), mPassword);
            if (listener != null) {
                listener.onUploadFolder(repo, uploadCashFiles);
            }
        } catch (SeafException e) {
            setTaskException(e);
        }
    }
}

public class UploadFolderRepoDialog extends TaskDialog {

    public static abstract class UploadDialogListener {
        public void onUploadFolder(CreateRepo repo, UploadFolder[] uploadCashFiles) {
        }
    }

    private final static String STATE_ACCOUNT = "new_upload_folder_repo_dialog.account";
    private final static String STATE_URI = "new_upload_folder_repo_dialog.uri";
    // The input fields of the dialog
    private TextView mRepoNameText;
    private SwitchCompat mEncryptSwitch;
    //  Use plain text field to avoid having to compare two obfuscated fields
    private EditText mPasswordText;
    private EditText mPasswordConfirmationText;
    private NestedScrollView mNestedScrollView;
    private Account mAccount;
    private Uri folderUri;
    private DataManager mDataManager;
    private DocumentFile documentsTree;
    private UploadFolderRepoDialog.UploadDialogListener listener;

    public static UploadFolderRepoDialog newInstance(final Account mAccount, final Uri folderUri) {
        Bundle args = new Bundle();
        args.putParcelable(STATE_ACCOUNT, mAccount);
        args.putParcelable(STATE_URI, folderUri);
        UploadFolderRepoDialog fragment = new UploadFolderRepoDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private DataManager getDataManager() {
        if (mDataManager == null) mDataManager = new DataManager(mAccount);
        return mDataManager;
    }

    public String getRepoName() {
        return mRepoNameText.getText().toString().trim();
    }

    private String getPassword() {
        return mPasswordText.getText().toString().trim();
    }

    private String getPasswordConfirmation() {
        return mPasswordConfirmationText.getText().toString().trim();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccount = getArguments().getParcelable(STATE_ACCOUNT);
        folderUri = getArguments().getParcelable(STATE_URI);
        documentsTree = DocumentFile.fromTreeUri(requireContext(), folderUri);
    }

    @Override
    protected View createDialogContentView(LayoutInflater inflater, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_upload_folder_repo, null);
        mRepoNameText = (TextView) view.findViewById(R.id.new_repo_name);
        mRepoNameText.setText(documentsTree.getName());
        mEncryptSwitch = (SwitchCompat) view.findViewById(R.id.new_repo_encrypt_switch);
        mPasswordText = (EditText) view.findViewById(R.id.new_repo_password);
        mPasswordText.setHint(String.format(
                getResources().getString(R.string.passwd_min_len_limit_hint),
                getResources().getInteger(R.integer.minimum_password_length)
        ));
        mPasswordConfirmationText = (EditText) view.findViewById(R.id.new_repo_password_confirmation);
        mNestedScrollView = (NestedScrollView) view.findViewById(R.id.nsv_new_repo_container);
        mEncryptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mNestedScrollView.setVisibility(View.VISIBLE);
            } else {
                mNestedScrollView.setVisibility(View.GONE);
                // Delete entered passwords so hiding the input fields creates an unencrypted repo
                mPasswordText.setText("");
                mPasswordConfirmationText.setText("");
            }
        });
        return view;
    }

    @Override
    protected void onDialogCreated(Dialog dialog) {
        dialog.setTitle(R.string.create_new_repo);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onValidateUserInput() throws Exception {
        if (getRepoName().length() == 0) {
            throw new Exception(getResources().getString(R.string.repo_name_empty));
        }

        if (mEncryptSwitch.isChecked()) {
            if (getPassword().length() == 0) {
                throw new Exception(getResources().getString(R.string.err_passwd_empty));
            }

            if (getPassword().length() < getResources().getInteger(R.integer.minimum_password_length)) {
                throw new Exception(getResources().getString(R.string.err_passwd_too_short));
            }

            if (!getPassword().equals(getPasswordConfirmation())) {
                throw new Exception(getResources().getString(R.string.err_passwd_mismatch));
            }
        }
    }

    @Override
    protected NewUploadFolderRepoTask prepareTask() {
        return new NewUploadFolderRepoTask(requireContext().getApplicationContext(),
                documentsTree, getPassword(),
                getDataManager(), listener);
    }

    @Override
    protected void disableInput() {
        super.disableInput();
        mRepoNameText.setEnabled(false);
        mEncryptSwitch.setEnabled(false);
        mPasswordText.setEnabled(false);
        mPasswordConfirmationText.setEnabled(false);
    }

    @Override
    protected void enableInput() {
        super.enableInput();
        mRepoNameText.setEnabled(true);
        mEncryptSwitch.setEnabled(true);
        mPasswordText.setEnabled(true);
        mPasswordConfirmationText.setEnabled(true);
    }

    public void setListener(UploadDialogListener listener) {
        this.listener = listener;
    }
}
