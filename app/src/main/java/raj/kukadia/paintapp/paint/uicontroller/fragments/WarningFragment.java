package raj.kukadia.paintapp.paint.uicontroller.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import raj.kukadia.paintapp.R;


public class WarningFragment extends DialogFragment {


    public interface WarningDialogListener{
        public void onPositiveListener(DialogFragment dialog);
        public void onNegativeListener(DialogFragment dialog);
    }

    WarningDialogListener listener;

    public WarningFragment() {
        // Required empty public constructor
    }

    public static WarningFragment newInstance() {
        return new WarningFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (WarningDialogListener) context;
    }




    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.warning)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            listener.onNegativeListener(WarningFragment.this);
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            listener.onPositiveListener(WarningFragment.this);
                    }
                });
        Dialog dialog = builder.create();

        return dialog;
    }
}

