package com.nihaocloud.nihao.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.nihaocloud.nihao.R;
import com.nihaocloud.nihao.NihaoApplication;
import com.nihaocloud.nihao.fileschooser.MultiFileChooserActivity;
import com.nihaocloud.nihao.gallery.MultipleImageSelectionActivity;
import com.nihaocloud.nihao.ui.activity.BrowserActivity;
import com.nihaocloud.nihao.util.Utils;

public class UploadChoiceDialog extends DialogFragment {
    private Context ctx = NihaoApplication.getAppContext();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).
                setTitle(getResources().getString(R.string.pick_upload_type)).
                setItems(R.array.pick_upload_array,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                });
        return builder.show();
    }
}
