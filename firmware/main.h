#include "user_interface.h"
#include "ets_sys.h"
#include "osapi.h"
#include "espconn.h"
#include "io.h"

#include "wpa2_enterprise.h"

int ICACHE_FLASH_ATTR shiftToPins(int data);

void ICACHE_FLASH_ATTR sent_cb(void *arg);
void ICACHE_FLASH_ATTR tcp_connected(void *arg);
void ICACHE_FLASH_ATTR tcp_disconnected(void *arg);
void ICACHE_FLASH_ATTR wifi_callback(System_Event_t *evt);
void ICACHE_FLASH_ATTR dns_done(const char *name, ip_addr_t *ipaddr, void *arg);
void ICACHE_FLASH_ATTR data_received(void *arg, char *pdata, unsigned short len);

void ICACHE_FLASH_ATTR timerloop(void *arg);

void ICACHE_FLASH_ATTR tcp_reconnect();

void ICACHE_FLASH_ATTR user_init();
