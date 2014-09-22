Feasibility Study
=====

### The Group

Davis Engeler, Michael Hetzel, Jesse Leonard, John Sloan

### The Client

Single site child development center (daycare center) looking for a digital system to organize and sync day-to-day routines to move away from their pen and paper system.

### Project Description

Most day care facilities use an inefficient pen and paper system. Our program will allow a transition into a user-friendly system that will help the personnel and the customers. One of the main benefits will be a consistently up-to-date view of children on site and fix organizational downfalls of the old paper system. This will be achieved through an automated sign in/out system for all children attending the day care facility. It will also improve communication between teachers and parents through classroom changes, issues that may arise, reminders from parents, illness updates and missed days, and any special need requirements for children. If any particular child is in need of timely or frequent medication it will alert the correct teacher and remind them to administer appropriately. This will increase accountability, improve and streamline record keeping for the day care, and provide an overall increase in care for the children.

### Benefits

Our project helps alleviate the accountability issues from a daycare center's day to day child organization routines. It offers a streamlined workflow for teachers and parents, with secure and simple sign in/out for the children.


### A Preliminary Requirements Analysis

Main features for each different type of views:

- **Main Lobby Access** *(tablet that allows parents to sign in their children in the lobby)*
	- RFID reader to quickly pull up child information
	- Sign children in / out
	- View child current classroom
	- Update notes for child
	- Ability to sync information to the database

- **Teacher Client**
	- Runs on small tablets or phones
	- Ability to move children to another class
	- Supports notifications for specific events
		- Medication times
		- Signed out
		- Messages / Notes from parents
	- Ability to send messages to parents about specific kids
	- Ability to send messages to all parents for classroom events
	- Accountability checks: Visual checklist for every child in the class
	
- **Administrative Client**
	- Web client
	- Different account types for particular areas of the system
	- View of all information
	- Staff management
		- Add new staff user accounts
		- Change their permissions
	- Child / Parent registration
		- Picture of parent and their children
		- Get information of the parent to create their account and link the child account to the parent
		- Assign an RFID key fob for login purposes
	- Manage [simulated] payment information for parents

- **Parent Client**
	- Phone app or web based (Some features require compatible hardware)
	- View child current classroom
	- Notification for child classroom changes
	- Add notes or timed reminders for medication, etc.
	- View teacher's notes on children
	- View teacher's notes on classroom activities
	- Reminders for updating information or annual registration
	- Add / change payment methods

- **Database**
	- Child, parent, teacher, and staff information
		- Payment, contact, other official information for parents and children
		- Medical needs, etc for children
		- Login information and permissions for all types of accounts
	- Child, parent, and default teacher links (know which children go with which parents and teachers)
	
- **Server**
	- Houses our database
	- Handles most of the processing
	- Interface with other clients through API
	- Host and serve files for web client


### Technical Requirements - Feasibility

We will be using the Android platform for the majority of the system. An Android Tablet with NFC enabled and an Android phone, with or without NFC, will be required in order for testing and implementation.  The development of the Android apps will be in Eclipse with the appropriate Android SDK.  Multiple RFID tags will be utilized for the sign-in/out process.  Web server will be needed for the process and delivery of the web page.  HTML, CSS, PHP, and Javascript (JQuery) is how the web page will be developed.  In addition, MySQL will be how the database is implemented for all the stored records.  

### Scope

The administrative client will allow new users to be created. Either parent, student, or staff. Permissions and information can be set and sent to the database. That database information and permission will determine a particular user's access to different features and clients. 

RFID tags will be assigned to parents allow them to quickly log in when dropping their kids off. This will access their account information from the database and give options based on their children's accounts. Sign the children in or out and add daily notes to the child profiles. The information is synced with the database and can be access by the day care staff.

Each teacher's client is updated when one of their students is signed in or out and notifies them of any timed reminders that the parents or other staff has set for the children. They can add new notes or communicate with the parents. Those notifications are sent either through email or push notification (if the parent has a supported device). They can transfer single or groups of children to other classrooms/teachers. This will all be synced and accessible to the parents in real time from their computer or Android phone and when they scan their RFID tag at pickup time.

The system will keep track of parent sign out times based off when the parent scans their RFID tag and signs their children out. This allows the day care staff to analyze parents who might be consistently late for possible overage charges.

Our system will *not* tie into employee payroll, time management system, or their current website login information. This is mainly due to the fact that, in this project, there's no true systems that are already in place. We would have to create the "existing" systems to tie into.

### Risk Analysis

We may come across communication issues with the different platforms, including web and android. To avoid these possible issues, we plan to build the main functionalities into the server and develop an API. The clients will simply use the API to send/retrieve information while the server handles most of the functionality. This will also avoid wasting time with developing the same features on different platforms.

A risk in Android development is the fragmentation of hardware and software compatibility. The main issued would rise when creating the parent client since we will build around the daycare center's devices. To help with this, we will develop the parent client for a version of Android with a high level of adoption and compatibility.

### Conclusion

We feel that the risks of this project do not pose any unsolvable threats to the time restraints of this project. We have access to the necessary technology to successfully create and develop our software package.











