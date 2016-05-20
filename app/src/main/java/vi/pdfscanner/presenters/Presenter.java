package vi.pdfscanner.presenters;

/**
 * Created by droidNinja on 20/04/16.
 */
public interface Presenter<T> {
    void attachView(T view);
    void detachView();
}
