package cn.sealiu.health;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 该service用户向设备请求历史数据，
 * 上传本地数据至服务器
 */
public class SyncService extends Service {
    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
