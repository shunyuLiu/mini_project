#include "clientList.h"

/**
 * create a client list struct
 *
 * @return return a client struct
 */
struct Client *create_client_list(){
    struct Client *clientList = (struct Client *)malloc(sizeof(struct Client));
    clientList->name = 0;
    clientList->read = NULL;
    clientList->write = NULL;
    clientList->next = NULL;
    clientList->length = 0;
    clientList->time = 0;
    pthread_mutex_init(&clientList->mutex, NULL);
    return clientList;
}

/**
 * create a struct client
 *
 * @param name the name of client
 * @param read read pipe
 * @param write write pipe
 * @return a created client
 */
struct Client *create_client(char *name, FILE *read, FILE *write){
    struct Client *client = (struct Client*)malloc(sizeof(struct Client));
    client->name = name;
    client->read = read;
    client->write = write;
    client->next = NULL;
    client->time = (int *)malloc(sizeof(int) * 3);
    return client;
}

/**
* append a client to the client struct
*
* @param clientList a struct stores all clients
* @param name the name of client
* @param read read pipe
* @param write write pipe
*/
void append_client(struct Client *clientList, char *name, FILE *read,
        FILE *write) {
    pthread_mutex_lock(&clientList->mutex);
    struct Client *newClient = create_client(name, read, write);
    struct Client *currentClient = clientList;
    clientList->length += 1;
    while (currentClient->next != NULL) {
        currentClient = currentClient->next;
    }
    newClient->next = currentClient->next;
    currentClient->next = newClient;
    pthread_mutex_unlock(&clientList->mutex);
}

/**
 * remove the specific client
 *
 * @param clientList a struct stores all clients
 * @param name the name of client
 */
void remove_index_client(struct Client *clientList, char *name) {
    pthread_mutex_lock(&clientList->mutex);
    if (clientList == NULL) {
        return;
    }
    struct Client *currentClient = clientList;
    for (int i = 0; i < clientList->length; ++i) {
        if (strcmp(currentClient->next->name, name) == 0) {
            struct Client *deleteClient = currentClient->next;
            currentClient->next = currentClient->next->next;
            clientList->length--;
            fclose(deleteClient->read);
            fclose(deleteClient->write);
            free(deleteClient);
        } else {
            currentClient = currentClient->next;
        }
    }
    pthread_mutex_unlock(&clientList->mutex);
}

/**
 * split the client list and get the client we want
 *
 * @param index the number of client we want
 * @param clients the list stores all clients
 * @return the name client
 */
char *split_client(int index, struct Client *clients) {
    int count = -1;
    while (clients->next != NULL) {
        count++;
        clients = clients->next;
        if (count == index) {
            return clients->name;
        }
    }
    return NULL;
}

/**
 * check whether the client list is empty
 *
 * @param clients the client list will be added
 * @return
 */
bool whether_empty_client(struct Client *clients){
    pthread_mutex_lock(&clients->mutex);
    if (clients != NULL) {
        if (clients->next == NULL) {
            pthread_mutex_unlock(&clients->mutex);
            return false;
        }
        pthread_mutex_unlock(&clients->mutex);
        return true;
    }
    pthread_mutex_unlock(&clients->mutex);
    return false;
}

/**
 * check whether the struct has the specific client
 *
 * @param clients a struct stores all clients
 * @param name
 * @return true - has the client
 *         false - not has the client
 */
bool whether_client(struct Client *clients, char *name){
    pthread_mutex_lock(&clients->mutex);
    struct Client *currentClient = clients->next;
    for (int i = 0; i < clients->length; ++i) {
        if (strcmp(currentClient->name, name) == 0) {
            pthread_mutex_unlock(&clients->mutex);
            return true;
        }
        currentClient = currentClient->next;
    }
    pthread_mutex_unlock(&clients->mutex);
    return false;
}

/**
 * compare two names
 *
 * @param x word x
 * @param y word y
 * @return if A > B, return positive number
 *         if A < B, return negative number
 *         if A == B, return zero
 */
int name_cmp(const void *x, const void *y) {
    return strcmp(*(char *const *) x, *(char *const *) y);
}

/**
 * The name should be in lexicographical order
 *
 * @param clients the ordered client list
 * @return the successfully order sentence
 */
char **sort(struct Client *clients) {
    char **buffer = (char **) malloc(sizeof(char *) * clients->length);
    for (int i = 0; i < clients->length; ++i) {
        buffer[i] = strdup(split_client(i, clients));
    }
    qsort(buffer, clients->length, sizeof(char *), name_cmp);
    return buffer;
}
