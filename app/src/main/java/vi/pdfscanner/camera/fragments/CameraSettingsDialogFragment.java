package vi.pdfscanner.camera.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import vi.pdfscanner.R;
import vi.pdfscanner.camera.adapters.ObjectToStringAdapter;
import vi.pdfscanner.camera.model.FocusMode;
import vi.pdfscanner.camera.model.HDRMode;
import vi.pdfscanner.camera.model.Quality;
import vi.pdfscanner.camera.model.Ratio;
import vi.pdfscanner.fragment.BaseDialogFragment;
import vi.pdfscanner.interfaces.CameraParamsChangedListener;

import java.util.Arrays;
import java.util.List;

public class CameraSettingsDialogFragment extends BaseDialogFragment {

    public static final String TAG = CameraSettingsDialogFragment.class.getSimpleName();

    private CameraParamsChangedListener paramsChangedListener;

    private Quality quality;
    private Ratio ratio;
    private FocusMode focusMode;
    private HDRMode hdrMode;
    private List<Ratio> ratios = Arrays.asList(Ratio.values());
    private List<Quality> qualities = Arrays.asList(Quality.values());
    private List<FocusMode> focusModes = Arrays.asList(FocusMode.values());

    public static CameraSettingsDialogFragment newInstance(Bundle bundle, CameraParamsChangedListener listener) {
        CameraSettingsDialogFragment fragment = new CameraSettingsDialogFragment();
        fragment.setArguments(bundle);
        fragment.paramsChangedListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expandParams(getArguments());
    }

    private void expandParams(Bundle params) {
        if (params == null) {
            params = new Bundle();
        }
        int id = 0;
        if (params.containsKey(CameraFragment.QUALITY)) {
            id = params.getInt(CameraFragment.QUALITY, 0);
        }
        quality = Quality.getQualityById(id);
        id = 0;
        if (params.containsKey(CameraFragment.RATIO)) {
            id = params.getInt(CameraFragment.RATIO, 0);
        }
        ratio = Ratio.getRatioById(id);
        id = 0;
        if (params.containsKey(CameraFragment.FOCUS_MODE)) {
            id = params.getInt(CameraFragment.FOCUS_MODE);
        }
        focusMode = FocusMode.getFocusModeById(id);
        id = 0;
        if (params.containsKey(CameraFragment.HDR_MODE)) {
            id = params.getInt(CameraFragment.HDR_MODE);
        }
        hdrMode = HDRMode.getHDRModeById(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_camera_params, container, false);

        Spinner ratioSwitcher = (Spinner) view.findViewById(R.id.ratios);
        ratioSwitcher.setAdapter(new ObjectToStringAdapter<>(activity, ratios));
        ratioSwitcher.setSelection(ratios.indexOf(ratio));
        ratioSwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ratio == ratios.get(position)) {
                    return;
                }
                ratio = ratios.get(position);
                onRatioChanged(ratio.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner qualitySwitcher = (Spinner) view.findViewById(R.id.qualities);
        qualitySwitcher.setAdapter(new ObjectToStringAdapter<>(activity, qualities));
        qualitySwitcher.setSelection(qualities.indexOf(quality));
        qualitySwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (quality == qualities.get(position)) {
                    return;
                }
                quality = qualities.get(position);
                onQualityChanged(quality.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner focusSwitcher = (Spinner) view.findViewById(R.id.focus_modes);
        focusSwitcher.setAdapter(new ObjectToStringAdapter<>(activity, focusModes));
        focusSwitcher.setSelection(focusModes.indexOf(focusMode));
        focusSwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (focusMode == focusModes.get(position)) {
                    return;
                }
                focusMode = focusModes.get(position);
                onFocusModeChanged(focusMode.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (hdrMode == HDRMode.NONE) {
            view.findViewById(R.id.relativeHdr).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.relativeHdr).setVisibility(View.VISIBLE);
            Switch hdrSwitch = (Switch) view.findViewById(R.id.switchHDR);
            hdrSwitch.setChecked(hdrMode == HDRMode.ON);
            hdrSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onHDRChanged(isChecked ? HDRMode.ON.getId() : HDRMode.OFF.getId());
                }
            });
        }

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }

        });

        return view;
    }

    public void onQualityChanged(int id) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onQualityChanged(id);
        }
    }

    public void onHDRChanged(int id) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onHDRChanged(id);
        }
    }

    public void onRatioChanged(int id) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onRatioChanged(id);
        }
    }

    public void onFocusModeChanged(int id) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onFocusModeChanged(id);
        }
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

}
