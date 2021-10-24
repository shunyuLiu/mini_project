#include "server.h"

void process_connections(int fdServer, char *authSentence) {
    int fd;
    struct sockaddr_in fromAddr;
    socklen_t fromAddrSize;
    struct Client *allClient = create_client_list();

    while (1) {
        fromAddrSize = sizeof(struct sockaddr_in);
        if (fdServer == -1) {
            continue;
        }
        fd = accept(fdServer, (struct sockaddr *) &fromAddr, &fromAddrSize);
        if (fd < 0) {
            exit(1);
        }
        char hostname[NI_MAXHOST];
        int error = getnameinfo((struct sockaddr *) &fromAddr,
                                fromAddrSize, hostname, NI_MAXHOST, NULL, 0, 0);
        if (error) {
            continue;
        }
        int fdTwo = dup(fd);
        FILE *to = fdopen(fd, "w");
        FILE *from = fdopen(fdTwo, "r");
        FILE **socketFds = (FILE **) malloc(sizeof(FILE *) * 2);
        socketFds[0] = to;
        socketFds[1] = from;
        struct ServerVariable *argList = (struct ServerVariable *) malloc(
                sizeof(struct ServerVariable));
        argList->fds = socketFds;
        argList->authSentence = authSentence;
        argList->allClient = allClient;
        pthread_t client;
        pthread_create(&client, NULL, client_processor, (void *) argList);
        //pthread_join(client, NULL);
        pthread_detach(client);
    }
}

int open_listen(char *port) {
    struct addrinfo *ai = 0;
    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    int err;
    int optVal = 1;
    int servFd = socket(AF_INET, SOCK_STREAM, 0);
    if ((err = getaddrinfo("localhost", port, &hints, &ai))) {
        communications_authentication_kick(0);
    }

    if (setsockopt(servFd, SOL_SOCKET, SO_REUSEADDR, &optVal, sizeof(int)) <
        0) {
        communications_authentication_kick(0);
    }

    if (bind(servFd, (struct sockaddr *) ai->ai_addr,
             sizeof(struct sockaddr))) {
        communications_authentication_kick(0);
    }

    struct sockaddr_in ad;
    memset(&ad, 0, sizeof(struct sockaddr_in));
    socklen_t len = sizeof(struct sockaddr_in);
    if (getsockname(servFd, (struct sockaddr *) &ad, &len)) {
        communications_authentication_kick(0);
    }
    fprintf(stderr, "%u\n", ntohs(ad.sin_port));
    fflush(stderr);

    if (listen(servFd, 128) < 0) {
        communications_authentication_kick(0);
    }
    return servFd;
}

char *check_server_name(struct Client *client, FILE *to, FILE *from,
                 struct ServerTime *time) {
    while (true) {
        print_message(to, "WHO");
        struct Node *nameList = separate_chara(input(from), ':');
        if (nameList->length == 0) {
            continue;
        }
        char *name = split(1, nameList);
        if (strcmp("NAME", split(0, nameList)) == 0) {
            //time_add_server(time, 1);
            if (strlen(name) == 0) {
                print_message(to, "NAME_TAKEN");
            }
            if (whether_client(client, name) == true) {
                print_message(to, "NAME_TAKEN");
            } else {
                print_message(to, "OK");
                return name;
            }
        }
    }
}

bool check_server_auth(FILE *to, FILE *from, char *authSentence, struct ServerTime *time) {
    while (true) {
        print_message(to, "AUTH");
        struct Node *nameList = separate_chara(input(from), ':');
        if (nameList->length == 0) {
            continue;
        }
        char *auth = split(1, nameList);
        if (strcmp("AUTH", split(0, nameList)) == 0) {
            //time_add_server(time, 0);
            if (strcmp(auth, authSentence) == 0) {
                print_message(to, "OK");
                return true;
            } else {
                return false;
            }
        }
    }
}

void chat_communications(struct ServerTime *time, struct Client *clients,
                         struct Client *oneClient) {
    while (true) {
        FILE *read = oneClient->read;
        char *sentence = input(read);
        //todo
        if (sentence == NULL) {
            leave_enter(clients, oneClient->name, "LEAVE");
            remove_index_client(clients, oneClient->name);
            pthread_exit(NULL);
        }
        struct Node *inputList = separate_chara(sentence, ':');
        if (inputList->length == 0) {
            continue;
        }
        char *handling = split(0, inputList);
        char *message = split(1, inputList);

        if (strcmp(handling, "KICK") == 0) {
            //time_add_server(time, 3);
            //oneClient->time[1]++;
            tell_kick(clients, message);
            pthread_exit(NULL);
        } else if (strcmp(handling, "LIST") == 0) {
            //time_add_server(time, 4);
            oneClient->time[2]++;
            tell_list(clients, oneClient->write);
        } else if (strcmp(handling, "LEAVE") == 0) {
            //time_add_server(time, 5);
            leave_enter(clients, oneClient->name, "LEAVE");
            remove_index_client(clients, oneClient->name);
            pthread_exit(NULL);
            //todo
        } else if (strcmp(handling, "SAY") == 0) {
            //time_add_server(time, 2);
            oneClient->time[0]++;
            tell_say(clients, oneClient->name, message);
        }
        usleep(100000);
    }
}

void *client_processor(void *args) {

    FILE *to = ((struct ServerVariable *) args)->fds[0];
    FILE *from = ((struct ServerVariable *) args)->fds[1];
    char *authSentence = ((struct ServerVariable *) args)->authSentence;
    struct Client *allClient = ((struct ServerVariable *) args)->allClient;

    if (check_server_auth(to, from, authSentence, NULL)) {
        char *name = check_server_name(allClient, to, from, NULL);
        struct Client *oneClient = create_client(name, from, to);
        append_client(allClient, name, from, to);
        leave_enter(allClient, name, "ENTER");
        chat_communications(NULL, allClient, oneClient);
    } else {
        fclose(to);
        fclose(from);
        pthread_exit(NULL);
    }
    return NULL;
}

int main(int argc, char **argv) {
    struct sigaction sact;
    sact.sa_handler = SIG_IGN;
    sigaction(SIGPIPE, &sact, 0);

    char *authSentence = check_argument(argc, argv[1], 1);
    int fdServer = open_listen(argv[2]);
    process_connections(fdServer, authSentence);
    return 0;
}

