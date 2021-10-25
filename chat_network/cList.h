#include <stdio.h>
#include <string.h>
#include <mm_malloc.h>
#include <stdbool.h>

#ifndef ASS3_CLIST_H
#define ASS3_CLIST_H

/**
* a struct stores length, data, and the next struct
*/
struct Node {
    //the length of the struct
    int length;
    //every struct stores data
    char *data;
    //the address of next struct
    struct Node *next;
};

struct Node *create_list();

struct Node *create_node(char *data);

void append(struct Node *headNode, char *data);

void print_list(struct Node *headNode);

void free_node(struct Node *list);

bool whether_empty(struct Node *node);

char *split(int index, struct Node *node);

void append_character(struct Node *headNode, char *data, int *length);

char *delete(struct Node *node);

void remove_index(struct Node *chatScript, int index);

#endif //ASS3_cLIST_H

