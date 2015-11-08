package it.jaschke.alexandria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import it.jaschke.alexandria.ui.FragmentBase;
import it.jaschke.alexandria.ui.FragmentKeys;
import it.jaschke.alexandria.ui.FragmentOrchestrator;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class BarcodeScannerFragment extends FragmentBase implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initToolbar(R.string.title_barcode, true);

            mScannerView = new ZBarScannerView(getActivity());

            // we define supported book barcode formats
            List<BarcodeFormat> formats = new ArrayList<>();
            formats.add(BarcodeFormat.EAN13);
            formats.add(BarcodeFormat.EAN8);
            formats.add(BarcodeFormat.ISBN10);
            formats.add(BarcodeFormat.ISBN13);
            mScannerView.setFormats(formats);

        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mScannerView != null) {
            // register barcode handler
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        if (rawResult.getContents() != null && rawResult.getContents().length() > 0) {
            mScannerView.stopCamera();
            Toast.makeText(getActivity(), "Contents = " + rawResult.getContents() + ", Format = " + rawResult.getBarcodeFormat().getName(), Toast.LENGTH_LONG).show();

            Bundle args = new Bundle();
            args.putString(AddBookFragment.EAN_KEY, rawResult.getContents());

            // load add book fragment using ISBN barcode value
            ((FragmentOrchestrator) getActivity()).loadFragment(FragmentKeys.ADD, args, FragmentKeys.BOOKS);

        } else {
            mScannerView.startCamera();
            Toast.makeText(getActivity(), getString(R.string.barcode_scan_retry), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopCamera();
            mScannerView.setResultHandler(null);
        }
    }
}
