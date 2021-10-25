#include "serverAssist.h"

/**
 * create the time list
 *
 * @return the list for time
 */
struct ServerTime *create_time() {
    struct ServerTime *time = 
            (struct ServerTime *) malloc(sizeof(struct ServerTime));
    time->serverTime = (int *) malloc(sizeof(int) * 6);
    memset(time->serverTime, 0, sizeof(int) * 6);
    pthread_mutex_init(&time->serverMutex, NULL);
    return time;
}

/**
 * add one depend on the index
 *
 * @param time the time list
 * @param index indicate which handling time will be added
 */
void time_add_server(struct ServerTime *time, int index) {
    pthread_mutex_lock(&time->serverMutex);
    time->serverTime[index]++;
    pthread_mutex_unlock(&time->serverMutex);
}

/**
 * send message to client
 *
 * @param to to file pointer
 * @param message message will be sent
 */
void print_message(FILE *to, char *message) {
    fprintf(to, "%s:\n", message);
    fflush(to);
}

/**
 * tell all client the kick message
 *
 * @param clients the list stores all client
 * @param name the name of current client
 */
void tell_kick(struct Client *clients, char *name) {
    struct Client *currentClient = clients->next;
    pthread_mutex_lock(&clients->mutex);
    for (int i = 0; i < clients->length; ++i) {
        if (strcmp(name, currentClient->name) == 0) {
            fprintf(currentClient->write, "KICK:\n");
            fflush(currentClient->write);
        }
        currentClient = currentClient->next;
    }
    pthread_mutex_unlock(&clients->mutex);
}

/**
 * tell all client the say message
 *
 * @param clients the list stores all client
 * @param name the name of current client
 * @param message message will be sent
 */
void tell_say(struct Client *clients, char *name, char *message) {
    fprintf(stdout, "%s: %s\n", name, message);
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

/**
 * show the name list
 *
 * @param clients the list stores all client
 * @param write write file pointer to send message to client
 */
void tell_list(struct Client *clients, FILE *write) {
    pthread_mutex_lock(&clients->mutex);
    int size = 6;
    char **names = sort(clients);
    char *printMessage = (char *)malloc(sizeof(char) * 6);
    sprintf(printMessage, "LIST:");
    for (int i = 0; i < clients->length; i++) {
        size += strlen(names[i]) + 1;
        printMessage = (char *)realloc(printMessage, sizeof(char) * size);
        sprintf(printMessage, "%s%s,", printMessage, names[i]);
    }
    printMessage[size - 2] = '\0';
    fprintf(write, "%s\n", printMessage);
    fflush(write);
    pthread_mutex_unlock(&clients->mutex);
}

/**
 * send the leave or enter message to client and the server stdout
 *
 * @param clients the list stores all client
 * @param name the name of current client
 * @param handling indicate whether the LEAVE or ENTER handling
 */
void leave_enter(struct Client *client, char *name, char *handling) {
    if (strcmp("ENTER", handling) == 0) {
        fprintf(stdout, "(%s has entered the chat)\n", name);
        fflush(stdout);
    } else {
        fprintf(stdout, "(%s has left the chat)\n", name);
        fflush(stdout);
    }
    struct Client *currentClients = client->next;
    pthread_mutex_lock(&client->mutex);
    for (int i = 0; i < client->length; ++i) {
        if (strcmp(currentClients->name, name) == 0 
                && strcmp("LEAVE", handling) == 0) {
            currentClients = currentClients->next;
            continue;
        }
        FILE *write = currentClients->write;
        fprintf(write, "%s:%s\n", handling, name);
        fflush(write);
        currentClients = currentClients->next;
    }
    pthread_mutex_unlock(&client->mutex);
}
