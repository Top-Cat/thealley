CC = xtensa-lx106-elf-gcc
CFLAGS = -I. -DICACHE_FLASH -mlongcalls -fdata-sections -ffunction-sections -DWPA2_PASSWORD=\"${WPA2_PASSWORD}\"
LDLIBS = -nostdlib -Wl,--start-group -lmain -lnet80211 -lwpa -lwpa2 -llwip -lpp -lphy -lc -Wl,--end-group -lgcc -Wl,--gc-sections
LDFLAGS = -Teagle.app.v6.ld

main-0x00000.bin: main
	esptool.py elf2image $^

main: main.o io.o

main.o: main.c

io.o: io.c

flash: main-0x00000.bin
	esptool.py -p /dev/cu.usbserial-00000000 -b 921600 write_flash -fs 32m -ff 40m -fm dout 0 main-0x00000.bin 0x10000 main-0x10000.bin

clean:
	rm -f main main.o io.o main-0x00000.bin main-0x10000.bin
