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


struct Server {
    FILE **socketArray; /* stores two socket file */
    char *authSentence; /* stores correct authfile's sentence */
    char *clientName;/* stores the client name */
    int *number;/*stores the number for the client name*/
    bool *messageFlag;/*sores the message flag*/
    struct Node *messageList;/*stores some message*/
};

/**
 * Function to connect the server
 *
 * @param argv arguments contains port number, client name and so on.
 * @return socket file pointer
 */
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

/**
 * check the AUTH handling and OK handling.
 *
 * @param from file pointer to receive message from server
 * @param to file pointer to send message to server
 * @param authSentence the correct authfile's sentence
 */
void check_auth(FILE *from, FILE *to, char *authSentence) {
    while (true) {
        char *line = input(from);
        struct Node *newLine = separate_chara(line, ':');
        if (newLine->length == 0) {
            continue;
        }
        char *handling = split(0, newLine);
        if (strcmp(handling, "AUTH") == 0) {
            print_auth_name(to, authSentence, 0);
        } else if (strcmp(handling, "OK") == 0) {
            break;
        } else {
            continue;
        }
    }
}

/**
 * check the NAME handling and NAME_TAKEN handling and OK handling.
 * @param from file pointer to receive message from server
 * @param to to file pointer to send message to server
 * @param clientName the name of client
 * @param number the number of client name
 * @param messageFlag if server sends OK, the flag will change to true
 */
void check_name(FILE *from, FILE *to, char *clientName, int *number,
        bool *messageFlag) {
    while (true) {
        char *line = input(from);

        struct Node *newLine = separate_chara(line, ':');
        if (newLine->length == 0) {
            continue;
        }
        char *handling = split(0, newLine);
        if (strcmp(handling, "WHO") == 0) {
            print_auth_name(to, name_taken(clientName, *number), 1);
        } else if (strcmp(handling, "NAME_TAKEN") == 0) {
            *number += 1;
        } else if (strcmp(handling, "OK") == 0) {
            *messageFlag = true;
            break;
        } else {
            continue;
        }
    }
}

/**
 * Communication with server.
 *
 * @param from file pointer to receive message from server
 * @param clientName the name of this client
 */
void compare(FILE *from, char *clientName) {
    while (true) {
        char *line = input(from);
        if(line == NULL) {
            communications_authentication_kick(0);
        }
        struct Node *newLine = separate_chara(line, ':');
        if (newLine->length == 0) {
            continue;
        }
        char *handling = split(0, newLine);
        char *message = split(1, newLine);
        if (strcmp(handling, "KICK") == 0) {
            communications_authentication_kick(2);
        } else if (strcmp(handling, "LIST") == 0) {
            enter_left_list(message, 2);
        } else if (strcmp(handling, "MSG") == 0) {
            say(newLine);
        } else if (strcmp(handling, "ENTER") == 0) {
            enter_left_list(message, 0);
        } else if (strcmp(handling, "LEAVE") == 0) {
            enter_left_list(message, 1);
        }
    }
}

/**
 * A thread to listen server
 *
 * @param args many argument, such as port number, client name and so on
 * @return NULL
 */
void *listen_server(void *args) {
    struct Server *serverList = (struct Server *) args;
    FILE *to = serverList->socketArray[0];
    FILE *from = serverList->socketArray[1];
    char *authSentence = serverList->authSentence;
    char *clientName = serverList->clientName;
    int *number = serverList->number;
    bool *messageFlag = serverList->messageFlag;

    check_auth(from, to, authSentence);
    check_name(from, to, clientName, number, messageFlag);
    compare(from, clientName);
    return NULL;
}

/**
 * print message to server.
 *
 * @param to to file pointer to send message to server
 * @param inputSentence the message from server
 */
void print_message(FILE *to, char *inputSentence) {
    if (strlen(inputSentence) == 0 || inputSentence[0] != '*') {
        fprintf(to, "SAY:%s\n", inputSentence);
        fflush(to);
    }
    if (inputSentence[0] == '*') {
        inputSentence = inputSentence + 1;
        fprintf(to, "%s\n", inputSentence);
        fflush(to);
        if (strcmp(inputSentence, "LEAVE:") == 0) {
            exit(0);
        }
    }
}

/**
 * A thread to listen stdin.
 * @param args many argument, such as port number, client name and so on
 * @return NULL
 */
void *listen_stdin(void *args) {
    struct Server *serverList = (struct Server *) args;
    FILE *to = serverList->socketArray[0];
    struct Node *messageList = serverList->messageList;
    bool *messageFlag = serverList->messageFlag;

    while (true) {
        if (*messageFlag == true) {
            while (messageList->length > 0) {
                char *newMessage = delete(messageList);
                print_message(to, newMessage);
            }
        }

    }
}

int main(int argc, char **argv) {
    struct sigaction sact;
    sact.sa_handler = SIG_IGN;
    sigaction(SIGPIPE, &sact, 0);

    char *authSentence = check_argument(argc, argv[2], 0);
    int number = -1;
    bool messageFlag = false;
    FILE **socketArray = connect_to(argv);

    struct Server server;
    memset(&server, 0, sizeof(server));
    server.authSentence = authSentence;
    server.socketArray = socketArray;
    server.clientName = argv[1];
    server.number = &number;
    server.messageFlag = &messageFlag;
    struct Node *messageList = create_list();
    server.messageList = messageList;

    pthread_t listenServer, listenStdin;
    pthread_create(&listenServer, NULL, listen_server, (void *) &server);
    pthread_detach(listenServer);
    pthread_create(&listenStdin, NULL, listen_stdin, (void *) &server);
    while(true) {
        char *inputSentence = input(stdin);
        append(messageList, inputSentence);
    }
    pthread_join(listenStdin, NULL);
    return 0;
}

