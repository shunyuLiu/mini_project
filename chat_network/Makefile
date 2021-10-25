CC=gcc
CFLAGS= -Wall -g -pthread -pedantic -std=gnu99
.PHONY: all clean
.DEFAULT_GOAL := all

all: client server

client: client.o cList.o clientAssist.o
	$(CC) $(CFLAGS) $^ -o $@

server: server.o cList.o clientList.o clientAssist.o serverAssist.o
	$(CC) $(CFLAGS) $^ -o $@

clean :
	rm -rf client server *.o