# CoroSechat
## Author: [Raphael Dray](https://www.linkedin.com/in/raphaeldray/)

CoroSechat is a chat server over a Secure TCP connection with SmartCard Challenge-Response Authentication based.

It is written in **Java 17** including libraries like so:
* [ActiveJDBC](https://javalite.io/activejdbc)

---
## Implementations:
Here is described some implemented features:
* Multi-Threaded Server (Threads for Accepting Connection and the Main thread used to handle Administration Management)
* Server Administration Management
  * _/kill username_<p style="padding:0 0 0 60px;">-- **Kill a connected user**</p>
  * _/killall_<p style="padding:0 0 0 60px;">-- **Kill all connected users**</p>
  * _/halt_<p style="padding:0 0 0 60px;">-- **Stop the server**</p>
  * _/deleteAccount username_<p style="padding:0 0 0 60px;">-- **Delete an existing account**</p>
  * _/addAccount username password_<p style="padding:0 0 0 60px;">-- **Add an account**</p>
  * _/loadBDD_<p style="padding:0 0 0 60px;">-- **Load the Database**</p>
  * _/saveBDD_<p style="padding:0 0 0 60px;">-- **Save the Database**<p>
* Regular User Server Commands:
  * _/logout | /exit_<p style="padding:0 0 0 60px;">-- **Disconnect / Quit**</p>
  * _/list_<p style="padding:0 0 0 60px;">-- **List current connected users**</p>
  * _/msg username message_<p style="padding:0 0 0 60px;">-- **Send a Private Message to _username_**</p>
* Hand-Crafted Logger
* PostgreSQL Database Persistence in conjunction with ActiveJDBC ORM

---
## How it works ?:
Launch the server and connect to it, simple.
```
INFO: 2022-01-22 11:47:18.901 ServerChat: Listening on port 4444
INFO: 2022-01-22 11:48:00.937 ServiceChat: A new user has initiated a connection on IP 0:0:0:0:0:0:0:1
INFO: 2022-01-22 11:48:05.215 ServiceChat: A new user has been created with username Raphael
INFO: 2022-01-22 11:48:12.608 ServiceChat: A new user has initiated a connection on IP 0:0:0:0:0:0:0:1
INFO: 2022-01-22 11:48:19.588 ServiceChat: [Raphael] Coucou
Salut !
INFO: 2022-01-22 11:48:27.503 ServiceChat: <ADMIN> Salut !
/list
<SYSTEM> List of connected users: 
<SYSTEM> Raphael
INFO: 2022-01-22 11:49:32.777 ServiceChat: <ADMIN> /list
/killall
INFO: 2022-01-22 11:49:43.169 ServiceChat: Raphael has been killed by the administrator.
INFO: 2022-01-22 11:49:43.173 ServiceChat: <ADMIN> /killall
/halt

Process finished with exit code 0
```

---
## Improvements:
Some improvements to come further:
- [ ] In-Docker PostgreSQL Deployment
- [ ] PostgreSQL Database Persistence
- [ ] Message Reply
- [ ] Password Hashing