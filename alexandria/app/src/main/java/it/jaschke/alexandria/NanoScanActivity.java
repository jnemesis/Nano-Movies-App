package it.jaschke.alexandria;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class NanoScanActivity extends Activity implements ZBarScannerView.ResultHandler  {


    private static final String FLASH = "FLASH";
    private static final String FOCUS = "FOCUS";
    private static final String CAMID = "CAMID";
    private static final String EAN = "EAN";
    public static final String EANFORMAT = "EANFORMAT";
    private ZBarScannerView zbarScannerView;
    private boolean bFlash;
    private boolean bFocus;
    private int iCamId = -1;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (state != null)  {
            bFlash = state.getBoolean(FLASH, false);
            bFocus = state.getBoolean(FOCUS, true);
            iCamId = state.getInt(CAMID, -1);
        } else {
            bFlash = false;
            bFocus = true;
            iCamId = -1;
        }

        zbarScannerView = new ZBarScannerView(this);
        setBarcodeFormats();
        setContentView(zbarScannerView);

    }

    public void setBarcodeFormats() {
        List<BarcodeFormat> lFormats = new ArrayList<BarcodeFormat>();
        lFormats.add(BarcodeFormat.EAN13);
        lFormats.add(BarcodeFormat.ISBN10);
        lFormats.add(BarcodeFormat.ISBN13);

        if (zbarScannerView != null) {
            zbarScannerView.setFormats(lFormats);
        }
    }

    @Override
    public void handleResult(Result result) {

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringTone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringTone.play();
        } catch (Exception e) {}

        Intent intentResult = new Intent();
        intentResult.putExtra(EAN, result.getContents());
        intentResult.putExtra(EANFORMAT, result.getBarcodeFormat().getName());
        setResult(RESULT_OK, intentResult);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        zbarScannerView.setResultHandler(this);
        zbarScannerView.startCamera(iCamId);
        zbarScannerView.setFlash(bFlash);
        zbarScannerView.setAutoFocus(bFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(FLASH, bFlash);
        state.putBoolean(FOCUS, bFocus);
        state.putInt(CAMID, iCamId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;

        if (bFlash) {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_off);
        }

        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        if (bFocus) {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, R.string.auto_focus_off);
        }

        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        menuItem = menu.add(Menu.NONE, R.id.menu_camera_selector, 0, R.string.select_camera);
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        switch (item.getItemId()) {
            case R.id.menu_flash:
                bFlash ^= true;
                if (bFlash) {
                    item.setTitle(R.string.flash_on);
                } else {
                    item.setTitle(R.string.flash_off);
                }
                zbarScannerView.setFlash(bFlash);
                return true;
            case R.id.menu_auto_focus:
                bFocus ^= true;
                if (bFocus) {
                    item.setTitle(R.string.auto_focus_on);
                } else {
                    item.setTitle(R.string.auto_focus_off);
                }
                zbarScannerView.setAutoFocus(bFocus);
                return true;
            case R.id.menu_camera_selector:
                zbarScannerView.stopCamera();
                DialogFragment camFragment = CameraSelectorDialogFragment.newInstance(this, iCamId);
                camFragment.show(getSupportFragmentManager(), "camera_selector");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void closeMessageDialog() {
        closeDialog("scan_results");
    }

    public void closeFormatDialog() {
        closeDialog("format_selector");
    }

    public void closeDialog(String sDialog) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment dialogFragment = (DialogFragment) fragmentManager.findFragmentByTag(sDialog);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        zbarScannerView.stopCamera();
        closeMessageDialog();
        closeFormatDialog();
    }

}
