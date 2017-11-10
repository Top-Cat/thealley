#include "main.h"

struct espconn dCon;
struct espconn *nCon;

char sendBuffer[4] = {0x00, 0xFF, 0xFF, 0xFF};

ip_addr_t controller_ip;
esp_tcp controller_tcp;

static volatile os_timer_t some_timer;

int i;
// 0:onMask, 1:onSpec, 2:pwmMask, 3:pwmSpec, 4:flashMask, 5:flashSpec, 6:flashPwmMask, 7:flashPwmSpec
int masks[8] = {0};

int pressed = 0;
int debounce = 0;
int sent = 0;

int ICACHE_FLASH_ATTR shiftToPins(int data) {
    return ((data & 0x01) << 13) |
           ((data & 0x02) << 11) |
           (data & 0x08)         |
           ((data & 0x10) << 1)  |
           ((data & 0x20) >> 1);
}

void ICACHE_FLASH_ATTR data_received(void *arg, char *pdata, unsigned short len) {
    if (pdata[0] == 0x00 && len > 3) {
        for (i = 0; i < 3; i++) {
            masks[i*2] = shiftToPins(pdata[i+1]);
            masks[i*2+1] = (pdata[i+1] & 0x04) > 0;
        }

        // Only apply flash to pins in an on state
        // (Means we can use one byte to send instead of two)
        masks[6] = masks[4] & masks[2]; // flashPwmMask = flashMask & pwmMask;
        masks[7] = masks[5] & masks[3]; // flashPwmSpec = flashSpec & pwmSpec;

        masks[4] = masks[4] & masks[0]; // flashMask = flashMask & onMask;
        masks[5] = masks[5] & masks[1]; // flashSpec = flashSpec & onSpec;

        setOnMask(masks[0]);
        setSpec(masks[1]);
    }
}

void ICACHE_FLASH_ATTR sent_cb(void *arg) {
    nCon = arg;
    sent = 1;
}

void ICACHE_FLASH_ATTR tcp_connected(void *arg) {
    struct espconn *conn = arg;

    espconn_regist_recvcb(conn, data_received);
    espconn_regist_sentcb(conn, sent_cb);

    char mac[6];
    wifi_get_macaddr(STATION_IF, mac);

    sendBuffer[0] = 0;
    os_memcpy(sendBuffer + 1, mac + 3, 3);
    sent = 0;
    espconn_sent(conn, sendBuffer, 4);
}

void ICACHE_FLASH_ATTR tcp_disconnected(void *arg) {
    tcp_reconnect();
}

void ICACHE_FLASH_ATTR dns_done(const char *name, ip_addr_t *ipaddr, void *arg) {
    // TODO: Set both leds blue
    //oneOn(4);

    struct espconn *conn = arg;

    if (ipaddr == NULL) {
        tcp_reconnect();
    } else {
        conn->type = ESPCONN_TCP;
        conn->state = ESPCONN_NONE;
        conn->proto.tcp=&controller_tcp;
        conn->proto.tcp->local_port = espconn_port();
        conn->proto.tcp->remote_port = 5558;
        os_memcpy(conn->proto.tcp->remote_ip, &ipaddr->addr, 4);

        espconn_regist_connectcb( conn, tcp_connected );
        espconn_regist_disconcb( conn, tcp_disconnected );

        espconn_connect( conn );
    }
}

void ICACHE_FLASH_ATTR tcp_reconnect() {
    espconn_gethostbyname(&dCon, "lights.kirkstall.top-cat.me", &controller_ip, dns_done);
}

void ICACHE_FLASH_ATTR wifi_callback(System_Event_t *evt) {
    switch (evt->event) {
        case EVENT_STAMODE_CONNECTED:
            break;
        case EVENT_STAMODE_DISCONNECTED:
            // TODO: Reconnect? Automatic?
            //oneOn(3);
            break;
        case EVENT_STAMODE_GOT_IP:
            // TODO: Set led yellow
            // Flash yellow
            masks[0] = 0;
            masks[2] = 0;
            masks[4] = 4128;
            masks[6] = 8200;

            tcp_reconnect();
            break;
    }
}

void ICACHE_FLASH_ATTR timerloop(void *arg) {
    // 50/50 PWM
    if (debounce % 2 == 0) {
        gpio_output_set(0, masks[2], masks[2], 0);
        if (masks[3]) { setSpec(1); }
    } else {
        gpio_output_set(0, 0, 0, masks[2]);
        if (masks[3]) { setSpec(0); }
    }

    // Check button every 10ms
    if (debounce % 10 == 0) {
        int state = gpio_input_get();
        int newPressed = ((state >> 14) & 0x01) | (state & 0x02);

        if (newPressed != pressed && sent == 1) {
            // Send update!

            sendBuffer[0] = 1;
            sendBuffer[1] = newPressed;

            int result = espconn_sent(nCon, sendBuffer, 2);
            if (result == ESPCONN_OK) {
                pressed = newPressed;
                sent = 0;
            }
        }
    }

    if (++debounce >= 1000) {
        debounce = 0;

        // pwmMask ^= flashPwmMask; pwmSpec ^= flashPwmSpec;
        // onMask ^= flashMask; onSpec ^= flashSpec;

        int i;
        for (i = 0; i < 4; i++) {
            masks[i] ^= masks[i+4];
        }

        setOnMask(masks[0]);
        setSpec(masks[1]);
    }
}

struct station_config stationConf = { 0 };

static const char* WPA2username = "lights";
static const char* WPA2password = WPA2_PASSWORD ;

char ssid[32] = "488-X";
char password[32] = "";

void ICACHE_FLASH_ATTR user_init() {
    wifi_station_set_hostname("lightswitchv2");
    wifi_set_opmode( STATION_MODE );

    // TODO: Only once
    os_memcpy(&stationConf.ssid, ssid, 32);
    os_memcpy(&stationConf.password, password, 32);
    wifi_station_set_config(&stationConf);

    wifi_station_set_wpa2_enterprise_auth(1);
    //wifi_station_set_enterprise_identity((uint8*)WPA2username, strlen(WPA2username));
    wifi_station_set_enterprise_username((uint8*)WPA2username, strlen(WPA2username));
    wifi_station_set_enterprise_password((uint8*)WPA2password, strlen(WPA2password));
    wifi_station_clear_enterprise_ca_cert();

    wifi_set_event_handler_cb(wifi_callback);

    initio();

    // Flash red
    masks[0] = 0;
    masks[4] = 8200;

    // Grounds pin 2 which is a pull down for gpio1
    setVal(2, 1);

    // TODO: Set both leds red
    //oneOn(3);

    // setup timer (1ms, repeating)
    os_timer_setfn(&some_timer, (os_timer_func_t *)timerloop, NULL);
    os_timer_arm(&some_timer, 1, 1);
}
