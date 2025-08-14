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
                    // Iniciar a MainActivity
                    Intent mainActivityIntent = new Intent(context, MainActivity.class);
                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mainActivityIntent);
                    
                    Toast.makeText(context, "Kiosk Mode iniciado automaticamente", Toast.LENGTH_SHORT).show();
                }
            }, 10000); // 10 segundos de delay
        }
    }
}
