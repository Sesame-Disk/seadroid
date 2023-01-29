package com.nihaocloud.sesamedisk.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.NihaoApplication;
import com.nihaocloud.sesamedisk.fileschooser.MultiFileChooserActivity;
import com.nihaocloud.sesamedisk.gallery.MultipleImageSelectionActivity;
import com.nihaocloud.sesamedisk.ui.activity.BrowserActivity;
import com.nihaocloud.sesamedisk.util.Utils;

public class UploadChoiceDialog extends DialogFragment {
    private Context ctx = NihaoApplication.getAppContext();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).
                setTitle(getResources().getString(R.string.pick_upload_type)).
                setItems(R.array.pick_upload_array,
                        (dialog, which) -> {
                            switch (which) {
                            case 0:
                                Intent intent = new Intent(ctx, MultiFileChooserActivity.class);
                                getActivity().startActivityForResult(intent, BrowserActivity.PICK_FILES_REQUEST);
                                break;
                            case 1:
                                // photos
                                intent = new Intent(ctx, MultipleImageSelectionActivity.class);
                                getActivity().startActivityForResult(intent, BrowserActivity.PICK_PHOTOS_VIDEOS_REQUEST);
                                break;
                            case 2:
                                // thirdparty file chooser
                                Intent target = Utils.createGetContentIntent();
                                intent = Intent.createChooser(target, getString(R.string.choose_file));
                                getActivity().startActivityForResult(intent, BrowserActivity.PICK_FILE_REQUEST);
                                break;
                            default:
                                return;
                            }
                        });
        return builder.show();
    }
}
