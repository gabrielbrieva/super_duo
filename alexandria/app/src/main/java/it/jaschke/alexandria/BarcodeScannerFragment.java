package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class BarcodeScannerFragment extends Fragment implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        // register barcode handler
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        if (rawResult.getContents() != null && rawResult.getContents().length() > 0) {
            mScannerView.stopCamera();
            // TODO go to add book fragment
            Toast.makeText(getActivity(), "Contents = " + rawResult.getContents() + ", Format = " + rawResult.getBarcodeFormat().getName(), Toast.LENGTH_LONG).show();
        } else {
            mScannerView.startCamera();
            Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        mScannerView.setResultHandler(null);
    }

}
