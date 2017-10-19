An android client app that uses its accelerometer to control cars in NFS MW 2013 video game for PC.

# Need For Speed Most Wanted 2013 Remote Controller
This repo consists of the python server that is needed to run on the PC and the android client that sends accelerometer data to the server that uses the data to control the cars.

# Concepts used here
1. Socket programming
2. DirectInput/DirectX Input using Python
3. Real time reading and sending of accelerometer data

# How it works
1. First a server is created using python start-server.py which creates a socket.
2. Start the android app and enter the ip and port number of the server.
3. Real time accelerometer data is sent to the server.
4. The python program takes the input from the accelerometer and uses some defined logics to move the car or control the game using DirectInput scan codes.

# Requirements
1. Python 3
2. pyautogui
3. pywin32

# Features included as of today
1. Sensitivity factor that can be different for different users and cars can be easily set by user.
2. Tilting the phone totally towards the front or back causes the car to use nitro or handbrakes respectively.
3. Game menu can easily be accessed from the phone.
4. Phone's screen brightness can be brought down to zero on one click.