# ASE-Chat

## Intro

The application consists of two components: The server and the client.

## Server component

Starting the server component:

`java -jar ase-chat.jar Server`

Arguments:

| Argument 	        | Description                 	        |
|-------------------|--------------------------------------|
| `--host`   	      | Host (default 0.0.0.0)      	        |
| `-h`       	      | Alias of `--host`             	      |
| `--port`   	      | Server port (default 25531) 	        |
| `-p`       	      | Alias of `--port`             	      |
| `--key`    	      | Room password (optional)    	        |
| `-k`       	      | Alias of `--key`              	      |


Starting the client component:

`java -jar ase-chat.jar Client`

Arguments

| Argument     | Description            |
|--------------|------------------------|
| '--username' | Username               |
| '-u'         | Alias of '--username'  |
| '--host'     | Ip adress of server    |
| '-h'         | Alias of '--host'      |
| '--port'     | Server port            |
| '-p'         | Alias of '-port'       |
| '--password' | Password of the server |
| '--key'      | Alias of '--password'  |
| '-k'         | Alias of '--password'  |
| '--color'    | Color of username      |
| '-c'         | Alias of '--color'     |

Once you're connected to the server:

In order to send a message just type in what you have to say

Possible in chat commands:

| Command         | Description                                                                |
|-----------------|----------------------------------------------------------------------------|
| /chat leave     | Leave chat room                                                            |
| /chat quit      | Leave chat room                                                            |
| /color message  | Changes the color of all following chat messages until it is changed again |
| /color username | Changes the color of the username                                          |
| /info           | Displays the number of clients connected to the user                       |
| /username       | Change your username                                                       |

