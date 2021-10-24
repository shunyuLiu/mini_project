#ifndef ASS4_CLIENT_CLIENTLIST_H
#define ASS4_CLIENT_CLIENTLIST_H

#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include "cList.h"

struct Client {
    //the name of client
    char *name;
    //file read pointer
    FILE *read;
    //file write pointer
    FILE *write;
    // a lock for thread
    pthread_mutex_t mutex;
    //the length of client
    int length;
    //the next struct
    struct Client *next;
    //time for sighup
    int *time;
};

struct Client *create_client_list();

struct Client *create_client(char *name, FILE *read, FILE *write);

void append_client(struct Client *clientList, char *name, FILE *read,
        FILE *write);

void remove_index_client(struct Client *clientList, char *name);

char *split_client(int index, struct Client *clients);

bool whether_empty_client(struct Client *clients);

bool whether_client(struct Client *clients, char *name);

int name_cmp(const void *x, const void *y);

char **sort(struct Client *clients);

#endif //ASS4_CLIENT_CLIENTLIST_H
