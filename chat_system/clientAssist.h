#ifndef ASS4_CLIENT_CLIENTASSIST_H
#define ASS4_CLIENT_CLIENTASSIST_H

#include <stdio.h>
#include <netdb.h>
#include <stdlib.h>
#include <string.h>
#include <zconf.h>
#include <signal.h>
#include <sys/socket.h>
#include "cList.h"

void communications_authentication_kick(int flag);

void usage_error(int flag);

char *check_argument(int argc, char *argv, int flag);

void print_auth_name(FILE *to, char *message, int flag);

char *name_taken(char *clientName, int number);

void enter_left_list(char *message, int flag);

char *input(FILE *file);

void say(struct Node *message);

struct Node *separate_chara(char *input, char symbol);

#endif //ASS4_CLIENT_CLIENTASSIST_H
