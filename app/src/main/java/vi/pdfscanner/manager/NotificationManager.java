package vi.pdfscanner.manager;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observer;

/**
 * Created by droidNinja on 28/03/16.
 */
public class NotificationManager {

    private HashMap<String, ArrayList<Observer>> notificationHashMap;

    private static NotificationManager ourInstance = new NotificationManager();

    public static NotificationManager getInstance() {
        return ourInstance;
    }

    private NotificationManager() {
        notificationHashMap = new HashMap<>();
    }

    public void registerNotification(String notificationName, Observer observer)
    {
        if(notificationHashMap.containsKey(notificationName))
        {
            ArrayList<Observer> observers = notificationHashMap.get(notificationName);
            if(!observers.contains(observer))
                observers.add(observer);
        }
        else
        {
            ArrayList<Observer> observers = new ArrayList<>();
            observers.add(observer);
            notificationHashMap.put(notificationName,observers);
        }
    }

    public void deRegisterNotification(String notificationName, Observer observer)
    {
        if(notificationHashMap.containsKey(notificationName))
        {
            ArrayList<Observer> observers = notificationHashMap.get(notificationName);
            if(observers.contains(observer))
                observers.remove(observer);
        }
    }

    public void raiseNotification(Context context, String notificationName, Object request, Object response)
    {
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.context = context;
        notificationModel.notificationName = notificationName;
        notificationModel.request = request;
        notificationModel.response = response;

        if(notificationHashMap.containsKey(notificationName))
        {
            ArrayList<Observer> observers = notificationHashMap.get(notificationName);
            for (Observer observer:observers) {
                observer.update(null, notificationModel);
            }
        }
    }
}