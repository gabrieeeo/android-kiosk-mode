package me.gabrieeeo.kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Aguardar um pouco para garantir que o sistema esteja pronto
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(context, "Kiosk Mode iniciado automaticamente", Toast.LENGTH_SHORT).show();
                }
            }, 60000); // 10 segundos de delay
        }
    }
}
