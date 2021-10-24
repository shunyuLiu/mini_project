#include "serverAssist.h"

struct ServerTime *create_time() {
    struct ServerTime *time = (struct ServerTime *) malloc(sizeof(struct ServerTime));
    time->serverTime = (int *) malloc(sizeof(int) * 6);
    memset(time->serverTime, 0, sizeof(int) * 6);
    pthread_mutex_init(&time->serverMutex, NULL);
    return time;
}

void time_add_server(struct ServerTime *time, int index) {
    pthread_mutex_lock(&time->serverMutex);
    time->serverTime[index]++;
    pthread_mutex_unlock(&time->serverMutex);
}

void print_message(FILE *to, char *message) {
    fprintf(to, "%s:\n", message);
    fflush(to);
}

void tell_kick(struct Client *clients, char *name) {
    struct Client *currentClients = clients->next;
    int length = clients->length;
    bool flag = false;
    if(whether_client(clients, name) == true) {
        flag = true;
        fprintf(stdout, "%s has left the chat\n", name);
        fflush(stdout);
    } else {
        return;
    }
    for (int i = 0; i < length; ++i) {
        if (strcmp(currentClients->name, name) == 0) {
            print_message(currentClients->write, "KICK");
            remove_index_client(clients, name);
        } else {
            if (currentClients->next != NULL) {
                FILE *write = currentClients->write;
                if (flag == true) {
                    fprintf(write, "LEAVE:%s\n", name);
                    fflush(write);
                }
                currentClients = currentClients->next;
            }
        }
    }
}

void tell_say(struct Client *clients, char *name, char *message) {
    fprintf(stdout, "%s:%s\n", name, message);
    fflush(stdout);
    pthread_mutex_lock(&clients->mutex);
    struct Client *currentClients = clients->next;
    for (int i = 0; i < clients->length; ++i) {
        fprintf(currentClients->write, "MSG:%s:%s\n", name, message);
        fflush(currentClients->write);
        if (currentClients != NULL) {
            currentClients = currentClients->next;
        }

    }
    pthread_mutex_unlock(&clients->mutex);
}

void tell_list(struct Client *clients, FILE *write) {
    pthread_mutex_lock(&clients->mutex);
    int size = 6;
    char **names = sort(clients);
    char *printMessage = (char *)malloc(sizeof(char) * 6);
    sprintf(printMessage, "LIST:");
    for (int i = 0; i < clients->length; i++) {
        size += strlen(names[i]) + 1;
        printMessage = (char *)realloc(printMessage, sizeof(char) * size);
        sprintf(printMessage, "%s%s,", printMessage,names[i]);
    }
    printMessage[size - 2] = '\0';
    fprintf(write, "%s\n", printMessage);
    fflush(write);
    pthread_mutex_unlock(&clients->mutex);
}

void leave_enter(struct Client *client, char *name, char *handling) {
    if (strcmp("ENTER", handling) == 0) {
        fprintf(stdout, "%s has entered the chat\n", name);
        fflush(stdout);
    } else {
        fprintf(stdout, "%s has left the chat\n", name);
        fflush(stdout);
    }
    struct Client *currentClients = client->next;
    pthread_mutex_lock(&client->mutex);
    for (int i = 0; i < client->length; ++i) {
        FILE *write = currentClients->write;
        fprintf(write, "%s:%s\n", handling, name);
        fflush(write);
        currentClients = currentClients->next;
    }
    pthread_mutex_unlock(&client->mutex);
}
