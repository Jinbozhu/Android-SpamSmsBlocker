Android App: Spam SMS Blocker
==========
This is an Android app that can replace the original messaging app to send, receive and block unwanted messages.

Messages, contacts and phone numbers are maintained in SQLite database. A field in the phone number table is used to mark if a phone number is in the blacklist or not. If it's in the blacklist, it will be blocked, otherwise it will get through.
Functionalities:
- Delete a message
- Delete a conversation thread
- Create/delete/modify contacts
- Add to/remove from the blacklist
- Add to/remove from the white list
- Make a copy of the message
- etc.

Future work
-------------
- Develop an advanced filter system which can take advantage of NLP to detect spam SMS
- Develop more functionalities, such as restore deleted messages, import/export contacts and messages