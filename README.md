Day-Care
-----

**CSCI 540:** Software Engineering Team 1 _(Davis Engeler, John Sloan, Michael Hetzel, Jesse Leonard)_

> Child management system for day care centers. Quickly sign children in and out. 
> Leave notes for children. Keep track of classroom changes. Parent notifications
> for events, issues, or complements. This document will be tweaked as we go.

Preliminary Requirements Analysis:
----

Main features for each different type of views:

- **On-Site Public Client** 	
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
	- Time scheduling
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



Java Projects on GitHub
----
> It looks like we have the projects working correctly with the new .gitignore so only the classes are synced. If you need to get the repository set up and imported into Eclipse correctly, ask Davis Engeler, Michael  Hetzel, or John Sloan.