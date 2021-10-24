#ifndef ASS4_CLIENT_SERVER_H
#define ASS4_CLIENT_SERVER_H

#include <stdio.h>
#include <netdb.h>
#include <stdlib.h>
#include <string.h>
#include <zconf.h>
#include <signal.h>
#include <stdbool.h>
#include <sys/socket.h>
#include <pthread.h>
#include "cList.h"
#include "clientAssist.h"
#include "clientList.h"
#include "serverAssist.h"
#include "server.h"

struct ServerVariable {
    FILE **fds;
    char *authSentence;
    struct Client *allClient;
    //struct ServerTime
};

int open_listen(char *port);

void process_connections(int fdServer, char *authSentence);

char *check_server_name(struct Client *client, FILE *to, FILE *from,
        struct ServerTime *time);

bool check_server_auth(FILE *to, FILE *from, char *authSentence, 
		struct ServerTime *time);

void chat_communications(struct ServerTime *time, struct Client *clients,
        struct Client *oneClient);

void *client_processor(void *args);

int main(int argc, char **argv);


#endif //ASS4_CLIENT_SERVER_H
