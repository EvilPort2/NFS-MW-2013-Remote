import socket
from time import *
import os
import pyautogui as gui
from threading import Thread
from win32api import GetKeyState 
from win32con import VK_NUMLOCK 
from my_input import PressKey, ReleaseKey

ACCELERATE = 0x1E
NITRO = 0x2A
LEFT = 0xCB
RIGHT = 0xCD
REVERSE = 0x2C
BRAKE = 0x39
ESCAPE = 0x01
CHANGE_CAR = 0x12

keys = [ACCELERATE, NITRO, LEFT, RIGHT, REVERSE, BRAKE, ESCAPE]

def num_lock_off():
	print(GetKeyState(VK_NUMLOCK))
	if GetKeyState(VK_NUMLOCK) == 1:
		gui.press('numlock')
	print(GetKeyState(VK_NUMLOCK))

def take_turn(direction, y, s):
	PressKey(direction)
	sleep(abs(y) * float(s / 100))
	ReleaseKey(direction)

def move_car(x, y, z, s):
	if x < -2 and x > -8:
                Thread(target=PressKey, args=(ACCELERATE,)).start()
		#thread.start_new_thread(PressKey, (ACCELERATE,))

	if x > 2 and x < 8:
                Thread(target=PressKey, args=(REVERSE,)).start()
		#thread.start_new_thread(PressKey, (REVERSE,))
		
	if y < -2:
                Thread(target=take_turn, args=(LEFT, y, s)).start()
		#thread.start_new_thread(take_turn, (LEFT, y, s))		

	if y > 2:
                Thread(target=take_turn, args=(RIGHT, y, s)).start()
		#thread.start_new_thread(take_turn, (RIGHT, y, s))

	if x < -8:
                Thread(target=PressKey, args=(NITRO,)).start()
		#thread.start_new_thread(PressKey, (NITRO,))

	if x > 8:
                Thread(target=PressKey, args=(BRAKE,)).start()
		#thread.start_new_thread(PressKey, (BRAKE,))	

	if abs(x) < 2:
		ReleaseKey(ACCELERATE)
		ReleaseKey(REVERSE)
		ReleaseKey(BRAKE)
		ReleaseKey(NITRO)

	if abs(y) < 2:
		ReleaseKey(LEFT)
		ReleaseKey(RIGHT)

def hand_brake():
	PressKey(BRAKE)
	sleep(0.08)
	ReleaseKey(BRAKE)

def game_menu():
	PressKey(ESCAPE)
	sleep(0.08)
	ReleaseKey(ESCAPE)

def change_car():
	PressKey(CHANGE_CAR)
	sleep(0.08)
	ReleaseKey(CHANGE_CAR)

def start_race():
	def accelerate():
		PressKey(ACCELERATE)
		sleep(0.08)
	def reverse():
		PressKey(REVERSE)
		sleep(0.08)
	Thread(target=accelerate, args=()).start()
	Thread(target=reverse, args=()).start()

def main():
	ip = input("Enter this machine's IP address: ")
	while True:
		try:
			port = int(input("Enter port number: "))
		except KeyboardInterrupt:
			print("\nbye!")
			exit(0)
		except:
			continue
		break

	while True:
		if os.name == 'nt':
			os.system("cls")
		else:
			os.system("clear")

		try:
			serv = socket.socket()
			serv.bind((ip, port))
			print("Socket bound to %s:%d" % (ip, port))
			serv.listen(1)
			print("Listening for one connection")
			conn, addr = serv.accept()
			print("Accepted connection from %s:%d" % (addr[0], addr[1]))

		except KeyboardInterrupt:
			print ("Keyboard Interrupt")
			conn.close()
			serv.close()
			exit(1)

		try:
			while True:
				data=conn.recv(4096)
				mdata = data.decode('utf-8').split("\n")
				conn.sendall(b'Received\n')
				x, y, z, s, operation = 0, 0, 0, 0, ""
				flag = 0
				if len(mdata[-2].split(" ")) == 5:
					x, y, z, s, operation = mdata[-2].split(" ")
					try:
						x, y, z, s = float(x), float(y), float(z), float(s)
					except ValueError:
						continue
				print("X = %.3f, Y = %.3f, Z = %.3f, s = %.1f, operation = %s" % (x,y,z,s,operation))
				
				if operation == "ControlOn":
					move_car(x, y, z, s)

				elif operation == "Handbrake":
                                        Thread(target=hand_brake, args=()).start()
					#thread.start_new_thread(hand_brake, ())
					#hand_brake()
                                           
				elif operation == "Menu":
                                        Thread(target=game_menu, args=()).start()
					#thread.start_new_thread(game_menu, ())

				elif operation == "ChangeCar":
                                        Thread(target=change_car, args=()).start()
					#thread.start_new_thread(change_car, ())

				elif operation == "StartRace":
					start_race()

				elif operation == "ControlOff":
					continue

		except KeyboardInterrupt:
			conn.close()
			serv.close()
			print ("bye!")
			for k in keys:
				ReleaseKey(k)
			exit(1)

		except TimeoutError:
			conn.close()
			serv.close()
			print ("Client did not respond in time or has disconnected.\n\n")
			input ("Press ENTER to continue....")
			for k in keys:
				ReleaseKey(k)

		except IndexError:
			conn.close()
			serv.close()
			print ("End of Stream!")
			for k in keys:
				ReleaseKey(k)
			while True:
				ch = input("Enter y to restart streamimg or n to stop the program: ")
				if ch in ('y', 'Y', 'n', 'N'):
					break

			if ch == 'y' or ch == 'Y':
				continue
			elif ch == 'n' or ch == 'N':
				print('bye!')
				exit(0)
				
num_lock_off()	
main()
