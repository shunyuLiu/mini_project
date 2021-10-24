#ifndef ASS4_CLIENT_CONNECT_H
#define ASS4_CLIENT_CONNECT_H

#include <stdio.h>
#include <netdb.h>
#include <stdlib.h>
#include <string.h>
#include <zconf.h>
#include <signal.h>
#include <stdbool.h>
#include <sys/socket.h>
#include <pthread.h>
#include <arpa/inet.h>
#include "cList.h"
#include "clientAssist.h"
#include "server.h"
#include "clientList.h"


struct ServerVariable {
    FILE **fds;
    char *authSentence;
    struct Client *allClient;
    //struct ServerTime
};

FILE **connect_to(char **argv);

int open_listen(char *port);

void process_connections(int fdServer, char *authSentence);

#endif //ASS4_CLIENT_CONNECT_H
