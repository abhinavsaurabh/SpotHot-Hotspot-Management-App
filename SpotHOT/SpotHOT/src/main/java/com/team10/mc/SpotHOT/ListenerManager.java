package com.team10.mc.SpotHOT;

import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.activity.helpers.AbstractRegisterHelper;

import com.team10.mc.SpotHOT.activity.helpers.RegisterDataLimitListenerHelper;
import com.team10.mc.SpotHOT.activity.helpers.RegisterGeneralListenerHelper;
import com.team10.mc.SpotHOT.activity.helpers.RegisterIdleListenerHelper;
import com.team10.mc.SpotHOT.activity.helpers.RegisterSchedulerListenerHelper;
import com.team10.mc.SpotHOT.activity.helpers.RegisterWiFiListListenerHelper;

import java.util.HashMap;
import java.util.Map;


public class ListenerManager {

    private Map<Class, AbstractRegisterHelper> map;
    private boolean created;
    private final MainActivity activity;

    public ListenerManager(MainActivity activity) {
        this.activity = activity;
    }

    private void create() {
        map = new HashMap<>();
        map.put(RegisterSchedulerListenerHelper.class, new RegisterSchedulerListenerHelper(activity));
        map.put(RegisterGeneralListenerHelper.class, new RegisterGeneralListenerHelper(activity));
        map.put(RegisterDataLimitListenerHelper.class, new RegisterDataLimitListenerHelper(activity));
        map.put(RegisterIdleListenerHelper.class, new RegisterIdleListenerHelper(activity));
        map.put(RegisterWiFiListListenerHelper.class, new RegisterWiFiListListenerHelper(activity));
        created = true;
    }

    public void registerAll() {
        if (!created) {
            create();
        }
        for (AbstractRegisterHelper helper : map.values()) {
            helper.registerUIListeners();
        }
    }

    public void unregisterAll() {
        for (AbstractRegisterHelper helper : map.values()) {
            helper.unregisterUIListeners();
        }
    }

    //TODO rewrite to use generic types
    public AbstractRegisterHelper getHelper(Class clazz) {
        return map.get(clazz);
    }

}
