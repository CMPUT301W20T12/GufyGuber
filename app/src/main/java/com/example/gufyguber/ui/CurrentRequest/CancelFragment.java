
        package com.example.gufyguber.ui.CurrentRequest;

        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Build;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.widget.Button;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.fragment.app.DialogFragment;
        import androidx.fragment.app.Fragment;

        import com.example.gufyguber.LoginActivity;
        import com.example.gufyguber.NavigationActivity;
        import com.example.gufyguber.R;
        import com.example.gufyguber.RideRequest;
        import com.example.gufyguber.ui.CurrentRequest.CancelRequestFragment;

        /**
 * This class creates a fragment that displays a message confirming the cancellation of a ride request.
 */

public class CancelFragment extends DialogFragment {

    private Button okBtn;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.cancelled_rider, null);

        okBtn = view.findViewById(R.id.cancelled_rider_button);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().remove(CancelFragment.this).commit();
                getActivity().onBackPressed();
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


        return builder
                .setView(view)
                .create();

    }
}