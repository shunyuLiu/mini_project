#include "server.h"

/**
 * open listen adn wait for the client
 *
 * @param port the port number
 * @return socket serverFd
 */
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

/**
 * start a new thread for the client
 *
 * @param fdServer socket fdServer
 * @param authSentence the correct authfile's sentence
 */
void process_connections(int fdServer, char *authSentence,
                         struct Client *allClient, struct ServerTime *time) {
    int fd;
    struct sockaddr_in fromAddr;
    socklen_t fromAddrSize;

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
        argList->time = time;
        pthread_t client;
        pthread_create(&client, NULL, client_processor, (void *) argList);
        pthread_detach(client);
    }
}

/**
 * start to process client
 *
 * @param args many arguments from main
 * @return NULL
 */
void *client_processor(void *args) {

    FILE *to = ((struct ServerVariable *) args)->fds[0];
    FILE *from = ((struct ServerVariable *) args)->fds[1];
    char *authSentence = ((struct ServerVariable *) args)->authSentence;
    struct Client *allClient = ((struct ServerVariable *) args)->allClient;
    struct ServerTime *time = ((struct ServerVariable *) args)->time;

    if (check_server_auth(to, from, authSentence, time)) {
        char *name = check_server_name(allClient, to, from, time);
        struct Client *oneClient = create_client(name, from, to);
        append_client(allClient, name, from, to);
        leave_enter(allClient, name, "ENTER");
        chat_communications(time, allClient, oneClient);
    } else {
        fclose(to);
        fclose(from);
        pthread_exit(NULL);
    }
    return NULL;
}

/**
 * check the authfile's sentence
 *
 * @param to to file pointer to send message
 * @param from from file pointer to receive message
 * @param authSentence the authfile's sentence
 * @param time the time struct of each handling
 * @return true or false
 */
bool check_server_auth(FILE *to, FILE *from, char *authSentence, struct ServerTime *time) {
    while (true) {
        print_message(to, "AUTH");
        struct Node *nameList = separate_chara(input(from), ':');
        if (nameList->length == 0) {
            continue;
        }
        char *auth = split(1, nameList);
        if (strcmp("AUTH", split(0, nameList)) == 0) {
            time_add_server(time, 0);
            if (strcmp(auth, authSentence) == 0) {
                print_message(to, "OK");
                return true;
            } else {
                return false;
            }
        }
    }
}

/**
 * check the server name
 *
 * @param client a list stores all clients
 * @param to to file pointer to send message
 * @param from from file pointer to receive message
 * @param time the time struct of each handling
 * @return the correct name for the client
 */
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
            time_add_server(time, 1);
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

/**
 * start communication with client
 *
 * @param time the time struct of each handling
 * @param clients a list stores all client
 * @param oneClient the current client
 */
void chat_communications(struct ServerTime *time, struct Client *clients,
                         struct Client *oneClient) {
    while (true) {
        FILE *read = oneClient->read;
        char *sentence = input(read);
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
            time_add_server(time, 3);
            oneClient->time[1]++;
            tell_kick(clients, message);
        } else if (strcmp(handling, "LIST") == 0) {
            time_add_server(time, 4);
            oneClient->time[2]++;
            tell_list(clients, oneClient->write);
        } else if (strcmp(handling, "LEAVE") == 0) {
            time_add_server(time, 5);
            leave_enter(clients, oneClient->name, "LEAVE");
            remove_index_client(clients, oneClient->name);
            pthread_exit(NULL);
        } else if (strcmp(handling, "SAY") == 0) {
            time_add_server(time, 2);
            oneClient->time[0]++;
            tell_say(clients, oneClient->name, message);
        }
        usleep(100000);
    }
}

/**
 * handling sighup signal and print to stderr
 *
 * @param clientList a struct contains all clients
 * @param time timer for server
 */
void print_sighup(struct Client *clientList, struct ServerTime *time) {
    char client[3][7] = {"SAY:", "KICK:", "LIST:"};
    char server[6][7] = {"AUTH:", "NAME:", "SAY:", "KICK:", "LIST:", "LEAVE:"};
    fprintf(stderr, "@CLIENTS@\n");
    for (int i = 0; i < clientList->length; i++) {
        fprintf(stderr, "%s", clientList->name);
        for (int j = 0; j < 3; j++) {
            fprintf(stderr, ":%s%d", client[j], clientList->time[j]);
        }
        fprintf(stderr, "\n");
    }
    fprintf(stderr, "@SERVER@\nserver");
    for (int i = 0; i < 6; ++i) {
        fprintf(stderr, ":%s%d", server[i], time->serverTime[i]);
    }
    fprintf(stderr, "\n");
}

/**
 * set signal
 *
 * @return a sigset contains the signal we need.
 */
sigset_t set_sig() {
    sigset_t set;
    sigemptyset(&set);
    sigaddset(&set, SIGHUP);
    sigaddset(&set, SIGPIPE);
    pthread_sigmask(SIG_BLOCK, &set, NULL);
    return set;
}

/**
 * process signal
 *
 * @param args arguments
 * @return NULL
 */
void *process_signal(void *args) {
    struct SigHup *argList = (struct SigHup *) args;
    int sig;
    while (true) {
        sigwait(&argList->set, &sig);
        if (sig == SIGHUP) {
            print_sighup(argList->allClient, argList->time);
        } else if (sig == SIGPIPE) {
            continue;
        } else {
            break;
        }
    }
    pthread_exit(NULL);
}

int main(int argc, char **argv) {
    struct Client *allClient = create_client_list();
    struct ServerTime *time = create_time();
    char *authSentence = check_argument(argc, argv[1], 1);
    int fdServer = open_listen(argv[2]);

    sigset_t set = set_sig();
    struct SigHup handlingSignal;
    handlingSignal.set = set;
    handlingSignal.allClient = allClient;
    handlingSignal.time = time;
    pthread_t signal;
    pthread_create(&signal, NULL, &process_signal, (void *) &handlingSignal);
    pthread_detach(signal);

    process_connections(fdServer, authSentence, allClient, time);
    return 0;
}

