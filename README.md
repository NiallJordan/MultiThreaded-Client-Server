# MultiThreaded-Client-Server

## Process
1) Start database (XAMP,MAMP)
2) Start the server
3) Start a Client


## Server: 
Server only accepts requests from registered students. Server creates a thread once a request is received and
is validated. Received radius from the Client and calculates the Area Of Circle. Sends the AoC to the client over
DataStream.
## Client: 
A Client can only press the login button after entering a number. They will receive a message whether that 
STUD_ID is valid from the Server. Once logged in the user can access the AoC panel below, where they can send the 
radius to the Server to calculate the area of the circle based on the request sent.

## Database: 
Assign2
## Table:
students
## Structure: 
- SID (INT(2), UK), 
- STUD_ID (INT(8)), 
- FNAME (VARCHAR(20)),
- SNAME (VARCHAR(20)), 
- TOT_REQ(INT(8))


# Resources: 
The jar files, sql files and a jpeg of the uml diagram are included in the resources folder. 
