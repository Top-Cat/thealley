#include "ets_sys.h"

void ICACHE_FLASH_ATTR gpio16_output_conf(void);
void ICACHE_FLASH_ATTR gpio16_output_set(uint8 value);
void ICACHE_FLASH_ATTR gpio16_input_conf(void);

void ICACHE_FLASH_ATTR setSpec(int val);
void ICACHE_FLASH_ATTR setVal(int d, int val);
void ICACHE_FLASH_ATTR setOnMask(int onMask);

void initio();
