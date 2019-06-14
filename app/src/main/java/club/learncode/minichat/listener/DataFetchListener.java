package club.learncode.minichat.listener;

public interface DataFetchListener<T> {
    void showProgress();
    void onComplete(T t);
    void onFailure(Exception e);
}
