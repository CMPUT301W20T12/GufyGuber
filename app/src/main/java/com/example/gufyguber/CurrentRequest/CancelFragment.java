
        package com.example.gufyguber.CurrentRequest;

        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.Context;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.widget.Button;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.fragment.app.DialogFragment;

        import com.example.gufyguber.Utility.GlobalDoubleClickHandler;
        import com.example.gufyguber.R;

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
                if (GlobalDoubleClickHandler.isDoubleClick()){
                    return;
                }

                getFragmentManager().beginTransaction().remove(CancelFragment.this).commit();
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


        return builder
                .setView(view)
                .create();

    }
}
