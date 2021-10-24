import tkinter as tk
import random
from tkinter import messagebox, filedialog
from tkinter import simpledialog
import time
from PIL import Image, ImageTk

POKEMON = "☺"
FLAG = "♥"
UNEXPOSED = "~"
EXPOSED = "0"
TASK_ONE = 'TASK_ONE'
TASK_TWO = 'TASK_TWO'
UP = "up"
DOWN = "down"
LEFT = "left"
RIGHT = "right"
DIRECTIONS = (UP, DOWN, LEFT, RIGHT,
              f"{UP}-{LEFT}", f"{UP}-{RIGHT}",
              f"{DOWN}-{LEFT}", f"{DOWN}-{RIGHT}")

NUMBER_IMAGE = {'0': 'zero', '1': 'one', '2': 'two', '3': 'three', '4': 'four', '5': 'five', '6': 'six', '7': 'seven',
                '8': 'eight'}
POKEMON_IMAGE = {0: 'charizard', 1: 'cyndaquil', 2: 'pikachu', 3: 'psyduck', 4: 'togepi', 5: 'umbreon'}


class BoardModel:
    """TThe BoardModel class should be used to store and manage the internal game state"""
    def __init__(self, grid_size, num_pokemon):
        """Construct a new selection panel canvas.

        Parameters:
            grid_size (int): Integer which shows the size of the grid.
            num_pokemon (int): The number of pokemons.
            """
        self._grid_size = grid_size
        self._num_pokemon = num_pokemon
        self._pokemon_locations = ()
        self._game_board = UNEXPOSED * self._grid_size ** 2
        self.generate_pokemons()

    def get_game(self):
        """Returns the game board"""
        return self._game_board

    def get_pokemon_locations(self):
        """Returns all pokemons' locations"""
        return self._pokemon_locations

    def get_num_attempted_catches(self):
        """Returns the number of pokeballs currently placed on the board."""
        attempted_number = 0
        for i in range(len(self._game_board)):
            if self._game_board[i] == FLAG:
                attempted_number += 1
        return attempted_number

    def get_num_pokemon(self):
        """Returns the number of pokemon hidden in the game."""
        hidden_pokemon = len(self._pokemon_locations) - self.get_num_attempted_catches()
        return hidden_pokemon

    def check_loss(self, index):
        """Returns True iff the game has been lost, else False."""
        if UNEXPOSED not in self._game_board and self._game_board.count(FLAG) == len(self._pokemon_locations):
            return True
        elif index in self._pokemon_locations and self._game_board[index] != FLAG:
            return False

    def replace_character_at_index(self, index, character):
        """This method change the old game string to an updated game string with the specified character placed at the
        specified index

        Parameters:
            index(int): Index of the currently clicked cell
            character(str): A specified character that the player chose
        """
        self._game_board = self._game_board[:index] + character + self._game_board[index + 1:]

    def flag_cell(self, index):
        """This method change the old game string to an updated game string  after “toggling” the flag with the
            specified character placed at the specified index

        Parameters:
            index(int): Index of the currently selected cell
        """
        if self._game_board[index] == FLAG:
            self.replace_character_at_index(index, UNEXPOSED)  # use the above function
        elif self._game_board[index] == UNEXPOSED:
            self.replace_character_at_index(index, FLAG)

    def generate_pokemons(self):
        """Pokemons will be generated and given a random index within the game

        Self._pokemon_locations (tuple<int>): A tuple containing  indexes where the pokemons are
        created for the game string.
        """
        cell_count = self._grid_size ** 2

        for _ in range(self._num_pokemon):
            if len(self._pokemon_locations) >= cell_count:
                break
            index = random.randint(0, cell_count - 1)

            while index in self._pokemon_locations:
                index = random.randint(0, cell_count - 1)

            self._pokemon_locations += (index,)

    def position_to_index(self, position):
        """This method convert the (row,column) to a game strings index.
        Parameters:
        position(tuple<int, int>): Tuple representing a row, column position of a cell.
        Returns:
        (int): An integer show the index of the cell in the game string.
        """
        x, y = position
        return x * self._grid_size + y

    def index_in_direction(self, index, direction):
        """This function takes in the index to a cell in the game string and returns a new index corresponding to an
        adjacent cell in the specified direction. Return None for invalid directions.
        eg.Grid_size is 3, index = 7 up is 5, down is none

        Parameters:
            index(int): Index of the currently selected cell
            direction(str):Directions to neighbouring cells are eight directions. See DIRECTIONS in the support code.

        Returns:
            (int): A number at a specified direction
        """
        # convert index to row, col coordinate
        col = index % self._grid_size
        row = index // self._grid_size
        if RIGHT in direction:
            col += 1
        elif LEFT in direction:
            col -= 1

        if UP in direction:
            row -= 1
        elif DOWN in direction:
            row += 1
        if not (0 <= col < self._grid_size and 0 <= row < self._grid_size):
            return None
        return self.position_to_index((row, col))

    def neighbour_directions(self, index):
        """This method returns a list of all valid indices which neighbour the supplied index.
        Parameters:
            index(int): Index of the currently selected cell
        Returns:
            (list<int>): A list of index that has a neighbouring cell.
        """
        neighbours = []
        for direction in DIRECTIONS:
            neighbour = self.index_in_direction(index, direction)
            if neighbour is not None:
                neighbours.append(neighbour)
        return neighbours

    def number_at_cell(self, index):
        """This method returns the number of Pokemon in neighbouring cells.
        Parameters:
            index(int): Index of the currently selected cell
        Returns:
                (int): The number of Pokemon in neighbouring cells
        """
        number = 0
        for neighbour in self.neighbour_directions(index):
            if neighbour in self._pokemon_locations:
                number += 1
        return number

    def big_fun_search(self, index):
        """Searching adjacent cells to see if there are any Pokemon"s present.

        Using some sick algorithms.

        Find all cells which should be revealed when a cell is selected.

        For cells which have a zero value (i.e. no neighbouring pokemons) all the cell"s
        neighbours are revealed. If one of the neighbouring cells is also zero then
        all of that cell"s neighbours are also revealed. This repeats until no
        zero value neighbours exist.

        For cells which have a non-zero value (i.e. cells with neighbour pokemons), only
        the cell itself is revealed.

    Parameters:
        index (int): Index of the currently selected cell
    Returns:
        (list<int>): List of cells to turn visible.
        """
        queue = [index]
        discovered = [index]
        visible = []
        if self._game_board[index] == FLAG:
            return queue

        number = self.number_at_cell(index)
        if number != 0:
            return queue

        while queue:
            node = queue.pop()
            for neighbour in self.neighbour_directions(node):
                if neighbour in discovered:
                    continue

                discovered.append(neighbour)
                if self._game_board[neighbour] != FLAG:
                    number = self.number_at_cell(neighbour)
                    if number == 0:
                        queue.append(neighbour)
                visible.append(neighbour)
        return visible

    def new_game(self):
        """Restart to a new game (i.e. new pokemon locations).
        Use the same grid size and number of pokemon as the current game.
        """
        self.restart_game()
        self._pokemon_locations = ()
        self.generate_pokemons()

    def restart_game(self):
        """Restart the current game, including game timer. Pokemon locations should persist. """
        self._game_board = UNEXPOSED * self._grid_size ** 2

    def set_load_game(self, grid_size, game_board, pokemon_locations):
        """Get the load_game data."""
        self._grid_size = grid_size
        self._game_board = game_board
        list_location = []
        pokemon_locations = pokemon_locations[1:-1].split(',')
        for i in pokemon_locations:
            list_location.append(int(i))
        self._pokemon_locations = tuple(list_location)


class PokemonGame:
    """PokemonGame represents the controller class."""
    def __init__(self, master, grid_size=10, num_pokemon=15, task=TASK_TWO):
        """Create a new game pokemonGame within a master widget
        Parameters:
            master (tk.Widget): Widget within which the board is placed.
            grid_size (int): Integer which shows the size of the grid.
            num_pokemon (int): The number of pokemons.
            task (str): Task is some constant (defined by myself) that allows the game to be displayed.
        """
        self._master = master
        self._grid_size = grid_size
        self._num_pokemon = num_pokemon
        self._task = task
        self.board_width = 600
        self._load_time = (0, 0)

        self._pokemon_game = BoardModel(self._grid_size, self._num_pokemon)
        self._board_view = BoardView(self._master, self._grid_size, self.board_width, self._pokemon_game)
        self._image_board_view = ImageBoardView(self._master, self._grid_size, self.board_width, self._pokemon_game)
        self._status_bar = StatusBar(self._master, self._num_pokemon)
        self._filename = None
        self.draw()

    def draw(self):
        """Draw the game to the master widget."""
        self._label1 = tk.Label(self._master, text='Pokemon: Got 2 Find Them All!', bg='#E16666', fg='#FFFFFF',
                                font=('Courier New', 32, 'bold'))
        self._label1.pack(side=tk.TOP, fill=tk.BOTH)
        # choose one or two.
        if self._task == TASK_ONE:
            self._board_view.pack()
            self._board_view.draw_board(self._pokemon_game.get_game())
        elif self._task == TASK_TWO:
            self._image_board_view.pack()
            self._image_board_view.draw_board(self._pokemon_game.get_game())
            self._status_bar.draw_status_bar()
            self._image_board_view.get_bar(self._status_bar)
            self.create_menu()
            self.create_button()

    def create_menu(self):
        """Create the menu for TASK_TWO"""
        self._menu_bar = tk.Menu(self._master)
        self._master.config(menu=self._menu_bar)
        self._file_menu = tk.Menu(self._menu_bar, tearoff=0)
        self._menu_bar.add_cascade(label='File', menu=self._file_menu)
        self._file_menu.add_command(label="Save game", command=self.save_game)
        self._file_menu.add_command(label="Load game", command=self.load_game)
        self._file_menu.add_command(label="Restart game", command=self.restart_game)
        self._file_menu.add_command(label="New game", command=self.new_game)
        self._file_menu.add_command(label="Quit", command=self.quit)

    def create_button(self):
        """Create the button for TASK_TWO"""
        self._button_frame = tk.Frame(self._master)
        self._button_frame.pack(side=tk.RIGHT, expand=1)

        restart_button = tk.Button(self._button_frame, text="New game", command=self.new_game)
        restart_button.pack(side=tk.TOP)

        new_button = tk.Button(self._button_frame, text="Restart Game", command=self.restart_game)
        new_button.pack(side=tk.TOP)

    def save_game(self):
        """Prompt the user for the location to save their file
        and save all necessary information to replicate the current state of the game
        """
        game_data = f"{self._grid_size};{self._pokemon_game.get_game()};{self.board_width};" \
                    f"{self._pokemon_game.get_pokemon_locations()};{self._status_bar.get_time()}"

        if self._filename is None:
            filename = filedialog.asksaveasfilename()
            if filename:
                self._filename = filename
        if self._filename:
            self._master.title(self._filename)
            with open(self._filename, 'w') as file:
                file.write(game_data)

    def load_game(self):
        """Prompt the user for the location of the file to load a game from and load the game described in that file
        """
        filename = filedialog.askopenfilename()
        with open(filename, 'r', encoding='utf_8') as file:
            load_game_data = file.readlines()
            for line in range(len(load_game_data)):
                load_game_data = load_game_data[line].split(';')

            self._grid_size = int(load_game_data[0])
            self.board_width = int(load_game_data[2])
            self._pokemon_game.set_load_game(self._grid_size, load_game_data[1], load_game_data[3])
            self._image_board_view.get_grid_board(self._grid_size, self.board_width)
            # convert str(time) to tuple(time)
            load_time = []
            time = load_game_data[-1][1:-1].split(',')
            for i in time:
                load_time.append(int(i))
            self._load_time = tuple(load_time)
        self.start_game()

    def get_load_time(self):
        """Returns the load time"""
        return self._load_time

    def restart_game(self):
        """Restart the current game, including game timer. Pokemon locations should persist."""
        self._pokemon_game.restart_game()
        self.start_game()

    def new_game(self):
        """Restart to a new game (i.e. new pokemon locations).
        Use the same grid size and number of pokemon as the current game.
        """
        self._pokemon_game.new_game()
        self.start_game()

    def start_game(self):
        """Same code for restart_game and new_game"""
        self._image_board_view.draw_board(self._pokemon_game.get_game())
        self._image_board_view.restart_game(self._load_time)
        self._load_time = (0,0)

    def quit(self):
        """Prompt the player via messagebox to ask whether they are sure they would like to quit.
        If no, do nothing. If yes, quit the game
        """
        answer = messagebox.askyesno("Quit Game", "Would you like to quit?")
        if answer:
            self._master.destroy()


class BoardView(tk.Canvas):
    """View of the game board of TASK_ONE"""
    def __init__(self, master, grid_size, board_width, *args, **kwargs):
        """Construct a board view from a board game.
            Parameters:
                master (tk.Widget): Widget within which the board is placed.
                grid_size (int): Integer which shows the size of the grid.
                board_width (int): Board width of the windows.
        """
        super().__init__(master, **kwargs)
        self._master = master
        self._grid_size = grid_size
        self._board_width = board_width
        self._pokemon_game = args[0]
        self._status_bar = None

        self._image = []
        self._grid_width = self._board_width / self._grid_size

    def get_bar(self, status):
        """Gets the StatusBar class
        Parameters:
            status: StatusBar class.
        """
        self._status_bar = status

    def draw_board(self, board):
        """Draw the game board of TASK_ONE.
        Parameters:
            board (str): Game string from BoardModel.
        """
        self.config(width=self._board_width, height=self._board_width)
        self.delete(tk.ALL)
        self._grid_width = self._board_width / self._grid_size

        for i in range(self._grid_size):
            for j in range(self._grid_size):
                board_layout = board[i * self._grid_size + j]

                x1 = j * self._grid_width
                y1 = i * self._grid_width
                x2 = x1 + self._grid_width
                y2 = y1 + self._grid_width

                if board_layout == UNEXPOSED:
                    self.create_rectangle(x1, y1, x2, y2, fill="#006200")

                elif board_layout == POKEMON:
                    self.create_rectangle(x1, y1, x2, y2, fill="yellow")

                elif board_layout.isdigit():
                    self.create_rectangle(x1, y1, x2, y2, fill="#90ED8F")
                    self.create_text((x1 + x2) / 2, (y1 + y2) / 2, text=board_layout)

                elif board_layout == FLAG:
                    self.create_rectangle(x1, y1, x2, y2, fill="#FF0000")
        # bind left click
        self.bind('<Button-1>', lambda e: self.left_click(e))
        # bind right click
        # right click can be either Button-2 or Button-3 depending on operating system
        self.bind('<Button-2>', lambda e: self.right_click(e))
        self.bind('<Button-3>', lambda e: self.right_click(e))

    def left_click(self, e):
        """Handle left clicking on a tile
        Parameters:
            e : Event.
        """
        coordinates = e.x, e.y
        position = self.find_position(coordinates)
        index = position[1] * self._grid_size + position[0]

        # if click pokemon, show all pokemon
        if index in self._pokemon_game.get_pokemon_locations() and self._pokemon_game.get_game()[index] != FLAG:

            for i in self._pokemon_game.get_pokemon_locations():
                self._pokemon_game.replace_character_at_index(i, POKEMON)

        # if click not pokemon, find numbers
        elif index not in self._pokemon_game.get_pokemon_locations():

            all_visible = self._pokemon_game.big_fun_search(index)
            all_visible.append(index)

            for i in all_visible:
                if self._pokemon_game.get_game()[i] != FLAG:
                    number_main = self._pokemon_game.number_at_cell(i)
                    self._pokemon_game.replace_character_at_index(i, str(number_main))

        self.draw_board(self._pokemon_game.get_game())
        self._master.update()
        self.check_game_over(index)

    def right_click(self, e):
        """Handle right clicking on a tile
        Parameters:
            e : Event.
        """
        coordinates = e.x, e.y
        position = self.find_position(coordinates)
        index = position[1] * self._grid_size + position[0]

        if self._pokemon_game.get_game()[index] == UNEXPOSED or FLAG:
            self._pokemon_game.flag_cell(index)
            self.draw_board(self._pokemon_game.get_game())
        # update the game board
        self._master.update()
        if self._pokemon_game.check_loss(index):
            self.check_game_over(index)

    def find_position(self, coordinates):
        """Converts the supplied coordinates to the position of the cell
         Parameters:
            coordinates (tuple): The coordinates of clicked.
        Returns:
            position_x, position_y(int): row and column of the coordinates.
        """
        position_x = int(coordinates[0] // self._grid_width)
        position_y = int(coordinates[1] // self._grid_width)
        return position_x, position_y

    def check_game_over(self, index):
        """Check win or lose and show the message box.
        Parameters:
            index (int): The grid of clicked.
        """
        if self._pokemon_game.check_loss(index) is True:
            messagebox.showinfo("Game Over", "You won! :D")
            self.master.destroy()
        elif self._pokemon_game.check_loss(index) is False:
            messagebox.showinfo("Game Over", "You loose!")
            self.master.destroy()


class ImageBoardView(BoardView):
    """View of the game board of TASK_TWO"""
    def draw_board(self, board):
        """Draw the game board of TASK_TWO.
        Parameters:
            board (str): Game string from BoardModel.
        """
        self.config(width=self._board_width, height=self._board_width)
        self.delete(tk.ALL)
        self._image.clear()
        self._grid_width = self._board_width / self._grid_size
        for i in range(self._grid_size):
            for j in range(self._grid_size):
                board_layout = board[i * self._grid_size + j]

                x1 = j * self._grid_width
                y1 = i * self._grid_width
                x2 = x1 + self._grid_width
                y2 = y1 + self._grid_width

                if board_layout == UNEXPOSED:
                    im = Image.open("images/unrevealed.png")
                    (width, height) = (im.width * self._board_width // (60 * self._grid_size),
                                       im.height * self._board_width // (58 * self._grid_size))
                    im_resized = im.resize((width, height))
                    ph = ImageTk.PhotoImage(im_resized)
                    self.create_image((x1 + x2) / 2, (y1 + y2) / 2, image=ph)
                    self._image.append(ph)

                elif board_layout == POKEMON:
                    pokemon = POKEMON_IMAGE[random.randint(0, 5)]
                    im = Image.open(f"images/pokemon_sprites/{pokemon}.png")
                    (width, height) = (im.width * self._board_width // (60 * self._grid_size),
                                       im.height * self._board_width // (58 * self._grid_size))
                    im_resized = im.resize((width, height))
                    ph = ImageTk.PhotoImage(im_resized)
                    self.create_image((x1 + x2) / 2, (y1 + y2) / 2, image=ph)
                    self._image.append(ph)

                elif board_layout.isdigit():
                    number = NUMBER_IMAGE[board_layout]
                    im = Image.open("images/" + number + "_adjacent.png")
                    (width, height) = (im.width * self._board_width // (60 * self._grid_size),
                                       im.height * self._board_width // (58 * self._grid_size))
                    im_resized = im.resize((width, height))
                    ph = ImageTk.PhotoImage(im_resized)
                    self.create_image((x1 + x2) / 2, (y1 + y2) / 2, image=ph)
                    self._image.append(ph)

                elif board_layout == FLAG:
                    im = Image.open("images/pokeball.png")
                    (width, height) = (im.width * self._board_width // (60 * self._grid_size),
                                       im.height * self._board_width // (58 * self._grid_size))
                    im_resized = im.resize((width, height))
                    ph = ImageTk.PhotoImage(im_resized)
                    self.create_image((x1 + x2) / 2, (y1 + y2) / 2, image=ph)
                    self._image.append(ph)

        self.bind('<Button-1>', lambda e: self.left_click(e))

        self.bind('<Button-2>', lambda e: self.right_click(e))
        self.bind('<Button-3>', lambda e: self.right_click(e))

    def right_click(self, e):
        """Handle right clicking on a tile
            Parameters:
                e : Event.
        """
        coordinates = e.x, e.y
        position = self.find_position(coordinates)
        index = position[1] * self._grid_size + position[0]

        if self._pokemon_game.get_game()[index] == UNEXPOSED or FLAG:
            self._pokemon_game.flag_cell(index)

        # update pokeball numbers
        self._status_bar._label_catches.config(
            text=f"{self._pokemon_game.get_num_attempted_catches()} attemped catches")
        self._status_bar._label_pokeballs.config(
            text=f"{self._pokemon_game.get_num_pokemon()} pokeballs left")

        self.draw_board(self._pokemon_game.get_game())
        self._master.update()
        if self._pokemon_game.check_loss(index):
            self.check_game_over(index)

    def check_game_over(self, index):
        """Check win or lose and show the message box.
        Parameters:
            index (int): The grid of clicked.
        """
        answer = ''
        if self._pokemon_game.check_loss(index):
            answer = messagebox.askyesno("Game Over", "You won! Would you like to play again?")

        elif self._pokemon_game.check_loss(index) is False:
            answer = messagebox.askyesno("Game Over", "You lose! Would you like to play again?")

        if answer is True:
            self._pokemon_game.new_game()
            self.draw_board(self._pokemon_game.get_game())
            self.restart_game((0, 0))
        elif answer is False:
            self._master.destroy()

    def restart_game(self, load_time):
        """If the player choose try again, restart game"""
        if load_time == (0,0):
            self._status_bar.reset_time()
        else:
            self._status_bar.reset_load_time(load_time)
        self._status_bar._label_catches.config(
            text=f"{self._pokemon_game.get_num_attempted_catches()} attemped catches")
        self._status_bar._label_pokeballs.config(
            text=f"{self._pokemon_game.get_num_pokemon()} pokeballs left")

    def get_grid_board(self, grid_size, board_width):
        """Get the updated grid size and board width from the load game
        Parameters:
            grid_size (int): Integer which shows the size of the grid.
        """
        self._grid_size = grid_size
        self._board_width = board_width


class StatusBar(tk.Frame):
    """Draw the status bar for TASK_TWO"""
    def __init__(self, master, num_pokemon, **kw):
        """Construct a status bar view.
        Parameters:
            master (tk.Widget): Widget within which the board is placed.
            num_pokemon (int): The number of pokemaons.
        """
        super().__init__(master, **kw)
        self._master = master
        self._num_pokemon = num_pokemon

        self._frame1 = tk.Frame(self._master)
        self._label_catches = tk.Label(self._frame1, text="0 attemped catches")
        self._label_pokeballs = tk.Label(self._frame1, text=f"{self._num_pokemon} pokeballs left")

        self._frame2 = tk.Frame(self._master)
        self._minutes_seconds = tk.Label(self._frame2, text="0m 0s")

        self._current_time = int(time.time())
        self.clock_time()

    def clock_time(self):
        """Calculate the elapsed time."""
        self._timer = int(time.time()) - self._current_time
        self._minutes = self._timer // 60
        self._seconds = self._timer % 60
        self._minutes_seconds.config(text=f"{self._minutes}m {self._seconds}s")
        self._master.after(1000, self.clock_time)

    def reset_load_time(self, load_time):
        """Reset the time for the load game"""
        self._current_time = int(time.time()) - load_time[0]*60 - load_time[1]

    def reset_time(self):
        """Reset the time"""
        self._current_time = int(time.time())

    def get_time(self):
        """Returns the minutes and seconds"""
        return self._minutes,self._seconds

    def draw_status_bar(self):
        """Drwa status bar"""
        # pokeball image
        image = get_image("images/full_pokeball")
        self._label_pokeball = tk.Label(self._master, image=image)
        self._label_pokeball.image = image
        self._label_pokeball.pack(side=tk.LEFT, expand=1, anchor = tk.E)

        # catches number label

        self._frame1.pack(side=tk.LEFT, expand=1)

        self._label_catches.pack(side=tk.TOP, anchor = tk.W)

        self._label_pokeballs.pack(side=tk.TOP, anchor = tk.W)

        # time image
        image_clock = get_image("images/clock")
        label_clock = tk.Label(self._master, image=image_clock)
        label_clock.image = image_clock
        label_clock.pack(side=tk.LEFT, expand=1, anchor = tk.E)

        # time label

        self._frame2.pack(side=tk.LEFT, expand=1)

        time_elapsed = tk.Label(self._frame2, text="Time elapsed")
        time_elapsed.pack(side=tk.TOP)

        self._minutes_seconds.pack(side=tk.TOP)

def get_image(image_name):
    """Get a image file based on capability.

    If a .png doesn't work, default to the .gif image.
    """
    try:
        image = tk.PhotoImage(file=image_name + ".png")
    except tk.TclError:
        image = tk.PhotoImage(file=image_name + ".gif")
    return image


def main():
    """Create the windows"""
    root = tk.Tk()
    root.title("Pokemon: Got 2 Find Them All!")
    PokemonGame(root)
    root.update()
    root.mainloop()


if __name__ == "__main__":
    main()



