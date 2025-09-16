package me.gabrieeeo.kiosk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class KioskService extends Service {

    private static final String CHANNEL_ID = "KioskServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TOTEM_SOLIDES_PACKAGE = "com.tangerino.touchless";
    private static final int CHECK_INTERVAL = 2000; // 2 segundos

    private Handler mHandler;
    private Runnable mMonitorRunnable;
    private boolean mIsMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startForeground(NOTIFICATION_ID, createNotification());
            startMonitoring();
        } catch (Exception e) {
            // Se falhar, tentar parar o serviço
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Kiosk Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Canal para o serviço de kiosk");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Kiosk Mode")
                    .setContentText("Monitorando aplicativo Totem Solides")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true);

            return builder.build();
        } catch (Exception e) {
            // Criar notificação simples em caso de erro
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Kiosk")
                    .setContentText("Ativo")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build();
        }
    }

    public void startMonitoring() {
        if (mIsMonitoring) {
            return;
        }

        mIsMonitoring = true;
        mMonitorRunnable = new Runnable() {
            @Override
            public void run() {
                if (mIsMonitoring) {
                    checkAndLaunchTotemSolides();
                    mHandler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        };
        mHandler.post(mMonitorRunnable);
    }

    private void stopMonitoring() {
        mIsMonitoring = false;
        if (mMonitorRunnable != null) {
            mHandler.removeCallbacks(mMonitorRunnable);
        }
    }

    private void checkAndLaunchTotemSolides() {
        if (!isAppRunning(TOTEM_SOLIDES_PACKAGE)) {
            launchTotemSolides();
        }
    }

    private boolean isAppRunning(String packageName) {
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            java.util.List<android.app.ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            
            if (processes != null) {
                for (android.app.ActivityManager.RunningAppProcessInfo processInfo : processes) {
                    if (processInfo.processName.equals(packageName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Log do erro se necessário
        }
        return false;
    }

    private void launchTotemSolides() {
        try {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(TOTEM_SOLIDES_PACKAGE);

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);

                // Mostrar toast apenas se o serviço estiver rodando há mais de 5 segundos
                Handler toastHandler = new Handler(Looper.getMainLooper());
                toastHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(KioskService.this, "Totem Solides reiniciado automaticamente", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        } catch (Exception e) {
            // Log do erro
        }
    }
}
