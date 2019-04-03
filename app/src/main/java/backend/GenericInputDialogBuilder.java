package backend;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import org.andengine.util.call.Callback;
import org.andengine.util.debug.Debug;


public abstract class GenericInputDialogBuilder<T> {
    protected final Callback<Object> mSuccessCallback;
    protected final DialogInterface.OnCancelListener mOnCancelListener;
    protected final String mTitleResID;
    protected final int mMessageResID;
    protected final int mIconResID;
    protected final Context mContext;
    private final int mErrorResID;
    private final String mDefaultText;

    public GenericInputDialogBuilder(Context pContext, String pTitleResID, int pMessageResID, int pErrorResID, int pIconResID, Callback<T> pSuccessCallback, DialogInterface.OnCancelListener pOnCancelListener) {
        this(pContext, pTitleResID, pMessageResID, pErrorResID, pIconResID, "", pSuccessCallback, pOnCancelListener);
    }

    public GenericInputDialogBuilder(Context pContext, String pTitleResID, int pMessageResID, int pErrorResID, int pIconResID, String pDefaultText, Callback<T> pSuccessCallback, DialogInterface.OnCancelListener pOnCancelListener) {
        this.mContext = pContext;
        this.mTitleResID = pTitleResID;
        this.mMessageResID = pMessageResID;
        this.mErrorResID = pErrorResID;
        this.mIconResID = pIconResID;
        this.mDefaultText = pDefaultText;
        this.mSuccessCallback = (Callback<Object>) pSuccessCallback;
        this.mOnCancelListener = pOnCancelListener;
    }

    protected abstract T generateResult(String var1);

    public Dialog create() {
        final EditText etInput = new EditText(this.mContext);
        etInput.setText(this.mDefaultText);
        AlertDialog.Builder ab = new AlertDialog.Builder(this.mContext);
        if(this.mTitleResID != "") {
            ab.setTitle(this.mTitleResID);
        }

        if(this.mMessageResID != 0) {
            ab.setMessage(this.mMessageResID);
        }

        if(this.mIconResID != 0) {
            ab.setIcon(this.mIconResID);
        }

        this.setView(ab, etInput);
        ab.setOnCancelListener(this.mOnCancelListener).setPositiveButton("Share", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface pDialog, int pWhich) {
                Object result;
                try {
                    result = backend.GenericInputDialogBuilder.this.generateResult(etInput.getText().toString());
                } catch (IllegalArgumentException var5) {
                    Debug.e("Error in GenericInputDialogBuilder.generateResult()", var5);
                    Toast.makeText(backend.GenericInputDialogBuilder.this.mContext,backend.GenericInputDialogBuilder.this.mErrorResID, 0).show();
                    return;
                }

                backend.GenericInputDialogBuilder.this.mSuccessCallback.onCallback(result);
                pDialog.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface pDialog, int pWhich) {
                backend.GenericInputDialogBuilder.this.mOnCancelListener.onCancel(pDialog);
                pDialog.dismiss();
            }
        });
        return ab.create();
    }

    protected void setView(AlertDialog.Builder pBuilder, EditText pInputEditText) {
        pBuilder.setView(pInputEditText);
    }
}
