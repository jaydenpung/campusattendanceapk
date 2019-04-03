# Campus Attendance APK


## Getting Started

Gradle version 4.10.1
Android Plugin Version 3.3.2

## Profile

All field editable except user id and staff position
Password field will not be updated unless changed

## Schedule

By default will show all the lessons to be participated by student/staff.
To only show lesson belong to the user on a certain date, click the change date button and pick a date
Each lesson can be clicked, displaying further details:
    For student, there will be a "tick" to indicate the student has attended the class, else there will be none
    For staff, there will be a "generate qr code" button. When clicked, a qr code will be generated and tied to that lesson. If a qr code has already been generated before, clicking the "generate qr code" button again will simply generate the same qr code.
    Student has a floating camera button on bottom right that can be accessed from most pages. Clicking this button allow them to scan the qr code generated, indicating they have attended that lesson. If a student scan a QR code of a lesson that does not belong to the student, an "Invalid lesson" toast will appear.

## Attendance

For student - A summary of subjects taken by the student, along with attendance percentage (TO DATE)
For staff - A list of student in class taught by the staff, along with each student's attendance percentage (TO DATE)
*TO DATE means only include lessons today and before today

## Notification

Shows a list of messages receieved through notifications. If the app is active when push notigication is sent, a toast will appear instead of a notification in notification tray
Staff can send send message by clicking new message.
When creating new message:
There are two spinners: "lessons" and "subject". Only one of the spinners can be selected at the same time. If "lesson" is chosen, the message will be sent to student participating in that lesson. If "subject" is chosen, all student who have lessons in that subject will receive the message.


## Technical explanations
    * __ApiRoute__ - List of strings that helps you with the url of api calls
    * __LoginActivity__ - The launcher activity, after login, a token will be received from api, and saved into sharedpreferences along with userId and userType
    * __MainActivity__ - The main activity, contains multiple fragments that can be navigated to. After login, this is the activity we will be redirected to. Device token for FCM push notification is also generated here if needed. The code for scanning QR code is here too.
    * __CustomListViewAdapter__ - In ListView, we are displaying more than one column per item. This adapter help us to put our object attributes into different columns. Column 0 is reserved for on click id, so is hidden by default.  
    * __MessageActivity__ - This is the activity called to create and send notification 
    * __LessonActivity__ - Activity called upon clicking of the lesson in Schedule list view. Contains generate qr code function.
    * __MyFirebaseMessagingService__ - Used to keep track of FCM push notification token; If the token refreshed, code in here will update our server about the new token
    * __DisplayQrActivity__ - Very simple portion of code to read QR code with QRQEncoder
    * __VolleyUtils__ - Exposed publicly to be called to make api calls
    * __VolleySingleton__ - Singleton structure of Volley allow reuse through requestqueue, so we dont have to define new Volley over and over again for every api call
    * __VolletResponseListener__ - Self implemented interface to allow override of listener event, onError and onResponse, so we can use the value returned by api calls at where we called it
    * __DatepickerFragment__ - Standard fragment of datepicker
    * __ProfileFragment__ - Shows and allow edit of user profile
    * __AttendanceFragment__ - Shows the summary of attendance of the user
    * __ScheduleFragment__ - Display list of lessons belong to the user, able to filter by date
    * __NotificationFragment__ - Shows the list of message from push notifications
