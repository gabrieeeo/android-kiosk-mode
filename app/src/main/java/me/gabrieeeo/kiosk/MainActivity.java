package me.gabrieeeo.kiosk;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button mButtonToggleKiosk;
    private Button mButtonLaunchTotem;
    private TextView mStatusText;

    private View mDecorView;
    private DevicePolicyManager mDpm;
    private boolean mIsKioskEnabled = false;
    private Handler mHandler;
    private static final String TOTEM_SOLIDES_PACKAGE = "com.tangerino.touchless";
    private static final int CHECK_INTERVAL = 5000; // 5 segundos
    private Runnable mKioskMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar views
        mButtonToggleKiosk = findViewById(R.id.button_toggle_kiosk);
        mButtonLaunchTotem = findViewById(R.id.button_launch_totem);
        mStatusText = findViewById(R.id.status_text);
        
        // Configurar listeners
        mButtonToggleKiosk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordDialogAndToggle();
            }
        });
        
        mButtonLaunchTotem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchTotemSolidesApp();
            }
        });

        mHandler = new Handler(Looper.getMainLooper());
        
        ComponentName deviceAdmin = new ComponentName(this, MyDeviceAdminReceiver.class);
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        
        if (!mDpm.isAdminActive(deviceAdmin)) {
            Toast.makeText(this, getString(R.string.not_device_admin), Toast.LENGTH_SHORT).show();
        }

        if (mDpm.isDeviceOwnerApp(getPackageName())) {
            mDpm.setLockTaskPackages(deviceAdmin, new String[]{getPackageName(), TOTEM_SOLIDES_PACKAGE});
        } else {
            Toast.makeText(this, getString(R.string.not_device_owner), Toast.LENGTH_SHORT).show();
        }

        mDecorView = getWindow().getDecorView();
        
        // Iniciar monitoramento automático
        startKioskMonitoring();
        
        // Atualizar status inicial
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        updateStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopKioskMonitoring();
    }

    // Esconde as barras do sistema
    private void hideSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showPasswordDialogAndToggle() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
        .setTitle(getString(R.string.password_prompt_title))
        .setMessage(getString(R.string.password_prompt_message))
        .setView(input)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String entered = input.getText().toString();
            if ("1234".equals(entered)) {
                enableKioskModeSimple(!mIsKioskEnabled);
            }else {
                Toast.makeText(MainActivity.this, getString(R.string.password_incorrect), Toast.LENGTH_SHORT).show();
            }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
    }

    private void enableKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (mDpm.isLockTaskPermitted(this.getPackageName())) {
                    // Primeiro ativar o lock task
                    try {
                        startLockTask();
                        mIsKioskEnabled = true;
                        Toast.makeText(this, "Modo Kiosk Ativado", Toast.LENGTH_SHORT).show();
                        
                        // Depois iniciar o serviço de monitoramento (com delay)
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent serviceIntent = new Intent(MainActivity.this, KioskService.class);
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        startForegroundService(serviceIntent);
                                    } else {
                                        startService(serviceIntent);
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Erro ao iniciar serviço: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 1000); // 1 segundo de delay
                        
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao ativar Lock Task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, "Permissão de Lock Task não concedida", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    stopLockTask();
                } catch (Exception e) {
                    // Ignorar erro ao parar lock task
                }
                
                mIsKioskEnabled = false;
                
                // Parar o serviço de monitoramento
                try {
                    Intent serviceIntent = new Intent(this, KioskService.class);
                    stopService(serviceIntent);
                } catch (Exception e) {
                    // Ignorar erro ao parar serviço
                }
                
                Toast.makeText(this, "Modo Kiosk Desativado", Toast.LENGTH_SHORT).show();
            }
            updateStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao alterar modo kiosk: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void launchTotemSolidesApp() {
        try {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(TOTEM_SOLIDES_PACKAGE);
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                Toast.makeText(this, "Totem Solides iniciado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Totem Solides não encontrado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao iniciar Totem Solides: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startKioskMonitoring() {
        mKioskMonitor = new Runnable() {
            @Override
            public void run() {
                if (mIsKioskEnabled) {
                    // Verificar se o Totem Solides está rodando
                    if (!isAppRunning(TOTEM_SOLIDES_PACKAGE)) {
                        launchTotemSolidesApp();
                    }
                }
                mHandler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        mHandler.post(mKioskMonitor);
    }

    private void stopKioskMonitoring() {
        if (mKioskMonitor != null) {
            mHandler.removeCallbacks(mKioskMonitor);
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

    private void enableKioskModeSimple(boolean enabled) {
        try {
            if (enabled) {
                if (mDpm.isLockTaskPermitted(this.getPackageName())) {
                    startLockTask();
                    mIsKioskEnabled = true;
                    Toast.makeText(this, "Modo Kiosk Ativado (Simples)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permissão de Lock Task não concedida", Toast.LENGTH_SHORT).show();
                }
            } else {
                stopLockTask();
                mIsKioskEnabled = false;
                Toast.makeText(this, "Modo Kiosk Desativado", Toast.LENGTH_SHORT).show();
            }
            updateStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus() {
        String status = "Status: ";
        status += mIsKioskEnabled ? "Kiosk ATIVO" : "Kiosk INATIVO";
        status += "\nTotem Solides: ";
        status += isAppRunning(TOTEM_SOLIDES_PACKAGE) ? "RODANDO" : "PARADO";
        status += "\nAdmin: ";
        status += mDpm.isAdminActive(new ComponentName(this, MyDeviceAdminReceiver.class)) ? "ATIVO" : "INATIVO";
        
        if (mStatusText != null) {
            mStatusText.setText(status);
        }
    }
}