#include "cList.h"

/**
 * create a list
 *
 * @return a successfully created list
 */
struct Node *create_list(){
    struct Node *headNode = (struct Node*) malloc(sizeof(struct Node));
    headNode->length = 0;
    headNode->data = NULL;
    headNode->next = NULL;
    return headNode;
}

/**
 * create a node
 *
 * @param data data will be added
 * @return a successfully created node
 */
struct Node *create_node(char *data){
    struct Node *headNode = (struct Node*)malloc(sizeof(struct Node));
    headNode->data = (char *) malloc(sizeof(char) * strlen(data) + 1);
    strcpy(headNode->data, data);
    headNode->next = NULL;
    return headNode;
}

/**
 * check whether the list is empty
 *
 * @param list a list will be checked
 * @return true - not empty
 *         false - empty
 */
bool whether_empty(struct Node *list){
    if (list != NULL) {
        if (list->next == NULL) {
            return false;
        }
        return true;
    }
    return false;
}

/**
 * append a head node to a list
 *
 * @param headNode list will be added
 * @param data data for the new node
 */
void append(struct Node *headNode, char *data) {
    struct Node *newNode = create_node(data);
    struct Node *currentNode = headNode;
    headNode->length += 1;
    while (currentNode->next != NULL) {
        currentNode = currentNode->next;
    }
    newNode->next = currentNode->next;
    currentNode->next = newNode;
}

/**
 * free the list
 *
 * @param node the list will be free
 */
void free_node(struct Node *node) {
    struct Node *current = node;
    if (current == NULL) {
        return;
    }
    while (node != NULL) {
        current = node;
        if (node->data != NULL) {
            free(node->data);
        }
        if (!node->next) {
            break;
        }
        node = node->next;
        free(current);
    }
}

/**
 *
 * @param index indicate which part will be separate
 *
 * @param node the list will be separated
 * @return the specific data
 */
char *split(int index, struct Node *node) {
    int count = -1;
    while (node->next != NULL) {
        count++;
        node = node->next;
        if (count == index) {
            return node->data;
        }
    }
    return NULL;
}

/**
 * delete the first node
 *
 * @param node list will be deleted
 * @return the deleted data
 */
char *delete(struct Node *node) {
    if (whether_empty(node) == false) {
        return NULL;
    }
    struct Node *currentNode = node;
    struct Node *deleteNode = currentNode->next;
    currentNode->next = currentNode->next->next;
    char *data = deleteNode->data;
    node->length -= 1;
    return data;
}

/**
 * remove the specific node
 *
 * @param chatScript struct stores chatScript message
 * @param index the number of node
 */
void remove_index(struct Node *chatScript, int index) {
    if (chatScript == NULL) {
        return;
    }
    if (index < 0 || index >= chatScript->length) {
        return;
    }
    struct Node *currentScript = chatScript;
    for (int i = 0; i < index; ++i) {
        currentScript = currentScript->next;
    }
    struct Node *deleteScript = currentScript->next;
    currentScript->next = currentScript->next->next;
    free(deleteScript);
    chatScript->length--;
}
