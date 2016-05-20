
package vi.pdfscanner.interfaces;

public interface CameraParamsChangedListener {

    public void onQualityChanged(int id);

    public void onRatioChanged(int id);

    public void onFlashModeChanged(int id);

    public void onHDRChanged(int id);

    public void onFocusModeChanged(int id);

    public void onCaptureModeChanged(int mode);
}
