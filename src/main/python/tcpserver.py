import sys
import logging
import socket
import random
import time

logging.basicConfig(level=logging.INFO)

args = sys.argv[1:]

HOST = ''
PORT = int(args[0])

while True:
    logging.info(f'Opening server on host {HOST} and port {PORT}.')

    # Create a TCP socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, PORT))
    server_socket.listen()

    logging.info(f'Started listening on host {HOST} and port {PORT}.')

    client_socket, client_address = server_socket.accept()

    logging.info(f'Accepted connection on address {client_address}.')

    while True:
        # Generate a random number between 1 and 666
        num = str(random.randint(1, 666))

        try:
            # Send the random number as a string to the client
            client_socket.sendall(num.encode())
        except:
            logging.info(f'Connection CLOSED on address {client_address}.')
            break

        # Wait for 1 second before sending the next random number
        time.sleep(1)

    logging.info(f'Releasing connection resources of {client_address}.')

    client_socket.close()
    server_socket.close()

logging.info(f'Finished')
