#include "clientAssist.h"

/**
 * print message to server
 *
 * @param message message will be sent to stdout
 */
void say(struct Node *message) {
    fprintf(stdout, "%s: %s\n", split(1, message), split(2, message));
    fflush(stdout);
}

/**
 * print error message
 *
 * @param flag check which message will be print
 */
void communications_authentication_kick(int flag) {
    if (flag == 0) {
        fprintf(stderr, "Communications error\n");
        fflush(stderr);
        exit(2);
    } else if (flag == 1) {
        fprintf(stderr, "Authentication error\n");
        fflush(stderr);
        exit(4);
    } else if (flag == 2) {
        fprintf(stderr, "Kicked\n");
        fflush(stderr);
        exit(3);
    }
}

/**
 * print enter, leave, list message
 *
 * @param message the content will be print
 * @param flag check which message will be print
 */
void enter_left_list(char *message, int flag) {
    if (flag == 0) {
        fprintf(stdout, "(%s has entered the chat)\n", message);
        fflush(stdout);
    } else if (flag == 1) {
        fprintf(stdout, "(%s has left the chat)\n", message);
        fflush(stdout);
    } else if (flag == 2) {
        fprintf(stdout, "(current chatters: %s)\n", message);
        fflush(stdout);
    }
}

/**
 * print error message
 *
 * @param flag check which message will be print
 */
void usage_error(int flag) {
    if (flag == 0) {
        fprintf(stderr, "Usage: client name authfile port\n");
    } else if (flag == 1) {
        fprintf(stderr, "Usage: server authfile [port]\n");
    }
    fflush(stderr);
    exit(1);
}

/**
 * send AUTH and NAME handling to server
 *
 * @param to to file pointer to send message to server
 * @param message the content will be sent to message
 * @param flag check it should send AUTH or NAME
 */
void print_auth_name(FILE *to, char *message, int flag) {
    if (flag == 0) {
        fprintf(to, "AUTH:%s\n", message);
        fflush(to);
    } else if (flag == 1) {
        fprintf(to, "NAME:%s\n", message);
        fflush(to);
    }
}

/**
 * if the client has been taken, number will added.
 *
 * @param clientName the number will be added
 * @param number the number will be added
 * @return the client  name
 */
char *name_taken(char *clientName, int number) {
    char *client;
    if (number == -1) {
        client = clientName;
    } else {
        client = malloc(sizeof(char) * strlen(clientName) + 10);
        sprintf(client, "%s%d", clientName, number);
    }
    return client;
}

/**
 * check the client pr the server argument.
 *
 * @param argc the argument number of client or server
 * @param argv the argument value of client or server
 * @param flag check it should be client or server
 * @return the correct authfile's sentence
 */
char *check_argument(int argc, char *argv, int flag) {
    FILE *fp = fopen(argv, "r");
    if (flag == 0) {
        if (argc != 4) {
            usage_error(flag);
        }
        if (fp == NULL) {
            communications_authentication_kick(1);
        }
    } else if (flag == 1) {
        if (argc != 2 && argc != 3) {
            usage_error(flag);
        }
        if (fp == NULL) {
            usage_error(flag);
        }
    }

    char *authSentence = input(fp);
    return authSentence;
}

/**
 * read the stdin/file message line by line
 *
 * @param file the file will be read
 * @return the finished sentence
 */
char *input(FILE *file) {
    int index = 0;
    int word;
    char *sentence = (char *) malloc(sizeof(char) * (index + 1));
    word = fgetc(file);
    if (word == EOF) {
        return NULL;
    }
    while (word != '\n') {
        if (word == EOF) {
            break;
        }
        sentence[index] = (char) word;
        index++;
        sentence = (char *) realloc(sentence, sizeof(char) * (index + 1));
        word = fgetc(file);
    }
    sentence[index] = '\0';
    return sentence;
}

/**
 * separate the sentence by specific character
 *
 * @param input the message will be separated
 * @param symbol the specific character
 * @return a struct stores message
 */
struct Node *separate_chara(char *input, char symbol){
    struct Node *newNode = create_list();
    if (strchr(input, symbol) == NULL) {
        return newNode;
    }
    char *separate = malloc(sizeof(char) * strlen(input) + 1);
    int index = 0;
    for (int i = 0; i < strlen(input); ++i) {
        if (input[i] != symbol) {
            separate[index] = input[i];
            index++;
        } else {
            separate[index] = '\0';
            append(newNode, separate);
            memset(separate, 0, strlen(separate));
            index = 0;
        }
    }
    separate[index] = '\0';
    append(newNode, separate);
    free(separate);
    return newNode;
}

