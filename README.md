# FunGame
simple game about memory


## Prepare  
01 Tomacat 8  
02 MySQL  
03 JDK8  
04 NetBeans 8  

##Build  
01 Open netbeasns 8  
02 pickup GameWeb and GameServer project    
##Deploy  
01 Change DB_Host in Settings.java  
02 Change GameServerIP in GameWeb\js\Define.js  
03 ReBuild code in netbeans  
04 Copy GameWeb.war to tomcat/webapps  
##Run  
01 Browse player page (http://IP:8080/GameWeb/GameClient.html)  
02 Browse monitor page (http://IP:8080/GameWeb/GameMonitor.html)  
03 Go to GameServer folder and excute(java -jar dist/GameServer.jar)  
##Client    
Build web pages with JQuery and Bootstrap  
01 Web page for player  
youc an  
-sign up(you can named whatever you want)  
-login  
-create room(you can start a game immediately or wait to other people join this room to start)  
-join room(join a room that already create)  
-exit room  
-play the game
  
02 Web page for monitor  
you can  
-login(fixed login name and passwod are monitor)  
-watch room(watch game status of all players in the room that you picked up)  
-show history(watch used time of a game)  
-show game statue real time  
##Server  
Build with java  
