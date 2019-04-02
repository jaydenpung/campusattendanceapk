package erp.droid.studentattendancesystem;

public class ApiRoute {
    public String SERVER = "http://37.247.116.48:5000/";
    public String LOGIN = this.SERVER + "api/v1/auth/login";
    public String GET_PROFILE = this.SERVER + "api/v1/mobileapi/getProfile";
    public String GET_TIMETABLE = this.SERVER + "api/v1/mobileapi/getTimetable";
    public String GET_LESSON = this.SERVER + "api/v1/mobileapi/getLesson";
    public String GET_SUBJECT_ATTENDANCE = this.SERVER + "api/v1/mobileapi/getSubjectAttendance";
    public String GET_NOTIFICATION_LIST = this.SERVER + "api/v1/mobileapi/getNotificationList";
    public String UPDATE_PROFILE = this.SERVER + "api/v1/mobileapi/updateProfile";
    public String UPDATE_LESSON_ATTENDANCE = this.SERVER + "api/v1/mobileapi/updateLessonAttendance";
    public String CREATE_NOTIFICATION = this.SERVER + "api/v1/mobileapi/createNotification";
    public String GENERATE_LESSON_ATTENDANCE_QR_CODE = this.SERVER + "api/v1/mobileapi/generateLessonAttendanceQrCode";
}
