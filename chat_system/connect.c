#include "connect.h"

FILE **connect_to(char **argv) {
    const char *port = argv[3];
    struct addrinfo *ai = 0;
    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    if (getaddrinfo("localhost", port, &hints, &ai)) {
        freeaddrinfo(ai);
        communications_authentication_kick(0);
    }
    int fdOne = socket(AF_INET, SOCK_STREAM, 0);
    if (connect(fdOne, (struct sockaddr *) ai->ai_addr,
                sizeof(struct sockaddr))) {
        communications_authentication_kick(0);
    }
    int fdTwo = dup(fdOne);
    FILE *to = fdopen(fdOne, "w");
    FILE *from = fdopen(fdTwo, "r");
    FILE **socketArray = (FILE **) malloc(sizeof(FILE *) * 2);
    socketArray[0] = to;
    socketArray[1] = from;
    freeaddrinfo(ai);
    return socketArray;
}

void process_connections(int fdServer, char *authSentence) {
    int fd;
    struct sockaddr_in fromAddr;
    socklen_t fromAddrSize;
    struct Client *allClient = create_client_list();

    while (1) {
        fromAddrSize = sizeof(struct sockaddr_in);
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
        pthread_create(&client, NULL, client_processor, (void *) socketFds);
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

