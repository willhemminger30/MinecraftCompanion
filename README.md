# Minecraft Companion Application
## Purpose
The pupose of this application is to serve as a companion application, running alongside Minecraft.

The goal is that it will allow the user to store/retrieve both text and GPS coordinates, with the additional functionality of being able to teleport to specific saved coordinates.
## Usage
### Setup
To set up the application, configure the *properties.txt* file with the appropriate Minecraft username, path to the Minecraft log file, and session that you want the data associated with.  You can also choose what set of characters will activate the companion application, known as the *companionPrefix*.

Once this is complete, ensure that you are in a running Minecraft server or singleplayer game, and run **java -jar MinecraftCompanion.jar** from the command line (or create a batch file to do the same).  This will engage the Minecraft Companion application, and it will begin reading the log file for your current session.

### Sending Commands
To use the application, send a whisper message to yourself in the Minecraft chat.  This is accomplished through the **/w \<UserName\>** command.  

E.g., **/w \<UserName\> >> data store topic This is the text I want to associate with this topic.** 


Pay attention to the syntax used.  The command begins with the *companion prefix*, **>>**, followed by the word **data**, which indicates that we will perform some operation using text data.  Next is the **store** sub-command, which declares that the following text will be stored under the respective **topic**.  After the topic is chosen, the text that forms the payload for the message is written, "**This is the text I want to associate with this topic.**"

The available commands at this time are **data** and **coord**.  The available sub-commands for **data** are as follows:
- **store**
  - store text information to be retrieved at a later time
  - Storing multiple text strings with the same topic will append them to that topic
  - ex: **data store topic1 This is topic 1.**
- **get**
  - retrieve text information using matching category/topic
  - ex: **data get topic1**
- **list**
  - list available topics/categories where text is stored
  - ex: **data list**
- **export**
  - export stored text to a .txt file
  - ex: **data export**

The available sub-commands for **coord** at this time are as follows:
- **store**
  - store coordinate information with associated id and x,y,z values
  - storing multiple coordinates with the same id will overwrite that coordinate
  - ex **coord store id1 100,100,100**
- **get**
  - retrieve coordinate information using associated id
  - ex: **coord get id1**
- **list**
  - list stored coordinate ids
  - ex: **coord list**
- **export**
  - export coordinate information to a .csv file
  - ex: **coord export**
- **tp**
  - teleport player to coordinate using id (if they have the teleport permissions)
  - ex: **coord tp id1**

### Exiting the Companion
To exit the companion, there are two options currently available.  In a singleplayer world, a user can either:
- Exit the world
- Send the **EXIT COMPANION** command (case sensitive)

In a multiplayer server, the user *must* use the  **EXIT COMPANION** command.

Upon exit, the user's information for that session will be saved, regardless of if they used the export commands.  When they start the application on subsequent occasions, the exiting information for the associated SessionID will be loaded back into memory, and the user can continue to use prior data.

There is currently no built-in command for deleting information.  However, through manipulation of the generated sessionID.txt file (after the companion exits), one can remove stored data and coordinates.



