import socket
import random

HOST = ''  # Listen on all available interfaces
PORT = 5050

# Create a TCP socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the address and port
server_socket.bind((HOST, PORT))

# Listen for incoming connections
server_socket.listen()

# Accept a connection
client_socket, client_address = server_socket.accept()

while True:
    # Generate a random number between 1 and 666
    num = str(random.randint(1, 666))

    # Send the random number as a string to the client
    client_socket.sendall(num.encode())

    # Wait for 1 second before sending the next random number
    time.sleep(1)
