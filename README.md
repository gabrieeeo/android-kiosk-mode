# Kiosk Mode (Android)

Este aplicativo Android foi desenvolvido para funcionar como um kiosk que mantém um aplicativo rodando constantemente.

## Funcionalidades

- **Modo Kiosk**: Bloqueia o dispositivo para executar apenas aplicativos específicos
- **Monitoramento Automático**: Verifica se o {seu aplicativo} está rodando e o reinicia automaticamente
- **Inicialização Automática**: Inicia automaticamente quando o dispositivo é ligado
- **Interface Fullscreen**: Remove barras de sistema para experiência imersiva
- **Serviço em Segundo Plano**: Mantém o monitoramento ativo mesmo quando o aplicativo não está em foco

## Configuração Inicial

### 1. Instalação do Aplicativo

1. Compile e instale o aplicativo no seu dispositivo
2. Abra o aplicativo pela primeira vez

### 2. Configuração de Permissões

#### Administrador do Dispositivo
1. O aplicativo solicitará permissões de administrador do dispositivo
2. Clique em "Ativar" quando solicitado
3. Isso é necessário para o modo kiosk funcionar

#### Proprietário do Dispositivo (Device Owner)
Para funcionamento completo, o aplicativo deve ser configurado como "Device Owner":

```bash
# Via ADB (Android Debug Bridge)
adb shell dpm set-device-owner me.gabrieeeo.kiosk/.MyDeviceAdminReceiver
```

### 3. Configuração do Totem Solides

**IMPORTANTE**: Você precisa ajustar o nome do pacote do {seu aplicativo} no código:

1. Abra o arquivo `MainActivity.java`
2. Localize a linha: `private static final String TOTEM_SOLIDES_PACKAGE = "package";`
3. Substitua `"package"` pelo nome correto do pacote do {seu aplicativo}

Para descobrir o nome do pacote:
```bash
adb shell pm list packages | grep -i totem
```

## Como Usar

### Ativação do Modo Kiosk

1. Abra o aplicativo
2. Clique no botão "Ativar/Desativar Kiosk"
3. O modo kiosk será ativado e o {seu aplicativo} será iniciado automaticamente

### Monitoramento Automático

- O aplicativo verifica a cada 10 segundos se o {seu aplicativo} está rodando
- Se o aplicativo for fechado, ele será reiniciado automaticamente
- Uma notificação persistente indica que o monitoramento está ativo

### Sair do Modo Kiosk

1. Pressione o botão "Ativar/Desativar Kiosk" novamente
2. O modo kiosk será desativado e o monitoramento será parado

## Configurações Avançadas

### Personalização do Intervalo de Verificação

Para alterar a frequência de verificação do Totem Solides:

1. No arquivo `MainActivity.java`, altere o valor de `CHECK_INTERVAL`
2. No arquivo `KioskService.java`, altere o valor de `CHECK_INTERVAL`

### Configuração de Inicialização Automática

O aplicativo já está configurado para iniciar automaticamente quando o dispositivo é ligado. Se necessário, você pode:

1. Verificar se a permissão `RECEIVE_BOOT_COMPLETED` está presente no `AndroidManifest.xml`
2. O `BootReceiver` já está configurado para iniciar o aplicativo após 10 segundos do boot

## Solução de Problemas

### O {seu aplicativo} não inicia

1. Verifique se o nome do pacote está correto
2. Confirme se o {seu aplicativo} está instalado no dispositivo
3. Verifique as permissões de administrador do dispositivo

### O modo kiosk não funciona

1. Confirme se o aplicativo é o "Device Owner"
2. Verifique se as permissões de administrador estão ativas
3. Reinicie o dispositivo e tente novamente

### O aplicativo não inicia automaticamente

1. Verifique se a permissão `RECEIVE_BOOT_COMPLETED` está concedida
2. Alguns fabricantes podem ter configurações adicionais para inicialização automática
3. Verifique as configurações de otimização de bateria do dispositivo

## Requisitos do Sistema

- Android 6.0 (API 23) ou superior
- Permissões de administrador do dispositivo
- Configuração como "Device Owner" (recomendado)
- {seu aplicativo} instalado no dispositivo

## Estrutura do Projeto

```
app/src/main/java/me/gabrieeeo/kiosk/
├── MainActivity.java          # Interface principal e controle do kiosk
├── KioskService.java          # Serviço de monitoramento em segundo plano
├── BootReceiver.java          # Receptor para inicialização automática
└── MyDeviceAdminReceiver.java # Receptor para permissões de administrador
```

## Licença

Este projeto é fornecido como está, sem garantias. Use por sua conta e risco.

## Suporte

Para suporte técnico ou dúvidas sobre a implementação, consulte a documentação do Android sobre:
- Device Administration
- Lock Task Mode
- Foreground Services
- Boot Receivers

## Por: @gabrieeeo (Gabriel Lima)
