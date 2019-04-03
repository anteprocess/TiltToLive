package backend;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.content.DialogInterface;

import org.andengine.util.call.Callback;

public class StringInputDialogBuilder extends GenericInputDialogBuilder<String> {
    public StringInputDialogBuilder(Context pContext, String pTitleResID, int pMessageResID, int pErrorResID, int pIconResID, Callback<String> pSuccessCallback, DialogInterface.OnCancelListener pOnCancelListener) {
        super(pContext, pTitleResID, pMessageResID, pErrorResID, pIconResID, pSuccessCallback, pOnCancelListener);
    }

    public StringInputDialogBuilder(Context pContext, String pTitleResID, int pMessageResID, int pErrorResID, int pIconResID, String pDefaultText, Callback<String> pSuccessCallback, DialogInterface.OnCancelListener pOnCancelListener) {
        super(pContext, pTitleResID, pMessageResID, pErrorResID, pIconResID, pDefaultText, pSuccessCallback, pOnCancelListener);
    }

    protected String generateResult(String pInput) {
        return pInput;
    }
}
