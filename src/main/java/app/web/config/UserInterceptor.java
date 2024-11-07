//package app.web.config;
//
//import app.web.Service.UserService;
//import app.web.persistence.entities.UserEntity;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import java.util.Optional;
//
//@Component
//public class UserInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private UserService userService;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        HttpSession session = request.getSession();
//        if (session != null && session.getAttribute("user_session_id") != null) {
//            Long userId = Long.parseLong(session.getAttribute("user_session_id").toString());
//            Optional<UserEntity> optionalUser = userService.getUserById(userId);
//            if (optionalUser.isPresent()) {
//                request.setAttribute("user", optionalUser.get());
//                System.out.println("User found and set in request attributes");
//            } else {
//                System.out.println("User not found for ID: " + userId);
//                return false;
//            }
//        } else {
//            System.out.println("Session or user_session_id attribute is null");
//        }
//        return true;
//    }
//
//}
