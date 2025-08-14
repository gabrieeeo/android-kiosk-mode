# Configuração de Uso para Aplicativo Específico

## Passo 1: Descobrir o Nome do Pacote do Aplicativo

Antes de compilar o aplicativo, você precisa descobrir o nome exato do pacote do aplicativo que você quer ativar o modo kiosk instalado no seu dispositivo.

### Método 1: Via ADB
```bash
adb shell pm list packages | grep -i {seu aplicativo}
```

### Método 2: Via Configurações do Android
1. Vá em Configurações > Aplicativos > {seu aplicativo}
2. Role para baixo e procure por "Nome do pacote"

## Passo 2: Atualizar o Código

Após descobrir o nome do pacote, você precisa atualizar em **3 arquivos**:

### 1. MainActivity.java
```java
private static final String TOTEM_SOLIDES_PACKAGE = "package"; // SUBSTITUA PELO NOME CORRETO
```

### 2. KioskService.java
```java
private static final String TOTEM_SOLIDES_PACKAGE = "package"; // SUBSTITUA PELO NOME CORRETO
```

## Passo 3: Compilar e Instalar

### Compilar o Projeto
```bash
./gradlew assembleDebug
```

### Instalar no dispositivo
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Passo 4: Configurar Device Owner

```bash
adb shell dpm set-device-owner me.gabrieeeo.kiosk/.MyDeviceAdminReceiver
```

## Passo 5: Configurar o Tablet

### Configurações de Desenvolvedor
1. Vá em Configurações > Sobre o telefone
2. Toque 7 vezes em "Número da versão"
3. Volte e vá em "Opções do desenvolvedor"
4. Ative "Depuração USB"

### Configurações de Otimização de Bateria
1. Vá em Configurações > Aplicativos > Kiosk Totem Solides
2. Vá em "Bateria"
3. Selecione "Não otimizar"

### Configurações de Inicialização Automática
Alguns fabricantes (Samsung, Xiaomi, etc.) têm configurações específicas:

#### Samsung
1. Configurações > Aplicativos > {seu aplicativo}
2. Bateria > Otimização de bateria > Não otimizar
3. Configurações > Aplicativos > Configurações especiais > Inicialização automática

#### Xiaomi
1. Configurações > Aplicativos > Gerenciar aplicativos > {seu aplicativo}
2. Permissões > Inicialização automática > Permitir
3. Configurações > Bateria e desempenho > Otimização de bateria > Aplicativos > {seu aplicativo} > Não otimizar

## Passo 6: Testar o Funcionamento

1. Abra o aplicativo "Kiosk Mode"
2. Clique em "Ativar/Desativar Kiosk"
3. O {seu aplicativo} deve abrir automaticamente
4. Feche o {seu aplicativo} manualmente
5. Aguarde alguns segundos - ele deve reabrir automaticamente

## Solução de Problemas Comuns

### O {seu aplicativo} não abre
- Verifique se o nome do pacote está correto
- Confirme se o {seu aplicativo} está instalado
- Verifique se as permissões de administrador estão ativas

### O modo kiosk não funciona
- Confirme se o aplicativo é o "Device Owner"
- Reinicie o tablet e tente novamente
- Verifique se não há outros aplicativos de administração ativos

### O aplicativo não inicia automaticamente
- Verifique as configurações de otimização de bateria
- Alguns fabricantes bloqueiam inicialização automática
- Configure manualmente nas configurações do fabricante

## Comandos Úteis para Debug

### Verificar Device Owner
```bash
adb shell dumpsys device_policy | grep "Device Owner"
```

### Verificar Aplicativos em Lock Task
```bash
adb shell dumpsys device_policy | grep "Lock Task"
```

### Verificar se o Totem Solides está rodando
```bash
adb shell ps | grep totem
```

### Forçar parada do aplicativo
```bash
adb shell am force-stop me.gabrieeeo.kiosk
```

### Limpar dados do aplicativo
```bash
adb shell pm clear me.gabrieeeo.kiosk
```

## Configurações Avançadas

### Alterar Intervalo de Verificação
Para verificar mais frequentemente (a cada 5 segundos):
```java
private static final int CHECK_INTERVAL = 5000; // 5 segundos
```

Para verificar menos frequentemente (a cada 30 segundos):
```java
private static final int CHECK_INTERVAL = 30000; // 30 segundos
```