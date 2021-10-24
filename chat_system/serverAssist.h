#ifndef ASS4_CLIENT_SERVERASSIST_H
#define ASS4_CLIENT_SERVERASSIST_H

#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "cList.h"
#include "clientAssist.h"
#include "clientList.h"

struct ServerTime {
    int *serverTime;
    pthread_mutex_t serverMutex;
};

struct ServerTime *create_time();

void time_add_server(struct ServerTime *time, int index);

void print_message(FILE *to, char *message);

void tell_kick(struct Client *clients, char *name);

void tell_say(struct Client *clients, char *name, char *message);

void tell_list(struct Client *clients, FILE *write);

void leave_enter(struct Client *client, char *name, char *handling);

#endif //ASS4_CLIENT_SERVERASSIST_H
